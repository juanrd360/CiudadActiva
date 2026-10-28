package com.example.ciudadactiva.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ciudadactiva.data.*
import com.example.ciudadactiva.navigation.Screen
import com.example.ciudadactiva.network.ApiClient
import com.example.ciudadactiva.network.models.CategoriaCreateDTO
import com.example.ciudadactiva.network.models.UsuarioCreateDTO
import com.example.ciudadactiva.ui.components.*
import com.example.ciudadactiva.ui.theme.*
import kotlinx.coroutines.launch

// =========================================================================
// 1. ADMIN CATEGORIES (`Admin · Categorías`, `Admin · Categoría formulario`, `Admin · Desactivar categoría`)
// =========================================================================
@Composable
fun AdminCategoriesScreen(navController: NavController) {
    var categoryToDeactivate by remember { mutableStateOf<CategoryItem?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        CiudadActivaRepository.refreshCategories()
    }

    if (categoryToDeactivate != null) {
        val target = categoryToDeactivate!!
        CaConfirmationDialog(
            title = "¿Desactivar categoría?",
            message = "${target.name} tiene ${target.reportsInUseCount} reportes en uso.\n\nNo se podrá elegir en nuevos reportes. Los reportes existentes conservarán su categoría y podrán seguir atendiéndose.\n\nPuedes volver a activarla cuando lo necesites.",
            confirmText = "Desactivar categoría",
            cancelText = "Cancelar",
            confirmType = CaButtonType.DESTRUCTIVO,
            onConfirm = {
                target.isActive = false
                scope.launch {
                    try {
                        ApiClient.apiService.updateCategoryStatus(target.numericId, false)
                    } catch (_: Exception) { }
                }
                categoryToDeactivate = null
            },
            onDismiss = { categoryToDeactivate = null }
        )
    }

    Scaffold(
        containerColor = CaNeutralLight,
        topBar = {
            CaTopAppBar(
                title = "Categorías",
                showLogo = true
            )
        },
        bottomBar = {
            CaBottomNavigationBar(
                role = UserRole.ADMINISTRADOR,
                currentRoute = Screen.AdminCategories.route,
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
                text = "Administra las categorías disponibles para los reportes de la ciudadanía.",
                style = Texto14Regular.copy(color = CaMuted)
            )
            Spacer(modifier = Modifier.height(10.dp))
            val activeCount = CiudadActivaRepository.categories.count { it.isActive }
            Text(
                text = "${CiudadActivaRepository.categories.size} categorías · $activeCount activas",
                style = Texto12Semibold
            )
            Spacer(modifier = Modifier.height(12.dp))

            CiudadActivaRepository.categories.forEach { cat ->
                Surface(
                    color = CaWhite,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CaBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = cat.name, style = Titulo16)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cat.description,
                            style = Texto12Regular.copy(color = CaMuted)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Prioridad inicial",
                            style = Texto12Regular.copy(color = CaMuted)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CaPriorityChip(cat.defaultPriority)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = cat.isActive,
                                    onCheckedChange = { checked ->
                                        if (!checked) {
                                            categoryToDeactivate = cat
                                        } else {
                                            cat.isActive = true
                                            scope.launch {
                                                try {
                                                    ApiClient.apiService.updateCategoryStatus(cat.numericId, true)
                                                } catch (_: Exception) { }
                                            }
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = CaNeutralDark,
                                        checkedTrackColor = CaPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (cat.isActive) "Activa" else "Inactiva",
                                    style = Texto12Regular.copy(color = CaNeutralDark)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                CaButton(
                    text = "Nueva categoría",
                    onClick = { navController.navigate(Screen.AdminCategoryForm.route) },
                    type = CaButtonType.PRIMARIO,
                    modifier = Modifier.width(180.dp)
                )
            }
        }
    }
}

@Composable
fun AdminCategoryFormScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(ReportPriority.MEDIA) }
    var isActive by remember { mutableStateOf(true) }
    var showDuplicateError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = CaNeutralLight,
        topBar = {
            CaTopAppBar(
                title = "Nueva categoría",
                showLogo = false,
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            CaBottomNavigationBar(
                role = UserRole.ADMINISTRADOR,
                currentRoute = Screen.AdminCategories.route,
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
                text = "Define cómo se clasificarán los nuevos reportes. Todos los campos son obligatorios.",
                style = Texto14Regular.copy(color = CaMuted)
            )
            Spacer(modifier = Modifier.height(14.dp))

            CaTextField(
                label = "Nombre",
                value = name,
                placeholder = "Ej. Arbolado urbano",
                onValueChange = {
                    name = it
                    showDuplicateError = CiudadActivaRepository.categories.any { c ->
                        c.name.equals(it.trim(), ignoreCase = true)
                    }
                },
                errorMessage = if (showDuplicateError) "Ya existe una categoría con ese nombre" else null
            )

            Spacer(modifier = Modifier.height(14.dp))

            CaTextField(
                label = "Descripción",
                value = description,
                placeholder = "Describe qué reportes aplican",
                onValueChange = { description = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            CaDropdownSelector(
                label = "Prioridad inicial",
                selectedValue = priority.label,
                options = listOf("Baja", "Media", "Alta"),
                onOptionSelected = { sel ->
                    priority = ReportPriority.entries.find { it.label == sel } ?: ReportPriority.MEDIA
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(ReportPriority.BAJA, ReportPriority.MEDIA, ReportPriority.ALTA).forEach { p ->
                    CaPriorityChip(
                        priority = p,
                        selected = priority == p,
                        onClick = { priority = p }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = CaWhite,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CaBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Categoría activa", style = Titulo16)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = isActive,
                                onCheckedChange = { isActive = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CaNeutralDark,
                                    checkedTrackColor = CaPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isActive) "Activa" else "Inactiva",
                                style = Texto12Regular
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Estará disponible al crear un reporte.",
                        style = Texto12Regular.copy(color = CaMuted)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            CaButton(
                text = "Crear categoría",
                enabled = name.isNotBlank() && !showDuplicateError,
                onClick = {
                    val newId = (CiudadActivaRepository.categories.maxOfOrNull { it.numericId } ?: 4) + 1
                    CiudadActivaRepository.categories.add(
                        0,
                        CategoryItem(
                            id = "cat-new-$newId",
                            numericId = newId,
                            name = name.trim(),
                            description = description.trim().ifBlank { "Categoría ciudadana" },
                            defaultPriority = priority,
                            isActive = isActive
                        )
                    )
                    scope.launch {
                        try {
                            ApiClient.apiService.createCategory(
                                CategoriaCreateDTO(
                                    nombre = name.trim(),
                                    descripcion = description.trim().ifBlank { "Categoría ciudadana" },
                                    prioridadInicial = priority.name
                                )
                            )
                            CiudadActivaRepository.refreshCategories()
                        } catch (_: Exception) { }
                    }
                    navController.popBackStack()
                },
                type = CaButtonType.PRIMARIO
            )
            Spacer(modifier = Modifier.height(12.dp))
            CaButton(
                text = "Cancelar creación",
                onClick = { navController.popBackStack() },
                type = CaButtonType.TEXTO
            )
        }
    }
}

// =========================================================================
// 2. ADMIN USERS (`Admin · Usuarios`, `Admin · Usuario formulario`, `Admin · Desactivar operador`)
// =========================================================================
@Composable
fun AdminUsersScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var userToDeactivate by remember { mutableStateOf<StaffUserItem?>(null) }
    var showCreatedSnackbar by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        CiudadActivaRepository.refreshUsers()
    }

    if (userToDeactivate != null) {
        val target = userToDeactivate!!
        CaConfirmationDialog(
            title = "¿Desactivar usuario?",
            message = "${target.fullName} perderá el acceso al sistema.\n\nSi tiene reportes asignados, el gestor deberá reasignarlos a otro operador activo.",
            confirmText = "Desactivar usuario",
            cancelText = "Cancelar",
            confirmType = CaButtonType.DESTRUCTIVO,
            onConfirm = {
                target.isActive = false
                if (target.numericId > 0) {
                    scope.launch {
                        try {
                            ApiClient.apiService.updateUserStatus(target.numericId, false)
                        } catch (_: Exception) { }
                    }
                }
                userToDeactivate = null
            },
            onDismiss = { userToDeactivate = null }
        )
    }

    Scaffold(
        containerColor = CaNeutralLight,
        topBar = {
            CaTopAppBar(
                title = "Usuarios",
                showLogo = true
            )
        },
        bottomBar = {
            CaBottomNavigationBar(
                role = UserRole.ADMINISTRADOR,
                currentRoute = Screen.AdminUsers.route,
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
            CaTextField(
                label = "Buscar usuario",
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Nombre o correo"
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CaFilterChip(
                    label = "Rol: todos",
                    selected = false,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                CaButton(
                    text = "Crear usuario",
                    onClick = { navController.navigate(Screen.AdminUserForm.route) },
                    type = CaButtonType.PRIMARIO,
                    modifier = Modifier.weight(1f)
                )
            }

            val filteredUsers = CiudadActivaRepository.staffUsers.filter { usr ->
                searchQuery.isBlank() ||
                    usr.fullName.contains(searchQuery, ignoreCase = true) ||
                    usr.email.contains(searchQuery, ignoreCase = true)
            }
            val activeCount = filteredUsers.count { it.isActive }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "${filteredUsers.size} usuarios · $activeCount activos",
                style = Texto12Semibold
            )
            Spacer(modifier = Modifier.height(10.dp))

            filteredUsers.forEach { usr ->
                Surface(
                    color = CaWhite,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CaBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (usr.isActive) {
                                userToDeactivate = usr
                            } else {
                                usr.isActive = true
                                if (usr.numericId > 0) {
                                    scope.launch {
                                        try {
                                            ApiClient.apiService.updateUserStatus(usr.numericId, true)
                                        } catch (_: Exception) { }
                                    }
                                }
                            }
                        }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = usr.fullName, style = Titulo16)
                            Surface(
                                color = if (usr.isActive) StatusAdmitidoBg else CaBorder,
                                shape = RoundedCornerShape(999.dp)
                            ) {
                                Text(
                                    text = if (usr.isActive) "Activo" else "Inactivo",
                                    style = Texto12Semibold.copy(
                                        color = if (usr.isActive) StatusAdmitidoText else CaNeutralDark
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = usr.email,
                            style = Texto12Regular.copy(color = CaMuted)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CaRoleChip(usr.role.label)
                            Text(
                                text = usr.statusDetail,
                                style = Texto12Regular.copy(color = CaMuted)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (showCreatedSnackbar) {
                Spacer(modifier = Modifier.height(6.dp))
                CaSnackbarBanner(
                    message = "Usuario interno creado.",
                    isError = false
                )
            }
        }
    }
}

@Composable
fun AdminUserFormScreen(navController: NavController) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("3001234567") }
    var password by remember { mutableStateOf("CiudadActiva2026") }
    var selectedRole by remember { mutableStateOf(UserRole.GESTOR) }
    var isActive by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = CaNeutralLight,
        topBar = {
            CaTopAppBar(
                title = "Crear usuario interno",
                showLogo = false,
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            CaBottomNavigationBar(
                role = UserRole.ADMINISTRADOR,
                currentRoute = Screen.AdminUsers.route,
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
                text = "Registra a un integrante de la Alcaldía y define sus permisos de acceso.",
                style = Texto14Regular.copy(color = CaMuted)
            )
            Spacer(modifier = Modifier.height(14.dp))

            CaTextField(
                label = "Nombre completo",
                value = fullName,
                placeholder = "Ej. Diana Rojas",
                onValueChange = { fullName = it }
            )
            Spacer(modifier = Modifier.height(14.dp))

            CaTextField(
                label = "Correo institucional",
                value = email,
                placeholder = "diana.rojas@ciudadactiva.com",
                onValueChange = { email = it }
            )
            Spacer(modifier = Modifier.height(14.dp))

            CaTextField(
                label = "Teléfono",
                value = phone,
                onValueChange = { phone = it }
            )
            Spacer(modifier = Modifier.height(14.dp))

            CaTextField(
                label = "Contraseña temporal",
                value = password,
                onValueChange = { password = it },
                isPassword = true
            )
            Spacer(modifier = Modifier.height(14.dp))

            CaDropdownSelector(
                label = "Rol del usuario",
                selectedValue = selectedRole.label,
                options = listOf("Gestor", "Operador", "Administrador"),
                onOptionSelected = { label ->
                    selectedRole = UserRole.entries.find { it.label == label } ?: UserRole.GESTOR
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CaRoleChip(selectedRole.label)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = selectedRole.permissionsSummary,
                    style = Texto12Regular.copy(color = CaMuted)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = CaWhite,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CaBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Acceso al sistema", style = Titulo16)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = isActive,
                                onCheckedChange = { isActive = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CaNeutralDark,
                                    checkedTrackColor = CaPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (isActive) "Activo" else "Inactivo", style = Texto12Regular)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Recibirá sus credenciales de acceso. Podrás cambiar el rol o estado luego.",
                        style = Texto12Regular.copy(color = CaMuted)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            CaButton(
                text = "Crear usuario interno",
                enabled = fullName.isNotBlank() && email.contains("@") && password.length >= 6,
                onClick = {
                    val nameParts = fullName.trim().split("\\s+".toRegex(), limit = 2)
                    val first = nameParts.getOrNull(0)?.ifBlank { "Usuario" } ?: "Usuario"
                    val last = nameParts.getOrNull(1)?.ifBlank { "Interno" } ?: "Interno"
                    CiudadActivaRepository.staffUsers.add(
                        0,
                        StaffUserItem(
                            id = "usr-new-${CiudadActivaRepository.staffUsers.size + 1}",
                            fullName = fullName.trim(),
                            email = email.trim(),
                            role = selectedRole,
                            isActive = isActive,
                            statusDetail = "Gestión y seguimiento"
                        )
                    )
                    scope.launch {
                        try {
                            ApiClient.apiService.createUserByAdmin(
                                UsuarioCreateDTO(
                                    nombre = first,
                                    apellido = last,
                                    correo = email.trim(),
                                    password = password,
                                    telefono = phone.trim().ifBlank { "3001234567" },
                                    rol = selectedRole.name
                                )
                            )
                            CiudadActivaRepository.refreshUsers()
                        } catch (_: Exception) { }
                    }
                    navController.popBackStack()
                },
                type = CaButtonType.PRIMARIO
            )
            Spacer(modifier = Modifier.height(12.dp))
            CaButton(
                text = "Cancelar creación",
                onClick = { navController.popBackStack() },
                type = CaButtonType.TEXTO
            )
        }
    }
}

// =========================================================================
// 3. ADMIN GENERAL REPORTS (`Admin · Reportes`)
// Read-only supervision + comment moderation
// =========================================================================
@Composable
fun AdminReportsScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        CiudadActivaRepository.refreshAllReportsForCurrentRole()
    }

    Scaffold(
        containerColor = CaNeutralLight,
        topBar = {
            CaTopAppBar(
                title = "Reportes generales",
                showLogo = true
            )
        },
        bottomBar = {
            CaBottomNavigationBar(
                role = UserRole.ADMINISTRADOR,
                currentRoute = Screen.AdminReports.route,
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
                text = "Solo lectura de reportes. Puedes moderar comentarios.",
                style = Texto12Regular.copy(color = CaMuted)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CaFilterChip("Categoría", false, {}, modifier = Modifier.weight(1f))
                CaFilterChip("Estado", false, {}, modifier = Modifier.weight(1f))
                CaFilterChip("Prioridad", false, {}, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${CiudadActivaRepository.reports.size} reportes", style = Texto12Semibold)
                Text("Más recientes primero", style = Texto12Regular.copy(color = CaMuted))
            }

            Spacer(modifier = Modifier.height(12.dp))

            CiudadActivaRepository.reports.forEach { rep ->
                CaReportCard(
                    report = rep,
                    onClick = {
                        navController.navigate(Screen.ReportDetail.createRoute(rep.id))
                    },
                    showAuthorInHeader = false
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${rep.comments.size} comentarios",
                        style = Texto12Regular.copy(color = CaMuted)
                    )
                    Text(
                        text = "Moderar comentarios",
                        style = Texto12Semibold.copy(color = CaPrimaryText),
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.ReportDetail.createRoute(rep.id))
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
