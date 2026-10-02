package com.example.data.remote

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER,
    BOT
}

object GeminiChatService {

    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val SYSTEM_PROMPT = """
Anda adalah "Praja Bot", Asisten Virtual Resmi Satuan Polisi Pamong Praja (Satpol PP) Kabupaten Indragiri Hulu - Semboyan "Praja Wibawa".
Tugas Anda adalah melayani masyarakat dan petugas dengan ramah, lugas, tegas, dan edukatif mengenai:
1. Peraturan Daerah (Perda) Ketenteraman dan Ketertiban Umum (Trantibum).
2. Tata cara dan panduan pelaporan pelanggaran real-time berbasis GPS (PKL bahu jalan, reklame liar, bangunan tanpa izin, miras, sampah liar, tawuran pelajar).
3. Jaminan perlindungan dan kerahasiaan identitas pelapor (anonimitas terjamin).
4. Layanan darurat Satpol PP Inhu dan Call Center 112.
5. Penjelasan alur penertiban yang humanis dan persuasif sesuai SOP Satpol PP.

Jawablah dengan Bahasa Indonesia yang jelas, profesional, dan solutif.
"""

    suspend fun sendMessage(
        conversationHistory: List<ChatMessage>,
        userMessage: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Provide intelligent fallback for prototype if API key is unconfigured
            return@withContext Result.success(generateLocalFallbackResponse(userMessage))
        }

        try {
            val rootJson = JSONObject()

            // System Instruction
            val systemInstructionJson = JSONObject()
            val systemParts = JSONArray().apply {
                put(JSONObject().put("text", SYSTEM_PROMPT))
            }
            systemInstructionJson.put("parts", systemParts)
            rootJson.put("systemInstruction", systemInstructionJson)

            // Contents array
            val contentsArray = JSONArray()

            // Include last 6 turns for context
            val recentTurns = conversationHistory.takeLast(6)
            for (msg in recentTurns) {
                val role = if (msg.sender == MessageSender.USER) "user" else "model"
                val turnJson = JSONObject()
                turnJson.put("role", role)
                val parts = JSONArray().apply {
                    put(JSONObject().put("text", msg.text))
                }
                turnJson.put("parts", parts)
                contentsArray.put(turnJson)
            }

            // Current prompt
            val currentTurnJson = JSONObject()
            currentTurnJson.put("role", "user")
            val currentParts = JSONArray().apply {
                put(JSONObject().put("text", userMessage))
            }
            currentTurnJson.put("parts", currentParts)
            contentsArray.put(currentTurnJson)

            rootJson.put("contents", contentsArray)

            // Generation config
            val genConfig = JSONObject()
            genConfig.put("temperature", 0.7)
            rootJson.put("generationConfig", genConfig)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = rootJson.toString().toRequestBody(mediaType)

            val url = "$BASE_URL?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody == null) {
                return@withContext Result.success(generateLocalFallbackResponse(userMessage))
            }

            val respJson = JSONObject(responseBody)
            val candidates = respJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                Result.success(generateLocalFallbackResponse(userMessage))
            }
        } catch (e: Exception) {
            Result.success(generateLocalFallbackResponse(userMessage))
        }
    }

    private fun generateLocalFallbackResponse(prompt: String): String {
        val p = prompt.lowercase()
        return when {
            p.contains("lapor") || p.contains("cara") -> {
                "Untuk membuat laporan pelanggaran Trantibum dengan GPS:\n" +
                        "1. Buka tab 'Lapor GPS' di menu bawah.\n" +
                        "2. Pastikan GPS ponsel Anda aktif agar koordinat otomatis terkunci.\n" +
                        "3. Pilih kategori pelanggaran (misalnya PKL Liar, Reklame Ilegal, dsb).\n" +
                        "4. Lampirkan foto bukti (file .jpg/.png tervalidasi).\n" +
                        "5. Anda dapat mengaktifkan opsi 'Laporkan Sebagai Anonim' untuk menjaga kerahasiaan identitas Anda.\n" +
                        "6. Tekan tombol Kirim. Anda akan mendapatkan Nomor Tiket Resmi untuk pemantauan."
            }
            p.contains("darurat") || p.contains("nomor") || p.contains("kontak") || p.contains("call center") -> {
                "🚨 Kontak Penting Satpol PP Indragiri Hulu:\n" +
                        "• Call Center Darurat: 112 (Bebas Pulsa 24 Jam)\n" +
                        "• Posko Reaksi Cepat: +62 813-7443-5636\n" +
                        "• Piket Pengawasan: +62 852-7268-3165\n" +
                        "• Mako Satpol PP: Jl. Lintas Timur, Rengat, Kab. Indragiri Hulu."
            }
            p.contains("pkl") || p.contains("pedagang") -> {
                "Sesuai Perda Ketertiban Umum, Pedagang Kaki Lima (PKL) dilarang menggelar lapak di badan trotoar pejalan kaki, taman jalur hijau, dan median jalan. Penertiban dilakukan melalui teguran simpatik terlebih dahulu, pembinaan, hingga penataan relokasi ke pasar rakyat resmi."
            }
            p.contains("reklame") || p.contains("spanduk") -> {
                "Pemasangan reklame, banner komersial, maupun spanduk wajib mengantongi stempel izin resmi dan membayar pajak reklame. Pemasangan yang dipaku di batang pohon lindung atau tiang rambu lalu lintas dilarang keras dan akan langsung dicopot oleh Satgas Satpol PP."
            }
            p.contains("anonim") || p.contains("rahasia") || p.contains("aman") -> {
                "Keamanan dan kerahasiaan Anda dijamin 100%! Sistem Satpol PP Siaga menyediakan fitur pelaporan Anonim. Nama dan kontak Anda tidak akan dipublikasikan kepada siapapun sesuai SOP perlindungan pelapor (Whistleblower Protection)."
            }
            else -> {
                "Halo! Saya Praja Bot, Asisten Satpol PP. Saya siap membantu Anda seputar informasi Perda Ketertiban Umum (Trantibum), alur penindakan patroli, panduan pelaporan GPS, hak perlindungan pelapor, dan kontak darurat Satpol PP. Ada yang bisa saya bantu hari ini?"
            }
        }
    }
}
