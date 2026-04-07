import 'package:cloud_firestore/cloud_firestore.dart' hide Transaction;
import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/foundation.dart' hide Category;
import 'database_service.dart';
import '../models/models.dart';

class SyncService {
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;
  final DatabaseService _dbService = DatabaseService.instance;

  /// Creates / updates the user profile document in Firestore
  Future<void> createUserProfile(User user) async {
    final docRef = _firestore.collection('users').doc(user.uid);
    final doc = await docRef.get();

    if (!doc.exists) {
      await docRef.set({
        'uid': user.uid,
        'name': user.displayName ?? '',
        'email': user.email ?? '',
        'premium': false,
        'created_at': FieldValue.serverTimestamp(),
      });
    } else {
      // Update name/email in case they changed
      await docRef.update({
        'name': user.displayName ?? '',
        'email': user.email ?? '',
      });
    }
  }

  // Sync all data for a user
  Future<void> syncData(String userId) async {
    // Check if user has already been migrated or if they have transactions
    final userDoc = await _firestore.collection('users').doc(userId).get();
    final transactionsSnapshot = await _firestore
        .collection('users')
        .doc(userId)
        .collection('transactions')
        .limit(1)
        .get();
    
    // Migration is needed if the user doesn't exist, has no transactions, or hasn't been explicitly marked as migrated
    final bool needsMigration = !userDoc.exists || 
                               (userDoc.exists && transactionsSnapshot.docs.isEmpty && !(userDoc.data()?['migrated_from_legacy'] ?? false));

    if (needsMigration) {
      await _restoreLegacyData(userId);
      // Mark as migrated to prevent redundant attempts
      await _firestore.collection('users').doc(userId).set({
        'migrated_from_legacy': true,
        'last_migration_check': FieldValue.serverTimestamp(),
      }, SetOptions(merge: true));
    }

    await syncCategories(userId);
    await syncTransactions(userId);
    await syncGoals(userId);
    await syncRecurring(userId);
  }

  /// Restoration bridge for legacy monolithic backups
  Future<void> _restoreLegacyData(String userId) async {
    try {
      final legacyDoc = await _firestore.collection('backups').doc(userId).get();
      if (!legacyDoc.exists) return;

      final data = legacyDoc.data();
      if (data == null || !data.containsKey('transactions')) return;

      final List<dynamic> legacyTransactions = data['transactions'] ?? [];
      
      for (var lt in legacyTransactions) {
        if (lt is! Map<String, dynamic>) continue;

        // Map legacy fields to new Transaction model
        final String id = lt['transaction_id']?.toString() ?? DateTime.now().millisecondsSinceEpoch.toString();
        final String title = lt['note'] ?? 'Imported Transaction';
        final double amount = (lt['amount'] as num?)?.toDouble() ?? 0.0;
        final String type = (lt['type']?.toString().toLowerCase() == 'income') ? 'income' : 'expense';
        
        // Handle date (Legacy was int ms, New is DateTime)
        DateTime date = DateTime.now();
        if (lt['date'] != null) {
          date = DateTime.fromMillisecondsSinceEpoch(lt['date'] as int);
        }

        final String categoryId = lt['category_id']?.toString() ?? '1'; // Default to first category

        final transaction = Transaction(
          id: id,
          title: title,
          amount: amount,
          type: type,
          date: date,
          categoryId: categoryId,
          lastModified: DateTime.now(), // Mark as modified now to ensure cloud sync picks it up
        );

        // Save legacy record locally; syncTransactions will push it to the new cloud granular store
        await _dbService.insertTransaction(transaction);
      }
      
      debugPrint('Legacy restoration complete: ${legacyTransactions.length} items migrated.');
    } catch (e) {
      debugPrint('Legacy restoration error: $e');
    }
  }

  // Categories Sync
  Future<void> syncCategories(String userId) async {
    final localCategories = await _dbService.getAllCategories();
    final remoteSnapshot = await _firestore
        .collection('users')
        .doc(userId)
        .collection('categories')
        .get();

    final remoteCategories = remoteSnapshot.docs
        .map((doc) => Category.fromMap({...doc.data(), 'id': doc.id}))
        .toList();

    // Push local to remote
    for (var local in localCategories) {
      final remote = remoteCategories.firstWhere(
        (r) => r.id == local.id,
        orElse: () => Category(id: 'none', name: '', color: '', icon: ''),
      );

      if (remote.id == 'none' || local.lastModified.isAfter(remote.lastModified)) {
        await _firestore
            .collection('users')
            .doc(userId)
            .collection('categories')
            .doc(local.id)
            .set({...local.toMap(), 'user_id': userId});
      }
    }

    // Pull remote to local
    for (var remote in remoteCategories) {
      final local = localCategories.firstWhere(
        (l) => l.id == remote.id,
        orElse: () => Category(id: 'none', name: '', color: '', icon: ''),
      );

      if (local.id == 'none' || remote.lastModified.isAfter(local.lastModified)) {
        await _dbService.updateCategory(remote);
      }
    }
  }

  // Transactions Sync
  Future<void> syncTransactions(String userId) async {
    final localTransactions = await _dbService.getAllTransactions();
    final remoteSnapshot = await _firestore
        .collection('users')
        .doc(userId)
        .collection('transactions')
        .get();

    final remoteTransactions = remoteSnapshot.docs
        .map((doc) => Transaction.fromMap({...doc.data(), 'id': doc.id}))
        .toList();

    // Push local to remote
    for (var local in localTransactions) {
      final remote = remoteTransactions.firstWhere(
        (r) => r.id == local.id,
        orElse: () => Transaction(id: 'none', title: '', amount: 0, type: 'expense', date: DateTime.now(), categoryId: ''),
      );

      if (remote.id == 'none' || local.lastModified.isAfter(remote.lastModified)) {
        await _firestore
            .collection('users')
            .doc(userId)
            .collection('transactions')
            .doc(local.id)
            .set({...local.toMap(), 'user_id': userId});
      }
    }

    // Pull remote to local
    for (var remote in remoteTransactions) {
      final local = localTransactions.firstWhere(
        (l) => l.id == remote.id,
        orElse: () => Transaction(id: 'none', title: '', amount: 0, type: 'expense', date: DateTime.now(), categoryId: ''),
      );

      if (local.id == 'none' || remote.lastModified.isAfter(local.lastModified)) {
        await _dbService.insertTransaction(remote);
      }
    }
  }

  // Goals Sync
  Future<void> syncGoals(String userId) async {
    final localGoals = await _dbService.getAllGoals();
    final remoteSnapshot = await _firestore
        .collection('users')
        .doc(userId)
        .collection('goals')
        .get();

    final remoteGoals = remoteSnapshot.docs
        .map((doc) => FinancialGoal.fromMap({...doc.data(), 'id': doc.id}))
        .toList();

    // Push local to remote
    for (var local in localGoals) {
      final remote = remoteGoals.firstWhere(
        (r) => r.id == local.id,
        orElse: () => FinancialGoal(id: 'none', name: '', targetAmount: 0, savedAmount: 0, deadline: DateTime.now()),
      );

      if (remote.id == 'none' || local.lastModified.isAfter(remote.lastModified)) {
        await _firestore
            .collection('users')
            .doc(userId)
            .collection('goals')
            .doc(local.id)
            .set({...local.toMap(), 'user_id': userId});
      }
    }

    // Pull remote to local
    for (var remote in remoteGoals) {
      final local = localGoals.firstWhere(
        (l) => l.id == remote.id,
        orElse: () => FinancialGoal(id: 'none', name: '', targetAmount: 0, savedAmount: 0, deadline: DateTime.now()),
      );

      if (local.id == 'none' || remote.lastModified.isAfter(local.lastModified)) {
        await _dbService.updateFinancialGoal(remote);
      }
    }
  }

  // Recurring Sync
  Future<void> syncRecurring(String userId) async {
    final localRecurring = await _dbService.getAllRecurring();
    final remoteSnapshot = await _firestore
        .collection('users')
        .doc(userId)
        .collection('recurring')
        .get();

    final remoteRecurring = remoteSnapshot.docs
        .map((doc) => RecurringTransaction.fromMap({...doc.data(), 'id': doc.id}))
        .toList();

    // Push local to remote
    for (var local in localRecurring) {
      final remote = remoteRecurring.firstWhere(
        (r) => r.id == local.id,
        orElse: () => RecurringTransaction(id: 'none', title: '', amount: 0, categoryId: '', repeatType: 'monthly'),
      );

      if (remote.id == 'none' || local.lastModified.isAfter(remote.lastModified)) {
        await _firestore
            .collection('users')
            .doc(userId)
            .collection('recurring')
            .doc(local.id)
            .set({...local.toMap(), 'user_id': userId});
      }
    }

    // Pull remote to local
    for (var remote in remoteRecurring) {
      final local = localRecurring.firstWhere(
        (l) => l.id == remote.id,
        orElse: () => RecurringTransaction(id: 'none', title: '', amount: 0, categoryId: '', repeatType: 'monthly'),
      );

      if (local.id == 'none' || remote.lastModified.isAfter(local.lastModified)) {
        await _dbService.insertRecurring(remote);
      }
    }
  }

  // --- Real-time Streams ---

  Stream<List<Category>> categoriesStream(String userId) {
    return _firestore
        .collection('users')
        .doc(userId)
        .collection('categories')
        .snapshots()
        .map((snapshot) {
      return snapshot.docs
          .map((doc) => Category.fromMap({...doc.data(), 'id': doc.id}))
          .toList();
    });
  }

  Stream<List<Transaction>> transactionsStream(String userId) {
    return _firestore
        .collection('users')
        .doc(userId)
        .collection('transactions')
        .snapshots()
        .map((snapshot) {
      return snapshot.docs
          .map((doc) => Transaction.fromMap({...doc.data(), 'id': doc.id}))
          .toList();
    });
  }

  Stream<List<FinancialGoal>> goalsStream(String userId) {
    return _firestore
        .collection('users')
        .doc(userId)
        .collection('goals')
        .snapshots()
        .map((snapshot) {
      return snapshot.docs
          .map((doc) => FinancialGoal.fromMap({...doc.data(), 'id': doc.id}))
          .toList();
    });
  }

  Stream<List<RecurringTransaction>> recurringStream(String userId) {
    return _firestore
        .collection('users')
        .doc(userId)
        .collection('recurring')
        .snapshots()
        .map((snapshot) {
      return snapshot.docs
          .map((doc) => RecurringTransaction.fromMap({...doc.data(), 'id': doc.id}))
          .toList();
    });
  }
}
