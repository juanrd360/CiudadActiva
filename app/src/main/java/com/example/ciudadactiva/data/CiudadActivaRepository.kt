package com.example.ciudadactiva.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.ciudadactiva.network.ApiClient
import com.example.ciudadactiva.network.models.*

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
    val comments: MutableList<ReportComment> = mutableListOf(),
    var categoryId: Int = 1,
    var latitude: Double = 4.6097,
    var longitude: Double = -74.0817
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
    val reportsInUseCount: Int = 0,
    val numericId: Int = 1
)

data class StaffUserItem(
    val id: String,
    var fullName: String,
    var email: String,
    var role: UserRole,
    var isActive: Boolean = true,
    val activeLoad: Int = 0,
    val maxLoad: Int = 5,
    val statusDetail: String = "",
    val numericId: Long = 0L
)

object CiudadActivaRepository {
    var currentRole by mutableStateOf(UserRole.CIUDADANO)
    var currentUserId by mutableStateOf<Long?>(null)
    var currentUserName by mutableStateOf("")
    var currentUserEmail by mutableStateOf("")
    var currentUserPhone by mutableStateOf("")

    var globalDemoState by mutableStateOf("Normal")

    // Categorías sincronizadas con la base de datos de Render
    val categories = mutableStateListOf(
        CategoryItem("1", "Malla Vial y Huecos", "Daños en pavimento, baches o hundimientos en la vía", ReportPriority.ALTA, true, 0, 1),
        CategoryItem("2", "Alumbrado Público", "Luminarias apagadas, postes dañados o cables caídos", ReportPriority.MEDIA, true, 0, 2),
        CategoryItem("3", "Basuras y Escombros", "Acumulación de residuos en espacio público", ReportPriority.BAJA, true, 0, 3),
        CategoryItem("4", "Fuga de Agua / Alcantarillado", "Rebose de aguas residuales, falta de tapas o fugas", ReportPriority.CRITICA, true, 0, 4)
    )
    val staffUsers = mutableStateListOf<StaffUserItem>()
    val notifications = mutableStateListOf<NotificationItem>()
    val reports = mutableStateListOf<ReportItem>()

    fun getReportById(id: String): ReportItem? {
        return reports.find { it.id == id || it.code == id }
    }

    fun parseRole(rolStr: String?): UserRole {
        return when (rolStr?.uppercase()) {
            "GESTOR" -> UserRole.GESTOR
            "OPERADOR" -> UserRole.OPERADOR
            "ADMINISTRADOR", "ADMIN" -> UserRole.ADMINISTRADOR
            else -> UserRole.CIUDADANO
        }
    }

    fun parseStatus(statusStr: String?): ReportStatus {
        return ReportStatus.entries.find { it.name.equals(statusStr, ignoreCase = true) }
            ?: ReportStatus.REGISTRADO
    }

    fun parsePriority(prioStr: String?): ReportPriority {
        return ReportPriority.entries.find { it.name.equals(prioStr, ignoreCase = true) }
            ?: ReportPriority.MEDIA
    }

    fun formatIsoDate(iso: String?): String {
        if (iso.isNullOrBlank()) return "Reciente"
        return iso.take(10)
    }

    fun mapReportResponse(dto: ReporteResponse, isFollowed: Boolean = false): ReportItem {
        val existing = reports.find { it.id == dto.id.toString() || it.code == dto.codigo }
        val desc = dto.descripcion ?: "Sin descripción"
        val shortTitle = desc.lineSequence().firstOrNull()?.take(48)?.let {
            if (desc.length > 48) "$it..." else it
        } ?: "Reporte ciudadano"
        val citizenId = dto.ciudadano?.id
        val isOwn = currentUserId != null && citizenId == currentUserId

        val rawLat = dto.ubicacion?.latitud
        val rawLon = dto.ubicacion?.longitud
        val validColombiaCoords = rawLat != null && rawLon != null &&
            rawLat in -4.5..13.5 && rawLon in -82.0..-66.0
        val finalLat = if (validColombiaCoords) rawLat!! else 4.6351 + ((dto.id % 5) * 0.0042)
        val finalLon = if (validColombiaCoords) rawLon!! else -74.0703 - ((dto.id % 4) * 0.0038)

        return ReportItem(
            id = dto.id.toString(),
            code = dto.codigo ?: "CA-${dto.id}",
            authorName = dto.ciudadano?.fullName ?: "Ciudadano",
            isOwnReport = isOwn,
            category = dto.categoria?.nombre ?: "General",
            title = existing?.title?.takeIf { it.isNotBlank() && it != desc } ?: shortTitle,
            description = desc,
            location = dto.ubicacion?.direccion?.takeIf { it.isNotBlank() && it != "string" }
                ?: "Bogotá, Colombia (${String.format("%.4f", finalLat)}, ${String.format("%.4f", finalLon)})",
            status = parseStatus(dto.estado),
            priority = parsePriority(dto.prioridad),
            isPriorityOfficial = dto.estado != "REGISTRADO",
            createdAt = formatIsoDate(dto.fechaCreacion),
            hasUnreadUpdate = existing?.hasUnreadUpdate ?: false,
            isOverdue = existing?.isOverdue ?: false,
            dueInText = existing?.dueInText,
            supportCount = existing?.supportCount ?: 0,
            hasSupported = existing?.hasSupported ?: false,
            isFollowing = isFollowed || (existing?.isFollowing ?: false),
            assignedOperator = existing?.assignedOperator,
            rejectionReason = dto.motivoRechazo,
            reopenReason = existing?.reopenReason,
            hasReopenedOnce = dto.reabierto == true,
            closingObservation = existing?.closingObservation,
            closingDate = dto.fechaCierre?.let { formatIsoDate(it) },
            citizenRating = dto.calificacion,
            citizenConfirmationComment = existing?.citizenConfirmationComment,
            timeline = existing?.timeline ?: mutableListOf(),
            comments = existing?.comments ?: mutableListOf(),
            categoryId = dto.categoria?.id ?: 1,
            latitude = finalLat,
            longitude = finalLon
        )
    }

    fun upsertReports(dtos: List<ReporteResponse>, markFollowed: Boolean = false) {
        dtos.forEach { dto ->
            val mapped = mapReportResponse(dto, isFollowed = markFollowed)
            val idx = reports.indexOfFirst { it.id == mapped.id || it.code == mapped.code }
            if (idx >= 0) {
                reports[idx] = mapped
            } else {
                reports.add(mapped)
            }
        }
    }

    suspend fun refreshCategories() {
        try {
            val remote = ApiClient.apiService.getCategories(soloActivas = false)
            if (remote.isNotEmpty()) {
                categories.clear()
                remote.forEach { cat ->
                    categories.add(
                        CategoryItem(
                            id = cat.id.toString(),
                            name = cat.nombre,
                            description = cat.descripcion ?: "",
                            defaultPriority = parsePriority(cat.prioridadInicial),
                            isActive = cat.activa,
                            reportsInUseCount = reports.count { it.categoryId == cat.id },
                            numericId = cat.id
                        )
                    )
                }
            }
        } catch (_: Exception) {
        }
    }

    suspend fun refreshUsers() {
        try {
            val remote = ApiClient.apiService.getUsers()
            if (remote.isNotEmpty()) {
                staffUsers.clear()
                remote.forEach { u ->
                    val role = parseRole(u.rol?.nombre)
                    staffUsers.add(
                        StaffUserItem(
                            id = u.id.toString(),
                            fullName = u.fullName,
                            email = u.correo ?: "",
                            role = role,
                            isActive = u.activo,
                            activeLoad = 0,
                            maxLoad = 5,
                            statusDetail = role.permissionsSummary,
                            numericId = u.id
                        )
                    )
                    if (currentUserId == u.id) {
                        currentUserPhone = u.telefono ?: currentUserPhone
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    suspend fun refreshAllReportsForCurrentRole() {
        try {
            refreshCategories()
            // 1. Feed público
            try {
                val feed = ApiClient.apiService.getFeed()
                upsertReports(feed.content)
            } catch (_: Exception) {}

            // 2. Mis reportes y seguidos si hay usuario logueado
            val uid = currentUserId
            if (uid != null) {
                try {
                    val mine = ApiClient.apiService.getMyReports(uid)
                    upsertReports(mine.content)
                } catch (_: Exception) {}

                try {
                    val followed = ApiClient.apiService.getFollowedReports(uid)
                    upsertReports(followed.content, markFollowed = true)
                } catch (_: Exception) {}
            }

            // 3. Bandeja de gestión (permite ver también reportes en REGISTRADO, ADMITIDO, etc.)
            listOf("REGISTRADO", "ADMITIDO", "ASIGNADO", "EN_ATENCION", "CERRADO", "REABIERTO", "CONFIRMADO", "RECHAZADO").forEach { est ->
                try {
                    val page = ApiClient.apiService.getGestorInbox(estado = est)
                    upsertReports(page.content)
                } catch (_: Exception) {}
            }

            // 4. Vencidos
            try {
                val overdue = ApiClient.apiService.getOverdueReports()
                overdue.forEach { dto ->
                    val mapped = mapReportResponse(dto).apply {
                        isOverdue = true
                        dueInText = "Vencido"
                    }
                    val idx = reports.indexOfFirst { it.id == mapped.id }
                    if (idx >= 0) reports[idx] = mapped else reports.add(mapped)
                }
            } catch (_: Exception) {}

            // 5. Asignados al operador
            if (uid != null && currentRole == UserRole.OPERADOR) {
                try {
                    val assigned = ApiClient.apiService.getAssignedReports(uid)
                    upsertReports(assigned)
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
        }
    }

    suspend fun refreshNotifications() {
        val uid = currentUserId ?: return
        try {
            val page = ApiClient.apiService.getNotifications(uid)
            notifications.clear()
            page.content.forEach { n ->
                notifications.add(
                    NotificationItem(
                        id = n.id.toString(),
                        reportCode = n.reporte?.id?.toString() ?: n.reporte?.codigo ?: "1",
                        title = n.reporte?.codigo ?: "Aviso del sistema",
                        subtitle = n.mensaje ?: "Actualización en reporte",
                        timestamp = formatIsoDate(n.fecha),
                        isUnread = !n.leida
                    )
                )
            }
        } catch (_: Exception) {
        }
    }

    suspend fun refreshReportDetails(reportIdOrCode: String) {
        val report = getReportById(reportIdOrCode)
        val numericId = report?.id?.toLongOrNull() ?: reportIdOrCode.toLongOrNull() ?: return
        try {
            val detail = ApiClient.apiService.getReportDetail(numericId)
            upsertReports(listOf(detail))
            val target = getReportById(numericId.toString()) ?: return

            // Cargar historial (línea de tiempo)
            try {
                val history = ApiClient.apiService.getReportTimeline(numericId)
                target.timeline.clear()
                history.forEach { seg ->
                    val roleName = seg.autor?.rol?.nombre ?: "CIUDADANO"
                    target.timeline.add(
                        TimelineEvent(
                            id = seg.id.toString(),
                            title = seg.tipoAccion ?: "Actualización",
                            transition = listOfNotNull(seg.estadoAnterior, seg.estadoNuevo).joinToString(" → "),
                            description = seg.observacion ?: "",
                            author = seg.autor?.fullName ?: "Sistema",
                            roleLabel = parseRole(roleName).label,
                            timestamp = formatIsoDate(seg.fechaRegistro),
                            isOfficialAdvance = seg.tipoAccion == "AVANCE" || seg.tipoAccion == "CIERRE"
                        )
                    )
                }
            } catch (_: Exception) {}

            // Cargar comentarios
            try {
                val commentsPage = ApiClient.apiService.getReportComments(numericId)
                target.comments.clear()
                commentsPage.content.forEach { com ->
                    val authorRole = com.autor?.rol?.nombre
                    val isOfficial = authorRole == "GESTOR" || authorRole == "OPERADOR" || authorRole == "ADMINISTRADOR"
                    target.comments.add(
                        ReportComment(
                            id = com.id.toString(),
                            author = com.autor?.fullName ?: "Ciudadano",
                            isOwn = currentUserId != null && com.autor?.id == currentUserId,
                            isOfficial = isOfficial,
                            roleBadge = if (isOfficial) parseRole(authorRole).label else null,
                            timestamp = formatIsoDate(com.fechaCreacion),
                            content = com.contenido ?: "",
                            isDeleted = com.estado == "ELIMINADO",
                            isHiddenByModeration = com.estado == "OCULTO",
                            replyToAuthor = com.comentarioPadre?.autor?.fullName,
                            isEdited = com.fechaEdicion != null
                        )
                    )
                }
            } catch (_: Exception) {}
        } catch (_: Exception) {
        }
    }
}
