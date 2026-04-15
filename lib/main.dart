import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'providers/app_state.dart';
import 'screens/dashboard_screen.dart';
import 'screens/analytics_screen.dart';
import 'screens/budgets_screen.dart';
import 'screens/settings_screen.dart';
import 'screens/login_screen.dart';
import 'widgets/add_transaction_modal.dart';
import 'services/notification_service.dart';

/// FCM background handler — must be a top-level function
@pragma('vm:entry-point')
Future<void> _firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  await Firebase.initializeApp();
  debugPrint('Background message: ${message.messageId}');
}

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp();
  
  // Set up FCM background handler
  FirebaseMessaging.onBackgroundMessage(_firebaseMessagingBackgroundHandler);
  
  // Initialize notifications
  await NotificationService.instance.initialize();
  
  runApp(
    ChangeNotifierProvider(
      create: (context) => AppState(),
      child: const CoinFlowApplication(),
    ),
  );
}

class CoinFlowApplication extends StatelessWidget {
  const CoinFlowApplication({super.key});

  @override
  Widget build(BuildContext context) {
    final appState = Provider.of<AppState>(context);
    return MaterialApp(
      title: 'CoinFlow',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        brightness: Brightness.light,
        scaffoldBackgroundColor: const Color(0xFFF8FAFC),
        primaryColor: const Color(0xFF4F46E5),
        colorScheme: const ColorScheme.light(
          primary: Color(0xFF4F46E5),
          secondary: Color(0xFF10B981),
          surface: Colors.white,
          error: Color(0xFFEF4444),
          onSurface: Color(0xFF0F172A),
        ),
        fontFamily: 'Manrope',
        useMaterial3: true,
      ),
      darkTheme: ThemeData(
        brightness: Brightness.dark,
        scaffoldBackgroundColor: const Color(0xFF121212),
        primaryColor: const Color(0xFF6366F1),
        colorScheme: const ColorScheme.dark(
          primary: Color(0xFF6366F1),
          secondary: Color(0xFF10B981),
          surface: Color(0xFF1E1E1E),
          onSurface: Colors.white,
        ),
        useMaterial3: true,
      ),
      themeMode: appState.themeMode,
      home: const AuthWrapper(),
    );
  }
}

class AuthWrapper extends StatelessWidget {
  const AuthWrapper({super.key});

  @override
  Widget build(BuildContext context) {
    final appState = Provider.of<AppState>(context);
    
    // Wait until FirebaseAuth has determined the initial state
    if (!appState.isAuthReady) {
      return const Scaffold(body: SizedBox.shrink());
    }

    if (appState.isAuthenticated) {
      return const MainLayoutScreen();
    } else {
      return const LoginScreen();
    }
  }
}

class MainLayoutScreen extends StatefulWidget {
  const MainLayoutScreen({super.key});

  @override
  State<MainLayoutScreen> createState() => _MainLayoutScreenState();
}

class _MainLayoutScreenState extends State<MainLayoutScreen> {
  int _currentIndex = 0;

  final List<Widget> _pages = [
    const DashboardScreen(),
    const AnalyticsScreen(),
    const BudgetsScreen(),
    const SettingsScreen(),
  ];

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      body: SafeArea(child: _pages[_currentIndex]),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          _showAddTransactionModal(context);
        },
        backgroundColor: theme.colorScheme.primary,
        foregroundColor: Colors.white,
        shape: const CircleBorder(),
        elevation: 6,
        child: const Icon(Icons.add, size: 32),
      ),
      floatingActionButtonLocation: FloatingActionButtonLocation.centerDocked,
      bottomNavigationBar: BottomAppBar(
        shape: const CircularNotchedRectangle(),
        notchMargin: 8.0,
        color: theme.colorScheme.surface,
        elevation: 10,
        child: SizedBox(
          height: 64,
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              _buildNavItem(Icons.home_outlined, Icons.home, 'Home', 0),
              _buildNavItem(Icons.insights_outlined, Icons.insights, 'Analytics', 1),
              const SizedBox(width: 40), // Space for FAB
              _buildNavItem(Icons.account_balance_wallet_outlined, Icons.account_balance_wallet, 'Budgets', 2),
              _buildNavItem(Icons.settings_outlined, Icons.settings, 'Settings', 3),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildNavItem(IconData unselectedIcon, IconData selectedIcon, String label, int index) {
    final theme = Theme.of(context);
    final isSelected = _currentIndex == index;
    final color = isSelected ? theme.colorScheme.primary : Colors.grey.shade400;
    
    return GestureDetector(
      onTap: () {
        setState(() {
          _currentIndex = index;
        });
      },
      behavior: HitTestBehavior.opaque,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            isSelected ? selectedIcon : unselectedIcon, 
            color: color, 
            size: 26,
          ),
          const SizedBox(height: 4),
          Text(
            label,
            style: TextStyle(
              color: color,
              fontSize: 10,
              fontWeight: isSelected ? FontWeight.bold : FontWeight.w600,
              letterSpacing: 0.5,
            ),
          ),
          if (isSelected)
            Container(
              margin: const EdgeInsets.only(top: 4),
              width: 4,
              height: 4,
              decoration: BoxDecoration(
                color: theme.colorScheme.primary,
                shape: BoxShape.circle,
              ),
            ),
        ],
      ),
    );
  }

  void _showAddTransactionModal(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      useSafeArea: true,
      builder: (context) => const AddTransactionModal(),
    );
  }
}
