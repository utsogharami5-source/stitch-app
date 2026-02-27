import React, { useState, useEffect } from 'react';
import { 
  Home, 
  PlusCircle, 
  PieChart, 
  Target, 
  User, 
  ArrowUpRight, 
  ArrowDownLeft, 
  ChevronRight,
  Sparkles,
  Calendar,
  Wallet,
  TrendingUp,
  TrendingDown,
  LayoutGrid
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer, 
  PieChart as RePieChart, 
  Pie, 
  Cell 
} from 'recharts';
import Markdown from 'react-markdown';
import { GoogleGenAI } from "@google/genai";

// Initialize Gemini
const genAI = new GoogleGenAI({ apiKey: process.env.GEMINI_API_KEY || '' });

// Types
interface Transaction {
  transaction_id: number;
  type: 'Income' | 'Expense';
  amount: number;
  category_id: number;
  category_name: string;
  category_icon: string;
  category_color: string;
  date: string;
  note: string;
}

interface Category {
  category_id: number;
  name: string;
  type: 'Income' | 'Expense';
  icon: string;
  color: string;
}

interface Goal {
  goal_id: number;
  title: string;
  target_amount: number;
  saved_amount: number;
  deadline: string;
}

interface UserProfile {
  name: string;
  email: string;
  monthly_budget: number;
}

export default function App() {
  const [activeTab, setActiveTab] = useState('home');
  const [isDarkMode, setIsDarkMode] = useState(() => {
    if (typeof window !== 'undefined') {
      return localStorage.getItem('darkMode') === 'true';
    }
    return false;
  });
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [goals, setGoals] = useState<Goal[]>([]);
  const [user, setUser] = useState<UserProfile | null>(null);
  const [aiAdvice, setAiAdvice] = useState<string>('');
  const [loadingAdvice, setLoadingAdvice] = useState(false);
  const [showAddModal, setShowAddModal] = useState(false);

  // Form states
  const [newTx, setNewTx] = useState({
    type: 'Expense',
    amount: '',
    category_id: '',
    date: new Date().toISOString().split('T')[0],
    note: ''
  });

  useEffect(() => {
    fetchData();
  }, []);

  useEffect(() => {
    if (isDarkMode) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
    localStorage.setItem('darkMode', isDarkMode.toString());
  }, [isDarkMode]);

  const fetchData = async () => {
    try {
      const [userRes, txRes, catRes, goalRes] = await Promise.all([
        fetch('/api/user'),
        fetch('/api/transactions'),
        fetch('/api/categories'),
        fetch('/api/goals')
      ]);
      setUser(await userRes.json());
      setTransactions(await txRes.json());
      setCategories(await catRes.json());
      setGoals(await goalRes.json());
    } catch (err) {
      console.error('Fetch error:', err);
    }
  };

  const handleAddTransaction = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const res = await fetch('/api/transactions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ...newTx,
          amount: parseFloat(newTx.amount),
          category_id: parseInt(newTx.category_id)
        })
      });
      if (res.ok) {
        setShowAddModal(false);
        setNewTx({
          type: 'Expense',
          amount: '',
          category_id: '',
          date: new Date().toISOString().split('T')[0],
          note: ''
        });
        fetchData();
      }
    } catch (err) {
      console.error('Add tx error:', err);
    }
  };

  const getAiAdvice = async () => {
    if (!user || transactions.length === 0) return;
    setLoadingAdvice(true);
    try {
      const response = await genAI.models.generateContent({
        model: "gemini-3-flash-preview",
        contents: `
          As a financial advisor for a user in Bangladesh, analyze these transactions and provide 3-4 concise, actionable insights or predictions for next month.
          User Monthly Budget: ৳${user.monthly_budget}
          Transactions: ${JSON.stringify(transactions.map(t => ({ type: t.type, amount: t.amount, category: t.category_name, date: t.date })))}
          Format the response in Markdown. Keep it encouraging and iOS-style minimalist (concise).
        `,
      });
      setAiAdvice(response.text || 'No advice available at the moment.');
    } catch (err) {
      console.error('AI error:', err);
      setAiAdvice('Sorry, I couldn\'t generate advice right now. Please check your API key or try again later.');
    } finally {
      setLoadingAdvice(false);
    }
  };

  const totalIncome = transactions
    .filter(t => t.type === 'Income')
    .reduce((sum, t) => sum + t.amount, 0);
  
  const totalExpense = transactions
    .filter(t => t.type === 'Expense')
    .reduce((sum, t) => sum + t.amount, 0);

  const balance = totalIncome - totalExpense;

  const renderHome = () => (
    <div className="space-y-6 pb-24">
      <header className="px-6 pt-8 flex justify-between items-center">
        <div>
          <p className="text-ios-text-muted text-sm font-medium">Good Morning,</p>
          <h1 className="text-3xl font-bold tracking-tight text-ios-text">{user?.name}</h1>
        </div>
        <div className="bg-ios-secondary p-2 rounded-full">
          <User className="w-6 h-6 text-ios-text-muted" />
        </div>
      </header>

      {/* Balance Card */}
      <div className="px-6">
        <div className="bg-ios-blue rounded-[32px] p-8 text-white shadow-xl shadow-ios-blue/20">
          <div className="flex justify-between items-start mb-8">
            <div>
              <p className="text-white/70 text-sm font-medium mb-1">Total Balance</p>
              <h2 className="text-4xl font-bold tracking-tight">৳{balance.toLocaleString()}</h2>
            </div>
            <div className="bg-white/20 p-3 rounded-2xl backdrop-blur-md">
              <Wallet className="w-6 h-6" />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div className="bg-white/10 p-4 rounded-2xl backdrop-blur-md">
              <div className="flex items-center gap-2 mb-1">
                <TrendingUp className="w-4 h-4 text-ios-green" />
                <span className="text-xs font-medium text-white/70 uppercase tracking-wider">Income</span>
              </div>
              <p className="text-lg font-bold">৳{totalIncome.toLocaleString()}</p>
            </div>
            <div className="bg-white/10 p-4 rounded-2xl backdrop-blur-md">
              <div className="flex items-center gap-2 mb-1">
                <TrendingDown className="w-4 h-4 text-ios-red" />
                <span className="text-xs font-medium text-white/70 uppercase tracking-wider">Expense</span>
              </div>
              <p className="text-lg font-bold">৳{totalExpense.toLocaleString()}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Budget Progress */}
      <div className="px-6">
        <div className="ios-card">
          <div className="flex justify-between items-center mb-4">
            <h3 className="font-bold text-lg text-ios-text">Monthly Budget</h3>
            <span className="text-ios-blue font-semibold text-sm">৳{user?.monthly_budget.toLocaleString()}</span>
          </div>
          <div className="h-3 bg-ios-secondary rounded-full overflow-hidden mb-2">
            <motion.div 
              initial={{ width: 0 }}
              animate={{ width: `${Math.min((totalExpense / (user?.monthly_budget || 1)) * 100, 100)}%` }}
              className={`h-full rounded-full ${totalExpense > (user?.monthly_budget || 0) ? 'bg-ios-red' : 'bg-ios-blue'}`}
            />
          </div>
          <p className="text-xs text-ios-text-muted">
            {totalExpense > (user?.monthly_budget || 0) 
              ? `You've exceeded your budget by ৳${(totalExpense - (user?.monthly_budget || 0)).toLocaleString()}`
              : `৳${((user?.monthly_budget || 0) - totalExpense).toLocaleString()} remaining for this month`}
          </p>
        </div>
      </div>

      {/* AI Advice */}
      <div className="px-6">
        <div className="bg-gradient-to-br from-indigo-50 to-blue-50 dark:from-indigo-950 dark:to-blue-950 border border-indigo-100 dark:border-indigo-900 rounded-[24px] p-6">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <Sparkles className="w-5 h-5 text-indigo-600 dark:text-indigo-400" />
              <h3 className="font-bold text-indigo-900 dark:text-indigo-100">Smart Insights</h3>
            </div>
            <button 
              onClick={getAiAdvice}
              disabled={loadingAdvice}
              className="text-xs font-bold text-indigo-600 dark:text-indigo-400 bg-white dark:bg-ios-card px-3 py-1.5 rounded-full shadow-sm hover:shadow-md transition-all active:scale-95 disabled:opacity-50"
            >
              {loadingAdvice ? 'Analyzing...' : 'Refresh'}
            </button>
          </div>
          {aiAdvice ? (
            <div className="prose prose-sm text-indigo-800/80 dark:text-indigo-200/80 prose-p:leading-relaxed">
              <Markdown>{aiAdvice}</Markdown>
            </div>
          ) : (
            <p className="text-sm text-indigo-800/60 dark:text-indigo-200/60 italic">Tap refresh to get AI-powered financial advice based on your spending patterns.</p>
          )}
        </div>
      </div>

      {/* Recent Transactions */}
      <div className="px-6">
        <div className="flex justify-between items-center mb-4">
          <h3 className="font-bold text-lg text-ios-text">Recent Transactions</h3>
          <button onClick={() => setActiveTab('transactions')} className="text-ios-blue text-sm font-semibold">See All</button>
        </div>
        <div className="space-y-3">
          {transactions.slice(0, 5).map(tx => (
            <div key={tx.transaction_id} className="flex items-center justify-between p-4 bg-ios-card rounded-2xl shadow-sm border border-ios-border">
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-2xl flex items-center justify-center" style={{ backgroundColor: `${tx.category_color}15`, color: tx.category_color }}>
                  {tx.type === 'Income' ? <ArrowUpRight className="w-6 h-6" /> : <ArrowDownLeft className="w-6 h-6" />}
                </div>
                <div>
                  <p className="font-bold text-sm text-ios-text">{tx.category_name}</p>
                  <p className="text-xs text-ios-text-muted">{new Date(tx.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}</p>
                </div>
              </div>
              <p className={`font-bold ${tx.type === 'Income' ? 'text-ios-green' : 'text-ios-red'}`}>
                {tx.type === 'Income' ? '+' : '-'}৳{tx.amount.toLocaleString()}
              </p>
            </div>
          ))}
          {transactions.length === 0 && (
            <div className="text-center py-8 text-ios-text-muted">
              <p className="text-sm">No transactions yet.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );

  const renderReports = () => {
    const data = transactions.reduce((acc: any[], tx) => {
      const date = new Date(tx.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
      const existing = acc.find(d => d.date === date);
      if (existing) {
        if (tx.type === 'Income') existing.income += tx.amount;
        else existing.expense += tx.amount;
      } else {
        acc.push({ date, income: tx.type === 'Income' ? tx.amount : 0, expense: tx.type === 'Expense' ? tx.amount : 0 });
      }
      return acc;
    }, []).reverse().slice(-7);

    const pieData = transactions
      .filter(t => t.type === 'Expense')
      .reduce((acc: any[], tx) => {
        const existing = acc.find(d => d.name === tx.category_name);
        if (existing) existing.value += tx.amount;
        else acc.push({ name: tx.category_name, value: tx.amount, color: tx.category_color });
        return acc;
      }, []);

    return (
      <div className="space-y-6 pb-24 px-6 pt-8">
        <h1 className="text-3xl font-bold tracking-tight mb-6 text-ios-text">Reports</h1>
        
        <div className="ios-card h-80">
          <h3 className="font-bold mb-4 text-ios-text">Weekly Overview</h3>
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={data}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke={isDarkMode ? "#333" : "#f0f0f0"} />
              <XAxis dataKey="date" axisLine={false} tickLine={false} tick={{ fontSize: 10, fill: isDarkMode ? '#888' : '#999' }} />
              <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 10, fill: isDarkMode ? '#888' : '#999' }} />
              <Tooltip 
                contentStyle={{ 
                  borderRadius: '12px', 
                  border: 'none', 
                  boxShadow: '0 4px 20px rgba(0,0,0,0.1)',
                  backgroundColor: isDarkMode ? '#1C1C1E' : '#FFFFFF',
                  color: isDarkMode ? '#FFFFFF' : '#000000'
                }}
                cursor={{ fill: isDarkMode ? '#2C2C2E' : '#f8f8f8' }}
              />
              <Bar dataKey="income" fill="#34C759" radius={[4, 4, 0, 0]} />
              <Bar dataKey="expense" fill="#FF3B30" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="ios-card h-80">
          <h3 className="font-bold mb-4 text-ios-text">Expense by Category</h3>
          <ResponsiveContainer width="100%" height="100%">
            <RePieChart>
              <Pie
                data={pieData}
                cx="50%"
                cy="50%"
                innerRadius={60}
                outerRadius={80}
                paddingAngle={5}
                dataKey="value"
              >
                {pieData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip 
                contentStyle={{ 
                  borderRadius: '12px', 
                  border: 'none', 
                  boxShadow: '0 4px 20px rgba(0,0,0,0.1)',
                  backgroundColor: isDarkMode ? '#1C1C1E' : '#FFFFFF',
                  color: isDarkMode ? '#FFFFFF' : '#000000'
                }}
              />
            </RePieChart>
          </ResponsiveContainer>
          <div className="grid grid-cols-2 gap-2 mt-4">
            {pieData.map((d, i) => (
              <div key={i} className="flex items-center gap-2">
                <div className="w-3 h-3 rounded-full" style={{ backgroundColor: d.color }} />
                <span className="text-xs text-ios-text-muted truncate">{d.name}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  };

  const renderGoals = () => (
    <div className="space-y-6 pb-24 px-6 pt-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold tracking-tight text-ios-text">Savings Goals</h1>
        <button className="bg-ios-blue text-white p-2 rounded-full shadow-lg shadow-ios-blue/20">
          <PlusCircle className="w-6 h-6" />
        </button>
      </div>

      <div className="space-y-4">
        {goals.map(goal => (
          <div key={goal.goal_id} className="ios-card">
            <div className="flex justify-between items-start mb-4">
              <div>
                <h3 className="font-bold text-lg text-ios-text">{goal.title}</h3>
                <p className="text-xs text-ios-text-muted">Target: ৳{goal.target_amount.toLocaleString()}</p>
              </div>
              <div className="bg-ios-blue/10 text-ios-blue px-3 py-1 rounded-full text-xs font-bold">
                {Math.round((goal.saved_amount / goal.target_amount) * 100)}%
              </div>
            </div>
            <div className="h-2 bg-ios-secondary rounded-full overflow-hidden mb-2">
              <div 
                className="h-full bg-ios-blue rounded-full" 
                style={{ width: `${(goal.saved_amount / goal.target_amount) * 100}%` }} 
              />
            </div>
            <div className="flex justify-between items-center text-xs">
              <span className="text-ios-text-muted">৳{goal.saved_amount.toLocaleString()} saved</span>
              <span className="text-ios-blue font-bold">৳{(goal.target_amount - goal.saved_amount).toLocaleString()} to go</span>
            </div>
          </div>
        ))}
        {goals.length === 0 && (
          <div className="text-center py-12 bg-ios-secondary/50 rounded-[32px] border-2 border-dashed border-ios-border">
            <Target className="w-12 h-12 text-ios-text-muted/30 mx-auto mb-4" />
            <p className="text-ios-text-muted font-medium">No active goals</p>
            <p className="text-xs text-ios-text-muted/60 mt-1">Start saving for something special!</p>
          </div>
        )}
      </div>
    </div>
  );

  return (
    <div className="max-w-md mx-auto min-h-screen bg-ios-bg font-sans relative overflow-x-hidden">
      {/* Main Content */}
      <AnimatePresence mode="wait">
        <motion.div
          key={activeTab}
          initial={{ opacity: 0, x: 10 }}
          animate={{ opacity: 1, x: 0 }}
          exit={{ opacity: 0, x: -10 }}
          transition={{ duration: 0.2 }}
        >
          {activeTab === 'home' && renderHome()}
          {activeTab === 'reports' && renderReports()}
          {activeTab === 'goals' && renderGoals()}
          {activeTab === 'transactions' && (
            <div className="space-y-6 pb-24 px-6 pt-8">
              <h1 className="text-3xl font-bold tracking-tight mb-6 text-ios-text">Transactions</h1>
              <div className="space-y-3">
                {transactions.map(tx => (
                  <div key={tx.transaction_id} className="flex items-center justify-between p-4 bg-ios-card rounded-2xl shadow-sm border border-ios-border">
                    <div className="flex items-center gap-4">
                      <div className="w-12 h-12 rounded-2xl flex items-center justify-center" style={{ backgroundColor: `${tx.category_color}15`, color: tx.category_color }}>
                        {tx.type === 'Income' ? <ArrowUpRight className="w-6 h-6" /> : <ArrowDownLeft className="w-6 h-6" />}
                      </div>
                      <div>
                        <p className="font-bold text-sm text-ios-text">{tx.category_name}</p>
                        <p className="text-xs text-ios-text-muted">{new Date(tx.date).toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' })}</p>
                      </div>
                    </div>
                    <p className={`font-bold ${tx.type === 'Income' ? 'text-ios-green' : 'text-ios-red'}`}>
                      {tx.type === 'Income' ? '+' : '-'}৳{tx.amount.toLocaleString()}
                    </p>
                  </div>
                ))}
              </div>
            </div>
          )}
          {activeTab === 'profile' && (
            <div className="space-y-6 pb-24 px-6 pt-8">
              <h1 className="text-3xl font-bold tracking-tight mb-6 text-ios-text">Profile</h1>
              <div className="ios-card flex flex-col items-center text-center py-8">
                <div className="w-24 h-24 bg-ios-blue rounded-full flex items-center justify-center text-white mb-4 shadow-lg shadow-ios-blue/20">
                  <User className="w-12 h-12" />
                </div>
                <h2 className="text-xl font-bold text-ios-text">{user?.name}</h2>
                <p className="text-ios-text-muted text-sm">{user?.email}</p>
              </div>
              
              <div className="space-y-2">
                <div className="ios-card flex items-center justify-between py-4">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-ios-secondary rounded-xl flex items-center justify-center text-ios-text">
                      <LayoutGrid className="w-5 h-5" />
                    </div>
                    <span className="font-medium">Dark Mode</span>
                  </div>
                  <button 
                    onClick={() => setIsDarkMode(!isDarkMode)}
                    className={`w-12 h-6 rounded-full transition-colors relative ${isDarkMode ? 'bg-ios-blue' : 'bg-gray-300'}`}
                  >
                    <motion.div 
                      animate={{ x: isDarkMode ? 24 : 2 }}
                      className="absolute top-1 w-4 h-4 bg-white rounded-full shadow-sm"
                    />
                  </button>
                </div>
                <div className="ios-card flex items-center justify-between py-4">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-ios-secondary rounded-xl flex items-center justify-center text-ios-text">
                      <Wallet className="w-5 h-5" />
                    </div>
                    <span className="font-medium">Monthly Budget</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-ios-text-muted font-medium">৳{user?.monthly_budget.toLocaleString()}</span>
                    <ChevronRight className="w-4 h-4 text-ios-text-muted" />
                  </div>
                </div>
                <div className="ios-card flex items-center justify-between py-4">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-ios-secondary rounded-xl flex items-center justify-center text-ios-text">
                      <Calendar className="w-5 h-5" />
                    </div>
                    <span className="font-medium">Currency</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-ios-text-muted font-medium">BDT (৳)</span>
                    <ChevronRight className="w-4 h-4 text-ios-text-muted" />
                  </div>
                </div>
              </div>
            </div>
          )}
        </motion.div>
      </AnimatePresence>

      {/* Add Transaction Modal */}
      <AnimatePresence>
        {showAddModal && (
          <>
            <motion.div 
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setShowAddModal(false)}
              className="fixed inset-0 bg-black/40 backdrop-blur-sm z-40"
            />
            <motion.div 
              initial={{ y: '100%' }}
              animate={{ y: 0 }}
              exit={{ y: '100%' }}
              transition={{ type: 'spring', damping: 25, stiffness: 200 }}
              className="fixed bottom-0 left-0 right-0 max-w-md mx-auto bg-ios-card rounded-t-[40px] p-8 z-50 shadow-2xl border-t border-ios-border"
            >
              <div className="w-12 h-1.5 bg-ios-secondary rounded-full mx-auto mb-8" />
              <h2 className="text-2xl font-bold mb-6 text-ios-text">Add Transaction</h2>
              
              <form onSubmit={handleAddTransaction} className="space-y-6">
                <div className="flex bg-ios-secondary p-1 rounded-2xl">
                  {['Expense', 'Income'].map(type => (
                    <button
                      key={type}
                      type="button"
                      onClick={() => setNewTx({ ...newTx, type: type as any })}
                      className={`flex-1 py-3 rounded-xl font-bold text-sm transition-all ${newTx.type === type ? 'bg-ios-card shadow-sm text-ios-blue' : 'text-ios-text-muted'}`}
                    >
                      {type}
                    </button>
                  ))}
                </div>

                <div className="space-y-4">
                  <div>
                    <label className="text-xs font-bold text-ios-text-muted uppercase tracking-wider mb-2 block">Amount</label>
                    <div className="relative">
                      <span className="absolute left-4 top-1/2 -translate-y-1/2 text-2xl font-bold text-ios-text-muted">৳</span>
                      <input 
                        type="number" 
                        required
                        value={newTx.amount}
                        onChange={e => setNewTx({ ...newTx, amount: e.target.value })}
                        placeholder="0.00"
                        className="w-full bg-ios-secondary rounded-2xl py-4 pl-10 pr-4 text-2xl font-bold text-ios-text focus:outline-none focus:ring-2 focus:ring-ios-blue/20"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="text-xs font-bold text-ios-text-muted uppercase tracking-wider mb-2 block">Category</label>
                    <select 
                      required
                      value={newTx.category_id}
                      onChange={e => setNewTx({ ...newTx, category_id: e.target.value })}
                      className="w-full bg-ios-secondary rounded-2xl py-4 px-4 font-medium text-ios-text focus:outline-none focus:ring-2 focus:ring-ios-blue/20 appearance-none"
                    >
                      <option value="">Select Category</option>
                      {categories.filter(c => c.type === newTx.type).map(cat => (
                        <option key={cat.category_id} value={cat.category_id}>{cat.name}</option>
                      ))}
                    </select>
                  </div>

                  <div>
                    <label className="text-xs font-bold text-ios-text-muted uppercase tracking-wider mb-2 block">Note (Optional)</label>
                    <input 
                      type="text" 
                      value={newTx.note}
                      onChange={e => setNewTx({ ...newTx, note: e.target.value })}
                      placeholder="What was this for?"
                      className="w-full bg-ios-secondary rounded-2xl py-4 px-4 font-medium text-ios-text focus:outline-none focus:ring-2 focus:ring-ios-blue/20"
                    />
                  </div>
                </div>

                <button type="submit" className="w-full ios-button py-5 text-lg shadow-lg shadow-ios-blue/20">
                  Save Transaction
                </button>
              </form>
            </motion.div>
          </>
        )}
      </AnimatePresence>

      {/* Bottom Navigation */}
      <nav className="fixed bottom-0 left-0 right-0 max-w-md mx-auto bg-ios-card/80 backdrop-blur-xl border-t border-ios-border px-6 py-4 flex justify-between items-center z-30">
        <NavButton active={activeTab === 'home'} onClick={() => setActiveTab('home')} icon={<Home />} label="Home" />
        <NavButton active={activeTab === 'reports'} onClick={() => setActiveTab('reports')} icon={<PieChart />} label="Reports" />
        
        <button 
          onClick={() => setShowAddModal(true)}
          className="bg-ios-blue text-white p-4 rounded-2xl shadow-lg shadow-ios-blue/30 -mt-12 transition-transform active:scale-90"
        >
          <PlusCircle className="w-7 h-7" />
        </button>

        <NavButton active={activeTab === 'goals'} onClick={() => setActiveTab('goals')} icon={<Target />} label="Goals" />
        <NavButton active={activeTab === 'profile'} onClick={() => setActiveTab('profile')} icon={<User />} label="Profile" />
      </nav>
    </div>
  );
}

function NavButton({ active, onClick, icon, label }: { active: boolean, onClick: () => void, icon: React.ReactNode, label: string }) {
  return (
    <button 
      onClick={onClick}
      className={`flex flex-col items-center gap-1 transition-colors ${active ? 'text-ios-blue' : 'text-ios-text-muted'}`}
    >
      {React.cloneElement(icon as React.ReactElement, { className: 'w-6 h-6' } as any)}
      <span className="text-[10px] font-bold uppercase tracking-widest">{label}</span>
    </button>
  );
}
