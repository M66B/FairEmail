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
-keep class eu.faircode.email.** {*;}
-keepnames class eu.faircode.email.** {*;}

#AndroidX
-keep class androidx.appcompat.widget.** {*;}
-keep class androidx.appcompat.app.AppCompatViewInflater {<init>(...);}
#android.os.BadParcelableException: Parcelable protocol requires a Parcelable.Creator object called CREATOR on class androidx...
-keepclassmembers class * implements android.os.Parcelable {static ** CREATOR;}
#ROOM inline compilation
-keep class androidx.work.impl.** {*;}
#Stack traces, InvalidationTracker reflection
-keepnames class androidx.** {*;}

#IAB
-keep class com.android.billingclient.** {*;}
-keepnames class com.android.billingclient.** {*;}
-keep class com.android.vending.billing.** {*;}
-keepnames class com.android.vending.billing.** {*;}

#JavaMail
-keep class javax.** {*;}
-keep class com.sun.** {*;}
-keep class myjava.** {*;}
-keep class org.apache.harmony.** {*;}
-keep class mailcap.** {*;}
-keep class mimetypes.** {*;}
-keepnames class com.sun.mail.** {*;}

-dontwarn java.awt.**
-dontwarn java.beans.Beans
-dontwarn javax.activation.**
-dontwarn javax.security.**

#jsoup
-keeppackagenames org.jsoup.nodes
-keepnames class org.jsoup.** {*;}

#CSS Parser
-keepnames class com.steadystate.css.** {*;}
-keepnames class org.w3c.css.** {*;}
-keepnames class org.w3c.dom.** {*;}

#CSS Parser / biweekly
-dontwarn org.w3c.dom.**

#JCharset
-keep class net.freeutils.charset.** {*;}

#dnsjava
-keep class org.xbill.DNS.** {*;}
-keepnames class org.xbill.DNS.** {*;}

-dontwarn sun.net.spi.nameservice.**

#OpenPGP
-keep class org.openintents.openpgp.** {*;}
-keepnames class org.openintents.openpgp.** {*;}

#biweekly
-keepnames class biweekly.** {*;}
-dontwarn biweekly.io.json.**

#MSAL
-keep class com.microsoft.aad.adal.** {*;}
-keep class com.microsoft.identity.common.** {*;}
-dontwarn com.nimbusds.jose.**
-keepclassmembers enum * {*;} #GSON

#Bouncy castle
-keep class org.bouncycastle.** {*;}
-keepnames class org.bouncycastle.* {*;}
-dontwarn org.bouncycastle.cert.dane.**
-dontwarn org.bouncycastle.jce.provider.**
-dontwarn org.bouncycastle.x509.util.**

#AppAuth
-keep class net.openid.appauth.** {*;}
-keepnames class net.openid.appauth.* {*;}

#Notes
-dontnote com.google.android.material.**
-dontnote com.sun.mail.**
-dontnote javax.activation.**
-dontnote org.xbill.DNS.**
-dontnote me.leolin.shortcutbadger.**
-dontnote com.github.chrisbanes.photoview.**
-dontnote com.bugsnag.android.**
-dontnote biweekly.io.**

#SASL
-keep class com.sun.mail.imap.protocol.IMAPSaslAuthenticator {*;}
-keep class com.sun.mail.smtp.SMTPSaslAuthenticator {*;}

#Color picker
-keepnames class com.flask.colorpicker.** {*;}

