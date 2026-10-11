package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.TeacherEntity
import com.example.data.local.entity.WhatsAppGroupEntity
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.AppViewModel

enum class ChatTargetType(val label: String, val description: String) {
    GROUP("Grup WhatsApp", "Kirim ke grup diskusi sekolah / dinas"),
    TEACHER("Kontak Guru", "Pilih guru dari kontak terdaftar"),
    ALL_TEACHERS("Semua Guru (Broadcast)", "Kirim serentak ke semua guru aktif"),
    CUSTOM_PHONE("Nomor Kontak Bebas", "Kirim ke nomor WA tertentu")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SendChatDialog(
    viewModel: AppViewModel,
    initialTargetType: ChatTargetType = ChatTargetType.GROUP,
    initialTeacher: TeacherEntity? = null,
    initialGroup: WhatsAppGroupEntity? = null,
    onDismiss: () -> Unit
) {
    val teachers by viewModel.teachers.collectAsState()
    val groups by viewModel.whatsAppGroups.collectAsState()
    val isSending by viewModel.isSendingManual.collectAsState()
    val sendResult by viewModel.manualSendResult.collectAsState()
    val isSyncingGroups by viewModel.isSyncingGroups.collectAsState()

    var selectedTargetType by remember {
        mutableStateOf(if (initialTeacher != null) ChatTargetType.TEACHER else initialTargetType)
    }

    var selectedGroup by remember {
        mutableStateOf(initialGroup ?: groups.firstOrNull())
    }
    var customGroupId by remember { mutableStateOf("") }
    var isManualGroupInput by remember { mutableStateOf(false) }

    var selectedTeacher by remember {
        mutableStateOf(initialTeacher ?: teachers.firstOrNull())
    }
    var customPhoneNumber by remember { mutableStateOf("") }

    var messageText by remember {
        mutableStateOf(
            "🔔 *PENGINGAT ABSEN PULANG GURU*\n\nKepada Yth. *{nama_guru}*,\nWaktu kepulangan sekolah telah tiba pada pukul *{jam}* WIB.\nMohon untuk segera mengisi *Presensi/Absen Pulang* hari ini.\n\nLink Presensi: {link_absen}\n\nTerima kasih atas dedikasi Bapak/Ibu hari ini. 🙏"
        )
    }

    var showAddGroupDialog by remember { mutableStateOf(false) }
    var groupDropdownExpanded by remember { mutableStateOf(false) }
    var teacherDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {
            viewModel.clearManualSendResult()
            onDismiss()
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(WhatsAppGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Chat ke WhatsApp",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Kirim ke Grup WhatsApp atau Kontak Guru",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dialog_send_chat"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Selector Tab: Grup WhatsApp atau Kontak
                item {
                    Text(
                        text = "Pilih Tujuan Kirim:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ChatTargetType.values().forEach { type ->
                            val isSelected = selectedTargetType == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedTargetType = type },
                                label = { Text(type.label) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (type) {
                                            ChatTargetType.GROUP -> Icons.Default.Groups
                                            ChatTargetType.TEACHER -> Icons.Default.Person
                                            ChatTargetType.ALL_TEACHERS -> Icons.Default.Group
                                            ChatTargetType.CUSTOM_PHONE -> Icons.Default.Phone
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.testTag("chip_target_${type.name}")
                            )
                        }
                    }
                }

                // 2. Specific Target Input Area
                item {
                    when (selectedTargetType) {
                        ChatTargetType.GROUP -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "👥 Pilih Grup WhatsApp:",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Row {
                                            IconButton(
                                                onClick = { viewModel.syncGroupsFromFonnte() },
                                                enabled = !isSyncingGroups,
                                                modifier = Modifier.testTag("btn_sync_fonnte_groups")
                                            ) {
                                                if (isSyncingGroups) {
                                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                                } else {
                                                    Icon(Icons.Default.Refresh, contentDescription = "Ambil Grup dari Fonnte", tint = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                            IconButton(
                                                onClick = { showAddGroupDialog = true },
                                                modifier = Modifier.testTag("btn_add_group_dialog")
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = "Tambah Grup", tint = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }

                                    if (!isManualGroupInput) {
                                        // Dropdown ComboBox Grup WhatsApp
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            OutlinedTextField(
                                                value = selectedGroup?.let { "${it.name} (${it.groupId})" } ?: "Pilih Grup WhatsApp...",
                                                onValueChange = {},
                                                readOnly = true,
                                                trailingIcon = {
                                                    IconButton(onClick = { groupDropdownExpanded = !groupDropdownExpanded }) {
                                                        Icon(
                                                            imageVector = if (groupDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                                            contentDescription = null
                                                        )
                                                    }
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("dropdown_select_group")
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .matchParentSize()
                                                    .clickable { groupDropdownExpanded = true }
                                            )

                                            DropdownMenu(
                                                expanded = groupDropdownExpanded,
                                                onDismissRequest = { groupDropdownExpanded = false },
                                                modifier = Modifier
                                                    .heightIn(max = 260.dp)
                                                    .fillMaxWidth(0.9f)
                                            ) {
                                                if (groups.isEmpty()) {
                                                    DropdownMenuItem(
                                                        text = { Text("Belum ada grup tersimpan. Klik '+' atau sinkron.") },
                                                        onClick = { groupDropdownExpanded = false }
                                                    )
                                                } else {
                                                    groups.forEach { group ->
                                                        DropdownMenuItem(
                                                            text = {
                                                                Column {
                                                                    Text(
                                                                        text = group.name,
                                                                        fontWeight = FontWeight.SemiBold
                                                                    )
                                                                    Text(
                                                                        text = group.groupId,
                                                                        style = MaterialTheme.typography.bodySmall,
                                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                    )
                                                                }
                                                            },
                                                            onClick = {
                                                                selectedGroup = group
                                                                groupDropdownExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            TextButton(onClick = { isManualGroupInput = true }) {
                                                Text("Ketik ID Grup Manual", style = MaterialTheme.typography.bodySmall)
                                            }
                                            Text(
                                                text = "${groups.size} Grup Tersimpan",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.align(Alignment.CenterVertically)
                                            )
                                        }
                                    } else {
                                        // Manual Input ID Grup
                                        OutlinedTextField(
                                            value = customGroupId,
                                            onValueChange = { customGroupId = it },
                                            label = { Text("ID Grup Fonnte (cth: 1203630248849@g.us)") },
                                            singleLine = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("input_custom_group_id")
                                        )
                                        TextButton(onClick = { isManualGroupInput = false }) {
                                            Text("Kembali ke Pilihan Dropdown Grup", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }

                        ChatTargetType.TEACHER -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "👤 Pilih Kontak Guru Penerima:",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Dropdown ComboBox Kontak Guru
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = selectedTeacher?.let { "${it.name} (${it.whatsappNumber})" } ?: "Pilih Guru...",
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = {
                                                IconButton(onClick = { teacherDropdownExpanded = !teacherDropdownExpanded }) {
                                                    Icon(
                                                        imageVector = if (teacherDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                                        contentDescription = null
                                                    )
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("dropdown_select_teacher")
                                        )

                                        Box(
                                            modifier = Modifier
                                                .matchParentSize()
                                                .clickable { teacherDropdownExpanded = true }
                                        )

                                        DropdownMenu(
                                            expanded = teacherDropdownExpanded,
                                            onDismissRequest = { teacherDropdownExpanded = false },
                                            modifier = Modifier
                                                .heightIn(max = 280.dp)
                                                .fillMaxWidth(0.9f)
                                        ) {
                                            teachers.forEach { teacher ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Column {
                                                            Text(
                                                                text = teacher.name,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                            Text(
                                                                text = "${teacher.position} • ${teacher.whatsappNumber}",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    },
                                                    onClick = {
                                                        selectedTeacher = teacher
                                                        teacherDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        ChatTargetType.ALL_TEACHERS -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Broadcast ke Seluruh Kontak Guru",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "Pesan otomatis dikirim satu per satu ke ${teachers.count { it.isActive }} guru aktif dengan personalisasi nama masing-masing.",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }

                        ChatTargetType.CUSTOM_PHONE -> {
                            OutlinedTextField(
                                value = customPhoneNumber,
                                onValueChange = { customPhoneNumber = it },
                                label = { Text("Nomor Kontak WhatsApp (cth: 08123456789)") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_custom_phone")
                            )
                        }
                    }
                }

                // 3. Template Pesan Cepat Chips
                item {
                    Text(
                        text = "Pilihan Template Pesan:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SuggestionChip(
                            onClick = {
                                messageText = "🔔 *PENGINGAT ABSEN PULANG GURU*\n\nKepada Yth. *{nama_guru}*,\nWaktu kepulangan sekolah telah tiba pada pukul *{jam}* WIB.\nMohon untuk segera mengisi *Presensi/Absen Pulang* hari ini.\n\nLink Presensi: {link_absen}\n\nTerima kasih atas dedikasi Bapak/Ibu hari ini. 🙏"
                            },
                            label = { Text("🔔 Absen Pulang") }
                        )
                        SuggestionChip(
                            onClick = {
                                messageText = "📢 *PENGUMUMAN SEKOLAH*\n\nKepada Yth. Bapak/Ibu Dewan Guru & Staf *{sekolah}*,\n\nDiberitahukan bahwa hari ini tanggal *{tanggal}*:\n(Tulis pengumuman di sini...)\n\nDemikian info ini disampaikan. Terima kasih."
                            },
                            label = { Text("📢 Pengumuman") }
                        )
                        SuggestionChip(
                            onClick = {
                                messageText = "⏰ *PERINGATAN BATAS WAKTU ABSENSI*\n\nKepada Yth. *{nama_guru}*,\nBatas waktu pengisian absensi kepulangan sekolah akan segera ditutup pada pukul 16:00 WIB.\nBagi yang belum mengisi, segera klik tautan berikut: {link_absen}\n\nTerima kasih!"
                            },
                            label = { Text("⏰ Batas Waktu") }
                        )
                    }
                }

                // 4. Input Pesan WhatsApp
                item {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        label = { Text("Isi Pesan WhatsApp") },
                        minLines = 4,
                        maxLines = 8,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_chat_message")
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("{nama_guru}", "{tanggal}", "{jam}", "{link_absen}", "{sekolah}").forEach { varTag ->
                            SuggestionChip(
                                onClick = { messageText += " $varTag" },
                                label = { Text(varTag, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }

                // 5. Pratinjau Pesan yang Diformat
                item {
                    val previewName = when (selectedTargetType) {
                        ChatTargetType.TEACHER -> selectedTeacher?.name ?: "Bapak/Ibu Guru"
                        ChatTargetType.GROUP -> selectedGroup?.name ?: "Bapak/Ibu Guru"
                        else -> "Bapak/Ibu Guru"
                    }
                    val formattedPreview = viewModel.formatMessagePreview(messageText, previewName)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "👁️ Pratinjau Tampilan Pesan WhatsApp:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formattedPreview,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 5
                            )
                        }
                    }
                }

                // 6. Hasil Pengiriman
                if (sendResult != null) {
                    item {
                        val (success, detail) = sendResult!!
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (success) WhatsAppGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (success) WhatsAppGreen else MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (success) "✅ Sukses: $detail" else "❌ Gagal: $detail",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (success) WhatsAppGreen else MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (selectedTargetType) {
                        ChatTargetType.GROUP -> {
                            val targetId = if (isManualGroupInput) customGroupId.trim() else selectedGroup?.groupId?.trim() ?: ""
                            val targetName = if (isManualGroupInput) "Grup $targetId" else selectedGroup?.name ?: targetId
                            if (targetId.isNotBlank() && messageText.isNotBlank()) {
                                viewModel.sendChatMessage("GROUP_WA", targetId, targetName, messageText)
                            }
                        }
                        ChatTargetType.TEACHER -> {
                            selectedTeacher?.let { teacher ->
                                if (messageText.isNotBlank()) {
                                    viewModel.sendChatMessage("TEACHER", teacher.whatsappNumber, teacher.name, messageText)
                                }
                            }
                        }
                        ChatTargetType.ALL_TEACHERS -> {
                            if (messageText.isNotBlank()) {
                                viewModel.sendChatMessage("ALL_TEACHERS", "", "Semua Guru", messageText)
                            }
                        }
                        ChatTargetType.CUSTOM_PHONE -> {
                            if (customPhoneNumber.isNotBlank() && messageText.isNotBlank()) {
                                viewModel.sendChatMessage("CUSTOM_PHONE", customPhoneNumber, "Kontak WA", messageText)
                            }
                        }
                    }
                },
                enabled = !isSending && messageText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                modifier = Modifier.testTag("btn_send_chat_submit")
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mengirim...")
                } else {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kirim Sekarang")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    viewModel.clearManualSendResult()
                    onDismiss()
                },
                modifier = Modifier.testTag("btn_close_send_dialog")
            ) {
                Text("Tutup")
            }
        }
    )

    // Sub-dialog: Tambah Grup WhatsApp Baru
    if (showAddGroupDialog) {
        var newGroupName by remember { mutableStateOf("") }
        var newGroupId by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddGroupDialog = false },
            title = { Text("Tambah Grup WhatsApp Baru", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Masukkan nama grup dan ID grup WhatsApp dari Fonnte (cth: 1203630248849@g.us):",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = newGroupName,
                        onValueChange = { newGroupName = it },
                        label = { Text("Nama Grup (cth: Grup Dewan Guru)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_new_group_name")
                    )
                    OutlinedTextField(
                        value = newGroupId,
                        onValueChange = { newGroupId = it },
                        label = { Text("ID Grup (cth: 1203630248849@g.us)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_new_group_id")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newGroupName.isNotBlank() && newGroupId.isNotBlank()) {
                            viewModel.saveWhatsAppGroup(newGroupName, newGroupId)
                            showAddGroupDialog = false
                        }
                    },
                    enabled = newGroupName.isNotBlank() && newGroupId.isNotBlank(),
                    modifier = Modifier.testTag("btn_confirm_save_new_group")
                ) {
                    Text("Simpan Grup")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGroupDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
