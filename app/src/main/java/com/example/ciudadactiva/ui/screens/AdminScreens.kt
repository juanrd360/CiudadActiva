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
import com.example.ciudadactiva.ui.components.*
import com.example.ciudadactiva.ui.theme.*

// =========================================================================
// 1. ADMIN CATEGORIES (`Admin · Categorías`, `Admin · Categoría formulario`, `Admin · Desactivar categoría`)
// =========================================================================
@Composable
fun AdminCategoriesScreen(navController: NavController) {
    var categoryToDeactivate by remember { mutableStateOf<CategoryItem?>(null) }

    if (categoryToDeactivate != null) {
        CaConfirmationDialog(
            title = "¿Desactivar categoría?",
            message = "${categoryToDeactivate!!.name} tiene ${categoryToDeactivate!!.reportsInUseCount} reportes en uso.\n\nNo se podrá elegir en nuevos reportes. Los reportes existentes conservarán su categoría y podrán seguir atendiéndose.\n\nPuedes volver a activarla cuando lo necesites.",
            confirmText = "Desactivar categoría",
            cancelText = "Cancelar",
            confirmType = CaButtonType.DESTRUCTIVO,
            onConfirm = {
                categoryToDeactivate!!.isActive = false
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

            CiudadActivaRepository.categories.take(3).forEach { cat ->
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
    var name by remember { mutableStateOf("Alumbrado público") }
    var description by remember { mutableStateOf("Luminarias apagadas o intermitentes.") }
    var priority by remember { mutableStateOf(ReportPriority.MEDIA) }
    var isActive by remember { mutableStateOf(true) }
    var showDuplicateError by remember { mutableStateOf(true) }

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
                onValueChange = {
                    name = it
                    showDuplicateError = it.equals("Alumbrado público", ignoreCase = true)
                },
                errorMessage = if (showDuplicateError) "Ya existe una categoría con ese nombre" else null
            )

            Spacer(modifier = Modifier.height(14.dp))

            CaTextField(
                label = "Descripción",
                value = description,
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
                onClick = {
                    CiudadActivaRepository.categories.add(
                        0,
                        CategoryItem(
                            id = "cat-new-${CiudadActivaRepository.categories.size + 1}",
                            name = name,
                            description = description,
                            defaultPriority = priority,
                            isActive = isActive
                        )
                    )
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
    var showCreatedSnackbar by remember { mutableStateOf(true) }

    if (userToDeactivate != null) {
        CaConfirmationDialog(
            title = "¿Desactivar operador?",
            message = "${userToDeactivate!!.fullName} perderá el acceso al sistema.\n\nSus 3 reportes quedarán marcados para reasignación:\n• RP-0012 · Hueco en la vía (Vías · Alta · Asignado)\n• RP-0007 · Luminaria apagada (Alumbrado · Alta · En atención)\n• RP-0013 · Luminaria intermitente (Alumbrado · Media · Asignado)\n\nEl gestor deberá asignarlos a otro operador.",
            confirmText = "Desactivar operador",
            cancelText = "Cancelar",
            confirmType = CaButtonType.DESTRUCTIVO,
            onConfirm = {
                userToDeactivate!!.isActive = false
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

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "3 usuarios internos · 2 activos",
                style = Texto12Semibold
            )
            Spacer(modifier = Modifier.height(10.dp))

            CiudadActivaRepository.staffUsers.drop(2).forEach { usr ->
                Surface(
                    color = CaWhite,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CaBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (usr.isActive && usr.role == UserRole.OPERADOR) {
                                userToDeactivate = usr
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
    var fullName by remember { mutableStateOf("Diana Rojas") }
    var email by remember { mutableStateOf("diana.rojas@alcaldia.gov.co") }
    var selectedRole by remember { mutableStateOf(UserRole.GESTOR) }
    var isActive by remember { mutableStateOf(true) }

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
                onValueChange = { fullName = it }
            )
            Spacer(modifier = Modifier.height(14.dp))

            CaTextField(
                label = "Correo institucional",
                value = email,
                onValueChange = { email = it }
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
                    text = "Admite reportes y coordina su atención.",
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
                        text = "Recibirá un correo para crear su contraseña. Podrás cambiar el rol al editarlo.",
                        style = Texto12Regular.copy(color = CaMuted)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            CaButton(
                text = "Crear usuario interno",
                onClick = {
                    CiudadActivaRepository.staffUsers.add(
                        StaffUserItem(
                            id = "usr-new-${CiudadActivaRepository.staffUsers.size + 1}",
                            fullName = fullName,
                            email = email,
                            role = selectedRole,
                            isActive = isActive,
                            statusDetail = "Gestión y seguimiento"
                        )
                    )
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
                Text("120 reportes", style = Texto12Semibold)
                Text("Más recientes primero", style = Texto12Regular.copy(color = CaMuted))
            }

            Spacer(modifier = Modifier.height(12.dp))

            CiudadActivaRepository.reports.take(2).forEachIndexed { idx, rep ->
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
                        text = if (idx == 0) "5 comentarios" else "3 comentarios",
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

            Text(
                text = "Mostrando 2 de 120 reportes",
                style = Texto12Regular.copy(color = CaMuted),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
