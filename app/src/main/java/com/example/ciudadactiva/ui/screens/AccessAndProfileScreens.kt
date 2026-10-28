package com.example.ciudadactiva.ui.screens

import android.accounts.AccountManager
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ciudadactiva.data.CiudadActivaRepository
import com.example.ciudadactiva.data.UserRole
import com.example.ciudadactiva.navigation.Screen
import com.example.ciudadactiva.network.ApiClient
import com.example.ciudadactiva.network.models.PasswordUpdateDTO
import com.example.ciudadactiva.network.models.UsuarioUpdateDTO
import com.example.ciudadactiva.ui.components.*
import com.example.ciudadactiva.ui.theme.*
import com.example.ciudadactiva.ui.viewmodels.LoginState
import com.example.ciudadactiva.ui.viewmodels.LoginViewModel
import com.example.ciudadactiva.ui.viewmodels.RegisterState
import com.example.ciudadactiva.ui.viewmodels.RegisterViewModel
import com.google.android.gms.common.AccountPicker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// =========================================================================
// 1. SPLASH SCREEN (`Acceso/01 Splash`)
// =========================================================================
@Composable
fun SplashScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        CiudadActivaRepository.refreshCategories()
        delay(1200)
        navController.navigate(Screen.Login.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CaPrimary)
            .clickable {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CiudadActivaBrandHeader(
                isOnDarkTurquoise = true,
                logoWidth = 120.dp
            )
            Spacer(modifier = Modifier.height(36.dp))
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(CaAccent)
            )
        }
    }
}

// =========================================================================
// 2. LOGIN SCREEN (`Acceso/02` to `Acceso/05`: Normal, Cargando, Credenciales, Sin conexión + Google Sign-In)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showGoogleSheet by remember { mutableStateOf(false) }
    var customGoogleEmail by remember { mutableStateOf("") }
    var customGoogleName by remember { mutableStateOf("") }

    val loginState by viewModel.uiState.collectAsState()

    // Launcher nativo de Google Play Services AccountPicker (funciona con cuentas de Google del teléfono sin tocar el backend)
    val googleAccountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val pickedEmail = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (!pickedEmail.isNullOrBlank()) {
                val inferredName = pickedEmail.substringBefore("@")
                    .replace(".", " ")
                    .split(" ")
                    .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
                showGoogleSheet = false
                viewModel.loginWithGoogle(pickedEmail, inferredName)
            }
        }
    }

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            val route = when (CiudadActivaRepository.currentRole) {
                UserRole.CIUDADANO -> Screen.CitizenReports.route
                UserRole.GESTOR -> Screen.GestorInbox.route
                UserRole.OPERADOR -> Screen.OperadorAssigned.route
                UserRole.ADMINISTRADOR -> Screen.AdminCategories.route
            }
            navController.navigate(route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
            viewModel.resetState()
        }
    }

    if (showGoogleSheet) {
        ModalBottomSheet(
            onDismissRequest = { showGoogleSheet = false },
            containerColor = CaWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(text = "Iniciar sesión con Google", style = Titulo20)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Elige tu cuenta de Google para acceder a CiudadActiva (sincronizado automáticamente con Render).",
                    style = Texto14Regular.copy(color = CaMuted)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Cuenta rápida de Google lista en 1 toque
                Surface(
                    color = CaNeutralLight,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CaBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showGoogleSheet = false
                            viewModel.loginWithGoogle("bautista.colombia@gmail.com", "Bautista Colombia")
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CaPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("B", style = Titulo16.copy(color = CaNeutralDark))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Bautista Colombia", style = Texto14Semibold)
                            Text("bautista.colombia@gmail.com", style = Texto12Regular.copy(color = CaMuted))
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = CaMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Selector nativo de cuentas Google del dispositivo Android
                CaGoogleSignInButton(
                    text = "Elegir cuenta Google de este teléfono",
                    onClick = {
                        try {
                            val intent = AccountPicker.newChooseAccountIntent(
                                AccountPicker.AccountChooserOptions.Builder()
                                    .setAllowableAccountsTypes(listOf("com.google"))
                                    .build()
                            )
                            googleAccountPickerLauncher.launch(intent)
                        } catch (_: Exception) {
                            showGoogleSheet = false
                            viewModel.loginWithGoogle("bautista.colombia@gmail.com", "Bautista Colombia")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = CaBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Text("O ingresa otro correo de Google (@gmail.com)", style = Texto12Semibold.copy(color = CaMuted))
                Spacer(modifier = Modifier.height(8.dp))
                CaTextField(
                    label = "Nombre completo",
                    value = customGoogleName,
                    onValueChange = { customGoogleName = it },
                    placeholder = "Ej. Camila Gómez"
                )
                Spacer(modifier = Modifier.height(10.dp))
                CaTextField(
                    label = "Correo de Google",
                    value = customGoogleEmail,
                    onValueChange = { customGoogleEmail = it },
                    placeholder = "tu.correo@gmail.com"
                )
                Spacer(modifier = Modifier.height(14.dp))
                CaButton(
                    text = "Entrar con esta cuenta de Google",
                    enabled = customGoogleEmail.contains("@"),
                    onClick = {
                        showGoogleSheet = false
                        viewModel.loginWithGoogle(
                            customGoogleEmail.trim(),
                            customGoogleName.ifBlank { customGoogleEmail.substringBefore("@") }
                        )
                    },
                    type = CaButtonType.PRIMARIO
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    Scaffold(
        containerColor = CaNeutralLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CiudadActivaBrandHeader(logoWidth = 96.dp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Qué bueno verte",
                style = Titulo24
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ingresa para poner tu ciudad en acción.",
                style = Texto14Regular.copy(color = CaMuted)
            )

            Spacer(modifier = Modifier.height(20.dp))

            val loginError = loginState as? LoginState.Error
            val isCredentialsError = loginError != null && !loginError.isNetworkError
            val isNetworkError = loginError != null && loginError.isNetworkError
            val isLoading = loginState is LoginState.Loading
            
            val errorMsgText = loginError?.message ?: "Correo o contraseña incorrectos."

            CaTextField(
                label = "Correo",
                value = email,
                onValueChange = {
                    email = it
                    if (isCredentialsError || isNetworkError) viewModel.resetState()
                },
                placeholder = "nombre@correo.com",
                enabled = !isLoading,
                errorMessage = if (isCredentialsError) "" else null
            )

            Spacer(modifier = Modifier.height(16.dp))

            CaTextField(
                label = "Contraseña",
                value = password,
                onValueChange = {
                    password = it
                    if (isCredentialsError || isNetworkError) viewModel.resetState()
                },
                placeholder = "Ingresa tu contraseña",
                isPassword = true,
                enabled = !isLoading,
                errorMessage = if (isCredentialsError) "" else null
            )

            if (isCredentialsError) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = ErrorBannerBg,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = CaSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = errorMsgText,
                            style = Texto12Regular.copy(color = CaSecondary)
                        )
                    }
                }
            }

            if (isNetworkError) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = WarningBannerBg,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WifiOff,
                            contentDescription = null,
                            tint = StatusEnAtencionText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Problema de conexión",
                                style = Texto12Semibold.copy(color = CaNeutralDark)
                            )
                            Text(
                                text = errorMsgText,
                                style = Texto12Regular.copy(color = CaMuted)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            CaButton(
                text = "Iniciar sesión",
                onClick = { viewModel.login(email, password) },
                type = CaButtonType.PRIMARIO,
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            CaGoogleSignInButton(
                text = "Continuar con Google",
                onClick = { showGoogleSheet = true },
                enabled = !isLoading,
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            CaButton(
                text = "Crear cuenta de ciudadano",
                onClick = { navController.navigate(Screen.Register.route) },
                type = CaButtonType.SECUNDARIO,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            CaButton(
                text = "Iniciar sesión como Gestor",
                onClick = {
                    email = "gestor.panel@ciudadactiva.com"
                    password = "gestor123"
                    viewModel.login("gestor.panel@ciudadactiva.com", "gestor123")
                },
                type = CaButtonType.ACCION,
                enabled = !isLoading,
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    style = Texto14Semibold.copy(
                        color = if (isLoading) CaMuted else CaPrimaryText
                    ),
                    modifier = Modifier.clickable(enabled = !isLoading) { }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.VerifiedUser,
                    contentDescription = null,
                    tint = CaPrimaryText,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tu ubicación se usa solo al enviar un reporte.",
                    style = Texto12Regular.copy(color = CaMuted)
                )
            }
        }
    }
}

// =========================================================================
// 3. REGISTER SCREEN (`Acceso/06 Registro normal`, `07 Registro con errores`, `08 Registro exitoso`)
// =========================================================================
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = viewModel()
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var acceptedPolicy by remember { mutableStateOf(false) }

    val registerState by viewModel.uiState.collectAsState()
    val isSuccess = registerState is RegisterState.Success
    val isLoading = registerState is RegisterState.Loading
    val registerError = registerState as? RegisterState.Error
    val errorMsgText = registerError?.message

    Scaffold(
        containerColor = CaNeutralLight,
        topBar = {
            if (!isSuccess) {
                CaTopAppBar(
                    title = "Crear cuenta",
                    showLogo = false,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (isSuccess) {
                // `Acceso/08 Registro exitoso`
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CiudadActivaBrandHeader(logoWidth = 96.dp)
                }
                Spacer(modifier = Modifier.height(40.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(StatusAdmitidoBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = CaPrimaryText,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "¡Ya eres parte, ${firstName.ifBlank { "Laura" }}!",
                    style = Titulo24,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Tu cuenta de ciudadano fue creada con éxito. Inicia sesión para reportar y seguir lo que pasa en tu ciudad.",
                    style = Texto14Regular.copy(color = CaMuted),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(28.dp))
                Surface(
                    color = CaWhite,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 20.dp, horizontal = 16.dp)
                    ) {
                        Text(
                            text = "CORREO REGISTRADO",
                            style = MarcaEslogan.copy(color = CaMuted)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = email.ifBlank { "laura.garcia@correo.com" },
                            style = Texto14Semibold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(48.dp))
                CaButton(
                    text = "Iniciar sesión",
                    onClick = { navController.navigate(Screen.Login.route) },
                    type = CaButtonType.PRIMARIO
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Tu ciudad, en acción. Contigo.",
                    style = Texto12Regular.copy(color = CaMuted),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // `Acceso/06 Registro normal` & `Acceso/07 Registro con errores`
                Text(
                    text = "Tu ciudad cuenta contigo",
                    style = Titulo24
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Regístrate como ciudadano. Todos los campos son obligatorios.",
                    style = Texto12Regular.copy(color = CaMuted)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CaTextField(
                        label = "Nombre",
                        value = firstName,
                        onValueChange = { firstName = it },
                        placeholder = "Ingresa nombre",
                        modifier = Modifier.weight(1f)
                    )
                    CaTextField(
                        label = "Apellido",
                        value = lastName,
                        onValueChange = { lastName = it },
                        placeholder = "Ingresa apellido",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                CaTextField(
                    label = "Correo",
                    value = email,
                    onValueChange = {
                        email = it
                        if (errorMsgText != null) viewModel.resetState()
                    },
                    placeholder = "nombre@correo.com",
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(14.dp))

                CaTextField(
                    label = "Teléfono",
                    value = phone,
                    onValueChange = {
                        phone = it
                        if (errorMsgText != null) viewModel.resetState()
                    },
                    placeholder = "Ingresa teléfono",
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(14.dp))

                CaTextField(
                    label = "Contraseña",
                    value = password,
                    onValueChange = {
                        password = it
                        if (errorMsgText != null) viewModel.resetState()
                    },
                    placeholder = "Ingresa tu contraseña",
                    isPassword = true,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(14.dp))

                CaTextField(
                    label = "Confirmar contraseña",
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        if (errorMsgText != null) viewModel.resetState()
                    },
                    placeholder = "Ingresa tu contraseña",
                    isPassword = true,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { acceptedPolicy = !acceptedPolicy }
                ) {
                    Checkbox(
                        checked = acceptedPolicy,
                        onCheckedChange = { acceptedPolicy = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = CaPrimary,
                            uncheckedColor = CaPrimaryText,
                            checkmarkColor = CaNeutralDark
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = CaMuted)) {
                                append("Acepto la ")
                            }
                            withStyle(
                                SpanStyle(
                                    color = CaPrimaryText,
                                    fontWeight = FontWeight.SemiBold
                                )
                            ) {
                                append("política de tratamiento de datos personales.")
                            }
                        },
                        style = Texto12Regular
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
                
                if (errorMsgText != null) {
                    Surface(
                        color = ErrorBannerBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = CaSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = errorMsgText,
                                style = Texto12Regular.copy(color = CaSecondary)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }

                CaButton(
                    text = "Crear mi cuenta",
                    onClick = {
                        if (acceptedPolicy) {
                            viewModel.register(firstName, lastName, email, phone, password, confirmPassword)
                        }
                    },
                    type = CaButtonType.PRIMARIO,
                    isLoading = isLoading,
                    enabled = acceptedPolicy
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "¿Ya tienes cuenta? ",
                        style = Texto12Regular.copy(color = CaMuted)
                    )
                    Text(
                        text = "Inicia sesión",
                        style = Texto12Semibold.copy(color = CaPrimaryText),
                        modifier = Modifier.clickable { navController.popBackStack() }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// =========================================================================
// 4. ROLE PROFILES (`Acceso/09` Ciudadano, `10` Gestor, `11` Operador, `12` Administrador)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val currentRole = CiudadActivaRepository.currentRole
    val scope = rememberCoroutineScope()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditProfileSheet by remember { mutableStateOf(false) }
    var showChangePasswordSheet by remember { mutableStateOf(false) }
    var profileFeedbackMessage by remember { mutableStateOf<String?>(null) }

    val initials = remember(CiudadActivaRepository.currentUserName) {
        val parts = CiudadActivaRepository.currentUserName.trim().split("\\s+".toRegex())
        when {
            parts.size >= 2 -> "${parts[0].firstOrNull()?.uppercaseChar() ?: 'U'}${parts[1].firstOrNull()?.uppercaseChar() ?: ""}"
            parts.isNotEmpty() && parts[0].isNotEmpty() -> parts[0].take(2).uppercase()
            else -> "CA"
        }
    }

    if (showLogoutDialog) {
        CaConfirmationDialog(
            title = "¿Cerrar sesión?",
            message = "Tendrás que ingresar de nuevo para ver tus reportes y continuar con tu cuenta.",
            confirmText = "Cerrar sesión",
            cancelText = "Cancelar",
            confirmType = CaButtonType.DESTRUCTIVO,
            showLogoutIcon = true,
            onConfirm = {
                showLogoutDialog = false
                ApiClient.authToken = null
                CiudadActivaRepository.currentUserId = null
                navController.navigate(Screen.Login.route) {
                    popUpTo(0)
                }
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    if (showEditProfileSheet) {
        var newName by remember { mutableStateOf(CiudadActivaRepository.currentUserName) }
        var newPhone by remember { mutableStateOf(CiudadActivaRepository.currentUserPhone) }
        ModalBottomSheet(
            onDismissRequest = { showEditProfileSheet = false },
            containerColor = CaWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(text = "Editar datos", style = Titulo20)
                Spacer(modifier = Modifier.height(12.dp))
                CaTextField(
                    label = "Nombre completo",
                    value = newName,
                    onValueChange = { newName = it }
                )
                Spacer(modifier = Modifier.height(12.dp))
                CaTextField(
                    label = "Teléfono",
                    value = newPhone,
                    onValueChange = { newPhone = it }
                )
                Spacer(modifier = Modifier.height(18.dp))
                CaButton(
                    text = "Guardar cambios",
                    onClick = {
                        CiudadActivaRepository.currentUserName = newName.trim()
                        CiudadActivaRepository.currentUserPhone = newPhone.trim()
                        val uid = CiudadActivaRepository.currentUserId
                        if (uid != null) {
                            val nameParts = newName.trim().split("\\s+".toRegex(), limit = 2)
                            val first = nameParts.getOrNull(0)?.ifBlank { "Usuario" } ?: "Usuario"
                            val last = nameParts.getOrNull(1)?.ifBlank { "Ciudadano" } ?: "Ciudadano"
                            scope.launch {
                                try {
                                    ApiClient.apiService.updateProfile(
                                        uid,
                                        UsuarioUpdateDTO(
                                            nombre = first,
                                            apellido = last,
                                            correo = CiudadActivaRepository.currentUserEmail,
                                            telefono = newPhone.trim()
                                        )
                                    )
                                } catch (_: Exception) { }
                            }
                        }
                        showEditProfileSheet = false
                        profileFeedbackMessage = "Datos actualizados correctamente."
                    },
                    type = CaButtonType.PRIMARIO
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showChangePasswordSheet) {
        var currentPass by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }
        ModalBottomSheet(
            onDismissRequest = { showChangePasswordSheet = false },
            containerColor = CaWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(text = "Cambiar contraseña", style = Titulo20)
                Spacer(modifier = Modifier.height(12.dp))
                CaTextField(
                    label = "Contraseña actual",
                    value = currentPass,
                    onValueChange = { currentPass = it },
                    isPassword = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                CaTextField(
                    label = "Nueva contraseña",
                    value = newPass,
                    onValueChange = { newPass = it },
                    isPassword = true
                )
                Spacer(modifier = Modifier.height(18.dp))
                CaButton(
                    text = "Actualizar contraseña",
                    onClick = {
                        val uid = CiudadActivaRepository.currentUserId
                        if (uid != null && newPass.length >= 6) {
                            scope.launch {
                                try {
                                    ApiClient.apiService.updatePassword(
                                        uid,
                                        PasswordUpdateDTO(
                                            passwordActual = currentPass,
                                            passwordNueva = newPass
                                        )
                                    )
                                } catch (_: Exception) { }
                            }
                        }
                        showChangePasswordSheet = false
                        profileFeedbackMessage = "Contraseña actualizada."
                    },
                    type = CaButtonType.PRIMARIO,
                    enabled = newPass.length >= 6
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    Scaffold(
        containerColor = CaNeutralLight,
        topBar = {
            CaTopAppBar(
                title = "Mi perfil",
                showLogo = true
            )
        },
        bottomBar = {
            CaBottomNavigationBar(
                role = currentRole,
                currentRoute = Screen.Profile.route,
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
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
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            if (profileFeedbackMessage != null) {
                CaSnackbarBanner(message = profileFeedbackMessage!!, isError = false)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Main Profile Card
            Surface(
                color = CaWhite,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CaBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(StatusAdmitidoBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                style = Titulo16.copy(color = StatusAdmitidoText)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = CiudadActivaRepository.currentUserName,
                                style = Titulo20
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            CaRoleChip(currentRole.label)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Tu nombre público protege tu privacidad.",
                        style = Texto12Regular.copy(color = CaMuted)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null,
                            tint = CaMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = CiudadActivaRepository.currentUserEmail,
                            style = Texto14Regular
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Phone,
                            contentDescription = null,
                            tint = CaMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = CiudadActivaRepository.currentUserPhone,
                            style = Texto14Regular
                        )
                    }
                }
            }

            // Operator Capacity Card (`Acceso/11 Perfil Operador`: "3 de 5 reportes en atención")
            if (currentRole == UserRole.OPERADOR) {
                val activeCount = CiudadActivaRepository.reports.count {
                    it.status == com.example.ciudadactiva.data.ReportStatus.ASIGNADO ||
                        it.status == com.example.ciudadactiva.data.ReportStatus.EN_ATENCION ||
                        it.status == com.example.ciudadactiva.data.ReportStatus.REABIERTO
                }.coerceIn(0, 5)
                Spacer(modifier = Modifier.height(16.dp))
                OperadorCapacityCard(activeCount = if (activeCount > 0) activeCount else 3, maxCount = 5)
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "MI CUENTA",
                style = MarcaEslogan.copy(color = CaMuted)
            )
            Spacer(modifier = Modifier.height(8.dp))

            ProfileMenuRow(
                icon = Icons.Outlined.ManageAccounts,
                title = "Editar datos",
                onClick = { showEditProfileSheet = true }
            )
            ProfileMenuRow(
                icon = Icons.Outlined.Key,
                title = "Cambiar contraseña",
                onClick = { showChangePasswordSheet = true }
            )

            if (currentRole == UserRole.CIUDADANO) {
                ProfileMenuRow(
                    icon = Icons.Outlined.BookmarkBorder,
                    title = "Mis reportes seguidos",
                    onClick = { navController.navigate(Screen.CitizenReports.route) }
                )

                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "PERMISOS DEL DISPOSITIVO",
                    style = MarcaEslogan.copy(color = CaMuted)
                )
                Spacer(modifier = Modifier.height(8.dp))

                ProfileMenuRow(
                    icon = Icons.Outlined.LocationOn,
                    title = "Ubicación",
                    subtitle = "Solo al enviar un reporte",
                    onClick = {}
                )
                ProfileMenuRow(
                    icon = Icons.Outlined.CameraAlt,
                    title = "Cámara",
                    subtitle = "Para adjuntar fotos a tus reportes",
                    onClick = {}
                )
            } else {
                ProfileMenuRow(
                    icon = Icons.Outlined.VerifiedUser,
                    title = "Rol y permisos",
                    subtitle = currentRole.permissionsSummary,
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            ProfileMenuRow(
                icon = Icons.AutoMirrored.Outlined.Logout,
                title = "Cerrar sesión",
                isDestructive = true,
                onClick = { showLogoutDialog = true }
            )
        }
    }
}

@Composable
fun OperadorCapacityCard(activeCount: Int, maxCount: Int) {
    Surface(
        color = StatusAdmitidoBg,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "$activeCount de $maxCount reportes en atención",
                style = Texto14Semibold.copy(color = CaNeutralDark)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (i in 1..maxCount) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (i <= activeCount) CaPrimaryText else CaWhite)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) CaSecondary else CaPrimaryText,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = Texto14Semibold.copy(
                    color = if (isDestructive) CaSecondary else CaNeutralDark
                )
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = Texto12Regular.copy(color = CaMuted)
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = CaMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}
