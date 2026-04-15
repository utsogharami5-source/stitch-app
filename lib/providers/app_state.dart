import 'dart:async';
import 'package:flutter/material.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:csv/csv.dart';
import 'package:intl/intl.dart';
import '../models/models.dart';
import '../services/database_service.dart';
import '../services/auth_service.dart';
import '../services/sync_service.dart';

class AppState extends ChangeNotifier {
  final AuthService _authService = AuthService();
  final SyncService _syncService = SyncService();
  
  List<Transaction> _transactions = [];
  List<Category> _categories = [];
  List<FinancialGoal> _goals = [];
  List<RecurringTransaction> _recurringTransactions = [];
  bool _isLoading = false;
  bool _isAuthReady = false;
  bool _isGuestMode = false;
  User? _currentUser;

  // Sync Subscriptions
  StreamSubscription? _catSubscription;
  StreamSubscription? _txSubscription;
  StreamSubscription? _goalSubscription;
  StreamSubscription? _recurringSubscription;

  // Theme & Preferences
  ThemeMode _themeMode = ThemeMode.light;
  String _currencySymbol = '৳'; // Default to BDT
  String _currencyCode = 'BDT';

  // Filtering & Selected Month
  DateTime _selectedMonth = DateTime(DateTime.now().year, DateTime.now().month);
  String _transactionFilter = 'all'; // 'all', 'income', 'expense'

  List<Transaction> get transactions {
    return _transactions.where((tx) {
      final isSameMonth = tx.date.year == _selectedMonth.year && tx.date.month == _selectedMonth.month;
      if (!isSameMonth) return false;
      
      if (_transactionFilter == 'all') return true;
      return tx.type == _transactionFilter;
    }).toList();
  }

  List<Transaction> get allTransactions => List.unmodifiable(_transactions);

  List<Category> get categories => _categories;
  List<FinancialGoal> get goals => _goals;
  List<RecurringTransaction> get recurringTransactions => _recurringTransactions;
  bool get isLoading => _isLoading;
  bool get isAuthReady => _isAuthReady;
  User? get currentUser => _currentUser;
  bool get isAuthenticated => _currentUser != null || _isGuestMode;
  bool get isGuestMode => _isGuestMode;
  ThemeMode get themeMode => _themeMode;
  String get currencySymbol => _currencySymbol;
  String get currencyCode => _currencyCode;
  DateTime get selectedMonth => _selectedMonth;
  String get transactionFilter => _transactionFilter;

  // Available currencies
  static const Map<String, String> availableCurrencies = {
    'BDT': '৳',
    'USD': '\$',
    'EUR': '€',
    'GBP': '£',
    'INR': '₹',
    'JPY': '¥',
    'CNY': '¥',
    'KRW': '₩',
    'SAR': '﷼',
    'AED': 'د.إ',
  };

  AppState() {
    _initPreferences();
    _initAuthListener();
    loadData();
  }

  @override
  void dispose() {
    _cancelSubscriptions();
    super.dispose();
  }

  void _cancelSubscriptions() {
    _catSubscription?.cancel();
    _txSubscription?.cancel();
    _goalSubscription?.cancel();
    _recurringSubscription?.cancel();
  }

  Future<void> _initPreferences() async {
    final prefs = await SharedPreferences.getInstance();
    final themeStr = prefs.getString('theme_mode') ?? 'light';
    _themeMode = themeStr == 'dark' ? ThemeMode.dark : ThemeMode.light;
    _currencyCode = prefs.getString('currency_code') ?? 'BDT';
    _currencySymbol = availableCurrencies[_currencyCode] ?? '৳';
    notifyListeners();
  }

  Future<void> setThemeMode(ThemeMode mode) async {
    _themeMode = mode;
    notifyListeners();
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('theme_mode', mode == ThemeMode.dark ? 'dark' : 'light');
  }

  Future<void> setCurrency(String code) async {
    _currencyCode = code;
    _currencySymbol = availableCurrencies[code] ?? code;
    notifyListeners();
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('currency_code', code);
  }

  void setSelectedMonth(DateTime month) {
    _selectedMonth = DateTime(month.year, month.month);
    notifyListeners();
  }

  void setTransactionFilter(String filter) {
    _transactionFilter = filter;
    notifyListeners();
  }

  String formatCurrency(double amount) {
    final formatted = amount.toStringAsFixed(2);
    // Add thousand separators
    final parts = formatted.split('.');
    final intPart = parts[0].replaceAllMapped(
      RegExp(r'(\d{1,3})(?=(\d{3})+(?!\d))'),
      (Match m) => '${m[1]},',
    );
    return '$_currencySymbol$intPart.${parts[1]}';
  }

  void _initAuthListener() {
    _authService.user.listen((user) async {
      _currentUser = user;
      _isAuthReady = true;
      
      if (user != null) {
        _isGuestMode = false;
        _startSyncSubscriptions(user.uid);
        try {
          await _syncService.createUserProfile(user);
        } catch (e) {
          debugPrint('Error creating user profile: $e');
        }
        await syncNow();
      } else {
        _cancelSubscriptions();
      }
      
      notifyListeners();
    });
  }

  void _startSyncSubscriptions(String userId) {
    _cancelSubscriptions();

    _catSubscription = _syncService.categoriesStream(userId).listen((remoteCategories) async {
      for (var remote in remoteCategories) {
        final local = _categories.firstWhere(
          (l) => l.id == remote.id,
          orElse: () => Category(id: 'none', name: '', color: '', icon: ''),
        );

        if (local.id == 'none' || remote.lastModified.isAfter(local.lastModified)) {
          await DatabaseService.instance.updateCategory(remote);
        }
      }
      await loadData();
    });

    _txSubscription = _syncService.transactionsStream(userId).listen((remoteTransactions) async {
      for (var remote in remoteTransactions) {
        final local = _transactions.firstWhere(
          (l) => l.id == remote.id,
          orElse: () => Transaction(id: 'none', title: '', amount: 0, type: 'expense', date: DateTime.now(), categoryId: ''),
        );

        if (local.id == 'none' || remote.lastModified.isAfter(local.lastModified)) {
          await DatabaseService.instance.insertTransaction(remote);
        }
      }
      await loadData();
    });

    _goalSubscription = _syncService.goalsStream(userId).listen((remoteGoals) async {
      for (var remote in remoteGoals) {
        final local = _goals.firstWhere(
          (l) => l.id == remote.id,
          orElse: () => FinancialGoal(id: 'none', name: '', targetAmount: 0, savedAmount: 0, deadline: DateTime.now()),
        );

        if (local.id == 'none' || remote.lastModified.isAfter(local.lastModified)) {
          await DatabaseService.instance.updateFinancialGoal(remote);
        }
      }
      await loadData();
    });

    _recurringSubscription = _syncService.recurringStream(userId).listen((remoteRecurring) async {
      for (var remote in remoteRecurring) {
        final local = _recurringTransactions.firstWhere(
          (l) => l.id == remote.id,
          orElse: () => RecurringTransaction(id: 'none', title: '', amount: 0, categoryId: '', repeatType: 'monthly'),
        );

        if (local.id == 'none' || remote.lastModified.isAfter(local.lastModified)) {
          await DatabaseService.instance.insertRecurring(remote);
        }
      }
      await loadData();
    });
  }

  void continueAsGuest() {
    _isGuestMode = true;
    _isAuthReady = true;
    _currentUser = null;
    notifyListeners();
  }

  Future<void> syncNow() async {
    if (_currentUser == null) return;
    
    _isLoading = true;
    notifyListeners();
    
    try {
      // Perform a full sequential sync to ensure data integrity
      await _syncService.syncData(_currentUser!.uid);
      await loadData();
    } catch (e) {
      debugPrint('Sync error: $e');
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> loadData() async {
    _transactions = await DatabaseService.instance.getAllTransactions();
    _categories = await DatabaseService.instance.getAllCategories();
    _goals = await DatabaseService.instance.getAllGoals();
    _recurringTransactions = await DatabaseService.instance.getAllRecurring();
    notifyListeners();
  }

  // Auth Methods
  Future<void> signInWithGoogle() async {
    await _authService.signInWithGoogle();
  }

  Future<void> signOut() async {
    await _authService.signOut();
    _currentUser = null;
    _isGuestMode = false;
    notifyListeners();
  }

  // Data Operations
  Future<void> addTransaction(Transaction transaction) async {
    final updatedTransaction = Transaction(
      id: transaction.id,
      title: transaction.title,
      amount: transaction.amount,
      type: transaction.type,
      date: transaction.date,
      categoryId: transaction.categoryId,
      note: transaction.note,
      receiptUrl: transaction.receiptUrl,
      currency: _currencyCode,
      userId: _currentUser?.uid,
      lastModified: DateTime.now(),
    );

    await DatabaseService.instance.insertTransaction(updatedTransaction);
    await loadData();
    
    if (_currentUser != null) {
      _syncService.syncTransactions(_currentUser!.uid);
    }
  }

  Future<void> deleteTransaction(String id) async {
    if (_currentUser != null) {
      await _syncService.deleteCloudItem(_currentUser!.uid, 'transactions', id);
    }
    await DatabaseService.instance.deleteTransaction(id);
    await loadData();
    if (_currentUser != null) {
      _syncService.syncTransactions(_currentUser!.uid);
    }
  }

  Future<void> deleteRecurring(String id) async {
    if (_currentUser != null) {
      await _syncService.deleteCloudItem(_currentUser!.uid, 'recurring', id);
    }
    await DatabaseService.instance.deleteRecurring(id);
    await loadData();
    if (_currentUser != null) {
      _syncService.syncRecurring(_currentUser!.uid);
    }
  }

  Future<void> addCategory(Category category) async {
    final updatedCategory = Category(
      id: category.id,
      name: category.name,
      color: category.color,
      icon: category.icon,
      monthlyBudget: category.monthlyBudget,
      userId: _currentUser?.uid,
      lastModified: DateTime.now(),
    );

    await DatabaseService.instance.insertCategory(updatedCategory);
    await loadData();

    if (_currentUser != null) {
      _syncService.syncCategories(_currentUser!.uid);
    }
  }

  Future<void> addGoal(FinancialGoal goal) async {
    final updatedGoal = FinancialGoal(
      id: goal.id,
      name: goal.name,
      targetAmount: goal.targetAmount,
      savedAmount: goal.savedAmount,
      deadline: goal.deadline,
      userId: _currentUser?.uid,
      lastModified: DateTime.now(),
    );

    await DatabaseService.instance.insertGoal(updatedGoal);
    await loadData();

    if (_currentUser != null) {
      _syncService.syncGoals(_currentUser!.uid);
    }
  }

  Future<void> deleteAllData() async {
    if (_currentUser != null) {
      await _syncService.deleteUserData(_currentUser!.uid);
    }
    await DatabaseService.instance.deleteAllData();
    await loadData();
  }

  Future<void> deleteUserAccount() async {
    if (_currentUser == null) return;
    
    _isLoading = true;
    notifyListeners();
    
    try {
      final uid = _currentUser!.uid;
      
      // 1. Delete remote data
      await _syncService.deleteUserData(uid);
      
      // 2. Delete local data
      await DatabaseService.instance.deleteAllData();
      await loadData();
      
      // 3. Delete Auth account
      await _authService.deleteAccount();
      
      _currentUser = null;
      _isGuestMode = false;
    } catch (e) {
      debugPrint('Error deleting account: $e');
      rethrow;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> importTransactionsFromCsv(String csvContent) async {
    String normalizedContent = csvContent.replaceAll('\r\n', '\n');
    final List<List<dynamic>> rows = const CsvToListConverter(eol: '\n').convert(normalizedContent);
    if (rows.length < 2) return; // Only header or empty

    _isLoading = true;
    notifyListeners();

    try {
      // Robust header detection
      final firstRow = rows[0].map((e) => e.toString().toLowerCase().trim()).toList();
      
      int dateIdx = -1, titleIdx = -1, typeIdx = -1, amountIdx = -1, categoryIdx = -1, noteIdx = -1;

      // Fuzzy matching for headers
      for (int i = 0; i < firstRow.length; i++) {
        final h = firstRow[i];
        if (h.contains('date')) {
          dateIdx = i;
        } else if (h.contains('title') || h.contains('description') || h.contains('detail')) {
          titleIdx = i;
        } else if (h.contains('type')) {
          typeIdx = i;
        } else if (h.contains('amount') || h.contains('sum') || h.contains('value')) {
          amountIdx = i;
        } else if (h.contains('cat')) {
          categoryIdx = i;
        } else if (h.contains('note') || h.contains('memo')) {
          noteIdx = i;
        }
      }

      bool hasHeader = dateIdx != -1 || titleIdx != -1 || amountIdx != -1;
      int startAt = hasHeader ? 1 : 0;

      // Fallback for headerless files
      if (!hasHeader) {
        debugPrint('No valid headers found. Using default column order: Date, Title, Type, Amount, Category');
        dateIdx = 0;
        titleIdx = 1;
        typeIdx = 2;
        amountIdx = 3;
        categoryIdx = 4;
        noteIdx = 5;
      }

      for (int i = startAt; i < rows.length; i++) {
        final row = rows[i];
        if (row.length <= (amountIdx != -1 ? amountIdx : 0)) continue;

        final dateStr = (dateIdx != -1 && row.length > dateIdx) ? row[dateIdx].toString().trim() : '';
        final title = (titleIdx != -1 && row.length > titleIdx) ? row[titleIdx].toString().trim() : 'Imported Item';
        final typeStr = (typeIdx != -1 && row.length > typeIdx) ? row[typeIdx].toString().toLowerCase() : 'expense';
        final amountRaw = (amountIdx != -1 && row.length > amountIdx) ? row[amountIdx].toString() : '0';
        final amountStr = amountRaw.replaceAll(RegExp(r'[^\d.]'), '');
        final amount = double.tryParse(amountStr) ?? 0.0;
        final categoryName = (categoryIdx != -1 && row.length > categoryIdx) ? row[categoryIdx].toString().trim() : 'Other';
        final note = (noteIdx != -1 && row.length > noteIdx) ? row[noteIdx].toString().trim() : null;

        if (dateStr.isEmpty && title == 'Imported Item' && amount == 0) continue;
        final formats = [
          'yyyy-MM-dd',
          'MM/dd/yyyy',
          'dd/MM/yyyy',
          'yyyy/MM/dd',
          'MMM dd, yyyy',
        ];

        DateTime date = DateTime.now();
        for (var format in formats) {
          try {
            date = DateFormat(format).parse(dateStr);
            break;
          } catch (_) {}
        }

        // Find or create category
        String categoryId;
        final existingCat = _categories.firstWhere(
          (c) => c.name.toLowerCase() == categoryName.toLowerCase(),
          orElse: () => Category(id: 'unknown', name: 'Other', color: 'FF999999', icon: 'help'),
        );

        if (existingCat.id == 'unknown' && categoryName.isNotEmpty) {
          final newCat = Category(
            id: 'imported_cat_${DateTime.now().millisecondsSinceEpoch}_$i',
            name: categoryName,
            color: 'FF4F46E5',
            icon: 'account_balance_wallet',
            userId: _currentUser?.uid,
            lastModified: DateTime.now(),
          );
          await addCategory(newCat);
          categoryId = newCat.id;
        } else {
          categoryId = existingCat.id;
        }

        final tx = Transaction(
          id: 'imported_tx_${DateTime.now().millisecondsSinceEpoch}_$i',
          title: title,
          amount: amount,
          type: typeStr.contains('income') ? 'income' : 'expense',
          date: date,
          categoryId: categoryId,
          note: note?.isEmpty ?? true ? null : note,
          userId: _currentUser?.uid,
          lastModified: DateTime.now(),
        );

        await DatabaseService.instance.insertTransaction(tx);
      }
      await loadData();
      
      // Sync to cloud if user is logged in
      if (_currentUser != null) {
        await _syncService.syncCategories(_currentUser!.uid);
        await _syncService.syncTransactions(_currentUser!.uid);
      }
    } catch (e) {
      debugPrint('CSV Import Error: $e');
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  // Statistics
  double get totalBalance {
    double balance = 0;
    for (var tx in _transactions) {
      if (tx.date.year != _selectedMonth.year || tx.date.month != _selectedMonth.month) continue;
      if (tx.type == 'income') {
        balance += tx.amount;
      } else {
        balance -= tx.amount;
      }
    }
    return balance;
  }

  double get totalIncome {
    double income = 0;
    for (var tx in _transactions) {
      if (tx.date.year != _selectedMonth.year || tx.date.month != _selectedMonth.month) continue;
      if (tx.type == 'income') income += tx.amount;
    }
    return income;
  }

  double get totalExpenses {
    double expenses = 0;
    for (var tx in _transactions) {
      if (tx.date.year != _selectedMonth.year || tx.date.month != _selectedMonth.month) continue;
      if (tx.type == 'expense') expenses += tx.amount;
    }
    return expenses;
  }
}
