# ── MLKit ────────────────────────────────────────────────────────────
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_pose.** { *; }
-dontwarn com.google.mlkit.**

# ── CameraX ──────────────────────────────────────────────────────────
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# ── Firebase ─────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ── OkHttp / Okio ────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# ── Gson ─────────────────────────────────────────────────────────────
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**

# ── Glide ────────────────────────────────────────────────────────────
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-keep public class * extends com.bumptech.glide.GeneratedAppGlideModule

# ── PushGram models (Gson serialisation) ─────────────────────────────
-keep class com.pushgram.app.model.** { *; }
-keep class com.pushgram.app.progression.model.** { *; }
-keep class com.pushgram.app.rank.model.** { *; }
-keep class com.pushgram.app.battle.model.** { *; }

# ── Services/receivers registered in manifest ────────────────────────
-keep class com.pushgram.app.service.InstagramMonitorService { *; }
-keep class com.pushgram.app.service.BootReceiver { *; }
-keep class com.pushgram.app.music.service.MusicService { *; }

# ── View binding ─────────────────────────────────────────────────────
-keep class * implements androidx.viewbinding.ViewBinding {
    public static *** bind(android.view.View);
    public static *** inflate(...);
}
