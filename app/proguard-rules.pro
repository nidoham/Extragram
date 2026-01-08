# Add project specific ProGuard rules here
# For more details, see http://developer.android.com/guide/developing/tools/proguard.html

# Keep line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Optimization passes
-optimizationpasses 5
-dontusemixedcaseclassnames
-verbose

# Keep generic signature of Call, Response (R8 full mode needs this)
-keepattributes Signature,InnerClasses,EnclosingMethod

# Keep Exceptions
-keepattributes *Annotation*,Exceptions

# Firebase
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.firebase.firestore.* <methods>;
}
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.nidoham.extragram.**$$serializer { *; }
-keepclassmembers class com.nidoham.extragram.** {
    *** Companion;
}
-keepclasseswithmembers class com.nidoham.extragram.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep data classes
-keep @kotlinx.serialization.Serializable class * { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Compose
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }

# Coil
-keep class coil3.** { *; }
-keep interface coil3.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$ViewComponentBuilderEntryPoint
-keepclasseswithmembers class * {
    @dagger.hilt.android.AndroidEntryPoint <init>(...);
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Retrofit/Gson (if you add them later)
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class sun.misc.Unsafe { *; }

# Parcelize
-keep interface org.jetbrains.annotations.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

# Remove logging in production
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Material Design
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**