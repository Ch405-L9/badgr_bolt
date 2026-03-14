package com.badgr.orbreader.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlock(achievement: AchievementEntity)

    @Query("SELECT * FROM achievements ORDER BY unlockedAt ASC")
    fun getAllUnlocked(): Flow<List<AchievementEntity>>

    @Query("SELECT id FROM achievements")
    suspend fun getUnlockedIds(): List<String>

    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun unlockedCount(): Int
}
