package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.MessageLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageLogDao {
    @Query("SELECT * FROM message_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogsFlow(limit: Int = 100): Flow<List<MessageLogEntity>>

    @Query("SELECT * FROM message_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<MessageLogEntity>>

    @Query("SELECT COUNT(*) FROM message_logs")
    suspend fun getLogCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MessageLogEntity): Long

    @Delete
    suspend fun deleteLog(log: MessageLogEntity)

    @Query("DELETE FROM message_logs")
    suspend fun deleteAllLogs()
}
