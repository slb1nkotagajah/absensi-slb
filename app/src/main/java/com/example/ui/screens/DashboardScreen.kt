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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import com.example.ui.components.ChatTargetType
import com.example.ui.components.SendChatDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.scheduler.ScheduleManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.ScheduleEntity
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.AppViewModel

@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
    onNavigateToSchedules: () -> Unit,
    modifier: Modifier = Modifier
) {
    val teachers by viewModel.teachers.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val logs by viewModel.recentLogs.collectAsState()
    val groups by viewModel.whatsAppGroups.collectAsState()
    val fonnteStatus by viewModel.fonnteDeviceStatus.collectAsState()
    val isCheckingDevice by viewModel.isCheckingDevice.collectAsState()
    val deviceCheckError by viewModel.deviceCheckError.collectAsState()
    val isSchedulerActive by viewModel.isSchedulerActive.collectAsState()
    val schoolName by viewModel.schoolName.collectAsState()

    var showChatDialog by remember { mutableStateOf(false) }
    var initialChatTargetType by remember { mutableStateOf(ChatTargetType.GROUP) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_header_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pengingat Absen Pulang Guru",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = schoolName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(WhatsAppGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Alarm Pulang",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Zona Waktu: Asia/Jakarta (WIB) • ${viewModel.getCurrentJakartaDateTime()}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sistem otomatis mengirim notifikasi WhatsApp pengingat absensi kepulangan langsung ke HP guru atau grup dinas sekolah.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Fonnte Gateway Connection Status Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fonnte_status_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val isConnected = fonnteStatus?.deviceStatus == "connect" || fonnteStatus?.status == true
                            val iconTint = if (isConnected) WhatsAppGreen else Color(0xFFE11D48)
                            Icon(
                                imageVector = if (isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                                contentDescription = "Status Koneksi",
                                tint = iconTint,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Fonnte WhatsApp Gateway",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        IconButton(
                            onClick = { viewModel.checkDeviceStatus() },
                            modifier = Modifier.testTag("btn_refresh_fonnte")
                        ) {
                            if (isCheckingDevice) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = "Cek Status")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (fonnteStatus != null) {
                        val isConnected = fonnteStatus?.deviceStatus == "connect" || fonnteStatus?.status == true
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isConnected) WhatsAppGreen else Color.Red)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isConnected) "WhatsApp Terhubung (Siap Kirim)" else "Status: ${fonnteStatus?.deviceStatus ?: "Menunggu Sinkronisasi"}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = if (isConnected) WhatsAppGreen else Color.Red
                            )
                        }

                        if (!fonnteStatus?.name.isNullOrBlank()) {
                            Text(
                                text = "Nama Perangkat: ${fonnteStatus?.name} (${fonnteStatus?.device ?: "-"})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (!fonnteStatus?.quota.isNullOrBlank()) {
                            Text(
                                text = "Sisa Kuota Fonnte: ${fonnteStatus?.quota}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else if (deviceCheckError != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = deviceCheckError ?: "Error koneksi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        Text(
                            text = "Menghubungkan ke server Fonnte dengan Token...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                initialChatTargetType = ChatTargetType.GROUP
                                showChatDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_open_chat_group")
                        ) {
                            Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Chat Grup WA")
                        }

                        Button(
                            onClick = {
                                initialChatTargetType = ChatTargetType.TEACHER
                                showChatDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_open_chat_contact")
                        ) {
                            Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Chat Kontak Guru")
                        }
                    }
                }
            }
        }

        // Quick Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Guru Terdaftar",
                    value = "${teachers.size}",
                    subtitle = "${teachers.count { it.isActive }} Aktif",
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Grup WhatsApp",
                    value = "${groups.size}",
                    subtitle = "Tersambung",
                    icon = Icons.Default.Groups,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Jadwal Aktif",
                    value = "${schedules.count { it.isEnabled }}",
                    subtitle = "Dari ${schedules.size}",
                    icon = Icons.Default.Alarm,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Background Automation Info Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = WhatsAppGreen,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Layanan Pengingat Latar Belakang (Aktif)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Menggunakan AlarmClock Android & WorkManager berkala agar pesan terkirim tepat waktu.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!viewModel.canScheduleExactAlarms) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Izin Alarm Tepat Waktu (Exact Alarm) belum diizinkan oleh sistem Android HP. Pastikan izin alarm tepat waktu diizinkan agar pesan tidak tertunda.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "📌 Catatan Pengiriman Otomatis:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "• Pesan otomatis hanya terkirim jika hari ini terdaftar pada hari aktif jadwal dan jam kirim telah tiba.\n" +
                                        "• Pastikan kontak nomor WA guru valid (format: 08xx / 628xx) dan gateway Fonnte berstatus Connect.\n" +
                                        "• Klik tombol 'Uji 30 Detik' pada kartu jadwal di bawah untuk menguji alarm otomatis saat aplikasi ditutup.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Section Title: Jadwal Pengingat Absen
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar Jadwal Pengingat",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = onNavigateToSchedules,
                    modifier = Modifier.testTag("btn_see_all_schedules")
                ) {
                    Text("Kelola Jadwal")
                }
            }
        }

        if (schedules.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Belum ada jadwal tersimpan. Klik Kelola Jadwal untuk menambah.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(schedules) { schedule ->
                ScheduleDashboardItem(
                    schedule = schedule,
                    onToggle = { enabled -> viewModel.toggleSchedule(schedule, enabled) },
                    onRunNow = { viewModel.executeScheduleNow(schedule) },
                    onTestAlarm30s = { viewModel.testScheduleIn30Seconds(schedule) }
                )
            }
        }

        // Recent Logs Section
        item {
            Text(
                text = "Riwayat Pengiriman Terbaru",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (logs.isEmpty()) {
            item {
                Text(
                    text = "Belum ada riwayat pesan dikirim.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(logs.take(3)) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isSuccess = log.status == "SUKSES"
                        Icon(
                            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = log.status,
                            tint = if (isSuccess) WhatsAppGreen else Color(0xFFE11D48),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${log.recipientName} (${log.recipient})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = log.responseRaw,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }

    if (showChatDialog) {
        SendChatDialog(
            viewModel = viewModel,
            initialTargetType = initialChatTargetType,
            onDismiss = { showChatDialog = false }
        )
    }
}

@Composable
fun ScheduleDashboardItem(
    schedule: ScheduleEntity,
    onToggle: (Boolean) -> Unit,
    onRunNow: () -> Unit,
    onTestAlarm30s: () -> Unit
) {
    val nextRunSummary = ScheduleManager.getNextExecutionSummary(schedule)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("schedule_item_${schedule.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = schedule.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Jam: %02d:%02d WIB • Hari: %s".format(
                            schedule.hour,
                            schedule.minute,
                            schedule.daysOfWeek
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Switch(
                    checked = schedule.isEnabled,
                    onCheckedChange = onToggle,
                    modifier = Modifier.testTag("switch_schedule_${schedule.id}")
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Info Jadwal Eksekusi Otomatis Berikutnya
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "⏳ Jadwal Otomatis: $nextRunSummary",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Target: ${if (schedule.targetType == "ALL_TEACHERS") "Semua Guru Aktif" else schedule.targetValue}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onTestAlarm30s,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_test_30s_${schedule.id}")
                ) {
                    Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("⏰ Uji 30 Detik")
                }

                OutlinedButton(
                    onClick = onRunNow,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_run_now_${schedule.id}")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kirim Sekarang")
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TestMessageDialog(
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    var targetPhone by remember { mutableStateOf("") }
    var testMessage by remember {
        mutableStateOf("🔔 *UJI COBA NOTIFIKASI*\n\nHalo Bapak/Ibu Guru, ini adalah pesan uji coba dari sistem Absen Pulang Guru via Fonnte Gateway.\nPresensi sukses diatur! 🙏")
    }

    val isSending by viewModel.isSendingManual.collectAsState()
    val sendResult by viewModel.manualSendResult.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Tes Kirim Pesan WhatsApp",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Kirim pesan uji coba langsung ke nomor WhatsApp guru atau ID grup WhatsApp sekolah.",
                    style = MaterialTheme.typography.bodySmall
                )

                OutlinedTextField(
                    value = targetPhone,
                    onValueChange = { targetPhone = it },
                    label = { Text("Nomor WA Target (cth: 08123456789 atau ID Grup)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_test_target")
                )

                OutlinedTextField(
                    value = testMessage,
                    onValueChange = { testMessage = it },
                    label = { Text("Isi Pesan WhatsApp") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_test_message")
                )

                if (sendResult != null) {
                    val (success, detail) = sendResult!!
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (success) WhatsAppGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = if (success) "✅ Sukses: $detail" else "❌ Gagal: $detail",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp),
                            color = if (success) WhatsAppGreen else MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (targetPhone.isNotBlank() && testMessage.isNotBlank()) {
                        viewModel.sendTestMessage(targetPhone, testMessage)
                    }
                },
                enabled = !isSending && targetPhone.isNotBlank() && testMessage.isNotBlank(),
                modifier = Modifier.testTag("btn_confirm_send_test")
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mengirim...")
                } else {
                    Text("Kirim Sekarang")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_test")
            ) {
                Text("Tutup")
            }
        }
    )
}
