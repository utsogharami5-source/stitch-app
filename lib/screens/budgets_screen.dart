import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import '../providers/app_state.dart';
import '../models/models.dart';

class BudgetsScreen extends StatelessWidget {
  const BudgetsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final appState = Provider.of<AppState>(context);

    final budgetItems = _computeBudgetItems(appState);

    return Scaffold(
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Header
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'OVERVIEW',
                      style: TextStyle(
                        fontSize: 10,
                        fontWeight: FontWeight.bold,
                        color: Colors.grey.shade500,
                        letterSpacing: 2.0,
                      ),
                    ),
                    const Text(
                      'Budgets',
                      style: TextStyle(
                        fontSize: 32,
                        fontWeight: FontWeight.w800,
                        letterSpacing: -1.0,
                      ),
                    ),
                  ],
                ),
                ElevatedButton.icon(
                  onPressed: () => _showAddBudgetDialog(context, appState),
                  icon: const Icon(Icons.add, size: 20),
                  label: const Text('New Budget', style: TextStyle(fontWeight: FontWeight.bold)),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: theme.primaryColor,
                    foregroundColor: Colors.white,
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                  ),
                ),
              ],
            ),
            
            const SizedBox(height: 32),
            
            // Featured Goal Card
            _buildFeaturedGoalCard(context, appState),
            
            const SizedBox(height: 32),
            
            // Budget Overview
            if (budgetItems.isNotEmpty) ...[
              Text(
                'BUDGET CATEGORIES',
                style: TextStyle(
                  fontSize: 10,
                  fontWeight: FontWeight.bold,
                  color: Colors.grey.shade500,
                  letterSpacing: 2.0,
                ),
              ),
              const SizedBox(height: 16),
              GridView.count(
                shrinkWrap: true,
                physics: const NeverScrollableScrollPhysics(),
                crossAxisCount: 2,
                childAspectRatio: 0.85,
                mainAxisSpacing: 16,
                crossAxisSpacing: 16,
                children: budgetItems.map((item) {
                  return _buildBudgetCard(
                    context,
                    appState: appState,
                    name: item['name'] as String,
                    spent: item['spent'] as double,
                    limit: item['limit'] as double,
                    icon: _getIconData(item['icon'] as String),
                    color: _safeParseColor(item['color'] as String),
                    isOver: (item['spent'] as double) > (item['limit'] as double),
                  );
                }).toList(),
              ),
            ] else
              _buildEmptyBudgetState(),
            
            const SizedBox(height: 32),
            
            // Goals Section
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text(
                  'Savings Goals',
                  style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                ),
                TextButton.icon(
                  onPressed: () => _showAddGoalDialog(context, appState),
                  icon: const Icon(Icons.add_circle_outline, size: 18),
                  label: const Text('Add', style: TextStyle(fontWeight: FontWeight.bold)),
                ),
              ],
            ),
            const SizedBox(height: 16),
            
            if (appState.goals.isEmpty)
              Center(
                child: Padding(
                  padding: const EdgeInsets.symmetric(vertical: 40),
                  child: Column(
                    children: [
                      Icon(Icons.flag_outlined, size: 48, color: Colors.grey.shade300),
                      const SizedBox(height: 12),
                      Text('No savings goals yet.', style: TextStyle(color: Colors.grey.shade500)),
                    ],
                  ),
                ),
              )
            else
              ...appState.goals.map((goal) {
                final percentage = goal.targetAmount > 0 
                    ? (goal.savedAmount / goal.targetAmount).clamp(0.0, 1.0) 
                    : 0.0;
                return _buildMilestoneRow(
                  context,
                  appState: appState,
                  name: goal.name,
                  achieved: goal.savedAmount,
                  total: goal.targetAmount,
                  percentage: percentage,
                  icon: Icons.savings_outlined,
                );
              }),
            const SizedBox(height: 40),
          ],
        ),
      ),
    );
  }

  Color _safeParseColor(String hex) {
    try {
      String cleanHex = hex.replaceAll('#', '');
      if (cleanHex.length == 6) cleanHex = 'FF$cleanHex';
      return Color(int.parse(cleanHex, radix: 16));
    } catch (_) {
      return Colors.grey;
    }
  }

  List<Map<String, dynamic>> _computeBudgetItems(AppState appState) {
    final List<Map<String, dynamic>> items = [];
    for (var cat in appState.categories) {
      if (cat.monthlyBudget != null && cat.monthlyBudget! > 0) {
        final spent = appState.transactions
            .where((tx) => tx.categoryId == cat.id && tx.type == 'expense')
            .fold(0.0, (sum, tx) => sum + tx.amount);
        items.add({
          'name': cat.name,
          'spent': spent,
          'limit': cat.monthlyBudget!,
          'icon': cat.icon,
          'color': cat.color,
        });
      }
    }
    return items;
  }

  Widget _buildFeaturedGoalCard(BuildContext context, AppState appState) {
    final theme = Theme.of(context);

    if (appState.goals.isEmpty) {
      return Container(
        padding: const EdgeInsets.all(24),
        decoration: BoxDecoration(
          gradient: LinearGradient(
            colors: [theme.primaryColor, theme.primaryColor.withAlpha(204)],
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
          ),
          borderRadius: BorderRadius.circular(32),
          boxShadow: [
            BoxShadow(color: theme.primaryColor.withAlpha(77), blurRadius: 20, offset: const Offset(0, 10)),
          ],
        ),
        child: Column(
          children: [
            const Icon(Icons.flag_outlined, color: Colors.white70, size: 40),
            const SizedBox(height: 12),
            const Text('Set Your First Goal', style: TextStyle(color: Colors.white, fontSize: 20, fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            Text('Start saving for something you love!', style: TextStyle(color: Colors.white.withAlpha(179), fontSize: 13)),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () => _showAddGoalDialog(context, appState),
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.white,
                foregroundColor: theme.primaryColor,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
              ),
              child: const Text('Create Goal', style: TextStyle(fontWeight: FontWeight.bold)),
            ),
          ],
        ),
      );
    }

    final goal = appState.goals.first;
    final percentage = goal.targetAmount > 0 ? (goal.savedAmount / goal.targetAmount).clamp(0.0, 1.0) : 0.0;

    return Container(
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [theme.primaryColor, theme.primaryColor.withAlpha(204)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(32),
        boxShadow: [
          BoxShadow(color: theme.primaryColor.withAlpha(77), blurRadius: 20, offset: const Offset(0, 10)),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                decoration: BoxDecoration(
                  color: Colors.white.withAlpha(51),
                  borderRadius: BorderRadius.circular(20),
                ),
                child: const Text('FEATURED GOAL', style: TextStyle(color: Colors.white, fontSize: 10, fontWeight: FontWeight.bold)),
              ),
              const Text('TARGET', style: TextStyle(color: Colors.white70, fontSize: 10, fontWeight: FontWeight.bold)),
            ],
          ),
          const SizedBox(height: 12),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Expanded(child: Text(goal.name, style: const TextStyle(color: Colors.white, fontSize: 28, fontWeight: FontWeight.w800), overflow: TextOverflow.ellipsis)),
              const SizedBox(width: 8),
              Text(appState.formatCurrency(goal.targetAmount), style: const TextStyle(color: Colors.white, fontSize: 22, fontWeight: FontWeight.bold)),
            ],
          ),
          const SizedBox(height: 32),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('${(percentage * 100).toStringAsFixed(0)}%', style: const TextStyle(color: Colors.white, fontSize: 24, fontWeight: FontWeight.w900)),
                  Text(
                    percentage >= 0.75 ? 'Almost there!' : percentage >= 0.5 ? 'Halfway!' : 'Keep going!',
                    style: TextStyle(color: Colors.white.withAlpha(179), fontSize: 11),
                  ),
                ],
              ),
              Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Text(appState.formatCurrency(goal.savedAmount), style: const TextStyle(color: Colors.white, fontSize: 20, fontWeight: FontWeight.bold)),
                  Text('Saved so far', style: TextStyle(color: Colors.white.withAlpha(179), fontSize: 11)),
                ],
              ),
            ],
          ),
          const SizedBox(height: 12),
          ClipRRect(
            borderRadius: BorderRadius.circular(10),
            child: LinearProgressIndicator(
              value: percentage,
              backgroundColor: Colors.white.withAlpha(51),
              valueColor: const AlwaysStoppedAnimation<Color>(Colors.white),
              minHeight: 12,
            ),
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              const Icon(Icons.event, color: Colors.white60, size: 14),
              const SizedBox(width: 4),
              Text('Deadline: ${DateFormat('MMM d, yyyy').format(goal.deadline)}', style: TextStyle(color: Colors.white.withAlpha(153), fontSize: 10, fontStyle: FontStyle.italic)),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildEmptyBudgetState() {
    return Center(
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 40),
        child: Column(
          children: [
            Icon(Icons.pie_chart_outline, size: 48, color: Colors.grey.shade300),
            const SizedBox(height: 12),
            Text('No budget categories configured.', style: TextStyle(color: Colors.grey.shade500)),
            const SizedBox(height: 4),
            Text('Add monthly budgets to your categories.', style: TextStyle(color: Colors.grey.shade400, fontSize: 12)),
          ],
        ),
      ),
    );
  }

  void _showAddBudgetDialog(BuildContext context, AppState appState) {
    final nameCtrl = TextEditingController();
    final budgetCtrl = TextEditingController();

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
        title: const Text('New Budget Category', style: TextStyle(fontWeight: FontWeight.bold)),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: nameCtrl,
              decoration: InputDecoration(
                hintText: 'Category name',
                filled: true,
                fillColor: Colors.grey.withAlpha(20),
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide.none),
              ),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: budgetCtrl,
              keyboardType: const TextInputType.numberWithOptions(decimal: true),
              decoration: InputDecoration(
                hintText: 'Monthly budget limit (${appState.currencySymbol})',
                filled: true,
                fillColor: Colors.grey.withAlpha(20),
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide.none),
              ),
            ),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
          ElevatedButton(
            onPressed: () {
              final name = nameCtrl.text.trim();
              final budget = double.tryParse(budgetCtrl.text.trim()) ?? 0;
              if (name.isEmpty || budget <= 0) return;
              
              appState.addCategory(Category(
                id: DateTime.now().millisecondsSinceEpoch.toString(),
                name: name,
                color: 'FF6366F1',
                icon: 'category',
                monthlyBudget: budget,
              ));
              Navigator.pop(ctx);
            },
            child: const Text('Create'),
          ),
        ],
      ),
    );
  }

  void _showAddGoalDialog(BuildContext context, AppState appState) {
    final nameCtrl = TextEditingController();
    final targetCtrl = TextEditingController();
    final savedCtrl = TextEditingController(text: '0');
    DateTime deadline = DateTime.now().add(const Duration(days: 90));

    showDialog(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setDialogState) => AlertDialog(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
          title: const Text('New Savings Goal', style: TextStyle(fontWeight: FontWeight.bold)),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: nameCtrl,
                decoration: InputDecoration(
                  hintText: 'Goal name (e.g. Japan Trip)',
                  filled: true,
                  fillColor: Colors.grey.withAlpha(20),
                  border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide.none),
                ),
              ),
              const SizedBox(height: 12),
              TextField(
                controller: targetCtrl,
                keyboardType: const TextInputType.numberWithOptions(decimal: true),
                decoration: InputDecoration(
                  hintText: 'Target amount (${appState.currencySymbol})',
                  filled: true,
                  fillColor: Colors.grey.withAlpha(20),
                  border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide.none),
                ),
              ),
              const SizedBox(height: 12),
              TextField(
                controller: savedCtrl,
                keyboardType: const TextInputType.numberWithOptions(decimal: true),
                decoration: InputDecoration(
                  hintText: 'Already saved (${appState.currencySymbol})',
                  filled: true,
                  fillColor: Colors.grey.withAlpha(20),
                  border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide.none),
                ),
              ),
              const SizedBox(height: 12),
              InkWell(
                onTap: () async {
                  final picked = await showDatePicker(
                    context: ctx,
                    initialDate: deadline,
                    firstDate: DateTime.now(),
                    lastDate: DateTime(2030),
                  );
                  if (picked != null) {
                    setDialogState(() => deadline = picked);
                  }
                },
                child: Container(
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: Colors.grey.withAlpha(20),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Row(
                    children: [
                      const Icon(Icons.event, size: 20, color: Colors.grey),
                      const SizedBox(width: 12),
                      Text('Deadline: ${DateFormat('MMM d, yyyy').format(deadline)}', style: const TextStyle(fontSize: 14)),
                    ],
                  ),
                ),
              ),
            ],
          ),
          actions: [
            TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
            ElevatedButton(
              onPressed: () {
                final name = nameCtrl.text.trim();
                final target = double.tryParse(targetCtrl.text.trim()) ?? 0;
                final saved = double.tryParse(savedCtrl.text.trim()) ?? 0;
                if (name.isEmpty || target <= 0) return;
                
                appState.addGoal(FinancialGoal(
                  id: DateTime.now().millisecondsSinceEpoch.toString(),
                  name: name,
                  targetAmount: target,
                  savedAmount: saved,
                  deadline: deadline,
                ));
                Navigator.pop(ctx);
              },
              child: const Text('Create'),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildBudgetCard(BuildContext context, {required AppState appState, required String name, required double spent, required double limit, required IconData icon, required Color color, bool isOver = false}) {
    final theme = Theme.of(context);
    final percentage = (spent / limit).clamp(0.0, 1.0);

    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: theme.colorScheme.surface,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: Colors.grey.withAlpha(13)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: color.withAlpha(25),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Icon(icon, color: color, size: 24),
              ),
              if (isOver)
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                  decoration: BoxDecoration(
                    color: Colors.red.withAlpha(20),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: const Text('!', style: TextStyle(color: Colors.red, fontWeight: FontWeight.bold, fontSize: 12)),
                ),
            ],
          ),
          const SizedBox(height: 16),
          Text(name, style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: isOver ? Colors.red : null)),
          const SizedBox(height: 4),
          Row(
            crossAxisAlignment: CrossAxisAlignment.baseline,
            textBaseline: TextBaseline.alphabetic,
            children: [
              Flexible(child: Text(appState.formatCurrency(spent), style: TextStyle(fontWeight: FontWeight.w900, fontSize: 20, color: isOver ? Colors.red : null))),
              const SizedBox(width: 4),
              Text('of ${appState.formatCurrency(limit)}', style: TextStyle(color: Colors.grey.shade500, fontSize: 10, fontWeight: FontWeight.bold)),
            ],
          ),
          const Spacer(),
          Column(
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(isOver ? 'OVER BUDGET' : 'USAGE', style: TextStyle(color: isOver ? Colors.red : color, fontWeight: FontWeight.bold, fontSize: 10)),
                  Text('${(spent / limit * 100).toStringAsFixed(0)}%', style: TextStyle(color: isOver ? Colors.red : color, fontWeight: FontWeight.bold, fontSize: 10)),
                ],
              ),
              const SizedBox(height: 4),
              ClipRRect(
                borderRadius: BorderRadius.circular(5),
                child: LinearProgressIndicator(
                  value: percentage,
                  minHeight: 6,
                  backgroundColor: Colors.grey.withAlpha(25),
                  valueColor: AlwaysStoppedAnimation<Color>(isOver ? Colors.red : color),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildMilestoneRow(BuildContext context, {required AppState appState, required String name, required double achieved, required double total, required double percentage, required IconData icon}) {
    final theme = Theme.of(context);

    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: theme.colorScheme.surface,
        borderRadius: BorderRadius.circular(20),
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: theme.primaryColor.withAlpha(25),
              borderRadius: BorderRadius.circular(16),
            ),
            child: Icon(icon, color: theme.primaryColor, size: 24),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(name, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                Text('${(percentage * 100).toStringAsFixed(0)}% achieved', style: TextStyle(color: Colors.grey.shade500, fontSize: 11)),
              ],
            ),
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Text(appState.formatCurrency(achieved), style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
              Text('of ${appState.formatCurrency(total)}', style: TextStyle(color: Colors.grey.shade500, fontSize: 10)),
            ],
          ),
        ],
      ),
    );
  }

  IconData _getIconData(String iconName) {
    switch (iconName) {
      case 'local_cafe': return Icons.local_cafe;
      case 'home_work': return Icons.home_work;
      case 'directions_car': return Icons.directions_car;
      case 'work': return Icons.work;
      case 'shopping_cart': return Icons.shopping_cart;
      case 'restaurant': return Icons.restaurant;
      case 'movie': return Icons.movie;
      default: return Icons.category;
    }
  }
}
