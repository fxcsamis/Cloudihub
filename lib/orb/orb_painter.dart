import 'dart:math';
import 'dart:typed_data';
import 'dart:ui' as ui;
import 'package:flutter/material.dart';
import 'orb_engine.dart';

/// Projects [OrbEngine]'s simulated 3D points into 2D and draws them.
/// Rotation (spinX/Y/Z) and the small camera parallax drift are applied here
/// rather than in the engine, since they're purely a view-space concern.
class OrbPainter extends CustomPainter {
  final OrbEngine engine;
  final double t; // elapsed seconds, for the camera's slow drift
  OrbPainter(this.engine, this.t) : super(repaint: null);

  static const _fovRad = 45 * pi / 180;

  @override
  void paint(Canvas canvas, Size size) {
    if (!engine.booted && !engine.activating) return;

    final focal = (size.height / 2) / tan(_fovRad / 2);
    final camX = sin(t * 0.02) * 5 * 0.55;
    final camY = cos(t * 0.03) * 3 * 0.55;
    final cx = size.width / 2;
    final cy = size.height / 2;

    final sx = sin(engine.spinX), csx = cos(engine.spinX);
    final sy = sin(engine.spinY), csy = cos(engine.spinY);
    final sz = sin(engine.spinZ), csz = cos(engine.spinZ);

    ui.Offset project(double x, double y, double z) {
      // Rotate X, then Y, then Z (order matters much less than consistency
      // here — this is a decorative particle cloud, not a rigid model).
      var y1 = y * csx - z * sx;
      var z1 = y * sx + z * csx;
      var x1 = x;
      var x2 = x1 * csy + z1 * sy;
      var z2 = -x1 * sy + z1 * csy;
      var y2 = y1;
      var x3 = x2 * csz - y2 * sz;
      var y3 = x2 * sz + y2 * csz;
      final z3 = z2 + engine.cloudZ;

      final distance = OrbEngine.cameraZ - z3;
      final scale = focal / (distance <= 1 ? 1 : distance);
      final sxp = cx + (x3 - camX) * scale;
      final syp = cy - (y3 - camY) * scale;
      return ui.Offset(sxp, syp);
    }

    // ── Particles ──
    final particlePts = Float32List(engine.n * 2);
    for (int i = 0; i < engine.n; i++) {
      final i3 = i * 3;
      final p = project(engine.drawPositions[i3], engine.drawPositions[i3 + 1], engine.drawPositions[i3 + 2]);
      particlePts[i * 2] = p.dx;
      particlePts[i * 2 + 1] = p.dy;
    }
    final particlePaint = Paint()
      ..color = engine.particleColor
      ..strokeWidth = (engine.particleSize * 6).clamp(1.0, 12.0)
      ..strokeCap = StrokeCap.round
      ..blendMode = BlendMode.plus
      ..maskFilter = const ui.MaskFilter.blur(ui.BlurStyle.normal, 1.4);
    canvas.drawRawPoints(ui.PointMode.points, particlePts, particlePaint);

    // ── Connection lines ──
    if (engine.lineVertexCount > 0) {
      final linePts = Float32List(engine.lineVertexCount * 2);
      for (int i = 0; i < engine.lineVertexCount; i++) {
        final i3 = i * 3;
        final p = project(engine.lineDrawPositions[i3], engine.lineDrawPositions[i3 + 1], engine.lineDrawPositions[i3 + 2]);
        linePts[i * 2] = p.dx;
        linePts[i * 2 + 1] = p.dy;
      }
      final linePaint = Paint()
        ..color = engine.lineColor
        ..strokeWidth = 0.8
        ..blendMode = BlendMode.plus;
      canvas.drawRawPoints(ui.PointMode.lines, linePts, linePaint);
    }

    // ── Electrons ──
    if (engine.electronVertexCount > 0) {
      final ePts = Float32List(engine.electronVertexCount * 2);
      for (int i = 0; i < engine.electronVertexCount; i++) {
        final i3 = i * 3;
        final p = project(engine.electronDrawPositions[i3], engine.electronDrawPositions[i3 + 1], engine.electronDrawPositions[i3 + 2]);
        ePts[i * 2] = p.dx;
        ePts[i * 2 + 1] = p.dy;
      }
      final electronPaint = Paint()
        ..color = engine.electronColor
        ..strokeWidth = 3.2
        ..strokeCap = StrokeCap.round
        ..blendMode = BlendMode.plus
        ..maskFilter = const ui.MaskFilter.blur(ui.BlurStyle.normal, 1.0);
      canvas.drawRawPoints(ui.PointMode.points, ePts, electronPaint);
    }
  }

  @override
  bool shouldRepaint(covariant OrbPainter oldDelegate) => true; // driven by a Ticker every frame
}
