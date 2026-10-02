package com.example.util

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Locale

sealed class FileValidationResult {
    data class Success(val safeFileUri: Uri, val fileName: String, val mimeType: String) : FileValidationResult()
    data class Error(val message: String) : FileValidationResult()
}

object FileSecurityValidator {

    private val ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp")
    private val ALLOWED_MIME_TYPES = setOf("image/jpeg", "image/png", "image/webp")

    // Magic Numbers (Signatures)
    // JPEG: FF D8 FF
    // PNG: 89 50 4E 47 0D 0A 1A 0A
    // WEBP: 52 49 46 46 (RIFF) ... 57 45 42 50 (WEBP)
    private val JPEG_MAGIC = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
    private val PNG_MAGIC = byteArrayOf(
        0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte(),
        0x0D.toByte(), 0x0A.toByte(), 0x1A.toByte(), 0x0A.toByte()
    )
    private val RIFF_HEADER = byteArrayOf(0x52.toByte(), 0x49.toByte(), 0x46.toByte(), 0x46.toByte())
    private val WEBP_MAGIC = byteArrayOf(0x57.toByte(), 0x45.toByte(), 0x42.toByte(), 0x50.toByte())

    private val DANGEROUS_EXTENSIONS = setOf(
        "php", "php3", "php4", "php5", "phtml", "sh", "bash", "exe", "bat", "cmd",
        "js", "jsp", "asp", "aspx", "cgi", "py", "pl", "rb", "jar", "bin", "html", "htm"
    )

    fun validateAndProcessImage(context: Context, uri: Uri): FileValidationResult {
        val contentResolver = context.contentResolver

        // 1. Dapatkan nama file asli
        val originalFileName = getFileName(context, uri) ?: "upload.jpg"

        // 2. Validasi Ekstensi & Deteksi Ekstensi Ganda (Contoh: foto.php.jpg)
        val lowerFileName = originalFileName.lowercase(Locale.ROOT)
        val nameSegments = lowerFileName.split(".")

        if (nameSegments.size < 2) {
            return FileValidationResult.Error(
                "File ditolak: Nama file tidak memiliki ekstensi yang valid."
            )
        }

        // Cek ekstensi ganda: jika terdapat lebih dari satu titik, periksa apakah ada ekstensi berbahaya di tengah
        if (nameSegments.size > 2) {
            for (i in 1 until nameSegments.size - 1) {
                val middleExt = nameSegments[i].trim()
                if (DANGEROUS_EXTENSIONS.contains(middleExt) || ALLOWED_EXTENSIONS.contains(middleExt)) {
                    return FileValidationResult.Error(
                        "File ditolak: Terdeteksi ekstensi ganda yang mencurigakan ('.$middleExt.${nameSegments.last()}'). Sistem menolak file berpotensi berbahaya."
                    )
                }
            }
        }

        val finalExt = nameSegments.last().trim()
        if (!ALLOWED_EXTENSIONS.contains(finalExt)) {
            return FileValidationResult.Error(
                "File ditolak: Ekstensi '.$finalExt' tidak diizinkan. Hanya format .jpg, .jpeg, .png, dan .webp yang diperbolehkan."
            )
        }

        // 3. Validasi ContentResolver MIME Type
        val declaredMime = contentResolver.getType(uri)?.lowercase(Locale.ROOT)
        if (declaredMime != null && !ALLOWED_MIME_TYPES.contains(declaredMime)) {
            return FileValidationResult.Error(
                "File ditolak: Tipe MIME '$declaredMime' tidak diizinkan. Wajib image/jpeg, image/png, atau image/webp."
            )
        }

        // 4. Validasi Magic Numbers (Byte Awal File)
        var inputStream: InputStream? = null
        val headerBytes = ByteArray(16)
        val bytesRead: Int
        try {
            inputStream = contentResolver.openInputStream(uri)
            if (inputStream == null) {
                return FileValidationResult.Error("Gagal membuka file untuk pemeriksaan keamanan.")
            }
            bytesRead = inputStream.read(headerBytes, 0, headerBytes.size)
        } catch (e: Exception) {
            return FileValidationResult.Error("Kesalahan membaca isi file: ${e.message}")
        } finally {
            try { inputStream?.close() } catch (_: Exception) {}
        }

        if (bytesRead < 8) {
            return FileValidationResult.Error("File ditolak: Ukuran file terlalu kecil atau rusak.")
        }

        val isJpeg = matchesPrefix(headerBytes, JPEG_MAGIC)
        val isPng = matchesPrefix(headerBytes, PNG_MAGIC)
        val isWebp = matchesPrefix(headerBytes, RIFF_HEADER) && matchesSubsequence(headerBytes, 8, WEBP_MAGIC)

        if (!isJpeg && !isPng && !isWebp) {
            return FileValidationResult.Error(
                "File ditolak: Magic numbers (tanda tangan byte) tidak cocok dengan file gambar asli. File terdeteksi sebagai skrip atau teks yang disamarkan."
            )
        }

        // 5. Validasi integritas Bitmap decoding
        try {
            contentResolver.openInputStream(uri)?.use { stream ->
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(stream, null, options)
                if (options.outWidth <= 0 || options.outHeight <= 0) {
                    return FileValidationResult.Error(
                        "File ditolak: Data gambar korup atau bukan grafis yang dapat dirender."
                    )
                }
            }
        } catch (e: Exception) {
            return FileValidationResult.Error("Gagal memvalidasi header gambar: ${e.message}")
        }

        // 6. Simpan secara aman ke internal storage dengan nama acak untuk mencegah path-traversal
        val safeExt = when {
            isPng -> "png"
            isWebp -> "webp"
            else -> "jpg"
        }
        val safeFileName = "bukti_${System.currentTimeMillis()}_${(1000..9999).random()}.$safeExt"
        val storageDir = File(context.filesDir, "report_photos").apply { if (!exists()) mkdirs() }
        val targetFile = File(storageDir, safeFileName)

        try {
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            return FileValidationResult.Error("Gagal mengamankan file ke penyimpanan internal: ${e.message}")
        }

        val safeUri = Uri.fromFile(targetFile)
        val resolvedMime = when {
            isPng -> "image/png"
            isWebp -> "image/webp"
            else -> "image/jpeg"
        }

        return FileValidationResult.Success(
            safeFileUri = safeUri,
            fileName = safeFileName,
            mimeType = resolvedMime
        )
    }

    private fun matchesPrefix(data: ByteArray, prefix: ByteArray): Boolean {
        if (data.size < prefix.size) return false
        for (i in prefix.indices) {
            if (data[i] != prefix[i]) return false
        }
        return true
    }

    private fun matchesSubsequence(data: ByteArray, offset: Int, expected: ByteArray): Boolean {
        if (data.size < offset + expected.size) return false
        for (i in expected.indices) {
            if (data[offset + i] != expected[i]) return false
        }
        return true
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        return cursor.getString(nameIndex)
                    }
                }
            } catch (_: Exception) {}
        }
        return uri.path?.let { File(it).name }
    }
}
