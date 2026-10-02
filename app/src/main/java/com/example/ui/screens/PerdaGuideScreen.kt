package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SatpolBlueDark
import com.example.ui.theme.SatpolBluePrimary
import com.example.ui.theme.SatpolGold
import com.example.ui.theme.SatpolGreen
import com.example.ui.theme.SatpolGreenLight
import com.example.ui.theme.SatpolRedAlert
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import com.example.util.LocationHelper

@Composable
fun PerdaGuideScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Emergency Call Center Directory Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SatpolBlueDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SatpolRedAlert),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "KONTAK DARURAT & CALL CENTER",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp
                                ),
                                color = Color(0xFFFEF08A)
                            )
                            Text(
                                text = "Mako Satpol PP Indragiri Hulu",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    ContactRow(
                        title = "Layanan Panggilan Darurat Nasional",
                        number = "112",
                        description = "Bebas pulsa 24 Jam tanggap darurat trantibum",
                        onCall = { LocationHelper.callEmergencyNumber(context, "112") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ContactRow(
                        title = "Posko Reaksi Cepat Satpol PP Inhu",
                        number = "+62 813-7443-5636",
                        description = "Unit Patroli Lapangan & Pengaduan Cepat",
                        onCall = { LocationHelper.callEmergencyNumber(context, "081374435636") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ContactRow(
                        title = "Piket Pengawasan Trantibum",
                        number = "+62 852-7268-3165",
                        description = "Sekretariat Pengaduan & Layanan Publik",
                        onCall = { LocationHelper.callEmergencyNumber(context, "085272683165") }
                    )
                }
            }
        }

        // Whistleblower Legal Protection Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SatpolGreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = SatpolGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Jaminan Kerahasiaan & Keamanan Pelapor",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate800
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Sesuai dengan ketentuan peraturan perundang-undangan, identitas setiap warga masyarakat yang melaporkan dugaan pelanggaran Perda dijamin kerahasiaannya oleh Satuan Polisi Pamong Praja. Pelapor dilindungi dari segala bentuk intimidasi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Perda Categories Guide
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE0E7FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = null,
                                tint = SatpolBluePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Pedoman Peraturan Daerah (Perda Trantibum)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate800
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    PerdaRuleItem(
                        title = "1. Tertib Jalan, Trotoar & Fasilitas Umum",
                        desc = "Dilarang berjualan atau menempatkan barang dagangan di atas trotoar yang mengganggu hak pejalan kaki dan kelancaran arus lalu lintas."
                    )

                    PerdaRuleItem(
                        title = "2. Tertib Bangunan & Tata Ruang",
                        desc = "Setiap pembangunan gedung/kios harus memiliki Persetujuan Bangunan Gedung (PBG/IMB) dan tidak melanggar Garis Sempadan Sungai/Jalan."
                    )

                    PerdaRuleItem(
                        title = "3. Tertib Lingkungan & Sampah",
                        desc = "Dilarang membuang sampah atau limbah sembarangan di bantaran sungai, parit, taman kota, atau tempat yang bukan TPS resmi."
                    )

                    PerdaRuleItem(
                        title = "4. Tertib Reklame & Media Promosi",
                        desc = "Pemasangan baliho, spanduk, dan pamflet wajib memiliki izin resmi dan dilarang dipaku pada batang pohon atau tiang rambu lalu lintas."
                    )

                    PerdaRuleItem(
                        title = "5. Ketenteraman Masyarakat & Asusila",
                        desc = "Penertiban peredaran miras tanpa izin, praktik perjudian, jam malam hiburan umum, serta razia pelajar berseragam pada jam sekolah."
                    )
                }
            }
        }
    }
}

@Composable
fun ContactRow(
    title: String,
    number: String,
    description: String,
    onCall: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = number,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xFF67E8F9)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Color(0xFF94A3B8)
            )
        }

        Button(
            onClick = onCall,
            colors = ButtonDefaults.buttonColors(containerColor = SatpolGold),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Panggil",
                tint = Slate800,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Panggil", color = Slate800, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}

@Composable
fun PerdaRuleItem(
    title: String,
    desc: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = SatpolBluePrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall,
            color = Slate600,
            lineHeight = 18.sp
        )
    }
}
