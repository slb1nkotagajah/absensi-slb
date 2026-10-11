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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AttendanceScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val teachers by viewModel.teachers.collectAsState()
    val attendances by viewModel.todayAttendances.collectAsState()
    val todayFormatted = com.example.util.DateTimeUtils.formatJakarta("EEEE, dd MMMM yyyy") + " WIB"

    val attendanceMap = attendances.associateBy { it.teacherId }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("attendance_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Monitoring Presensi Pulang Guru",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Tanggal: $todayFormatted",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pantau status guru yang sudah maupun belum mengisi absensi pulang hari ini.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Summary Stats
        item {
            val total = teachers.size
            val sudahPulang = teachers.count {
                val att = attendanceMap[it.id]
                att?.status == "SUDAH_PULANG"
            }
            val belumPulang = total - sudahPulang

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Sudah Pulang",
                    value = "$sudahPulang",
                    subtitle = "Guru",
                    icon = Icons.Default.Check,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Belum Pulang",
                    value = "$belumPulang",
                    subtitle = "Guru",
                    icon = Icons.Default.NotificationsActive,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Broadcast to unclocked teachers button
        item {
            val unclockedTeachers = teachers.filter {
                val att = attendanceMap[it.id]
                att?.status != "SUDAH_PULANG" && it.isActive
            }
            if (unclockedTeachers.isNotEmpty()) {
                Button(
                    onClick = {
                        unclockedTeachers.forEach { teacher ->
                            val msg = "🔔 *PENGINGAT PRESENSI PULANG*\n\nYth. Bapak/Ibu ${teacher.name},\nSistem mencatat Bapak/Ibu belum melakukan absen pulang hari ini. Mohon segera mengisi presensi kepulangan sekolah. Terima kasih! 🙏"
                            viewModel.sendTestMessage(teacher.whatsappNumber, msg)
                        }
                        viewModel.showMessage("Mengirim pengingat ke ${unclockedTeachers.size} guru yang belum absen pulang...")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_remind_unclocked"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ingatkan ${unclockedTeachers.size} Guru Yang Belum Pulang")
                }
            }
        }

        item {
            Text(
                text = "Daftar Presensi Guru Hari Ini",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(teachers) { teacher ->
            val attendance = attendanceMap[teacher.id]
            val isDone = attendance?.status == "SUDAH_PULANG"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_att_${teacher.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isDone) WhatsAppGreen.copy(alpha = 0.2f) else Color(0xFFF59E0B).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDone) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (isDone) WhatsAppGreen else Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = teacher.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isDone) "Sudah Absen Pulang (Jam: ${attendance?.clockOutTime ?: "-"})" else "Belum Absen Pulang",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDone) WhatsAppGreen else Color(0xFFD97706),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val newStatus = if (isDone) "BELUM_PULANG" else "SUDAH_PULANG"
                            viewModel.markAttendance(teacher, newStatus)
                        },
                        modifier = Modifier.testTag("btn_toggle_att_${teacher.id}")
                    ) {
                        Text(if (isDone) "Batal" else "Set Pulang")
                    }
                }
            }
        }
    }
}
