# ML Kit ProGuard rules
-keep class com.google.android.gms.internal.mlkit_vision_text_common.** { *; }
-keep class com.google.mlkit.vision.text.** { *; }
-keep class com.google.android.gms.tflite.** { *; }
-keep class com.google.android.gms.common.** { *; }

# For Chinese text recognition if used
-dontwarn com.google.mlkit.vision.text.chinese.**
-dontwarn com.google.mlkit.vision.text.devanagari.**
-dontwarn com.google.mlkit.vision.text.japanese.**
-dontwarn com.google.mlkit.vision.text.korean.**
