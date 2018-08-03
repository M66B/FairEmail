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
-keepattributes SourceFile,LineNumberTable

# If you after the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

#App
-keep class eu.faircode.email.**
-keepnames class eu.faircode.email.** { *; }

#Support library
-keep class android.support.v7.app.AppCompatViewInflater{ <init>(...); }

#JavaMail
-dontshrink
-keep class javax.** {*;}
-keep class com.sun.** {*;}
-keep class myjava.** {*;}
-keep class org.apache.harmony.** {*;}
-dontwarn java.awt.**
-dontwarn java.beans.Beans
-dontwarn javax.activation.**
-dontwarn javax.security.**

#jsoup
-keeppackagenames org.jsoup.nodes
