import 'dart:math';
import 'dart:typed_data';
import 'dart:ui' show Color;

/// Voice/assistant states the orb can be in. Mirrors the state strings used
/// by the original app.js/main-orb.js web build 1:1 so behaviour ports over
/// exactly — see /areas/arkitak.md history for what each state means.
enum OrbState { boot, idle, listening, thinking, speaking, error, disabling, disabled, compacting }

double _clamp01(double x) => x < 0 ? 0 : (x > 1 ? 1 : x);
double _easeOutCubic(double x) {
  final f = x - 1;
  return f * f * f + 1;
}

/// One RGB colour target a state lerps its particles/lines toward, and how
/// fast (per-frame lerp factor) it lerps there. Mirrors the `mat.color.lerp`
/// calls in main-orb.js.
class _ColorTarget {
  final double r, g, b; // 0..1
  final double speed;
  const _ColorTarget(this.r, this.g, this.b, this.speed);
}

const _purple = _ColorTarget(0x9d / 0xff, 0x5c / 0xff, 0xff / 0xff, 0.015);
const _thinkingColor = _ColorTarget(0xc9 / 0xff, 0xa3 / 0xff, 0xff / 0xff, 0.015);
const _speakingColor = _ColorTarget(0xe0 / 0xff, 0xaa / 0xff, 0xff / 0xff, 0.02);
const _compactingColor = _ColorTarget(0x4a / 0xff, 0x35 / 0xff, 0x80 / 0xff, 0.03);
const _errorColor = _ColorTarget(0xff / 0xff, 0x4d / 0xff, 0x4d / 0xff, 0.04);
const _disablingColor = _ColorTarget(0x8a / 0xff, 0x7a / 0xff, 0xa8 / 0xff, 0.05);
const _disabledColor = _ColorTarget(0x6a / 0xff, 0x64 / 0xff, 0x78 / 0xff, 0.03);

/// A single travelling "electron" dot along one of the connection lines.
class _Electron {
  double sx, sy, sz, ex, ey, ez;
  double t = 0;
  final double speed;
  _Electron(this.sx, this.sy, this.sz, this.ex, this.ey, this.ez, this.speed);
}

class _Connection {
  final double x1, y1, z1, x2, y2, z2;
  const _Connection(this.x1, this.y1, this.z1, this.x2, this.y2, this.z2);
}

/// Pure-Dart port of the web build's particle physics. Call [step] once per
/// frame; read [particlePositions] / [linePositions] / [electronPositions]
/// (already rotated + camera-space, but NOT yet perspective-projected —
/// that's OrbPainter's job) plus [particleColor]/[lineColor]/[opacity]/
/// [particleSize] to draw the current frame.
class OrbEngine {
  static const int n = 2000;
  static const int maxLines = 8000;
  static const int maxElectrons = 200;
  // Real (already "group-scaled") sphere radius — see the Flutter-port note
  // in main-orb.js history: ORB_SCALE folded directly into these numbers
  // instead of kept as a separate transform, to keep the per-frame math simple.
  static const double _scale = 0.55;
  static const double cameraZ = 80;
  static const double _fovDeg = 45;

  final _rng = Random();

  final Float32List pos = Float32List(n * 3);
  final Float32List targetPos = Float32List(n * 3);
  final Float32List cornerPos = Float32List(n * 3);
  final Float32List vel = Float32List(n * 3);
  final Float32List phase = Float32List(n);
  final Float32List bootDelay = Float32List(n);
  final Float32List bootDuration = Float32List(n);

  OrbState state = OrbState.idle;
  double targetRadius = 25, currentRadius = 25;
  double targetSpeed = 0.3, currentSpeed = 0.3;
  double targetBright = 0.6, currentBright = 0.6;
  double targetSize = 0.4, currentSize = 0.4;
  double lineAmount = 0, targetLineAmount = 0;
  static const double lineDistance = 8 * _scale;

  double spinX = 0, spinY = 0, spinZ = 0;
  double transitionEnergy = 0;
  OrbState _lastState = OrbState.idle;

  double cloudZ = 0, cloudZVel = 0;

  bool booted = false;
  bool activating = false;
  double _activateStartTime = 0;
  double elapsed = 0;

  // Audio-reactive inputs, 0..1. Feed these from the mic level analysis —
  // see lib/audio/duplex_audio_session.dart. If you don't have real
  // frequency-band data yet, driving both from overall RMS level is a fine
  // stand-in (that's what the pre-LLM demo does).
  double bass = 0;
  double mid = 0;

  double _r = _purple.r, _g = _purple.g, _b = _purple.b;
  double particleOpacity = 0;
  double particleSize = 0.4;

  final List<_Electron> _activeElectrons = [];
  List<_Connection> _activeConnections = const [];
  double _electronSpawnRate = 0;
  double _targetElectronRate = 0;
  double _lastElectronSpawn = 0;

  // Filled by step(); read these to draw.
  int visibleParticleCount = 0;
  final Float32List drawPositions = Float32List(n * 3); // post-physics, pre-rotation
  int lineVertexCount = 0; // number of (x,y,z) triples, i.e. 2x segment count
  final Float32List lineDrawPositions = Float32List(maxLines * 6);
  int electronVertexCount = 0;
  final Float32List electronDrawPositions = Float32List(maxElectrons * 3);
  Color electronColor = const Color(0xfff3e8ff);
  double lineOpacity = 0;

  OrbEngine() {
    for (int i = 0; i < n; i++) {
      final offset = 2 / n;
      final increment = pi * (3 - sqrt(5));
      final y = i * offset - 1 + offset / 2;
      final r = sqrt(max(0, 1 - y * y));
      final phi = i * increment;
      final jitter = 0.94 + _rng.nextDouble() * 0.12;
      final radius = 25 * _scale;
      targetPos[i * 3] = cos(phi) * r * radius * jitter;
      targetPos[i * 3 + 1] = y * radius * jitter;
      targetPos[i * 3 + 2] = sin(phi) * r * radius * jitter;
      phase[i] = _rng.nextDouble() * 1000;
      bootDelay[i] = _rng.nextDouble() * 0.9;
      bootDuration[i] = 1.1 + _rng.nextDouble() * 0.6;
    }
  }

  void _scatterCorners(double halfW, double halfH) {
    for (int i = 0; i < n; i++) {
      final i3 = i * 3;
      final cx = (_rng.nextDouble() < 0.5 ? -1 : 1) * (halfW * (0.85 + _rng.nextDouble() * 0.35));
      final cy = (_rng.nextDouble() < 0.5 ? -1 : 1) * (halfH * (0.85 + _rng.nextDouble() * 0.35));
      cornerPos[i3] = cx;
      cornerPos[i3 + 1] = cy;
      cornerPos[i3 + 2] = (_rng.nextDouble() - 0.5) * 40 * _scale;
    }
    pos.setAll(0, cornerPos);
  }

  /// [halfW]/[halfH] are the true visible half-extents at the sphere's depth
  /// (z=0), in the same units as targetPos — i.e. cameraZ * tan(fov/2), not
  /// inflated by any extra scale factor (see the class doc comment).
  void activate({required double halfW, required double halfH}) {
    if (booted || activating) return;
    _scatterCorners(halfW, halfH);
    activating = true;
    _activateStartTime = elapsed;
  }

  void setState(OrbState s) => state = s;

  /// Overall mic level 0..1. Splits crudely into "bass"/"mid" the same way
  /// the web build's two frequency bands roughly behaved — replace with real
  /// FFT band data later if you wire one in; this is a fine stand-in.
  void setAudioLevel(double level) {
    bass = level;
    mid = level;
  }

  void _applyStateTargets() {
    switch (state) {
      case OrbState.boot:
      case OrbState.idle:
        targetRadius = 28 * _scale; targetSpeed = 0.2; targetBright = 0.5;
        targetSize = 0.35; targetLineAmount = 0.15; _targetElectronRate = 0;
        break;
      case OrbState.listening:
        targetRadius = 22 * _scale; targetSpeed = 0.3; targetBright = 0.65;
        targetSize = 0.4; targetLineAmount = 0.4; _targetElectronRate = 0;
        break;
      case OrbState.thinking:
        targetRadius = 16 * _scale; targetSpeed = 0.5; targetBright = 0.7;
        targetSize = 0.3; targetLineAmount = 1.0; _targetElectronRate = 0.015;
        break;
      case OrbState.speaking:
        targetRadius = 20 * _scale; targetSpeed = 0.9; targetBright = 0.75;
        targetSize = 0.42; targetLineAmount = 0.55; _targetElectronRate = 0.02;
        break;
      case OrbState.compacting:
        targetRadius = 12 * _scale; targetSpeed = 0.08; targetBright = 0.28;
        targetSize = 0.28; targetLineAmount = 0.0; _targetElectronRate = 0;
        break;
      case OrbState.error:
        targetRadius = 18 * _scale; targetSpeed = 1.6; targetBright = 0.85;
        targetSize = 0.46; targetLineAmount = 0.65; _targetElectronRate = 0.03;
        break;
      case OrbState.disabling:
        targetRadius = 9 * _scale; targetSpeed = 0.05; targetBright = 0.18;
        targetSize = 0.22; targetLineAmount = 0.0; _targetElectronRate = 0;
        break;
      case OrbState.disabled:
        targetRadius = 30 * _scale; targetSpeed = 0.03; targetBright = 0.15;
        targetSize = 0.22; targetLineAmount = 0.0; _targetElectronRate = 0;
        break;
    }
  }

  /// Advance the simulation by [dt] seconds. [t] is total elapsed seconds
  /// (matches the JS THREE.Clock elapsed time used for all the sin/cos
  /// phase math, so keep passing a monotonically increasing clock here).
  void step(double dt, double t) {
    elapsed = t;
    if (!booted && !activating) return; // nothing to simulate pre-boot

    _applyStateTargets();
    currentRadius += (targetRadius - currentRadius) * 0.02;
    currentSpeed += (targetSpeed - currentSpeed) * 0.02;
    currentBright += (targetBright - currentBright) * 0.02;
    currentSize += (targetSize - currentSize) * 0.02;
    lineAmount += (targetLineAmount - lineAmount) * 0.02;
    _electronSpawnRate += (_targetElectronRate - _electronSpawnRate) * 0.02;

    if (state != _lastState) {
      transitionEnergy = 1.0;
      _lastState = state;
    }
    transitionEnergy *= 0.985;
    if (transitionEnergy > 0.05) {
      spinX += transitionEnergy * 0.012 * sin(t * 1.7);
      spinY += transitionEnergy * 0.015;
      spinZ += transitionEnergy * 0.008 * cos(t * 1.3);
    }
    if (state == OrbState.speaking) {
      spinY += 0.006;
      spinX += 0.0015 * sin(t * 0.4);
    }

    double zTarget = sin(t * 0.12) * 8 * _scale;
    if (state == OrbState.thinking) {
      zTarget = sin(t * 0.3) * 15 * _scale + sin(t * 0.9) * 6 * _scale;
    } else if (state == OrbState.speaking) {
      zTarget = sin(t * 0.15) * 6 * _scale - bass * 10 * _scale;
    } else if (state == OrbState.compacting) {
      zTarget = -20 * _scale + sin(t * 0.08) * 3 * _scale;
    }
    cloudZVel += (zTarget - cloudZ) * 0.008;
    cloudZVel *= 0.94;
    cloudZ += cloudZVel;

    double speakPulse = 0;
    double breathTalk = 0;
    if (state == OrbState.speaking) {
      breathTalk = sin(t * 1.6) * 0.8 + sin(t * 0.7 + 1.3) * 0.2;
      speakPulse = max(0, breathTalk) * (0.5 + mid * 0.8);
    }
    final shellRadius = currentRadius *
        (1 + (state == OrbState.speaking ? breathTalk * 0.06 : -speakPulse * 0.12));

    bool allArrived = true;
    if (booted) {
      for (int i = 0; i < n; i++) {
        final i3 = i * 3;
        final x = pos[i3], y = pos[i3 + 1], z = pos[i3 + 2];
        final px = phase[i];
        vel[i3] += sin(t * 0.05 + px) * 0.001 * currentSpeed;
        vel[i3 + 1] += cos(t * 0.06 + px * 1.3) * 0.001 * currentSpeed;
        vel[i3 + 2] += sin(t * 0.055 + px * 0.7) * 0.001 * currentSpeed;
        vel[i3] += sin(t * 0.02 + px * 2.1 + y * 0.1) * 0.0008 * currentSpeed;
        vel[i3 + 1] += cos(t * 0.025 + px * 1.7 + z * 0.1) * 0.0008 * currentSpeed;
        vel[i3 + 2] += sin(t * 0.022 + px * 0.9 + x * 0.1) * 0.0008 * currentSpeed;

        final dist = sqrt(x * x + y * y + z * z) == 0 ? 0.01 : sqrt(x * x + y * y + z * z);
        double localShell = shellRadius;
        if (state == OrbState.listening) {
          final wobble = sin(t * 0.8 + px * 0.05) * 0.5 +
              sin(t * 1.4 + px * 0.11 + 2.1) * 0.3 +
              sin(t * 2.3 + px * 0.07 + 4.4) * 0.2;
          localShell = shellRadius * (1 + wobble * 0.05 + bass * 0.22);
        }
        final pull = (dist - localShell) * 0.0032;
        vel[i3] -= (x / dist) * pull;
        vel[i3 + 1] -= (y / dist) * pull;
        vel[i3 + 2] -= (z / dist) * pull;

        if (bass > 0.05) {
          vel[i3] += (x / dist) * bass * 0.02;
          vel[i3 + 1] += (y / dist) * bass * 0.02;
          vel[i3 + 2] += (z / dist) * bass * 0.02;
        }
        if (state == OrbState.speaking && mid > 0.1) {
          final pulse = sin(t * 8 + px);
          vel[i3] += (x / dist) * mid * 0.012 * pulse;
          vel[i3 + 1] += (y / dist) * mid * 0.012 * pulse;
        }
        if (state == OrbState.error) {
          vel[i3] += (_rng.nextDouble() - 0.5) * 0.01;
          vel[i3 + 1] += (_rng.nextDouble() - 0.5) * 0.01;
          vel[i3 + 2] += (_rng.nextDouble() - 0.5) * 0.01;
        }
        vel[i3] *= 0.992;
        vel[i3 + 1] *= 0.992;
        vel[i3 + 2] *= 0.992;
        pos[i3] += vel[i3];
        pos[i3 + 1] += vel[i3 + 1];
        pos[i3 + 2] += vel[i3 + 2];
      }
    } else {
      final bootT = t - _activateStartTime;
      for (int i = 0; i < n; i++) {
        final i3 = i * 3;
        final localT = (bootT - bootDelay[i]) / bootDuration[i];
        if (localT < 1) allArrived = false;
        final et = _easeOutCubic(_clamp01(localT));
        pos[i3] = cornerPos[i3] + (targetPos[i3] - cornerPos[i3]) * et;
        pos[i3 + 1] = cornerPos[i3 + 1] + (targetPos[i3 + 1] - cornerPos[i3 + 1]) * et;
        pos[i3 + 2] = cornerPos[i3 + 2] + (targetPos[i3 + 2] - cornerPos[i3 + 2]) * et;
      }
      if (allArrived) {
        booted = true;
        activating = false;
      }
    }

    drawPositions.setAll(0, pos);
    visibleParticleCount = n;

    double bootFade = 1, lineFade = 1;
    if (!booted) {
      final bootT = t - _activateStartTime;
      bootFade = _clamp01(bootT / 1.8);
      lineFade = _clamp01((bootT - 1.1) / 0.9);
    }

    _updateLines(lineFade);
    _updateElectrons(t);

    final errorStrobe = state == OrbState.error ? (0.55 + 0.45 * sin(t * 14).abs()) : 1.0;
    particleOpacity = (currentBright + bass * 0.08 + speakPulse * 0.1) * bootFade * errorStrobe;
    particleSize = currentSize + bass * 0.05 + speakPulse * 0.05;
    lineOpacity = (lineAmount * 0.12 + speakPulse * 0.1) * lineFade;

    _ColorTarget target;
    switch (state) {
      case OrbState.thinking: target = _thinkingColor; break;
      case OrbState.speaking: target = _speakingColor; break;
      case OrbState.compacting: target = _compactingColor; break;
      case OrbState.error: target = _errorColor; break;
      case OrbState.disabling: target = _disablingColor; break;
      case OrbState.disabled: target = _disabledColor; break;
      default: target = _purple;
    }
    _r += (target.r - _r) * target.speed;
    _g += (target.g - _g) * target.speed;
    _b += (target.b - _b) * target.speed;
  }

  void _updateLines(double lineFade) {
    if (lineAmount <= 0.01 || lineFade <= 0.001) {
      lineVertexCount = 0;
      _activeConnections = const [];
      return;
    }
    final maxDist = lineDistance * (1 + bass * 0.5);
    final maxDistSq = maxDist * maxDist;
    final step = max(1, (n / 600).floor());
    int lineCount = 0;
    final connections = <_Connection>[];
    for (int i = 0; i < n && lineCount < maxLines; i += step) {
      final i3 = i * 3;
      final x1 = pos[i3], y1 = pos[i3 + 1], z1 = pos[i3 + 2];
      for (int j = i + step; j < n && lineCount < maxLines; j += step) {
        final j3 = j * 3;
        final dx = pos[j3] - x1, dy = pos[j3 + 1] - y1, dz = pos[j3 + 2] - z1;
        if (dx * dx + dy * dy + dz * dz < maxDistSq) {
          final idx = lineCount * 6;
          lineDrawPositions[idx] = x1;
          lineDrawPositions[idx + 1] = y1;
          lineDrawPositions[idx + 2] = z1;
          lineDrawPositions[idx + 3] = pos[j3];
          lineDrawPositions[idx + 4] = pos[j3 + 1];
          lineDrawPositions[idx + 5] = pos[j3 + 2];
          lineCount++;
          if (connections.length < 500) {
            connections.add(_Connection(x1, y1, z1, pos[j3], pos[j3 + 1], pos[j3 + 2]));
          }
        }
      }
    }
    lineVertexCount = lineCount * 2;
    _activeConnections = connections;
  }

  void _updateElectrons(double t) {
    if (_activeConnections.isNotEmpty && _electronSpawnRate > 0.005) {
      if (_activeElectrons.length < 3 && (t - _lastElectronSpawn) > 1.0) {
        final conn = _activeConnections[_rng.nextInt(_activeConnections.length)];
        _activeElectrons.add(_Electron(
          conn.x1, conn.y1, conn.z1, conn.x2, conn.y2, conn.z2,
          0.003 + _rng.nextDouble() * 0.003,
        ));
        _lastElectronSpawn = t;
      }
    }
    int alive = 0;
    for (int e = _activeElectrons.length - 1; e >= 0; e--) {
      final el = _activeElectrons[e];
      el.t += el.speed;
      if (el.t >= 1) {
        _activeElectrons.removeAt(e);
        continue;
      }
      final ei = alive * 3;
      electronDrawPositions[ei] = el.sx + (el.ex - el.sx) * el.t;
      electronDrawPositions[ei + 1] = el.sy + (el.ey - el.sy) * el.t;
      electronDrawPositions[ei + 2] = el.sz + (el.ez - el.sz) * el.t;
      alive++;
    }
    electronVertexCount = alive;
  }

  Color get particleColor => Color.fromRGBO(
      (_r * 255).round(), (_g * 255).round(), (_b * 255).round(), particleOpacity.clamp(0, 1));
  Color get lineColor => Color.fromRGBO(
      (_r * 255).round(), (_g * 255).round(), (_b * 255).round(), lineOpacity.clamp(0, 1));
}
