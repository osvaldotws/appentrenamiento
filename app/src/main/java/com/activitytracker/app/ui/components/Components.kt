package com.activitytracker.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DatePickerDialog(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Fecha") },
        text = {
            Column {
                Text(
                    text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(selectedDate)
                onDismiss()
            }) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun NumberInputDialog(
    initialValue: Double,
    increment: Double,
    onValueConfirmed: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var value by remember { mutableStateOf(initialValue.toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ingrese Valor") },
        text = {
            Column {
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Valor") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Accesos rápidos:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(10, 20, 50).forEach { quickValue ->
                        Button(
                            onClick = { value = (value.toDoubleOrNull() ?: 0.0) + quickValue },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+${quickValue}")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val newValue = value.toDoubleOrNull() ?: initialValue
                onValueConfirmed(newValue)
                onDismiss()
            }) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ActivityCard(
    name: String,
    emoji: String,
    currentValue: Double,
    category: String,
    target: Double?,
    streak: Int,
    maxStreak: Int,
    onClick: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (target != null && target > 0) (currentValue / target).coerceIn(0.0, 1.0) else null
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = emoji, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = name, style = MaterialTheme.typography.titleLarge)
                        Text(text = category, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDecrement) {
                        Text(text = "➖", style = MaterialTheme.typography.titleMedium)
                    }
                    Text(
                        text = currentValue.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.width(60.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    IconButton(onClick = onIncrement) {
                        Text(text = "➕", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                if (streak > 0 || maxStreak > 0) {
                    Text(
                        text = "🔥 $streak días (máx: $maxStreak)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (progress != null) {
                    Text(
                        text = "🎯 ${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            if (progress != null) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = progress.toFloat(),
                    modifier = Modifier.fillMaxWidth(),
                    color = if (progress >= 1.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun StatCard(
    activityName: String,
    emoji: String,
    total: Double,
    daysActive: Int,
    period: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = activityName, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Total $period: $total", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Días activos: $daysActive", style = MaterialTheme.typography.bodySmall)
        }
    }
}
