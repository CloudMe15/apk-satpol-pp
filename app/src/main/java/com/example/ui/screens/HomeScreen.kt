package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.ui.text.TextStyle
import com.example.data.auth.AuthManager
import com.example.ui.theme.satpolTextFieldColors
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Search
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.Surface
import androidx.compose.material3.AlertDialog
import com.example.data.remote.RealtimeCloudSyncService
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ViolationReport
import com.example.ui.components.GpsLiveCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.UrgencyBadge
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
import com.example.util.LocationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: ReportViewModel,
    onNavigateToNewReport: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val reports by viewModel.filteredReports.collectAsStateWithLifecycle()
    val allReports by viewModel.allReports.collectAsStateWithLifecycle()
    val currentGps by viewModel.currentGps.collectAsStateWithLifecycle()
    val isFetchingGps by viewModel.isFetchingGps.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedStatus by viewModel.statusFilter.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.categoryFilter.collectAsStateWithLifecycle()

    val isOfficerLoggedIn by AuthManager.isOfficerLoggedIn.collectAsStateWithLifecycle()
    val isOfficerOnline by AuthManager.isOfficerOnline.collectAsStateWithLifecycle()
    val activeOfficer by AuthManager.activeOfficer.collectAsStateWithLifecycle()
    val cloudOfficerOnline by AuthManager.cloudOfficerOnline.collectAsStateWithLifecycle()
    val cloudOfficerName by AuthManager.cloudOfficerName.collectAsStateWithLifecycle()
    val cloudOfficerSquad by AuthManager.cloudOfficerSquad.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val appUpdateInfo by RealtimeCloudSyncService.appUpdateInfo.collectAsStateWithLifecycle()

    val isOnlineNow = if (isOfficerLoggedIn) isOfficerOnline else cloudOfficerOnline
    val currentOfficerName = if (isOfficerLoggedIn) (activeOfficer?.fullName ?: "Personil Satpol PP") else (cloudOfficerName ?: "Bripka Danu Prasetyo")
    val currentOfficerSquad = if (isOfficerLoggedIn) (activeOfficer?.squadName ?: "Regu Reaksi Cepat") else (cloudOfficerSquad ?: "Unit Patroli Reaksi Cepat (UPRC)")

    val pendingCount = allReports.count { it.status == ViolationReport.STATUS_PENDING }
    val inProgressCount = allReports.count { it.status == ViolationReport.STATUS_IN_PROGRESS }
    val resolvedCount = allReports.count { it.status == ViolationReport.STATUS_RESOLVED }

    val activeDispatchedReport = allReports.firstOrNull {
        it.dispatchStatus == ViolationReport.DISPATCH_EN_ROUTE ||
                it.dispatchStatus == ViolationReport.DISPATCH_ACCEPTED ||
                it.dispatchStatus == ViolationReport.DISPATCH_ARRIVED
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // HERO BANNER: Tombol Lapor Utama yang Sangat Jelas & Mudah Ditemukan
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SatpolBluePrimary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = SatpolGold,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LAYANAN PENGADUAN TRANTIBUM",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = SatpolGoldLight
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Ada Gangguan Ketertiban di Sekitar Anda?",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Laporkan PKL liar, ternak berkeliaran, parkir sembarangan, reklame ilegal, miras, atau pelanggaran Perda.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = Color(0xFFE2E8F0)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onNavigateToNewReport,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_hero_buat_laporan"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SatpolRedAlert),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAlert,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🚨 KLIK DI SINI: BUAT LAPORAN BARU",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Status Petugas Piket: Online vs Offline (Langsung Terlihat oleh Warga & Terhubung Cloud)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOnlineNow) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isOnlineNow) Color(0xFF16A34A) else Color(0xFFD97706)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (isOnlineNow) SatpolGreen else Color(0xFFD97706))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOnlineNow) {
                                    "🟢 PETUGAS PATROLI SEDANG ONLINE & SIAGA"
                                } else {
                                    "🟡 STATUS: PETUGAS SEDANG LEPAS PIKET / OFFLINE"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isOnlineNow) Color(0xFF166534) else Color(0xFF92400E)
                            )
                            Text(
                                text = if (isOnlineNow) {
                                    "Petugas Aktif: $currentOfficerName ($currentOfficerSquad) • Siaga menerima laporan"
                                } else {
                                    "Belum ada petugas piket yang online. Laporan warga tetap dapat dikirimkan & akan diproses saat dinas piket berikutnya."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = if (isOnlineNow) Color(0xFF15803D) else Color(0xFFB45309)
                            )
                        }
                    }
                }
            }

            // Real-Time Multi-HP Cloud Sync Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isSyncing) SatpolGold else SatpolGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSyncing) "Menyinkronkan Cloud Real-Time..." else "Cloud Real-Time Aktif (Multi-HP)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = Slate600
                        )
                    }

                    TextButton(
                        onClick = { viewModel.manualSync() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync Cloud",
                            modifier = Modifier.size(14.dp),
                            tint = SatpolBluePrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sync Sekarang",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SatpolBluePrimary
                        )
                    }
                }
            }
            // Active Dispatch Live Tracking Alert (Ojek Online style order in-progress banner)
            if (activeDispatchedReport != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { onNavigateToDetail(activeDispatchedReport.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SatpolBlueDark),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(SatpolGold)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "PATROLI SIAGA MENUJU LOKASI",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = SatpolGoldLight
                                    )
                                }

                                Text(
                                    text = "ETA: ~${activeDispatchedReport.dispatchEtaMinutes} mnt",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = activeDispatchedReport.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = "Petugas: ${activeDispatchedReport.assignedOfficerName ?: "Regu Reaksi Cepat Satpol PP"}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Color(0xFFCBD5E1)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { onNavigateToDetail(activeDispatchedReport.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SatpolGold),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Lacak Posisi Petugas (Live) ➔",
                                        color = Slate800,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Live GPS Status Banner
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    GpsLiveCard(
                        gps = currentGps,
                        isFetching = isFetchingGps,
                        onRefreshGps = { viewModel.refreshLocation(context) }
                    )
                }
            }

            // Quick Call Center SOS Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SatpolBlueDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LAYANAN PENGADUAN 24 JAM",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp
                                ),
                                color = SatpolGoldLight
                            )
                            Text(
                                text = "Satpol PP Siaga Reaksi Cepat",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Laporan darurat ketertiban & ketenteraman umum",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Color(0xFFBFDBFE)
                            )
                        }

                        Button(
                            onClick = { LocationHelper.callEmergencyNumber(context, "112") },
                            colors = ButtonDefaults.buttonColors(containerColor = SatpolRedAlert),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_sos_call")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Panggil 112",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SOS 112",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Stats Dashboard
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBox(
                        title = "Menunggu",
                        count = pendingCount,
                        color = SatpolGold,
                        bgColor = SatpolGoldLight,
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "Diproses",
                        count = inProgressCount,
                        color = SatpolBluePrimary,
                        bgColor = Color(0xFFDBEAFE),
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "Ditertibkan",
                        count = resolvedCount,
                        color = SatpolGreen,
                        bgColor = SatpolGreenLight,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("search_reports_input"),
                    placeholder = {
                        Text(
                            text = "Cari tiket, lokasi, atau jenis pelanggaran...",
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari",
                            tint = Slate600
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Hapus",
                                    tint = Slate600
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = satpolTextFieldColors(),
                    textStyle = TextStyle(
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }

            // Status Filter Chips
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedStatus == null,
                            onClick = { viewModel.setStatusFilter(null) },
                            label = { Text("Semua Status (${allReports.size})", fontSize = 12.sp) },
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
                            label = { Text("Menunggu ($pendingCount)", fontSize = 12.sp) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedStatus == ViolationReport.STATUS_IN_PROGRESS,
                            onClick = { viewModel.setStatusFilter(ViolationReport.STATUS_IN_PROGRESS) },
                            label = { Text("Diproses ($inProgressCount)", fontSize = 12.sp) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedStatus == ViolationReport.STATUS_RESOLVED,
                            onClick = { viewModel.setStatusFilter(ViolationReport.STATUS_RESOLVED) },
                            label = { Text("Selesai ($resolvedCount)", fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Laporan Real-Time Terkini",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Slate800
                    )
                    Text(
                        text = "${reports.size} Laporan",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate600
                    )
                }
            }

            // Reports List
            if (reports.isEmpty()) {
                item {
                    EmptyReportsCard(
                        isFiltered = searchQuery.isNotEmpty() || selectedStatus != null,
                        onResetFilter = {
                            viewModel.setSearchQuery("")
                            viewModel.setStatusFilter(null)
                        }
                    )
                }
            } else {
                items(reports, key = { it.id }) { report ->
                    ViolationReportItemCard(
                        report = report,
                        currentGps = currentGps,
                        onClick = { onNavigateToDetail(report.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Floating Action Button - Selalu melayang jelas di atas bilah navigasi
        FloatingActionButton(
            onClick = onNavigateToNewReport,
            containerColor = SatpolRedAlert,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 16.dp)
                .testTag("fab_new_report")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = "Lapor Pelanggaran",
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🚨 BUAT LAPORAN",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }
        }

        // In-App Update Notification Dialog (Mendukung Catatan Rilis & Wajib/Opsional)
        if (appUpdateInfo != null && appUpdateInfo!!.hasUpdate) {
            val update = appUpdateInfo!!
            AlertDialog(
                onDismissRequest = {
                    if (!update.forceUpdate) {
                        RealtimeCloudSyncService.dismissUpdate()
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = SatpolBluePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (update.forceUpdate)
                                "Pembaruan Wajib (${update.latestVersionName})"
                            else
                                "Pembaruan Tersedia (${update.latestVersionName})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate800
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = if (update.forceUpdate)
                                "Versi aplikasi Anda sudah usang. Mohon perbarui ke versi terbaru untuk tetap dapat menggunakan aplikasi Satpol PP Siaga."
                            else
                                "Tersedia pembaruan versi baru untuk aplikasi Satpol PP Siaga.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )

                        // Kotak Catatan Pembaruan (Release Notes)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "📋 Catatan Pembaruan:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SatpolBluePrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = update.updateNotes.ifBlank { "Peningkatan performa dan sinkronisasi real-time." },
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = Slate800
                                )
                            }
                        }

                        Text(
                            text = "Aplikasi akan otomatis mengunduh APK terbaru dan memandu instalasi.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            try {
                                com.example.util.ApkDownloadInstallManager.startDownloadAndInstall(
                                    context = context,
                                    downloadUrl = update.downloadUrl,
                                    versionName = update.latestVersionName
                                )
                            } catch (_: Exception) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(update.downloadUrl))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }
                            if (!update.forceUpdate) {
                                RealtimeCloudSyncService.dismissUpdate()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SatpolBluePrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Update Sekarang", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    if (!update.forceUpdate) {
                        TextButton(onClick = { RealtimeCloudSyncService.dismissUpdate() }) {
                            Text("Nanti Saja")
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun StatBox(
    title: String,
    count: Int,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Slate800,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ViolationReportItemCard(
    report: ViolationReport,
    currentGps: com.example.util.GpsCoordinate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val distanceText = if (currentGps != null) {
        val dist = LocationHelper.calculateDistanceMeters(
            currentGps.latitude,
            currentGps.longitude,
            report.latitude,
            report.longitude
        )
        LocationHelper.formatDistance(dist)
    } else null

    val timeFormatted = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
        .format(Date(report.timestamp))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("report_item_${report.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header with Ticket & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = report.ticketNumber,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = SatpolBluePrimary
                )

                StatusBadge(status = report.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Category & Urgency
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                        color = Color(0xFF3730A3),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                UrgencyBadge(urgency = report.urgency)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = report.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Slate800,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Description preview
            Text(
                text = report.description,
                style = MaterialTheme.typography.bodySmall,
                color = Slate600,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // GPS Address & Distance
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = SatpolRedAlert,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = report.address,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Slate800,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (distanceText != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NearMe,
                            contentDescription = null,
                            tint = SatpolBluePrimary,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = distanceText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = SatpolBluePrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Timestamp and Reporter info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Slate600
                )

                Text(
                    text = if (report.isAnonymous) "Pelapor: Anonim" else "Pelapor: ${report.reporterName}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Slate600
                )
            }
        }
    }
}

@Composable
fun EmptyReportsCard(
    isFiltered: Boolean,
    onResetFilter: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = SatpolBluePrimary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isFiltered) "Tidak ada laporan yang sesuai kriteria" else "Belum ada laporan pelanggaran",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Slate800
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isFiltered) "Coba atur ulang pencarian atau filter status Anda" else "Laporkan pelanggaran ketertiban umum di sekitar Anda menggunakan tombol di bawah",
                style = MaterialTheme.typography.bodySmall,
                color = Slate600
            )
            if (isFiltered) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onResetFilter,
                    colors = ButtonDefaults.buttonColors(containerColor = SatpolBluePrimary)
                ) {
                    Text("Reset Filter")
                }
            }
        }
    }
}
