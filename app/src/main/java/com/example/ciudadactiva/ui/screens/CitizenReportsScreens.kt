package com.example.ciudadactiva.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ciudadactiva.data.*
import com.example.ciudadactiva.navigation.Screen
import com.example.ciudadactiva.network.ApiClient
import com.example.ciudadactiva.ui.components.*
import com.example.ciudadactiva.ui.theme.*
import com.example.ciudadactiva.ui.viewmodels.CreateReportState
import com.example.ciudadactiva.ui.viewmodels.CreateReportViewModel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.io.File

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
    var isLoading by remember { mutableStateOf(CiudadActivaRepository.reports.isEmpty()) }

    val currentRole = CiudadActivaRepository.currentRole
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (CiudadActivaRepository.reports.isEmpty()) isLoading = true
        CiudadActivaRepository.refreshCategories()
        CiudadActivaRepository.refreshAllReportsForCurrentRole()
        CiudadActivaRepository.refreshNotifications()
        isLoading = false
    }

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

                val catOptions = CiudadActivaRepository.categories.map { it.name }.ifEmpty {
                    listOf("Malla Vial y Huecos", "Alumbrado Público", "Basuras y Escombros", "Fuga de Agua / Alcantarillado")
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    catOptions.chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            pair.forEach { catName ->
                                CaFilterChip(
                                    label = catName,
                                    selected = selectedCategoryFilter == catName,
                                    onClick = {
                                        selectedCategoryFilter = if (selectedCategoryFilter == catName) null else catName
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
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

    val unreadCount = CiudadActivaRepository.notifications.count { it.isUnread }

    Scaffold(
        containerColor = CaNeutralLight,
        topBar = {
            Column {
                CaTopAppBar(
                    title = "Reportes",
                    showLogo = true,
                    notificationCount = unreadCount,
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

            Spacer(modifier = Modifier.height(14.dp))

            val hasActiveFilters = selectedCategoryFilter != null || selectedStatusFilter != null || selectedPriorityFilter != null
            val filteredReports = CiudadActivaRepository.reports.filter { rep ->
                (selectedCategoryFilter == null || rep.category.contains(selectedCategoryFilter!!, ignoreCase = true)) &&
                    (selectedStatusFilter == null || rep.status == selectedStatusFilter) &&
                    (selectedPriorityFilter == null || rep.priority == selectedPriorityFilter)
            }

            when {
                isLoading && CiudadActivaRepository.reports.isEmpty() -> {
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
                CiudadActivaRepository.reports.isEmpty() -> {
                    Spacer(modifier = Modifier.height(32.dp))
                    CaEmptyStateCard(
                        title = "Aún no hay reportes",
                        subtitle = "El primer paso para mejorar tu barrio puede ser tuyo.",
                        ctaText = "Crear reporte",
                        onCtaClick = { navController.navigate(Screen.CreateReport.route) }
                    )
                }
                filteredReports.isEmpty() && hasActiveFilters -> {
                    Spacer(modifier = Modifier.height(32.dp))
                    CaEmptyStateCard(
                        title = "No encontramos reportes",
                        subtitle = "Prueba cambiando o limpiando los filtros seleccionados.",
                        ctaText = "Limpiar filtros",
                        onCtaClick = {
                            selectedCategoryFilter = null
                            selectedStatusFilter = null
                            selectedPriorityFilter = null
                        }
                    )
                }
                else -> {
                    if (isMapView) {
                        // `Reportes · Vista mapa` - Mapa real de Colombia con todos los reportes
                        var selectedMapReportId by remember {
                            mutableStateOf(filteredReports.firstOrNull()?.id)
                        }
                        val highlighted = filteredReports.find { it.id == selectedMapReportId }
                            ?: filteredReports.firstOrNull()

                        CaCityMapPlaceholder(
                            locationLabel = highlighted?.location ?: "Bogotá · Colombia",
                            height = 380.dp,
                            multiplePins = true,
                            latitude = highlighted?.latitude ?: 4.6351,
                            longitude = highlighted?.longitude ?: -74.0703,
                            reportsOnMap = filteredReports,
                            selectedReportId = highlighted?.id,
                            onReportPinClick = { clickedReport ->
                                selectedMapReportId = clickedReport.id
                            }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Reporte seleccionado · ${filteredReports.size} reportes en Colombia (toca un marcador para cambiar)",
                            style = Texto12Regular.copy(color = CaMuted)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
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
                        val followed = filteredReports.filter { it.isFollowing || it.hasUnreadUpdate }
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
                        filteredReports.filter { !it.hasUnreadUpdate && it.isFollowing }.forEach { rep ->
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
                            text = "Sigues ${followed.size} reportes de tu comunidad",
                            style = Texto12Regular.copy(color = CaMuted),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        // `Reportes · Comunidad`
                        filteredReports.forEach { rep ->
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
                                text = "Actualizar lista",
                                style = Texto14Semibold.copy(color = CaPrimaryText),
                                modifier = Modifier
                                    .clickable {
                                        scope.launch {
                                            CiudadActivaRepository.refreshAllReportsForCurrentRole()
                                            CiudadActivaRepository.refreshNotifications()
                                        }
                                    }
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
    var isLoading by remember { mutableStateOf(CiudadActivaRepository.reports.none { it.isOwnReport }) }
    var reportToCancel by remember { mutableStateOf<ReportItem?>(null) }
    var cancelSuccessSnackbar by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (CiudadActivaRepository.reports.none { it.isOwnReport }) isLoading = true
        CiudadActivaRepository.refreshAllReportsForCurrentRole()
        CiudadActivaRepository.refreshNotifications()
        isLoading = false
    }

    if (reportToCancel != null) {
        val target = reportToCancel!!
        CaConfirmationDialog(
            title = "¿Cancelar este reporte?",
            message = "Esta acción no se puede deshacer.\n${target.code} · ${target.title}",
            confirmText = "Cancelar reporte",
            cancelText = "Volver",
            confirmType = CaButtonType.DESTRUCTIVO,
            onConfirm = {
                target.status = ReportStatus.CANCELADO
                val uid = CiudadActivaRepository.currentUserId
                val repLongId = target.id.toLongOrNull()
                if (uid != null && repLongId != null) {
                    scope.launch {
                        try {
                            ApiClient.apiService.cancelOwnReport(repLongId, uid)
                        } catch (_: Exception) { }
                    }
                }
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

            Spacer(modifier = Modifier.height(14.dp))

            if (cancelSuccessSnackbar) {
                CaSnackbarBanner(
                    message = "Tu reporte fue cancelado correctamente.",
                    isError = false
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            val allOwnReports = CiudadActivaRepository.reports.filter { it.isOwnReport }
            val myReports = allOwnReports.filter { rep ->
                when (selectedFilter) {
                    "Registrado" -> rep.status == ReportStatus.REGISTRADO
                    "Admitido" -> rep.status == ReportStatus.ADMITIDO
                    "En atención" -> rep.status == ReportStatus.EN_ATENCION
                    else -> true
                }
            }

            when {
                isLoading && allOwnReports.isEmpty() -> {
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
                allOwnReports.isEmpty() -> {
                    Spacer(modifier = Modifier.height(40.dp))
                    CaEmptyStateCard(
                        title = "Todavía no has reportado nada",
                        subtitle = "Ayúdanos a mejorar tu barrio. Crea tu primer reporte y sigue su estado aquí.",
                        ctaText = "Crear reporte",
                        onCtaClick = { navController.navigate(Screen.CreateReport.route) }
                    )
                }
                myReports.isEmpty() -> {
                    Spacer(modifier = Modifier.height(40.dp))
                    CaEmptyStateCard(
                        title = "No tienes reportes en este estado",
                        subtitle = "Prueba seleccionando otro filtro para ver tus demás reportes.",
                        ctaText = "Ver todos",
                        onCtaClick = { selectedFilter = "Todos" }
                    )
                }
                else -> {
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
                                Row(verticalAlignment = Alignment.Top) {
                                    CaReportThumbnail(
                                        size = 64.dp,
                                        imageUri = report.imageUris.firstOrNull()
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${report.category} · ${report.code} · ${report.createdAt}",
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
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CaStatusChip(report.status)
                                    if (report.hasUnreadUpdate) {
                                        CaActualizadoIndicator()
                                    }
                                }

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
    val context = LocalContext.current
    val existingReport = reportIdToEdit?.let { CiudadActivaRepository.getReportById(it) }
    val isEditing = existingReport != null

    LaunchedEffect(Unit) {
        CiudadActivaRepository.refreshCategories()
    }

    val reportState by viewModel.uiState.collectAsState()
    val isSuccess = reportState is CreateReportState.Success
    val isLoading = reportState is CreateReportState.Loading
    val reportError = reportState as? CreateReportState.Error
    val isNetworkError = reportError?.isNetworkError == true
    val errorMsgText = reportError?.message

    val defaultCat = CiudadActivaRepository.categories.firstOrNull { it.isActive }
    var category by remember {
        mutableStateOf(existingReport?.category ?: defaultCat?.name ?: "Malla Vial y Huecos")
    }
    var categoryId by remember {
        mutableIntStateOf(existingReport?.categoryId ?: defaultCat?.numericId ?: 1)
    }
    var title by remember { mutableStateOf(existingReport?.title ?: "") }
    var description by remember { mutableStateOf(existingReport?.description ?: "") }
    var location by remember { mutableStateOf(existingReport?.location ?: "Cra. 7 con calle 45 · Bogotá") }
    var latitude by remember { mutableDoubleStateOf(existingReport?.latitude ?: 4.6097) }
    var longitude by remember { mutableDoubleStateOf(existingReport?.longitude ?: -74.0817) }
    
    var visibility by remember {
        mutableStateOf("Solo tú y el equipo de gestión, hasta su validación")
    }
    val selectedImageUris = remember {
        mutableStateListOf<Uri>().apply {
            existingReport?.imageUris?.let { addAll(it) }
        }
    }
    var showEvidenceSheet by remember { mutableStateOf(false) }

    // Camera & Gallery Launchers
    val cameraFileUri = remember {
        val file = File(context.cacheDir, "camera_photo.jpg")
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val savedUri = CiudadActivaRepository.copyUriToInternalStorage(context, cameraFileUri)
            if (selectedImageUris.size < 3) {
                selectedImageUris.add(savedUri)
            } else {
                selectedImageUris[selectedImageUris.lastIndex] = savedUri
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val savedUri = CiudadActivaRepository.copyUriToInternalStorage(context, uri)
            if (selectedImageUris.size < 3) {
                selectedImageUris.add(savedUri)
            } else {
                selectedImageUris[selectedImageUris.lastIndex] = savedUri
            }
        }
    }

    // GPS Location Client
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        latitude = loc.latitude
                        longitude = loc.longitude
                        location = "Ubicación GPS (${String.format("%.4f", loc.latitude)}, ${String.format("%.4f", loc.longitude)})"
                    }
                }
            } catch (_: SecurityException) { }
        }
    }

    LaunchedEffect(reportState) {
        if (isSuccess) {
            if (isEditing) {
                navController.popBackStack()
            } else {
                navController.navigate(Screen.CitizenReports.route) {
                    popUpTo(Screen.CitizenReports.route) { inclusive = false }
                }
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
                            showEvidenceSheet = false
                            cameraLauncher.launch(cameraFileUri)
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
                        text = "Tomar foto con la cámara",
                        style = Texto14Semibold,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showEvidenceSheet = false
                            galleryLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
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
                text = if (isEditing) "${existingReport?.code} · Registrado" else "Nuevo reporte ciudadano",
                style = Texto14Semibold.copy(color = CaPrimaryText)
            )
            Spacer(modifier = Modifier.height(12.dp))

            CaDropdownSelector(
                label = "Categoría *",
                selectedValue = category,
                options = CiudadActivaRepository.categories.filter { it.isActive }.map { it.name },
                onOptionSelected = { selectedName ->
                    category = selectedName
                    categoryId = CiudadActivaRepository.categories.find { it.name == selectedName }?.numericId ?: 1
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
                placeholder = "Describe el lugar con claridad (mín. 20 caracteres).",
                singleLine = false,
                minLines = 3,
                maxLength = 120,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CaTextField(
                    label = "Ubicación",
                    value = location,
                    onValueChange = { location = it },
                    placeholder = "Dirección o punto de referencia",
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            CaButton(
                text = "Obtener ubicación actual por GPS",
                onClick = {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                type = CaButtonType.SECUNDARIO
            )
            Spacer(modifier = Modifier.height(10.dp))
            CaCityMapPlaceholder(
                locationLabel = "Toca el mapa de Colombia para ubicar el punto exacto",
                height = 190.dp,
                showGpsBadge = true,
                latitude = latitude,
                longitude = longitude,
                onLocationPicked = { pickedLat, pickedLon ->
                    latitude = pickedLat
                    longitude = pickedLon
                    location = String.format("Colombia (%.4f, %.4f)", pickedLat, pickedLon)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (selectedImageUris.isEmpty()) "Evidencia" else "Evidencia · ${selectedImageUris.size} foto(s)",
                style = Texto12Semibold.copy(color = CaMuted)
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (selectedImageUris.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    selectedImageUris.forEachIndexed { index, uri ->
                        CaReportThumbnail(
                            size = 96.dp,
                            isRealPhotoStyle = true,
                            imageUri = uri,
                            onRemove = {
                                selectedImageUris.removeAt(index)
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (selectedImageUris.size < 3) {
                CaButton(
                    text = if (selectedImageUris.isEmpty()) "Adjuntar foto" else "Agregar otra foto (${selectedImageUris.size}/3)",
                    onClick = { showEvidenceSheet = true },
                    type = CaButtonType.SECUNDARIO
                )
            } else {
                CaButton(
                    text = "Reemplazar última foto",
                    onClick = { showEvidenceSheet = true },
                    type = CaButtonType.SECUNDARIO
                )
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

            val isFormValid = description.length >= 20

            CaButton(
                text = if (isEditing) "Guardar cambios" else "Enviar reporte",
                onClick = {
                    if (isEditing && existingReport != null) {
                        existingReport.category = category
                        existingReport.categoryId = categoryId
                        existingReport.title = title.ifBlank { description.take(40) }
                        existingReport.description = description
                        existingReport.location = location
                        existingReport.latitude = latitude
                        existingReport.longitude = longitude
                        CiudadActivaRepository.saveReportImages(
                            reportId = existingReport.id,
                            reportCode = existingReport.code,
                            uris = selectedImageUris.toList()
                        )
                        val repLongId = existingReport.id.toLongOrNull()
                        if (repLongId != null) {
                            viewModel.updateReport(
                                reportId = repLongId,
                                categoriaId = categoryId,
                                descripcion = description,
                                latitud = latitude,
                                longitud = longitude,
                                direccion = location,
                                imageUris = selectedImageUris.toList()
                            )
                        } else {
                            navController.popBackStack()
                        }
                    } else if (isFormValid) {
                        viewModel.createReport(
                            categoriaId = categoryId,
                            titulo = title.ifBlank { null },
                            descripcion = description,
                            latitud = latitude,
                            longitud = longitude,
                            direccion = location,
                            imageUris = selectedImageUris.toList()
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
    var isLoading by remember { mutableStateOf(CiudadActivaRepository.notifications.isEmpty()) }
    val currentRole = CiudadActivaRepository.currentRole
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (CiudadActivaRepository.notifications.isEmpty()) isLoading = true
        CiudadActivaRepository.refreshNotifications()
        isLoading = false
    }

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
            when {
                isLoading && CiudadActivaRepository.notifications.isEmpty() -> {
                    repeat(3) {
                        CaSkeletonCard()
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                CiudadActivaRepository.notifications.isEmpty() -> {
                    Spacer(modifier = Modifier.height(40.dp))
                    CaEmptyStateCard(
                        title = "No tienes avisos",
                        subtitle = "Te avisaremos aquí cuando haya cambios o respuestas en tus reportes."
                    )
                }
                else -> {
                    val unreadCount = CiudadActivaRepository.notifications.count { it.isUnread }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Hoy", style = Titulo16)
                        Text(
                            text = "$unreadCount sin leer",
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
                                    CiudadActivaRepository.markNotificationAsRead(item.id, item.reportCode)
                                    val numId = item.id.removePrefix("srv_").toLongOrNull()
                                    if (numId != null) {
                                        scope.launch {
                                            try {
                                                ApiClient.apiService.markNotificationRead(numId)
                                            } catch (_: Exception) { }
                                        }
                                    }
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
