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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import com.example.ui.viewmodel.AppViewModel

@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val currentToken by viewModel.fonnteToken.collectAsState()
    val currentSchool by viewModel.schoolName.collectAsState()
    val currentLink by viewModel.attendanceLink.collectAsState()
    val isSchedulerActive by viewModel.isSchedulerActive.collectAsState()

    var token by remember(currentToken) { mutableStateOf(currentToken) }
    var school by remember(currentSchool) { mutableStateOf(currentSchool) }
    var link by remember(currentLink) { mutableStateOf(currentLink) }
    var schedulerEnabled by remember(isSchedulerActive) { mutableStateOf(isSchedulerActive) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Pengaturan Sistem & Fonnte Gateway",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Konfigurasi token Fonnte WhatsApp, data sekolah, dan tautan absensi kepulangan.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Fonnte Token & Config
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.padding(start = 8.dp))
                        Text(
                            text = "Kredensial Fonnte WhatsApp",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = token,
                        onValueChange = { token = it },
                        label = { Text("Token API Fonnte") },
                        singleLine = true,
                        supportingText = {
                            Text("Token aktif saat ini: DEc5ECUwTKXBpnh989tD")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_fonnte_token")
                    )

                    OutlinedTextField(
                        value = school,
                        onValueChange = { school = it },
                        label = { Text("Nama Sekolah / Instansi") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_school_name")
                    )

                    OutlinedTextField(
                        value = link,
                        onValueChange = { link = it },
                        label = { Text("Tautan Link Absen Pulang Guru") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_attendance_link")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Otomatis Latar Belakang (Scheduler)",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Menjalankan alarm exact pengingat tepat waktu setiap hari.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = schedulerEnabled,
                            onCheckedChange = { schedulerEnabled = it },
                            modifier = Modifier.testTag("switch_scheduler")
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            viewModel.saveSettings(token, school, link, schedulerEnabled)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_save_settings")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.padding(start = 6.dp))
                        Text("Simpan Pengaturan")
                    }
                }
            }
        }

        // Guide Card for Fonnte
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.padding(start = 8.dp))
                        Text(
                            text = "Petunjuk Penggunaan Fonnte & Grup WA",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "1. Cara Menghubungkan Nomor WhatsApp:\n" +
                                "• Buka dashboard fonnte.com dan login.\n" +
                                "• Pada menu Device, klik 'Connect' lalu scan barcode QR menggunakan WhatsApp di HP.\n" +
                                "• Masukkan token device Anda ke kolom Token di atas.\n\n" +
                                "2. Mengirim Pesan ke Grup WhatsApp:\n" +
                                "• Tambahkan nomor bot Fonnte ke dalam grup WhatsApp sekolah.\n" +
                                "• Dapatkan ID Grup dari Fonnte (format: 1203630xxxxxxxx@g.us).\n" +
                                "• Masukkan ID grup tersebut ke dalam kolom target jadwal pengingat.\n\n" +
                                "3. Keamanan Database:\n" +
                                "• Menu Database diproteksi dengan password resmi: Sukses88.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                    )
                }
            }
        }
    }
}
