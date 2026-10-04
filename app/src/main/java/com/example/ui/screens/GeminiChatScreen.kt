package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.remote.ChatMessage
import com.example.data.remote.GeminiChatService
import com.example.data.remote.MessageSender
import com.example.ui.theme.satpolTextFieldColors
import com.example.ui.theme.SatpolBlueDark
import com.example.ui.theme.SatpolBluePrimary
import com.example.ui.theme.SatpolGold
import com.example.ui.theme.SatpolGoldLight
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GeminiChatScreen(
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = MessageSender.BOT,
                text = "Siap Praja Wibawa! Saya Praja Bot, Asisten Virtual Resmi Satpol PP. Saya dapat membantu menjawab pertanyaan Anda seputar Peraturan Daerah (Perda Trantibum), panduan pelaporan pelanggaran GPS, hak kerahasiaan pelapor, dan prosedur penindakan. Ada yang ingin Anda tanyakan?"
            )
        )
    }

    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val quickQuestions = listOf(
        "Bagaimana cara lapor pelanggaran GPS?",
        "Apakah identitas pelapor terjamin rahasia?",
        "Apa aturan penertiban PKL di trotoar?",
        "Berapa kontak call center darurat Satpol PP?",
        "Bagaimana aturan pemasangan reklame tanpa izin?"
    )

    fun sendUserMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank() || isLoading) return

        val userMsg = ChatMessage(sender = MessageSender.USER, text = trimmed)
        messages.add(userMsg)
        inputText = ""
        isLoading = true

        coroutineScope.launch {
            listState.animateScrollToItem(messages.size - 1)
            val result = GeminiChatService.sendMessage(messages, trimmed)
            val replyText = result.getOrElse {
                "Mohon maaf, terjadi gangguan saat menghubungi asisten AI. Silakan coba kembali."
            }
            messages.add(ChatMessage(sender = MessageSender.BOT, text = replyText))
            isLoading = false
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Chat Header Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = SatpolBlueDark)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_satpol),
                            contentDescription = "Praja Bot",
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Praja Bot AI",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SatpolGold)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "GEMINI 3.5",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Slate800
                                )
                            }
                        }
                        Text(
                            text = "Asisten Cerdas Trantibum & Layanan Masyarakat",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color(0xFFBFDBFE)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        messages.clear()
                        messages.add(
                            ChatMessage(
                                sender = MessageSender.BOT,
                                text = "Riwayat percakapan telah dibersihkan. Ada hal lain seputar Perda atau layanan Satpol PP yang ingin Anda tanyakan?"
                            )
                        )
                    },
                    modifier = Modifier.testTag("btn_clear_chat")
                ) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = "Bersihkan Percakapan",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubble(message = msg)
            }

            if (isLoading) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = SatpolBluePrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Praja Bot sedang menyusun jawaban...",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate600
                        )
                    }
                }
            }
        }

        // Quick Suggestions Horizontal Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(quickQuestions) { question ->
                AssistChip(
                    onClick = { sendUserMessage(question) },
                    label = { Text(question, fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = SatpolGold,
                            modifier = Modifier.size(13.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFFF1F5F9),
                        labelColor = Slate800
                    )
                )
            }
        }

        // Input Field & Send Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Tanyakan seputar Perda, laporan, dsb...", fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_gemini_chat"),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                colors = satpolTextFieldColors(),
                textStyle = TextStyle(
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { sendUserMessage(inputText) },
                enabled = inputText.isNotBlank() && !isLoading,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (inputText.isNotBlank() && !isLoading) SatpolBluePrimary else Color(0xFFCBD5E1)
                    )
                    .testTag("btn_send_gemini_chat")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Kirim",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.sender == MessageSender.USER
    val timeFormatted = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(SatpolBluePrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp,
                        bottomStart = if (isUser) 14.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 14.dp
                    )
                )
                .background(
                    if (isUser) SatpolBluePrimary else Color.White
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    ),
                    color = if (isUser) Color.White else Slate800
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = if (isUser) Color.White.copy(alpha = 0.7f) else Slate600,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Slate800,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
