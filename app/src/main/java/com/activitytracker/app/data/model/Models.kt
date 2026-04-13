package com.activitytracker.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "activities")
@Serializable
data class Activity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String = "🏃",
    val category: String = "General",
    val increment: Double = 1.0,
    val target: Double? = null,
    val restDaysPerWeek: Int = 0,
    val isArchived: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_logs")
@Serializable
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val activityId: Long,
    val date: Long,
    val value: Double,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class Category(val displayName: String, val emoji: String) {
    FUERZA("Fuerza", "💪"),
    CARDIO("Cardio", "❤️"),
    HABITO("Hábito", "📅"),
    ESTUDIO("Estudio", "📚"),
    GENERAL("General", "📝")
}

object SuggestedActivities {
    val suggestions = listOf(
        Activity(name = "Pesas", emoji = "🏋️", category = "FUERZA", increment = 1.0),
        Activity(name = "Abdominales", emoji = "🤸", category = "FUERZA", increment = 1.0),
        Activity(name = "Flexiones de brazos", emoji = "💪", category = "FUERZA", increment = 1.0),
        Activity(name = "Dominadas", emoji = "🤸", category = "FUERZA", increment = 1.0),
        Activity(name = "Correr (km)", emoji = "🏃", category = "CARDIO", increment = 0.5),
        Activity(name = "Caminar (km)", emoji = "🚶", category = "CARDIO", increment = 0.5),
        Activity(name = "Nadar (min)", emoji = "🏊", category = "CARDIO", increment = 5.0),
        Activity(name = "Yoga (min)", emoji = "🧘", category = "HABITO", increment = 5.0),
        Activity(name = "Estudio (horas)", emoji = "📚", category = "ESTUDIO", increment = 0.5),
        Activity(name = "Lectura (páginas)", emoji = "📖", category = "ESTUDIO", increment = 5.0),
        Activity(name = "Sentadillas", emoji = "🦵", category = "FUERZA", increment = 5.0),
        Activity(name = "Saltar cuerda (min)", emoji = "⭕", category = "CARDIO", increment = 1.0)
    )
}
