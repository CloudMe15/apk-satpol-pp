package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.ViolationDao
import com.example.data.model.ViolationReport
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ViolationRepository(private val dao: ViolationDao) {

    val allReports: Flow<List<ViolationReport>> = dao.getAllReports()

    fun getReportById(id: Long): Flow<ViolationReport?> = dao.getReportById(id)

    fun getReportsByStatus(status: String): Flow<List<ViolationReport>> = dao.getReportsByStatus(status)

    suspend fun getReportCount(): Int = dao.getReportCount()

    suspend fun submitReport(report: ViolationReport): Long {
        return dao.insertReport(report)
    }

    suspend fun insertReports(reports: List<ViolationReport>) {
        dao.insertReports(reports)
    }

    suspend fun updateReport(report: ViolationReport) {
        dao.updateReport(report)
    }

    suspend fun updateStatus(id: Long, status: String, officerNotes: String?) {
        dao.updateStatus(id, status, officerNotes)
    }

    suspend fun acceptDispatch(
        report: ViolationReport,
        officerName: String = "Bripka Danu Prasetyo",
        officerPhone: String = "0812-7890-1122",
        squad: String = "Unit Patroli Reaksi Cepat (UPRC)",
        vehicle: String = "Mobil Patroli Dalmas (BM 1002 IN)"
    ) {
        // Officer starts roughly 1-2 km away from report coordinates
        val officerLat = report.latitude + 0.0065
        val officerLng = report.longitude - 0.0055
        dao.updateDispatch(
            id = report.id,
            officerName = officerName,
            officerPhone = officerPhone,
            squad = squad,
            vehicle = vehicle,
            officerLat = officerLat,
            officerLng = officerLng,
            dispatchStatus = ViolationReport.DISPATCH_ACCEPTED,
            etaMinutes = 7,
            reportStatus = ViolationReport.STATUS_IN_PROGRESS,
            officerNotes = "[$officerName] Laporan diterima. Petugas bersiap bergerak menuju TKP."
        )
    }

    suspend fun advanceDispatch(report: ViolationReport) {
        when (report.dispatchStatus) {
            ViolationReport.DISPATCH_PENDING -> {
                acceptDispatch(report)
            }
            ViolationReport.DISPATCH_ACCEPTED -> {
                // Officer is en route - closer coordinates
                val newLat = (report.officerLatitude ?: (report.latitude + 0.0040)) * 0.5 + report.latitude * 0.5
                val newLng = (report.officerLongitude ?: (report.longitude - 0.0030)) * 0.5 + report.longitude * 0.5
                dao.updateDispatch(
                    id = report.id,
                    officerName = report.assignedOfficerName ?: "Bripka Danu Prasetyo",
                    officerPhone = report.assignedOfficerPhone ?: "0812-7890-1122",
                    squad = report.assignedOfficerSquad ?: "Unit Patroli Reaksi Cepat (UPRC)",
                    vehicle = report.assignedOfficerVehicle ?: "Mobil Patroli Dalmas (BM 1002 IN)",
                    officerLat = newLat,
                    officerLng = newLng,
                    dispatchStatus = ViolationReport.DISPATCH_EN_ROUTE,
                    etaMinutes = 3,
                    reportStatus = ViolationReport.STATUS_IN_PROGRESS,
                    officerNotes = "[${report.assignedOfficerName ?: "Petugas"}] Petugas sedang dalam perjalanan meluncur ke lokasi kejadian."
                )
            }
            ViolationReport.DISPATCH_EN_ROUTE -> {
                // Officer arrived at the scene!
                dao.updateDispatch(
                    id = report.id,
                    officerName = report.assignedOfficerName,
                    officerPhone = report.assignedOfficerPhone,
                    squad = report.assignedOfficerSquad,
                    vehicle = report.assignedOfficerVehicle,
                    officerLat = report.latitude,
                    officerLng = report.longitude,
                    dispatchStatus = ViolationReport.DISPATCH_ARRIVED,
                    etaMinutes = 0,
                    reportStatus = ViolationReport.STATUS_IN_PROGRESS,
                    officerNotes = "[${report.assignedOfficerName ?: "Petugas"}] Petugas telah tiba di lokasi. Sedang melakukan penertiban dan pembinaan."
                )
            }
            ViolationReport.DISPATCH_ARRIVED -> {
                // Completed!
                dao.updateDispatch(
                    id = report.id,
                    officerName = report.assignedOfficerName,
                    officerPhone = report.assignedOfficerPhone,
                    squad = report.assignedOfficerSquad,
                    vehicle = report.assignedOfficerVehicle,
                    officerLat = report.latitude,
                    officerLng = report.longitude,
                    dispatchStatus = ViolationReport.DISPATCH_COMPLETED,
                    etaMinutes = 0,
                    reportStatus = ViolationReport.STATUS_RESOLVED,
                    officerNotes = "[${report.assignedOfficerName ?: "Petugas"}] Penertiban selesai dengan tertib dan humanis."
                )
            }
        }
    }

    suspend fun deleteReport(id: Long) {
        dao.deleteReport(id)
    }

    fun generateTicketNumber(): String {
        val randomSuffix = UUID.randomUUID().toString().substring(0, 4).uppercase()
        val datePrefix = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date())
        return "STP-$datePrefix-$randomSuffix"
    }

    suspend fun seedSampleReportsIfEmpty() {
        if (dao.getReportCount() == 0) {
            AppDatabase.populateInitialData(dao)
        }
    }
}
