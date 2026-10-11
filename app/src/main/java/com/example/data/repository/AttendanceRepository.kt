package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AttendanceEntity
import com.example.data.local.entity.MessageLogEntity
import com.example.data.local.entity.ScheduleEntity
import com.example.data.local.entity.SettingEntity
import com.example.data.local.entity.TeacherEntity
import com.example.data.remote.FonnteRepository
import com.example.data.remote.FonnteResult
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import com.example.data.local.entity.WhatsAppGroupEntity

class AttendanceRepository(
    private val context: Context,
    private val db: AppDatabase = AppDatabase.getInstance(context),
    private val fonnteRepo: FonnteRepository = FonnteRepository()
) {
    val teachersFlow: Flow<List<TeacherEntity>> = db.teacherDao().getAllTeachersFlow()
    val schedulesFlow: Flow<List<ScheduleEntity>> = db.scheduleDao().getAllSchedulesFlow()
    val recentLogsFlow: Flow<List<MessageLogEntity>> = db.messageLogDao().getRecentLogsFlow(100)
    val settingsFlow: Flow<List<SettingEntity>> = db.settingDao().getAllSettingsFlow()
    val whatsAppGroupsFlow: Flow<List<WhatsAppGroupEntity>> = db.whatsAppGroupDao().getAllGroupsFlow()

    fun getAttendancesByDate(date: String): Flow<List<AttendanceEntity>> {
        return db.attendanceDao().getAttendancesByDateFlow(date)
    }

    // Settings helpers
    suspend fun getSetting(key: String, default: String): String {
        return db.settingDao().getSettingValue(key) ?: default
    }

    suspend fun saveSetting(key: String, value: String) {
        db.settingDao().setSetting(SettingEntity(key, value))
    }

    suspend fun getFonnteToken(): String {
        return getSetting(AppDatabase.KEY_FONNTE_TOKEN, AppDatabase.DEFAULT_FONNTE_TOKEN)
    }

    suspend fun getDbPassword(): String {
        return getSetting(AppDatabase.KEY_DB_PASSWORD, AppDatabase.DEFAULT_DB_PASSWORD)
    }

    // Teacher CRUD
    suspend fun insertTeacher(teacher: TeacherEntity): Long = db.teacherDao().insertTeacher(teacher)
    suspend fun updateTeacher(teacher: TeacherEntity) = db.teacherDao().updateTeacher(teacher)
    suspend fun deleteTeacher(teacher: TeacherEntity) = db.teacherDao().deleteTeacher(teacher)
    suspend fun getActiveTeachers(): List<TeacherEntity> = db.teacherDao().getActiveTeachers()

    // Schedule CRUD
    suspend fun insertSchedule(schedule: ScheduleEntity): Long = db.scheduleDao().insertSchedule(schedule)
    suspend fun updateSchedule(schedule: ScheduleEntity) = db.scheduleDao().updateSchedule(schedule)
    suspend fun deleteSchedule(schedule: ScheduleEntity) = db.scheduleDao().deleteSchedule(schedule)
    suspend fun getScheduleById(id: Long): ScheduleEntity? = db.scheduleDao().getScheduleById(id)
    suspend fun getEnabledSchedules(): List<ScheduleEntity> = db.scheduleDao().getEnabledSchedules()

    // Message Log
    suspend fun clearLogs() = db.messageLogDao().deleteAllLogs()

    // Attendance
    suspend fun markAttendance(teacherId: Long, teacherName: String, status: String, clockOutTime: String) {
        val today = com.example.util.DateTimeUtils.getTodayDateString()
        val existing = db.attendanceDao().getAttendance(teacherId, today)
        if (existing != null) {
            db.attendanceDao().updateAttendance(
                existing.copy(status = status, clockOutTime = clockOutTime)
            )
        } else {
            db.attendanceDao().insertAttendance(
                AttendanceEntity(
                    teacherId = teacherId,
                    teacherName = teacherName,
                    date = today,
                    clockOutTime = clockOutTime,
                    status = status
                )
            )
        }
    }

    // Check Fonnte device status
    suspend fun checkFonnteDevice() = fonnteRepo.checkDevice(getFonnteToken())

    // Format Message (Always Asia/Jakarta WIB Timezone)
    fun formatMessage(
        template: String,
        teacherName: String,
        schoolName: String,
        attendanceLink: String
    ): String {
        val currentDate = com.example.util.DateTimeUtils.formatJakarta("EEEE, dd MMMM yyyy")
        val currentTime = com.example.util.DateTimeUtils.formatJakarta("HH:mm")

        return template
            .replace("{nama_guru}", teacherName)
            .replace("{nama}", teacherName)
            .replace("{tanggal}", currentDate)
            .replace("{jam}", currentTime)
            .replace("{waktu}", currentTime)
            .replace("{link_absen}", attendanceLink)
            .replace("{link}", attendanceLink)
            .replace("{sekolah}", schoolName)
    }

    // Execute Schedule (Broadcast to targets)
    suspend fun executeSchedule(scheduleId: Long): List<String> {
        val schedule = db.scheduleDao().getScheduleById(scheduleId) ?: return listOf("Jadwal tidak ditemukan")
        val token = getFonnteToken()
        val schoolName = getSetting(AppDatabase.KEY_SCHOOL_NAME, "SLB N 1 KOTA GAJAH")
        val linkAbsen = getSetting(AppDatabase.KEY_ATTENDANCE_LINK, "https://absensi.sekolah.id/pulang")

        val results = mutableListOf<String>()

        when (schedule.targetType) {
            "ALL_TEACHERS" -> {
                val teachers = db.teacherDao().getActiveTeachers()
                if (teachers.isEmpty()) {
                    results.add("Tidak ada guru aktif yang terdaftar.")
                    return results
                }

                for (teacher in teachers) {
                    val finalMsg = formatMessage(schedule.messageTemplate, teacher.name, schoolName, linkAbsen)
                    val response = fonnteRepo.sendMessage(
                        token = token,
                        target = teacher.whatsappNumber,
                        message = finalMsg
                    )

                    val isSuccess = response is FonnteResult.Success
                    val respRaw = when (response) {
                        is FonnteResult.Success -> response.data.detail ?: response.data.message ?: "Berhasil dikirim"
                        is FonnteResult.Error -> response.message
                    }

                    db.messageLogDao().insertLog(
                        MessageLogEntity(
                            scheduleTitle = schedule.title,
                            recipient = teacher.whatsappNumber,
                            recipientName = teacher.name,
                            messageText = finalMsg,
                            status = if (isSuccess) "SUKSES" else "GAGAL",
                            responseRaw = respRaw
                        )
                    )

                    results.add("${teacher.name} (${teacher.whatsappNumber}): $respRaw")
                }
            }
            "GROUP_WA", "SPECIFIC_CONTACTS" -> {
                val targets = schedule.targetValue.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                if (targets.isEmpty()) {
                    results.add("Target nomor / grup WhatsApp kosong.")
                    return results
                }

                for (target in targets) {
                    val finalMsg = formatMessage(schedule.messageTemplate, "Bapak/Ibu Guru", schoolName, linkAbsen)
                    val response = fonnteRepo.sendMessage(
                        token = token,
                        target = target,
                        message = finalMsg
                    )

                    val isSuccess = response is FonnteResult.Success
                    val respRaw = when (response) {
                        is FonnteResult.Success -> response.data.detail ?: response.data.message ?: "Berhasil dikirim"
                        is FonnteResult.Error -> response.message
                    }

                    db.messageLogDao().insertLog(
                        MessageLogEntity(
                            scheduleTitle = schedule.title,
                            recipient = target,
                            recipientName = if (schedule.targetType == "GROUP_WA") "Grup WA" else "Kontak",
                            messageText = finalMsg,
                            status = if (isSuccess) "SUKSES" else "GAGAL",
                            responseRaw = respRaw
                        )
                    )

                    results.add("$target: $respRaw")
                }
            }
        }

        return results
    }

    // Send single manual test message
    suspend fun sendManualMessage(target: String, messageText: String, recipientName: String = "Test"): Pair<Boolean, String> {
        val token = getFonnteToken()
        val result = fonnteRepo.sendMessage(
            token = token,
            target = target,
            message = messageText
        )

        val (isSuccess, rawMsg) = when (result) {
            is FonnteResult.Success -> Pair(true, result.data.detail ?: result.data.message ?: "Terkirim sukses ke server Fonnte.")
            is FonnteResult.Error -> Pair(false, result.message)
        }

        db.messageLogDao().insertLog(
            MessageLogEntity(
                scheduleTitle = "Kirim Manual / Tes",
                recipient = target,
                recipientName = recipientName,
                messageText = messageText,
                status = if (isSuccess) "SUKSES" else "GAGAL",
                responseRaw = rawMsg
            )
        )

        return Pair(isSuccess, rawMsg)
    }

    // WhatsApp Group Management
    suspend fun getAllWhatsAppGroups(): List<WhatsAppGroupEntity> = db.whatsAppGroupDao().getAllGroups()
    suspend fun insertWhatsAppGroup(group: WhatsAppGroupEntity): Long = db.whatsAppGroupDao().insertGroup(group)
    suspend fun deleteWhatsAppGroup(group: WhatsAppGroupEntity) = db.whatsAppGroupDao().deleteGroup(group)

    suspend fun syncGroupsFromFonnte(): Pair<Boolean, String> {
        val token = getFonnteToken()
        return when (val result = fonnteRepo.getWhatsAppGroups(token)) {
            is FonnteResult.Success -> {
                val groups = result.data
                if (groups.isNotEmpty()) {
                    val entities = groups.map {
                        WhatsAppGroupEntity(
                            name = it.name.ifBlank { "Grup WA ${it.id}" },
                            groupId = it.id,
                            description = if (it.member.isNotBlank()) "${it.member} anggota" else "Sinkron dari Fonnte"
                        )
                    }
                    db.whatsAppGroupDao().insertGroups(entities)
                    Pair(true, "Berhasil menyinkronkan ${entities.size} grup WhatsApp dari akun Fonnte.")
                } else {
                    Pair(true, "Tidak ditemukan grup baru di akun Fonnte.")
                }
            }
            is FonnteResult.Error -> {
                Pair(false, result.message)
            }
        }
    }

    suspend fun refreshFonnteGroupCache(): Pair<Boolean, String> {
        val token = getFonnteToken()
        return when (val result = fonnteRepo.refreshGroupList(token)) {
            is FonnteResult.Success -> Pair(true, result.data)
            is FonnteResult.Error -> Pair(false, result.message)
        }
    }

    suspend fun sendBroadcastToAllTeachers(messageTemplate: String): Pair<Int, Int> {
        val teachers = db.teacherDao().getActiveTeachers()
        if (teachers.isEmpty()) return Pair(0, 0)
        val token = getFonnteToken()
        val schoolName = getSetting(AppDatabase.KEY_SCHOOL_NAME, "SLB N 1 KOTA GAJAH")
        val linkAbsen = getSetting(AppDatabase.KEY_ATTENDANCE_LINK, "https://absensi.sekolah.id/pulang")

        var successCount = 0
        var failCount = 0

        for (teacher in teachers) {
            val finalMsg = formatMessage(messageTemplate, teacher.name, schoolName, linkAbsen)
            val response = fonnteRepo.sendMessage(
                token = token,
                target = teacher.whatsappNumber,
                message = finalMsg
            )
            val isSuccess = response is FonnteResult.Success
            val respRaw = when (response) {
                is FonnteResult.Success -> response.data.detail ?: response.data.message ?: "Berhasil terkirim"
                is FonnteResult.Error -> response.message
            }
            if (isSuccess) successCount++ else failCount++

            db.messageLogDao().insertLog(
                MessageLogEntity(
                    scheduleTitle = "Broadcast ke Semua Guru",
                    recipient = teacher.whatsappNumber,
                    recipientName = teacher.name,
                    messageText = finalMsg,
                    status = if (isSuccess) "SUKSES" else "GAGAL",
                    responseRaw = respRaw
                )
            )
        }
        return Pair(successCount, failCount)
    }

    // Reset database to initial sample
    suspend fun resetDatabaseToInitial() {
        db.teacherDao().deleteAllTeachers()
        db.scheduleDao().deleteAllSchedules()
        db.messageLogDao().deleteAllLogs()
        db.attendanceDao().deleteAllAttendances()
        AppDatabase.seedInitialData(db)
    }
}
