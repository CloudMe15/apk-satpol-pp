# ==============================================================================
# PROGUARD / R8 SECURITY HARDENING - SATPOL PP SIAGA
# Melindungi kode dari dekompilasi (JADX, APKTool, Bytecode Viewer)
# ==============================================================================

# 1. Hapus semua log debug sensitif pada rilis produksi agar tidak bocor di logcat
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}

# 2. Samarkan nama berkas sumber & nomor baris
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# 3. Obfuskasi agresif nama kelas dan paket
-repackageclasses 'com.aistudio.satpolpp.repkzy.core'
-allowaccessmodification

# 4. Pertahankan model data Room Database & JSON Serialization agar tidak rusak
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# Moshi / Reflection Protection
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
    @com.squareup.moshi.JsonQualifier <fields>;
}

# 5. Keamanan Model Laporan & Auth Satpol PP
-keepclassmembers class com.example.data.model.** { *; }
-keepclassmembers class com.example.data.auth.** { *; }

