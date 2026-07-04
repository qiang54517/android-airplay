# Netty
-keepattributes Signature,InnerClasses,*Annotation*
-keep class io.netty.** { *; }
-dontwarn io.netty.**
-dontwarn sun.misc.**
-dontwarn javax.naming.**
-dontwarn org.slf4j.**

# dd-plist
-keep class com.dd.plist.** { *; }

# Keep AirPlay protocol classes
-keep class com.example.airplay.protocol.** { *; }
