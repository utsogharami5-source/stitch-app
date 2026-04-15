import 'dart:io';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import 'package:path_provider/path_provider.dart';
import 'package:share_plus/share_plus.dart';
import 'package:file_picker/file_picker.dart';
import '../providers/app_state.dart';
import '../models/models.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final appState = Provider.of<AppState>(context);
    final user = appState.currentUser;
    final isAuthenticated = appState.isAuthenticated;
    
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
                const Text(
                  'Settings',
                  style: TextStyle(
                    fontSize: 32,
                    fontWeight: FontWeight.w800,
                    letterSpacing: -1.0,
                  ),
                ),
                if (appState.isLoading)
                  const SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  ),
              ],
            ),
            const SizedBox(height: 8),
            Text(
              'Manage your preferences and account.',
              style: TextStyle(color: Colors.grey.shade600, fontSize: 13),
            ),
            
            const SizedBox(height: 32),
            
            // Profile Card
            Container(
              padding: const EdgeInsets.all(24),
              decoration: BoxDecoration(
                color: theme.colorScheme.surface,
                borderRadius: BorderRadius.circular(32),
                boxShadow: [
                  BoxShadow(color: Colors.black.withAlpha(10), blurRadius: 30, offset: const Offset(0, 10)),
                ],
              ),
              child: Column(
                children: [
                  Row(
                    children: [
                      CircleAvatar(
                        radius: 35,
                        backgroundImage: user?.photoURL != null 
                            ? NetworkImage(user!.photoURL!) 
                            : null,
                        backgroundColor: theme.primaryColor.withAlpha(51),
                        child: user?.photoURL == null
                            ? Icon(Icons.person, color: theme.primaryColor, size: 30)
                            : null,
                      ),
                      const SizedBox(width: 20),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              user?.displayName ?? (isAuthenticated ? 'User' : 'Guest'), 
                              style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 20)
                            ),
                            Text(
                              user?.email ?? 'Sign in to sync your data', 
                              style: TextStyle(color: Colors.grey.shade500, fontSize: 13)
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 24),
                  if (!isAuthenticated)
                    ElevatedButton.icon(
                      onPressed: () => appState.signInWithGoogle(),
                      icon: Container(
                        width: 22,
                        height: 22,
                        alignment: Alignment.center,
                        child: const Text('G', style: TextStyle(fontSize: 16, fontWeight: FontWeight.w900, color: Color(0xFF4285F4))),
                      ),
                      label: const Text('Sign in with Google', style: TextStyle(fontWeight: FontWeight.bold)),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.white,
                        foregroundColor: Colors.black,
                        minimumSize: const Size.fromHeight(56),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(16),
                          side: BorderSide(color: Colors.grey.shade300),
                        ),
                        elevation: 0,
                      ),
                    )
                  else
                    Row(
                      children: [
                        Expanded(
                          child: OutlinedButton.icon(
                            onPressed: () {
                              appState.syncNow();
                              ScaffoldMessenger.of(context).showSnackBar(
                                const SnackBar(content: Text('Syncing your data...'), duration: Duration(seconds: 1)),
                              );
                            },
                            icon: const Icon(Icons.sync, size: 18),
                            label: const Text('Sync Now', style: TextStyle(fontWeight: FontWeight.bold)),
                            style: OutlinedButton.styleFrom(
                              minimumSize: const Size.fromHeight(50),
                              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                            ),
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: OutlinedButton.icon(
                            onPressed: () => _confirmSignOut(context, appState),
                            icon: const Icon(Icons.logout, size: 18),
                            label: const Text('Sign Out', style: TextStyle(fontWeight: FontWeight.bold)),
                            style: OutlinedButton.styleFrom(
                              foregroundColor: Colors.red,
                              side: const BorderSide(color: Colors.red),
                              minimumSize: const Size.fromHeight(50),
                              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                            ),
                          ),
                        ),
                      ],
                    ),
                ],
              ),
            ),
            
            const SizedBox(height: 32),
            
            // Preferences Group
            _buildSettingsGroup(
              context,
              children: [
                _buildThemeToggle(context, appState),
                _buildCurrencyPicker(context, appState),
                _buildNavigationItem(
                  context,
                  icon: Icons.notifications_active_outlined,
                  title: 'Notifications',
                  subtitle: 'Alerts, budgets, and summaries',
                  onTap: () => _showNotificationSettings(context),
                ),
                _buildNavigationItem(
                  context,
                  icon: Icons.manage_accounts_outlined,
                  title: 'Account Security',
                  subtitle: isAuthenticated 
                      ? 'Signed in via ${user?.providerData.isNotEmpty == true ? user!.providerData.first.providerId : "email"}' 
                      : 'Sign in to manage',
                  onTap: () => _showAccountSecurity(context, appState),
                ),
                _buildExportItem(context, appState),
              ],
            ),

            const SizedBox(height: 24),

            // App Info
            _buildSettingsGroup(
              context,
              children: [
                _buildNavigationItem(
                  context,
                  icon: Icons.info_outline,
                  title: 'About CoinFlow',
                  subtitle: 'Version 1.0.0',
                  onTap: () => _showAboutDialog(context),
                ),
                _buildNavigationItem(
                  context,
                  icon: Icons.privacy_tip_outlined,
                  title: 'Privacy Policy',
                  subtitle: 'How we handle your data',
                  onTap: () => _showPrivacyPolicy(context),
                ),
              ],
            ),
            
            const SizedBox(height: 48),
            
            // Danger Zone
            Container(
              padding: const EdgeInsets.all(32),
              decoration: BoxDecoration(
                color: Colors.red.withAlpha(13),
                borderRadius: BorderRadius.circular(32),
                border: Border.all(color: Colors.red.withAlpha(25)),
              ),
              child: Column(
                children: [
                  Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: theme.colorScheme.surface,
                      borderRadius: BorderRadius.circular(16),
                      boxShadow: [BoxShadow(color: Colors.red.withAlpha(13), blurRadius: 10)],
                    ),
                    child: const Icon(Icons.warning_amber_rounded, color: Colors.red),
                  ),
                  const SizedBox(height: 16),
                  const Text('Need a Fresh Start?', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
                  const SizedBox(height: 8),
                  Text(
                    'Resetting your data will permanently delete all local records and cannot be undone.',
                    textAlign: TextAlign.center,
                    style: TextStyle(color: Colors.grey.shade600, fontSize: 12),
                  ),
                  const SizedBox(height: 24),
                  OutlinedButton(
                    onPressed: () => _confirmDeleteData(context, appState),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: Colors.red,
                      side: BorderSide(color: Colors.red.withAlpha(51), width: 2),
                      minimumSize: const Size.fromHeight(56),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                    ),
                    child: const Text('Delete My Data', style: TextStyle(fontWeight: FontWeight.bold)),
                  ),
                  if (isAuthenticated) ...[
                    const SizedBox(height: 16),
                    ElevatedButton(
                      onPressed: () => _confirmDeleteAccount(context, appState),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.red,
                        foregroundColor: Colors.white,
                        minimumSize: const Size.fromHeight(56),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                        elevation: 0,
                      ),
                      child: const Text('Permanently Delete Account', style: TextStyle(fontWeight: FontWeight.bold)),
                    ),
                  ],
                ],
              ),
            ),
            const SizedBox(height: 40),
          ],
        ),
      ),
    );
  }

  // ─── Dark Mode Toggle ──────────────────────────────────
  Widget _buildThemeToggle(BuildContext context, AppState appState) {
    final theme = Theme.of(context);
    final isDark = appState.themeMode == ThemeMode.dark;
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: theme.primaryColor.withAlpha(25),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Icon(Icons.palette_outlined, color: theme.primaryColor, size: 24),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('Appearance', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                Text(isDark ? 'Midnight mode active' : 'Clean mode active', style: TextStyle(color: Colors.grey.shade500, fontSize: 11)),
              ],
            ),
          ),
          Container(
            padding: const EdgeInsets.all(4),
            decoration: BoxDecoration(
              color: Colors.grey.withAlpha(25),
              borderRadius: BorderRadius.circular(50),
            ),
            child: Row(
              children: [
                GestureDetector(
                  onTap: () => appState.setThemeMode(ThemeMode.light),
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                    decoration: BoxDecoration(
                      color: !isDark ? theme.colorScheme.surface : Colors.transparent,
                      borderRadius: BorderRadius.circular(50),
                      boxShadow: !isDark ? [BoxShadow(color: Colors.black.withAlpha(13), blurRadius: 10)] : [],
                    ),
                    child: Text('Clean', style: TextStyle(color: !isDark ? theme.primaryColor : Colors.grey.shade500, fontWeight: FontWeight.bold, fontSize: 11)),
                  ),
                ),
                GestureDetector(
                  onTap: () => appState.setThemeMode(ThemeMode.dark),
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                    decoration: BoxDecoration(
                      color: isDark ? theme.colorScheme.surface : Colors.transparent,
                      borderRadius: BorderRadius.circular(50),
                      boxShadow: isDark ? [BoxShadow(color: Colors.black.withAlpha(13), blurRadius: 10)] : [],
                    ),
                    child: Text('Midnight', style: TextStyle(color: isDark ? theme.primaryColor : Colors.grey.shade500, fontWeight: FontWeight.bold, fontSize: 11)),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  // ─── Currency Picker ──────────────────────────────────
  Widget _buildCurrencyPicker(BuildContext context, AppState appState) {
    final theme = Theme.of(context);
    return InkWell(
      onTap: () => _showCurrencyDialog(context, appState),
      borderRadius: BorderRadius.circular(32),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(
                color: theme.primaryColor.withAlpha(25),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Icon(Icons.currency_exchange, color: theme.primaryColor, size: 24),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('Currency', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                  Text('${appState.currencyCode} (${appState.currencySymbol})', style: TextStyle(color: Colors.grey.shade500, fontSize: 11)),
                ],
              ),
            ),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
              decoration: BoxDecoration(
                color: theme.primaryColor.withAlpha(15),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Text(
                appState.currencySymbol,
                style: TextStyle(color: theme.primaryColor, fontWeight: FontWeight.bold, fontSize: 18),
              ),
            ),
            const SizedBox(width: 8),
            Icon(Icons.chevron_right, color: Colors.grey.shade400),
          ],
        ),
      ),
    );
  }

  void _showCurrencyDialog(BuildContext context, AppState appState) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
        title: const Text('Select Currency', style: TextStyle(fontWeight: FontWeight.bold)),
        content: SizedBox(
          width: double.maxFinite,
          child: ListView(
            shrinkWrap: true,
            children: AppState.availableCurrencies.entries.map((entry) {
              final isSelected = appState.currencyCode == entry.key;
              return ListTile(
                leading: Container(
                  width: 40,
                  height: 40,
                  alignment: Alignment.center,
                  decoration: BoxDecoration(
                    color: isSelected 
                        ? Theme.of(context).primaryColor.withAlpha(25) 
                        : Colors.grey.withAlpha(20),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Text(
                    entry.value,
                    style: TextStyle(
                      fontSize: 20,
                      fontWeight: FontWeight.bold,
                      color: isSelected ? Theme.of(context).primaryColor : null,
                    ),
                  ),
                ),
                title: Text(entry.key, style: TextStyle(fontWeight: isSelected ? FontWeight.bold : FontWeight.normal)),
                trailing: isSelected ? Icon(Icons.check_circle, color: Theme.of(context).primaryColor) : null,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                onTap: () {
                  appState.setCurrency(entry.key);
                  Navigator.pop(ctx);
                },
              );
            }).toList(),
          ),
        ),
      ),
    );
  }

  // ─── Export ──────────────────────────────────
  Widget _buildExportItem(BuildContext context, AppState appState) {
    final theme = Theme.of(context);
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: theme.primaryColor.withAlpha(25),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Icon(Icons.cloud_download_outlined, color: theme.primaryColor, size: 24),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('Data Export', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                Text('${appState.allTransactions.length} transactions', style: TextStyle(color: Colors.grey.shade500, fontSize: 11)),
              ],
            ),
          ),
          Wrap(
            spacing: 8,
            children: [
              _buildExportButton(context, 'Export CSV', () => _exportCSV(context, appState)),
              _buildExportButton(context, 'Import CSV', () => _importCSV(context, appState), isImport: true),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildExportButton(BuildContext context, String label, VoidCallback onTap, {bool isImport = false}) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        decoration: BoxDecoration(
          color: isImport ? Colors.green.shade600 : Theme.of(context).primaryColor,
          borderRadius: BorderRadius.circular(12),
        ),
        child: Text(label, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 12, color: Colors.white)),
      ),
    );
  }

  Future<void> _importCSV(BuildContext context, AppState appState) async {
    try {
      final result = await FilePicker.platform.pickFiles(
        type: FileType.custom,
        allowedExtensions: ['csv'],
      );

      if (result != null && result.files.single.path != null) {
        final file = File(result.files.single.path!);
        final content = await file.readAsString();
        
        await appState.importTransactionsFromCsv(content);
        
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Data imported successfully!')),
          );
        }
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Import failed: $e')),
        );
      }
    }
  }

  Future<void> _exportCSV(BuildContext context, AppState appState) async {
    if (appState.allTransactions.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('No transactions to export!')),
      );
      return;
    }

    try {
      final buffer = StringBuffer();
      buffer.writeln('Date,Title,Type,Amount (${appState.currencyCode}),Category,Note');
      
      for (var tx in appState.allTransactions) {
        final catName = appState.categories
            .firstWhere((c) => c.id == tx.categoryId, orElse: () => Category(id: '', name: 'Other', color: 'FF999999', icon: 'category'))
            .name;
        final note = tx.note?.replaceAll('"', '""') ?? '';
        buffer.writeln('${DateFormat('yyyy-MM-dd').format(tx.date)},"${tx.title}",${tx.type},${tx.amount},"$catName","$note"');
      }

      final tempDir = await getTemporaryDirectory();
      final file = File('${tempDir.path}/coinflow_export_${DateFormat('yyyyMMdd_HHmmss').format(DateTime.now())}.csv');
      await file.writeAsString(buffer.toString());

      await Share.shareXFiles(
        [XFile(file.path)],
        subject: 'CoinFlow Export',
      );

      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Exported ${appState.allTransactions.length} transactions!'), duration: const Duration(seconds: 2)),
        );
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Export failed: $e'), duration: const Duration(seconds: 3)),
        );
      }
    }
  }

  // ─── Notifications Settings ──────────────────────────────────
  void _showNotificationSettings(BuildContext context) {
    showDialog(
      context: context,
      builder: (ctx) => _NotificationSettingsDialog(),
    );
  }

  // ─── Account Security ──────────────────────────────────
  void _showAccountSecurity(BuildContext context, AppState appState) {
    if (!appState.isAuthenticated) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Sign in first to manage account security.')),
      );
      return;
    }

    final user = appState.currentUser!;
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
        title: const Text('Account Security', style: TextStyle(fontWeight: FontWeight.bold)),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            _buildSecurityRow(Icons.email, 'Email', user.email ?? 'Not set'),
            const SizedBox(height: 12),
            _buildSecurityRow(Icons.fingerprint, 'UID', '${user.uid.substring(0, 12)}...'),
            const SizedBox(height: 12),
            _buildSecurityRow(
              Icons.verified_user, 
              'Provider', 
              user.providerData.isNotEmpty ? user.providerData.first.providerId : 'Unknown',
            ),
            const SizedBox(height: 12),
            _buildSecurityRow(
              Icons.verified, 
              'Email Verified', 
              user.emailVerified ? 'Yes ✓' : 'No',
            ),
            const SizedBox(height: 20),
            if (!user.emailVerified)
              OutlinedButton.icon(
                onPressed: () async {
                  try {
                    await user.sendEmailVerification();
                    if (ctx.mounted) {
                      Navigator.pop(ctx);
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('Verification email sent!')),
                      );
                    }
                  } catch (e) {
                    if (ctx.mounted) {
                      ScaffoldMessenger.of(context).showSnackBar(
                        SnackBar(content: Text('Failed: $e')),
                      );
                    }
                  }
                },
                icon: const Icon(Icons.send, size: 18),
                label: const Text('Send Verification Email'),
                style: OutlinedButton.styleFrom(
                  minimumSize: const Size.fromHeight(48),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                ),
              ),
            OutlinedButton.icon(
              onPressed: () async {
                try {
                  await user.reload();
                  if (ctx.mounted) {
                    Navigator.pop(ctx);
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('Account refreshed!')),
                    );
                  }
                } catch (e) {
                  if (ctx.mounted) {
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text('Refresh failed: $e')),
                    );
                  }
                }
              },
              icon: const Icon(Icons.refresh, size: 18),
              label: const Text('Refresh Account'),
              style: OutlinedButton.styleFrom(
                minimumSize: const Size.fromHeight(48),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
              ),
            ),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Close')),
        ],
      ),
    );
  }

  Widget _buildSecurityRow(IconData icon, String label, String value) {
    return Row(
      children: [
        Icon(icon, size: 20, color: Colors.grey),
        const SizedBox(width: 12),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(label, style: TextStyle(fontSize: 11, color: Colors.grey.shade500, fontWeight: FontWeight.bold)),
              Text(value, style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w600)),
            ],
          ),
        ),
      ],
    );
  }

  // ─── Privacy Policy ──────────────────────────────────
  void _showPrivacyPolicy(BuildContext context) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
        title: const Text('Privacy Policy', style: TextStyle(fontWeight: FontWeight.bold)),
        content: const SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              Text('CoinFlow Privacy Policy', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
              SizedBox(height: 12),
              Text('• Your financial data is stored locally on your device using SQLite.', style: TextStyle(fontSize: 13)),
              SizedBox(height: 8),
              Text('• If you sign in, data is synced to Firebase Cloud Firestore under your authenticated user account.', style: TextStyle(fontSize: 13)),
              SizedBox(height: 8),
              Text('• We do not sell or share your data with third parties.', style: TextStyle(fontSize: 13)),
              SizedBox(height: 8),
              Text('• You can delete all your data at any time from Settings.', style: TextStyle(fontSize: 13)),
              SizedBox(height: 8),
              Text('• Authentication is handled through Firebase Auth and Google Sign-In.', style: TextStyle(fontSize: 13)),
              SizedBox(height: 16),
              Text('Last updated: April 2026', style: TextStyle(color: Colors.grey, fontSize: 11)),
            ],
          ),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Close')),
        ],
      ),
    );
  }

  // ─── Dialogs ──────────────────────────────────
  void _confirmSignOut(BuildContext context, AppState appState) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        title: const Text('Sign Out?'),
        content: const Text('Your local data will stay on this device. Cloud sync will stop until you sign in again.'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
          TextButton(
            onPressed: () {
              Navigator.pop(ctx);
              appState.signOut();
            },
            child: const Text('Sign Out', style: TextStyle(color: Colors.red, fontWeight: FontWeight.bold)),
          ),
        ],
      ),
    );
  }

  void _confirmDeleteData(BuildContext context, AppState appState) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        title: const Text('Delete All Data?', style: TextStyle(color: Colors.red)),
        content: const Text('This will permanently delete all your local transactions, categories, and goals. This action cannot be undone.'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
          TextButton(
            onPressed: () async {
              Navigator.pop(ctx);
              await appState.deleteAllData();
              if (context.mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('All data deleted.'), duration: Duration(seconds: 2)),
                );
              }
            },
            child: const Text('Delete Everything', style: TextStyle(color: Colors.red, fontWeight: FontWeight.bold)),
          ),
        ],
      ),
    );
  }

  void _confirmDeleteAccount(BuildContext context, AppState appState) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
        title: const Text('Delete Account Permanently?', style: TextStyle(color: Colors.red, fontWeight: FontWeight.bold)),
        content: const Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'This is a permanent action that cannot be reversed. By deleting your account, you will:',
              style: TextStyle(fontWeight: FontWeight.bold),
            ),
            SizedBox(height: 16),
            Text('• Permanently lose access to your CoinFlow profile.'),
            Text('• Delete all your synced transactions and goals from the cloud.'),
            Text('• Wipe all local data from this device.'),
            SizedBox(height: 16),
            Text(
              'Note: You may be asked to re-authenticate for security reasons before the deletion proceeds.',
              style: TextStyle(fontSize: 12, color: Colors.grey),
            ),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
          ElevatedButton(
            onPressed: () async {
              Navigator.pop(ctx);
              try {
                await appState.deleteUserAccount();
                if (context.mounted) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Your account and all data have been permanently deleted.'), duration: Duration(seconds: 5)),
                  );
                  Navigator.of(context).popUntil((route) => route.isFirst);
                }
              } catch (e) {
                if (context.mounted) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(
                      content: Text('Deletion failed: $e\n\nPlease try signing out and signing in again to refresh your session.'),
                      backgroundColor: Colors.red,
                      duration: const Duration(seconds: 8),
                    ),
                  );
                }
              }
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: Colors.red,
              foregroundColor: Colors.white,
              elevation: 0,
            ),
            child: const Text('Delete Permanently', style: TextStyle(fontWeight: FontWeight.bold)),
          ),
        ],
      ),
    );
  }

  void _showAboutDialog(BuildContext context) {
    final theme = Theme.of(context);
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.account_balance_wallet, size: 48, color: theme.primaryColor),
            const SizedBox(height: 16),
            const Text('CoinFlow', style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold)),
            const SizedBox(height: 4),
            Text('Version 1.0.0', style: TextStyle(color: Colors.grey.shade500)),
            const SizedBox(height: 16),
            Text(
              'Master your money with class.\nOffline-first, cloud-synced expense tracker.',
              textAlign: TextAlign.center,
              style: TextStyle(color: Colors.grey.shade600, fontSize: 13),
            ),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Close')),
        ],
      ),
    );
  }

  Widget _buildSettingsGroup(BuildContext context, {required List<Widget> children}) {
    return Container(
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surface,
        borderRadius: BorderRadius.circular(32),
        boxShadow: [
          BoxShadow(color: Colors.black.withAlpha(8), blurRadius: 20, offset: const Offset(0, 10)),
        ],
      ),
      child: Column(
        children: children.asMap().entries.map((entry) {
          final isLast = entry.key == children.length - 1;
          return Column(
            children: [
              entry.value,
              if (!isLast) Divider(height: 1, color: Colors.grey.withAlpha(25), indent: 70),
            ],
          );
        }).toList(),
      ),
    );
  }

  Widget _buildNavigationItem(BuildContext context, {required IconData icon, required String title, required String subtitle, required VoidCallback onTap}) {
    final theme = Theme.of(context);
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(32),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(
                color: theme.primaryColor.withAlpha(25),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Icon(icon, color: theme.primaryColor, size: 24),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(title, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                  Text(subtitle, style: TextStyle(color: Colors.grey.shade500, fontSize: 11)),
                ],
              ),
            ),
            Icon(Icons.chevron_right, color: Colors.grey.shade400),
          ],
        ),
      ),
    );
  }
}

// ─── Notification Settings Dialog ──────────────────────────────────
class _NotificationSettingsDialog extends StatefulWidget {
  @override
  State<_NotificationSettingsDialog> createState() => _NotificationSettingsDialogState();
}

class _NotificationSettingsDialogState extends State<_NotificationSettingsDialog> {
  bool _budgetAlerts = true;
  bool _dailySummary = false;
  bool _weeklySummary = true;

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
      title: const Text('Notification Settings', style: TextStyle(fontWeight: FontWeight.bold)),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          SwitchListTile(
            title: const Text('Budget Alerts', style: TextStyle(fontWeight: FontWeight.w600)),
            subtitle: const Text('Alert when you hit 80% of a budget', style: TextStyle(fontSize: 11)),
            value: _budgetAlerts,
            onChanged: (v) => setState(() => _budgetAlerts = v),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          ),
          SwitchListTile(
            title: const Text('Daily Summary', style: TextStyle(fontWeight: FontWeight.w600)),
            subtitle: const Text('End-of-day spending recap', style: TextStyle(fontSize: 11)),
            value: _dailySummary,
            onChanged: (v) => setState(() => _dailySummary = v),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          ),
          SwitchListTile(
            title: const Text('Weekly Report', style: TextStyle(fontWeight: FontWeight.w600)),
            subtitle: const Text('Weekly spending insights', style: TextStyle(fontSize: 11)),
            value: _weeklySummary,
            onChanged: (v) => setState(() => _weeklySummary = v),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          ),
        ],
      ),
      actions: [
        TextButton(onPressed: () => Navigator.pop(context), child: const Text('Cancel')),
        ElevatedButton(
          onPressed: () {
            Navigator.pop(context);
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('Notification preferences saved!')),
            );
          },
          child: const Text('Save'),
        ),
      ],
    );
  }
}

