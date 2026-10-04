package com.example.data.remote

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.BuildConfig
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File

/**
 * Model Data Laporan untuk Firebase Realtime Database
 */
@IgnoreExtraProperties
data class LaporanItem(
    var id: String = "",
    var judul: String = "",
    var deskripsi: String = "",
    var lokasi: String = "",
    var status: String = "MENUNGGU",
    var timestamp: Long = System.currentTimeMillis()
)

/**
 * ============================================================================
 * FITUR 1: SINKRONISASI LAPORAN REAL-TIME DENGAN FIREBASE
 * ============================================================================
 */
object FirebaseLaporanRepository {

    private const val TAG = "FirebaseLaporanRepo"

    // Referensi ke node 'laporan' di Firebase Realtime Database
    private val databaseRef: DatabaseReference by lazy {
        try {
            FirebaseDatabase.getInstance("https://apk-satpol-pp-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("laporan")
        } catch (_: Exception) {
            FirebaseDatabase.getInstance().getReference("laporan")
        }
    }

    /**
     * Mengirim data laporan baru ke node 'laporan'
     */
    fun kirimLaporan(
        judul: String,
        deskripsi: String,
        lokasi: String,
        onSuccess: (String) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val newKey = databaseRef.push().key ?: System.currentTimeMillis().toString()
        val dataLaporan = LaporanItem(
            id = newKey,
            judul = judul,
            deskripsi = deskripsi,
            lokasi = lokasi,
            status = "MENUNGGU",
            timestamp = System.currentTimeMillis()
        )

        databaseRef.child(newKey).setValue(dataLaporan)
            .addOnSuccessListener {
                Log.d(TAG, "Laporan berhasil dikirim dengan ID: $newKey")
                onSuccess(newKey)
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Gagal mengirim laporan: ${error.message}", error)
                onError(error)
            }
    }

    /**
     * Membaca node 'laporan' secara real-time menggunakan Kotlin Flow
     */
    fun getLaporanFlow(): Flow<List<LaporanItem>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<LaporanItem>()
                for (child in snapshot.children) {
                    val item = child.getValue(LaporanItem::class.java)
                    if (item != null) {
                        list.add(item)
                    }
                }
                // Urutkan laporan terbaru di paling atas
                trySend(list.sortedByDescending { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        // Daftarkan listener real-time
        databaseRef.addValueEventListener(listener)

        // Bersihkan listener saat Flow dibatalkan/ditutup (mencegah kebocoran memori)
        awaitClose {
            databaseRef.removeEventListener(listener)
        }
    }
}

/**
 * Tampilan Jetpack Compose untuk Menampilkan Daftar Laporan Real-Time (LazyColumn)
 */
@Composable
fun DaftarLaporanRealtimeScreen(
    modifier: Modifier = Modifier,
    laporanList: List<LaporanItem>
) {
    if (laporanList.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Belum ada laporan di database.",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(laporanList, key = { it.id }) { item ->
                LaporanCardItem(item = item)
            }
        }
    }
}

@Composable
fun LaporanCardItem(item: LaporanItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.judul,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (item.status) {
                        "SELESAI" -> Color(0xFF16A34A)
                        "PROSES" -> Color(0xFFD97706)
                        else -> Color(0xFFDC2626)
                    }
                ) {
                    Text(
                        text = item.status,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.deskripsi,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📍 ${item.lokasi}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * ============================================================================
 * FITUR 2: IN-APP AUTO UPDATE (PEMBARUAN APK MANDIRI)
 * ============================================================================
 */
object AppUpdateHelper {

    private const val TAG = "AppUpdateHelper"

    /**
     * Memeriksa versi APK di node: app_config
     */
    fun checkForUpdate(
        onUpdateAvailable: (
            latestVersionCode: Int,
            latestVersionName: String,
            downloadUrl: String,
            releaseNotes: String,
            forceUpdate: Boolean
        ) -> Unit
    ) {
        val configRef = try {
            FirebaseDatabase.getInstance("https://apk-satpol-pp-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("app_config")
        } catch (_: Exception) {
            FirebaseDatabase.getInstance().getReference("app_config")
        }
        configRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val latestCode = snapshot.child("latest_version_code").getValue(Long::class.java)?.toInt() ?: 0
                val latestName = snapshot.child("latest_version_name").getValue(String::class.java) ?: "Terbaru"
                val downloadUrl = snapshot.child("download_url").getValue(String::class.java) ?: ""
                val releaseNotes = snapshot.child("release_notes").getValue(String::class.java) ?: "Peningkatan performa dan stabilitas aplikasi."
                val forceUpdate = snapshot.child("force_update").getValue(Boolean::class.java) ?: true

                // Bandingkan dengan versionCode aplikasi saat ini
                val currentVersionCode = BuildConfig.VERSION_CODE
                if (latestCode > currentVersionCode && downloadUrl.isNotBlank()) {
                    Log.i(TAG, "Pembaruan ditemukan! Versi Cloud: $latestCode > Lokal: $currentVersionCode (Force: $forceUpdate)")
                    onUpdateAvailable(latestCode, latestName, downloadUrl, releaseNotes, forceUpdate)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Gagal mengecek update: ${error.message}")
            }
        })
    }

    /**
     * Mengunduh APK menggunakan DownloadManager & otomatis memicu instalasi
     */
    fun startDownloadAndInstallApk(
        context: Context,
        downloadUrl: String,
        versionName: String = "terbaru"
    ) {
        val appContext = context.applicationContext

        // Cek izin instal aplikasi dari sumber tidak dikenal (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!appContext.packageManager.canRequestPackageInstalls()) {
                Toast.makeText(
                    appContext,
                    "Harap izinkan instalasi dari sumber ini untuk melanjutkan pembaruan.",
                    Toast.LENGTH_LONG
                ).show()

                val permissionIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${appContext.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(permissionIntent)
                return
            }
        }

        try {
            val fileName = "SatpolPP-v$versionName.apk"
            val destinationFile = File(
                appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )

            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                setTitle("Pembaruan Satpol PP v$versionName")
                setDescription("Mengunduh berkas APK versi terbaru...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationUri(Uri.fromFile(destinationFile))
                setMimeType("application/vnd.android.package-archive")
            }

            val downloadManager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)

            Toast.makeText(appContext, "Mengunduh pembaruan APK di latar belakang...", Toast.LENGTH_SHORT).show()

            val onCompleteReceiver = object : BroadcastReceiver() {
                override fun onReceive(recvContext: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: -1L
                    if (id == downloadId) {
                        try {
                            appContext.unregisterReceiver(this)
                        } catch (_: Exception) {}

                        val query = DownloadManager.Query().setFilterById(downloadId)
                        val cursor = downloadManager.query(query)
                        if (cursor != null && cursor.moveToFirst()) {
                            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                            if (statusIndex != -1 && cursor.getInt(statusIndex) == DownloadManager.STATUS_SUCCESSFUL) {
                                triggerInstall(appContext, destinationFile)
                            }
                            cursor.close()
                        }
                    }
                }
            }

            val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                appContext.registerReceiver(onCompleteReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                appContext.registerReceiver(onCompleteReceiver, filter)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Gagal mengunduh APK: ${e.message}", e)
            Toast.makeText(appContext, "Gagal mengunduh: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Menimpa APK lama dengan APK baru via FileProvider
     */
    fun triggerInstall(context: Context, apkFile: File) {
        if (!apkFile.exists()) return

        try {
            // Menggunakan FileProvider untuk keamanan Android 7.0+
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(installIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Gagal membuka installer APK: ${e.message}", e)
            Toast.makeText(context, "Gagal memasang APK: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

/**
 * AlertDialog Jetpack Compose yang Menampilkan Pembaruan APK (Mendukung Catatan Rilis & Wajib/Opsional)
 */
@Composable
fun MandatoryUpdateDialog(
    latestVersionName: String,
    downloadUrl: String,
    releaseNotes: String = "Peningkatan sistem dan stabilitas aplikasi.",
    forceUpdate: Boolean = true,
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = {
            if (!forceUpdate) {
                onDismiss()
            }
        },
        icon = {
            Icon(
                imageVector = Icons.Default.SystemUpdate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        },
        title = {
            Text(
                text = if (forceUpdate) "Pembaruan Wajib (v$latestVersionName)" else "Pembaruan Tersedia (v$latestVersionName)",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (forceUpdate)
                        "Versi aplikasi Anda sudah usang. Mohon perbarui ke versi terbaru untuk tetap dapat menggunakan aplikasi Satpol PP."
                    else
                        "Tersedia versi baru aplikasi Satpol PP. Anda dapat memperbarui sekarang untuk mendapatkan fitur terbaru.",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Kotak Catatan Rilis (Release Notes)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "📋 Catatan Pembaruan:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = releaseNotes.ifBlank { "Peningkatan keamanan dan sinkronisasi real-time." },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Aplikasi akan otomatis mengunduh berkas APK dan memandu pemasangan.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    AppUpdateHelper.startDownloadAndInstallApk(
                        context = context,
                        downloadUrl = downloadUrl,
                        versionName = latestVersionName
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Update Sekarang", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = if (!forceUpdate) {
            {
                TextButton(onClick = onDismiss) {
                    Text("Nanti Saja")
                }
            }
        } else null
    )
}
