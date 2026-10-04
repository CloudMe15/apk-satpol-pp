package com.example.util

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
import androidx.core.content.FileProvider
import java.io.File

object ApkDownloadInstallManager {

    private const val TAG = "ApkDownloadManager"

    /**
     * Mengunduh file APK menggunakan Android DownloadManager dan otomatis
     * memicu Intent instalasi (PackageInstaller) saat proses unduh selesai.
     */
    fun startDownloadAndInstall(
        context: Context,
        downloadUrl: String,
        versionName: String = "terbaru"
    ) {
        val appContext = context.applicationContext

        // Cek izin instal aplikasi dari sumber tidak dikenal untuk Android 8.0 (Oreo) ke atas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!appContext.packageManager.canRequestPackageInstalls()) {
                Toast.makeText(
                    appContext,
                    "Harap izinkan instalasi aplikasi dari sumber ini untuk melanjutkan pembaruan.",
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
            val fileName = "SatpolPP-Siaga-v$versionName.apk"
            val destinationFile = File(
                appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )

            // Hapus file lama jika sudah ada agar tidak bentrok
            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                setTitle("Pembaruan Satpol PP Siaga v$versionName")
                setDescription("Mengunduh berkas pembaruan aplikasi resmi...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationUri(Uri.fromFile(destinationFile))
                setMimeType("application/vnd.android.package-archive")
            }

            val downloadManager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)

            Toast.makeText(
                appContext,
                "Mengunduh pembaruan APK Satpol PP... Silakan cek bilah notifikasi.",
                Toast.LENGTH_SHORT
            ).show()

            // Receiver untuk menangkap status saat unduhan selesai
            val onCompleteReceiver = object : BroadcastReceiver() {
                override fun onReceive(recvContext: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: -1L
                    if (id == downloadId) {
                        try {
                            appContext.unregisterReceiver(this)
                        } catch (_: Exception) {}

                        // Verifikasi status sukses di DownloadManager
                        val query = DownloadManager.Query().setFilterById(downloadId)
                        val cursor = downloadManager.query(query)
                        if (cursor != null && cursor.moveToFirst()) {
                            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                            if (statusIndex != -1 && cursor.getInt(statusIndex) == DownloadManager.STATUS_SUCCESSFUL) {
                                triggerInstall(appContext, destinationFile)
                            } else {
                                Toast.makeText(
                                    appContext,
                                    "Unduhan pembaruan gagal. Silakan coba kembali.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            cursor.close()
                        }
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                appContext.registerReceiver(
                    onCompleteReceiver,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                    Context.RECEIVER_EXPORTED
                )
            } else {
                appContext.registerReceiver(
                    onCompleteReceiver,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "Gagal memulai unduhan APK: ${e.message}", e)
            Toast.makeText(appContext, "Gagal mengunduh: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Memicu Intent instalasi APK menggunakan FileProvider
     */
    fun triggerInstall(context: Context, apkFile: File) {
        if (!apkFile.exists()) {
            Log.e(TAG, "File APK tidak ditemukan: ${apkFile.absolutePath}")
            return
        }

        try {
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
            Log.e(TAG, "Gagal memicu instalasi APK: ${e.message}", e)
            Toast.makeText(context, "Gagal membuka paket instalasi: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
