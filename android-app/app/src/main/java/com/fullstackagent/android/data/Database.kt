package com.fullstackagent.android.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "messages")
data class Message(@PrimaryKey(autoGenerate = true) val id: Long = 0, val role: String, val content: String, val createdAt: Long = System.currentTimeMillis())
@Entity(tableName = "memories")
data class Memory(@PrimaryKey(autoGenerate = true) val id: Long = 0, val title: String, val body: String, val updatedAt: Long = System.currentTimeMillis())

@Dao interface AgentDao {
    @Query("SELECT * FROM messages ORDER BY createdAt") fun messages(): Flow<List<Message>>
    @Insert suspend fun addMessage(message: Message)
    @Query("DELETE FROM messages") suspend fun clearMessages()
    @Query("SELECT * FROM memories ORDER BY updatedAt DESC") fun memories(): Flow<List<Memory>>
    @Query("SELECT * FROM memories ORDER BY updatedAt DESC LIMIT :limit") suspend fun recentMemories(limit: Int = 20): List<Memory>
    @Insert suspend fun addMemory(memory: Memory)
    @Delete suspend fun deleteMemory(memory: Memory)
}

@Database(entities = [Message::class, Memory::class], version = 1, exportSchema = false)
abstract class AgentDatabase : RoomDatabase() { abstract fun dao(): AgentDao }
