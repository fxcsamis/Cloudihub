import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'orb/orb_widget.dart';
import 'voice/voice_controller.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  // True edge-to-edge, no system bars nibbling into the black canvas.
  SystemChrome.setEnabledSystemUIMode(SystemUiMode.immersiveSticky);
  SystemChrome.setPreferredOrientations([DeviceOrientation.portraitUp]);
  runApp(const ArkitakApp());
}

class ArkitakApp extends StatelessWidget {
  const ArkitakApp({super.key});

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      debugShowCheckedModeBanner: false,
      home: OrbScreen(),
    );
  }
}

/// No buttons anywhere on purpose — boot, listening, thinking, speaking,
/// error and disable are all automatic, driven by VoiceController from the
/// mic. See voice/voice_controller.dart.
class OrbScreen extends StatefulWidget {
  const OrbScreen({super.key});

  @override
  State<OrbScreen> createState() => _OrbScreenState();
}

class _OrbScreenState extends State<OrbScreen> with WidgetsBindingObserver {
  final GlobalKey<OrbWidgetState> _orbKey = GlobalKey<OrbWidgetState>();
  VoiceController? _controller;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    WidgetsBinding.instance.addPostFrameCallback((_) => _start());
  }

  Future<void> _start() async {
    final orb = _orbKey.currentState;
    if (orb == null) return;
    final controller = VoiceController(orb);
    _controller = controller;
    await controller.init();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    // If the mic was never granted, retry on return-to-foreground — no
    // visible prompt, matches the web build's silent-retry behaviour.
    if (state == AppLifecycleState.resumed) {
      final controller = _controller;
      if (controller != null && !controller.audio.isRunning && controller.current != OrbState.disabled) {
        controller.init();
      }
    }
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _controller?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF05030A),
      body: OrbWidget(key: _orbKey),
    );
  }
}
