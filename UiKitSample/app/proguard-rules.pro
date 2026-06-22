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

# --- OneStep SDK / UIKit ---
# No app-side keep rules are needed: the OneStep core and uikit AARs ship their own *consumer*
# ProGuard rules, which R8 applies automatically. Avoid blanket
# `-keep class co.onestep.android.** { *; }` rules — they disable R8 shrinking/optimization for the
# whole SDK and bloat your APK. If a release build fails, add the narrowest keep for the specific
# class R8 reports, not a package wildcard.
#
# kotlin-parcelize's @Parcelize is a compile-time annotation referenced by the UIKit AAR but absent
# at runtime; R8 full mode escalates that missing-class warning to an error. This is the one narrow
# rule R8 asks for (see build/outputs/mapping/release/missing_rules.txt) — not a keep.
-dontwarn kotlinx.parcelize.**