package com.example.ciudadactiva.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ciudadactiva.data.*
import com.example.ciudadactiva.navigation.Screen
import com.example.ciudadactiva.network.ApiClient
import com.example.ciudadactiva.ui.components.*
import com.example.ciudadactiva.ui.theme.*
import kotlinx.coroutines.launch

// =========================================================================
// 1. GESTOR INBOX & GENERAL REPORTS (`Gestor · Bandeja` & `Gestor · Reportes generales`)
// =========================================================================
@Composable
fun GestorInboxScreen(navController: NavController) {
    var selectedFilter by remember { mutableStateOf("Todos") }

    LaunchedEffect(Unit) {
        CiudadActivaRepository.refreshAllReportsForCurrentRole()
        CiudadActivaRepository.refreshUsers()
        CiudadActivaRepository.refreshNotifications()
    }

    val unreadCount = CiudadActivaRepository.notifications.count { it.isUnread }

    Scaffold(
        containerColor = CaNeutralLight,
        topBar = {
            CaTopAppBar(
                title = "Bandeja de gestión",
                showLogo = true,
                notificationCount = unreadCount,
                onNotificationClick = { navController.navigate(Screen.Notifications.route) }
            )
        },
        bottomBar = {
            CaBottomNavigationBar(
                role = UserRole.GESTOR,
                currentRoute = Screen.GestorInbox.route,
                onNavigate = { route ->
                    navController.navigate(route) { launchSingleTop = true }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Valida reportes nuevos, asigna operadores activos y supervisa vencimientos.",
                style = Texto12Regular.copy(color = CaMuted)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Todos", "Registrado", "Admitido", "Asignado", "Vencidos").forEach { f ->
                    CaFilterChip(
                        label = f,
                        selected = selectedFilter == f,
                        onClick = { selectedFilter = f }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val list = CiudadActivaRepository.reports.filter { rep ->
                when (selectedFilter) {
                    "Registrado" -> rep.status == ReportStatus.REGISTRADO
                    "Admitido" -> rep.status == ReportStatus.ADMITIDO
                    "Asignado" -> rep.status == ReportStatus.ASIGNADO || rep.status == ReportStatus.EN_ATENCION
                    "Vencidos" -> rep.isOverdue
                    else -> true
                }
            }

            list.forEach { rep ->
                CaReportCard(
                    report = rep,
                    onClick = {
                        CiudadActivaRepository.currentRole = UserRole.GESTOR
                        navController.navigate(Screen.ReportDetail.createRoute(rep.id))
                    },
                    showPriorityRow = true
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

// =========================================================================
// 2. INDICATORS DASHBOARD (`Gestor · Indicadores`, `Indicadores · Vacío`, `Indicadores · Error`)
// =========================================================================
@Composable
fun IndicatorsScreen(navController: NavController) {
    var uiState by remember { mutableStateOf("Datos") } // Datos, Vacío, Error

    LaunchedEffect(Unit) {
        CiudadActivaRepository.refreshAllReportsForCurrentRole()
    }

    val totalReports = CiudadActivaRepository.reports.size.coerceAtLeast(1)
    val openReports = CiudadActivaRepository.reports.count {
        it.status != ReportStatus.CERRADO &&
            it.status != ReportStatus.CONFIRMADO &&
            it.status != ReportStatus.RECHAZADO &&
            it.status != ReportStatus.CANCELADO
    }
    val overdueReports = CiudadActivaRepository.reports.count { it.isOverdue }

    Scaffold(
        containerColor = CaNeutralLight,
        topBar = {
            CaTopAppBar(
                title = "Indicadores",
                showLogo = true
            )
        },
        bottomBar = {
            CaBottomNavigationBar(
                role = CiudadActivaRepository.currentRole,
                currentRoute = Screen.Indicators.route,
                onNavigate = { route ->
                    navController.navigate(route) { launchSingleTop = true }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Period Filter Pill (`Periodo: 1–27 oct. 2026` or `Periodo: sep. 2026`)
            Surface(
                color = StatusAdmitidoBg,
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, CaPrimaryText),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = CaPrimaryText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState == "Vacío") "Periodo: sep. 2026" else "Periodo: 1–27 oct. 2026",
                        style = Texto12Semibold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (uiState) {
                "Vacío" -> {
                    Spacer(modifier = Modifier.height(32.dp))
                    CaEmptyStateCard(
                        title = "Aún no hay datos para este periodo",
                        subtitle = "Selecciona otro periodo para consultar los indicadores de gestión.",
                        ctaText = "Cambiar periodo",
                        onCtaClick = { uiState = "Datos" }
                    )
                }
                "Error" -> {
                    Spacer(modifier = Modifier.height(32.dp))
                    CaErrorStateCard(
                        title = "No pudimos cargar los indicadores",
                        subtitle = "Revisa tu conexión a internet e intenta de nuevo.",
                        onRetry = { uiState = "Datos" }
                    )
                }
                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        KpiStatCard(
                            label = "Total de reportes",
                            value = totalReports.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        KpiStatCard(
                            label = "Abiertos",
                            value = openReports.toString(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        KpiStatCard(
                            label = "Vencidos · de $openReports abiertos",
                            value = overdueReports.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        KpiStatCard(
                            label = "Tiempo promedio de atención",
                            value = "27 h",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Reportes por categoría
                    Surface(
                        color = CaWhite,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, CaBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Reportes por categoría", style = Titulo16)
                            Spacer(modifier = Modifier.height(12.dp))
                            val byCategory = CiudadActivaRepository.reports.groupBy { it.category }
                            byCategory.entries.forEachIndexed { idx, (catName, items) ->
                                if (idx > 0) Spacer(modifier = Modifier.height(10.dp))
                                val frac = (items.size.toFloat() / totalReports.toFloat()).coerceIn(0.1f, 1f)
                                CategoryProgressBarRow(catName.take(14), items.size, frac)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Reportes por estado (Donut chart + legend)
                    Surface(
                        color = CaWhite,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, CaBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Reportes por estado", style = Titulo16)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(96.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.size(84.dp)) {
                                        val stroke = Stroke(width = 22f, cap = StrokeCap.Butt)
                                        drawArc(
                                            color = CaSecondary,
                                            startAngle = 90f,
                                            sweepAngle = 180f,
                                            useCenter = false,
                                            style = stroke
                                        )
                                        drawArc(
                                            color = CaPrimaryText,
                                            startAngle = 270f,
                                            sweepAngle = 90f,
                                            useCenter = false,
                                            style = stroke
                                        )
                                        drawArc(
                                            color = CaAccent,
                                            startAngle = 0f,
                                            sweepAngle = 90f,
                                            useCenter = false,
                                            style = stroke
                                        )
                                    }
                                    Text(text = totalReports.toString(), style = Titulo16)
                                }
                                Spacer(modifier = Modifier.width(20.dp))
                                val regOrAdmCount = CiudadActivaRepository.reports.count {
                                    it.status == ReportStatus.REGISTRADO || it.status == ReportStatus.ADMITIDO
                                }
                                val inAttCount = CiudadActivaRepository.reports.count {
                                    it.status == ReportStatus.ASIGNADO || it.status == ReportStatus.EN_ATENCION || it.status == ReportStatus.REABIERTO
                                }
                                val closedCount = CiudadActivaRepository.reports.count {
                                    it.status == ReportStatus.CERRADO || it.status == ReportStatus.CONFIRMADO
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    DonutLegendRow(CaPrimaryText, "Registrado / Admitido", "$regOrAdmCount")
                                    DonutLegendRow(CaAccent, "En gestión", "$inAttCount")
                                    DonutLegendRow(CaSecondary, "Cerrado", "$closedCount")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tiempo promedio de atención (Line trend chart: 36 -> 30 -> 24 -> 18)
                    Surface(
                        color = CaWhite,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, CaBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Tiempo promedio de atención", style = Titulo16)
                            Spacer(modifier = Modifier.height(12.dp))
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                            ) {
                                val w = size.width
                                val h = size.height
                                val p1 = Offset(w * 0.12f, h * 0.25f)
                                val p2 = Offset(w * 0.38f, h * 0.42f)
                                val p3 = Offset(w * 0.64f, h * 0.58f)
                                val p4 = Offset(w * 0.88f, h * 0.72f)

                                drawLine(CaBorder, Offset(0f, h * 0.2f), Offset(w, h * 0.2f), strokeWidth = 2f)
                                drawLine(CaBorder, Offset(0f, h * 0.85f), Offset(w, h * 0.85f), strokeWidth = 2f)

                                drawLine(CaPrimaryText, p1, p2, strokeWidth = 5f)
                                drawLine(CaPrimaryText, p2, p3, strokeWidth = 5f)
                                drawLine(CaPrimaryText, p3, p4, strokeWidth = 5f)

                                listOf(p1, p2, p3, p4).forEach { pt ->
                                    drawCircle(CaPrimaryText, radius = 8f, center = pt)
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("1–7 (36h)", style = Texto12Regular)
                                Text("8–14 (30h)", style = Texto12Regular)
                                Text("15–21 (24h)", style = Texto12Regular)
                                Text("22–27 (18h)", style = Texto12Regular)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Indicadores sincronizados con Render · Octubre",
                                style = Texto12Regular.copy(color = CaMuted)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        color = CaWhite,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CaBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                style = Texto12Regular.copy(color = CaMuted),
                minLines = 2
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, style = Titulo24)
        }
    }
}

@Composable
private fun CategoryProgressBarRow(name: String, count: Int, fraction: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = Texto12Regular,
            modifier = Modifier.width(100.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(CaBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(999.dp))
                    .background(CaPrimaryText)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = count.toString(), style = Texto12Semibold)
    }
}

@Composable
private fun DonutLegendRow(color: Color, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, style = Texto12Regular)
        }
        Text(text = value, style = Texto12Semibold)
    }
}

// =========================================================================
// 3. OPERADOR ASSIGNED QUEUE (`Operador · Asignados`, `Operador · Vacío`)
// =========================================================================
@Composable
fun OperadorAssignedScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Por iniciar, 1 = En atención, 2 = Reabiertos
    var showEmptyState by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        CiudadActivaRepository.refreshAllReportsForCurrentRole()
        CiudadActivaRepository.refreshNotifications()
    }

    val porIniciarList = CiudadActivaRepository.reports.filter { it.status == ReportStatus.ASIGNADO }
    val enAtencionList = CiudadActivaRepository.reports.filter { it.status == ReportStatus.EN_ATENCION }
    val reabiertosList = CiudadActivaRepository.reports.filter { it.status == ReportStatus.REABIERTO }
    val activeCount = (porIniciarList.size + enAtencionList.size + reabiertosList.size).coerceIn(0, 5)

    Scaffold(
        containerColor = CaNeutralLight,
        topBar = {
            CaTopAppBar(
                title = "Asignados",
                showLogo = true
            )
        },
        bottomBar = {
            CaBottomNavigationBar(
                role = UserRole.OPERADOR,
                currentRoute = Screen.OperadorAssigned.route,
                onNavigate = { route ->
                    navController.navigate(route) { launchSingleTop = true }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            OperadorCapacityCard(activeCount = activeCount, maxCount = 5)
            Spacer(modifier = Modifier.height(12.dp))

            CaSegmentedTabs(
                tabs = listOf("Por iniciar", "En atención", "Reabiertos"),
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (showEmptyState) {
                Spacer(modifier = Modifier.height(32.dp))
                CaEmptyStateCard(
                    title = "No tienes reportes asignados",
                    subtitle = "Cuando te asignen un reporte, lo encontrarás aquí para iniciar su atención."
                )
            } else {
                // Por iniciar
                if (porIniciarList.isNotEmpty()) {
                    Text(text = "Por iniciar", style = Texto12Semibold)
                    Spacer(modifier = Modifier.height(8.dp))
                    porIniciarList.forEach { r1 ->
                        OperadorQueueCard(
                            report = r1,
                            onClick = {
                                CiudadActivaRepository.currentRole = UserRole.OPERADOR
                                navController.navigate(Screen.ReportDetail.createRoute(r1.id))
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // En atención
                if (enAtencionList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "En atención", style = Texto12Semibold)
                    Spacer(modifier = Modifier.height(8.dp))
                    enAtencionList.forEach { r2 ->
                        OperadorQueueCard(
                            report = r2,
                            onClick = {
                                CiudadActivaRepository.currentRole = UserRole.OPERADOR
                                navController.navigate(Screen.ReportDetail.createRoute(r2.id))
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Reabiertos
                if (reabiertosList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Reabiertos", style = Texto12Semibold)
                    Spacer(modifier = Modifier.height(8.dp))
                    reabiertosList.forEach { r3 ->
                        OperadorQueueCard(
                            report = r3,
                            onClick = {
                                CiudadActivaRepository.currentRole = UserRole.OPERADOR
                                navController.navigate(Screen.ReportDetail.createRoute(r3.id))
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun OperadorQueueCard(report: ReportItem, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable { onClick() }) {
        Surface(
            color = CaWhite,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, CaBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = report.code,
                    style = Texto12Semibold.copy(color = CaPrimaryText)
                )
                Text(
                    text = report.category,
                    style = Texto12Regular.copy(color = CaMuted)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = report.title, style = Titulo16)
                Spacer(modifier = Modifier.height(10.dp))
                CaStatusChip(report.status)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CaPriorityChip(report.priority)
            Text(
                text = report.dueInText ?: "Vence en 5 h",
                style = Texto12Semibold.copy(
                    color = if (report.isOverdue) CaNeutralDark else StatusEnAtencionText
                )
            )
        }
    }
}

// =========================================================================
// 4. OPERADOR REGISTER PROGRESS FORM (`Avance · Formulario` & `Avance · Éxito`)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperadorProgressFormScreen(
    navController: NavController,
    reportId: String = "rep-0012"
) {
    val report = remember(reportId) { CiudadActivaRepository.getReportById(reportId) }
    val scope = rememberCoroutineScope()
    var observation by remember {
        mutableStateOf("Se señalizó el tramo y se inició el retiro del asfalto deteriorado.")
    }
    var showPhotoSheet by remember { mutableStateOf(false) }

    if (showPhotoSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoSheet = false },
            containerColor = CaWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(text = "Adjuntar foto", style = Titulo20)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "JPG o PNG, máximo 5 MB",
                    style = Texto12Regular.copy(color = CaMuted)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPhotoSheet = false }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = CaPrimaryText)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Tomar foto con la cámara", style = Texto14Semibold)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPhotoSheet = false }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, tint = CaPrimaryText)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Elegir de la galería", style = Texto14Semibold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    Scaffold(
        containerColor = CaNeutralLight,
        topBar = {
            CaTopAppBar(
                title = "Registrar avance",
                showLogo = false,
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            if (report == null) {
                CaEmptyStateCard(
                    title = "Reporte no encontrado",
                    subtitle = "No se pudo cargar la información del reporte."
                )
                return@Column
            }
            Text(
                text = "${report.code} · ${report.category}",
                style = Texto14Semibold.copy(color = CaPrimaryText)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = report.title, style = Titulo24)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = report.createdAt,
                style = Texto12Regular.copy(color = CaMuted)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CaStatusChip(ReportStatus.EN_ATENCION)
                CaPriorityChip(report.priority)
                Text("Oficial", style = Texto12Regular.copy(color = CaMuted))
            }

            Spacer(modifier = Modifier.height(18.dp))

            CaTextField(
                label = "Observación *",
                value = observation,
                onValueChange = { observation = it },
                helperText = "Obligatoria para guardar el avance.",
                singleLine = false,
                minLines = 3
            )

            Spacer(modifier = Modifier.height(18.dp))
            Text(text = "Foto del avance · Opcional", style = Texto12Semibold)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { showPhotoSheet = true }
            ) {
                CaReportThumbnail(size = 64.dp, isRealPhotoStyle = true)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "senalizacion.jpg", style = Texto12Semibold)
                    Text(
                        text = "1,8 MB · Foto adjunta",
                        style = Texto12Regular.copy(color = CaMuted)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            CaButton(
                text = "Cambiar o adjuntar foto",
                onClick = { showPhotoSheet = true },
                type = CaButtonType.SECUNDARIO
            )

            Spacer(modifier = Modifier.height(28.dp))
            CaButton(
                text = "Guardar avance",
                onClick = {
                    report.timeline.add(
                        TimelineEvent(
                            id = "ev-new-${report.timeline.size + 1}",
                            title = "Avance",
                            transition = "En atención · Avance registrado",
                            description = observation,
                            author = CiudadActivaRepository.currentUserName,
                            roleLabel = "Operador",
                            timestamp = "Hoy · Ahora",
                            isOfficialAdvance = true,
                            photoLabel = "senalizacion.jpg"
                        )
                    )
                    val uid = CiudadActivaRepository.currentUserId
                    val repLongId = report.id.toLongOrNull()
                    if (uid != null && repLongId != null) {
                        scope.launch {
                            try {
                                ApiClient.apiService.registerProgress(repLongId, uid, observation)
                            } catch (_: Exception) { }
                        }
                    }
                    navController.popBackStack()
                },
                type = CaButtonType.ACCION
            )
        }
    }
}

// =========================================================================
// 5. OPERADOR CLOSE REPORT FLOW (`Cerrar · Sin avance`, `Cerrar · Incompleto`, `Cerrar · Listo`, `Cerrar · Diálogo`)
// =========================================================================
@Composable
fun OperadorCloseReportScreen(
    navController: NavController,
    reportId: String = "rep-0012"
) {
    val report = remember(reportId) { CiudadActivaRepository.getReportById(reportId) }
    val scope = rememberCoroutineScope()
    var closeMode by remember { mutableStateOf("Listo") } // Sin avance, Incompleto, Listo
    var observation by remember {
        mutableStateOf("Se reparó el hueco y se habilitó el paso seguro en la vía.")
    }
    var hasPhoto by remember { mutableStateOf(true) }
    var hasAdvance by remember { mutableStateOf(true) }
    var showConfirmCloseDialog by remember { mutableStateOf(false) }

    val isReadyToClose = hasAdvance && hasPhoto && observation.length >= 20

    if (showConfirmCloseDialog) {
        CaConfirmationDialog(
            title = "¿Cerrar este reporte?",
            message = "El ciudadano tendrá 3 días para confirmar",
            confirmText = "Cerrar reporte",
            cancelText = "Volver",
            confirmType = CaButtonType.ACCION,
            onConfirm = {
                report?.status = ReportStatus.CERRADO
                report?.closingObservation = observation
                val uid = CiudadActivaRepository.currentUserId
                val repLongId = report?.id?.toLongOrNull()
                if (repLongId != null && uid != null) {
                    scope.launch {
                        try {
                            ApiClient.apiService.closeReport(repLongId, uid, observation)
                        } catch (_: Exception) { }
                    }
                }
                showConfirmCloseDialog = false
                navController.popBackStack()
            },
            onDismiss = { showConfirmCloseDialog = false }
        )
    }

    Scaffold(
        containerColor = CaNeutralLight,
        topBar = {
            CaTopAppBar(
                title = "Cerrar reporte",
                showLogo = false,
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            if (report == null) {
                CaEmptyStateCard(
                    title = "Reporte no encontrado",
                    subtitle = "No se pudo cargar la información del reporte."
                )
                return@Column
            }

            // Quick Switcher for `Cerrar · Sin avance`, `Cerrar · Incompleto`, `Cerrar · Listo`
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Sin avance", "Incompleto", "Listo").forEach { m ->
                    val selected = closeMode == m
                    Surface(
                        color = if (selected) StatusAdmitidoBg else CaWhite,
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, if (selected) CaPrimaryText else CaBorder),
                        modifier = Modifier.clickable {
                            closeMode = m
                            when (m) {
                                "Sin avance" -> {
                                    hasAdvance = false
                                    hasPhoto = false
                                    observation = ""
                                }
                                "Incompleto" -> {
                                    hasAdvance = true
                                    hasPhoto = false
                                    observation = "Se señalizó."
                                }
                                "Listo" -> {
                                    hasAdvance = true
                                    hasPhoto = true
                                    observation = "Se reparó el hueco y se habilitó el paso seguro en la vía."
                                }
                            }
                        }
                    ) {
                        Text(
                            text = "Cerrar · $m",
                            style = Texto12Semibold.copy(
                                color = if (selected) StatusAdmitidoText else CaMuted
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "${report.code} · ${report.category}",
                style = Texto14Semibold.copy(color = CaPrimaryText)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = report.title, style = Titulo24)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = report.createdAt, style = Texto12Regular.copy(color = CaMuted))
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CaStatusChip(ReportStatus.EN_ATENCION)
                CaPriorityChip(report.priority)
                Text("Oficial", style = Texto12Regular.copy(color = CaMuted))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Requisitos para cerrar", style = Texto12Semibold)
            Spacer(modifier = Modifier.height(10.dp))

            ChecklistRow("Al menos un avance registrado", hasAdvance)
            Spacer(modifier = Modifier.height(8.dp))
            ChecklistRow("Foto de evidencia", hasPhoto)
            Spacer(modifier = Modifier.height(8.dp))
            ChecklistRow("Observación de 20 caracteres o más", observation.length >= 20)

            Spacer(modifier = Modifier.height(16.dp))

            CaTextField(
                label = "Observación *",
                value = observation,
                onValueChange = { observation = it },
                placeholder = "Describe el trabajo realizado",
                singleLine = false,
                minLines = 3
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Mínimo 20 caracteres", style = Texto12Regular.copy(color = CaMuted))
                Text(
                    text = "${observation.length} caracteres",
                    style = Texto12Semibold.copy(
                        color = if (observation.length >= 20) CaPrimaryText else CaSecondary
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Foto de evidencia *", style = Texto12Semibold)
            Spacer(modifier = Modifier.height(8.dp))

            if (hasPhoto) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CaReportThumbnail(size = 64.dp, isRealPhotoStyle = true)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "senalizacion.jpg", style = Texto12Semibold)
                        Text(
                            text = "1,8 MB · Foto adjunta",
                            style = Texto12Regular.copy(color = CaMuted)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Todo listo para cerrar el reporte.",
                    style = Texto12Regular.copy(color = CaPrimaryText)
                )
            } else {
                Text(
                    text = "Aún no has adjuntado una foto de evidencia.",
                    style = Texto12Regular.copy(color = CaMuted)
                )
                Spacer(modifier = Modifier.height(8.dp))
                CaButton(
                    text = "Adjuntar foto",
                    onClick = { hasPhoto = true },
                    type = CaButtonType.SECUNDARIO
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (!hasAdvance) {
                        "No puedes cerrar sin registrar al menos un avance"
                    } else {
                        "Adjunta una foto y completa al menos 20 caracteres en la observación."
                    },
                    style = Texto12Regular.copy(color = CaSecondary)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            CaButton(
                text = "Cerrar reporte",
                onClick = { showConfirmCloseDialog = true },
                type = CaButtonType.ACCION,
                enabled = isReadyToClose
            )
        }
    }
}

@Composable
private fun ChecklistRow(label: String, isMet: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isMet) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = if (isMet) CaPrimaryText else CaSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = Texto12Regular.copy(color = CaNeutralDark)
        )
    }
}
