import 'package:sqflite/sqflite.dart' hide Transaction;
import 'package:path/path.dart';
import '../models/models.dart';

class DatabaseService {
  static final DatabaseService instance = DatabaseService._init();
  static Database? _database;

  DatabaseService._init();

  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDB('smart_budget.db');
    return _database!;
  }

  Future<Database> _initDB(String filePath) async {
    final dbPath = await getDatabasesPath();
    final path = join(dbPath, filePath);

    return await openDatabase(
      path,
      version: 3, // Bumped for recurring table
      onCreate: _createDB,
      onUpgrade: _onUpgrade,
    );
  }

  Future _createDB(Database db, int version) async {
    await db.execute('''
      CREATE TABLE categories(
        id TEXT PRIMARY KEY,
        name TEXT NOT NULL,
        color TEXT NOT NULL,
        icon TEXT NOT NULL,
        monthly_budget REAL,
        user_id TEXT,
        last_modified TEXT NOT NULL
      )
    ''');

    await db.execute('''
      CREATE TABLE transactions(
        id TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        amount REAL NOT NULL,
        type TEXT NOT NULL,
        date TEXT NOT NULL,
        category_id TEXT NOT NULL,
        note TEXT,
        receipt_url TEXT,
        currency TEXT NOT NULL,
        user_id TEXT,
        last_modified TEXT NOT NULL,
        FOREIGN KEY (category_id) REFERENCES categories (id)
      )
    ''');

    await db.execute('''
      CREATE TABLE goals(
        id TEXT PRIMARY KEY,
        name TEXT NOT NULL,
        target_amount REAL NOT NULL,
        saved_amount REAL NOT NULL,
        deadline TEXT NOT NULL,
        user_id TEXT,
        last_modified TEXT NOT NULL
      )
    ''');

    await db.execute('''
      CREATE TABLE recurring(
        id TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        amount REAL NOT NULL,
        category_id TEXT NOT NULL,
        repeat_type TEXT NOT NULL,
        user_id TEXT,
        last_modified TEXT NOT NULL,
        FOREIGN KEY (category_id) REFERENCES categories (id)
      )
    ''');

    // Insert Default Categories
    final List<Category> defaults = [
      Category(id: '1', name: 'Coffee & Dining', color: 'FFF59E0B', icon: 'local_cafe', monthlyBudget: 200),
      Category(id: '2', name: 'Housing', color: 'FF3B82F6', icon: 'home_work', monthlyBudget: 1500),
      Category(id: '3', name: 'Transport', color: 'FF10B981', icon: 'directions_car', monthlyBudget: 300),
      Category(id: '4', name: 'Income', color: 'FF8B5CF6', icon: 'work'),
      Category(id: '5', name: 'Shopping', color: 'FFEF4444', icon: 'shopping_cart', monthlyBudget: 400),
      Category(id: '6', name: 'Entertainment', color: 'FF6366F1', icon: 'movie', monthlyBudget: 150),
    ];

    for (var cat in defaults) {
      await db.insert('categories', cat.toMap());
    }
  }

  Future _onUpgrade(Database db, int oldVersion, int newVersion) async {
    if (oldVersion < 2) {
      await db.execute('ALTER TABLE categories ADD COLUMN user_id TEXT');
      await db.execute('ALTER TABLE categories ADD COLUMN last_modified TEXT NOT NULL DEFAULT "${DateTime.now().toIso8601String()}"');
      
      await db.execute('ALTER TABLE transactions ADD COLUMN user_id TEXT');
      await db.execute('ALTER TABLE transactions ADD COLUMN last_modified TEXT NOT NULL DEFAULT "${DateTime.now().toIso8601String()}"');
      
      await db.execute('ALTER TABLE goals ADD COLUMN user_id TEXT');
      await db.execute('ALTER TABLE goals ADD COLUMN last_modified TEXT NOT NULL DEFAULT "${DateTime.now().toIso8601String()}"');
    }
    if (oldVersion < 3) {
      await db.execute('''
        CREATE TABLE IF NOT EXISTS recurring(
          id TEXT PRIMARY KEY,
          title TEXT NOT NULL,
          amount REAL NOT NULL,
          category_id TEXT NOT NULL,
          repeat_type TEXT NOT NULL,
          user_id TEXT,
          last_modified TEXT NOT NULL,
          FOREIGN KEY (category_id) REFERENCES categories (id)
        )
      ''');
    }
  }

  // Categories CRUD
  Future<List<Category>> getAllCategories() async {
    final db = await instance.database;
    final result = await db.query('categories');
    return result.map((json) => Category.fromMap(json)).toList();
  }

  Future<void> insertCategory(Category category) async {
    final db = await instance.database;
    await db.insert('categories', category.toMap(), conflictAlgorithm: ConflictAlgorithm.replace);
  }

  // Transactions CRUD
  Future<void> insertTransaction(Transaction transaction) async {
    final db = await instance.database;
    await db.insert(
      'transactions', 
      transaction.toMap(),
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  Future<List<Transaction>> getAllTransactions() async {
    final db = await instance.database;
    final result = await db.query('transactions', orderBy: 'date DESC');
    return result.map((json) => Transaction.fromMap(json)).toList();
  }

  Future<void> deleteTransaction(String id) async {
    final db = await instance.database;
    await db.delete('transactions', where: 'id = ?', whereArgs: [id]);
  }

  // Goals CRUD
  Future<void> insertGoal(FinancialGoal goal) async {
    final db = await instance.database;
    await db.insert(
      'goals', 
      goal.toMap(),
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  Future<List<FinancialGoal>> getAllGoals() async {
    final db = await instance.database;
    final result = await db.query('goals');
    return result.map((json) => FinancialGoal.fromMap(json)).toList();
  }

  // Recurring CRUD
  Future<void> insertRecurring(RecurringTransaction recurring) async {
    final db = await instance.database;
    await db.insert('recurring', recurring.toMap(), conflictAlgorithm: ConflictAlgorithm.replace);
  }

  Future<List<RecurringTransaction>> getAllRecurring() async {
    final db = await instance.database;
    final result = await db.query('recurring');
    return result.map((json) => RecurringTransaction.fromMap(json)).toList();
  }

  Future<void> deleteRecurring(String id) async {
    final db = await instance.database;
    await db.delete('recurring', where: 'id = ?', whereArgs: [id]);
  }

  // Update methods for sync
  Future<void> updateCategory(Category category) async {
    final db = await instance.database;
    await db.insert('categories', category.toMap(), conflictAlgorithm: ConflictAlgorithm.replace);
  }

  Future<void> updateFinancialGoal(FinancialGoal goal) async {
    final db = await instance.database;
    await db.insert('goals', goal.toMap(), conflictAlgorithm: ConflictAlgorithm.replace);
  }

  Future<void> deleteAllData() async {
    final db = await instance.database;
    await db.delete('transactions');
    await db.delete('categories');
    await db.delete('goals');
    await db.delete('recurring');
  }

  Future<void> close() async {
    final db = await instance.database;
    db.close();
  }
}
