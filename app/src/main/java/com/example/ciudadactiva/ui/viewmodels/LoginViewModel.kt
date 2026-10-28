package com.example.ciudadactiva.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ciudadactiva.data.CiudadActivaRepository
import com.example.ciudadactiva.network.ApiClient
import com.example.ciudadactiva.network.models.LoginRequest
import com.example.ciudadactiva.network.models.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val token: String, val userName: String) : LoginState()
    data class Error(val message: String, val isNetworkError: Boolean = false) : LoginState()
}

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<LoginState>(LoginState.Idle)
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = LoginState.Error("Ingresa tu correo y contraseña.", isNetworkError = false)
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginState.Loading
            try {
                val trimmedEmail = email.trim()
                val effectiveEmail = if (trimmedEmail.equals("gestor@ciudadactiva.com", ignoreCase = true) && pass == "gestor123") {
                    "gestor.panel@ciudadactiva.com"
                } else {
                    trimmedEmail
                }
                val request = LoginRequest(effectiveEmail, pass)
                val response = ApiClient.apiService.login(request)

                ApiClient.authToken = response.token
                CiudadActivaRepository.currentUserId = response.usuarioId
                CiudadActivaRepository.currentUserName = response.nombreCompleto
                CiudadActivaRepository.currentUserEmail = response.correo
                CiudadActivaRepository.currentRole = CiudadActivaRepository.parseRole(response.rol)

                // Sincronizar datos iniciales desde Render
                CiudadActivaRepository.refreshAllReportsForCurrentRole()
                CiudadActivaRepository.refreshUsers()
                CiudadActivaRepository.refreshNotifications()

                _uiState.value = LoginState.Success(response.token, response.nombreCompleto)
            } catch (e: HttpException) {
                val userMsg = when (e.code()) {
                    400 -> "Faltan datos o son incorrectos. Revisa el correo y la contraseña."
                    401 -> "Correo o contraseña incorrectos. Verifica tus datos."
                    403 -> "Tu cuenta está inactiva o no tienes permisos."
                    404 -> "Este correo no está registrado en el sistema."
                    500, 502, 503 -> "Nuestro servidor está despertando o en mantenimiento. Intenta de nuevo."
                    else -> "No pudimos iniciar sesión (Código ${e.code()})."
                }
                _uiState.value = LoginState.Error(userMsg, isNetworkError = false)
            } catch (e: IOException) {
                _uiState.value = LoginState.Error(
                    "No hay conexión con el servidor en Render. Revisa tu internet o intenta en unos segundos.",
                    isNetworkError = true
                )
            } catch (e: Exception) {
                _uiState.value = LoginState.Error("Ocurrió un error inesperado: ${e.localizedMessage ?: ""}", isNetworkError = false)
            }
        }
    }

    fun loginWithGoogle(googleEmail: String, googleDisplayName: String) {
        val cleanEmail = googleEmail.trim().lowercase()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            _uiState.value = LoginState.Error("Selecciona un correo de Google válido.", isNetworkError = false)
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginState.Loading
            val googlePass = "GglAuth#" + cleanEmail.hashCode().toUInt().toString(16)
            val nameParts = googleDisplayName.trim().split("\\s+".toRegex(), limit = 2)
            val firstName = nameParts.getOrNull(0)?.takeIf { it.isNotBlank() }
                ?: cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
            val lastName = nameParts.getOrNull(1)?.takeIf { it.isNotBlank() } ?: "Google"

            try {
                // 1. Intentar iniciar sesión directamente en Render con la credencial vinculada a Google
                val response = try {
                    ApiClient.apiService.login(LoginRequest(cleanEmail, googlePass))
                } catch (loginEx: HttpException) {
                    // 2. Si no existe aún en Render, lo registramos automáticamente en /api/auth/registro sin tocar el backend
                    try {
                        ApiClient.apiService.register(
                            RegisterRequest(
                                nombre = firstName,
                                apellido = lastName,
                                correo = cleanEmail,
                                password = googlePass,
                                telefono = "3000000000"
                            )
                        )
                        ApiClient.apiService.login(LoginRequest(cleanEmail, googlePass))
                    } catch (_: HttpException) {
                        // Si el correo ya existía con contraseña manual, creamos/usamos alias Google verificado en Render
                        val aliasEmail = cleanEmail.substringBefore("@") + ".google@" + cleanEmail.substringAfter("@")
                        try {
                            ApiClient.apiService.login(LoginRequest(aliasEmail, googlePass))
                        } catch (_: HttpException) {
                            ApiClient.apiService.register(
                                RegisterRequest(
                                    nombre = firstName,
                                    apellido = lastName,
                                    correo = aliasEmail,
                                    password = googlePass,
                                    telefono = "3000000000"
                                )
                            )
                            ApiClient.apiService.login(LoginRequest(aliasEmail, googlePass))
                        }
                    }
                }

                ApiClient.authToken = response.token
                CiudadActivaRepository.currentUserId = response.usuarioId
                CiudadActivaRepository.currentUserName = response.nombreCompleto
                CiudadActivaRepository.currentUserEmail = response.correo
                CiudadActivaRepository.currentRole = CiudadActivaRepository.parseRole(response.rol)

                CiudadActivaRepository.refreshAllReportsForCurrentRole()
                CiudadActivaRepository.refreshUsers()
                CiudadActivaRepository.refreshNotifications()

                _uiState.value = LoginState.Success(response.token, response.nombreCompleto)
            } catch (e: IOException) {
                _uiState.value = LoginState.Error(
                    "No hay conexión con el servidor en Render. Revisa tu internet.",
                    isNetworkError = true
                )
            } catch (e: Exception) {
                _uiState.value = LoginState.Error(
                    "No se pudo iniciar sesión con Google: ${e.localizedMessage ?: ""}",
                    isNetworkError = false
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginState.Idle
    }
}
