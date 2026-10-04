package com.example.data.repository

import android.util.Log
import com.example.data.auth.AuthManager
import com.example.data.local.AppDatabase
import com.example.data.local.ViolationDao
import com.example.data.model.ViolationReport
import com.example.data.remote.RealtimeCloudSyncService
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

class ViolationRepository(private val dao: ViolationDao) {

    val allReports: Flow<List<ViolationReport>> = dao.getAllReports()

    fun getReportById(id: Long): Flow<ViolationReport?> = dao.getReportById(id)

    fun getReportsByStatus(status: String): Flow<List<ViolationReport>> = dao.getReportsByStatus(status)

    suspend fun getReportCount(): Int = dao.getReportCount()

    private val firebaseDb: FirebaseDatabase by lazy {
        try {
            FirebaseDatabase.getInstance("https://apk-satpol-pp-default-rtdb.asia-southeast1.firebasedatabase.app")
        } catch (_: Exception) {
            FirebaseDatabase.getInstance()
        }
    }

    private val firebaseReportsRef by lazy {
        firebaseDb.getReference("laporan")
    }

    fun pushToFirebase(report: ViolationReport) {
        try {
            val key = report.ticketNumber.ifBlank { "TKT-${System.currentTimeMillis()}" }
            val map = mapOf(
                "id" to key,
                "ticketNumber" to report.ticketNumber,
                "title" to report.title,
                "judul" to report.title,
                "description" to report.description,
                "deskripsi" to report.description,
                "category" to report.category,
                "urgency" to report.urgency,
                "reporterName" to report.reporterName,
                "reporterPhone" to report.reporterPhone,
                "isAnonymous" to report.isAnonymous,
                "latitude" to report.latitude,
                "longitude" to report.longitude,
                "accuracyMeters" to report.accuracyMeters.toDouble(),
                "address" to report.address,
                "lokasi" to report.address,
                "landmark" to report.landmark,
                "photoUri" to (report.photoUri ?: ""),
                "status" to report.status,
                "officerNotes" to (report.officerNotes ?: ""),
                "timestamp" to report.timestamp,
                "updatedAt" to report.updatedAt,
                "dispatchStatus" to report.dispatchStatus,
                "assignedOfficerName" to (report.assignedOfficerName ?: ""),
                "assignedOfficerPhone" to (report.assignedOfficerPhone ?: ""),
                "assignedOfficerSquad" to (report.assignedOfficerSquad ?: ""),
                "assignedOfficerVehicle" to (report.assignedOfficerVehicle ?: "")
            )
            firebaseReportsRef.child(key).setValue(map)
        } catch (e: Exception) {
            Log.w("ViolationRepository", "Firebase push error: ${e.message}")
        }
    }

    fun startFirebaseRealtimeListener(scope: CoroutineScope) {
        try {
            firebaseReportsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    scope.launch(Dispatchers.IO) {
                        for (child in snapshot.children) {
                            try {
                                val ticket = child.child("ticketNumber").getValue(String::class.java)
                                    ?: child.child("id").getValue(String::class.java)
                                    ?: child.key ?: continue

                                val title = child.child("title").getValue(String::class.java)
                                    ?: child.child("judul").getValue(String::class.java) ?: "Laporan"
                                val description = child.child("description").getValue(String::class.java)
                                    ?: child.child("deskripsi").getValue(String::class.java) ?: ""
                                val category = child.child("category").getValue(String::class.java) ?: ViolationReport.CAT_PKL
                                val urgency = child.child("urgency").getValue(String::class.java) ?: ViolationReport.URGENCY_MEDIUM
                                val reporterName = child.child("reporterName").getValue(String::class.java) ?: "Warga"
                                val reporterPhone = child.child("reporterPhone").getValue(String::class.java) ?: ""
                                val isAnonymous = child.child("isAnonymous").getValue(Boolean::class.java) ?: false
                                val latitude = child.child("latitude").getValue(Double::class.java) ?: -0.3785
                                val longitude = child.child("longitude").getValue(Double::class.java) ?: 102.2982
                                val accuracyMeters = (child.child("accuracyMeters").getValue(Double::class.java) ?: 10.0).toFloat()
                                val address = child.child("address").getValue(String::class.java)
                                    ?: child.child("lokasi").getValue(String::class.java) ?: "Rengat"
                                val landmark = child.child("landmark").getValue(String::class.java) ?: ""
                                val photoUri = child.child("photoUri").getValue(String::class.java)?.takeIf { it.isNotBlank() }
                                val status = child.child("status").getValue(String::class.java) ?: ViolationReport.STATUS_PENDING
                                val officerNotes = child.child("officerNotes").getValue(String::class.java)?.takeIf { it.isNotBlank() }
                                val timestamp = child.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()
                                val updatedAt = child.child("updatedAt").getValue(Long::class.java) ?: timestamp
                                val dispatchStatus = child.child("dispatchStatus").getValue(String::class.java) ?: ViolationReport.DISPATCH_PENDING
                                val assignedOfficerName = child.child("assignedOfficerName").getValue(String::class.java)?.takeIf { it.isNotBlank() }
                                val assignedOfficerPhone = child.child("assignedOfficerPhone").getValue(String::class.java)?.takeIf { it.isNotBlank() }
                                val assignedOfficerSquad = child.child("assignedOfficerSquad").getValue(String::class.java)?.takeIf { it.isNotBlank() }
                                val assignedOfficerVehicle = child.child("assignedOfficerVehicle").getValue(String::class.java)?.takeIf { it.isNotBlank() }

                                val reportFromFirebase = ViolationReport(
                                    id = 0,
                                    ticketNumber = ticket,
                                    category = category,
                                    title = title,
                                    description = description,
                                    urgency = urgency,
                                    reporterName = reporterName,
                                    reporterPhone = reporterPhone,
                                    isAnonymous = isAnonymous,
                                    latitude = latitude,
                                    longitude = longitude,
                                    accuracyMeters = accuracyMeters,
                                    address = address,
                                    landmark = landmark,
                                    photoUri = photoUri,
                                    status = status,
                                    officerNotes = officerNotes,
                                    timestamp = timestamp,
                                    updatedAt = updatedAt,
                                    dispatchStatus = dispatchStatus,
                                    assignedOfficerName = assignedOfficerName,
                                    assignedOfficerPhone = assignedOfficerPhone,
                                    assignedOfficerSquad = assignedOfficerSquad,
                                    assignedOfficerVehicle = assignedOfficerVehicle
                                )

                                val existing = dao.getReportByTicket(ticket)
                                if (existing == null) {
                                    dao.insertReport(reportFromFirebase)
                                } else if (updatedAt > existing.updatedAt) {
                                    dao.updateReport(reportFromFirebase.copy(id = existing.id))
                                }
                            } catch (_: Exception) {}
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("ViolationRepository", "Firebase listener error: ${error.message}")
                }
            })
        } catch (e: Exception) {
            Log.w("ViolationRepository", "Firebase init error: ${e.message}")
        }
    }

    suspend fun submitReport(report: ViolationReport): Long {
        val id = dao.insertReport(report)
        pushToFirebase(report)
        try {
            syncWithCloud()
        } catch (_: Exception) {}
        return id
    }

    suspend fun insertReports(reports: List<ViolationReport>) {
        dao.insertReports(reports)
        for (r in reports) {
            pushToFirebase(r)
        }
    }

    suspend fun updateReport(report: ViolationReport) {
        dao.updateReport(report)
        pushToFirebase(report)
        try {
            syncWithCloud()
        } catch (_: Exception) {}
    }

    suspend fun updateStatus(id: Long, status: String, officerNotes: String?) {
        dao.updateStatus(id, status, officerNotes)
        val updated = dao.getReportByIdDirect(id)
        if (updated != null) {
            pushToFirebase(updated)
        }
        try {
            syncWithCloud()
        } catch (_: Exception) {}
    }

    suspend fun syncWithCloud(): Int {
        val localList = dao.getAllReportsDirect()
        val isOfficerLoggedIn = AuthManager.isOfficerLoggedIn.value
        val isOfficerOnline = AuthManager.isOfficerOnline.value
        val officerName = if (isOfficerLoggedIn) AuthManager.activeOfficer.value?.fullName else null

        val (mergedList, cloudOfficerState) = RealtimeCloudSyncService.syncReports(
            localReports = localList,
            currentOfficerOnline = isOfficerOnline,
            currentOfficerName = officerName
        )

        var newOrUpdatedCount = 0
        for (cloudReport in mergedList) {
            val existing = dao.getReportByTicket(cloudReport.ticketNumber)
            if (existing == null) {
                // New report arrived from another device!
                dao.insertReport(cloudReport.copy(id = 0))
                newOrUpdatedCount++
            } else if (cloudReport.updatedAt > existing.updatedAt) {
                // More recent update from another device!
                dao.updateReport(cloudReport.copy(id = existing.id))
                newOrUpdatedCount++
            }
        }

        // Update citizen view with latest officer state from cloud
        if (!isOfficerLoggedIn && cloudOfficerState != null) {
            AuthManager.updateCloudOfficerStatus(
                isOnline = cloudOfficerState.isOnline,
                officerName = cloudOfficerState.officerName,
                squadName = cloudOfficerState.squadName,
                vehicleName = cloudOfficerState.vehicleName
            )
        }

        return newOrUpdatedCount
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
        val updated = dao.getReportByIdDirect(report.id)
        if (updated != null) {
            pushToFirebase(updated)
        }
        try {
            syncWithCloud()
        } catch (_: Exception) {}
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
                dao.getReportByIdDirect(report.id)?.let { pushToFirebase(it) }
                try {
                    syncWithCloud()
                } catch (_: Exception) {}
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
                dao.getReportByIdDirect(report.id)?.let { pushToFirebase(it) }
                try {
                    syncWithCloud()
                } catch (_: Exception) {}
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
                dao.getReportByIdDirect(report.id)?.let { pushToFirebase(it) }
                try {
                    syncWithCloud()
                } catch (_: Exception) {}
            }
        }
    }

    suspend fun deleteReport(id: Long) {
        val report = dao.getReportByIdDirect(id)
        dao.deleteReport(id)
        if (report != null) {
            try {
                firebaseReportsRef.child(report.ticketNumber).removeValue()
            } catch (_: Exception) {}
        }
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
