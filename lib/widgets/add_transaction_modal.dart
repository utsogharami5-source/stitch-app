import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import 'package:image_picker/image_picker.dart';
import 'package:google_mlkit_text_recognition/google_mlkit_text_recognition.dart';
import '../providers/app_state.dart';
import '../models/models.dart';
import '../services/permission_service.dart';

class AddTransactionModal extends StatefulWidget {
  final Transaction? initialTransaction;

  const AddTransactionModal({super.key, this.initialTransaction});

  @override
  State<AddTransactionModal> createState() => _AddTransactionModalState();
}

class _AddTransactionModalState extends State<AddTransactionModal> {
  String _amount = "0";
  String _type = 'expense';
  String? _selectedCategoryId;
  DateTime _selectedDate = DateTime.now();
  late TextEditingController _titleController;
  late TextEditingController _noteController;

  @override
  void initState() {
    super.initState();
    final tx = widget.initialTransaction;
    if (tx != null) {
      _amount = tx.amount % 1 == 0 ? tx.amount.toInt().toString() : tx.amount.toString();
      _type = tx.type;
      _selectedCategoryId = tx.categoryId;
      _selectedDate = tx.date;
      _titleController = TextEditingController(text: tx.title);
      _noteController = TextEditingController(text: tx.note ?? "");
    } else {
      _titleController = TextEditingController();
      _noteController = TextEditingController();
    }
  }

  @override
  void dispose() {
    _titleController.dispose();
    _noteController.dispose();
    super.dispose();
  }

  void _onKeyPress(String key) {
    setState(() {
      if (key == 'backspace') {
        if (_amount.length > 1) {
          _amount = _amount.substring(0, _amount.length - 1);
        } else {
          _amount = "0";
        }
      } else if (key == '.') {
        if (!_amount.contains('.')) {
          _amount += ".";
        }
      } else {
        if (_amount == "0") {
          _amount = key;
        } else {
          if (_amount.contains('.') && _amount.split('.')[1].length >= 2) {
            return;
          }
          _amount += key;
        }
      }
    });
  }

  Future<void> _selectDate(BuildContext context) async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: _selectedDate,
      firstDate: DateTime(2000),
      lastDate: DateTime(2101),
    );
    if (picked != null && picked != _selectedDate) {
      setState(() {
        _selectedDate = picked;
      });
    }
  }

  void _saveTransaction() {
    final double amountValue = double.tryParse(_amount) ?? 0.0;
    if (amountValue <= 0) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please enter an amount greater than 0')),
      );
      return;
    }
    if (_selectedCategoryId == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please select a category')),
      );
      return;
    }

    final title = _titleController.text.trim().isEmpty 
        ? 'New $_type' 
        : _titleController.text.trim();

    final tx = Transaction(
      id: widget.initialTransaction?.id ?? DateTime.now().millisecondsSinceEpoch.toString(),
      title: title,
      amount: amountValue,
      type: _type,
      date: _selectedDate,
      categoryId: _selectedCategoryId!,
      note: _noteController.text.trim().isEmpty ? null : _noteController.text.trim(),
    );

    Provider.of<AppState>(context, listen: false).addTransaction(tx);
    Navigator.pop(context);
  }

  void _showAddCategoryDialog() {
    final titleController = TextEditingController();
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('New Category'),
        content: TextField(
          controller: titleController,
          decoration: const InputDecoration(hintText: 'Category Name (e.g. Travel)'),
          autofocus: true,
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: const Text('Cancel')),
          TextButton(
            onPressed: () {
              if (titleController.text.trim().isNotEmpty) {
                final newCat = Category(
                  id: DateTime.now().millisecondsSinceEpoch.toString(),
                  name: titleController.text.trim(),
                  color: 'FF4F46E5', // Default color
                  icon: 'category',   // Default icon
                );
                Provider.of<AppState>(context, listen: false).addCategory(newCat);
                setState(() => _selectedCategoryId = newCat.id);
                Navigator.pop(context);
              }
            },
            child: const Text('Add'),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final appState = Provider.of<AppState>(context);
    final theme = Theme.of(context);
    
    final categories = appState.categories;
    if (_selectedCategoryId == null && categories.isNotEmpty && widget.initialTransaction == null) {
      _selectedCategoryId = categories.first.id;
    }

    return Container(
      height: MediaQuery.of(context).size.height * 0.92,
      decoration: BoxDecoration(
        color: theme.scaffoldBackgroundColor,
        borderRadius: const BorderRadius.vertical(top: Radius.circular(32)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          // Handle
          Center(
            child: Container(
              margin: const EdgeInsets.only(top: 12),
              width: 40,
              height: 4,
              decoration: BoxDecoration(color: Colors.grey.shade300, borderRadius: BorderRadius.circular(2)),
            ),
          ),

          Expanded(
            child: SingleChildScrollView(
              padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  // Header
                  Row(
                    children: [
                      IconButton(
                        icon: const Icon(Icons.close),
                        onPressed: () => Navigator.pop(context),
                      ),
                      Expanded(
                        child: Text(
                          widget.initialTransaction == null ? 'Add Transaction' : 'Edit Transaction',
                          style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                          textAlign: TextAlign.center,
                        ),
                      ),
                      const SizedBox(width: 48),
                    ],
                  ),
                  
                  // Type Toggle
                  const SizedBox(height: 16),
                  Center(
                    child: Container(
                      padding: const EdgeInsets.all(4),
                      decoration: BoxDecoration(
                        color: Colors.grey.withAlpha(25),
                        borderRadius: BorderRadius.circular(50),
                      ),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          _buildTypeButton('Income', 'income'),
                          _buildTypeButton('Expense', 'expense'),
                        ],
                      ),
                    ),
                  ),
                  
                  // Amount Display
                  const SizedBox(height: 24),
                  Column(
                    children: [
                      const Text(
                        'CURRENT AMOUNT',
                        style: TextStyle(
                          fontSize: 10,
                          fontWeight: FontWeight.bold,
                          letterSpacing: 2,
                          color: Colors.grey,
                        ),
                      ),
                      const SizedBox(height: 8),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        crossAxisAlignment: CrossAxisAlignment.baseline,
                        textBaseline: TextBaseline.alphabetic,
                        children: [
                          Text(
                            appState.currencySymbol,
                            style: const TextStyle(fontSize: 32, fontWeight: FontWeight.bold, color: Colors.grey),
                          ),
                          const SizedBox(width: 4),
                          Text(
                            _amount,
                            style: const TextStyle(fontSize: 56, fontWeight: FontWeight.w800),
                          ),
                        ],
                      ),
                    ],
                  ),

                  // Title Input (PRD requirement)
                  const SizedBox(height: 16),
                  TextField(
                    controller: _titleController,
                    decoration: InputDecoration(
                      hintText: 'Transaction title (optional)',
                      hintStyle: TextStyle(color: Colors.grey.shade400, fontSize: 14),
                      filled: true,
                      fillColor: Colors.grey.withAlpha(20),
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(16),
                        borderSide: BorderSide.none,
                      ),
                      contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
                      prefixIcon: Icon(Icons.edit_outlined, color: Colors.grey.shade400, size: 20),
                    ),
                  ),

                  // Note Input
                  const SizedBox(height: 12),
                  TextField(
                    controller: _noteController,
                    decoration: InputDecoration(
                      hintText: 'Add a note...',
                      hintStyle: TextStyle(color: Colors.grey.shade400, fontSize: 14),
                      filled: true,
                      fillColor: Colors.grey.withAlpha(20),
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(16),
                        borderSide: BorderSide.none,
                      ),
                      contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
                      prefixIcon: Icon(Icons.note_outlined, color: Colors.grey.shade400, size: 20),
                    ),
                  ),
                  
                  const SizedBox(height: 20),
                  
                  // Custom Keypad
                  _buildKeypad(),
                  
                  const SizedBox(height: 20),
                  
                  // Contextual Controls (Date & Scan)
                  Row(
                    children: [
                      Expanded(
                        child: _buildActionButton(
                          icon: Icons.calendar_today,
                          label: DateFormat('MMM d').format(_selectedDate),
                          onTap: () => _selectDate(context),
                        ),
                      ),
                      const SizedBox(width: 16),
                      Expanded(
                        child: _buildActionButton(
                          icon: Icons.camera_alt_outlined,
                          label: 'Scan Receipt',
                          onTap: () => _scanReceipt(context),
                          isBordered: true,
                        ),
                      ),
                    ],
                  ),
                  
                  const SizedBox(height: 20),
                  
                  // Categories
                  const Text(
                    'QUICK CATEGORIES',
                    style: TextStyle(fontSize: 10, fontWeight: FontWeight.bold, letterSpacing: 1.5, color: Colors.grey),
                  ),
                  const SizedBox(height: 12),
                  SizedBox(
                    height: 50,
                    child: ListView(
                      scrollDirection: Axis.horizontal,
                      children: [
                        ...categories.map((cat) {
                          final isSelected = _selectedCategoryId == cat.id;
                          return GestureDetector(
                            onTap: () => setState(() => _selectedCategoryId = cat.id),
                            child: Container(
                              margin: const EdgeInsets.only(right: 12),
                              padding: const EdgeInsets.symmetric(horizontal: 20),
                              alignment: Alignment.center,
                              decoration: BoxDecoration(
                                color: isSelected ? theme.primaryColor : Colors.grey.withAlpha(25),
                                borderRadius: BorderRadius.circular(16),
                              ),
                              child: Text(
                                cat.name,
                                style: TextStyle(
                                  color: isSelected ? Colors.white : Colors.grey.shade600,
                                  fontWeight: FontWeight.bold,
                                  fontSize: 13,
                                ),
                              ),
                            ),
                          );
                        }),
                        GestureDetector(
                          onTap: _showAddCategoryDialog,
                          child: Container(
                            margin: const EdgeInsets.only(right: 12),
                            padding: const EdgeInsets.symmetric(horizontal: 16),
                            alignment: Alignment.center,
                            decoration: BoxDecoration(
                              color: Colors.grey.withAlpha(15),
                              borderRadius: BorderRadius.circular(16),
                              border: Border.all(color: Colors.grey.withAlpha(30)),
                            ),
                            child: const Icon(Icons.add, size: 20, color: Colors.grey),
                          ),
                        ),
                      ],
                    ),
                  ),
                  
                  const SizedBox(height: 24),
                  
                  // Save & Delete Buttons
                  Row(
                    children: [
                      if (widget.initialTransaction != null)
                        Padding(
                          padding: const EdgeInsets.only(right: 12),
                          child: IconButton.filled(
                            onPressed: () {
                              Provider.of<AppState>(context, listen: false).deleteTransaction(widget.initialTransaction!.id);
                              Navigator.pop(context);
                            },
                            icon: const Icon(Icons.delete_outline),
                            style: IconButton.styleFrom(
                              backgroundColor: Colors.red.withAlpha(25),
                              foregroundColor: Colors.red,
                              padding: const EdgeInsets.all(16),
                            ),
                          ),
                        ),
                      Expanded(
                        child: ElevatedButton(
                          onPressed: _saveTransaction,
                          style: ElevatedButton.styleFrom(
                            backgroundColor: theme.primaryColor,
                            foregroundColor: Colors.white,
                            minimumSize: const Size.fromHeight(68),
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
                            elevation: 4,
                          ),
                          child: Row(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Text(
                                widget.initialTransaction == null ? 'Save Transaction' : 'Update Transaction',
                                style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                              ),
                              const SizedBox(width: 8),
                              const Icon(Icons.arrow_forward),
                            ],
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 20),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _scanReceipt(BuildContext context) async {
    final hasPermission = await PermissionService.instance.requestCameraPermission(context);
    if (!hasPermission) return;

    // Pick image
    try {
      final ImagePicker picker = ImagePicker();
      final XFile? image = await picker.pickImage(source: ImageSource.camera, maxWidth: 1200);
      
      if (image == null) return;

      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Scanning receipt...'), duration: Duration(seconds: 2)),
        );
      }

      // OCR Recognition
      final inputImage = InputImage.fromFilePath(image.path);
      final textRecognizer = TextRecognizer();
      final RecognizedText recognizedText = await textRecognizer.processImage(inputImage);
      await textRecognizer.close();

      if (recognizedText.text.isEmpty) {
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('No text found on receipt. Try again with better lighting.')),
          );
        }
        return;
      }

      // Parse amount from text — look for currency patterns
      String? detectedAmount;
      String? detectedTitle;
      final lines = recognizedText.text.split('\n');
      
      // Try to find amounts (patterns like $12.34, 12.34, Total: 12.34)
      final amountRegex = RegExp(r'[\$৳€£₹¥₩]?\s*(\d{1,3}(?:[,\s]\d{3})*(?:\.\d{1,2})?)', caseSensitive: false);
      double maxAmount = 0;

      for (var line in lines) {
        final lowerLine = line.toLowerCase();
        // Prioritize lines with "total", "amount", "grand total", etc.
        if (lowerLine.contains('total') || lowerLine.contains('amount') || lowerLine.contains('due') || lowerLine.contains('sum')) {
          final matches = amountRegex.allMatches(line);
          for (var match in matches) {
            final numStr = match.group(1)?.replaceAll(RegExp(r'[,\s]'), '') ?? '';
            final val = double.tryParse(numStr) ?? 0;
            if (val > maxAmount) {
              maxAmount = val;
              detectedAmount = numStr;
            }
          }
        }
      }

      // Fallback: just find the largest number
      if (detectedAmount == null) {
        for (var line in lines) {
          final matches = amountRegex.allMatches(line);
          for (var match in matches) {
            final numStr = match.group(1)?.replaceAll(RegExp(r'[,\s]'), '') ?? '';
            final val = double.tryParse(numStr) ?? 0;
            if (val > maxAmount) {
              maxAmount = val;
              detectedAmount = numStr;
            }
          }
        }
      }

      // Try to detect store/merchant name (usually first non-empty line)
      for (var line in lines) {
        final trimmed = line.trim();
        if (trimmed.length > 2 && !RegExp(r'^\d+$').hasMatch(trimmed)) {
          detectedTitle = trimmed;
          break;
        }
      }

      // Apply detected values
      if (mounted) {
        setState(() {
          if (detectedAmount != null) {
            _amount = detectedAmount;
          }
          if (detectedTitle != null && _titleController.text.isEmpty) {
            _titleController.text = detectedTitle;
          }
          _type = 'expense'; // Receipts are usually expenses
        });
      }

      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
              detectedAmount != null 
                ? 'Found amount: $detectedAmount${detectedTitle != null ? ' from $detectedTitle' : ''}'
                : 'Could not auto-detect amount. Please enter manually.',
            ),
            duration: const Duration(seconds: 3),
          ),
        );
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Scan failed: $e')),
        );
      }
    }
  }

  Widget _buildTypeButton(String label, String value) {
    final isSelected = _type == value;
    return GestureDetector(
      onTap: () => setState(() => _type = value),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 12),
        decoration: BoxDecoration(
          color: isSelected ? (_type == 'expense' ? Colors.red : Colors.green) : Colors.transparent,
          borderRadius: BorderRadius.circular(50),
          boxShadow: isSelected ? [BoxShadow(color: (_type == 'expense' ? Colors.red : Colors.green).withAlpha(76), blurRadius: 10, offset: const Offset(0, 4))] : [],
        ),
        child: Text(
          label,
          style: TextStyle(
            color: isSelected ? Colors.white : Colors.grey,
            fontWeight: FontWeight.bold,
            fontSize: 13,
          ),
        ),
      ),
    );
  }

  Widget _buildActionButton({required IconData icon, required String label, required VoidCallback onTap, bool isBordered = false}) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        height: 56,
        decoration: BoxDecoration(
          color: isBordered ? Colors.transparent : Colors.grey.withAlpha(25),
          borderRadius: BorderRadius.circular(16),
          border: isBordered ? Border.all(color: Colors.grey.withAlpha(51), width: 2) : null,
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(icon, size: 20, color: Colors.grey.shade700),
            const SizedBox(width: 8),
            Text(label, style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13, color: Colors.grey.shade700)),
          ],
        ),
      ),
    );
  }

  Widget _buildKeypad() {
    final keys = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '0', 'backspace'];
    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 3,
        childAspectRatio: 2.0,
        mainAxisSpacing: 10,
        crossAxisSpacing: 10,
      ),
      itemCount: keys.length,
      itemBuilder: (context, index) {
        final key = keys[index];
        if (key == 'backspace') {
          return _buildKeypadButton(
            child: const Icon(Icons.backspace_outlined, size: 22),
            onPressed: () => _onKeyPress(key),
          );
        }
        return _buildKeypadButton(
          child: Text(key, style: const TextStyle(fontSize: 22, fontWeight: FontWeight.bold)),
          onPressed: () => _onKeyPress(key),
        );
      },
    );
  }

  Widget _buildKeypadButton({required Widget child, required VoidCallback onPressed}) {
    return GestureDetector(
      onTap: onPressed,
      child: Container(
        decoration: BoxDecoration(
          color: Colors.grey.withAlpha(15),
          borderRadius: BorderRadius.circular(16),
        ),
        alignment: Alignment.center,
        child: child,
      ),
    );
  }
}
