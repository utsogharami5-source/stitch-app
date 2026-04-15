import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';

/// Centralized permission manager for the CoinFlow app.
class PermissionService {
  static final PermissionService _instance = PermissionService._();
  static PermissionService get instance => _instance;
  PermissionService._();

  // ─── Camera ───────────────────────────────────────────────────────

  Future<bool> checkCameraPermission() async {
    final status = await Permission.camera.status;
    return status.isGranted;
  }

  Future<bool> requestCameraPermission(BuildContext context) async {
    var status = await Permission.camera.status;

    if (status.isGranted) return true;

    if (status.isDenied) {
      // Show rationale dialog
      if (context.mounted) {
        final shouldRequest = await showDialog<bool>(
          context: context,
          builder: (ctx) => AlertDialog(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
            title: const Text('Camera Access', style: TextStyle(fontWeight: FontWeight.bold)),
            content: const Text(
              'CoinFlow needs camera access to scan receipts and auto-fill transaction amounts.',
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(ctx, false),
                child: const Text('Not Now'),
              ),
              ElevatedButton(
                onPressed: () => Navigator.pop(ctx, true),
                child: const Text('Allow'),
              ),
            ],
          ),
        );
        if (shouldRequest != true) return false;
      }

      status = await Permission.camera.request();
    }

    if (status.isPermanentlyDenied && context.mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: const Text('Camera permission denied. Enable it in Settings.'),
          action: SnackBarAction(
            label: 'Open Settings',
            onPressed: () => openAppSettings(),
          ),
        ),
      );
      return false;
    }

    return status.isGranted;
  }

  // ─── Notifications ────────────────────────────────────────────────

  Future<bool> checkNotificationPermission() async {
    final status = await Permission.notification.status;
    return status.isGranted;
  }

  Future<bool> requestNotificationPermission(BuildContext context) async {
    var status = await Permission.notification.status;

    if (status.isGranted) return true;

    if (status.isDenied) {
      if (context.mounted) {
        final shouldRequest = await showDialog<bool>(
          context: context,
          builder: (ctx) => AlertDialog(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
            title: const Text('Stay Updated', style: TextStyle(fontWeight: FontWeight.bold)),
            content: const Text(
              'Enable notifications to receive budget alerts, spending summaries, and important updates.',
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(ctx, false),
                child: const Text('Skip'),
              ),
              ElevatedButton(
                onPressed: () => Navigator.pop(ctx, true),
                child: const Text('Enable'),
              ),
            ],
          ),
        );
        if (shouldRequest != true) return false;
      }

      status = await Permission.notification.request();
    }

    if (status.isPermanentlyDenied && context.mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: const Text('Notification permission denied. Enable it in Settings.'),
          action: SnackBarAction(
            label: 'Open Settings',
            onPressed: () => openAppSettings(),
          ),
        ),
      );
      return false;
    }

    return status.isGranted;
  }
}
