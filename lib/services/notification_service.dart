import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/foundation.dart';

/// Manages FCM token registration and notification polling from Firestore.
class NotificationService {
  static final NotificationService _instance = NotificationService._();
  static NotificationService get instance => _instance;
  NotificationService._();

  final FirebaseMessaging _messaging = FirebaseMessaging.instance;
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;

  /// Initialize messaging + request permission + register FCM token
  Future<void> initialize() async {
    try {
      // Request permission (for Android 13+ and iOS)
      final settings = await _messaging.requestPermission(
        alert: true,
        badge: true,
        sound: true,
        announcement: false,
        criticalAlert: false,
        provisional: false,
      );

      debugPrint('FCM permission status: ${settings.authorizationStatus}');

      if (settings.authorizationStatus == AuthorizationStatus.authorized ||
          settings.authorizationStatus == AuthorizationStatus.provisional) {
        await _registerToken();
      }

      // Listen for token refresh
      _messaging.onTokenRefresh.listen((newToken) {
        _saveTokenToFirestore(newToken);
      });

      // Handle foreground messages
      FirebaseMessaging.onMessage.listen((RemoteMessage message) {
        debugPrint('Foreground message: ${message.notification?.title}');
      });
    } catch (e) {
      debugPrint('NotificationService init error: $e');
    }
  }

  Future<void> _registerToken() async {
    try {
      final token = await _messaging.getToken();
      if (token != null) {
        await _saveTokenToFirestore(token);
      }
    } catch (e) {
      debugPrint('FCM token registration error: $e');
    }
  }

  Future<void> _saveTokenToFirestore(String token) async {
    final user = FirebaseAuth.instance.currentUser;
    if (user == null) return;

    try {
      await _firestore.collection('users').doc(user.uid).set({
        'fcm_token': token,
        'fcm_updated_at': FieldValue.serverTimestamp(),
        'platform': defaultTargetPlatform.toString(),
      }, SetOptions(merge: true));
      debugPrint('FCM token saved for user ${user.uid}');
    } catch (e) {
      debugPrint('Error saving FCM token: $e');
    }
  }

  /// Fetch unread notifications from Firestore for this user
  Future<List<Map<String, dynamic>>> getNotifications() async {
    final user = FirebaseAuth.instance.currentUser;
    if (user == null) return [];

    try {
      // Get broadcast notifications (target = 'all')
      final broadcastSnap = await _firestore
          .collection('notifications')
          .where('target', isEqualTo: 'all')
          .orderBy('created_at', descending: true)
          .limit(20)
          .get();

      // Get user-specific notifications (target = user email or uid)
      final userEmailSnap = await _firestore
          .collection('notifications')
          .where('target', isEqualTo: user.email)
          .orderBy('created_at', descending: true)
          .limit(10)
          .get();

      final userUidSnap = await _firestore
          .collection('notifications')
          .where('target', isEqualTo: user.uid)
          .orderBy('created_at', descending: true)
          .limit(10)
          .get();

      final List<Map<String, dynamic>> notifications = [];
      final seenIds = <String>{};

      void addDocs(QuerySnapshot snap, String source) {
        for (var doc in snap.docs) {
          if (seenIds.contains(doc.id)) continue;
          seenIds.add(doc.id);
          final data = doc.data() as Map<String, dynamic>;
          notifications.add({
            'id': doc.id,
            'title': data['title'] ?? 'Notification',
            'body': data['body'] ?? '',
            'type': data['type'] ?? 'general',
            'created_at': data['created_at'],
            'source': source,
          });
        }
      }

      addDocs(broadcastSnap, 'broadcast');
      addDocs(userEmailSnap, 'personal');
      addDocs(userUidSnap, 'personal');

      // Sort by date, newest first
      notifications.sort((a, b) {
        final aDate = a['created_at'];
        final bDate = b['created_at'];
        if (aDate == null && bDate == null) return 0;
        if (aDate == null) return 1;
        if (bDate == null) return -1;
        return (bDate as Timestamp).compareTo(aDate as Timestamp);
      });

      return notifications;
    } catch (e) {
      debugPrint('Error fetching notifications: $e');
      return [];
    }
  }

  /// Get a real-time stream of broadcast notifications
  Stream<QuerySnapshot> notificationsStream() {
    return _firestore
        .collection('notifications')
        .where('target', isEqualTo: 'all')
        .orderBy('created_at', descending: true)
        .limit(20)
        .snapshots();
  }

  /// Return the count of notifications
  Future<int> getUnreadCount() async {
    final notifications = await getNotifications();
    return notifications.length;
  }
}
