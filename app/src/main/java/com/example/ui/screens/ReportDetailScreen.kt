package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.auth.AuthManager
import com.example.data.model.ViolationReport
import com.example.ui.components.MiniMapVisual
import com.example.ui.components.PatrolLiveTrackingCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.UrgencyBadge
import com.example.ui.theme.SatpolBluePrimary
import com.example.ui.theme.SatpolGold
import com.example.ui.theme.SatpolGreen
import com.example.ui.theme.SatpolRedAlert
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import com.example.ui.viewmodel.ReportViewModel
import com.example.util.LocationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    reportId: Long,
    viewModel: ReportViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler { onNavigateBack() }

    val allReports by viewModel.allReports.collectAsStateWithLifecycle()
    val report = allReports.find { it.id == reportId }
    val isOfficerLoggedIn by AuthManager.isOfficerLoggedIn.collectAsStateWithLifecycle()

    var showOfficerActionDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var officerStatusSelection by remember(report?.status) { mutableStateOf(report?.status ?: ViolationReport.STATUS_IN_PROGRESS) }
    var officerNotesInput by remember(report?.officerNotes) { mutableStateOf(report?.officerNotes ?: "") }

    if (report == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Laporan tidak ditemukan.")
        }
        return
    }

    val timeFormatted = SimpleDateFormat("EEEE, dd MMMM yyyy - HH:mm", Locale.forLanguageTag("id-ID"))
        .format(Date(report.timestamp))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detail Laporan #${report.ticketNumber}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_back_from_detail")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            LocationHelper.shareReport(
                                context = context,
                                ticketNumber = report.ticketNumber,
                                title = report.title,
                                address = report.address,
                                lat = report.latitude,
                                lng = report.longitude
                            )
                        },
                        modifier = Modifier.testTag("btn_share_report")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Bagikan",
                            tint = Color.White
                        )
                    }
                    if (isOfficerLoggedIn) {
                        IconButton(
                            onClick = { showDeleteConfirmDialog = true },
                            modifier = Modifier.testTag("btn_delete_report")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SatpolBluePrimary)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = report.ticketNumber,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = SatpolBluePrimary
                            )
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
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = report.category,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF3730A3)
                                )
                            }
                            UrgencyBadge(urgency = report.urgency)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = report.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Slate800
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = timeFormatted,
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate600
                        )
                    }
                }
            }

            // Live Ojek-Online Style Patrol Tracking & Dispatch Card
            item {
                PatrolLiveTrackingCard(
                    report = report,
                    isOfficer = isOfficerLoggedIn,
                    onAdvanceDispatch = { viewModel.advanceDispatch(report) }
                )
            }

            // Status Progress Pipeline
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tahapan Penanganan Trantibum",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Slate800
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        val steps = listOf(
                            Pair("Laporan Masuk", true),
                            Pair("Verifikasi Posko", report.status != ViolationReport.STATUS_PENDING),
                            Pair("Regu Bergerak", report.status == ViolationReport.STATUS_IN_PROGRESS || report.status == ViolationReport.STATUS_RESOLVED),
                            Pair("Tuntas Ditertibkan", report.status == ViolationReport.STATUS_RESOLVED)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            steps.forEachIndexed { idx, step ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (step.second) SatpolBluePrimary else Color(0xFFE2E8F0)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (step.second) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "${idx + 1}",
                                                color = Slate600,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = step.first,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = if (step.second) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (step.second) Slate800 else Slate600,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Description
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Rincian Kejadian Pelanggaran",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Slate800
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = report.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate800,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // GPS & Location Details
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = SatpolRedAlert,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Lokasi Kejadian & Koordinat GPS",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Slate800
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = report.address,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Slate800
                        )

                        if (report.landmark.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Patokan: ${report.landmark}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = String.format(Locale.getDefault(), "Koordinat GPS: %.6f, %.6f (Akurasi ±%.0fm)", report.latitude, report.longitude, report.accuracyMeters),
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate600
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        MiniMapVisual(
                            latitude = report.latitude,
                            longitude = report.longitude,
                            label = report.title
                        )
                    }
                }
            }

            // Attached Photo Proof
            if (!report.photoUri.isNullOrBlank()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Foto Bukti Pelanggaran Terlampir",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Slate800
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF1F5F9))
                            ) {
                                AsyncImage(
                                    model = report.photoUri,
                                    contentDescription = "Foto Pelanggaran",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            // Officer Dispatch Notes
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = SatpolBluePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Tindak Lanjut Petugas Satpol PP",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Slate800
                                )
                            }

                            if (isOfficerLoggedIn) {
                                IconButton(
                                    onClick = { showOfficerActionDialog = true },
                                    modifier = Modifier.testTag("btn_edit_officer_action")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Perbarui Status Tindakan",
                                        tint = SatpolBluePrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (!report.officerNotes.isNullOrBlank()) {
                            Text(
                                text = report.officerNotes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate800
                            )
                        } else {
                            Text(
                                text = "Belum ada catatan tindak lanjut dari regu piket/patroli.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (isOfficerLoggedIn) {
                            Button(
                                onClick = { showOfficerActionDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SatpolBluePrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Mode Petugas: Update Status Penindakan")
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Slate600,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Hak Akses Terbatas: Hanya aparatur Satpol PP terotentikasi yang berwenang memperbarui status dan catatan penindakan.",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = Slate600
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Reporter Information Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Informasi Pelapor",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Slate800
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (report.isAnonymous) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = SatpolGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Identitas Dilindungi (Laporan Anonim)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = SatpolGreen
                                )
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Slate600,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = report.reporterName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = Slate800
                                )
                            }

                            if (report.reporterPhone.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = Slate600,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = report.reporterPhone,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate600
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Officer Action Dialog
    if (showOfficerActionDialog) {
        AlertDialog(
            onDismissRequest = { showOfficerActionDialog = false },
            title = {
                Text(
                    text = "Update Status Tindakan Petugas",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Pilih Status Laporan:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Slate600
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val statuses = listOf(
                        ViolationReport.STATUS_PENDING to "Menunggu Verifikasi",
                        ViolationReport.STATUS_IN_PROGRESS to "Patroli Menuju Lokasi / Diproses",
                        ViolationReport.STATUS_RESOLVED to "Selesai Ditertibkan",
                        ViolationReport.STATUS_REJECTED to "Ditolak / Tidak Valid"
                    )

                    statuses.forEach { (st, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (officerStatusSelection == st) Color(0xFFDBEAFE) else Color.Transparent)
                                .clickable { officerStatusSelection = st }
                                .padding(vertical = 8.dp, horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (officerStatusSelection == st) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (officerStatusSelection == st) SatpolBluePrimary else Slate800
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = officerNotesInput,
                        onValueChange = { officerNotesInput = it },
                        label = { Text("Catatan Penindakan Lapangan") },
                        placeholder = { Text("Rincian tindakan: unit patroli yang turun, hasil teguran, sitaan barang...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateOfficerAction(
                            reportId = report.id,
                            newStatus = officerStatusSelection,
                            notes = officerNotesInput
                        )
                        showOfficerActionDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SatpolBluePrimary)
                ) {
                    Text("Simpan Perubahan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOfficerActionDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Hapus Laporan Ini?") },
            text = { Text("Apakah Anda yakin ingin menghapus data laporan #${report.ticketNumber} secara permanen?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteReport(report.id)
                        showDeleteConfirmDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SatpolRedAlert)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
