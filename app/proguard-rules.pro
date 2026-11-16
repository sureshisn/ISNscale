# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   https://developer.android.com/build/shrink-code

# Keep names of measurement-related classes (useful for debugging)
-keep class com.example.isnscale.** { *; }

# Keep CameraX classes
-keep class androidx.camera.** { *; }

# Keep ML Kit classes
-keep class com.google.mlkit.** { *; }
-keep class org.tensorflow.** { *; }

# Keep view binding classes
-keep class * extends androidx.viewbinding.ViewBinding { *; }

# If your app uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class as well as the JavaScript interface name.
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment all the lines below if your app uses WebView with JavaScript
# and you want to keep JavaScript interface classes from being
# obfuscated by the ProGuard optimizer.
#-keepclassmembers class * {
#   @android.webkit.JavascriptInterface <methods>;
#}

# If your app uses native libraries, uncomment the following
# and specify the fully qualified class names to the native methods
# from your native code.
#-keepclasseswithmembers class * {
#    native <methods>;
#}

# If you keep the line below, Google Play services won't be able to help you
# with crash reports that only contain obfuscated classes, line numbers, and thread IDs.
#-dontobfuscate

# If you keep the line below, it prevents the static field numbers from being
# optimized away, so the crash reports will contain the following lines instead
# of the modified ones.
#-dontoptimize