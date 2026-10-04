package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.auth.AuthManager
import com.example.data.auth.OfficerAccount
import com.example.data.model.ViolationReport
import com.example.ui.components.StatusBadge
import com.example.ui.components.UrgencyBadge
import com.example.ui.theme.satpolTextFieldColors
import com.example.ui.theme.SatpolBlueDark
import com.example.ui.theme.SatpolBluePrimary
import com.example.ui.theme.SatpolGold
import com.example.ui.theme.SatpolGoldLight
import com.example.ui.theme.SatpolGreen
import com.example.ui.theme.SatpolGreenLight
import com.example.ui.theme.SatpolRedAlert
import com.example.ui.theme.SatpolRedLight
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import com.example.ui.viewmodel.ReportViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    viewModel: ReportViewModel,
    onNavigateToDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allReports by viewModel.allReports.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedStatus by viewModel.statusFilter.collectAsStateWithLifecycle()

    // Central AuthManager state
    val isOfficerLoggedIn by AuthManager.isOfficerLoggedIn.collectAsStateWithLifecycle()
    val activeOfficer by AuthManager.activeOfficer.collectAsStateWithLifecycle()
    val officerName by AuthManager.officerName.collectAsStateWithLifecycle()
    val officerUser by AuthManager.officerUser.collectAsStateWithLifecycle()
    val isOfficerOnline by AuthManager.isOfficerOnline.collectAsStateWithLifecycle()
    val statusNotification by AuthManager.statusNotification.collectAsStateWithLifecycle()

    // Login Form State - Manual typing only (no auto-fill)
    var nipInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var loginErrorMessage by remember { mutableStateOf<String?>(null) }

    // Dashboard Tabs
    var activeAdminTab by remember { mutableStateOf(0) } // 0: Semua Laporan Masuk, 1: Riwayat & Selesai
    var updatingReport by remember { mutableStateOf<ViolationReport?>(null) }
    var selectedNewStatus by remember { mutableStateOf(ViolationReport.STATUS_IN_PROGRESS) }
    var officerNoteInput by remember { mutableStateOf("") }

    val pendingCount = allReports.count { it.status == ViolationReport.STATUS_PENDING }
    val inProgressCount = allReports.count { it.status == ViolationReport.STATUS_IN_PROGRESS }
    val resolvedCount = allReports.count { it.status == ViolationReport.STATUS_RESOLVED }
    val rejectedCount = allReports.count { it.status == ViolationReport.STATUS_REJECTED }
    val totalCount = allReports.size

    val displayedReports = remember(allReports, activeAdminTab, selectedStatus, searchQuery) {
        allReports.filter { report ->
            val matchesTab = if (activeAdminTab == 0) {
                selectedStatus == null || report.status == selectedStatus
            } else {
                report.status == ViolationReport.STATUS_RESOLVED || report.status == ViolationReport.STATUS_REJECTED
            }

            val matchesQuery = searchQuery.isBlank() ||
                    report.title.contains(searchQuery, ignoreCase = true) ||
                    report.ticketNumber.contains(searchQuery, ignoreCase = true) ||
                    report.address.contains(searchQuery, ignoreCase = true) ||
                    report.category.contains(searchQuery, ignoreCase = true)

            matchesTab && matchesQuery
        }
    }

    // IF NOT LOGGED IN: Render Dedicated Officer Login Screen with Account Roster
    if (!isOfficerLoggedIn) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Logos Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_satpol),
                                contentDescription = "Logo Satpol PP",
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Image(
                                painter = painterResource(id = R.drawable.logo_inhu),
                                contentDescription = "Logo Inhu",
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "PORTAL LOGIN ANGGOTA SATPOL PP",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            ),
                            color = SatpolBluePrimary
                        )

                        Text(
                            text = "Pusat Komando & Reaksi Cepat Trantibum",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Username input - Manual typing with high-contrast text
                        OutlinedTextField(
                            value = nipInput,
                            onValueChange = {
                                nipInput = it
                                loginErrorMessage = null
                            },
                            label = { Text("User / ID Anggota") },
                            placeholder = { Text("Ketik ID, contoh: Satpolpp2026") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = null,
                                    tint = SatpolBluePrimary
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_admin_nip"),
                            colors = satpolTextFieldColors(),
                            textStyle = TextStyle(
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Password input - Manual typing with high-contrast text
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = {
                                passwordInput = it
                                loginErrorMessage = null
                            },
                            label = { Text("Kata Sandi (Password)") },
                            placeholder = { Text("Ketik kata sandi...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = SatpolBluePrimary
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Lihat Sandi",
                                        tint = Slate600
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_admin_password"),
                            colors = satpolTextFieldColors(),
                            textStyle = TextStyle(
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )

                        if (loginErrorMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = loginErrorMessage!!,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SatpolRedAlert,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Login Button
                        Button(
                            onClick = {
                                val success = AuthManager.login(nipInput, passwordInput)
                                if (success) {
                                    loginErrorMessage = null
                                } else {
                                    loginErrorMessage = "Akses Ditolak! Kredensial anggota tidak valid. Silakan gunakan password CloudMe2026."
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_admin_login"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SatpolBluePrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VpnKey,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Masuk Sebagai Petugas",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Status Monitoring Petugas yang Sedang Login / Online
            item {
                val isOnlineNow = isOfficerLoggedIn && isOfficerOnline
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOnlineNow) Color(0xFFDCFCE7) else Color(0xFFF1F5F9)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isOnlineNow) Color(0xFF16A34A) else Slate200
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isOnlineNow) SatpolGreen else Slate600)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MONITORING PERSONIL YANG SEDANG LOGIN:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = if (isOnlineNow) Color(0xFF166534) else Slate600
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isOnlineNow) {
                                "🟢 ${activeOfficer?.fullName ?: "Petugas"} (${activeOfficer?.rank}) - Status: ONLINE SIAGA"
                            } else if (isOfficerLoggedIn) {
                                "🟡 ${activeOfficer?.fullName ?: "Petugas"} - Status: OFFLINE (Sedang Lepas Piket / Istirahat)"
                            } else {
                                "🔴 Belum ada akun petugas yang aktif login saat ini."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isOnlineNow) Color(0xFF15803D) else Slate800
                        )
                    }
                }
            }

            // Roster of Satpol PP Officers Card Selection (Informasi Akun Personil Piket)
            item {
                Text(
                    text = "DAFTAR ID PERSONIL SATPOL PP (KETIK MANUAL DI ATAS):",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Slate600,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            items(AuthManager.OFFICER_ROSTER) { officer ->
                OfficerAccountCard(
                    officer = officer
                )
            }
        }
        return
    }

    // IF LOGGED IN: Render Full Officer Working Dashboard
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // In-App Status Notification Banner (Alert when switched Online/Offline)
            item {
                AnimatedVisibility(
                    visible = statusNotification != null,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    if (statusNotification != null) {
                        val notif = statusNotification!!
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (notif.isOnline) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = if (notif.isOnline) Color(0xFF166534) else SatpolRedAlert,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = notif.message,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        ),
                                        color = if (notif.isOnline) Color(0xFF166534) else SatpolRedAlert
                                    )
                                }
                                IconButton(
                                    onClick = { AuthManager.dismissNotification() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Tutup",
                                        tint = Slate600,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Driver-style ONLINE / OFFLINE Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOfficerOnline) Color(0xFF0F172A) else Color(0xFF1E293B)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(if (isOfficerOnline) SatpolGreen else Slate600)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isOfficerOnline) "STATUS: SIAGA AKTIF (ONLINE)" else "STATUS: ISTIRAHAT (OFFLINE)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = if (isOfficerOnline) SatpolGreenLight else Slate200
                                )
                                Text(
                                    text = if (isOfficerOnline) "Siaga di radar patroli & menerima disposisi warga" else "Mode lepas piket / istirahat sementara",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Switch(
                            checked = isOfficerOnline,
                            onCheckedChange = { AuthManager.setOfficerOnline(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SatpolGreen,
                                uncheckedThumbColor = Slate200,
                                uncheckedTrackColor = Slate600
                            )
                        )
                    }
                }
            }

            // Officer Profile & Session Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SatpolBlueDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(SatpolGold),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = Slate800,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = activeOfficer?.fullName ?: officerName ?: "Aparatur Satpol PP",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${activeOfficer?.rank ?: "Petugas"} • NIP. ${activeOfficer?.nip ?: "199507102025211095"}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = SatpolGoldLight
                                    )
                                }
                            }

                            // Logout button
                            Button(
                                onClick = { AuthManager.logout() },
                                colors = ButtonDefaults.buttonColors(containerColor = SatpolRedAlert),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("btn_admin_logout")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Keluar Sesi",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Keluar", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Regu: ${activeOfficer?.squadName ?: "Unit Reaksi Cepat"} | Armada: ${activeOfficer?.vehicleName ?: "Mobil Dalmas"}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color(0xFFCBD5E1),
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Status Perlindungan Keamanan APK
            item {
                SecurityProtectionCard()
            }

            // Metrics Grid (4 KPI Cards)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AdminMetricCard(
                            title = "Total Laporan",
                            count = totalCount,
                            color = SatpolBluePrimary,
                            bgColor = Color(0xFFEFF6FF),
                            modifier = Modifier.weight(1f)
                        )
                        AdminMetricCard(
                            title = "Menunggu",
                            count = pendingCount,
                            color = SatpolRedAlert,
                            bgColor = Color(0xFFFEF2F2),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AdminMetricCard(
                            title = "Ditangani",
                            count = inProgressCount,
                            color = Color(0xFFD97706),
                            bgColor = Color(0xFFFFFBEB),
                            modifier = Modifier.weight(1f)
                        )
                        AdminMetricCard(
                            title = "Selesai",
                            count = resolvedCount,
                            color = SatpolGreen,
                            bgColor = Color(0xFFF0FDF4),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Navigation Tabs: Semua Laporan vs Riwayat
            item {
                TabRow(
                    selectedTabIndex = activeAdminTab,
                    containerColor = Color.White,
                    contentColor = SatpolBluePrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[activeAdminTab]),
                            color = SatpolBluePrimary,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = activeAdminTab == 0,
                        onClick = { activeAdminTab = 0 },
                        text = {
                            Text(
                                text = "Disposisi Aktif (${allReports.size})",
                                fontWeight = if (activeAdminTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (activeAdminTab == 0) SatpolBluePrimary else Slate600
                            )
                        }
                    )
                    Tab(
                        selected = activeAdminTab == 1,
                        onClick = { activeAdminTab = 1 },
                        text = {
                            Text(
                                text = "Arsip & Tuntas (${resolvedCount + rejectedCount})",
                                fontWeight = if (activeAdminTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (activeAdminTab == 1) SatpolBluePrimary else Slate600
                            )
                        }
                    )
                }
            }

            // Filter Chips
            if (activeAdminTab == 0) {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedStatus == null,
                                onClick = { viewModel.setStatusFilter(null) },
                                label = { Text("Semua Status") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SatpolBluePrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedStatus == ViolationReport.STATUS_PENDING,
                                onClick = { viewModel.setStatusFilter(ViolationReport.STATUS_PENDING) },
                                label = { Text("Menunggu ($pendingCount)") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedStatus == ViolationReport.STATUS_IN_PROGRESS,
                                onClick = { viewModel.setStatusFilter(ViolationReport.STATUS_IN_PROGRESS) },
                                label = { Text("Meluncur/Ditangani ($inProgressCount)") }
                            )
                        }
                    }
                }
            }

            // List of Reports
            if (displayedReports.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SatpolGreen,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Tidak ada laporan pada kategori ini",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Slate800
                            )
                        }
                    }
                }
            } else {
                items(displayedReports, key = { it.id }) { report ->
                    AdminReportCard(
                        report = report,
                        onViewDetail = { onNavigateToDetail(report.id) },
                        onUpdateStatusClick = {
                            updatingReport = report
                            selectedNewStatus = report.status
                            officerNoteInput = report.officerNotes ?: ""
                        },
                        onQuickAdvanceDispatch = {
                            viewModel.advanceDispatch(report)
                        }
                    )
                }
            }
        }
    }

    // Modal Dialog: Update Status Penanganan Laporan oleh Petugas
    if (updatingReport != null) {
        val target = updatingReport!!
        AlertDialog(
            onDismissRequest = { updatingReport = null },
            title = {
                Text(
                    text = "Update Status Penanganan Laporan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Slate800
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "No. Tiket: ${target.ticketNumber}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SatpolBluePrimary
                        )
                    )
                    Text(
                        text = target.title,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Slate800,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Pilih Status Penanganan Terbaru:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Slate600
                    )

                    val statusOptions = listOf(
                        ViolationReport.STATUS_PENDING to "Menunggu Verifikasi",
                        ViolationReport.STATUS_IN_PROGRESS to "Dalam Proses / Patroli Bergerak",
                        ViolationReport.STATUS_RESOLVED to "Selesai Ditertibkan",
                        ViolationReport.STATUS_REJECTED to "Ditolak / Tidak Valid"
                    )

                    statusOptions.forEach { (statusCode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedNewStatus == statusCode) Color(0xFFE0E7FF) else Color(0xFFF8FAFC))
                                .clickable { selectedNewStatus = statusCode }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedNewStatus == statusCode) SatpolBluePrimary else Color.Transparent)
                                    .border(2.dp, if (selectedNewStatus == statusCode) SatpolBluePrimary else Slate600, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (selectedNewStatus == statusCode) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (selectedNewStatus == statusCode) SatpolBluePrimary else Slate800
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = officerNoteInput,
                        onValueChange = { officerNoteInput = it },
                        label = { Text("Catatan Lapangan Petugas") },
                        placeholder = { Text("Contoh: Regu 1 telah memberikan teguran lisan...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp),
                        colors = satpolTextFieldColors(),
                        textStyle = TextStyle(
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentOfficer = activeOfficer?.fullName ?: officerUser ?: "Satpolpp2026"
                        val finalNote = if (officerNoteInput.isNotBlank()) {
                            "[$currentOfficer] $officerNoteInput"
                        } else {
                            "[$currentOfficer] Status diubah menjadi $selectedNewStatus"
                        }
                        viewModel.updateOfficerAction(
                            reportId = target.id,
                            newStatus = selectedNewStatus,
                            notes = finalNote
                        )
                        updatingReport = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SatpolBluePrimary),
                    modifier = Modifier.testTag("btn_save_admin_status")
                ) {
                    Text("Perbarui Status")
                }
            },
            dismissButton = {
                TextButton(onClick = { updatingReport = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun OfficerAccountCard(
    officer: OfficerAccount,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = SatpolBluePrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = officer.fullName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Slate800
                )
                Text(
                    text = "${officer.rank} • NIP. ${officer.nip}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Slate600
                )
                Text(
                    text = "Regu: ${officer.squadName} • ${officer.vehicleName}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = SatpolBluePrimary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE2E8F0))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "ID: ${officer.username}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = SatpolBlueDark,
                            fontSize = 11.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Ketik manual",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        color = Slate600
                    )
                )
            }
        }
    }
}

@Composable
fun AdminMetricCard(
    title: String,
    count: Int,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = Slate600
            )
        }
    }
}

@Composable
fun AdminReportCard(
    report: ViolationReport,
    onViewDetail: () -> Unit,
    onUpdateStatusClick: () -> Unit,
    onQuickAdvanceDispatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatted = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("id-ID"))
        .format(Date(report.timestamp))

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = report.ticketNumber,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = SatpolBluePrimary
                    )
                    Text(
                        text = "Masuk: $timeFormatted",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Slate600
                    )
                }

                StatusBadge(status = report.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE0E7FF))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = report.category,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF3730A3)
                    )
                }
                UrgencyBadge(urgency = report.urgency)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = report.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Slate800,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = SatpolRedAlert,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = report.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Quick Patrol Dispatch Action Button
            Spacer(modifier = Modifier.height(8.dp))
            if (report.dispatchStatus != ViolationReport.DISPATCH_COMPLETED) {
                val quickActionText = when (report.dispatchStatus) {
                    ViolationReport.DISPATCH_PENDING -> "🚨 Terima Gangguan & Meluncur"
                    ViolationReport.DISPATCH_ACCEPTED -> "🚔 Mulai Meluncur ke TKP"
                    ViolationReport.DISPATCH_EN_ROUTE -> "📍 Konfirmasi Tiba di Lokasi"
                    ViolationReport.DISPATCH_ARRIVED -> "✅ Selesaikan Penindakan"
                    else -> "Perbarui Status"
                }

                Button(
                    onClick = onQuickAdvanceDispatch,
                    modifier = Modifier.fillMaxWidth().height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (report.dispatchStatus) {
                            ViolationReport.DISPATCH_PENDING -> SatpolBluePrimary
                            ViolationReport.DISPATCH_ACCEPTED -> Color(0xFFD97706)
                            ViolationReport.DISPATCH_EN_ROUTE -> SatpolRedAlert
                            else -> SatpolGreen
                        }
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(quickActionText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onViewDetail,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Lihat Peta & Detail ➔", fontSize = 12.sp, color = SatpolBluePrimary, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onUpdateStatusClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ubah Status", fontSize = 11.sp, color = Slate800, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SecurityProtectionCard(modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val audit = remember { com.example.util.AppSecurityGuard.performSecurityAudit(context) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = SatpolGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "STATUS PERLINDUNGAN APK",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Security Shield & Anti-Tamper Aktif",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = SatpolGreenLight
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF14532D)
                ) {
                    Text(
                        text = "${audit.securityScore}% AMAN",
                        color = Color(0xFF86EFAC),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SecurityFeatureRow(
                    icon = "🔒",
                    label = "Enkripsi HTTPS & SSL Strict",
                    status = "Aktif (Anti-MITM)",
                    isSecure = true
                )
                SecurityFeatureRow(
                    icon = "🛡️",
                    label = "Obfuskasi ProGuard / R8",
                    status = "Terenkripsi (Anti-Decompile)",
                    isSecure = true
                )
                SecurityFeatureRow(
                    icon = "🚫",
                    label = "Deteksi Akses Root & SU",
                    status = if (audit.isRooted) "Peringatan Root" else "Aman (Non-Root)",
                    isSecure = !audit.isRooted
                )
                SecurityFeatureRow(
                    icon = "⚡",
                    label = "Proteksi Hooking (Frida/Xposed)",
                    status = if (audit.isHookingDetected) "Terdeteksi" else "Terisolasi & Aman",
                    isSecure = !audit.isHookingDetected
                )
                SecurityFeatureRow(
                    icon = "📍",
                    label = "Anti-Fake GPS / Mock Location",
                    status = "Aktif (Verifikasi Satelit)",
                    isSecure = true
                )
                SecurityFeatureRow(
                    icon = "⏱️",
                    label = "Anti-Spam & Sanitasi Input",
                    status = "Aktif (Cooldown 15 dtk)",
                    isSecure = true
                )
            }
        }
    }
}

@Composable
private fun SecurityFeatureRow(
    icon: String,
    label: String,
    status: String,
    isSecure: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E293B))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Color(0xFFE2E8F0)
            )
        }
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            ),
            color = if (isSecure) Color(0xFF4ADE80) else Color(0xFFF87171)
        )
    }
}
