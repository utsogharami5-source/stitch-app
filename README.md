# CoinFlow — Smart Budget & Expense Tracker

![CoinFlow Banner](https://raw.githubusercontent.com/utsogharami5-source/smart_budget/main/assets/images/banner.png)

CoinFlow is a premium, offline-first expense tracker built with Flutter. It combines a stunning, modern UI with powerful features like cloud synchronization, receipt scanning, and robust security to help you master your finances with ease.

## ✨ Features

- **Smart Expense Tracking:** Quickly log transactions with categories, notes, and dates.
- **Dynamic Budgets:** Set monthly limits for different categories and track your progress in real-time.
- **Cloud Sync:** Seamlessly sync your data across devices using Firebase.
- **Offline First:** Fully functional without an internet connection using local SQLite storage.
- **Receipt Scanning:** Automatically extract transaction details from receipts using Google ML Kit.
- **Deep Analytics:** Visualize your spending habits with interactive charts and monthly summaries.
- **Robust Security:** Protect your sensitive data with a 4-digit PIN or Biometric (Fingerprint/Face) lock.
- **Data Portability:** Export your transaction history to CSV or import data from other sources.
- **Customizable Experience:** Choose between a sleek 'Midnight' dark mode and a 'Clean' light mode.

## 🛠️ Tech Stack

- **Framework:** [Flutter](https://flutter.dev)
- **State Management:** [Provider](https://pub.dev/packages/provider)
- **Local Database:** [SQLite (sqflite)](https://pub.dev/packages/sqflite)
- **Backend/Sync:** [Firebase Auth](https://firebase.google.com/docs/auth), [Cloud Firestore](https://firebase.google.com/docs/firestore), [Firebase Storage](https://firebase.google.com/docs/storage)
- **Machine Learning:** [Google ML Kit Text Recognition](https://pub.dev/packages/google_mlkit_text_recognition)
- **Security:** [local_auth](https://pub.dev/packages/local_auth)
- **Charts:** [fl_chart](https://pub.dev/packages/fl_chart)

## 🚀 Getting Started

### Prerequisites

- Flutter SDK (v3.0.0 or higher)
- Android Studio / VS Code
- A Firebase project (for cloud sync features)

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/utsogharami5-source/smart_budget.git
   cd smart_budget
   ```

2. **Install dependencies:**
   ```bash
   flutter pub get
   ```

3. **Configure Firebase:**
   - Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/).
   - Add an Android app with the package name `com.coinflow.app`.
   - Download the `google-services.json` and place it in `android/app/`.
   - Enable **Authentication** (Google & Email/Password), **Firestore**, and **Storage**.

4. **Run the app:**
   ```bash
   flutter run
   ```

## 🔐 Security Note

CoinFlow implements an optional App Lock. If enabled, it uses `local_auth` to provide biometric authentication. Ensure your device has at least one fingerprint or face enrolled for this feature to work.

## 👤 Developer

**Utso**
- **Portfolio:** [utsoportfolio.vercel.app](https://utsoportfolio.vercel.app/)
- **Email:** [utsogharami5@gmail.com](mailto:utsogharami5@gmail.com)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

*Made with ❤️ for better financial health.*
