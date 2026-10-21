# ProGuard / R8 Rules for Samad Dining Hall App

# Line numbers in stack traces
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# Moshi rules
-dontwarn com.squareup.moshi.**
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-keep class * extends com.squareup.moshi.JsonAdapter { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}

# Data models
-keep class com.example.data.model.** { *; }
-keep class com.example.data.remote.** { *; }

# Retrofit rules
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Room rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class com.example.data.local.entity.** { *; }
-keep class com.example.data.local.dao.** { *; }

# Firebase rules
-dontwarn com.google.firebase.**
-keep class com.google.firebase.** { *; }
-keep class com.example.fcm.** { *; }

# App Widget & receivers
-keep class com.example.widget.** { *; }

# ZXing QR code generator
-keep class com.google.zxing.** { *; }
-keep class com.example.util.QrCodeGenerator** { *; }
