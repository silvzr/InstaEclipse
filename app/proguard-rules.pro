# Aggressive ProGuard rules for maximum size reduction while keeping Xposed functionality
-dontobfuscate
-optimizations !code/simplification/cast
-keepattributes *Annotation*
-allowaccessmodification
-mergeinterfacesaggressively
-overloadaggressively

# Keep essential classes for Xposed
-keep class ps.reso.instaeclipse.** { 
    public protected *;
    *** *Hook*(...);
    *** handle*(...);
}

# Keep Xposed API essentials only
-keep class de.robv.android.xposed.** { 
    public protected *;
}
-keep interface de.robv.android.xposed.** { *; }

# Keep DexKit essentials
-keep class org.luckypray.dexkit.** { 
    public *;
}

# Keep reflection targets but allow optimization elsewhere
-keepclassmembers class * {
    @de.robv.android.xposed.** *;
}

# Remove debug/unused code
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Remove unused methods from common classes
-assumenosideeffects class java.lang.System {
    public static void gc();
    public static long currentTimeMillis();
    public static void arraycopy(...);
}

# Aggressive removal of unused code
-assumenosideeffects class java.lang.String {
    public java.lang.String intern();
}

# Remove unused reflection capabilities
-assumenosideeffects class java.lang.Class {
    public java.lang.reflect.Method[] getDeclaredMethods();
    public java.lang.reflect.Field[] getDeclaredFields();
}

# More aggressive optimizations
-optimizations code/removal/*,code/allocation/variable

# Remove unused classes aggressively  
-assumenosideeffects class java.lang.StringBuilder {
    public java.lang.StringBuilder();
    public java.lang.StringBuilder(int);
    public java.lang.StringBuilder(java.lang.String);
    public java.lang.StringBuilder append(...);
    public java.lang.String toString();
}

# Remove reflection usage we don't need
-assumenosideeffects class java.lang.Class {
    public java.lang.String getName();
    public java.lang.String getSimpleName();
}

# Suppress warnings
-dontwarn javax.lang.model.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn org.checkerframework.**


# keep GSON serialized classes
-keep class * implements com.google.gson.JsonDeserializer { *; }
-keep class * implements com.google.gson.JsonSerializer { *; }

# Extremely aggressive removal of unused code
-assumenosideeffects class android.content.Context {
    public java.lang.String getPackageName();
    public android.content.pm.PackageManager getPackageManager();
}

-assumenosideeffects class android.content.res.Resources {
    public java.lang.String getString(int);
    public java.lang.String getString(int, java.lang.Object[]);
}

# Remove unused Android framework methods
-assumenosideeffects class android.os.Build {
    public static final java.lang.String MANUFACTURER;
    public static final java.lang.String MODEL;
    public static final java.lang.String BRAND;
}
