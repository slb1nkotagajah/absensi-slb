package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.TeacherEntity
import com.example.ui.viewmodel.AppViewModel

@Composable
fun TeacherScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val teachers by viewModel.teachers.collectAsState()
    var editingTeacher by remember { mutableStateOf<TeacherEntity?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }
    var sendDirectTeacher by remember { mutableStateOf<TeacherEntity?>(null) }

    Scaffold(
        modifier = modifier.testTag("teacher_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddingNew = true },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("fab_add_teacher")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Guru")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Daftar Guru & Kontak WhatsApp",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Kelola nomor telepon WhatsApp guru untuk menerima pesan pengingat absen kepulangan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (teachers.isEmpty()) {
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
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Belum Ada Data Guru",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Klik tombol '+' untuk menambahkan guru penerima pengingat WA.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(teachers) { teacher ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_teacher_${teacher.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = teacher.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (teacher.nip.isNotBlank()) {
                                    Text(
                                        text = "NIP: ${teacher.nip}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "Jabatan: ${teacher.position}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Phone,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = teacher.whatsappNumber,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { sendDirectTeacher = teacher },
                                    modifier = Modifier.testTag("btn_send_teacher_${teacher.id}")
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Kirim WA Cepat", tint = MaterialTheme.colorScheme.secondary)
                                }
                                IconButton(
                                    onClick = { editingTeacher = teacher },
                                    modifier = Modifier.testTag("btn_edit_teacher_${teacher.id}")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Guru")
                                }
                                IconButton(
                                    onClick = { viewModel.deleteTeacher(teacher) },
                                    modifier = Modifier.testTag("btn_delete_teacher_${teacher.id}")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus Guru", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isAddingNew) {
        TeacherFormDialog(
            teacher = null,
            onDismiss = { isAddingNew = false },
            onSave = { nip, name, phone, position, isActive ->
                viewModel.saveTeacher(
                    id = 0,
                    nip = nip,
                    name = name,
                    whatsappNumber = phone,
                    position = position,
                    isActive = isActive
                )
                isAddingNew = false
            }
        )
    }

    editingTeacher?.let { teacher ->
        TeacherFormDialog(
            teacher = teacher,
            onDismiss = { editingTeacher = null },
            onSave = { nip, name, phone, position, isActive ->
                viewModel.saveTeacher(
                    id = teacher.id,
                    nip = nip,
                    name = name,
                    whatsappNumber = phone,
                    position = position,
                    isActive = isActive
                )
                editingTeacher = null
            }
        )
    }

    sendDirectTeacher?.let { teacher ->
        var customMsg by remember {
            mutableStateOf("🔔 *PENGINGAT ABSEN PULANG*\n\nYth. Bapak/Ibu ${teacher.name},\nMohon jangan lupa melakukan presensi kepulangan sekolah hari ini. Terima kasih! 🙏")
        }
        AlertDialog(
            onDismissRequest = { sendDirectTeacher = null },
            title = { Text("Kirim Pengingat Langsung") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tujuan: ${teacher.name} (${teacher.whatsappNumber})")
                    OutlinedTextField(
                        value = customMsg,
                        onValueChange = { customMsg = it },
                        label = { Text("Pesan") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.sendTestMessage(teacher.whatsappNumber, customMsg)
                        sendDirectTeacher = null
                    }
                ) {
                    Text("Kirim WA")
                }
            },
            dismissButton = {
                TextButton(onClick = { sendDirectTeacher = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun TeacherFormDialog(
    teacher: TeacherEntity?,
    onDismiss: () -> Unit,
    onSave: (nip: String, name: String, phone: String, position: String, isActive: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(teacher?.name ?: "") }
    var nip by remember { mutableStateOf(teacher?.nip ?: "") }
    var phone by remember { mutableStateOf(teacher?.whatsappNumber ?: "") }
    var position by remember { mutableStateOf(teacher?.position ?: "Guru Kelas") }
    var isActive by remember { mutableStateOf(teacher?.isActive ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (teacher == null) "Tambah Guru Baru" else "Edit Data Guru", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Lengkap & Gelar") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_teacher_name")
                )
                OutlinedTextField(
                    value = nip,
                    onValueChange = { nip = it },
                    label = { Text("NIP (Opsional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Nomor WhatsApp (cth: 08123456789)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_teacher_phone")
                )
                OutlinedTextField(
                    value = position,
                    onValueChange = { position = it },
                    label = { Text("Jabatan / Mata Pelajaran") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Status Aktif Pengingat")
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onSave(nip, name, phone, position, isActive)
                    }
                },
                enabled = name.isNotBlank() && phone.isNotBlank(),
                modifier = Modifier.testTag("btn_save_teacher_confirm")
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
