package com.activitytracker.app.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.activitytracker.app.data.model.Activity
import com.activitytracker.app.data.model.ActivityLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities WHERE isArchived = :showArchived ORDER BY sortOrder ASC, name ASC")
    fun getAllActivities(showArchived: Boolean = false): Flow<List<Activity>>
    
    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getActivityById(id: Long): Activity?
    
    @Query("SELECT * FROM activities WHERE name LIKE :query OR category LIKE :query")
    fun searchActivities(query: String): Flow<List<Activity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: Activity): Long
    
    @Update
    suspend fun updateActivity(activity: Activity)
    
    @Delete
    suspend fun deleteActivity(activity: Activity)
    
    @Query("UPDATE activities SET isArchived = :isArchived WHERE id = :id")
    suspend fun toggleArchive(id: Long, isArchived: Boolean)
    
    @Query("UPDATE activities SET sortOrder = :order WHERE id = :id")
    suspend fun updateSortOrder(id: Long, order: Int)
    
    @Query("SELECT * FROM activities WHERE isArchived = :showArchived ORDER BY sortOrder ASC, name ASC")
    suspend fun getAllActivities(showArchived: Boolean = false): List<Activity>
}

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs WHERE activityId = :activityId AND date = :date")
    suspend fun getLogByDate(activityId: Long, date: Long): ActivityLog?
    
    @Query("SELECT * FROM activity_logs WHERE activityId = :activityId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getLogsByDateRange(activityId: Long, startDate: Long, endDate: Long): Flow<List<ActivityLog>>
    
    @Query("SELECT * FROM activity_logs WHERE date = :date")
    fun getLogsByDate(date: Long): Flow<List<ActivityLog>>
    
    @Query("SELECT * FROM activity_logs WHERE activityId = :activityId ORDER BY date DESC")
    fun getAllLogsByActivity(activityId: Long): Flow<List<ActivityLog>>
    
    @Query("SELECT * FROM activity_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<ActivityLog>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLog): Long
    
    @Update
    suspend fun updateLog(log: ActivityLog)
    
    @Delete
    suspend fun deleteLog(log: ActivityLog)
    
    @Query("DELETE FROM activity_logs WHERE activityId = :activityId AND date = :date")
    suspend fun deleteLogByDate(activityId: Long, date: Long)
    
    @Query("SELECT SUM(value) FROM activity_logs WHERE activityId = :activityId AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalByDateRange(activityId: Long, startDate: Long, endDate: Long): Double?
    
    @Query("SELECT SUM(value) FROM activity_logs WHERE activityId = :activityId")
    suspend fun getTotalAllTime(activityId: Long): Double?
    
    @Query("SELECT DISTINCT date FROM activity_logs WHERE activityId = :activityId ORDER BY date DESC")
    suspend fun getUniqueDates(activityId: Long): List<Long>
    
    @Query("SELECT * FROM activity_logs ORDER BY date DESC")
    suspend fun getAllLogsSync(): List<ActivityLog>
}
