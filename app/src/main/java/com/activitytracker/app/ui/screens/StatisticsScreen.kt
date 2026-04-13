package com.activitytracker.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.activitytracker.app.ui.components.StatCard
import com.activitytracker.app.viewmodel.ActivityViewModel
import com.activitytracker.app.viewmodel.StatsPeriod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: ActivityViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedPeriod by remember { mutableStateOf(StatsPeriod.WEEK) }
    
    val periodStats = remember { mutableStateMapOf<Long, Pair<Double, Int>>() }
    val totalStats = remember { mutableStateMapOf<Long, Double>() }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(uiState.activities, selectedPeriod) {
        isLoading = true
        uiState.activities.forEach { activity ->
            val stats = viewModel.getStatsForPeriod(activity.id, selectedPeriod)
            periodStats[activity.id] = stats
            
            val total = viewModel.getTotalAllTime(activity.id)
            totalStats[activity.id] = total
        }
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Period selector
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    StatsPeriod.values().forEach { period ->
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = { selectedPeriod = period },
                            label = { 
                                Text(when (period) {
                                    StatsPeriod.WEEK -> "Semana"
                                    StatsPeriod.MONTH -> "Mes"
                                    StatsPeriod.YEAR -> "Año"
                                })
                            }
                        )
                    }
                }
            }
            
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                // Stats by period
                item {
                    Text("Estadísticas por ${when (selectedPeriod) {
                        StatsPeriod.WEEK -> "semana"
                        StatsPeriod.MONTH -> "mes"
                        StatsPeriod.YEAR -> "año"
                    }}", style = MaterialTheme.typography.titleLarge)
                }
                
                items(uiState.activities.filter { !it.isArchived }.size) { index ->
                    val activity = uiState.activities.filter { !it.isArchived }[index]
                    val stats = periodStats[activity.id] ?: (0.0 to 0)
                    
                    StatCard(
                        activityName = activity.name,
                        emoji = activity.emoji,
                        total = stats.first,
                        daysActive = stats.second,
                        period = when (selectedPeriod) {
                            StatsPeriod.WEEK -> "esta semana"
                            StatsPeriod.MONTH -> "este mes"
                            StatsPeriod.YEAR -> "este año"
                        }
                    )
                }
                
                // Total stats
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Estadísticas Totales", style = MaterialTheme.typography.titleLarge)
                }
                
                items(uiState.activities.filter { !it.isArchived }.size) { index ->
                    val activity = uiState.activities.filter { !it.isArchived }[index]
                    val total = totalStats[activity.id] ?: 0.0
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(activity.emoji, style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(activity.name, style = MaterialTheme.typography.titleMedium)
                            }
                            Text(
                                "Total: $total",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
