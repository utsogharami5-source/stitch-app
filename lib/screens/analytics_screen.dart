import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:fl_chart/fl_chart.dart';
import '../providers/app_state.dart';
import '../models/models.dart';

class AnalyticsScreen extends StatefulWidget {
  const AnalyticsScreen({super.key});

  @override
  State<AnalyticsScreen> createState() => _AnalyticsScreenState();
}

class _AnalyticsScreenState extends State<AnalyticsScreen> {
  String _selectedTimeframe = 'Month';

  DateTime _getStartDate(AppState appState) {
    final baseDate = appState.selectedMonth;
    switch (_selectedTimeframe) {
      case 'Week':
        return baseDate.subtract(const Duration(days: 7));
      case 'Year':
        return DateTime(baseDate.year, 1, 1);
      case 'Month':
      default:
        // For 'Month', we return the start of that month
        return DateTime(baseDate.year, baseDate.month, 1);
    }
  }

  List<Transaction> _getFilteredTransactions(AppState appState) {
    final startDate = _getStartDate(appState);
    final endDate = _selectedTimeframe == 'Month' 
        ? DateTime(appState.selectedMonth.year, appState.selectedMonth.month + 1, 1)
        : DateTime(appState.selectedMonth.year + 1, 1, 1); // rough end for Year/Week

    // If timeframe is Month, we strictly show that month's data
    if (_selectedTimeframe == 'Month') {
      return appState.allTransactions.where((tx) => 
        tx.date.year == appState.selectedMonth.year && 
        tx.date.month == appState.selectedMonth.month
      ).toList();
    }

    return appState.allTransactions.where((tx) => 
      tx.date.isAfter(startDate.subtract(const Duration(seconds: 1))) && 
      tx.date.isBefore(endDate)
    ).toList();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final appState = Provider.of<AppState>(context);
    
    final filteredTx = _getFilteredTransactions(appState);
    final filteredIncome = filteredTx.where((tx) => tx.type == 'income').fold(0.0, (sum, tx) => sum + tx.amount);
    final filteredExpenses = filteredTx.where((tx) => tx.type == 'expense').fold(0.0, (sum, tx) => sum + tx.amount);
    final netCashflow = filteredIncome - filteredExpenses;

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
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      '${_selectedTimeframe.toUpperCase()} INSIGHTS',
                      style: TextStyle(
                        fontSize: 10,
                        fontWeight: FontWeight.bold,
                        color: Colors.grey.shade500,
                        letterSpacing: 2.0,
                      ),
                    ),
                    const Text(
                      'Analytics',
                      style: TextStyle(
                        fontSize: 32,
                        fontWeight: FontWeight.w800,
                        letterSpacing: -1.0,
                      ),
                    ),
                  ],
                ),
                TextButton.icon(
                  onPressed: () => _exportData(context, appState),
                  icon: const Icon(Icons.file_download_outlined, size: 20),
                  label: const Text('Export', style: TextStyle(fontWeight: FontWeight.bold)),
                  style: TextButton.styleFrom(
                    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                    backgroundColor: theme.colorScheme.surface,
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                  ),
                ),
              ],
            ),
            
            const SizedBox(height: 20),

            // Timeframe Filter
            Container(
              padding: const EdgeInsets.all(4),
              decoration: BoxDecoration(
                color: Colors.grey.withAlpha(25),
                borderRadius: BorderRadius.circular(20),
              ),
              child: Row(
                children: ['Week', 'Month', 'Year'].map((timeframe) {
                  final isSelected = _selectedTimeframe == timeframe;
                  return Expanded(
                    child: GestureDetector(
                      onTap: () => setState(() => _selectedTimeframe = timeframe),
                      child: Container(
                        padding: const EdgeInsets.symmetric(vertical: 12),
                        decoration: BoxDecoration(
                          color: isSelected ? theme.colorScheme.surface : Colors.transparent,
                          borderRadius: BorderRadius.circular(16),
                          boxShadow: isSelected ? [
                            BoxShadow(color: Colors.black.withAlpha(10), blurRadius: 8, offset: const Offset(0, 2)),
                          ] : [],
                        ),
                        alignment: Alignment.center,
                        child: Text(
                          timeframe,
                          style: TextStyle(
                            color: isSelected ? theme.primaryColor : Colors.grey.shade500,
                            fontWeight: FontWeight.bold,
                            fontSize: 14,
                          ),
                        ),
                      ),
                    ),
                  );
                }).toList(),
              ),
            ),

            const SizedBox(height: 24),
            
            // Net Cashflow Card
            Container(
              padding: const EdgeInsets.all(24),
              decoration: BoxDecoration(
                color: theme.colorScheme.surface,
                borderRadius: BorderRadius.circular(24),
                boxShadow: [
                  BoxShadow(color: Colors.black.withAlpha(10), blurRadius: 20, offset: const Offset(0, 8)),
                ],
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        'NET CASHFLOW',
                        style: TextStyle(
                          fontSize: 10,
                          fontWeight: FontWeight.bold,
                          color: Colors.grey.shade500,
                          letterSpacing: 1.5,
                        ),
                      ),
                      Icon(
                        netCashflow >= 0 ? Icons.trending_up : Icons.trending_down,
                        color: netCashflow >= 0 ? Colors.green : Colors.red,
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  FittedBox(
                    fit: BoxFit.scaleDown,
                    alignment: Alignment.centerLeft,
                    child: Text(
                      appState.formatCurrency(netCashflow),
                      style: TextStyle(
                        fontSize: 32,
                        fontWeight: FontWeight.w800,
                        color: netCashflow >= 0 ? null : Colors.red,
                      ),
                    ),
                  ),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      _buildMiniStat(Icons.south_west, Colors.green, '+${appState.formatCurrency(filteredIncome)}'),
                      const SizedBox(width: 20),
                      _buildMiniStat(Icons.north_east, Colors.red, '-${appState.formatCurrency(filteredExpenses)}'),
                    ],
                  ),
                ],
              ),
            ),
            
            const SizedBox(height: 32),
            
            // Spending Chart
            const Text(
              'Spending Distribution',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),
            Container(
              height: 280,
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: theme.colorScheme.surface,
                borderRadius: BorderRadius.circular(24),
              ),
              child: filteredTx.where((tx) => tx.type == 'expense').isEmpty
                  ? Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(Icons.pie_chart_outline, size: 48, color: Colors.grey.shade300),
                          const SizedBox(height: 12),
                          Text('No expense data', style: TextStyle(color: Colors.grey.shade500)),
                        ],
                      ),
                    )
                  : Row(
                      children: [
                        Expanded(
                          flex: 3,
                          child: PieChart(
                            PieChartData(
                              sections: _getPieChartSections(appState, filteredTx),
                              centerSpaceRadius: 35,
                              sectionsSpace: 3,
                              borderData: FlBorderData(show: false),
                            ),
                          ),
                        ),
                        const SizedBox(width: 16),
                        Expanded(
                          flex: 2,
                          child: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: _getPieLegend(appState, filteredTx),
                          ),
                        ),
                      ],
                    ),
            ),
            
            const SizedBox(height: 32),
            
            // Top Categories List
            const Text(
              'Top Categories',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),
            if (filteredTx.where((tx) => tx.type == 'expense').isEmpty)
              Center(
                child: Padding(
                  padding: const EdgeInsets.all(40),
                  child: Column(
                    children: [
                      Icon(Icons.analytics_outlined, size: 48, color: Colors.grey.shade300),
                      const SizedBox(height: 12),
                      Text('No data for this period.', style: TextStyle(color: Colors.grey.shade500)),
                    ],
                  ),
                ),
              )
            else
              ..._buildCategoryList(appState, filteredTx, filteredExpenses),
            const SizedBox(height: 40),
          ],
        ),
      ),
    );
  }

  void _exportData(BuildContext context, AppState appState) {
    if (appState.transactions.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('No transactions to export!')),
      );
      return;
    }
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Go to Settings → Data Export to download CSV'),
        duration: Duration(seconds: 2),
      ),
    );
  }

  Widget _buildMiniStat(IconData icon, Color color, String text) {
    return Flexible(
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, color: color, size: 14),
          const SizedBox(width: 4),
          Flexible(child: Text(text, style: TextStyle(color: color, fontSize: 12, fontWeight: FontWeight.bold), overflow: TextOverflow.ellipsis)),
        ],
      ),
    );
  }

  Color _safeParseColor(String hex) {
    try {
      // Ensure 8-char hex with alpha
      String cleanHex = hex.replaceAll('#', '');
      if (cleanHex.length == 6) cleanHex = 'FF$cleanHex';
      return Color(int.parse(cleanHex, radix: 16));
    } catch (_) {
      return Colors.grey;
    }
  }

  List<PieChartSectionData> _getPieChartSections(AppState appState, List<Transaction> filteredTx) {
    final totalExpenses = filteredTx.where((tx) => tx.type == 'expense').fold(0.0, (sum, tx) => sum + tx.amount);
    
    if (totalExpenses == 0) return [];

    final Map<String, double> catTotals = {};
    for (var tx in filteredTx) {
      if (tx.type == 'expense') {
        catTotals[tx.categoryId] = (catTotals[tx.categoryId] ?? 0) + tx.amount;
      }
    }

    final colors = [
      const Color(0xFF6366F1), // Indigo
      const Color(0xFF10B981), // Green
      const Color(0xFFF59E0B), // Amber
      const Color(0xFFEF4444), // Red
      const Color(0xFF8B5CF6), // Purple
      const Color(0xFF06B6D4), // Cyan
      const Color(0xFFEC4899), // Pink
      const Color(0xFFF97316), // Orange
    ];

    int colorIndex = 0;
    return catTotals.entries.map((entry) {
      final cat = appState.categories.firstWhere(
        (c) => c.id == entry.key,
        orElse: () => Category(id: 'unknown', name: 'Other', color: 'FF9E9E9E', icon: 'category'),
      );
      final value = entry.value;
      final percentage = (value / totalExpenses * 100);
      
      // Use category color or fallback to palette
      Color sectionColor;
      try {
        sectionColor = _safeParseColor(cat.color);
        // If the parsed color is too dark or transparent, use palette
        if ((sectionColor.a * 255).round() < 100) {
          sectionColor = colors[colorIndex % colors.length];
        }
      } catch (_) {
        sectionColor = colors[colorIndex % colors.length];
      }
      colorIndex++;
      
      return PieChartSectionData(
        color: sectionColor,
        value: value,
        title: '${percentage.toStringAsFixed(0)}%',
        radius: 55,
        titleStyle: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: Colors.white),
        titlePositionPercentageOffset: 0.55,
      );
    }).toList();
  }

  List<Widget> _getPieLegend(AppState appState, List<Transaction> filteredTx) {
    final Map<String, double> catTotals = {};
    for (var tx in filteredTx) {
      if (tx.type == 'expense') {
        catTotals[tx.categoryId] = (catTotals[tx.categoryId] ?? 0) + tx.amount;
      }
    }

    final colors = [
      const Color(0xFF6366F1),
      const Color(0xFF10B981),
      const Color(0xFFF59E0B),
      const Color(0xFFEF4444),
      const Color(0xFF8B5CF6),
      const Color(0xFF06B6D4),
      const Color(0xFFEC4899),
      const Color(0xFFF97316),
    ];

    int colorIndex = 0;
    return catTotals.entries.map((entry) {
      final cat = appState.categories.firstWhere(
        (c) => c.id == entry.key,
        orElse: () => Category(id: 'unknown', name: 'Other', color: 'FF9E9E9E', icon: 'category'),
      );

      Color dotColor;
      try {
        dotColor = _safeParseColor(cat.color);
        if ((dotColor.a * 255).round() < 100) dotColor = colors[colorIndex % colors.length];
      } catch (_) {
        dotColor = colors[colorIndex % colors.length];
      }
      colorIndex++;

      return Padding(
        padding: const EdgeInsets.only(bottom: 8),
        child: Row(
          children: [
            Container(width: 10, height: 10, decoration: BoxDecoration(color: dotColor, shape: BoxShape.circle)),
            const SizedBox(width: 8),
            Flexible(child: Text(cat.name, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w600), overflow: TextOverflow.ellipsis)),
          ],
        ),
      );
    }).toList();
  }

  List<Widget> _buildCategoryList(AppState appState, List<Transaction> filteredTx, double filteredExpenses) {
    final Map<String, double> catTotals = {};
    for (var tx in filteredTx) {
      if (tx.type == 'expense') {
        catTotals[tx.categoryId] = (catTotals[tx.categoryId] ?? 0) + tx.amount;
      }
    }

    final sorted = catTotals.entries.toList()
      ..sort((a, b) => b.value.compareTo(a.value));

    return sorted.map((entry) {
      final cat = appState.categories.firstWhere(
        (c) => c.id == entry.key,
        orElse: () => Category(id: 'unknown', name: 'Other', color: 'FF9E9E9E', icon: 'category'),
      );
      final totalForCat = entry.value;
      final percentage = filteredExpenses > 0 ? (totalForCat / filteredExpenses) : 0.0;
      final color = _safeParseColor(cat.color);

      return _buildCategoryStatRow(
        name: cat.name,
        amount: appState.formatCurrency(totalForCat),
        percentage: percentage,
        color: color,
        icon: _getIconData(cat.icon),
      );
    }).toList();
  }

  Widget _buildCategoryStatRow({required String name, required String amount, required double percentage, required Color color, required IconData icon}) {
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surface,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: Colors.grey.withAlpha(12)),
      ),
      child: Column(
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: color.withAlpha(25),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Icon(icon, color: color, size: 20),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(name, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                    Text('${(percentage * 100).toStringAsFixed(0)}% of total spending', style: const TextStyle(color: Colors.grey, fontSize: 11)),
                  ],
                ),
              ),
              Text(amount, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
            ],
          ),
          const SizedBox(height: 16),
          ClipRRect(
            borderRadius: BorderRadius.circular(10),
            child: LinearProgressIndicator(
              value: percentage,
              backgroundColor: Colors.grey.withAlpha(25),
              valueColor: AlwaysStoppedAnimation<Color>(color),
              minHeight: 6,
            ),
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
