import 'package:flutter/material.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:google_sign_in/google_sign_in.dart';

class AuthService {
  final FirebaseAuth _auth = FirebaseAuth.instance;
  bool _initialized = false;

  // Stream of auth state changes
  Stream<User?> get user => _auth.authStateChanges();

  // Get current user
  User? get currentUser => _auth.currentUser;

  Future<void> _ensureInitialized() async {
    if (!_initialized) {
      await GoogleSignIn.instance.initialize();
      _initialized = true;
    }
  }

  // Sign in with Google (google_sign_in v7.x API)
  Future<UserCredential?> signInWithGoogle() async {
    try {
      await _ensureInitialized();

      // Authenticate — replaces the old signIn() method
      final googleUser = await GoogleSignIn.instance.authenticate();


      // Get idToken from authentication
      final GoogleSignInAuthentication googleAuth = googleUser.authentication;
      final OAuthCredential credential = GoogleAuthProvider.credential(
        idToken: googleAuth.idToken,
      );

      return await _auth.signInWithCredential(credential);
    } catch (e) {
      debugPrint('Error signing in with Google: $e');
      return null;
    }
  }

  // Sign in with email and password
  Future<UserCredential?> signInWithEmail(String email, String password) async {
    try {
      return await _auth.signInWithEmailAndPassword(email: email, password: password);
    } catch (e) {
      debugPrint('Error signing in with email: $e');
      return null;
    }
  }

  // Register with email and password
  Future<UserCredential?> registerWithEmail(String email, String password) async {
    try {
      return await _auth.createUserWithEmailAndPassword(email: email, password: password);
    } catch (e) {
      debugPrint('Error registering: $e');
      return null;
    }
  }

  // Sign out
  Future<void> signOut() async {
    try {
      await _ensureInitialized();
      await GoogleSignIn.instance.disconnect();
      await _auth.signOut();
    } catch (e) {
      debugPrint('Error signing out: $e');
    }
  }
}
