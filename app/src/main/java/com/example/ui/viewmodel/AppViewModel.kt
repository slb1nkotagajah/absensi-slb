package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AttendanceEntity
import com.example.data.local.entity.MessageLogEntity
import com.example.data.local.entity.ScheduleEntity
import com.example.data.local.entity.TeacherEntity
import com.example.data.local.entity.WhatsAppGroupEntity
import com.example.data.remote.FonnteDeviceResponse
import com.example.data.remote.FonnteResult
import com.example.data.repository.AttendanceRepository
import com.example.scheduler.ScheduleManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AttendanceRepository(application)
    private val context = application.applicationContext

    val teachers: StateFlow<List<TeacherEntity>> = repository.teachersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val schedules: StateFlow<List<ScheduleEntity>> = repository.schedulesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs: StateFlow<List<MessageLogEntity>> = repository.recentLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val whatsAppGroups: StateFlow<List<WhatsAppGroupEntity>> = repository.whatsAppGroupsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val todayDate = com.example.util.DateTimeUtils.getTodayDateString()
    val todayAttendances: StateFlow<List<AttendanceEntity>> = repository.getAttendancesByDate(todayDate)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _fonnteToken = MutableStateFlow(AppDatabase.DEFAULT_FONNTE_TOKEN)
    val fonnteToken: StateFlow<String> = _fonnteToken.asStateFlow()

    private val _schoolName = MutableStateFlow("SLB N 1 KOTA GAJAH")
    val schoolName: StateFlow<String> = _schoolName.asStateFlow()

    private val _attendanceLink = MutableStateFlow("https://absensi.sekolah.id/pulang")
    val attendanceLink: StateFlow<String> = _attendanceLink.asStateFlow()

    private val _isSchedulerActive = MutableStateFlow(true)
    val isSchedulerActive: StateFlow<Boolean> = _isSchedulerActive.asStateFlow()

    private val _dbPassword = MutableStateFlow(AppDatabase.DEFAULT_DB_PASSWORD)
    val dbPassword: StateFlow<String> = _dbPassword.asStateFlow()

    private val _isDbUnlocked = MutableStateFlow(false)
    val isDbUnlocked: StateFlow<Boolean> = _isDbUnlocked.asStateFlow()

    private val _dbUnlockError = MutableStateFlow<String?>(null)
    val dbUnlockError: StateFlow<String?> = _dbUnlockError.asStateFlow()

    private val _fonnteDeviceStatus = MutableStateFlow<FonnteDeviceResponse?>(null)
    val fonnteDeviceStatus: StateFlow<FonnteDeviceResponse?> = _fonnteDeviceStatus.asStateFlow()

    private val _isCheckingDevice = MutableStateFlow(false)
    val isCheckingDevice: StateFlow<Boolean> = _isCheckingDevice.asStateFlow()

    private val _deviceCheckError = MutableStateFlow<String?>(null)
    val deviceCheckError: StateFlow<String?> = _deviceCheckError.asStateFlow()

    private val _isSendingManual = MutableStateFlow(false)
    val isSendingManual: StateFlow<Boolean> = _isSendingManual.asStateFlow()

    private val _manualSendResult = MutableStateFlow<Pair<Boolean, String>?>(null)
    val manualSendResult: StateFlow<Pair<Boolean, String>?> = _manualSendResult.asStateFlow()

    private val _isSyncingGroups = MutableStateFlow(false)
    val isSyncingGroups: StateFlow<Boolean> = _isSyncingGroups.asStateFlow()

    private val _groupSyncResult = MutableStateFlow<Pair<Boolean, String>?>(null)
    val groupSyncResult: StateFlow<Pair<Boolean, String>?> = _groupSyncResult.asStateFlow()

    private val _sqlScriptContent = MutableStateFlow("")
    val sqlScriptContent: StateFlow<String> = _sqlScriptContent.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        loadSettings()
        loadSqlScript()
        checkDeviceStatus()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _fonnteToken.value = repository.getFonnteToken()
            _schoolName.value = repository.getSetting(AppDatabase.KEY_SCHOOL_NAME, "SLB N 1 KOTA GAJAH")
            _attendanceLink.value = repository.getSetting(AppDatabase.KEY_ATTENDANCE_LINK, "https://absensi.sekolah.id/pulang")
            _isSchedulerActive.value = repository.getSetting(AppDatabase.KEY_SCHEDULER_ENABLED, "true").toBoolean()
            _dbPassword.value = repository.getDbPassword()
        }
    }

    private fun loadSqlScript() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = context.assets.open("schema_database_absen.sql")
                val reader = BufferedReader(InputStreamReader(inputStream))
                val content = reader.readText()
                reader.close()
                _sqlScriptContent.value = content
            } catch (e: Exception) {
                _sqlScriptContent.value = "-- Gagal memuat asset SQL: ${e.message}"
            }
        }
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun checkDeviceStatus() {
        viewModelScope.launch {
            _isCheckingDevice.value = true
            _deviceCheckError.value = null
            when (val result = repository.checkFonnteDevice()) {
                is FonnteResult.Success -> {
                    _fonnteDeviceStatus.value = result.data
                }
                is FonnteResult.Error -> {
                    _deviceCheckError.value = result.message
                }
            }
            _isCheckingDevice.value = false
        }
    }

    fun sendTestMessage(target: String, message: String) {
        viewModelScope.launch {
            _isSendingManual.value = true
            _manualSendResult.value = null
            val result = repository.sendManualMessage(target, message, "Pengujian Langsung")
            _manualSendResult.value = result
            _isSendingManual.value = false
        }
    }

    fun sendChatMessage(
        targetType: String,
        targetId: String,
        targetName: String,
        messageText: String
    ) {
        viewModelScope.launch {
            _isSendingManual.value = true
            _manualSendResult.value = null
            if (targetType == "ALL_TEACHERS") {
                val (success, fail) = withContext(Dispatchers.IO) {
                    repository.sendBroadcastToAllTeachers(messageText)
                }
                val isSuccess = success > 0
                val detail = "Broadcast: $success sukses, $fail gagal."
                _manualSendResult.value = Pair(isSuccess, detail)
                showMessage(detail)
            } else {
                val formattedTarget = if (targetType == "GROUP_WA") {
                    targetId.trim()
                } else {
                    formatPhoneNumber(targetId.trim())
                }

                val finalMessage = if (targetType == "TEACHER") {
                    repository.formatMessage(messageText, targetName, _schoolName.value, _attendanceLink.value)
                } else if (targetType == "GROUP_WA") {
                    repository.formatMessage(messageText, "Bapak/Ibu Guru", _schoolName.value, _attendanceLink.value)
                } else {
                    repository.formatMessage(messageText, targetName, _schoolName.value, _attendanceLink.value)
                }

                val displayName = if (targetType == "GROUP_WA") "Grup WA: $targetName" else targetName
                val result = withContext(Dispatchers.IO) {
                    repository.sendManualMessage(formattedTarget, finalMessage, displayName)
                }
                _manualSendResult.value = result
                if (result.first) {
                    showMessage("Pesan berhasil dikirim ke $targetName")
                } else {
                    showMessage("Gagal: ${result.second}")
                }
            }
            _isSendingManual.value = false
        }
    }

    fun formatMessagePreview(template: String, recipientName: String = "Bapak/Ibu Guru"): String {
        return repository.formatMessage(template, recipientName, _schoolName.value, _attendanceLink.value)
    }

    fun syncGroupsFromFonnte() {
        viewModelScope.launch {
            _isSyncingGroups.value = true
            _groupSyncResult.value = null
            val result = withContext(Dispatchers.IO) {
                repository.syncGroupsFromFonnte()
            }
            _groupSyncResult.value = result
            _isSyncingGroups.value = false
            showMessage(result.second)
        }
    }

    fun refreshFonnteGroupCache() {
        viewModelScope.launch {
            _isSyncingGroups.value = true
            val result = withContext(Dispatchers.IO) {
                repository.refreshFonnteGroupCache()
            }
            showMessage(result.second)
            val syncResult = withContext(Dispatchers.IO) {
                repository.syncGroupsFromFonnte()
            }
            _groupSyncResult.value = syncResult
            _isSyncingGroups.value = false
            showMessage(syncResult.second)
        }
    }

    fun clearGroupSyncResult() {
        _groupSyncResult.value = null
    }

    fun saveWhatsAppGroup(name: String, groupId: String, description: String = "") {
        viewModelScope.launch {
            if (name.isBlank() || groupId.isBlank()) {
                showMessage("Nama grup dan ID grup WhatsApp wajib diisi.")
                return@launch
            }
            val entity = WhatsAppGroupEntity(
                name = name.trim(),
                groupId = groupId.trim(),
                description = description.trim()
            )
            repository.insertWhatsAppGroup(entity)
            showMessage("Grup '${name}' berhasil disimpan!")
        }
    }

    fun deleteWhatsAppGroup(group: WhatsAppGroupEntity) {
        viewModelScope.launch {
            repository.deleteWhatsAppGroup(group)
            showMessage("Grup '${group.name}' dihapus.")
        }
    }

    fun clearManualSendResult() {
        _manualSendResult.value = null
    }

    fun executeScheduleNow(schedule: ScheduleEntity) {
        viewModelScope.launch {
            showMessage("Mengirim pengingat WhatsApp '${schedule.title}'...")
            val results = withContext(Dispatchers.IO) {
                repository.executeSchedule(schedule.id)
            }
            showMessage("Selesai: ${results.size} target diproses.")
        }
    }

    val canScheduleExactAlarms: Boolean
        get() = ScheduleManager.canScheduleExactAlarms(context)

    fun testScheduleIn30Seconds(schedule: ScheduleEntity) {
        viewModelScope.launch {
            ScheduleManager.scheduleTestInSeconds(context, schedule, 30)
            showMessage("⏰ Pengingat dijadwalkan otomatis dalam 30 detik! Silakan keluar atau kunci layar HP untuk menguji.")
        }
    }

    fun toggleSchedule(schedule: ScheduleEntity, enabled: Boolean) {
        viewModelScope.launch {
            val updated = schedule.copy(isEnabled = enabled)
            repository.updateSchedule(updated)
            if (enabled) {
                ScheduleManager.scheduleReminder(context, updated)
                showMessage("Jadwal '${schedule.title}' diaktifkan: ${ScheduleManager.getNextExecutionSummary(updated)}")
            } else {
                ScheduleManager.cancelReminder(context, schedule.id)
                showMessage("Jadwal '${schedule.title}' dinonaktifkan")
            }
        }
    }

    fun saveSchedule(
        id: Long = 0,
        title: String,
        targetType: String,
        targetValue: String,
        daysOfWeek: String,
        hour: Int,
        minute: Int,
        messageTemplate: String,
        isEnabled: Boolean
    ) {
        viewModelScope.launch {
            val entity = ScheduleEntity(
                id = id,
                title = title.trim(),
                targetType = targetType,
                targetValue = targetValue.trim(),
                daysOfWeek = daysOfWeek,
                hour = hour,
                minute = minute,
                messageTemplate = messageTemplate.trim(),
                isEnabled = isEnabled
            )
            if (id == 0L) {
                val newId = repository.insertSchedule(entity)
                if (isEnabled) {
                    ScheduleManager.scheduleReminder(context, entity.copy(id = newId))
                }
                showMessage("Jadwal baru berhasil disimpan")
            } else {
                repository.updateSchedule(entity)
                if (isEnabled) {
                    ScheduleManager.scheduleReminder(context, entity)
                } else {
                    ScheduleManager.cancelReminder(context, entity.id)
                }
                showMessage("Jadwal berhasil diperbarui")
            }
        }
    }

    fun deleteSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            ScheduleManager.cancelReminder(context, schedule.id)
            repository.deleteSchedule(schedule)
            showMessage("Jadwal '${schedule.title}' dihapus")
        }
    }

    fun saveTeacher(
        id: Long = 0,
        nip: String,
        name: String,
        whatsappNumber: String,
        position: String,
        isActive: Boolean = true
    ) {
        viewModelScope.launch {
            val formattedWa = formatPhoneNumber(whatsappNumber)
            val teacher = TeacherEntity(
                id = id,
                nip = nip.trim(),
                name = name.trim(),
                whatsappNumber = formattedWa,
                position = position.trim(),
                isActive = isActive
            )
            if (id == 0L) {
                repository.insertTeacher(teacher)
                showMessage("Guru baru '${teacher.name}' berhasil ditambahkan")
            } else {
                repository.updateTeacher(teacher)
                showMessage("Data guru '${teacher.name}' berhasil diperbarui")
            }
        }
    }

    fun deleteTeacher(teacher: TeacherEntity) {
        viewModelScope.launch {
            repository.deleteTeacher(teacher)
            showMessage("Guru '${teacher.name}' dihapus")
        }
    }

    fun getCurrentJakartaDateTime(): String {
        return com.example.util.DateTimeUtils.getCurrentFullDateTimeString()
    }

    fun markAttendance(teacher: TeacherEntity, status: String) {
        viewModelScope.launch {
            val timeNow = com.example.util.DateTimeUtils.getCurrentTimeString()
            repository.markAttendance(teacher.id, teacher.name, status, timeNow)
            showMessage("Presensi ${teacher.name} diubah menjadi $status")
        }
    }

    fun saveSettings(
        token: String,
        school: String,
        link: String,
        schedulerEnabled: Boolean
    ) {
        viewModelScope.launch {
            repository.saveSetting(AppDatabase.KEY_FONNTE_TOKEN, token.trim())
            repository.saveSetting(AppDatabase.KEY_SCHOOL_NAME, school.trim())
            repository.saveSetting(AppDatabase.KEY_ATTENDANCE_LINK, link.trim())
            repository.saveSetting(AppDatabase.KEY_SCHEDULER_ENABLED, schedulerEnabled.toString())

            _fonnteToken.value = token.trim()
            _schoolName.value = school.trim()
            _attendanceLink.value = link.trim()
            _isSchedulerActive.value = schedulerEnabled

            if (schedulerEnabled) {
                ScheduleManager.scheduleAllActiveReminders(context)
            }

            checkDeviceStatus()
            showMessage("Pengaturan berhasil disimpan!")
        }
    }

    // Database Authentication & Management (Password: Sukses88)
    fun unlockDatabase(passwordInput: String): Boolean {
        return if (passwordInput == _dbPassword.value || passwordInput == AppDatabase.DEFAULT_DB_PASSWORD) {
            _isDbUnlocked.value = true
            _dbUnlockError.value = null
            true
        } else {
            _dbUnlockError.value = "Password salah! Gunakan password resmi (default: Sukses88)"
            false
        }
    }

    fun lockDatabase() {
        _isDbUnlocked.value = false
        _dbUnlockError.value = null
    }

    fun changeDatabasePassword(newPassword: String) {
        viewModelScope.launch {
            if (newPassword.isBlank()) {
                showMessage("Password baru tidak boleh kosong")
                return@launch
            }
            repository.saveSetting(AppDatabase.KEY_DB_PASSWORD, newPassword)
            _dbPassword.value = newPassword
            showMessage("Password database berhasil diubah!")
        }
    }

    fun resetDatabaseToDefaults() {
        viewModelScope.launch {
            repository.resetDatabaseToInitial()
            loadSettings()
            ScheduleManager.scheduleAllActiveReminders(context)
            showMessage("Database telah di-reset ke konfigurasi awal!")
        }
    }

    fun clearMessageLogs() {
        viewModelScope.launch {
            repository.clearLogs()
            showMessage("Riwayat pesan berhasil dibersihkan")
        }
    }

    private fun formatPhoneNumber(input: String): String {
        var clean = input.replace(Regex("[^0-9]"), "")
        if (clean.startsWith("0")) {
            clean = "62" + clean.substring(1)
        } else if (!clean.startsWith("62") && clean.isNotEmpty()) {
            clean = "62$clean"
        }
        return clean
    }
}
