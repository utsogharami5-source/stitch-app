# ── Flutter ──────────────────────────────────────────────
-keep class io.flutter.** { *; }
-keep class io.flutter.plugins.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# ── Firebase ─────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ── Google Sign-In ───────────────────────────────────────
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.common.** { *; }

# ── ML Kit (Text Recognition) ───────────────────────────
-keep class com.google.android.gms.internal.mlkit_vision_text_common.** { *; }
-keep class com.google.mlkit.vision.text.** { *; }
-keep class com.google.android.gms.tflite.** { *; }

# For non-Latin text recognition if used
-dontwarn com.google.mlkit.vision.text.chinese.**
-dontwarn com.google.mlkit.vision.text.devanagari.**
-dontwarn com.google.mlkit.vision.text.japanese.**
-dontwarn com.google.mlkit.vision.text.korean.**

# ── Gson / JSON ──────────────────────────────────────────
-keep class com.google.gson.** { *; }
-keepattributes EnclosingMethod

# ── Permission Handler ──────────────────────────────────
-keep class com.baseflow.permissionhandler.** { *; }

# ── Firebase Messaging ──────────────────────────────────
-keep class com.google.firebase.messaging.** { *; }

# ── General R8 safety ───────────────────────────────────
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# ── Google Play Core (Fixes R8 Tasks missing class) ──────
-keep class com.google.android.play.core.** { *; }
-dontwarn com.google.android.play.core.**
-keep class com.google.android.play.core.common.PlayCoreDialogWrapper { *; }

# ── Additional Firebase & Auth Stability ────────────────
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.android.gms.auth.api.** { *; }
-keep class com.google.android.gms.common.api.** { *; }
-keep class com.google.android.gms.tasks.** { *; }
-dontwarn com.google.android.gms.tasks.**

# Avoid stripping of standard library parts used by plugins
-keep class java.lang.** { *; }
-keep class java.util.** { *; }
-dontwarn java.lang.management.**
-dontwarn javax.annotation.**
