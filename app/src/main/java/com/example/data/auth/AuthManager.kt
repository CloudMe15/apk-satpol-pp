package com.example.data.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class UserRole {
    CITIZEN, // Warga / Pelapor (Seperti Penumpang di Ojek Online)
    OFFICER  // Anggota Satpol PP (Seperti Driver / Patroli yang Menerima Tugas)
}

data class OfficerAccount(
    val username: String,
    val password: String,
    val fullName: String,
    val rank: String,
    val nip: String,
    val squadName: String,
    val vehicleName: String,
    val phoneNumber: String
)

data class StatusNotification(
    val isOnline: Boolean,
    val officerName: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class UserProfile(
    val username: String,
    val fullName: String,
    val phoneNumber: String,
    val role: UserRole,
    val badgeNumber: String? = null,
    val squadName: String? = null,
    val vehicleName: String? = null
)

object AuthManager {

    const val CITIZEN_USER = "Warga2026"
    const val CITIZEN_PASS = "Warga2026"

    // Roster of Official Satpol PP Officers
    val OFFICER_ROSTER = listOf(
        OfficerAccount(
            username = "Satpolpp2026",
            password = "CloudMe2026",
            fullName = "Bripka Danu Prasetyo",
            rank = "Danru Reaksi Cepat",
            nip = "199507102025211095",
            squadName = "Unit Patroli Reaksi Cepat (UPRC)",
            vehicleName = "Mobil Patroli Dalmas (BM 1002 IN)",
            phoneNumber = "0812-7890-1122"
        ),
        OfficerAccount(
            username = "AgusSatpol",
            password = "CloudMe2026",
            fullName = "Serma Agus Kurniawan",
            rank = "Komandan Regu 2",
            nip = "198804152010121003",
            squadName = "Regu Penegakan Perda 2 (Tibum)",
            vehicleName = "Motor Trail Reaksi Cepat (BM 3421 IN)",
            phoneNumber = "0821-4455-6677"
        ),
        OfficerAccount(
            username = "HendraSatpol",
            password = "CloudMe2026",
            fullName = "Brigadir Hendra Saputra",
            rank = "Anggota Patroli Lapangan",
            nip = "199203112015031008",
            squadName = "Tim Patroli Wilayah Kota Rengat",
            vehicleName = "Mobil Patroli Ranger (BM 1005 IN)",
            phoneNumber = "0813-9988-1122"
        ),
        OfficerAccount(
            username = "KomandanSatpol",
            password = "CloudMe2026",
            fullName = "Komandan Fajar Ari Prakoso",
            rank = "Perwira Pengawas Piket Mako",
            nip = "198509202008011005",
            squadName = "Komando Pengawasan & Pengendalian Operasional",
            vehicleName = "Mobil Komando Ranger (BM 1 IN)",
            phoneNumber = "0812-3456-7890"
        )
    )

    // Default Citizen profile (Fajar Ari Prakoso)
    private val defaultCitizen = UserProfile(
        username = CITIZEN_USER,
        fullName = "Fajar Ari Prakoso",
        phoneNumber = "0812-3456-7890",
        role = UserRole.CITIZEN
    )

    private val _currentUser = MutableStateFlow<UserProfile?>(defaultCitizen)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _isOfficerLoggedIn = MutableStateFlow(false)
    val isOfficerLoggedIn: StateFlow<Boolean> = _isOfficerLoggedIn.asStateFlow()

    private val _activeOfficer = MutableStateFlow<OfficerAccount?>(null)
    val activeOfficer: StateFlow<OfficerAccount?> = _activeOfficer.asStateFlow()

    private val _officerUser = MutableStateFlow<String?>(null)
    val officerUser: StateFlow<String?> = _officerUser.asStateFlow()

    private val _officerName = MutableStateFlow<String?>("Bripka Danu Prasetyo")
    val officerName: StateFlow<String?> = _officerName.asStateFlow()

    // Officer Online/Offline Status (Like Gojek/Grab driver online toggle)
    private val _isOfficerOnline = MutableStateFlow(true)
    val isOfficerOnline: StateFlow<Boolean> = _isOfficerOnline.asStateFlow()

    // Status Notification Banner
    private val _statusNotification = MutableStateFlow<StatusNotification?>(null)
    val statusNotification: StateFlow<StatusNotification?> = _statusNotification.asStateFlow()

    fun login(user: String, pass: String): Boolean {
        val cleanUser = user.trim()
        val cleanPass = pass.trim()

        // Check against officer roster
        val matchedOfficer = OFFICER_ROSTER.find {
            it.username.equals(cleanUser, ignoreCase = true) && it.password == cleanPass
        }

        if (matchedOfficer != null) {
            _activeOfficer.value = matchedOfficer
            _currentUser.value = UserProfile(
                username = matchedOfficer.username,
                fullName = matchedOfficer.fullName,
                phoneNumber = matchedOfficer.phoneNumber,
                role = UserRole.OFFICER,
                badgeNumber = "NIP. ${matchedOfficer.nip}",
                squadName = matchedOfficer.squadName,
                vehicleName = matchedOfficer.vehicleName
            )
            _isOfficerLoggedIn.value = true
            _officerUser.value = matchedOfficer.username
            _officerName.value = matchedOfficer.fullName
            _isOfficerOnline.value = true

            // Trigger Welcome Online Notification
            _statusNotification.value = StatusNotification(
                isOnline = true,
                officerName = matchedOfficer.fullName,
                message = "🟢 STATUS SIAGA AKTIF (ONLINE): Anda siap menerima dan merespons gangguan trantibum."
            )
            return true
        }

        // Check citizen account
        if (cleanUser.equals(CITIZEN_USER, ignoreCase = true) && (cleanPass == CITIZEN_PASS || cleanPass == "123456")) {
            _currentUser.value = defaultCitizen
            _isOfficerLoggedIn.value = false
            _activeOfficer.value = null
            _officerUser.value = null
            return true
        }

        return false
    }

    fun setOfficerOnline(online: Boolean) {
        _isOfficerOnline.value = online
        val name = _activeOfficer.value?.fullName ?: "Petugas Satpol PP"

        _statusNotification.value = if (online) {
            StatusNotification(
                isOnline = true,
                officerName = name,
                message = "🟢 ANDA SEKARANG ONLINE: Siaga di radar patroli dan siap menerima disposisi gangguan warga!"
            )
        } else {
            StatusNotification(
                isOnline = false,
                officerName = name,
                message = "🔴 ANDA SEKARANG OFFLINE (Lepas Piket): Tidak akan menerima panggilan penugasan baru."
            )
        }
    }

    fun dismissNotification() {
        _statusNotification.value = null
    }

    fun loginAsCitizen() {
        _currentUser.value = defaultCitizen
        _isOfficerLoggedIn.value = false
        _activeOfficer.value = null
        _officerUser.value = null
    }

    fun logout() {
        _isOfficerLoggedIn.value = false
        _activeOfficer.value = null
        _officerUser.value = null
        _currentUser.value = defaultCitizen
        _statusNotification.value = null
    }
}
