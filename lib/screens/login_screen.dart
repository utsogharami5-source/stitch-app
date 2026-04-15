import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/app_state.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> with SingleTickerProviderStateMixin {
  bool _isLoggingIn = false;
  late AnimationController _animController;
  late Animation<double> _fadeAnim;

  @override
  void initState() {
    super.initState();
    _animController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1200),
    );
    _fadeAnim = CurvedAnimation(parent: _animController, curve: Curves.easeOut);
    _animController.forward();
  }

  @override
  void dispose() {
    _animController.dispose();
    super.dispose();
  }

  Future<void> _handleGoogleSignIn() async {
    setState(() => _isLoggingIn = true);
    try {
      await Provider.of<AppState>(context, listen: false).signInWithGoogle();
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Login failed: $e'), backgroundColor: Colors.red),
        );
      }
    } finally {
      if (mounted) setState(() => _isLoggingIn = false);
    }
  }

  void _continueAsGuest() {
    Provider.of<AppState>(context, listen: false).continueAsGuest();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isLight = theme.brightness == Brightness.light;
    
    return Scaffold(
      body: Stack(
        children: [
          // Premium Background Gradient
          Container(
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: isLight
                    ? [const Color(0xFFF0F4FF), const Color(0xFFE0E7FF), const Color(0xFFF8FAFC)]
                    : [const Color(0xFF0F172A), const Color(0xFF1E1B4B), const Color(0xFF0F172A)],
              ),
            ),
          ),
          
          // Decorative blurry blobs
          Positioned(
            top: -100,
            right: -50,
            child: Container(
              width: 300,
              height: 300,
              decoration: BoxDecoration(
                color: theme.primaryColor.withAlpha(20),
                shape: BoxShape.circle,
              ),
            ),
          ),
          Positioned(
            bottom: -80,
            left: -60,
            child: Container(
              width: 250,
              height: 250,
              decoration: BoxDecoration(
                color: const Color(0xFF10B981).withAlpha(15),
                shape: BoxShape.circle,
              ),
            ),
          ),

          SafeArea(
            child: FadeTransition(
              opacity: _fadeAnim,
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 24.0),
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    const Spacer(flex: 3),
                    
                    // App Branding with real logo — NO ClipOval
                    Center(
                      child: Container(
                        width: 130,
                        height: 130,
                        decoration: BoxDecoration(
                          borderRadius: BorderRadius.circular(32),
                          boxShadow: [
                            BoxShadow(
                              color: theme.primaryColor.withAlpha(30),
                              blurRadius: 40,
                              offset: const Offset(0, 10),
                            ),
                          ],
                        ),
                        child: ClipRRect(
                          borderRadius: BorderRadius.circular(32),
                          child: Image.asset(
                            isLight ? 'assets/images/logo_light.png' : 'assets/images/logo_dark.png',
                            width: 130,
                            height: 130,
                            fit: BoxFit.cover,
                            errorBuilder: (context, error, stackTrace) => 
                              Container(
                                decoration: BoxDecoration(
                                  color: theme.primaryColor.withAlpha(40),
                                  borderRadius: BorderRadius.circular(32),
                                ),
                                child: Icon(Icons.account_balance_wallet, size: 64, color: theme.primaryColor),
                              ),
                          ),
                        ),
                      ),
                    ),
                    const SizedBox(height: 32),
                    const Text(
                      'CoinFlow',
                      textAlign: TextAlign.center,
                      style: TextStyle(
                        fontSize: 48, 
                        fontWeight: FontWeight.w900, 
                        letterSpacing: -2.0,
                      ),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      'Master your money with class.',
                      textAlign: TextAlign.center,
                      style: TextStyle(
                        color: isLight ? Colors.grey.shade600 : Colors.grey.shade400,
                        fontSize: 16, 
                        fontWeight: FontWeight.w500,
                        letterSpacing: 0.5,
                      ),
                    ),
                    
                    const Spacer(flex: 2),
                    
                    // Login Options Card (Glassmorphism)
                    Container(
                      padding: const EdgeInsets.all(32),
                      decoration: BoxDecoration(
                        color: theme.colorScheme.surface.withAlpha(isLight ? 200 : 150),
                        borderRadius: BorderRadius.circular(32),
                        border: Border.all(color: (isLight ? Colors.white : Colors.grey).withAlpha(30)),
                        boxShadow: [
                          BoxShadow(
                            color: Colors.black.withAlpha(20),
                            blurRadius: 40,
                            offset: const Offset(0, 15),
                          ),
                        ],
                      ),
                      child: Column(
                        children: [
                          const Text(
                            'Welcome Back',
                            style: TextStyle(fontSize: 26, fontWeight: FontWeight.bold),
                          ),
                          const SizedBox(height: 10),
                          Text(
                            'Sign in to secure your wealth and\naccess your sync across all devices.',
                            textAlign: TextAlign.center,
                            style: TextStyle(
                              color: isLight ? Colors.grey.shade600 : Colors.grey.shade500, 
                              fontSize: 14,
                              height: 1.4,
                            ),
                          ),
                          const SizedBox(height: 32),
                          
                          // Google Sign In Button
                          _isLoggingIn 
                            ? const Padding(
                                padding: EdgeInsets.all(16),
                                child: CircularProgressIndicator(),
                              )
                            : Container(
                                decoration: BoxDecoration(
                                  borderRadius: BorderRadius.circular(16),
                                  boxShadow: [
                                    BoxShadow(
                                      color: Colors.black.withAlpha(5),
                                      blurRadius: 10,
                                      offset: const Offset(0, 4),
                                    ),
                                  ],
                                ),
                                child: ElevatedButton(
                                  onPressed: _handleGoogleSignIn,
                                  style: ElevatedButton.styleFrom(
                                    backgroundColor: isLight ? Colors.white : const Color(0xFF334155),
                                    foregroundColor: isLight ? Colors.black87 : Colors.white,
                                    minimumSize: const Size.fromHeight(60),
                                    shape: RoundedRectangleBorder(
                                      borderRadius: BorderRadius.circular(16),
                                      side: BorderSide(
                                        color: isLight ? Colors.grey.shade200 : Colors.transparent,
                                      ),
                                    ),
                                    elevation: 0,
                                  ),
                                  child: Row(
                                    mainAxisAlignment: MainAxisAlignment.center,
                                    children: [
                                      Container(
                                        width: 22,
                                        height: 22,
                                        alignment: Alignment.center,
                                        child: const Text('G', style: TextStyle(fontSize: 18, fontWeight: FontWeight.w900, color: Color(0xFF4285F4))),
                                      ),
                                      const SizedBox(width: 14),
                                      const Text(
                                        'Continue with Google', 
                                        style: TextStyle(fontWeight: FontWeight.w700, fontSize: 16),
                                      ),
                                    ],
                                  ),
                                ),
                              ),
                          
                          const SizedBox(height: 20),
                          
                          // Guest Mode Button
                          TextButton(
                            onPressed: _continueAsGuest,
                            style: TextButton.styleFrom(
                              minimumSize: const Size.fromHeight(50),
                              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                            ),
                            child: Text(
                              'Continue as Guest',
                              style: TextStyle(
                                color: theme.primaryColor,
                                fontWeight: FontWeight.bold,
                                fontSize: 15,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                    
                    const Spacer(flex: 3),
                    
                    // Footer
                    Text(
                      'By continuing, you agree to our Terms of Service\nand Privacy Policy.',
                      textAlign: TextAlign.center,
                      style: TextStyle(
                        color: Colors.grey.shade500, 
                        fontSize: 12,
                        height: 1.5,
                      ),
                    ),
                    const SizedBox(height: 16),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
