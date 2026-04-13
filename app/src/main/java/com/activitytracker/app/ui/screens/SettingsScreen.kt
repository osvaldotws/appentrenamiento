package com.activitytracker.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.activitytracker.app.viewmodel.ActivityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ActivityViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Información", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Activity Tracker v1.0", style = MaterialTheme.typography.bodyMedium)
                    Text("Desarrollado con Kotlin y Jetpack Compose", style = MaterialTheme.typography.bodySmall)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Características", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    listOf(
                        "📅 Calendario para seleccionar fechas",
                        "➕ Contadores personalizables",
                        "🎯 Metas diarias/semanales",
                        "🔥 Rachas y récords históricos",
                        "📊 Estadísticas por período",
                        "💾 Exportar/Importar datos",
                        "🏷️ Categorización de actividades",
                        "📝 Notas por actividad",
                        "🗄️ Archivado de actividades"
                    ).forEach { feature ->
                        Text(feature, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ayuda", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• Presiona el botón + para agregar una nueva actividad",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "• Toca el número del contador para editar el valor manualmente",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "• Usa los botones + y - para incrementar/decrementar rápidamente",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "• Cambia la fecha desde la tarjeta superior",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "• Exporta tus datos en Configuración para hacer backup",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
