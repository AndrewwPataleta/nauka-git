#
#-keepnames @toothpick.InjectConstructor class *
#-keepclasseswithmembernames class * {
#    @javax.inject.Inject <init>(...);
# }
#-keep class javax.inject.**
#-keep class javax.annotation.**
#-keep class **__Factory { *; }
#-keep class toothpick.** { *; }
#-keep @javax.inject.Qualifier @interface *
#
-dontwarn javax.inject.**
-dontwarn javax.annotation.**
-keep class javax.inject.**
-keep class javax.annotation.**
-keep class toothpick.** { *; }
-keepnames @javax.inject.Qualifier class *
-keepclassmembers enum * { *; }

-keep class **__Factory { *; }
-keep class **__MemberInjector { *; }
-keepclasseswithmembers class *{ @javax.inject.Inject <init>(...); }
-keepclasseswithmembers class *{ @javax.inject.Inject <init>(); }
-keepclasseswithmembers class *{ @javax.inject.Inject <fields>; }
-keepclasseswithmembers class *{ public <init>(...); }
-keepclassmembers class * {
 @javax.inject.Inject <init>(...);
 @javax.inject.Inject <init>();
 @javax.inject.Inject <fields>;
 public <init>(...);
}
-keepnames @toothpick.InjectConstructor class *

-keepclasseswithmembernames class * {
    toothpick.ktp.delegate.* *;
    @javax.inject.Inject <init>(...);
}
-keepclassmembers class * {
    toothpick.ktp.delegate.* *;
}

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
