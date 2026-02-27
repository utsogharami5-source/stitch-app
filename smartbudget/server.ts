import express from "express";
import { createServer as createViteServer } from "vite";
import Database from "better-sqlite3";
import path from "path";
import { fileURLToPath } from "url";
import dotenv from "dotenv";

dotenv.config();

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const db = new Database("smartbudget.db");

// Initialize Database Schema
db.exec(`
  CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    email TEXT UNIQUE,
    monthly_budget REAL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  );

  CREATE TABLE IF NOT EXISTS categories (
    category_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    type TEXT CHECK(type IN ('Income', 'Expense')) NOT NULL,
    icon TEXT,
    color TEXT
  );

  CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    type TEXT CHECK(type IN ('Income', 'Expense')) NOT NULL,
    amount REAL NOT NULL,
    category_id INTEGER,
    date TEXT NOT NULL,
    note TEXT,
    FOREIGN KEY(user_id) REFERENCES users(user_id),
    FOREIGN KEY(category_id) REFERENCES categories(category_id)
  );

  CREATE TABLE IF NOT EXISTS savings_goals (
    goal_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    title TEXT NOT NULL,
    target_amount REAL NOT NULL,
    saved_amount REAL DEFAULT 0,
    deadline TEXT,
    FOREIGN KEY(user_id) REFERENCES users(user_id)
  );
`);

// Seed default categories if empty
const categoryCount = db.prepare("SELECT COUNT(*) as count FROM categories").get() as { count: number };
if (categoryCount.count === 0) {
  const insertCategory = db.prepare("INSERT INTO categories (name, type, icon, color) VALUES (?, ?, ?, ?)");
  const defaultCategories = [
    ['Salary', 'Income', 'Briefcase', '#34C759'],
    ['Freelance', 'Income', 'Laptop', '#34C759'],
    ['Food', 'Expense', 'Utensils', '#FF3B30'],
    ['Transport', 'Expense', 'Car', '#007AFF'],
    ['Shopping', 'Expense', 'ShoppingBag', '#FF9500'],
    ['Rent', 'Expense', 'Home', '#5856D6'],
    ['Entertainment', 'Expense', 'Play', '#AF52DE'],
    ['Health', 'Expense', 'Heart', '#FF2D55'],
  ];
  defaultCategories.forEach(cat => insertCategory.run(...cat));
}

// Seed a default user if none exists
const userCount = db.prepare("SELECT COUNT(*) as count FROM users").get() as { count: number };
if (userCount.count === 0) {
  db.prepare("INSERT INTO users (name, email, monthly_budget) VALUES (?, ?, ?)").run("Demo User", "demo@example.com", 50000);
}

const app = express();
app.use(express.json());

const PORT = 3000;

// API Routes
app.get("/api/user", (req, res) => {
  const user = db.prepare("SELECT * FROM users LIMIT 1").get();
  res.json(user);
});

app.get("/api/categories", (req, res) => {
  const categories = db.prepare("SELECT * FROM categories").all();
  res.json(categories);
});

app.get("/api/transactions", (req, res) => {
  const transactions = db.prepare(`
    SELECT t.*, c.name as category_name, c.icon as category_icon, c.color as category_color 
    FROM transactions t 
    LEFT JOIN categories c ON t.category_id = c.category_id 
    ORDER BY date DESC
  `).all();
  res.json(transactions);
});

app.post("/api/transactions", (req, res) => {
  const { type, amount, category_id, date, note } = req.body;
  const user = db.prepare("SELECT user_id FROM users LIMIT 1").get() as { user_id: number };
  const result = db.prepare(`
    INSERT INTO transactions (user_id, type, amount, category_id, date, note) 
    VALUES (?, ?, ?, ?, ?, ?)
  `).run(user.user_id, type, amount, category_id, date, note);
  res.json({ id: result.lastInsertRowid });
});

app.get("/api/goals", (req, res) => {
  const goals = db.prepare("SELECT * FROM savings_goals").all();
  res.json(goals);
});

app.post("/api/goals", (req, res) => {
  const { title, target_amount, deadline } = req.body;
  const user = db.prepare("SELECT user_id FROM users LIMIT 1").get() as { user_id: number };
  const result = db.prepare(`
    INSERT INTO savings_goals (user_id, title, target_amount, deadline) 
    VALUES (?, ?, ?, ?)
  `).run(user.user_id, title, target_amount, deadline);
  res.json({ id: result.lastInsertRowid });
});

// Vite Middleware
if (process.env.NODE_ENV !== "production") {
  const vite = await createViteServer({
    server: { middlewareMode: true },
    appType: "spa",
  });
  app.use(vite.middlewares);
} else {
  app.use(express.static(path.join(__dirname, "dist")));
  app.get("*", (req, res) => {
    res.sendFile(path.join(__dirname, "dist", "index.html"));
  });
}

app.listen(PORT, "0.0.0.0", () => {
  console.log(`Server running on http://localhost:${PORT}`);
});
