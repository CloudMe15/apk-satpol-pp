package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "violation_reports")
data class ViolationReport(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ticketNumber: String,
    val category: String,
    val title: String,
    val description: String,
    val urgency: String, // TINGGI, SEDANG, RENDAH
    val reporterName: String,
    val reporterPhone: String,
    val isAnonymous: Boolean = false,
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float = 0f,
    val address: String,
    val landmark: String = "",
    val photoUri: String? = null,
    val status: String = STATUS_PENDING, // MENUNGGU_VERIFIKASI, SEDANG_DITANGANI, SELESAI, DITOLAK
    val officerNotes: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    
    // Live Officer Dispatch & Gojek-style Tracking Fields
    val assignedOfficerName: String? = null,
    val assignedOfficerPhone: String? = null,
    val assignedOfficerSquad: String? = null,
    val assignedOfficerVehicle: String? = null,
    val officerLatitude: Double? = null,
    val officerLongitude: Double? = null,
    val dispatchStatus: String = DISPATCH_PENDING,
    val dispatchEtaMinutes: Int = 0
) {
    companion object {
        const val STATUS_PENDING = "MENUNGGU_VERIFIKASI"
        const val STATUS_IN_PROGRESS = "SEDANG_DITANGANI"
        const val STATUS_RESOLVED = "SELESAI"
        const val STATUS_REJECTED = "DITOLAK"

        const val DISPATCH_PENDING = "MENUNGGU_PETUGAS"
        const val DISPATCH_ACCEPTED = "DITERIMA_PETUGAS"
        const val DISPATCH_EN_ROUTE = "DALAM_PERJALANAN"
        const val DISPATCH_ARRIVED = "TIBA_DI_LOKASI"
        const val DISPATCH_COMPLETED = "SELESAI_PENINDAKAN"

        const val URGENCY_HIGH = "TINGGI"
        const val URGENCY_MEDIUM = "SEDANG"
        const val URGENCY_LOW = "RENDAH"

        const val CAT_PKL = "PKL Liar & Bahu Jalan"
        const val CAT_BANGUNAN = "Bangunan Liar / Tanpa Izin"
        const val CAT_KETERTIBAN = "Gangguan Ketertiban Umum"
        const val CAT_REKLAME = "Reklame & Spanduk Ilegal"
        const val CAT_SAMPAH = "Sampah & Limbah Sembarangan"
        const val CAT_MIRAS_JUDI = "Penyakit Masyarakat / Miras"
        const val CAT_PELAJAR = "Pelajar Bolos / Tawuran"
        const val CAT_JAM_OPERASIONAL = "Pelanggaran Jam Usaha / Hiburan"
        const val CAT_LAINNYA = "Pelanggaran Trantibum Lainnya"

        val ALL_CATEGORIES = listOf(
            CAT_PKL,
            CAT_BANGUNAN,
            CAT_KETERTIBAN,
            CAT_REKLAME,
            CAT_SAMPAH,
            CAT_MIRAS_JUDI,
            CAT_PELAJAR,
            CAT_JAM_OPERASIONAL,
            CAT_LAINNYA
        )
    }
}
