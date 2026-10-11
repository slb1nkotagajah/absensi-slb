package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AttendanceDao
import com.example.data.local.dao.MessageLogDao
import com.example.data.local.dao.ScheduleDao
import com.example.data.local.dao.SettingDao
import com.example.data.local.dao.TeacherDao
import com.example.data.local.entity.AttendanceEntity
import com.example.data.local.entity.MessageLogEntity
import com.example.data.local.entity.ScheduleEntity
import com.example.data.local.entity.SettingEntity
import com.example.data.local.entity.TeacherEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TeacherEntity::class,
        ScheduleEntity::class,
        AttendanceEntity::class,
        MessageLogEntity::class,
        SettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun teacherDao(): TeacherDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun messageLogDao(): MessageLogDao
    abstract fun settingDao(): SettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        const val DEFAULT_FONNTE_TOKEN = "DEc5ECUwTKXBpnh989tD"
        const val DEFAULT_DB_PASSWORD = "Sukses88"
        const val KEY_FONNTE_TOKEN = "fonnte_token"
        const val KEY_DB_PASSWORD = "db_password"
        const val KEY_SCHOOL_NAME = "school_name"
        const val KEY_ATTENDANCE_LINK = "attendance_link"
        const val KEY_SCHEDULER_ENABLED = "scheduler_enabled"

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "absen_guru.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                seedInitialData(getInstance(context))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedInitialData(database: AppDatabase) {
            val settingDao = database.settingDao()
            settingDao.setSetting(SettingEntity(KEY_FONNTE_TOKEN, DEFAULT_FONNTE_TOKEN))
            settingDao.setSetting(SettingEntity(KEY_DB_PASSWORD, DEFAULT_DB_PASSWORD))
            settingDao.setSetting(SettingEntity(KEY_SCHOOL_NAME, "SLB N 1 KOTA GAJAH"))
            settingDao.setSetting(SettingEntity(KEY_ATTENDANCE_LINK, "https://absensi.sekolah.id/pulang"))
            settingDao.setSetting(SettingEntity(KEY_SCHEDULER_ENABLED, "true"))

            val teacherDao = database.teacherDao()
            if (teacherDao.getTeacherCount() == 0) {
                teacherDao.insertTeachers(
                    listOf(
                        TeacherEntity(nip = "197505102005011002", name = "Drs. Supriyanto, M.Pd.", whatsappNumber = "081234567890", position = "Kepala Sekolah"),
                        TeacherEntity(nip = "198208152009022003", name = "Siti Rahmawati, S.Pd.", whatsappNumber = "085712345678", position = "Waka Kurikulum"),
                        TeacherEntity(nip = "198603212011011004", name = "Ahmad Fauzi, S.Pd.", whatsappNumber = "087812345678", position = "Guru Kelas A"),
                        TeacherEntity(nip = "199011122019032005", name = "Nurul Hidayah, S.Kom.", whatsappNumber = "089612345678", position = "Guru TIK / Operator"),
                        TeacherEntity(nip = "199307042022041006", name = "Budi Santoso, S.Pd.", whatsappNumber = "082112345678", position = "Guru Penjaskes")
                    )
                )
            }

            val scheduleDao = database.scheduleDao()
            if (scheduleDao.getScheduleCount() == 0) {
                scheduleDao.insertSchedule(
                    ScheduleEntity(
                        title = "Pengingat Absen Pulang Guru (Setiap Hari Kerja)",
                        targetType = "ALL_TEACHERS",
                        targetValue = "",
                        daysOfWeek = "Senin,Selasa,Rabu,Kamis,Jumat,Sabtu,Minggu",
                        hour = 15,
                        minute = 0,
                        messageTemplate = "🔔 *PENGINGAT ABSEN PULANG GURU*\n\nKepada Yth. Bapak/Ibu *{nama_guru}*,\n\nWaktu kepulangan sekolah telah tiba pada pukul *{jam}* WIB.\nMohon untuk tidak lupa melakukan *Presensi/Absen Pulang* hari ini tanggal *{tanggal}*.\n\n📱 Link Presensi: {link_absen}\n\nTerima kasih atas dedikasi dan pengabdian Bapak/Ibu hari ini. Selamat beristirahat dan semoga selamat sampai di rumah. 🙏✨\n\n_Pesan otomatis dikirim oleh Sistem Presensi SLB N 1 Kota Gajah_",
                        isEnabled = true
                    )
                )
                scheduleDao.insertSchedule(
                    ScheduleEntity(
                        title = "Pengingat Absen Pulang Siang / Hari Jumat",
                        targetType = "ALL_TEACHERS",
                        targetValue = "",
                        daysOfWeek = "Jumat",
                        hour = 11,
                        minute = 30,
                        messageTemplate = "🔔 *PENGINGAT ABSEN PULANG HARI JUMAT*\n\nKepada Yth. Bapak/Ibu *{nama_guru}*,\n\nMengingatkan jam dinas hari Jumat telah selesai pada pukul *{jam}* WIB. Harap segera mengisi absen pulang:\n\n📱 Link: {link_absen}\n\nSelamat berakhir pekan bersama keluarga tercinta! 🙏✨",
                        isEnabled = true
                    )
                )
            }
        }
    }
}
