# Add project specific ProGuard rules here.

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep Retrofit and Gson classes safe
-keepattributes Signature
-keepattributes *Annotation*

-keep class com.example.gymfitness.data.remote.dto.** { *; }
-keep class com.example.gymfitness.domain.models.** { *; }

-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}