package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ViolationReport
import com.example.ui.theme.SatpolBlueDark
import com.example.ui.theme.SatpolBluePrimary
import com.example.ui.theme.SatpolGold
import com.example.ui.theme.SatpolGreen
import com.example.ui.theme.SatpolRedAlert
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import com.example.util.LocationHelper

@Composable
fun PatrolLiveTrackingCard(
    report: ViolationReport,
    isOfficer: Boolean,
    onAdvanceDispatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Pulsing animation for active en-route state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header: Live Dispatch Badge like Gojek/Grab
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (report.dispatchStatus) {
                                    ViolationReport.DISPATCH_COMPLETED -> SatpolGreen
                                    ViolationReport.DISPATCH_PENDING -> SatpolGold
                                    else -> SatpolRedAlert
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LIVE DISPATCH PATROLI SATPOL PP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        color = SatpolBluePrimary
                    )
                }

                // ETA Pill
                if (report.dispatchStatus == ViolationReport.DISPATCH_ACCEPTED || report.dispatchStatus == ViolationReport.DISPATCH_EN_ROUTE) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ETA: ~${report.dispatchEtaMinutes.coerceAtLeast(1)} Menit",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        )
                    }
                } else if (report.dispatchStatus == ViolationReport.DISPATCH_ARRIVED) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFDCFCE7))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Petugas di Lokasi",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Visual Tracking Map (Simulated Live Route like Gojek / Grab)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
            ) {
                // Background street lines & radar circles
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height

                    // Grid street road lines
                    drawLine(
                        color = Color(0xFF1E293B),
                        start = Offset(0f, h * 0.5f),
                        end = Offset(w, h * 0.5f),
                        strokeWidth = 32f
                    )
                    drawLine(
                        color = Color(0xFF334155),
                        start = Offset(0f, h * 0.5f),
                        end = Offset(w, h * 0.5f),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                    )

                    // Target incident point (Right side)
                    val targetX = w * 0.82f
                    val targetY = h * 0.5f

                    // Officer vehicle point (Progresses from Left to Right)
                    val officerProgress = when (report.dispatchStatus) {
                        ViolationReport.DISPATCH_PENDING -> 0.15f
                        ViolationReport.DISPATCH_ACCEPTED -> 0.25f
                        ViolationReport.DISPATCH_EN_ROUTE -> 0.60f
                        ViolationReport.DISPATCH_ARRIVED, ViolationReport.DISPATCH_COMPLETED -> 0.80f
                        else -> 0.20f
                    }
                    val officerX = w * officerProgress
                    val officerY = h * 0.5f

                    // Route path line connecting officer to target
                    drawLine(
                        color = SatpolGold,
                        start = Offset(officerX, officerY),
                        end = Offset(targetX, targetY),
                        strokeWidth = 4f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )

                    // Pulse glow around officer marker
                    if (report.dispatchStatus == ViolationReport.DISPATCH_EN_ROUTE) {
                        drawCircle(
                            color = SatpolBluePrimary.copy(alpha = 0.35f),
                            radius = 24f * pulseScale,
                            center = Offset(officerX, officerY)
                        )
                    }
                }

                // Start Marker: Officer Vehicle
                val officerAlignment = when (report.dispatchStatus) {
                    ViolationReport.DISPATCH_PENDING -> 0.15f
                    ViolationReport.DISPATCH_ACCEPTED -> 0.25f
                    ViolationReport.DISPATCH_EN_ROUTE -> 0.60f
                    ViolationReport.DISPATCH_ARRIVED, ViolationReport.DISPATCH_COMPLETED -> 0.78f
                    else -> 0.20f
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = (officerAlignment * 240).dp)
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SatpolBluePrimary)
                        .border(2.dp, SatpolGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "Mobil Patroli",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // End Marker: Incident Location (Red Pin)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 22.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SatpolRedAlert)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Lokasi Kejadian",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Overlay Text Status inside map
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (report.dispatchStatus) {
                            ViolationReport.DISPATCH_PENDING -> "📡 Menunggu konfirmasi regu patroli..."
                            ViolationReport.DISPATCH_ACCEPTED -> "📋 Patroli bersiap menuju lokasi..."
                            ViolationReport.DISPATCH_EN_ROUTE -> "🚔 Meluncur: Jl. Sultan Ibrahim (~850m)"
                            ViolationReport.DISPATCH_ARRIVED -> "📍 Petugas sudah tiba di TKP"
                            ViolationReport.DISPATCH_COMPLETED -> "✅ Penertiban selesai ditangani"
                            else -> "Status patroli aktif"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Officer Card (Driver Profile Card Style)
            val officerName = report.assignedOfficerName ?: "Regu Reaksi Cepat Satpol PP"
            val officerSquad = report.assignedOfficerSquad ?: "Unit Patroli Trantibum Inhu"
            val officerVehicle = report.assignedOfficerVehicle ?: "Mobil Patroli Dalmas (BM 1002 IN)"
            val officerPhone = report.assignedOfficerPhone ?: "0812-7890-1122"

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Officer Avatar
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(SatpolBlueDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = SatpolGold,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = officerName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Slate800
                    )
                    Text(
                        text = "$officerSquad • $officerVehicle",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Slate600
                    )
                }

                // Call Officer Button
                OutlinedButton(
                    onClick = { LocationHelper.callEmergencyNumber(context, officerPhone) },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Telepon Petugas",
                        tint = SatpolGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hubungi", fontSize = 11.sp, color = SatpolGreen, fontWeight = FontWeight.Bold)
                }
            }

            // Stepped Timeline (1. Diterima -> 2. Menuju Lokasi -> 3. Tiba di TKP -> 4. Selesai)
            Spacer(modifier = Modifier.height(14.dp))
            DispatchStepIndicator(currentStatus = report.dispatchStatus)

            // Officer Dispatch Controls (If Officer is viewing, they can progress the order)
            if (isOfficer) {
                Spacer(modifier = Modifier.height(12.dp))
                val nextActionLabel = when (report.dispatchStatus) {
                    ViolationReport.DISPATCH_PENDING -> "Terima Tugas & Bersiap Meluncur"
                    ViolationReport.DISPATCH_ACCEPTED -> "Mulai Berangkat (Meluncur ke Lokasi)"
                    ViolationReport.DISPATCH_EN_ROUTE -> "Konfirmasi Tiba di Lokasi Kejadian"
                    ViolationReport.DISPATCH_ARRIVED -> "Selesaikan Penindakan (Tertibkan)"
                    else -> "Tugas Telah Selesai"
                }

                if (report.dispatchStatus != ViolationReport.DISPATCH_COMPLETED) {
                    Button(
                        onClick = onAdvanceDispatch,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_advance_dispatch"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SatpolBluePrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = nextActionLabel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DispatchStepIndicator(currentStatus: String) {
    val steps = listOf(
        "Diterima" to (currentStatus != ViolationReport.DISPATCH_PENDING),
        "Meluncur" to (currentStatus == ViolationReport.DISPATCH_EN_ROUTE || currentStatus == ViolationReport.DISPATCH_ARRIVED || currentStatus == ViolationReport.DISPATCH_COMPLETED),
        "Tiba di TKP" to (currentStatus == ViolationReport.DISPATCH_ARRIVED || currentStatus == ViolationReport.DISPATCH_COMPLETED),
        "Selesai" to (currentStatus == ViolationReport.DISPATCH_COMPLETED)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, (label, isCompleted) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) SatpolGreen else Slate200),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isCompleted) Slate800 else Slate600
                )
            }

            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(2.dp)
                        .background(if (steps[index + 1].second) SatpolGreen else Slate200)
                )
            }
        }
    }
}
