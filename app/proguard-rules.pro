# === Retrofit ===
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# === Gson ===
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken
-keep class com.dongmedicine.data.model.** { *; }
-keep class com.dongmedicine.data.api.ApiService$ApiResponse { *; }

# === Room ===
-keep class * extends androidx.room.RoomDatabase
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static ** INSTANCE;
}
-keep class com.dongmedicine.data.local.** { *; }

# === Hilt ===
-keep class com.dongmedicine.DongmedicineApplication { *; }

# === Glide ===
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder { *** rewind(); }
