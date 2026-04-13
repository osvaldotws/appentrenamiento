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
import com.activitytracker.app.data.model.Activity
import com.activitytracker.app.data.model.SuggestedActivities
import com.activitytracker.app.viewmodel.ActivityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityScreen(
    viewModel: ActivityViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("🏃") }
    var selectedCategory by remember { mutableStateOf("General") }
    var increment by remember { mutableStateOf("1.0") }
    var target by remember { mutableStateOf("") }
    var restDays by remember { mutableStateOf("0") }
    var showSuggestions by remember { mutableStateOf(false) }
    
    val categories = listOf("General", "Fuerza", "Cardio", "Hábito", "Estudio")
    val commonEmojis = listOf("🏃", "🏋️", "🤸", "💪", "🚶", "🏊", "🧘", "📚", "📖", "🦵", "⭕", "❤️", "📅", "📝")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Actividad") },
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
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la actividad") },
                    placeholder = { Text("Ej: Pesas, Correr, Yoga...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            item {
                Text("Seleccionar Emoji:", style = MaterialTheme.typography.labelMedium)
                LazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(commonEmojis.size) { index ->
                        FilterChip(
                            selected = emoji == commonEmojis[index],
                            onClick = { emoji = commonEmojis[index] },
                            label = { Text(commonEmojis[index], style = MaterialTheme.typography.titleMedium) }
                        )
                    }
                }
            }
            
            item {
                Text("Categoría:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) }
                        )
                    }
                }
            }
            
            item {
                OutlinedTextField(
                    value = increment,
                    onValueChange = { increment = it },
                    label = { Text("Incremento (valor al presionar +)") },
                    placeholder = { Text("Ej: 1, 0.5, 5") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            item {
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Meta diaria (opcional)") },
                    placeholder = { Text("Ej: 100") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            item {
                OutlinedTextField(
                    value = restDays,
                    onValueChange = { restDays = it },
                    label = { Text("Días de descanso por semana") },
                    placeholder = { Text("Ej: 1") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            item {
                Button(
                    onClick = { showSuggestions = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver sugerencias")
                }
            }
            
            item {
                Button(
                    onClick = {
                        val newActivity = Activity(
                            name = name,
                            emoji = emoji,
                            category = selectedCategory,
                            increment = increment.toDoubleOrNull() ?: 1.0,
                            target = target.toDoubleOrNull(),
                            restDaysPerWeek = restDays.toIntOrNull() ?: 0
                        )
                        viewModel.addActivity(newActivity)
                        onNavigateBack()
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar Actividad")
                }
            }
        }
    }
    
    if (showSuggestions) {
        AlertDialog(
            onDismissRequest = { showSuggestions = false },
            title = { Text("Actividades Sugeridas") },
            text = {
                LazyColumn {
                    items(SuggestedActivities.suggestions.size) { index ->
                        val suggestion = SuggestedActivities.suggestions[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            onClick = {
                                name = suggestion.name
                                emoji = suggestion.emoji
                                selectedCategory = suggestion.category
                                increment = suggestion.increment.toString()
                                showSuggestions = false
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(suggestion.emoji, style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(suggestion.name, style = MaterialTheme.typography.titleMedium)
                                    Text(suggestion.category, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSuggestions = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}
