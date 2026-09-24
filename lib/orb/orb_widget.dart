import 'dart:math';
import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import 'orb_engine.dart';
import 'orb_painter.dart';

/// Drop this anywhere full-screen. Call [OrbWidgetState.activate] (via the
/// GlobalKey) to kick off the boot assembly, [setState]/[setAudioLevel] to
/// drive it from the voice controller.
class OrbWidget extends StatefulWidget {
  const OrbWidget({super.key});

  @override
  State<OrbWidget> createState() => OrbWidgetState();
}

class OrbWidgetState extends State<OrbWidget> with SingleTickerProviderStateMixin {
  late final Ticker _ticker;
  final OrbEngine engine = OrbEngine();
  Duration _last = Duration.zero;
  double _elapsed = 0;
  bool _activated = false;

  @override
  void initState() {
    super.initState();
    _ticker = createTicker(_onTick)..start();
  }

  void _onTick(Duration elapsed) {
    final dt = ((elapsed - _last).inMicroseconds / 1e6).clamp(0.0, 0.05);
    _last = elapsed;
    _elapsed += dt;
    engine.step(dt, _elapsed);
    if (mounted) setState(() {}); // cheap: only marks this CustomPaint dirty
  }

  /// Starts the "system active" boot assembly. Safe to call multiple times —
  /// no-ops once already booted/mid-assembly (mirrors main-orb.js).
  void activate() {
    if (_activated) return;
    _activated = true;
    final size = MediaQuery.of(context).size;
    const fovRad = 45 * pi / 180;
    final halfH = OrbEngine.cameraZ * tan(fovRad / 2);
    final halfW = halfH * (size.width / size.height);
    engine.activate(halfW: halfW, halfH: halfH);
  }

  void setOrbState(OrbState s) => engine.setState(s);

  /// Overall mic level, 0..1. See OrbEngine.setAudioLevel for the bass/mid
  /// simplification note.
  void setAudioLevel(double level) => engine.setAudioLevel(level);

  bool get isBooted => engine.booted;

  @override
  void dispose() {
    _ticker.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return CustomPaint(
      size: Size.infinite,
      painter: OrbPainter(engine, _elapsed),
    );
  }
}

// Re-exported here so callers only need to import orb_widget.dart.
export 'orb_engine.dart' show OrbState;
export 'orb_painter.dart' show OrbPainter;
