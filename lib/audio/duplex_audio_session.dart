import 'dart:async';
import 'package:flutter/services.dart';

/// Talks to the native side (DuplexAudioPlugin on Android/iOS — see
/// android_native/ and ios_native/) to run one continuous, echo-cancelled
/// mic session for the whole time the app is active — the actual fix for
/// the "Google Assistant chime, mic clicks on/off" feel. Native owns:
///   - putting the OS audio session into real voice-call mode
///     (Android: MODE_IN_COMMUNICATION + AudioSource.VOICE_COMMUNICATION +
///      AcousticEchoCanceler/NoiseSuppressor; iOS: AVAudioSession .voiceChat)
///   - a persistent low-level PCM tap that streams a normalized 0..1 RMS
///     level back to Dart at ~30-60Hz, for the orb's voice reactivity and
///     for VAD (listening/thinking/barge-in) — decoupled from whatever
///     phrase-level STT engine sits on top (mirrors the web build, where a
///     separate Web Audio AnalyserNode drove the orb regardless of what
///     SpeechRecognition was doing).
class DuplexAudioSession {
  static const _method = MethodChannel('arkitak/audio_session');
  static const _levelEvents = EventChannel('arkitak/audio_level');

  StreamSubscription? _sub;
  final _levelController = StreamController<double>.broadcast();

  /// Normalized mic level, 0..1, updated continuously while [start]ed.
  Stream<double> get levelStream => _levelController.stream;

  bool _running = false;
  bool get isRunning => _running;

  /// Puts the OS into voice-call audio mode and starts the persistent tap.
  /// Safe to call again while already running (no-ops).
  Future<void> start() async {
    if (_running) return;
    try {
      await _method.invokeMethod('start');
    } on PlatformException catch (e) {
      // Surfaced to the caller as an empty level stream — the voice
      // controller should treat "never got a level event" as "mic
      // unavailable" and retry (e.g. on the next foreground/gesture).
      // ignore: avoid_print
      print('DuplexAudioSession.start failed: ${e.message}');
      return;
    }
    _running = true;
    _sub = _levelEvents.receiveBroadcastStream().listen(
      (event) => _levelController.add((event as num).toDouble().clamp(0.0, 1.0)),
      onError: (_) {},
    );
  }

  /// Fully tears down the session — mic light off, audio mode restored.
  /// Only call this on the intentional "disabled" transition; everything
  /// else (thinking/speaking/error) should leave the session running.
  Future<void> stop() async {
    if (!_running) return;
    _running = false;
    await _sub?.cancel();
    _sub = null;
    try {
      await _method.invokeMethod('stop');
    } on PlatformException catch (_) {}
  }

  void dispose() {
    _sub?.cancel();
    _levelController.close();
  }
}
