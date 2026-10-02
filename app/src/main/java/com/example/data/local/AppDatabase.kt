package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ViolationReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ViolationReport::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun violationDao(): ViolationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "satpol_pp_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialData(getInstance(context).violationDao())
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateInitialData(dao: ViolationDao) {
            val now = System.currentTimeMillis()
            val sampleReports = listOf(
                ViolationReport(
                    ticketNumber = "STP-202610-001",
                    category = ViolationReport.CAT_PKL,
                    title = "PKL Membuka Lapak di Atas Trotoar Pejalan Kaki",
                    description = "Ditemukan deretan gerobak dan lapak jualan memakan badan trotoar dan sebagian bahu jalan sehingga pejalan kaki harus turun ke jalan raya yang ramai lalu lintas.",
                    urgency = ViolationReport.URGENCY_MEDIUM,
                    reporterName = "Fajar Ari Prakoso",
                    reporterPhone = "081234567890",
                    isAnonymous = false,
                    latitude = -0.3785,
                    longitude = 102.2982,
                    accuracyMeters = 8.5f,
                    address = "Jl. Sultan Ibrahim No. 45, Rengat, Indragiri Hulu",
                    landmark = "Depan Ruko Sentra Elektronik",
                    status = ViolationReport.STATUS_IN_PROGRESS,
                    officerNotes = "[Regu PRC 01] Petugas telah meluncur dengan Mobil Patroli Ranger menuju lokasi untuk penertiban simpatik.",
                    timestamp = now - 3600_000 * 1,
                    updatedAt = now - 1800_000,
                    assignedOfficerName = "Bripka Danu Prasetyo",
                    assignedOfficerPhone = "0812-7890-1122",
                    assignedOfficerSquad = "Unit Patroli Reaksi Cepat (UPRC)",
                    assignedOfficerVehicle = "Mobil Patroli Dalmas (BM 1002 IN)",
                    officerLatitude = -0.3755,
                    officerLongitude = 102.2965,
                    dispatchStatus = ViolationReport.DISPATCH_EN_ROUTE,
                    dispatchEtaMinutes = 4
                ),
                ViolationReport(
                    ticketNumber = "STP-202610-002",
                    category = ViolationReport.CAT_BANGUNAN,
                    title = "Pembangunan Kios Semi Permanen Tanpa Izin PBG",
                    description = "Ada aktivitas pendirian kios semi permanen di atas saluran drainase parit umum yang berpotensi menyumbat aliran air saat hujan deras.",
                    urgency = ViolationReport.URGENCY_HIGH,
                    reporterName = "Siti Rahmawati",
                    reporterPhone = "085298765432",
                    isAnonymous = true,
                    latitude = -0.3742,
                    longitude = 102.3021,
                    accuracyMeters = 6.2f,
                    address = "Jl. Narasinga Ujung RT 02/RW 04, Rengat",
                    landmark = "Samping Jembatan Sei Indragiri",
                    status = ViolationReport.STATUS_IN_PROGRESS,
                    officerNotes = "[Regu Patroli 2] Petugas telah berada di lokasi dan menyerahkan SP-1 kepada penanggung jawab bangunan.",
                    timestamp = now - 3600_000 * 5,
                    updatedAt = now - 3600_000 * 2,
                    assignedOfficerName = "Serma Agus Kurniawan",
                    assignedOfficerPhone = "0821-4455-6677",
                    assignedOfficerSquad = "Regu Penegakan Perda 2",
                    assignedOfficerVehicle = "Motor Trail Patroli Reaksi Cepat",
                    officerLatitude = -0.3742,
                    officerLongitude = 102.3021,
                    dispatchStatus = ViolationReport.DISPATCH_ARRIVED,
                    dispatchEtaMinutes = 0
                ),
                ViolationReport(
                    ticketNumber = "STP-202610-003",
                    category = ViolationReport.CAT_REKLAME,
                    title = "Baliho Komersial Tanpa Izin Dipaku pada Batang Pohon",
                    description = "Beberapa spanduk promosi produk dipaku langsung pada pohon lindung di median jalan protokol, merusak estetika dan kelestarian pohon peneduh.",
                    urgency = ViolationReport.URGENCY_LOW,
                    reporterName = "Budi Santoso",
                    reporterPhone = "082155667788",
                    isAnonymous = false,
                    latitude = -0.3811,
                    longitude = 102.2954,
                    accuracyMeters = 10.0f,
                    address = "Jl. H. Agus Salim, Kawasan Danau Raja, Rengat",
                    landmark = "Dekat Gerbang Utama Danau Raja",
                    status = ViolationReport.STATUS_RESOLVED,
                    officerNotes = "[Tim Patroli Danau Raja] Seluruh spanduk liar telah dibongkar dan diamankan ke Mako Satpol PP.",
                    timestamp = now - 3600_000 * 24,
                    updatedAt = now - 3600_000 * 10,
                    assignedOfficerName = "Brigadir Hendra Saputra",
                    assignedOfficerPhone = "0813-9988-1122",
                    assignedOfficerSquad = "Regu Wilayah Kota Rengat",
                    assignedOfficerVehicle = "Mobil Patroli Ranger",
                    officerLatitude = -0.3811,
                    officerLongitude = 102.2954,
                    dispatchStatus = ViolationReport.DISPATCH_COMPLETED,
                    dispatchEtaMinutes = 0
                ),
                ViolationReport(
                    ticketNumber = "STP-202610-004",
                    category = ViolationReport.CAT_KETERTIBAN,
                    title = "Suara Musik Keras & Kerumunan Lewat Tengah Malam",
                    description = "Terdapat kegiatan berkumpul dengan sound system portable di area fasilitas umum lewat jam 01.00 WIB dini hari mengganggu istirahat warga sekitar.",
                    urgency = ViolationReport.URGENCY_HIGH,
                    reporterName = "Fajar Ari Prakoso",
                    reporterPhone = "081234567890",
                    isAnonymous = false,
                    latitude = -0.3770,
                    longitude = 102.2995,
                    accuracyMeters = 5.0f,
                    address = "Jl. Veteran No. 18, Rengat, Indragiri Hulu",
                    landmark = "Dekat Taman Kota Rengat",
                    status = ViolationReport.STATUS_PENDING,
                    officerNotes = null,
                    timestamp = now - 1800_000,
                    updatedAt = now - 1800_000,
                    assignedOfficerName = null,
                    assignedOfficerPhone = null,
                    assignedOfficerSquad = null,
                    assignedOfficerVehicle = null,
                    officerLatitude = null,
                    officerLongitude = null,
                    dispatchStatus = ViolationReport.DISPATCH_PENDING,
                    dispatchEtaMinutes = 0
                )
            )
            dao.insertReports(sampleReports)
        }
    }
}
