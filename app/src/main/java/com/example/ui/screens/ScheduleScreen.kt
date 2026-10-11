package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.scheduler.ScheduleManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.ScheduleEntity
import com.example.ui.viewmodel.AppViewModel

@Composable
fun ScheduleScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val schedules by viewModel.schedules.collectAsState()
    var editingSchedule by remember { mutableStateOf<ScheduleEntity?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.testTag("schedule_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddingNew = true },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("fab_add_schedule")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Jadwal")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Pengelolaan Waktu & Jadwal Kirim Pesan",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Atur waktu jam dan menit, hari aktif pengiriman, serta format pesan pengingat absen pulang.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (schedules.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Belum Ada Jadwal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Klik tombol '+' di pojok kanan bawah untuk membuat jadwal pengingat baru.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(schedules) { schedule ->
                    ScheduleCard(
                        schedule = schedule,
                        onToggle = { enabled -> viewModel.toggleSchedule(schedule, enabled) },
                        onEdit = { editingSchedule = schedule },
                        onDelete = { viewModel.deleteSchedule(schedule) },
                        onRunNow = { viewModel.executeScheduleNow(schedule) },
                        onTestAlarm30s = { viewModel.testScheduleIn30Seconds(schedule) }
                    )
                }
            }
        }
    }

    if (isAddingNew) {
        ScheduleFormDialog(
            schedule = null,
            onDismiss = { isAddingNew = false },
            onSave = { title, targetType, targetValue, days, hour, minute, template, isEnabled ->
                viewModel.saveSchedule(
                    id = 0,
                    title = title,
                    targetType = targetType,
                    targetValue = targetValue,
                    daysOfWeek = days,
                    hour = hour,
                    minute = minute,
                    messageTemplate = template,
                    isEnabled = isEnabled
                )
                isAddingNew = false
            }
        )
    }

    editingSchedule?.let { schedule ->
        ScheduleFormDialog(
            schedule = schedule,
            onDismiss = { editingSchedule = null },
            onSave = { title, targetType, targetValue, days, hour, minute, template, isEnabled ->
                viewModel.saveSchedule(
                    id = schedule.id,
                    title = title,
                    targetType = targetType,
                    targetValue = targetValue,
                    daysOfWeek = days,
                    hour = hour,
                    minute = minute,
                    messageTemplate = template,
                    isEnabled = isEnabled
                )
                editingSchedule = null
            }
        )
    }
}

@Composable
fun ScheduleCard(
    schedule: ScheduleEntity,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRunNow: () -> Unit,
    onTestAlarm30s: () -> Unit
) {
    val nextRunSummary = ScheduleManager.getNextExecutionSummary(schedule)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_schedule_${schedule.id}"),
        shape = RoundedCornerShape(16.dp),
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
                        text = "⏰ Pukul %02d:%02d WIB".format(schedule.hour, schedule.minute),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Switch(
                    checked = schedule.isEnabled,
                    onCheckedChange = onToggle,
                    modifier = Modifier.testTag("switch_schedule_toggle_${schedule.id}")
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
                text = "📅 Hari: ${schedule.daysOfWeek}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "🎯 Target: ${
                    when (schedule.targetType) {
                        "ALL_TEACHERS" -> "Semua Kontak Guru Terdaftar"
                        "GROUP_WA" -> "Grup WA (${schedule.targetValue})"
                        else -> "Kontak Khusus (${schedule.targetValue})"
                    }
                }",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Text(
                    text = schedule.messageTemplate,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(10.dp),
                    maxLines = 4
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.testTag("btn_edit_${schedule.id}")) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.testTag("btn_delete_${schedule.id}")) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onTestAlarm30s,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.testTag("btn_test_schedule_30s_${schedule.id}")
                    ) {
                        Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("⏰ Uji 30 Detik")
                    }

                    OutlinedButton(
                        onClick = onRunNow,
                        modifier = Modifier.testTag("btn_execute_${schedule.id}")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kirim Sekarang")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScheduleFormDialog(
    schedule: ScheduleEntity?,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        targetType: String,
        targetValue: String,
        days: String,
        hour: Int,
        minute: Int,
        template: String,
        isEnabled: Boolean
    ) -> Unit
) {
    val allDays = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")

    var title by remember { mutableStateOf(schedule?.title ?: "Pengingat Absen Pulang") }
    var targetType by remember { mutableStateOf(schedule?.targetType ?: "ALL_TEACHERS") }
    var targetValue by remember { mutableStateOf(schedule?.targetValue ?: "") }
    var selectedDays by remember {
        mutableStateOf(
            schedule?.daysOfWeek?.split(",")?.map { it.trim() }?.toSet()
                ?: setOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat")
        )
    }
    var hour by remember { mutableIntStateOf(schedule?.hour ?: 15) }
    var minute by remember { mutableIntStateOf(schedule?.minute ?: 0) }
    var template by remember {
        mutableStateOf(
            schedule?.messageTemplate ?: "🔔 *PENGINGAT ABSEN PULANG GURU*\n\nKepada Yth. Bapak/Ibu *{nama_guru}*,\nWaktu kepulangan sekolah telah tiba pada pukul *{jam}* WIB.\nMohon untuk tidak lupa melakukan *Presensi/Absen Pulang* hari ini tanggal *{tanggal}*.\n\nLink Presensi: {link_absen}\n\nTerima kasih atas dedikasi Bapak/Ibu. Selamat beristirahat! 🙏"
        )
    }
    var isEnabled by remember { mutableStateOf(schedule?.isEnabled ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (schedule == null) "Tambah Jadwal Pengingat" else "Edit Jadwal Pengingat",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Nama/Judul Jadwal") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_schedule_title")
                    )
                }

                item {
                    Text(
                        text = "Waktu Kirim (Model ComboBox / Dropdown)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    // ComboBox Pilihan Cepat Jam Pulang
                    PresetTimeComboBox(
                        onSelectPreset = { h, m ->
                            hour = h
                            minute = m
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // ComboBox Jam dan ComboBox Menit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TimeComboBox(
                            label = "Jam WIB",
                            selectedValue = hour,
                            items = (0..23).toList(),
                            formatPattern = "Pukul %02d:00",
                            onItemSelected = { selectedHour -> hour = selectedHour },
                            modifier = Modifier.weight(1f),
                            testTag = "combobox_schedule_hour"
                        )

                        TimeComboBox(
                            label = "Menit",
                            selectedValue = minute,
                            items = (0..59).toList(),
                            formatPattern = "%02d Menit",
                            onItemSelected = { selectedMin -> minute = selectedMin },
                            modifier = Modifier.weight(1f),
                            testTag = "combobox_schedule_minute"
                        )
                    }

                    // Tampilan status waktu terpilih
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "⏰ Waktu Terpilih: %02d:%02d WIB".format(hour, minute),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Pilih Hari Kirim",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        allDays.forEach { day ->
                            val isSelected = selectedDays.contains(day)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedDays = if (isSelected) {
                                        selectedDays - day
                                    } else {
                                        selectedDays + day
                                    }
                                },
                                label = { Text(day) }
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Tujuan Pengiriman WhatsApp",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = targetType == "ALL_TEACHERS",
                                onClick = { targetType = "ALL_TEACHERS" }
                            )
                            Text("Kirim ke Semua Kontak Guru")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = targetType == "GROUP_WA",
                                onClick = { targetType = "GROUP_WA" }
                            )
                            Text("Kirim ke Grup WhatsApp")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = targetType == "SPECIFIC_CONTACTS",
                                onClick = { targetType = "SPECIFIC_CONTACTS" }
                            )
                            Text("Kirim ke Nomor Tertentu")
                        }
                    }

                    if (targetType != "ALL_TEACHERS") {
                        OutlinedTextField(
                            value = targetValue,
                            onValueChange = { targetValue = it },
                            label = {
                                Text(if (targetType == "GROUP_WA") "ID Grup WhatsApp Fonnte (contoh: 1203630248849@g.us)" else "Nomor WhatsApp (pisahkan koma)")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_schedule_target_value")
                        )
                    }
                }

                item {
                    Text(
                        text = "Pesan Apa yang Dikirim (Format Pesan)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Klik variabel untuk menyisipkan ke dalam pesan:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("{nama_guru}", "{tanggal}", "{jam}", "{link_absen}", "{sekolah}").forEach { v ->
                            SuggestionChip(
                                onClick = { template += " $v" },
                                label = { Text(v) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = template,
                        onValueChange = { template = it },
                        label = { Text("Isi Pesan Template") },
                        minLines = 4,
                        maxLines = 8,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_schedule_template")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && selectedDays.isNotEmpty() && template.isNotBlank()) {
                        val daysString = selectedDays.joinToString(",")
                        onSave(title, targetType, targetValue, daysString, hour, minute, template, isEnabled)
                    }
                },
                enabled = title.isNotBlank() && selectedDays.isNotEmpty() && template.isNotBlank(),
                modifier = Modifier.testTag("btn_save_schedule_confirm")
            ) {
                Text("Simpan Jadwal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun TimeComboBox(
    label: String,
    selectedValue: Int,
    items: List<Int>,
    formatPattern: String,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    var expanded by remember { mutableStateOf(false) }
    val displayValue = formatPattern.format(selectedValue)

    Box(modifier = modifier) {
        OutlinedTextField(
            value = displayValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Pilih $label"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            shape = RoundedCornerShape(12.dp)
        )

        // Overlay transparent click handler
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .heightIn(max = 280.dp)
                .fillMaxWidth(0.42f)
        ) {
            items.forEach { value ->
                val isSelected = value == selectedValue
                DropdownMenuItem(
                    text = {
                        Text(
                            text = formatPattern.format(value),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onItemSelected(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PresetTimeComboBox(
    onSelectPreset: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf(
        Pair("11:30 WIB (Hari Jumat Pulang Cepat)", Pair(11, 30)),
        Pair("13:00 WIB (Kepulangan Siang)", Pair(13, 0)),
        Pair("14:00 WIB (Kepulangan 14:00)", Pair(14, 0)),
        Pair("14:30 WIB (Kepulangan 14:30)", Pair(14, 30)),
        Pair("15:00 WIB (Kepulangan Reguler Standar)", Pair(15, 0)),
        Pair("15:30 WIB (Kepulangan Sore 15:30)", Pair(15, 30)),
        Pair("16:00 WIB (Kepulangan Pukul 16:00)", Pair(16, 0))
    )

    var expanded by remember { mutableStateOf(false) }
    var selectedPresetTitle by remember { mutableStateOf("Pilih Template Jam Cepat...") }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedPresetTitle,
            onValueChange = {},
            readOnly = true,
            label = { Text("Pilihan Cepat Jam Pulang (Combobox)") },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Pilih Cepat"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("combobox_preset_time"),
            shape = RoundedCornerShape(12.dp)
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .heightIn(max = 300.dp)
                .fillMaxWidth(0.9f)
        ) {
            presets.forEach { (title, time) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        selectedPresetTitle = title
                        onSelectPreset(time.first, time.second)
                        expanded = false
                    }
                )
            }
        }
    }
}

