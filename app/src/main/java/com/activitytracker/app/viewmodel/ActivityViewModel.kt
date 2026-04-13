package com.activitytracker.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.activitytracker.app.data.model.Activity
import com.activitytracker.app.data.model.ActivityLog
import com.activitytracker.app.data.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class ActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ActivityRepository(application)
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()
    
    init {
        loadActivities()
        observeLogsForSelectedDate()
    }
    
    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
        observeLogsForSelectedDate()
    }
    
    fun toggleShowArchived() {
        _uiState.update { it.copy(showArchived = !it.showArchived) }
        loadActivities()
    }
    
    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
    
    private fun loadActivities() {
        viewModelScope.launch {
            repository.getAllActivities(_uiState.value.showArchived).collect { activities ->
                _uiState.update { it.copy(activities = activities) }
            }
        }
    }
    
    private fun observeLogsForSelectedDate() {
        viewModelScope.launch {
            val dateMillis = _selectedDate.value.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            repository.getLogsByDate(dateMillis).collect { logs ->
                _uiState.update { it.copy(logsForSelectedDate = logs) }
            }
        }
    }
    
    fun addActivity(activity: Activity) {
        viewModelScope.launch {
            repository.insertActivity(activity)
        }
    }
    
    fun updateActivity(activity: Activity) {
        viewModelScope.launch {
            repository.updateActivity(activity)
        }
    }
    
    fun deleteActivity(activity: Activity) {
        viewModelScope.launch {
            repository.deleteActivity(activity)
        }
    }
    
    fun archiveActivity(id: Long, isArchived: Boolean) {
        viewModelScope.launch {
            repository.toggleArchive(id, isArchived)
        }
    }
    
    fun addOrUpdateLog(activityId: Long, value: Double, note: String = "") {
        viewModelScope.launch {
            val dateMillis = _selectedDate.value.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val existingLog = repository.getLogByDate(activityId, dateMillis)
            
            if (existingLog != null) {
                repository.updateLog(existingLog.copy(value = value, note = note))
            } else {
                repository.insertLog(
                    ActivityLog(
                        activityId = activityId,
                        date = dateMillis,
                        value = value,
                        note = note
                    )
                )
            }
        }
    }
    
    fun deleteLog(activityId: Long) {
        viewModelScope.launch {
            val dateMillis = _selectedDate.value.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            repository.deleteLogByDate(activityId, dateMillis)
        }
    }
    
    suspend fun getStatsForPeriod(activityId: Long, period: StatsPeriod): Pair<Double, Int> {
        val now = LocalDate.now()
        val (startDate, endDate) = when (period) {
            StatsPeriod.WEEK -> {
                val startOfWeek = now.minusDays(now.dayOfWeek.value.toLong() - 1)
                startOfWeek to now
            }
            StatsPeriod.MONTH -> {
                val startOfMonth = now.withDayOfMonth(1)
                startOfMonth to now
            }
            StatsPeriod.YEAR -> {
                val startOfYear = now.withDayOfYear(1)
                startOfYear to now
            }
        }
        
        val startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endDate.atEndOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val total = repository.getTotalByDateRange(activityId, startMillis, endMillis) ?: 0.0
        val uniqueDates = repository.getUniqueDates(activityId)
            .filter { it in startMillis..endMillis }
            .size
        
        return total to uniqueDates
    }
    
    suspend fun getTotalAllTime(activityId: Long): Double {
        return repository.getTotalAllTime(activityId) ?: 0.0
    }
    
    suspend fun calculateStreak(activityId: Long): Pair<Int, Int> {
        val uniqueDates = repository.getUniqueDates(activityId).sortedDescending()
        if (uniqueDates.isEmpty()) return 0 to 0
        
        val today = LocalDate.now()
        val todayMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val yesterdayMillis = today.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        // Check if streak is active (has log today or yesterday)
        val hasToday = uniqueDates.any { it >= todayMillis }
        val hasYesterday = uniqueDates.any { it in yesterdayMillis until todayMillis }
        
        if (!hasToday && !hasYesterday) {
            return 0 to calculateMaxStreak(uniqueDates)
        }
        
        var currentStreak = 0
        var currentDate = if (hasToday) today else today.minusDays(1)
        
        for (dateMillis in uniqueDates) {
            val logDate = LocalDate.ofInstant(
                java.time.Instant.ofEpochMilli(dateMillis),
                ZoneId.systemDefault()
            )
            
            if (ChronoUnit.DAYS.between(logDate, currentDate) <= 1) {
                currentStreak++
                currentDate = logDate
            } else {
                break
            }
        }
        
        return currentStreak to calculateMaxStreak(uniqueDates)
    }
    
    private fun calculateMaxStreak(uniqueDates: List<Long>): Int {
        if (uniqueDates.isEmpty()) return 0
        
        val sortedDates = uniqueDates.sorted()
        var maxStreak = 1
        var currentStreak = 1
        
        for (i in 1 until sortedDates.size) {
            val prevDate = LocalDate.ofInstant(
                java.time.Instant.ofEpochMilli(sortedDates[i - 1]),
                ZoneId.systemDefault()
            )
            val currDate = LocalDate.ofInstant(
                java.time.Instant.ofEpochMilli(sortedDates[i]),
                ZoneId.systemDefault()
            )
            
            if (ChronoUnit.DAYS.between(prevDate, currDate) == 1L) {
                currentStreak++
                maxStreak = maxOf(maxStreak, currentStreak)
            } else {
                currentStreak = 1
            }
        }
        
        return maxStreak
    }
    
    suspend fun exportData(): String {
        return repository.exportData(getApplication())
    }
    
    suspend fun importData(jsonData: String): Boolean {
        return repository.importData(getApplication(), jsonData)
    }
    
    fun searchActivities(query: String) {
        viewModelScope.launch {
            repository.searchActivities(query).collect { activities ->
                _uiState.update { it.copy(searchResults = activities) }
            }
        }
    }
}

data class UiState(
    val activities: List<Activity> = emptyList(),
    val logsForSelectedDate: List<ActivityLog> = emptyList(),
    val searchResults: List<Activity> = emptyList(),
    val showArchived: Boolean = false,
    val searchQuery: String = ""
)

enum class StatsPeriod {
    WEEK, MONTH, YEAR
}
