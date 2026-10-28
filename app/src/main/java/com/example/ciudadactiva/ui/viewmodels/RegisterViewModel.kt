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

    fun register(
        nombre: String,
        apellido: String,
        correo: String,
        telefono: String,
        contrasena: String,
        confirmarContrasena: String = contrasena
    ) {
        if (nombre.isBlank() || apellido.isBlank() || correo.isBlank() || telefono.isBlank() || contrasena.isBlank()) {
            _uiState.value = RegisterState.Error("Todos los campos son obligatorios.")
            return
        }
        if (contrasena != confirmarContrasena) {
            _uiState.value = RegisterState.Error("Las contraseñas no coinciden.")
            return
        }
        if (contrasena.length < 6) {
            _uiState.value = RegisterState.Error("La contraseña debe tener al menos 6 caracteres.")
            return
        }
        val cleanPhone = telefono.trim()
        if (!cleanPhone.matches(Regex("^\\+?[0-9]{7,15}$"))) {
            _uiState.value = RegisterState.Error("El teléfono debe contener entre 7 y 15 dígitos numéricos.")
            return
        }

        viewModelScope.launch {
            _uiState.value = RegisterState.Loading
            try {
                val request = RegisterRequest(
                    nombre = nombre.trim(),
                    apellido = apellido.trim(),
                    correo = correo.trim(),
                    password = contrasena,
                    telefono = cleanPhone
                )
                ApiClient.apiService.register(request)
                _uiState.value = RegisterState.Success
            } catch (e: HttpException) {
                val userMsg = when (e.code()) {
                    400 -> "Faltan datos o el formato es inválido (verifica correo, teléfono de 7-15 dígitos y contraseña de mínimo 6 caracteres)."
                    409, 500 -> "Este correo electrónico ya está registrado o hubo un problema en el servidor."
                    502, 503 -> "Nuestro servidor en Render está despertando. Intenta en unos segundos."
                    else -> "No pudimos crear tu cuenta (Código ${e.code()})."
                }
                _uiState.value = RegisterState.Error(userMsg, isNetworkError = false)
            } catch (e: IOException) {
                _uiState.value = RegisterState.Error(
                    "No hay conexión con el servidor. Revisa tu internet o intenta en unos segundos.",
                    isNetworkError = true
                )
            } catch (e: Exception) {
                _uiState.value = RegisterState.Error("Ocurrió un error inesperado.", isNetworkError = false)
            }
        }
    }

    fun showValidationError(message: String) {
        _uiState.value = RegisterState.Error(message, isNetworkError = false)
    }

    fun resetState() {
        _uiState.value = RegisterState.Idle
    }
}
