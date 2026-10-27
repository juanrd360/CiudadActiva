package com.example.ciudadactiva.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class UserRole(val label: String, val permissionsSummary: String) {
    CIUDADANO("Ciudadano", "Reportar y hacer seguimiento"),
    GESTOR("Gestor", "Gestionar y asignar reportes"),
    OPERADOR("Operador", "Atender reportes asignados"),
    ADMINISTRADOR("Administrador", "Administrar categorías, usuarios y permisos")
}

enum class ReportStatus(val label: String) {
    REGISTRADO("Registrado"),
    ADMITIDO("Admitido"),
    ASIGNADO("Asignado"),
    EN_ATENCION("En atención"),
    CERRADO("Cerrado"),
    CONFIRMADO("Confirmado"),
    REABIERTO("Reabierto"),
    RECHAZADO("Rechazado"),
    CANCELADO("Cancelado")
}

enum class ReportPriority(val label: String) {
    BAJA("Baja"),
    MEDIA("Media"),
    ALTA("Alta"),
    CRITICA("Crítica")
}

data class TimelineEvent(
    val id: String,
    val title: String,
    val transition: String,
    val description: String,
    val author: String,
    val roleLabel: String,
    val timestamp: String,
    val isOfficialAdvance: Boolean = false,
    val photoLabel: String? = null
)

data class ReportComment(
    val id: String,
    val author: String,
    val isOwn: Boolean = false,
    val isOfficial: Boolean = false,
    val roleBadge: String? = null,
    val timestamp: String,
    val content: String,
    val isDeleted: Boolean = false,
    val isHiddenByModeration: Boolean = false,
    val moderationReason: String? = null,
    val replyToAuthor: String? = null,
    val isEdited: Boolean = false
)

data class ReportItem(
    val id: String,
    val code: String,
    val authorName: String,
    val isOwnReport: Boolean,
    var category: String,
    var title: String,
    var description: String,
    var location: String,
    var status: ReportStatus,
    var priority: ReportPriority,
    var isPriorityOfficial: Boolean = false,
    val createdAt: String,
    var hasUnreadUpdate: Boolean = false,
    var isOverdue: Boolean = false,
    var dueInText: String? = null,
    var supportCount: Int = 0,
    var hasSupported: Boolean = false,
    var isFollowing: Boolean = false,
    var assignedOperator: String? = null,
    var rejectionReason: String? = null,
    var reopenReason: String? = null,
    var hasReopenedOnce: Boolean = false,
    var closingObservation: String? = null,
    var closingDate: String? = null,
    var citizenRating: Int? = null,
    var citizenConfirmationComment: String? = null,
    val timeline: MutableList<TimelineEvent> = mutableListOf(),
    val comments: MutableList<ReportComment> = mutableListOf()
)

data class NotificationItem(
    val id: String,
    val reportCode: String,
    val title: String,
    val subtitle: String,
    val timestamp: String,
    var isUnread: Boolean = true,
    val targetRoles: Set<UserRole> = emptySet()
)

data class CategoryItem(
    val id: String,
    var name: String,
    var description: String,
    var defaultPriority: ReportPriority,
    var isActive: Boolean = true,
    val reportsInUseCount: Int = 0
)

data class StaffUserItem(
    val id: String,
    var fullName: String,
    var email: String,
    var role: UserRole,
    var isActive: Boolean = true,
    val activeLoad: Int = 0,
    val maxLoad: Int = 5,
    val statusDetail: String = ""
)

// Repositorio completamente limpio.
// Las listas están vacías, esperando a ser llenadas por tu API en Render.
object CiudadActivaRepository {
    var currentRole by mutableStateOf(UserRole.CIUDADANO)
    var currentUserName by mutableStateOf("")
    var currentUserEmail by mutableStateOf("")
    var currentUserPhone by mutableStateOf("")

    var globalDemoState by mutableStateOf("Normal")

    val categories = mutableStateListOf<CategoryItem>()
    val staffUsers = mutableStateListOf<StaffUserItem>()
    val notifications = mutableStateListOf<NotificationItem>()
    val reports = mutableStateListOf<ReportItem>()

    fun getReportById(id: String): ReportItem? {
        return reports.find { it.id == id || it.code == id }
    }
}
