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

# --- OneStep SDK ---
# No app-side keep rules are needed: the OneStep SDK AAR ships its own *consumer* ProGuard rules,
# which R8 applies automatically. Avoid a blanket `-keep class co.onestep.android.**` — it disables
# R8 shrinking/optimization for the whole SDK. If a release build fails, add the narrowest keep for
# the specific class R8 reports, not a package wildcard.