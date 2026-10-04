package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.components.SatpolHeader
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.GeminiChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NewReportScreen
import com.example.ui.screens.PerdaGuideScreen
import com.example.ui.screens.RadarMapScreen
import com.example.ui.screens.ReportDetailScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SatpolBluePrimary
import com.example.ui.theme.Slate600
import com.example.ui.viewmodel.ReportViewModel

enum class AppTab(val title: String) {
    HOME("Beranda"),
    RADAR("Radar GPS"),
    NEW_REPORT("🚨 Lapor"),
    ADMIN("Admin"),
    CHATBOT("Praja Bot"),
    GUIDE("Panduan")
}

class MainActivity : ComponentActivity() {

    private val reportViewModel: ReportViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainApp(viewModel = reportViewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: ReportViewModel) {
    var currentTab by rememberSaveable { mutableStateOf(AppTab.HOME) }
    var selectedReportId by rememberSaveable { mutableStateOf<Long?>(null) }

    if (selectedReportId != null) {
        ReportDetailScreen(
            reportId = selectedReportId!!,
            viewModel = viewModel,
            onNavigateBack = { selectedReportId = null }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (currentTab != AppTab.NEW_REPORT) {
                    SatpolHeader(
                        title = "SATPOL PP SIAGA",
                        subtitle = when (currentTab) {
                            AppTab.HOME -> "Pelaporan Trantibum Real-Time & GPS"
                            AppTab.RADAR -> "Pusat Radar Pemantauan Titik Pelanggaran"
                            AppTab.ADMIN -> "Dashboard Admin Pengawasan & Penindakan"
                            AppTab.CHATBOT -> "Praja Bot - Asisten Virtual Gemini AI"
                            AppTab.GUIDE -> "Panduan Perda Trantibum & Hotline Mako"
                            else -> "Satuan Polisi Pamong Praja"
                        }
                    )
                }
            },
            bottomBar = {
                if (currentTab != AppTab.NEW_REPORT) {
                    NavigationBar(
                        containerColor = Color.White
                    ) {
                        NavigationBarItem(
                            selected = currentTab == AppTab.HOME,
                            onClick = { currentTab = AppTab.HOME },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Beranda"
                                )
                            },
                            label = {
                                Text(
                                    text = AppTab.HOME.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (currentTab == AppTab.HOME) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SatpolBluePrimary,
                                selectedTextColor = SatpolBluePrimary,
                                indicatorColor = Color(0xFFDBEAFE),
                                unselectedIconColor = Slate600,
                                unselectedTextColor = Slate600
                            ),
                            modifier = Modifier.testTag("nav_tab_home")
                        )

                        NavigationBarItem(
                            selected = currentTab == AppTab.RADAR,
                            onClick = { currentTab = AppTab.RADAR },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Radar,
                                    contentDescription = "Radar GPS"
                                )
                            },
                            label = {
                                Text(
                                    text = AppTab.RADAR.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (currentTab == AppTab.RADAR) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SatpolBluePrimary,
                                selectedTextColor = SatpolBluePrimary,
                                indicatorColor = Color(0xFFDBEAFE),
                                unselectedIconColor = Slate600,
                                unselectedTextColor = Slate600
                            ),
                            modifier = Modifier.testTag("nav_tab_radar")
                        )

                        NavigationBarItem(
                            selected = currentTab == AppTab.NEW_REPORT,
                            onClick = { currentTab = AppTab.NEW_REPORT },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.AddLocationAlt,
                                    contentDescription = "Lapor GPS"
                                )
                            },
                            label = {
                                Text(
                                    text = AppTab.NEW_REPORT.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (currentTab == AppTab.NEW_REPORT) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SatpolBluePrimary,
                                selectedTextColor = SatpolBluePrimary,
                                indicatorColor = Color(0xFFDBEAFE),
                                unselectedIconColor = Slate600,
                                unselectedTextColor = Slate600
                            ),
                            modifier = Modifier.testTag("nav_tab_new_report")
                        )

                        NavigationBarItem(
                            selected = currentTab == AppTab.ADMIN,
                            onClick = { currentTab = AppTab.ADMIN },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Admin"
                                )
                            },
                            label = {
                                Text(
                                    text = AppTab.ADMIN.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (currentTab == AppTab.ADMIN) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SatpolBluePrimary,
                                selectedTextColor = SatpolBluePrimary,
                                indicatorColor = Color(0xFFDBEAFE),
                                unselectedIconColor = Slate600,
                                unselectedTextColor = Slate600
                            ),
                            modifier = Modifier.testTag("nav_tab_admin")
                        )

                        NavigationBarItem(
                            selected = currentTab == AppTab.CHATBOT,
                            onClick = { currentTab = AppTab.CHATBOT },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Praja Bot AI"
                                )
                            },
                            label = {
                                Text(
                                    text = AppTab.CHATBOT.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (currentTab == AppTab.CHATBOT) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SatpolBluePrimary,
                                selectedTextColor = SatpolBluePrimary,
                                indicatorColor = Color(0xFFDBEAFE),
                                unselectedIconColor = Slate600,
                                unselectedTextColor = Slate600
                            ),
                            modifier = Modifier.testTag("nav_tab_chatbot")
                        )
                    }
                }
            }
        ) { innerPadding ->
            BackHandler(enabled = currentTab != AppTab.HOME) {
                currentTab = AppTab.HOME
            }

            when (currentTab) {
                AppTab.HOME -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToNewReport = { currentTab = AppTab.NEW_REPORT },
                        onNavigateToDetail = { reportId -> selectedReportId = reportId },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                AppTab.RADAR -> {
                    RadarMapScreen(
                        viewModel = viewModel,
                        onNavigateToDetail = { reportId -> selectedReportId = reportId },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                AppTab.NEW_REPORT -> {
                    NewReportScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentTab = AppTab.HOME },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                AppTab.ADMIN -> {
                    AdminDashboardScreen(
                        viewModel = viewModel,
                        onNavigateToDetail = { reportId -> selectedReportId = reportId },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                AppTab.CHATBOT -> {
                    GeminiChatScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                AppTab.GUIDE -> {
                    PerdaGuideScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
