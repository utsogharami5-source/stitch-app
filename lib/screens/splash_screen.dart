import 'package:flutter/material.dart';
import '../providers/app_state.dart';
import 'package:provider/provider.dart';

class SplashScreen extends StatelessWidget {
  const SplashScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final appMode = Provider.of<AppState>(context).themeMode;
    final isDark = appMode == ThemeMode.dark || 
        (appMode == ThemeMode.system && MediaQuery.of(context).platformBrightness == Brightness.dark);
    
    final logoPath = isDark ? 'assets/images/logo_dark.png' : 'assets/images/logo_light.png';
    final backgroundColor = isDark ? Colors.black : Colors.white;

    return Scaffold(
      backgroundColor: backgroundColor,
      body: Center(
        child: TweenAnimationBuilder<double>(
          tween: Tween<double>(begin: 0.0, end: 1.0),
          duration: const Duration(milliseconds: 1500),
          curve: Curves.easeOutCubic,
          builder: (context, value, child) {
            return Transform.scale(
              scale: 0.8 + (0.2 * value),
              child: Opacity(
                opacity: value,
                child: child,
              ),
            );
          },
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Image.asset(
                logoPath,
                width: 250,
                height: 250,
                fit: BoxFit.contain,
              ),
              const SizedBox(height: 24),
              const CircularProgressIndicator(
                valueColor: AlwaysStoppedAnimation<Color>(Colors.cyan),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
