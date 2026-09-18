# Add project specific ProGuard rules here.
# Preserve line numbers and attributes for clean stack traces
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable

# Room Database Entities and DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.paging.**

# CALCX Local Data Layer
-keep class com.example.data.local.entity.** { *; }
-keep class com.example.data.local.dao.** { *; }

# CALCX Mathematical Engines and Calculus AST
-keep class com.example.math.** { *; }
-keep class com.example.engine.** { *; }

# Kotlinx Serialization / Reflection / State models
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Coroutines and ViewModels
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

