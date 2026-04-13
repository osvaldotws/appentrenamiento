package com.activitytracker.app.data.repository

import android.content.Context
import com.activitytracker.app.data.database.AppDatabase
import com.activitytracker.app.data.model.Activity
import com.activitytracker.app.data.model.ActivityLog
import kotlinx.coroutines.flow.Flow
import java.io.File

class ActivityRepository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val activityDao = database.activityDao()
    private val logDao = database.activityLogDao()
    
    // Activity operations
    fun getAllActivities(showArchived: Boolean = false): Flow<List<Activity>> = 
        activityDao.getAllActivities(showArchived)
    
    suspend fun getActivityById(id: Long): Activity? = activityDao.getActivityById(id)
    
    fun searchActivities(query: String): Flow<List<Activity>> = 
        activityDao.searchActivities("%$query%")
    
    suspend fun insertActivity(activity: Activity): Long = 
        activityDao.insertActivity(activity)
    
    suspend fun updateActivity(activity: Activity) = 
        activityDao.updateActivity(activity)
    
    suspend fun deleteActivity(activity: Activity) = 
        activityDao.deleteActivity(activity)
    
    suspend fun toggleArchive(id: Long, isArchived: Boolean) = 
        activityDao.toggleArchive(id, isArchived)
    
    suspend fun updateSortOrder(id: Long, order: Int) = 
        activityDao.updateSortOrder(id, order)
    
    // Log operations
    suspend fun getLogByDate(activityId: Long, date: Long): ActivityLog? = 
        logDao.getLogByDate(activityId, date)
    
    fun getLogsByDateRange(activityId: Long, startDate: Long, endDate: Long): Flow<List<ActivityLog>> = 
        logDao.getLogsByDateRange(activityId, startDate, endDate)
    
    fun getLogsByDate(date: Long): Flow<List<ActivityLog>> = 
        logDao.getLogsByDate(date)
    
    fun getAllLogsByActivity(activityId: Long): Flow<List<ActivityLog>> = 
        logDao.getAllLogsByActivity(activityId)
    
    fun getAllLogs(): Flow<List<ActivityLog>> = 
        logDao.getAllLogs()
    
    suspend fun insertLog(log: ActivityLog): Long = 
        logDao.insertLog(log)
    
    suspend fun updateLog(log: ActivityLog) = 
        logDao.updateLog(log)
    
    suspend fun deleteLog(log: ActivityLog) = 
        logDao.deleteLog(log)
    
    suspend fun deleteLogByDate(activityId: Long, date: Long) = 
        logDao.deleteLogByDate(activityId, date)
    
    suspend fun getTotalByDateRange(activityId: Long, startDate: Long, endDate: Long): Double? = 
        logDao.getTotalByDateRange(activityId, startDate, endDate)
    
    suspend fun getTotalAllTime(activityId: Long): Double? = 
        logDao.getTotalAllTime(activityId)
    
    suspend fun getUniqueDates(activityId: Long): List<Long> = 
        logDao.getUniqueDates(activityId)
    
    // Export/Import data
    suspend fun exportData(context: Context): String {
        val activities = getAllActivities(true).first()
        val allLogs = getAllLogs().first()
        
        val exportData = ExportData(activities = activities, logs = allLogs)
        return kotlinx.serialization.json.Json.encodeToString(ExportData.serializer(), exportData)
    }
    
    suspend fun importData(context: Context, jsonData: String): Boolean {
        return try {
            val exportData = kotlinx.serialization.json.Json.decodeFromString(ExportData.serializer(), jsonData)
            
            // Clear existing data
            getAllActivities(true).first().forEach { activityDao.deleteActivity(it) }
            
            // Import activities and collect new IDs mapping
            val idMapping = mutableMapOf<Long, Long>()
            exportData.activities.forEach { activity ->
                val oldId = activity.id
                val newId = activityDao.insertActivity(activity.copy(id = 0))
                idMapping[oldId] = newId
            }
            
            // Import logs with updated activity IDs
            exportData.logs.forEach { log ->
                val newActivityId = idMapping[log.activityId] ?: log.activityId
                logDao.insertLog(log.copy(id = 0, activityId = newActivityId))
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
}

@kotlinx.serialization.Serializable
data class ExportData(
    val activities: List<Activity>,
    val logs: List<ActivityLog>
)
