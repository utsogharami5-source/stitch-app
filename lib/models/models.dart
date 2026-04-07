class Transaction {
  final String id;
  final String title;
  final double amount;
  final String type; // 'income' or 'expense'
  final DateTime date;
  final String categoryId;
  final String? note;
  final String? receiptUrl;
  final String currency;
  final String? userId;
  final DateTime lastModified;

  Transaction({
    required this.id,
    required this.title,
    required this.amount,
    required this.type,
    required this.date,
    required this.categoryId,
    this.note,
    this.receiptUrl,
    this.currency = 'USD',
    this.userId,
    DateTime? lastModified,
  }) : lastModified = lastModified ?? DateTime.now();

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'title': title,
      'amount': amount,
      'type': type,
      'date': date.toIso8601String(),
      'category_id': categoryId,
      'note': note,
      'receipt_url': receiptUrl,
      'currency': currency,
      'user_id': userId,
      'last_modified': lastModified.toIso8601String(),
    };
  }

  factory Transaction.fromMap(Map<String, dynamic> map) {
    return Transaction(
      id: map['id'].toString(),
      title: map['title'] ?? '',
      amount: (map['amount'] as num).toDouble(),
      type: map['type'] ?? 'expense',
      date: _parseDateTime(map['date']),
      categoryId: map['category_id']?.toString() ?? '',
      note: map['note'],
      receiptUrl: map['receipt_url'],
      currency: map['currency'] ?? 'USD',
      userId: map['user_id'],
      lastModified: _parseDateTime(map['last_modified']),
    );
  }

  static DateTime _parseDateTime(dynamic value) {
    if (value == null) return DateTime.now();
    if (value is String) return DateTime.parse(value);
    if (value is DateTime) return value;
    // Handle Firestore Timestamp if possible (without importing cloud_firestore here if possible, 
    // but better to just check for 'toDate' method via reflection or just check type)
    try {
      return value.toDate();
    } catch (_) {
      return DateTime.now();
    }
  }
}

class Category {
  final String id;
  final String name;
  final String color; // Hex string e.g. FFF46E5
  final String icon; // Icon name as string
  final double? monthlyBudget;
  final String? userId;
  final DateTime lastModified;

  Category({
    required this.id,
    required this.name,
    required this.color,
    required this.icon,
    this.monthlyBudget,
    this.userId,
    DateTime? lastModified,
  }) : lastModified = lastModified ?? DateTime.now();

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'name': name,
      'color': color,
      'icon': icon,
      'monthly_budget': monthlyBudget,
      'user_id': userId,
      'last_modified': lastModified.toIso8601String(),
    };
  }

  factory Category.fromMap(Map<String, dynamic> map) {
    return Category(
      id: map['id'].toString(),
      name: map['name'] ?? '',
      color: map['color'] ?? 'FF4F46E5',
      icon: map['icon'] ?? 'category',
      monthlyBudget: map['monthly_budget'] != null 
          ? (map['monthly_budget'] as num).toDouble() 
          : null,
      userId: map['user_id'],
      lastModified: Transaction._parseDateTime(map['last_modified']),
    );
  }
}

class FinancialGoal {
  final String id;
  final String name;
  final double targetAmount;
  final double savedAmount;
  final DateTime deadline;
  final String? userId;
  final DateTime lastModified;

  FinancialGoal({
    required this.id,
    required this.name,
    required this.targetAmount,
    required this.savedAmount,
    required this.deadline,
    this.userId,
    DateTime? lastModified,
  }) : lastModified = lastModified ?? DateTime.now();

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'name': name,
      'target_amount': targetAmount,
      'saved_amount': savedAmount,
      'deadline': deadline.toIso8601String(),
      'user_id': userId,
      'last_modified': lastModified.toIso8601String(),
    };
  }

  factory FinancialGoal.fromMap(Map<String, dynamic> map) {
    return FinancialGoal(
      id: map['id'].toString(),
      name: map['name'] ?? '',
      targetAmount: (map['target_amount'] as num).toDouble(),
      savedAmount: (map['saved_amount'] as num).toDouble(),
      deadline: Transaction._parseDateTime(map['deadline']),
      userId: map['user_id'],
      lastModified: Transaction._parseDateTime(map['last_modified']),
    );
  }
}

class RecurringTransaction {
  final String id;
  final String title;
  final double amount;
  final String categoryId;
  final String repeatType; // 'daily', 'weekly', 'monthly'
  final String? userId;
  final DateTime lastModified;

  RecurringTransaction({
    required this.id,
    required this.title,
    required this.amount,
    required this.categoryId,
    required this.repeatType,
    this.userId,
    DateTime? lastModified,
  }) : lastModified = lastModified ?? DateTime.now();

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'title': title,
      'amount': amount,
      'category_id': categoryId,
      'repeat_type': repeatType,
      'user_id': userId,
      'last_modified': lastModified.toIso8601String(),
    };
  }

  factory RecurringTransaction.fromMap(Map<String, dynamic> map) {
    return RecurringTransaction(
      id: map['id'].toString(),
      title: map['title'] ?? '',
      amount: (map['amount'] as num).toDouble(),
      categoryId: map['category_id']?.toString() ?? '',
      repeatType: map['repeat_type'] ?? 'monthly',
      userId: map['user_id'],
      lastModified: Transaction._parseDateTime(map['last_modified']),
    );
  }
}
