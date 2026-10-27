package com.example.ciudadactiva.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ciudadactiva.network.ApiClient
import com.example.ciudadactiva.network.models.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String, val isNetworkError: Boolean = false) : RegisterState()
}

class RegisterViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val uiState: StateFlow<RegisterState> = _uiState.asStateFlow()

    fun register(nombre: String, apellido: String, correo: String, telefono: String, contrasena: String) {
        viewModelScope.launch {
            _uiState.value = RegisterState.Loading
            try {
                // Hacemos el llamado a Retrofit
                val request = RegisterRequest(nombre, apellido, correo, contrasena, telefono)
                ApiClient.apiService.register(request)
                
                // Si es exitoso, pasamos al estado Success
                _uiState.value = RegisterState.Success
            } catch (e: HttpException) {
                // Errores que el servidor de Render nos responde (400, 409...)
                val userMsg = when (e.code()) {
                    400 -> "Faltan datos o el formato es incorrecto. Revisa todos los campos."
                    409 -> "Este correo electrónico ya está registrado." // Conflict
                    500, 502, 503 -> "Nuestro servidor está en mantenimiento. Intenta más tarde."
                    else -> "No pudimos crear tu cuenta. Inténtalo de nuevo."
                }
                _uiState.value = RegisterState.Error(userMsg, isNetworkError = false)
            } catch (e: IOException) {
                // Errores de red
                _uiState.value = RegisterState.Error("No hay conexión con el servidor. Revisa tu internet o intenta en unos minutos.", isNetworkError = true)
            } catch (e: Exception) {
                _uiState.value = RegisterState.Error("Ocurrió un error inesperado.", isNetworkError = false)
            }
        }
    }
    
    fun resetState() {
        _uiState.value = RegisterState.Idle
    }
}
