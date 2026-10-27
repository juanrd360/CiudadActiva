package com.example.ciudadactiva.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ciudadactiva.network.ApiClient
import com.example.ciudadactiva.network.models.LoginRequest
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
        viewModelScope.launch {
            _uiState.value = LoginState.Loading
            try {
                // Hacemos el llamado a Retrofit
                val request = LoginRequest(email, pass)
                val response = ApiClient.apiService.login(request)
                
                // Si es exitoso
                _uiState.value = LoginState.Success(response.token, response.user.name)
            } catch (e: HttpException) {
                // Errores que el servidor de Render nos responde (401, 404, 500...)
                val userMsg = when (e.code()) {
                    400 -> "Faltan datos o son incorrectos. Revisa el correo y la contraseña."
                    401 -> "Correo o contraseña incorrectos. Verifica tus datos."
                    403 -> "Este correo no está registrado en el sistema."
                    500, 502, 503 -> "Nuestro servidor está en mantenimiento. Intenta más tarde."
                    else -> "No pudimos iniciar sesión. Inténtalo de nuevo."
                }
                _uiState.value = LoginState.Error(userMsg, isNetworkError = false)
            } catch (e: IOException) {
                // Errores porque no hay internet o el servidor en Render está apagado / durmiendo
                _uiState.value = LoginState.Error("No hay conexión con el servidor. Revisa tu internet o intenta en unos minutos.", isNetworkError = true)
            } catch (e: Exception) {
                // Cualquier otro error raro
                _uiState.value = LoginState.Error("Ocurrió un error inesperado en la aplicación.", isNetworkError = false)
            }
        }
    }
    
    fun resetState() {
        _uiState.value = LoginState.Idle
    }
}
