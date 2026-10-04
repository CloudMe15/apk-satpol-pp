package com.example.data.remote

import android.util.Log
import com.example.data.auth.AuthManager
import com.example.data.model.ViolationReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.BuildConfig

data class AppUpdateInfo(
    val hasUpdate: Boolean,
    val latestVersionName: String,
    val updateNotes: String,
    val downloadUrl: String,
    val forceUpdate: Boolean = false
)

object RealtimeCloudSyncService {

    private const val TAG = "RealtimeCloudSync"
    private const val OBJECT_ID = "ff808181a09d98f701a102e4ebc96e09"
    private const val ENDPOINT_URL = "https://api.restful-api.dev/objects/$OBJECT_ID"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val _appUpdateInfo = MutableStateFlow<AppUpdateInfo?>(null)
    val appUpdateInfo: StateFlow<AppUpdateInfo?> = _appUpdateInfo.asStateFlow()

    init {
        // Automatically listen to Firebase Realtime Database node 'app_config'
        try {
            val db = try {
                com.google.firebase.database.FirebaseDatabase.getInstance("https://apk-satpol-pp-default-rtdb.asia-southeast1.firebasedatabase.app")
            } catch (_: Exception) {
                com.google.firebase.database.FirebaseDatabase.getInstance()
            }
            val configRef = db.getReference("app_config")
            configRef.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val latestCode = snapshot.child("latest_version_code").getValue(Long::class.java)?.toInt() ?: 0
                    val latestName = snapshot.child("latest_version_name").getValue(String::class.java) ?: "1.3"
                    val downloadUrl = snapshot.child("download_url").getValue(String::class.java) ?: ""
                    val releaseNotes = snapshot.child("release_notes").getValue(String::class.java)
                        ?: "Peningkatan performa, perbaikan sinkronisasi real-time, dan pembaruan sistem keamanan."
                    val forceUpdate = snapshot.child("force_update").getValue(Boolean::class.java) ?: false

                    if (latestCode > BuildConfig.VERSION_CODE && downloadUrl.isNotBlank()) {
                        Log.i(TAG, "Update available from Firebase app_config: v$latestCode > local ${BuildConfig.VERSION_CODE} (Force: $forceUpdate)")
                        _appUpdateInfo.value = AppUpdateInfo(
                            hasUpdate = true,
                            latestVersionName = latestName,
                            updateNotes = releaseNotes,
                            downloadUrl = downloadUrl,
                            forceUpdate = forceUpdate
                        )
                    }
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    Log.w(TAG, "Firebase app_config cancelled: ${error.message}")
                }
            })
        } catch (e: Exception) {
            Log.w(TAG, "Error initializing Firebase app_config listener: ${e.message}")
        }
    }

    fun dismissUpdate() {
        _appUpdateInfo.value = null
    }

    /**
     * Performs a bidirectional sync between this device's local reports and the shared cloud backend.
     * Returns the merged list of all reports.
     */
    suspend fun syncReports(
        localReports: List<ViolationReport>,
        currentOfficerOnline: Boolean,
        currentOfficerName: String?
    ): Pair<List<ViolationReport>, CloudOfficerState?> = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch current cloud state
            val fetchRequest = Request.Builder()
                .url(ENDPOINT_URL)
                .get()
                .build()

            val cloudReports = mutableListOf<ViolationReport>()
            var cloudOfficerState: CloudOfficerState? = null

            httpClient.newCall(fetchRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (!bodyString.isNullOrBlank()) {
                        val rootObj = JSONObject(bodyString)
                        val dataObj = rootObj.optJSONObject("data")
                        if (dataObj != null) {
                            val officerOnline = when {
                                dataObj.has("isOfficerOnline") -> dataObj.optBoolean("isOfficerOnline", false)
                                dataObj.has("on") -> dataObj.optBoolean("on", false)
                                else -> false
                            }
                            val officerName = when {
                                dataObj.has("activeOfficerName") -> dataObj.optString("activeOfficerName", "")
                                dataObj.has("off") -> dataObj.optString("off", "")
                                else -> ""
                            }
                            val squad = dataObj.optString("activeOfficerSquad", "")
                            val vehicle = dataObj.optString("activeOfficerVehicle", "")
                            val lastOnlineUpdate = when {
                                dataObj.has("lastOnlineUpdate") -> dataObj.optLong("lastOnlineUpdate", 0L)
                                dataObj.has("t") -> dataObj.optLong("t", 0L)
                                else -> 0L
                            }

                            cloudOfficerState = CloudOfficerState(
                                isOnline = officerOnline,
                                officerName = officerName,
                                squadName = squad,
                                vehicleName = vehicle,
                                timestamp = lastOnlineUpdate
                            )

                            // Read reports from either "r", "reportsJson", or "reports"
                            val reportsStr = when {
                                dataObj.has("r") -> dataObj.optString("r", "")
                                dataObj.has("reportsJson") -> dataObj.optString("reportsJson", "")
                                else -> ""
                            }

                            if (reportsStr.isNotBlank()) {
                                try {
                                    val jsonArr = JSONArray(reportsStr)
                                    for (i in 0 until jsonArr.length()) {
                                        val rObj = jsonArr.optJSONObject(i)
                                        if (rObj != null) {
                                            val parsed = jsonToReport(rObj)
                                            if (parsed != null) {
                                                cloudReports.add(parsed)
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed parsing cloud reports: ${e.message}")
                                }
                            }

                            // In-App Version Update Check
                            val latestVersionCode = when {
                                dataObj.has("latestVersionCode") -> dataObj.optInt("latestVersionCode", 3)
                                dataObj.has("vc") -> dataObj.optInt("vc", 3)
                                else -> 3
                            }
                            val latestVersionName = when {
                                dataObj.has("latestVersionName") -> dataObj.optString("latestVersionName", "1.2")
                                dataObj.has("vn") -> dataObj.optString("vn", "1.2")
                                else -> "1.2"
                            }
                            val updateNotes = dataObj.optString("updateNotes", "Tersedia pembaruan versi baru untuk aplikasi Satpol PP Siaga.")
                            val downloadUrl = dataObj.optString("updateDownloadUrl", "https://ais-dev-rphctfh57sdaa5pr775pyv-312441348465.asia-east1.run.app")

                            if (latestVersionCode > BuildConfig.VERSION_CODE) {
                                _appUpdateInfo.value = AppUpdateInfo(
                                    hasUpdate = true,
                                    latestVersionName = latestVersionName,
                                    updateNotes = updateNotes,
                                    downloadUrl = downloadUrl
                                )
                            }
                        }
                    }
                } else {
                    Log.w(TAG, "Fetch cloud reports returned HTTP ${response.code}")
                }
            }

            // 2. Merge local and cloud reports by ticketNumber
            val mergedMap = mutableMapOf<String, ViolationReport>()

            // Add all cloud reports first
            for (cr in cloudReports) {
                mergedMap[cr.ticketNumber] = cr
            }

            // Merge local reports (keep the version with the latest updatedAt)
            for (lr in localReports) {
                val existing = mergedMap[lr.ticketNumber]
                if (existing == null) {
                    mergedMap[lr.ticketNumber] = lr
                } else {
                    if (lr.updatedAt >= existing.updatedAt) {
                        mergedMap[lr.ticketNumber] = lr
                    }
                }
            }

            val mergedList = mergedMap.values.sortedByDescending { it.timestamp }

            // 3. Keep payload under 800 bytes to prevent restful-api.dev 500 error
            val recentToUpload = mergedList.take(2)
            val minifiedArray = JSONArray()
            for (report in recentToUpload) {
                minifiedArray.put(reportToMinifiedJson(report))
            }

            val shouldUploadOfficer = currentOfficerName != null
            val uploadPayload = JSONObject().apply {
                put("name", "SatpolPP")
                val dataObj = JSONObject().apply {
                    put("v", 3)
                    put("vc", 3)
                    put("vn", "1.2")
                    put("t", System.currentTimeMillis())

                    if (shouldUploadOfficer) {
                        put("on", currentOfficerOnline)
                        put("off", currentOfficerName ?: "")
                    } else if (cloudOfficerState != null) {
                        put("on", cloudOfficerState.isOnline)
                        put("off", cloudOfficerState.officerName)
                    }

                    put("r", minifiedArray.toString())
                }
                put("data", dataObj)
            }

            val putBody = uploadPayload.toString().toRequestBody(JSON_MEDIA_TYPE)
            val putRequest = Request.Builder()
                .url(ENDPOINT_URL)
                .put(putBody)
                .build()

            httpClient.newCall(putRequest).execute().use { putResponse ->
                if (!putResponse.isSuccessful) {
                    Log.w(TAG, "PUT cloud update returned HTTP ${putResponse.code}")
                } else {
                    Log.d(TAG, "Successfully synced reports to cloud (${mergedList.size} reports)!")
                }
            }

            Pair(mergedList, cloudOfficerState)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing with cloud: ${e.message}", e)
            Pair(localReports, null)
        }
    }

    private fun reportToMinifiedJson(r: ViolationReport): JSONObject {
        return JSONObject().apply {
            put("t", r.ticketNumber)
            put("c", r.category)
            put("tl", r.title.take(30))
            put("d", r.description.take(50))
            put("u", r.urgency)
            put("n", r.reporterName.take(20))
            put("p", r.reporterPhone.take(15))
            put("a", if (r.isAnonymous) 1 else 0)
            put("lt", r.latitude)
            put("lg", r.longitude)
            put("ad", r.address.take(30))
            put("s", r.status)
            put("ts", r.timestamp)
            put("ut", r.updatedAt)
            if (!r.assignedOfficerName.isNullOrBlank()) put("on", r.assignedOfficerName)
            put("ds", r.dispatchStatus)
            put("eta", r.dispatchEtaMinutes)
        }
    }

    private fun jsonToReport(obj: JSONObject): ViolationReport? {
        return try {
            val ticketNumber = obj.optString("ticketNumber", "").ifBlank { obj.optString("t", "") }
            if (ticketNumber.isBlank()) return null

            val category = when {
                obj.has("category") -> obj.optString("category")
                obj.has("c") -> obj.optString("c")
                else -> ViolationReport.CAT_PKL
            }

            val title = when {
                obj.has("title") -> obj.optString("title")
                obj.has("tl") -> obj.optString("tl")
                else -> "Laporan Pelanggaran"
            }

            val description = when {
                obj.has("description") -> obj.optString("description")
                obj.has("d") -> obj.optString("d")
                else -> ""
            }

            val urgency = when {
                obj.has("urgency") -> obj.optString("urgency")
                obj.has("u") -> obj.optString("u")
                else -> ViolationReport.URGENCY_MEDIUM
            }

            val reporterName = when {
                obj.has("reporterName") -> obj.optString("reporterName")
                obj.has("n") -> obj.optString("n")
                else -> "Warga"
            }

            val reporterPhone = when {
                obj.has("reporterPhone") -> obj.optString("reporterPhone")
                obj.has("p") -> obj.optString("p")
                else -> ""
            }

            val isAnonymous = when {
                obj.has("isAnonymous") -> obj.optBoolean("isAnonymous")
                obj.has("a") -> obj.optInt("a") == 1
                else -> false
            }

            val latitude = when {
                obj.has("latitude") -> obj.optDouble("latitude", -0.3785)
                obj.has("lt") -> obj.optDouble("lt", -0.3785)
                else -> -0.3785
            }

            val longitude = when {
                obj.has("longitude") -> obj.optDouble("longitude", 102.2982)
                obj.has("lg") -> obj.optDouble("lg", 102.2982)
                else -> 102.2982
            }

            val address = when {
                obj.has("address") -> obj.optString("address")
                obj.has("ad") -> obj.optString("ad")
                else -> "Rengat, Indragiri Hulu"
            }

            val status = when {
                obj.has("status") -> obj.optString("status")
                obj.has("s") -> obj.optString("s")
                else -> ViolationReport.STATUS_PENDING
            }

            val timestamp = when {
                obj.has("timestamp") -> obj.optLong("timestamp")
                obj.has("ts") -> obj.optLong("ts")
                else -> System.currentTimeMillis()
            }

            val updatedAt = when {
                obj.has("updatedAt") -> obj.optLong("updatedAt")
                obj.has("ut") -> obj.optLong("ut")
                else -> timestamp
            }

            val assignedOfficerName = when {
                obj.has("assignedOfficerName") -> obj.optString("assignedOfficerName").takeIf { it.isNotBlank() }
                obj.has("on") -> obj.optString("on").takeIf { it.isNotBlank() }
                else -> null
            }

            val dispatchStatus = when {
                obj.has("dispatchStatus") -> obj.optString("dispatchStatus")
                obj.has("ds") -> obj.optString("ds")
                else -> ViolationReport.DISPATCH_PENDING
            }

            val dispatchEtaMinutes = when {
                obj.has("dispatchEtaMinutes") -> obj.optInt("dispatchEtaMinutes")
                obj.has("eta") -> obj.optInt("eta")
                else -> 0
            }

            ViolationReport(
                id = 0, // Generated by Room locally
                ticketNumber = ticketNumber,
                category = category,
                title = title,
                description = description,
                urgency = urgency,
                reporterName = reporterName,
                reporterPhone = reporterPhone,
                isAnonymous = isAnonymous,
                latitude = latitude,
                longitude = longitude,
                accuracyMeters = 5.0f,
                address = address,
                landmark = obj.optString("landmark", ""),
                photoUri = obj.optString("photoUri").takeIf { it.isNotBlank() },
                status = status,
                officerNotes = obj.optString("officerNotes").takeIf { it.isNotBlank() },
                timestamp = timestamp,
                updatedAt = updatedAt,
                assignedOfficerName = assignedOfficerName,
                assignedOfficerPhone = obj.optString("assignedOfficerPhone").takeIf { it.isNotBlank() },
                assignedOfficerSquad = obj.optString("assignedOfficerSquad").takeIf { it.isNotBlank() },
                assignedOfficerVehicle = obj.optString("assignedOfficerVehicle").takeIf { it.isNotBlank() },
                officerLatitude = if (obj.has("officerLatitude")) obj.optDouble("officerLatitude") else null,
                officerLongitude = if (obj.has("officerLongitude")) obj.optDouble("officerLongitude") else null,
                dispatchStatus = dispatchStatus,
                dispatchEtaMinutes = dispatchEtaMinutes
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse report json: ${e.message}")
            null
        }
    }
}

data class CloudOfficerState(
    val isOnline: Boolean,
    val officerName: String,
    val squadName: String,
    val vehicleName: String,
    val timestamp: Long
)
