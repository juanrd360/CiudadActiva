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
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ciudadactiva.data.*
import com.example.ciudadactiva.navigation.Screen
import com.example.ciudadactiva.ui.components.*
import com.example.ciudadactiva.ui.theme.*
import com.example.ciudadactiva.ui.viewmodels.CreateReportState
import com.example.ciudadactiva.ui.viewmodels.CreateReportViewModel

// =========================================================================
// 1. CITIZEN REPORTS FEED, FOLLOWING, MAP & FILTERS
// (`Reportes · Comunidad`, `Siguiendo`, `Vista mapa`, `Filtros`, `Cargando`, `Vacío`, `Sin resultados`, `Error de red`)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenReportsScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Comunidad, 1 = Siguiendo
    var isMapView by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<ReportStatus?>(null) }
    var selectedPriorityFilter by remember { mutableStateOf<ReportPriority?>(null) }
    var uiState by remember { mutableStateOf("Normal") } // Normal, Cargando, Vacío, Sin resultados, Error

    val currentRole = CiudadActivaRepository.currentRole

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = CaWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(text = "Filtrar reportes", style = Titulo20)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Elige los reportes que quieres ver.",
                    style = Texto14Regular.copy(color = CaMuted)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Categoría", style = Titulo16)
                Spacer(modifier = Modifier.height(10.dp))

                val catOptions = listOf("Vías", "Alumbrado", "Residuos", "Espacios públicos")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CaFilterChip(
                            label = catOptions[0],
                            selected = selectedCategoryFilter == catOptions[0],
                            onClick = {
                                selectedCategoryFilter = if (selectedCategoryFilter == catOptions[0]) null else catOptions[0]
                            },
                            modifier = Modifier.weight(1f)
                        )
                        CaFilterChip(
                            label = catOptions[1],
                            selected = selectedCategoryFilter == catOptions[1],
                            onClick = {
                                selectedCategoryFilter = if (selectedCategoryFilter == catOptions[1]) null else catOptions[1]
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CaFilterChip(
                            label = catOptions[2],
                            selected = selectedCategoryFilter == catOptions[2],
                            onClick = {
                                selectedCategoryFilter = if (selectedCategoryFilter == catOptions[2]) null else catOptions[2]
                            },
                            modifier = Modifier.weight(1f)
                        )
                        CaFilterChip(
                            label = catOptions[3],
                            selected = selectedCategoryFilter == catOptions[3],
                            onClick = {
                                selectedCategoryFilter = if (selectedCategoryFilter == catOptions[3]) null else catOptions[3]
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Estado", style = Titulo16)
                Spacer(modifier = Modifier.height(10.dp))
                val statusOptions = listOf(
                    ReportStatus.ASIGNADO,
                    ReportStatus.ADMITIDO,
                    ReportStatus.EN_ATENCION,
                    ReportStatus.CERRADO
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CaFilterChip(
                            label = statusOptions[0].label,
                            selected = selectedStatusFilter == statusOptions[0],
                            onClick = {
                                selectedStatusFilter = if (selectedStatusFilter == statusOptions[0]) null else statusOptions[0]
                            },
                            modifier = Modifier.weight(1f)
                        )
                        CaFilterChip(
                            label = statusOptions[1].label,
                            selected = selectedStatusFilter == statusOptions[1],
                            onClick = {
                                selectedStatusFilter = if (selectedStatusFilter == statusOptions[1]) null else statusOptions[1]
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CaFilterChip(
                            label = statusOptions[2].label,
                            selected = selectedStatusFilter == statusOptions[2],
                            onClick = {
                                selectedStatusFilter = if (selectedStatusFilter == statusOptions[2]) null else statusOptions[2]
                            },
                            modifier = Modifier.weight(1f)
                        )
                        CaFilterChip(
                            label = statusOptions[3].label,
                            selected = selectedStatusFilter == statusOptions[3],
                            onClick = {
                                selectedStatusFilter = if (selectedStatusFilter == statusOptions[3]) null else statusOptions[3]
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Prioridad", style = Titulo16)
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(ReportPriority.BAJA, ReportPriority.MEDIA, ReportPriority.ALTA).forEach { prio ->
                        CaPriorityChip(
                            priority = prio,
                            selected = selectedPriorityFilter == prio,
                            onClick = {
                                selectedPriorityFilter = if (selectedPriorityFilter == prio) null else prio
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            selectedCategoryFilter = null
                            selectedStatusFilter = null
                            selectedPriorityFilter = null
                        }
                    ) {
                        Text("Limpiar", style = Texto14Semibold.copy(color = CaPrimaryText))
                    }
                    CaButton(
                        text = "Aplicar",
                        onClick = { showFilterSheet = false },
                        type = CaButtonType.PRIMARIO,
                        modifier = Modifier.width(200.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    Scaffold(
        containerColor = CaNeutralLight,
        topBar = {
            Column {
                CaTopAppBar(
                    title = "Reportes",
                    showLogo = true,
                    notificationCount = 3,
                    onNotificationClick = { navController.navigate(Screen.Notifications.route) }
                )
                CaSegmentedTabs(
                    tabs = listOf("Comunidad", "Siguiendo"),
                    selectedIndex = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        },
        bottomBar = {
            CaBottomNavigationBar(
                role = currentRole,
                currentRoute = Screen.CitizenReports.route,
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
            // Filter & Map Switcher Row (`Categoría`, `Estado`, `Lista | Mapa`)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CaFilterChip(
                        label = selectedCategoryFilter ?: "Categoría",
                        selected = selectedCategoryFilter != null,
                        onClick = { showFilterSheet = true }
                    )
                    CaFilterChip(
                        label = selectedStatusFilter?.label ?: "Estado",
                        selected = selectedStatusFilter != null,
                        onClick = { showFilterSheet = true }
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = isMapView,
                        onCheckedChange = { isMapView = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CaNeutralDark,
                            checkedTrackColor = CaPrimary,
                            uncheckedThumbColor = CaMuted,
                            uncheckedTrackColor = CaBorder
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lista | Mapa",
                        style = Texto12Regular.copy(color = CaMuted)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick state selector so user can inspect Cargando, Vacío, Sin resultados, Error de red
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Normal", "Cargando", "Vacío", "Sin resultados", "Error").forEach { mode ->
                    val selected = uiState == mode
                    Surface(
                        color = if (selected) StatusAdmitidoBg else CaWhite,
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, if (selected) CaPrimaryText else CaBorder),
                        modifier = Modifier.clickable { uiState = mode }
                    ) {
                        Text(
                            text = mode,
                            style = Texto12Semibold.copy(
                                color = if (selected) StatusAdmitidoText else CaMuted
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (uiState) {
                "Cargando" -> {
                    Text(
                        text = "Cargando reportes...",
                        style = Texto12Regular.copy(color = CaMuted)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    repeat(4) {
                        CaSkeletonCard()
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                "Vacío" -> {
                    Spacer(modifier = Modifier.height(32.dp))
                    CaEmptyStateCard(
                        title = "Aún no hay reportes",
                        subtitle = "El primer paso para mejorar tu barrio puede ser tuyo.",
                        ctaText = "Crear reporte",
                        onCtaClick = { navController.navigate(Screen.CreateReport.route) }
                    )
                }
                "Sin resultados" -> {
                    Spacer(modifier = Modifier.height(32.dp))
                    CaEmptyStateCard(
                        title = "No encontramos reportes",
                        subtitle = "Prueba cambiando o limpiando los filtros seleccionados.",
                        ctaText = "Limpiar filtros",
                        onCtaClick = {
                            selectedCategoryFilter = null
                            selectedStatusFilter = null
                            selectedPriorityFilter = null
                            uiState = "Normal"
                        }
                    )
                }
                "Error" -> {
                    Spacer(modifier = Modifier.height(32.dp))
                    CaErrorStateCard(
                        title = "No pudimos cargar los reportes",
                        subtitle = "Revisa tu conexión a internet e intenta de nuevo.",
                        onRetry = { uiState = "Normal" }
                    )
                }
                else -> {
                    val filteredReports = CiudadActivaRepository.reports.filter { rep ->
                        (selectedCategoryFilter == null || rep.category.contains(selectedCategoryFilter!!, ignoreCase = true)) &&
                            (selectedStatusFilter == null || rep.status == selectedStatusFilter) &&
                            (selectedPriorityFilter == null || rep.priority == selectedPriorityFilter)
                    }

                    if (isMapView) {
                        // `Reportes · Vista mapa`
                        CaCityMapPlaceholder(
                            locationLabel = "Chapinero · Bogotá",
                            height = 400.dp,
                            multiplePins = true
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Reporte seleccionado · 4 reportes en esta zona",
                            style = Texto12Regular.copy(color = CaMuted)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        val highlighted = filteredReports.find { it.code == "RP-0013" } ?: filteredReports.firstOrNull()
                        if (highlighted != null) {
                            CaReportCard(
                                report = highlighted,
                                onClick = {
                                    navController.navigate(Screen.ReportDetail.createRoute(highlighted.id))
                                }
                            )
                        }
                    } else if (selectedTab == 1) {
                        // `Reportes · Siguiendo`
                        Text(text = "Con novedades", style = Titulo16)
                        Spacer(modifier = Modifier.height(10.dp))
                        filteredReports.filter { it.hasUnreadUpdate }.forEach { rep ->
                            CaReportCard(
                                report = rep,
                                onClick = {
                                    navController.navigate(Screen.ReportDetail.createRoute(rep.id))
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Sin cambios", style = Titulo16)
                        Spacer(modifier = Modifier.height(10.dp))
                        filteredReports.filter { !it.hasUnreadUpdate }.take(2).forEach { rep ->
                            CaReportCard(
                                report = rep,
                                onClick = {
                                    navController.navigate(Screen.ReportDetail.createRoute(rep.id))
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Sigues 3 reportes de tu comunidad",
                            style = Texto12Regular.copy(color = CaMuted),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        // `Reportes · Comunidad`
                        filteredReports.take(4).forEach { rep ->
                            CaReportCard(
                                report = rep,
                                onClick = {
                                    navController.navigate(Screen.ReportDetail.createRoute(rep.id))
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cargar más",
                                style = Texto14Semibold.copy(color = CaPrimaryText),
                                modifier = Modifier
                                    .clickable { }
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 2. MY REPORTS, EDIT REPORT & CANCEL DIALOG
// (`Mis reportes · Lista`, `Vacío`, `Cargando`, `Error`, `Editar reporte`, `Cancelar · Diálogo`, `Cancelar · Éxito`)
// =========================================================================
@Composable
fun MyReportsScreen(navController: NavController) {
    var selectedFilter by remember { mutableStateOf("Todos") }
    var uiState by remember { mutableStateOf("Normal") } // Normal, Vacío, Cargando, Error
    var reportToCancel by remember { mutableStateOf<ReportItem?>(null) }
    var cancelSuccessSnackbar by remember { mutableStateOf(false) }

    if (reportToCancel != null) {
        CaConfirmationDialog(
            title = "¿Cancelar este reporte?",
            message = "Esta acción no se puede deshacer.\n${reportToCancel!!.code} · ${reportToCancel!!.title}",
            confirmText = "Cancelar reporte",
            cancelText = "Volver",
            confirmType = CaButtonType.DESTRUCTIVO,
            onConfirm = {
                reportToCancel!!.status = ReportStatus.CANCELADO
                reportToCancel = null
                cancelSuccessSnackbar = true
            },
            onDismiss = { reportToCancel = null }
        )
    }

    Scaffold(
        containerColor = CaNeutralLight,
        topBar = {
            CaTopAppBar(
                title = "Mis reportes",
                showLogo = false,
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            CaBottomNavigationBar(
                role = CiudadActivaRepository.currentRole,
                currentRoute = Screen.MyReports.route,
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
            // Horizontal Filter Pills (`Todos`, `Registrado`, `Admitido`, `En atención`)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Todos", "Registrado", "Admitido", "En atención").forEach { filter ->
                    CaFilterChip(
                        label = filter,
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick state switcher for Mis reportes (Normal, Vacío, Cargando, Error)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Normal", "Vacío", "Cargando", "Error").forEach { mode ->
                    val selected = uiState == mode
                    Surface(
                        color = if (selected) StatusAdmitidoBg else CaWhite,
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, if (selected) CaPrimaryText else CaBorder),
                        modifier = Modifier.clickable { uiState = mode }
                    ) {
                        Text(
                            text = mode,
                            style = Texto12Semibold.copy(
                                color = if (selected) StatusAdmitidoText else CaMuted
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (cancelSuccessSnackbar) {
                CaSnackbarBanner(
                    message = "Tu reporte fue cancelado correctamente.",
                    isError = false
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            when (uiState) {
                "Vacío" -> {
                    Spacer(modifier = Modifier.height(40.dp))
                    CaEmptyStateCard(
                        title = "Todavía no has reportado nada",
                        subtitle = "Ayúdanos a mejorar tu barrio. Crea tu primer reporte y sigue su estado aquí.",
                        ctaText = "Crear reporte",
                        onCtaClick = { navController.navigate(Screen.CreateReport.route) }
                    )
                }
                "Cargando" -> {
                    Text(
                        text = "Cargando tus reportes...",
                        style = Texto12Regular.copy(color = CaMuted)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    repeat(4) {
                        CaSkeletonCard()
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                "Error" -> {
                    Spacer(modifier = Modifier.height(40.dp))
                    CaErrorStateCard(
                        title = "No pudimos cargar tus reportes",
                        subtitle = "Revisa tu conexión e inténtalo de nuevo.",
                        onRetry = { uiState = "Normal" }
                    )
                }
                else -> {
                    val myReports = CiudadActivaRepository.reports.filter { it.isOwnReport }
                    Text(
                        text = "${myReports.size} reportes · Más recientes primero",
                        style = Texto12Regular.copy(color = CaMuted)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    myReports.forEach { report ->
                        Surface(
                            color = CaWhite,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, CaBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate(Screen.ReportDetail.createRoute(report.id))
                                }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "${report.category} · ${report.code} · 27 oct",
                                    style = Texto12Semibold.copy(color = CaPrimaryText)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = report.location,
                                    style = Texto12Regular.copy(color = CaMuted)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = report.title,
                                    style = Titulo16
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                CaStatusChip(report.status)

                                if (report.status == ReportStatus.REGISTRADO) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        CaButton(
                                            text = "Editar",
                                            onClick = {
                                                navController.navigate(Screen.EditReport.createRoute(report.id))
                                            },
                                            type = CaButtonType.SECUNDARIO,
                                            modifier = Modifier.weight(1f)
                                        )
                                        TextButton(
                                            onClick = { reportToCancel = report },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "Cancelar",
                                                style = Texto14Semibold.copy(color = CaPrimaryText)
                                            )
                                        }
                                    }
                                }

                                if (report.status == ReportStatus.RECHAZADO && report.rejectionReason != null) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Surface(
                                        color = ErrorBannerBg,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Motivo: ${report.rejectionReason}",
                                            style = Texto12Regular.copy(color = CaSecondary),
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

// =========================================================================
// 3. CREATE & EDIT REPORT (`Crear reporte` & `Editar reporte` + `Adjuntar evidencia` Sheet)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrEditReportScreen(
    navController: NavController,
    viewModel: CreateReportViewModel = viewModel(),
    reportIdToEdit: String? = null
) {
    val existingReport = reportIdToEdit?.let { CiudadActivaRepository.getReportById(it) }
    val isEditing = existingReport != null

    val reportState by viewModel.uiState.collectAsState()
    val isSuccess = reportState is CreateReportState.Success
    val isLoading = reportState is CreateReportState.Loading
    val reportError = reportState as? CreateReportState.Error
    val isNetworkError = reportError?.isNetworkError == true
    val errorMsgText = reportError?.message

    var category by remember { mutableStateOf(existingReport?.category ?: "Vías") }
    var categoryId by remember { mutableStateOf<Short?>(null) }
    var title by remember { mutableStateOf(existingReport?.title ?: "") }
    var description by remember { mutableStateOf(existingReport?.description ?: "") }
    var location by remember { mutableStateOf(existingReport?.location ?: "Cra. 7 con calle 45 · Bogotá") }
    var visibility by remember {
        mutableStateOf("Solo tú y el equipo de gestión, hasta su validación")
    }
    var hasPhoto by remember { mutableStateOf(true) }
    var showEvidenceSheet by remember { mutableStateOf(false) }
    LaunchedEffect(reportState) {
        if (isSuccess) {
            navController.navigate(Screen.CitizenReports.route) {
                popUpTo(Screen.CitizenReports.route) { inclusive = false }
            }
            viewModel.resetState()
        }
    }

    if (showEvidenceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEvidenceSheet = false },
            containerColor = CaWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(text = "Adjuntar evidencia", style = Titulo20)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Una foto ayuda a entender mejor el reporte.",
                    style = Texto14Regular.copy(color = CaMuted)
                )
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            hasPhoto = true
                            showEvidenceSheet = false
                        }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CameraAlt,
                        contentDescription = null,
                        tint = CaPrimaryText
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Tomar foto",
                        style = Texto14Semibold,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            hasPhoto = true
                            showEvidenceSheet = false
                        }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoLibrary,
                        contentDescription = null,
                        tint = CaPrimaryText
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Elegir de la galería",
                        style = Texto14Semibold,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    Scaffold(
        containerColor = CaNeutralLight,
        topBar = {
            CaTopAppBar(
                title = if (isEditing) "Editar reporte" else "Crear reporte",
                showLogo = false,
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            CaBottomNavigationBar(
                role = CiudadActivaRepository.currentRole,
                currentRoute = if (isEditing) Screen.MyReports.route else Screen.CreateReport.route,
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
                text = if (isEditing) "${existingReport!!.code} · Registrado" else "Nuevo reporte ciudadano",
                style = Texto14Semibold.copy(color = CaPrimaryText)
            )
            Spacer(modifier = Modifier.height(12.dp))

            CaDropdownSelector(
                label = "Categoría *",
                selectedValue = category,
                options = CiudadActivaRepository.categories.filter { it.isActive }.map { it.name },
                onOptionSelected = {
                    category = it
                    categoryId = when(it) {
                        "Vías" -> 1
                        "Alumbrado público" -> 2
                        "Arbolado" -> 3
                        "Residuos" -> 4
                        "Espacios públicos" -> 5
                        else -> 1
                    }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            CaTextField(
                label = "Título",
                value = title,
                onValueChange = { title = it },
                placeholder = "Ej. Luminaria apagada en el parque",
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(14.dp))

            CaTextField(
                label = "Descripción *",
                value = description,
                onValueChange = { description = it },
                placeholder = "Describe el lugar con claridad.",
                singleLine = false,
                minLines = 3,
                maxLength = 120,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(14.dp))

            CaTextField(
                label = "Ubicación",
                value = location,
                onValueChange = { location = it },
                placeholder = "Dirección o punto de referencia",
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Evidencia",
                style = Texto12Semibold.copy(color = CaMuted)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (hasPhoto) {
                    CaReportThumbnail(
                        size = 96.dp,
                        isRealPhotoStyle = true,
                        onRemove = { hasPhoto = false }
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CaButton(
                        text = if (hasPhoto) "Reemplazar foto" else "Adjuntar foto",
                        onClick = { showEvidenceSheet = true },
                        type = CaButtonType.SECUNDARIO
                    )
                    if (hasPhoto) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Quitar foto",
                            style = Texto14Semibold.copy(color = CaPrimaryText),
                            modifier = Modifier.clickable { hasPhoto = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            CaDropdownSelector(
                label = "Visibilidad",
                selectedValue = visibility,
                options = listOf(
                    "Solo tú y el equipo de gestión, hasta su validación",
                    "Público para toda la comunidad tras validación"
                ),
                onOptionSelected = { visibility = it }
            )

            if (errorMsgText != null) {
                Spacer(modifier = Modifier.height(18.dp))
                Surface(
                    color = if (isNetworkError) WarningBannerBg else ErrorBannerBg,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isNetworkError) Icons.Outlined.WifiOff else Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = if (isNetworkError) StatusEnAtencionText else CaSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = errorMsgText,
                            style = Texto12Regular.copy(
                                color = if (isNetworkError) CaNeutralDark else CaSecondary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            val isFormValid = categoryId != null && description.length >= 20

            CaButton(
                text = if (isEditing) "Guardar cambios" else "Enviar reporte",
                onClick = {
                    if (isEditing && existingReport != null) {
                        existingReport.category = category
                        existingReport.title = title
                        existingReport.description = description
                        existingReport.location = location
                        navController.popBackStack()
                    } else if (isFormValid) {
                        viewModel.createReport(
                            categoriaId = categoryId!!,
                            descripcion = description,
                            latitud = 4.6097, // Falso
                            longitud = -74.0817,
                            direccion = location
                        )
                    }
                },
                type = CaButtonType.ACCION,
                enabled = isFormValid && !isLoading,
                isLoading = isLoading
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// =========================================================================
// 4. NOTIFICATIONS SCREEN (`Avisos · Lista`, `Vacío`, `Cargando`, `Error`)
// Works across Ciudadano, Gestor, and Operador roles
// =========================================================================
@Composable
fun NotificationsScreen(navController: NavController) {
    var uiState by remember { mutableStateOf("Lista") } // Lista, Vacío, Cargando, Error
    val currentRole = CiudadActivaRepository.currentRole

    Scaffold(
        containerColor = CaNeutralLight,
        topBar = {
            CaTopAppBar(
                title = "Avisos",
                showLogo = true
            )
        },
        bottomBar = {
            CaBottomNavigationBar(
                role = currentRole,
                currentRoute = Screen.Notifications.route,
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
            // Mode Switcher for Avisos states (`Lista`, `Vacío`, `Cargando`, `Error`)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Lista", "Vacío", "Cargando", "Error").forEach { mode ->
                    val selected = uiState == mode
                    Surface(
                        color = if (selected) StatusAdmitidoBg else CaWhite,
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, if (selected) CaPrimaryText else CaBorder),
                        modifier = Modifier.clickable { uiState = mode }
                    ) {
                        Text(
                            text = mode,
                            style = Texto12Semibold.copy(
                                color = if (selected) StatusAdmitidoText else CaMuted
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (uiState) {
                "Vacío" -> {
                    Spacer(modifier = Modifier.height(40.dp))
                    CaEmptyStateCard(
                        title = "No tienes avisos",
                        subtitle = "Te avisaremos aquí cuando haya cambios o respuestas en tus reportes."
                    )
                }
                "Cargando" -> {
                    repeat(3) {
                        CaSkeletonCard()
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                "Error" -> {
                    Spacer(modifier = Modifier.height(40.dp))
                    CaErrorStateCard(
                        title = "No pudimos cargar tus avisos",
                        subtitle = "Revisa tu conexión a internet e intenta de nuevo.",
                        onRetry = { uiState = "Lista" }
                    )
                }
                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Hoy", style = Titulo16)
                        Text(
                            text = "2 sin leer",
                            style = Texto12Semibold.copy(color = CaPrimaryText)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    CiudadActivaRepository.notifications.forEach { item ->
                        Surface(
                            color = if (item.isUnread) UnreadNotificationBg else CaWhite,
                            shape = RoundedCornerShape(16.dp),
                            border = if (item.isUnread) null else BorderStroke(1.dp, CaBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    item.isUnread = false
                                    navController.navigate(Screen.ReportDetail.createRoute(item.reportCode))
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = null,
                                    tint = CaPrimaryText,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        style = if (item.isUnread) Texto14Semibold else Texto14Regular
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.subtitle,
                                        style = Texto12Regular.copy(color = CaMuted)
                                    )
                                }
                                if (item.isUnread) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(CaAccent)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}
