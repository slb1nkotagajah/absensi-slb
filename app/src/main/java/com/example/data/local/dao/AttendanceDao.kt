package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AttendanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendances WHERE date = :date ORDER BY teacherName ASC")
    fun getAttendancesByDateFlow(date: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendances ORDER BY date DESC, teacherName ASC")
    fun getAllAttendancesFlow(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendances WHERE teacherId = :teacherId AND date = :date LIMIT 1")
    suspend fun getAttendance(teacherId: Long, date: String): AttendanceEntity?

    @Query("SELECT COUNT(*) FROM attendances")
    suspend fun getAttendanceCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendances(attendances: List<AttendanceEntity>)

    @Update
    suspend fun updateAttendance(attendance: AttendanceEntity)

    @Delete
    suspend fun deleteAttendance(attendance: AttendanceEntity)

    @Query("DELETE FROM attendances")
    suspend fun deleteAllAttendances()
}
