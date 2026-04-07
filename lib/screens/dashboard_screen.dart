import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import '../providers/app_state.dart';
import '../models/models.dart';
import '../widgets/add_transaction_modal.dart';

class DashboardScreen extends StatelessWidget {
  const DashboardScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final appState = Provider.of<AppState>(context);
    final theme = Theme.of(context);
    final user = appState.currentUser;

    return Scaffold(
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Top Bar
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    CircleAvatar(
                      backgroundImage: user?.photoURL != null
                          ? NetworkImage(user!.photoURL!)
                          : null,
                      backgroundColor: theme.primaryColor.withAlpha(40),
                      radius: 20,
                      child: user?.photoURL == null
                          ? Icon(Icons.person, color: theme.primaryColor, size: 22)
                          : null,
                    ),
                    const SizedBox(width: 12),
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          _getGreeting(),
                          style: TextStyle(fontSize: 12, color: Colors.grey.shade500, fontWeight: FontWeight.w600),
                        ),
                        Text(
                          user?.displayName ?? 'Guest',
                          style: TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.bold,
                            color: theme.colorScheme.onSurface,
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
                Row(
                  children: [
                    if (appState.isAuthenticated)
                      IconButton(
                        icon: const Icon(Icons.sync, size: 22),
                        onPressed: () {
                          appState.syncNow();
                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(content: Text('Syncing...'), duration: Duration(seconds: 1)),
                          );
                        },
                      ),
                    Stack(
                      children: [
                        IconButton(
                          icon: const Icon(Icons.notifications_outlined),
                          onPressed: () {
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(content: Text('No new notifications'), duration: Duration(seconds: 1)),
                            );
                          },
                        ),
                        Positioned(
                          right: 8,
                          top: 8,
                          child: Container(
                            width: 8,
                            height: 8,
                            decoration: BoxDecoration(
                              color: Colors.red,
                              shape: BoxShape.circle,
                              border: Border.all(color: theme.scaffoldBackgroundColor, width: 1.5),
                            ),
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ],
            ),
            
            const SizedBox(height: 32),
            
            // Total Balance & Month Selector
            Column(
              children: [
                GestureDetector(
                  onTap: () => _selectMonth(context, appState),
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                    decoration: BoxDecoration(
                      color: theme.primaryColor.withAlpha(20),
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(Icons.calendar_month, size: 16, color: theme.primaryColor),
                        const SizedBox(width: 8),
                        Text(
                          DateFormat('MMMM yyyy').format(appState.selectedMonth).toUpperCase(),
                          style: TextStyle(
                            fontSize: 12,
                            fontWeight: FontWeight.w800,
                            letterSpacing: 1.2,
                            color: theme.primaryColor,
                          ),
                        ),
                        const Icon(Icons.arrow_drop_down, size: 18),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 24),
                Text(
                  appState.transactionFilter == 'all' 
                    ? 'TOTAL BALANCE' 
                    : '${appState.transactionFilter.toUpperCase()} TOTAL',
                  style: TextStyle(
                    fontSize: 11,
                    fontWeight: FontWeight.w700,
                    letterSpacing: 2.0,
                    color: Colors.grey.shade600,
                  ),
                ),
                const SizedBox(height: 8),
                GestureDetector(
                  onTap: () => appState.setTransactionFilter('all'),
                  child: Text(
                    appState.formatCurrency(
                      appState.transactionFilter == 'all' ? appState.totalBalance :
                      appState.transactionFilter == 'income' ? appState.totalIncome : appState.totalExpenses
                    ),
                    style: const TextStyle(
                      fontSize: 44,
                      fontWeight: FontWeight.w800,
                      letterSpacing: -1.5,
                    ),
                  ),
                ),
                if (appState.transactionFilter != 'all')
                  Padding(
                    padding: const EdgeInsets.only(top: 8),
                    child: TextButton.icon(
                      onPressed: () => appState.setTransactionFilter('all'),
                      icon: const Icon(Icons.clear, size: 14),
                      label: const Text('Reset Filter', style: TextStyle(fontSize: 12)),
                      style: TextButton.styleFrom(
                        visualDensity: VisualDensity.compact,
                        foregroundColor: Colors.grey,
                      ),
                    ),
                  ),
              ],
            ),
            
            const SizedBox(height: 32),
            
            // Income / Expense Cards
            Row(
              children: [
                Expanded(
                  child: _buildSummaryCard(
                    context,
                    title: 'INCOME',
                    amount: appState.formatCurrency(appState.totalIncome),
                    icon: Icons.south_west,
                    iconColor: Colors.green,
                    iconBg: Colors.green.withAlpha(25),
                    isActive: appState.transactionFilter == 'income',
                    onTap: () => appState.setTransactionFilter('income'),
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: _buildSummaryCard(
                    context,
                    title: 'EXPENSES',
                    amount: appState.formatCurrency(appState.totalExpenses),
                    icon: Icons.north_east,
                    iconColor: Colors.red,
                    iconBg: Colors.red.withAlpha(25),
                    isActive: appState.transactionFilter == 'expense',
                    onTap: () => appState.setTransactionFilter('expense'),
                  ),
                ),
              ],
            ),
            
            const SizedBox(height: 32),
            
            // Recent Activity Header
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text(
                  'Recent Activity',
                  style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                ),
                TextButton(
                  onPressed: () {
                    _showAllTransactions(context, appState);
                  },
                  child: Text(
                    'See All',
                    style: TextStyle(
                      color: theme.primaryColor,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ],
            ),
            
            const SizedBox(height: 8),
            
            // Transactions List
            if (appState.isLoading)
              const Center(child: Padding(padding: EdgeInsets.all(40), child: CircularProgressIndicator()))
            else if (appState.transactions.isEmpty)
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 60),
                child: Center(
                  child: Column(
                    children: [
                      Icon(Icons.receipt_long_outlined, size: 64, color: Colors.grey.shade300),
                      const SizedBox(height: 16),
                      Text("No transactions yet.", style: TextStyle(color: Colors.grey.shade500, fontWeight: FontWeight.w500)),
                      const SizedBox(height: 8),
                      Text("Tap + to add your first one!", style: TextStyle(color: Colors.grey.shade400, fontSize: 12)),
                    ],
                  ),
                ),
              )
            else
              ListView.builder(
                shrinkWrap: true,
                physics: const NeverScrollableScrollPhysics(),
                itemCount: appState.transactions.length > 5 ? 5 : appState.transactions.length,
                itemBuilder: (context, index) {
                  final tx = appState.transactions[index];
                  return _buildTransactionRow(context, tx, appState);
                },
              ),
            const SizedBox(height: 40),
          ],
        ),
      ),
    );
  }

  String _getGreeting() {
    final hour = DateTime.now().hour;
    if (hour >= 5 && hour < 12) return 'Good Morning 👋';
    if (hour >= 12 && hour < 17) return 'Good Afternoon ☀️';
    if (hour >= 17 && hour < 21) return 'Good Evening 🌙';
    return 'Good Night 🌜';
  }

  Future<void> _selectMonth(BuildContext context, AppState appState) async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: appState.selectedMonth,
      firstDate: DateTime(2020),
      lastDate: DateTime(2101),
      initialDatePickerMode: DatePickerMode.year,
      helpText: 'SELECT MONTH',
    );
    if (picked != null) {
      appState.setSelectedMonth(picked);
    }
  }

  void _showAllTransactions(BuildContext context, AppState appState) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      useSafeArea: true,
      builder: (context) => Container(
        height: MediaQuery.of(context).size.height * 0.85,
        decoration: BoxDecoration(
          color: Theme.of(context).scaffoldBackgroundColor,
          borderRadius: const BorderRadius.vertical(top: Radius.circular(32)),
        ),
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Center(
              child: Container(width: 40, height: 4, decoration: BoxDecoration(color: Colors.grey.shade300, borderRadius: BorderRadius.circular(2))),
            ),
            const SizedBox(height: 24),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text('Transactions', style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold)),
                    Text(
                      '${DateFormat('MMMM yyyy').format(appState.selectedMonth)} - ${appState.transactions.length} items', 
                      style: TextStyle(color: Colors.grey.shade500),
                    ),
                  ],
                ),
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: Theme.of(context).primaryColor.withAlpha(20),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Text(
                    appState.transactionFilter.toUpperCase(),
                    style: TextStyle(
                      fontSize: 10,
                      fontWeight: FontWeight.bold,
                      color: Theme.of(context).primaryColor,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Expanded(
              child: ListView.builder(
                itemCount: appState.transactions.length,
                itemBuilder: (context, index) {
                  final tx = appState.transactions[index];
                  return Dismissible(
                    key: Key(tx.id),
                    direction: DismissDirection.endToStart,
                    background: Container(
                      alignment: Alignment.centerRight,
                      padding: const EdgeInsets.only(right: 20),
                      margin: const EdgeInsets.symmetric(vertical: 8),
                      decoration: BoxDecoration(
                        color: Colors.red,
                        borderRadius: BorderRadius.circular(20),
                      ),
                      child: const Icon(Icons.delete, color: Colors.white),
                    ),
                    onDismissed: (direction) {
                      appState.deleteTransaction(tx.id);
                    },
                    child: _buildTransactionRow(context, tx, appState),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSummaryCard(BuildContext context, {
    required String title, 
    required String amount, 
    required IconData icon, 
    required Color iconColor, 
    required Color iconBg,
    bool isActive = false,
    VoidCallback? onTap,
  }) {
    final theme = Theme.of(context);
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          color: theme.colorScheme.surface,
          borderRadius: BorderRadius.circular(24),
          border: isActive ? Border.all(color: iconColor.withAlpha(100), width: 2) : Border.all(color: Colors.transparent, width: 2),
          boxShadow: [
            BoxShadow(
              color: isActive ? iconColor.withAlpha(20) : Colors.black.withAlpha(10),
              blurRadius: 20,
              offset: const Offset(0, 8),
            ),
          ],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Container(
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(
                color: iconBg,
                borderRadius: BorderRadius.circular(16),
              ),
              child: Icon(icon, color: iconColor, size: 24),
            ),
            const SizedBox(height: 24),
            Text(
              title,
              style: TextStyle(
                fontSize: 10,
                fontWeight: FontWeight.bold,
                letterSpacing: 1.5,
                color: Colors.grey.shade500,
              ),
            ),
            const SizedBox(height: 4),
            FittedBox(
              fit: BoxFit.scaleDown,
              alignment: Alignment.centerLeft,
              child: Text(
                amount,
                style: const TextStyle(
                  fontSize: 22,
                  fontWeight: FontWeight.bold,
                  letterSpacing: -0.5,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTransactionRow(BuildContext context, Transaction tx, AppState appState) {
    final isIncome = tx.type == 'income';
    final dateStr = DateFormat('MMM d').format(tx.date);
    final theme = Theme.of(context);

    return Dismissible(
      key: Key(tx.id),
      direction: DismissDirection.endToStart,
      background: Container(
        alignment: Alignment.centerRight,
        padding: const EdgeInsets.only(right: 20.0),
        decoration: BoxDecoration(
          color: Colors.red,
          borderRadius: BorderRadius.circular(20),
        ),
        child: const Icon(Icons.delete, color: Colors.white),
      ),
      confirmDismiss: (direction) async {
        return await showDialog(
          context: context,
          builder: (ctx) => AlertDialog(
            title: const Text('Delete Transaction'),
            content: Text('Delete "${tx.title}" for ${appState.formatCurrency(tx.amount)}?'),
            actions: [
              TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Cancel')),
              TextButton(
                onPressed: () => Navigator.pop(ctx, true), 
                child: const Text('Delete', style: TextStyle(color: Colors.red)),
              ),
            ],
          ),
        );
      },
      onDismissed: (direction) {
        Provider.of<AppState>(context, listen: false).deleteTransaction(tx.id);
      },
      child: InkWell(
        onTap: () {
          showModalBottomSheet(
            context: context,
            isScrollControlled: true,
            backgroundColor: Colors.transparent,
            builder: (context) => AddTransactionModal(initialTransaction: tx),
          );
        },
        borderRadius: BorderRadius.circular(20),
        child: Container(
          margin: const EdgeInsets.only(bottom: 12),
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: theme.colorScheme.surface,
            borderRadius: BorderRadius.circular(20),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withAlpha(8),
                blurRadius: 10,
                offset: const Offset(0, 4),
              ),
            ],
          ),
          child: Row(
            children: [
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: (isIncome ? Colors.green : Colors.red).withAlpha(20),
                  borderRadius: BorderRadius.circular(16),
                ),
                child: Icon(
                  isIncome ? Icons.attach_money : Icons.shopping_bag_outlined, 
                  color: isIncome ? Colors.green : Colors.red.shade400,
                  size: 20,
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(tx.title, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                    Text(tx.type.toUpperCase(), style: TextStyle(color: Colors.grey.shade500, fontSize: 11, fontWeight: FontWeight.bold, letterSpacing: 1.0)),
                  ],
                ),
              ),
              Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Text(
                    isIncome ? "+${appState.formatCurrency(tx.amount)}" : "-${appState.formatCurrency(tx.amount)}",
                    style: TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 15,
                      color: isIncome ? Colors.green : Colors.red,
                    ),
                  ),
                  Text(
                    dateStr,
                    style: TextStyle(
                      color: Colors.grey.shade500,
                      fontSize: 12,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
