package com.example.ciudadactiva.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ciudadactiva.data.*
import com.example.ciudadactiva.navigation.Screen
import com.example.ciudadactiva.network.ApiClient
import com.example.ciudadactiva.network.models.AsignacionRequestDTO
import com.example.ciudadactiva.network.models.ComentarioRequestDTO
import com.example.ciudadactiva.network.models.ConfirmacionReporteDTO
import com.example.ciudadactiva.ui.components.*
import com.example.ciudadactiva.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    navController: NavController,
    reportId: String = "rep-0012"
) {
    val scope = rememberCoroutineScope()
    val report = remember(reportId, CiudadActivaRepository.reports.size) {
        CiudadActivaRepository.getReportById(reportId)
    }

    LaunchedEffect(reportId) {
        CiudadActivaRepository.refreshReportDetails(reportId)
        CiudadActivaRepository.refreshUsers()
    }
    
    if (report == null) {
        Scaffold(
            containerColor = CaNeutralLight,
            topBar = {
                CaTopAppBar(
                    title = "Cargando reporte...",
                    showLogo = false,
                    onBackClick = { navController.popBackStack() }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CaPrimaryText)
            }
        }
        return
    }

    var currentStatus by remember(report.status) { mutableStateOf(report.status) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Resumen, 1 = Línea de tiempo, 2 = Comentarios
    val currentRole = CiudadActivaRepository.currentRole

    // Interactive state states
    var hasSupported by remember { mutableStateOf(report.hasSupported) }
    var supportCount by remember(report.supportCount) { mutableIntStateOf(report.supportCount) }
    var isFollowing by remember { mutableStateOf(report.isFollowing) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    // Bottom sheets & modals
    var showConfirmResolutionSheet by remember { mutableStateOf(false) }
    var showReopenSheet by remember { mutableStateOf(false) }
    var showAssignSheet by remember { mutableStateOf(false) }
    var showReassignSheet by remember { mutableStateOf(false) }
    var showRejectSheet by remember { mutableStateOf(false) }
    var showModerateCommentSheet by remember { mutableStateOf(false) }
    var showStartAttentionDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    // Rating & Reopen form
    var citizenRating by remember { mutableIntStateOf(report.citizenRating ?: 5) }
    var confirmationComment by remember { mutableStateOf("") }
    var reopenReasonText by remember {
        mutableStateOf("El hueco aún dificulta el paso por el carril derecho. Solicito revisar la reparación.")
    }
    var rejectReasonText by remember {
        mutableStateOf("El daño reportado está dentro de un predio privado y no corresponde al espacio público.")
    }

    // Gestor assignment form
    val activeOperators = CiudadActivaRepository.staffUsers.filter {
        it.role == UserRole.OPERADOR && it.isActive
    }
    var selectedOperatorName by remember {
        mutableStateOf(report.assignedOperator ?: activeOperators.firstOrNull()?.fullName ?: "Operador Municipal")
    }
    var selectedOperatorId by remember {
        mutableLongStateOf(activeOperators.firstOrNull()?.numericId ?: 3L)
    }
    var selectedOfficialPriority by remember { mutableStateOf(report.priority) }
    var priorityJustification by remember {
        mutableStateOf("Riesgo inmediato para ciclistas en el carril derecho.")
    }
    var reassignReason by remember {
        mutableStateOf("Reasignación operativa para agilizar la atención del reporte.")
    }
    var isGestorAuthorConflict by remember { mutableStateOf(false) }

    // Comments state
    var newCommentText by remember { mutableStateOf("") }
    var commentErrorText by remember { mutableStateOf<String?>(null) }

    // 1. Confirm Resolution Bottom Sheet (`Confirmar · Sheet`)
    if (showConfirmResolutionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showConfirmResolutionSheet = false },
            containerColor = CaWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(text = "Confirmar resultado", style = Titulo20)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Cuéntanos cómo quedó la atención de tu reporte.",
                    style = Texto14Regular.copy(color = CaMuted)
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(text = "¿Cómo calificas el resultado?", style = Texto14Semibold)
                Spacer(modifier = Modifier.height(10.dp))
                CaStarRating(
                    rating = citizenRating,
                    onRatingChange = { citizenRating = it }
                )
                Spacer(modifier = Modifier.height(18.dp))
                CaTextField(
                    label = "Comentario (opcional)",
                    value = confirmationComment,
                    onValueChange = { confirmationComment = it },
                    placeholder = "Escribe tu comentario sobre el resultado",
                    singleLine = false,
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(20.dp))
                CaButton(
                    text = "Confirmar",
                    onClick = {
                        currentStatus = ReportStatus.CONFIRMADO
                        report.status = ReportStatus.CONFIRMADO
                        report.citizenRating = citizenRating
                        val finalComment = confirmationComment.ifBlank {
                            "Confirmo que el reporte quedó atendido. Gracias por la atención."
                        }
                        report.citizenConfirmationComment = finalComment
                        val uid = CiudadActivaRepository.currentUserId
                        val repLongId = report.id.toLongOrNull()
                        if (uid != null && repLongId != null) {
                            scope.launch {
                                try {
                                    ApiClient.apiService.confirmReportResult(
                                        repLongId,
                                        ConfirmacionReporteDTO(
                                            ciudadanoId = uid,
                                            calificacion = citizenRating,
                                            comentarioResultado = finalComment
                                        )
                                    )
                                } catch (_: Exception) { }
                            }
                        }
                        showConfirmResolutionSheet = false
                        selectedTab = 2
                        snackbarMessage = "Gracias por confirmar"
                    },
                    type = CaButtonType.ACCION
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // 2. Reopen Report Bottom Sheet (`Reabrir · Sheet`)
    if (showReopenSheet) {
        ModalBottomSheet(
            onDismissRequest = { showReopenSheet = false },
            containerColor = CaWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(text = "Reabrir reporte", style = Titulo20)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Cuéntanos por qué no estás conforme con el cierre.",
                    style = Texto14Regular.copy(color = CaMuted)
                )
                Spacer(modifier = Modifier.height(16.dp))
                CaTextField(
                    label = "Motivo de reapertura",
                    value = reopenReasonText,
                    onValueChange = { reopenReasonText = it },
                    singleLine = false,
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = CaBorder,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Solo puedes reabrir una vez",
                        style = Texto12Semibold.copy(color = CaNeutralDark),
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                CaButton(
                    text = "Reabrir reporte",
                    onClick = {
                        currentStatus = ReportStatus.REABIERTO
                        report.status = ReportStatus.REABIERTO
                        report.reopenReason = reopenReasonText
                        report.hasReopenedOnce = true
                        val uid = CiudadActivaRepository.currentUserId
                        val repLongId = report.id.toLongOrNull()
                        if (uid != null && repLongId != null) {
                            scope.launch {
                                try {
                                    ApiClient.apiService.reopenReport(repLongId, uid, reopenReasonText)
                                } catch (_: Exception) { }
                            }
                        }
                        showReopenSheet = false
                        snackbarMessage = "Reporte reabierto"
                    },
                    type = CaButtonType.ACCION
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // 2b. Reject Report Bottom Sheet (`Rechazar · Sheet`)
    if (showRejectSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRejectSheet = false },
            containerColor = CaWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(text = "Rechazar reporte", style = Titulo20)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Indica el motivo por el cual no procede este reporte.",
                    style = Texto14Regular.copy(color = CaMuted)
                )
                Spacer(modifier = Modifier.height(16.dp))
                CaTextField(
                    label = "Motivo de rechazo *",
                    value = rejectReasonText,
                    onValueChange = { rejectReasonText = it },
                    singleLine = false,
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(18.dp))
                CaButton(
                    text = "Confirmar rechazo",
                    onClick = {
                        currentStatus = ReportStatus.RECHAZADO
                        report.status = ReportStatus.RECHAZADO
                        report.rejectionReason = rejectReasonText
                        val uid = CiudadActivaRepository.currentUserId ?: 2L
                        val repLongId = report.id.toLongOrNull()
                        if (repLongId != null) {
                            scope.launch {
                                try {
                                    ApiClient.apiService.validateReport(
                                        id = repLongId,
                                        gestorId = uid,
                                        admitir = false,
                                        motivo = rejectReasonText
                                    )
                                } catch (_: Exception) { }
                            }
                        }
                        showRejectSheet = false
                        snackbarMessage = "Reporte rechazado"
                    },
                    type = CaButtonType.DESTRUCTIVO
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // 3. Gestor Assign Operator Bottom Sheet (`Asignar · Sheet`)
    if (showAssignSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAssignSheet = false },
            containerColor = CaWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(text = "Asignar operador", style = Titulo20)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${report.code} · Elige un operador activo.",
                    style = Texto14Regular.copy(color = CaMuted)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Operadores activos · carga actual",
                    style = Texto12Semibold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (activeOperators.isNotEmpty()) {
                    activeOperators.forEachIndexed { idx, op ->
                        val load = (idx + 1).coerceAtMost(4)
                        OperatorLoadOptionRow(
                            name = op.fullName,
                            load = load,
                            maxLoad = 5,
                            selected = selectedOperatorName == op.fullName,
                            onClick = {
                                selectedOperatorName = op.fullName
                                selectedOperatorId = op.numericId
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                } else {
                    OperatorLoadOptionRow(
                        name = "Diego R.",
                        load = 3,
                        maxLoad = 5,
                        selected = selectedOperatorName == "Diego R.",
                        onClick = {
                            selectedOperatorName = "Diego R."
                            selectedOperatorId = 3L
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OperatorLoadOptionRow(
                        name = "Ana P.",
                        load = 2,
                        maxLoad = 5,
                        selected = selectedOperatorName == "Ana P.",
                        onClick = {
                            selectedOperatorName = "Ana P."
                            selectedOperatorId = 3L
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = "No se puede asignar a operadores con carga 5/5.",
                    style = Texto12Regular.copy(color = CaMuted)
                )

                Spacer(modifier = Modifier.height(14.dp))
                CaDropdownSelector(
                    label = "Prioridad oficial *",
                    selectedValue = "${selectedOfficialPriority.label} · seleccionada",
                    options = ReportPriority.entries.map { it.label },
                    onOptionSelected = { label ->
                        selectedOfficialPriority = ReportPriority.entries.find { it.label == label } ?: ReportPriority.ALTA
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(ReportPriority.BAJA, ReportPriority.MEDIA, ReportPriority.ALTA).forEach { p ->
                        CaPriorityChip(
                            priority = p,
                            selected = selectedOfficialPriority == p,
                            onClick = { selectedOfficialPriority = p }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Sugerida: Alta. Si la prioridad oficial cambia, la justificación es obligatoria.",
                    style = Texto12Regular.copy(color = CaMuted)
                )
                Spacer(modifier = Modifier.height(12.dp))
                CaTextField(
                    label = "Justificación del cambio *",
                    value = priorityJustification,
                    onValueChange = { priorityJustification = it },
                    singleLine = false,
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = CaNeutralLight,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Vencimiento calculado · ${selectedOfficialPriority.label}",
                            style = Texto12Semibold
                        )
                        Text(
                            text = "Fecha de asignación + SLA de prioridad oficial (${selectedOfficialPriority.label}).",
                            style = Texto12Regular.copy(color = CaMuted)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                CaButton(
                    text = "Asignar",
                    onClick = {
                        currentStatus = ReportStatus.ASIGNADO
                        report.status = ReportStatus.ASIGNADO
                        report.assignedOperator = selectedOperatorName
                        report.priority = selectedOfficialPriority
                        report.isPriorityOfficial = true
                        val gestorId = CiudadActivaRepository.currentUserId ?: 2L
                        val repLongId = report.id.toLongOrNull()
                        if (repLongId != null) {
                            scope.launch {
                                try {
                                    ApiClient.apiService.assignOperator(
                                        id = repLongId,
                                        request = AsignacionRequestDTO(
                                            gestorId = gestorId,
                                            operadorId = selectedOperatorId,
                                            nuevaPrioridad = selectedOfficialPriority.name,
                                            motivoCambioPrioridad = priorityJustification
                                        )
                                    )
                                    CiudadActivaRepository.refreshReportDetails(report.id)
                                } catch (_: Exception) { }
                            }
                        }
                        showAssignSheet = false
                        snackbarMessage = "Reporte asignado a $selectedOperatorName"
                    },
                    type = CaButtonType.ACCION
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // 4. Gestor Reassign Operator Bottom Sheet (`Reasignar · Sheet`)
    if (showReassignSheet) {
        ModalBottomSheet(
            onDismissRequest = { showReassignSheet = false },
            containerColor = CaWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(text = "Reasignar operador", style = Titulo20)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${report.code} · Asignado actualmente a ${report.assignedOperator ?: "Diego R."}",
                    style = Texto14Regular.copy(color = CaMuted)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Selecciona el nuevo operador", style = Texto12Semibold)
                Spacer(modifier = Modifier.height(8.dp))
                if (activeOperators.isNotEmpty()) {
                    activeOperators.forEachIndexed { idx, op ->
                        OperatorLoadOptionRow(
                            name = op.fullName,
                            load = (idx + 1).coerceAtMost(4),
                            maxLoad = 5,
                            selected = selectedOperatorName == op.fullName,
                            onClick = {
                                selectedOperatorName = op.fullName
                                selectedOperatorId = op.numericId
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                } else {
                    OperatorLoadOptionRow(
                        name = "Ana P.",
                        load = 2,
                        maxLoad = 5,
                        selected = true,
                        onClick = { selectedOperatorName = "Ana P." }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                CaTextField(
                    label = "Motivo de la reasignación *",
                    value = reassignReason,
                    onValueChange = { reassignReason = it },
                    helperText = "Obligatorio para reasignar el reporte.",
                    singleLine = false,
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = WarningBannerBg,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Quedará en el historial y no se puede borrar",
                        style = Texto12Regular.copy(color = CaNeutralDark),
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                CaButton(
                    text = "Reasignar operador",
                    onClick = {
                        report.assignedOperator = selectedOperatorName
                        val gestorId = CiudadActivaRepository.currentUserId ?: 2L
                        val repLongId = report.id.toLongOrNull()
                        if (repLongId != null) {
                            scope.launch {
                                try {
                                    ApiClient.apiService.assignOperator(
                                        id = repLongId,
                                        request = AsignacionRequestDTO(
                                            gestorId = gestorId,
                                            operadorId = selectedOperatorId,
                                            nuevaPrioridad = selectedOfficialPriority.name,
                                            motivoReasignacion = reassignReason
                                        )
                                    )
                                } catch (_: Exception) { }
                            }
                        }
                        showReassignSheet = false
                        snackbarMessage = "Reporte reasignado a $selectedOperatorName"
                    },
                    type = CaButtonType.ACCION
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // 5. Operador Start Attention Dialog (`Iniciar · Diálogo`)
    if (showStartAttentionDialog) {
        CaConfirmationDialog(
            title = "¿Iniciar atención de este reporte?",
            message = "${report.code} · El reporte pasará a En atención y ocupará un cupo de tu capacidad.",
            confirmText = "Iniciar atención",
            cancelText = "Volver",
            confirmType = CaButtonType.ACCION,
            onConfirm = {
                currentStatus = ReportStatus.EN_ATENCION
                report.status = ReportStatus.EN_ATENCION
                val opId = CiudadActivaRepository.currentUserId ?: 3L
                val repLongId = report.id.toLongOrNull()
                if (repLongId != null) {
                    scope.launch {
                        try {
                            ApiClient.apiService.startAttention(repLongId, opId)
                            CiudadActivaRepository.refreshReportDetails(report.id)
                        } catch (_: Exception) { }
                    }
                }
                showStartAttentionDialog = false
                snackbarMessage = "El reporte pasó a En atención"
            },
            onDismiss = { showStartAttentionDialog = false }
        )
    }

    // 6. Moderate Comment Sheet (`Moderar · Sheet`)
    if (showModerateCommentSheet) {
        var modReason by remember { mutableStateOf("Contiene datos personales de un tercero.") }
        ModalBottomSheet(
            onDismissRequest = { showModerateCommentSheet = false },
            containerColor = CaWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(text = "Ocultar comentario por moderación", style = Titulo20)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "El contenido dejará de ser público y el autor verá el motivo.",
                    style = Texto14Regular.copy(color = CaMuted)
                )
                Spacer(modifier = Modifier.height(14.dp))
                CaTextField(
                    label = "Motivo de moderación *",
                    value = modReason,
                    onValueChange = { modReason = it },
                    singleLine = false,
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(18.dp))
                CaButton(
                    text = "Ocultar comentario",
                    onClick = {
                        val firstCommentId = report.comments.firstOrNull()?.id?.toLongOrNull()
                        val modId = CiudadActivaRepository.currentUserId ?: 2L
                        if (firstCommentId != null) {
                            scope.launch {
                                try {
                                    ApiClient.apiService.moderateComment(
                                        id = firstCommentId,
                                        moderadorId = modId,
                                        motivo = modReason
                                    )
                                    CiudadActivaRepository.refreshReportDetails(report.id)
                                } catch (_: Exception) { }
                            }
                        }
                        showModerateCommentSheet = false
                        snackbarMessage = "Comentario ocultado por moderación"
                    },
                    type = CaButtonType.DESTRUCTIVO
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    Scaffold(
        containerColor = CaNeutralLight,
        topBar = {
            CaTopAppBar(
                title = "Detalle del reporte",
                showLogo = false,
                onBackClick = { navController.popBackStack() },
                trailingContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                val nextFollow = !isFollowing
                                isFollowing = nextFollow
                                report.isFollowing = nextFollow
                                val uid = CiudadActivaRepository.currentUserId
                                val repLongId = report.id.toLongOrNull()
                                if (uid != null && repLongId != null) {
                                    scope.launch {
                                        try {
                                            if (nextFollow) {
                                                ApiClient.apiService.followReport(repLongId, uid)
                                            } else {
                                                ApiClient.apiService.unfollowReport(repLongId, uid)
                                            }
                                        } catch (_: Exception) { }
                                    }
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isFollowing) "Siguiendo" else "Seguir",
                            style = Texto14Semibold.copy(color = CaPrimaryText)
                        )
                        if (isFollowing) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = CaPrimaryText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    IconButton(onClick = { showModerateCommentSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Más opciones",
                            tint = CaNeutralDark
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Report Header (`RP-0012 · Vías`, `Hueco en la vía`, Author & Date, Status + Priority Pills)
            Text(
                text = "${report.code} · ${report.category}",
                style = Texto14Semibold.copy(color = CaPrimaryText)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = report.title,
                style = Titulo24
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${report.authorName} · ${report.createdAt}",
                style = Texto12Regular.copy(color = CaMuted)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CaStatusChip(currentStatus)
                CaPriorityChip(selectedOfficialPriority)
                if (currentRole == UserRole.GESTOR || currentRole == UserRole.OPERADOR) {
                    Text(
                        text = if (report.isPriorityOfficial || currentStatus == ReportStatus.ASIGNADO) "Oficial" else "Sugerida",
                        style = Texto12Regular.copy(color = CaMuted)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            CaSegmentedTabs(
                tabs = listOf("Resumen", "Línea de tiempo", "Comentarios"),
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    // =====================================================
                    // TAB 0: RESUMEN (Status & Role specific cards + Map + Photos + CTAs)
                    // =====================================================

                    // Status banners (`Detalle · Reabierto`, `Detalle · Cancelado`, `Detalle · Rechazado`)
                    if (currentStatus == ReportStatus.REABIERTO) {
                        if (currentRole == UserRole.OPERADOR) {
                            Surface(
                                color = WarningBannerBg,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "El ciudadano no quedó conforme",
                                        style = Texto14Semibold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = report.reopenReason ?: reopenReasonText,
                                        style = Texto12Regular.copy(color = CaMuted)
                                    )
                                }
                            }
                        } else {
                            Surface(
                                color = StatusAdmitidoBg,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Tu reporte está en revisión",
                                        style = Texto14Semibold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Recibimos tu solicitud de reapertura. El equipo revisará el resultado de la intervención.",
                                        style = Texto12Regular.copy(color = CaMuted)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    if (currentStatus == ReportStatus.CANCELADO) {
                        Surface(
                            color = CaBorder,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Reporte cancelado por su autor",
                                    style = Texto14Semibold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Este reporte ya no está activo.",
                                    style = Texto12Regular.copy(color = CaMuted)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    if (currentStatus == ReportStatus.RECHAZADO) {
                        Surface(
                            color = ErrorBannerBg,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Reporte rechazado en validación",
                                    style = Texto14Semibold.copy(color = CaSecondary)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = report.rejectionReason
                                        ?: "El daño está en un predio privado; no corresponde al espacio público.",
                                    style = Texto12Regular.copy(color = CaSecondary)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Map block (shown in active states) - Real map of Colombia at exact coordinates
                    if (currentStatus != ReportStatus.CERRADO && currentStatus != ReportStatus.CANCELADO) {
                        CaCityMapPlaceholder(
                            locationLabel = report.location,
                            height = 210.dp,
                            showGpsBadge = true,
                            latitude = report.latitude,
                            longitude = report.longitude
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Text(
                        text = report.location,
                        style = Texto12Regular.copy(color = CaMuted)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = report.description,
                        style = Texto14Regular
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Closing Result Evidence Card (`Detalle · Cerrado`)
                    if (currentStatus == ReportStatus.CERRADO) {
                        Surface(
                            color = CaWhite,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, CaBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Resultado del cierre", style = Titulo16)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = report.closingDate ?: "23 oct. 2026 · 3:20 p. m.",
                                    style = Texto12Regular.copy(color = CaMuted)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    CaReportThumbnail(size = 96.dp, isRealPhotoStyle = true)
                                    CaReportThumbnail(size = 96.dp, isRealPhotoStyle = true)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = report.closingObservation
                                        ?: "Terminamos el parcheo y habilitamos el carril para el tránsito.",
                                    style = Texto14Regular
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            color = StatusAdmitidoBg,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Tienes 3 días para confirmar o comentar",
                                style = Texto12Semibold.copy(color = CaNeutralDark),
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        CaButton(
                            text = "Confirmar resultado",
                            onClick = { showConfirmResolutionSheet = true },
                            type = CaButtonType.ACCION
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        CaButton(
                            text = "No estoy conforme",
                            onClick = { showReopenSheet = true },
                            type = CaButtonType.SECUNDARIO
                        )
                    } else {
                        // Photos of the report (3 thumbnails)
                        Text(
                            text = "Fotos del reporte · 3",
                            style = Texto12Semibold.copy(color = CaNeutralDark)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CaReportThumbnail(size = 96.dp, isRealPhotoStyle = true)
                            CaReportThumbnail(size = 96.dp, isRealPhotoStyle = true)
                            CaReportThumbnail(size = 96.dp, isRealPhotoStyle = true)
                        }

                        if (currentStatus == ReportStatus.REABIERTO && currentRole == UserRole.CIUDADANO) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                color = CaBorder,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(text = "Motivo de reapertura", style = Texto12Semibold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = report.reopenReason ?: reopenReasonText,
                                        style = Texto12Regular.copy(color = CaMuted)
                                    )
                                }
                            }
                        }

                        // Assigned Operator Info Box (`Asignar · Éxito`, `Operador · Detalle Asignado`)
                        if (currentStatus == ReportStatus.ASIGNADO ||
                            currentStatus == ReportStatus.EN_ATENCION ||
                            (currentStatus == ReportStatus.REABIERTO && currentRole == UserRole.OPERADOR)
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                color = StatusAsignadoBg,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Operador asignado · ${report.assignedOperator ?: "Diego R."}",
                                        style = Texto14Semibold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Prioridad oficial: ${selectedOfficialPriority.label}",
                                        style = Texto12Regular.copy(color = CaMuted)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (currentStatus == ReportStatus.REABIERTO) {
                                            "Vencido"
                                        } else if (currentRole == UserRole.OPERADOR) {
                                            "Vence en 5 h"
                                        } else {
                                            "Vence: fecha de asignación + SLA de ${selectedOfficialPriority.label}."
                                        },
                                        style = Texto12Semibold.copy(
                                            color = if (currentStatus == ReportStatus.REABIERTO) CaSecondary else StatusEnAtencionText
                                        )
                                    )
                                }
                            }
                        }

                        // Notifications toggle & Upvote button for Citizen
                        if (currentRole == UserRole.CIUDADANO && currentStatus == ReportStatus.ADMITIDO) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Notificaciones de este reporte",
                                    style = Texto14Semibold
                                )
                                Switch(
                                    checked = notificationsEnabled,
                                    onCheckedChange = { notificationsEnabled = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = CaNeutralDark,
                                        checkedTrackColor = CaPrimary
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "$supportCount personas lo vieron",
                                style = Texto12Regular.copy(color = CaMuted),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            CaButton(
                                text = if (hasSupported) "Ya apoyaste" else "Yo también lo vi",
                                onClick = {
                                    val uid = CiudadActivaRepository.currentUserId
                                    val repLongId = report.id.toLongOrNull()
                                    if (!hasSupported) {
                                        hasSupported = true
                                        supportCount += 1
                                        report.hasSupported = true
                                        report.supportCount = supportCount
                                        if (uid != null && repLongId != null) {
                                            scope.launch {
                                                try {
                                                    ApiClient.apiService.supportReport(repLongId, uid)
                                                } catch (_: Exception) { }
                                            }
                                        }
                                    } else {
                                        hasSupported = false
                                        supportCount -= 1
                                        report.hasSupported = false
                                        report.supportCount = supportCount
                                        if (uid != null && repLongId != null) {
                                            scope.launch {
                                                try {
                                                    ApiClient.apiService.unsupportReport(repLongId, uid)
                                                } catch (_: Exception) { }
                                            }
                                        }
                                    }
                                },
                                type = CaButtonType.ACCION
                            )
                        }

                        // Gestor Actions (`Gestor · Detalle Registrado`, `Detalle Admitido`, `Asignar · Éxito`, `Gestor · Es el autor`)
                        if (currentRole == UserRole.GESTOR) {
                            Spacer(modifier = Modifier.height(16.dp))
                            if (isGestorAuthorConflict) {
                                Surface(
                                    color = WarningBannerBg,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Eres el autor de este reporte. Otro gestor debe validarlo o asignarlo.",
                                        style = Texto12Semibold.copy(color = StatusEnAtencionText),
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            } else if (currentStatus == ReportStatus.REGISTRADO) {
                                CaButton(
                                    text = "Admitir reporte",
                                    onClick = {
                                        currentStatus = ReportStatus.ADMITIDO
                                        report.status = ReportStatus.ADMITIDO
                                        val gestorId = CiudadActivaRepository.currentUserId ?: 2L
                                        val repLongId = report.id.toLongOrNull()
                                        if (repLongId != null) {
                                            scope.launch {
                                                try {
                                                    ApiClient.apiService.validateReport(
                                                        id = repLongId,
                                                        gestorId = gestorId,
                                                        admitir = true,
                                                        motivo = "Reporte admitido para gestión."
                                                    )
                                                    CiudadActivaRepository.refreshReportDetails(report.id)
                                                } catch (_: Exception) { }
                                            }
                                        }
                                        snackbarMessage = "Reporte admitido con éxito"
                                    },
                                    type = CaButtonType.ACCION
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                CaButton(
                                    text = "Rechazar reporte",
                                    onClick = { showRejectSheet = true },
                                    type = CaButtonType.DESTRUCTIVO
                                )
                            } else if (currentStatus == ReportStatus.ASIGNADO) {
                                CaButton(
                                    text = "Reasignar operador",
                                    onClick = { showReassignSheet = true },
                                    type = CaButtonType.SECUNDARIO
                                )
                            } else {
                                CaButton(
                                    text = "Asignar operador",
                                    onClick = { showAssignSheet = true },
                                    type = CaButtonType.ACCION
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                CaButton(
                                    text = "Ajustar prioridad",
                                    onClick = { showAssignSheet = true },
                                    type = CaButtonType.SECUNDARIO
                                )
                            }
                        }

                        // Operador Actions (`Operador · Detalle Asignado`, `Iniciar · Éxito`, `Detalle Reabierto`)
                        if (currentRole == UserRole.OPERADOR) {
                            Spacer(modifier = Modifier.height(18.dp))
                            when (currentStatus) {
                                ReportStatus.ASIGNADO -> {
                                    CaButton(
                                        text = "Iniciar atención",
                                        onClick = { showStartAttentionDialog = true },
                                        type = CaButtonType.ACCION
                                    )
                                }
                                ReportStatus.REABIERTO -> {
                                    CaButton(
                                        text = "Retomar reporte",
                                        onClick = {
                                            currentStatus = ReportStatus.EN_ATENCION
                                            report.status = ReportStatus.EN_ATENCION
                                            val opId = CiudadActivaRepository.currentUserId ?: 3L
                                            val repLongId = report.id.toLongOrNull()
                                            if (repLongId != null) {
                                                scope.launch {
                                                    try {
                                                        ApiClient.apiService.startAttention(repLongId, opId)
                                                    } catch (_: Exception) { }
                                                }
                                            }
                                            snackbarMessage = "El reporte pasó a En atención"
                                        },
                                        type = CaButtonType.ACCION
                                    )
                                }
                                ReportStatus.EN_ATENCION -> {
                                    CaButton(
                                        text = "Registrar avance",
                                        onClick = {
                                            navController.navigate(Screen.OperadorProgressForm.createRoute(report.id))
                                        },
                                        type = CaButtonType.ACCION
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    CaButton(
                                        text = "Cerrar reporte",
                                        onClick = {
                                            navController.navigate(Screen.OperadorCloseReport.createRoute(report.id))
                                        },
                                        type = CaButtonType.SECUNDARIO
                                    )
                                }
                                else -> {}
                            }
                        }
                    }

                    if (snackbarMessage != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        CaSnackbarBanner(
                            message = snackbarMessage!!,
                            isError = false
                        )
                    }
                }

                1 -> {
                    // =====================================================
                    // TAB 1: LÍNEA DE TIEMPO (`Detalle · Línea de tiempo`, `Línea corta`, `Avance · Éxito`)
                    // =====================================================
                    var shortTimeline by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (shortTimeline) "2 eventos" else "${report.timeline.size} eventos",
                            style = Texto12Semibold
                        )
                        Text(
                            text = if (shortTimeline) "Ver todos (${report.timeline.size} eventos)" else "Del más antiguo al reciente",
                            style = Texto12Regular.copy(color = CaPrimaryText),
                            modifier = Modifier.clickable { shortTimeline = !shortTimeline }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val eventsToShow = if (shortTimeline) report.timeline.take(2) else report.timeline

                    eventsToShow.forEachIndexed { index, ev ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(StatusAdmitidoBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (index) {
                                            0 -> Icons.Outlined.NoteAdd
                                            1 -> Icons.Outlined.FactCheck
                                            2 -> Icons.Outlined.PersonAddAlt
                                            else -> Icons.Outlined.Engineering
                                        },
                                        contentDescription = null,
                                        tint = CaPrimaryText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                if (index < eventsToShow.lastIndex) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(90.dp)
                                            .background(CaBorder)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = ev.title, style = Titulo16)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = ev.transition,
                                    style = Texto12Semibold.copy(color = CaPrimaryText)
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                if (ev.isOfficialAdvance) {
                                    Surface(
                                        color = CaWhite,
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, CaPrimary),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Verified,
                                                    contentDescription = null,
                                                    tint = CaPrimaryText,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Column {
                                                    Text(
                                                        text = "${ev.author} · ${ev.roleLabel}",
                                                        style = Texto12Semibold
                                                    )
                                                    Text(
                                                        text = ev.timestamp,
                                                        style = Texto12Regular.copy(color = CaMuted)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            CaRoleChip("Avance oficial")
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(text = ev.description, style = Texto14Regular)
                                        }
                                    }
                                    if (ev.photoLabel != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        CaReportThumbnail(size = 64.dp, isRealPhotoStyle = true)
                                    }
                                } else {
                                    Text(text = ev.description, style = Texto14Regular)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${ev.author} · ${ev.roleLabel}",
                                        style = Texto12Semibold
                                    )
                                    Text(
                                        text = ev.timestamp,
                                        style = Texto12Regular.copy(color = CaMuted)
                                    )
                                }
                                Spacer(modifier = Modifier.height(18.dp))
                            }
                        }
                    }

                    HorizontalDivider(color = CaBorder)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = CaMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Los registros no se pueden modificar",
                            style = Texto12Regular.copy(color = CaMuted)
                        )
                    }
                }

                2 -> {
                    // =====================================================
                    // TAB 2: COMENTARIOS
                    // =====================================================
                    if (currentStatus == ReportStatus.CONFIRMADO) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = "Tu calificación", style = Texto14Semibold)
                            CaStarRating(rating = citizenRating)
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${report.comments.size} comentarios",
                            style = Texto12Semibold
                        )
                        Text(text = "Más antiguos primero", style = Texto12Regular.copy(color = CaMuted))
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    if (currentStatus == ReportStatus.CONFIRMADO) {
                        Surface(
                            color = CaWhite,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, CaBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(text = "Comentarios cerrados", style = Texto14Semibold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Este reporte fue confirmado. Los comentarios están cerrados.",
                                    style = Texto12Regular.copy(color = CaMuted)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (report.comments.isNotEmpty()) {
                        report.comments.forEach { c ->
                            if (c.replyToAuthor != null) {
                                Text(
                                    text = "En respuesta a ${c.replyToAuthor}",
                                    style = Texto12Regular.copy(color = CaMuted),
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                            CommentCardItem(
                                author = c.author,
                                timestamp = c.timestamp,
                                content = when {
                                    c.isHiddenByModeration -> "Ocultado por moderación"
                                    c.isDeleted -> "Comentario eliminado"
                                    else -> c.content
                                },
                                isOfficial = c.isOfficial,
                                isMutedText = c.isHiddenByModeration || c.isDeleted,
                                modifier = if (c.replyToAuthor != null) Modifier.padding(start = 12.dp) else Modifier
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    } else {
                        Surface(
                            color = CaWhite,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, CaBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Aún no hay comentarios en este reporte.",
                                style = Texto14Regular.copy(color = CaMuted),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (currentStatus != ReportStatus.CONFIRMADO) {
                        Text(text = "Participa en la conversación", style = Titulo16)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tu comentario será público. No compartas datos personales y mantén un trato respetuoso.",
                            style = Texto12Regular.copy(color = CaMuted)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        CaTextField(
                            label = "Tu comentario",
                            value = newCommentText,
                            onValueChange = {
                                newCommentText = it
                                commentErrorText = null
                            },
                            placeholder = "Escribe tu comentario...",
                            maxLength = 500,
                            errorMessage = commentErrorText
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        CaButton(
                            text = "Enviar comentario",
                            onClick = {
                                if (newCommentText.isBlank()) {
                                    commentErrorText = "Escribe un comentario antes de enviar."
                                } else {
                                    val commentToSend = newCommentText.trim()
                                    report.comments.add(
                                        ReportComment(
                                            id = "com-new-${report.comments.size + 1}",
                                            author = "${CiudadActivaRepository.currentUserName} · Tú",
                                            isOwn = true,
                                            timestamp = "Ahora",
                                            content = commentToSend
                                        )
                                    )
                                    val uid = CiudadActivaRepository.currentUserId
                                    val repLongId = report.id.toLongOrNull()
                                    if (uid != null && repLongId != null) {
                                        scope.launch {
                                            try {
                                                ApiClient.apiService.createComment(
                                                    id = repLongId,
                                                    request = ComentarioRequestDTO(
                                                        autorId = uid,
                                                        contenido = commentToSend
                                                    )
                                                )
                                                CiudadActivaRepository.refreshReportDetails(report.id)
                                            } catch (_: Exception) { }
                                        }
                                    }
                                    newCommentText = ""
                                    snackbarMessage = "Comentario publicado"
                                }
                            },
                            type = CaButtonType.PRIMARIO
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun OperatorLoadOptionRow(
    name: String,
    load: Int,
    maxLoad: Int,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) StatusAdmitidoBg else CaWhite,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (selected) CaPrimaryText else CaBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        style = Texto14Semibold.copy(
                            color = if (enabled) CaNeutralDark else CaMuted
                        )
                    )
                    if (selected) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = CaPrimaryText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = if (load >= maxLoad) "$load/$maxLoad · Carga máxima" else "$load/$maxLoad",
                    style = Texto12Regular.copy(color = CaMuted)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { load.toFloat() / maxLoad.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = if (load >= maxLoad) CaMuted else CaPrimaryText,
                trackColor = CaBorder
            )
        }
    }
}

@Composable
fun CommentCardItem(
    author: String,
    timestamp: String,
    content: String,
    isOfficial: Boolean,
    isMutedText: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CaWhite,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isOfficial) 1.5.dp else 1.dp,
            color = if (isOfficial) CaPrimary else CaBorder
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isOfficial) Icons.Outlined.Verified else Icons.Outlined.PersonOutline,
                    contentDescription = null,
                    tint = CaPrimaryText,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = author, style = Texto14Semibold)
                    Text(
                        text = timestamp,
                        style = Texto12Regular.copy(color = CaMuted)
                    )
                }
            }
            if (isOfficial) {
                Spacer(modifier = Modifier.height(8.dp))
                CaRoleChip("Respuesta oficial")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = Texto14Regular.copy(
                    color = if (isMutedText) CaMuted else CaNeutralDark
                )
            )
        }
    }
}
