# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-dontwarn com.google.common.**
-dontwarn org.slf4j.**
-dontwarn com.fasterxml.**
-dontwarn android.os.Build.**

# Gson specific classes
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent TypeToken related issues
-keepclassmembers class com.google.gson.reflect.TypeToken {
    <methods>;
}
-keep class com.google.gson.reflect.TypeToken { *; }

# Keep model classes for Gson serialization/deserialization
-keep class com.hp.jetadvantage.link.logdaemon.model.** { *; }

# Keep generic signature of model classes (for Gson)
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# Prevent obfuscation of model class fields and methods
-keepclassmembers class com.hp.jetadvantage.link.logdaemon.model.** {
    <fields>;
    <methods>;
}

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Gson TypeAdapters
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep JsonParser utility class
-keep class com.hp.jetadvantage.link.logdaemon.util.JsonParser { *; }

# Keep all constructors for model classes
-keepclassmembers class com.hp.jetadvantage.link.logdaemon.model.** {
    <init>(...);
}

-keepattributes SourceFile,LineNumberTable

-keepnames class com.fasterxml.jackson.** {
    *;
}
-keepnames interface com.fasterxml.jackson.** {
    *;
}

# OkHttp 5.x
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }