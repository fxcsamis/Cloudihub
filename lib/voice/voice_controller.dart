import 'dart:async';
import 'dart:math';
import 'package:flutter_tts/flutter_tts.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:speech_to_text/speech_to_text.dart';
import '../orb/orb_widget.dart';
import '../audio/duplex_audio_session.dart';

/// Ports app.js's state machine onto native STT/TTS + the always-on duplex
/// audio session. No backend/LLM wired in yet, same as the web build —
/// unrecognized speech gets the same placeholder reply until that's added.
class VoiceController {
  VoiceController(this.orb);

  final OrbWidgetState orb;
  final DuplexAudioSession audio = DuplexAudioSession();
  final SpeechToText stt = SpeechToText();
  final FlutterTts tts = FlutterTts();
  final _rng = Random();

  static const _listenThreshold = 0.10;
  static const _bargeInThreshold = 0.14;
  static const _silenceToThinkMs = 700;
  static const _strayNoiseMs = 2500;

  OrbState current = OrbState.boot;
  bool _sttAvailable = false;
  String _finalTranscript = '';
  int _lastVoiceTime = 0;
  bool _heardSpeechThisTurn = false;
  StreamSubscription<double>? _levelSub;

  Future<void> init() async {
    await Permission.microphone.request();

    _sttAvailable = await stt.initialize(
      onStatus: _onSttStatus,
      onError: (_) {},
    );

    _setState(OrbState.boot);
    orb.activate();
    _waitForBoot();
  }

  void _waitForBoot() {
    if (orb.isBooted) {
      _setState(OrbState.idle);
      _beginListening();
      return;
    }
    Future.delayed(const Duration(milliseconds: 50), _waitForBoot);
  }

  Future<void> _beginListening() async {
    await audio.start();
    _levelSub = audio.levelStream.listen(_onLevel);
    _startRecognition();
  }

  void _onSttStatus(String status) {
    // Mirrors app.js's recognition.onend restart loop — most STT engines
    // auto-stop a session after a pause, so keep re-arming it to stay
    // effectively always-on until we're disabled.
    if ((status == 'done' || status == 'notListening') &&
        current != OrbState.disabled &&
        current != OrbState.disabling) {
      _startRecognition();
    }
  }

  void _startRecognition() {
    if (!_sttAvailable || stt.isListening) return;
    // NOTE: speech_to_text's listen() signature has changed across major
    // versions (some moved partialResults/cancelOnError/listenMode into a
    // single `listenOptions:` object). Check the installed package version
    // against pub.dev if this doesn't match — the important bits are just
    // "keep listening continuously" and "give me final results".
    stt.listen(
      onResult: (result) {
        if (result.finalResult) {
          _finalTranscript += '${result.recognizedWords} ';
        }
      },
      listenFor: const Duration(minutes: 5),
      pauseFor: const Duration(seconds: 30),
    );
  }

  void _onLevel(double level) {
    orb.setAudioLevel(level);
    final now = DateTime.now().millisecondsSinceEpoch;
    final talking = level > _listenThreshold;
    if (talking) {
      _lastVoiceTime = now;
      _heardSpeechThisTurn = true;
    }

    if (current == OrbState.speaking && level > _bargeInThreshold) {
      _bargeIn();
    } else if (current == OrbState.idle && talking) {
      _setState(OrbState.listening);
    } else if (current == OrbState.listening) {
      if (!talking && _heardSpeechThisTurn && now - _lastVoiceTime > _silenceToThinkMs) {
        _goThink();
      } else if (!talking && !_heardSpeechThisTurn && now - _lastVoiceTime > _strayNoiseMs) {
        _setState(OrbState.idle);
      }
    }
  }

  void _setState(OrbState s) {
    current = s;
    orb.setOrbState(s);
  }

  Future<void> _goThink() async {
    _setState(OrbState.thinking);
    final transcript = _finalTranscript.trim().toLowerCase();
    _finalTranscript = '';
    _heardSpeechThisTurn = false;

    await Future.delayed(Duration(milliseconds: 500 + _rng.nextInt(700)));
    _handleTranscript(transcript);
  }

  void _handleTranscript(String transcript) {
    // Demo trigger words for testing the animations without a backend yet.
    if (RegExp(r'\berror\b|ভুল|বাগ').hasMatch(transcript)) {
      _goError();
      return;
    }
    if (RegExp(r'\bdisable\b|\bdisabled\b|turn off|shut down|\boff\b|বন্ধ').hasMatch(transcript)) {
      _goDisable();
      return;
    }
    // No backend yet — fixed placeholder reply until an LLM is wired in.
    _speak('ARKITAK is alive, my king.');
  }

  Future<void> _goError() async {
    _setState(OrbState.error);
    await Future.delayed(const Duration(milliseconds: 2200));
    if (current == OrbState.error) _setState(OrbState.idle);
  }

  Future<void> _goDisable() async {
    _setState(OrbState.disabling);
    await tts.stop();
    await Future.delayed(const Duration(milliseconds: 1400));
    _setState(OrbState.disabled);
    await audio.stop();
    await _levelSub?.cancel();
  }

  void _bargeIn() {
    tts.stop();
    _finalTranscript = '';
    _heardSpeechThisTurn = true;
    _lastVoiceTime = DateTime.now().millisecondsSinceEpoch;
    _setState(OrbState.listening);
  }

  Future<void> _speak(String text) async {
    _setState(OrbState.speaking);
    tts.setCompletionHandler(() {
      if (current == OrbState.speaking) _setState(OrbState.listening);
    });
    tts.setErrorHandler((_) {
      if (current == OrbState.speaking) _setState(OrbState.listening);
    });
    await tts.speak(text);
  }

  void dispose() {
    _levelSub?.cancel();
    audio.dispose();
  }
}
