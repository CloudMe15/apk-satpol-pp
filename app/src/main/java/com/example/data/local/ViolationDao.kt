package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ViolationReport
import kotlinx.coroutines.flow.Flow

@Dao
interface ViolationDao {
    @Query("SELECT * FROM violation_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ViolationReport>>

    @Query("SELECT * FROM violation_reports WHERE id = :id")
    fun getReportById(id: Long): Flow<ViolationReport?>

    @Query("SELECT * FROM violation_reports WHERE status = :status ORDER BY timestamp DESC")
    fun getReportsByStatus(status: String): Flow<List<ViolationReport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ViolationReport): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReports(reports: List<ViolationReport>)

    @Update
    suspend fun updateReport(report: ViolationReport)

    @Query("UPDATE violation_reports SET status = :status, officerNotes = :officerNotes, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, officerNotes: String?, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE violation_reports SET assignedOfficerName = :officerName, assignedOfficerPhone = :officerPhone, assignedOfficerSquad = :squad, assignedOfficerVehicle = :vehicle, officerLatitude = :officerLat, officerLongitude = :officerLng, dispatchStatus = :dispatchStatus, dispatchEtaMinutes = :etaMinutes, status = :reportStatus, officerNotes = :officerNotes, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateDispatch(
        id: Long,
        officerName: String?,
        officerPhone: String?,
        squad: String?,
        vehicle: String?,
        officerLat: Double?,
        officerLng: Double?,
        dispatchStatus: String,
        etaMinutes: Int,
        reportStatus: String,
        officerNotes: String?,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM violation_reports WHERE id = :id")
    suspend fun deleteReport(id: Long)

    @Query("SELECT COUNT(*) FROM violation_reports")
    suspend fun getReportCount(): Int
}
