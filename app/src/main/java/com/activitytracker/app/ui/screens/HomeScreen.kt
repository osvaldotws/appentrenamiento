package com.activitytracker.app.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.activitytracker.app.ui.components.ActivityCard
import com.activitytracker.app.ui.components.NumberInputDialog
import com.activitytracker.app.viewmodel.ActivityViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ActivityViewModel = viewModel(),
    onNavigateToAddActivity: () -> Unit,
    onNavigateToActivityDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showAddActivityDialog by remember { mutableStateOf(false) }
    var activityToShowDialog by remember { mutableStateOf<Long?>(null) }
    
    // Export/Import launchers
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val jsonData = viewModel.exportData()
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonData.toByteArray())
                }
            }
        }
    }
    
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val jsonData = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().readText()
                }
                if (jsonData != null) {
                    viewModel.importData(jsonData)
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity Tracker") },
                actions = {
                    IconButton(onClick = { exportLauncher.launch("activity_tracker_export.json") }) {
                        Icon(Icons.Default.Upload, contentDescription = "Exportar")
                    }
                    IconButton(onClick = { importLauncher.launch("application/json") }) {
                        Icon(Icons.Default.Download, contentDescription = "Importar")
                    }
                    IconButton(onClick = { viewModel.toggleShowArchived() }) {
                        Icon(
                            if (uiState.showArchived) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Ver archivados"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddActivity) {
                Icon(Icons.Default.Add, contentDescription = "Agregar actividad")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Date selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showDatePicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Fecha seleccionada", style = MaterialTheme.typography.labelMedium)
                        Text(
                            selectedDate.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Icon(Icons.Default.CalendarToday, contentDescription = "Cambiar fecha")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { 
                    viewModel.setSearchQuery(it)
                    viewModel.searchActivities(it)
                },
                label = { Text("Buscar actividades") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Activities list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.activities, key = { it.id }) { activity ->
                    val logForDate = uiState.logsForSelectedDate.find { it.activityId == activity.id }
                    val currentValue = logForDate?.value ?: 0.0
                    
                    // Get streak info (would need to be loaded asynchronously in production)
                    val streak = 0
                    val maxStreak = 0
                    
                    ActivityCard(
                        name = activity.name,
                        emoji = activity.emoji,
                        currentValue = currentValue,
                        category = activity.category,
                        target = activity.target,
                        streak = streak,
                        maxStreak = maxStreak,
                        onClick = { onNavigateToActivityDetail(activity.id) },
                        onIncrement = {
                            val newValue = currentValue + activity.increment
                            viewModel.addOrUpdateLog(activity.id, newValue, logForDate?.note ?: "")
                        },
                        onDecrement = {
                            val newValue = (currentValue - activity.increment).coerceAtLeast(0.0)
                            if (newValue > 0) {
                                viewModel.addOrUpdateLog(activity.id, newValue, logForDate?.note ?: "")
                            } else {
                                viewModel.deleteLog(activity.id)
                            }
                        },
                        modifier = Modifier.animateItemPlacement()
                    )
                }
                
                if (uiState.activities.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No hay actividades", style = MaterialTheme.typography.titleLarge)
                                Text("Presiona + para agregar tu primera actividad")
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showDatePicker) {
        DatePickerDialog(
            selectedDate = selectedDate,
            onDateSelected = { viewModel.setSelectedDate(it) },
            onDismiss = { showDatePicker = false }
        )
    }
    
    activityToShowDialog?.let { activityId ->
        val activity = uiState.activities.find { it.id == activityId }
        val logForDate = uiState.logsForSelectedDate.find { it.activityId == activityId }
        
        if (activity != null) {
            NumberInputDialog(
                initialValue = logForDate?.value ?: 0.0,
                increment = activity.increment,
                onValueConfirmed = { newValue ->
                    viewModel.addOrUpdateLog(activityId, newValue, logForDate?.note ?: "")
                },
                onDismiss = { activityToShowDialog = null }
            )
        }
    }
}
