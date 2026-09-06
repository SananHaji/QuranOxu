# ProGuard & R8 Configuration for QuranOxu

# Keep Domain Entities & Models
-keep class az.sananhaji.quranoxu.domain.model.** { *; }
-keep class az.sananhaji.quranoxu.data.** { *; }
-keep class az.sananhaji.quranoxu.ui.theme.** { *; }

# Kotlin Reflection & Annotations
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# AndroidX Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class androidx.room.** { *; }

# AndroidX Media3 / ExoPlayer
-keep class androidx.media3.common.** { *; }
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.session.** { *; }
-dontwarn androidx.media3.**

# Compose
-keepclassmembers class * extends androidx.compose.ui.Modifier { *; }
-dontwarn androidx.compose.**

# Firebase Analytics & Crashlytics
-keepattributes *Annotation*,SourceFile,LineNumberTable
-keepclassmembers class * {
    @com.google.firebase.crashlytics.** *;
}
-dontwarn com.google.firebase.**

