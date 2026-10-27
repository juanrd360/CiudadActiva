package com.example.ciudadactiva.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ciudadactiva.network.ApiClient
import com.example.ciudadactiva.network.models.ReporteCreateDTO
import com.example.ciudadactiva.network.models.UbicacionDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed class CreateReportState {
    object Idle : CreateReportState()
    object Loading : CreateReportState()
    object Success : CreateReportState()
    data class Error(val message: String, val isNetworkError: Boolean = false) : CreateReportState()
}

class CreateReportViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<CreateReportState>(CreateReportState.Idle)
    val uiState: StateFlow<CreateReportState> = _uiState.asStateFlow()

    fun createReport(
        categoriaId: Short,
        descripcion: String,
        latitud: Double,
        longitud: Double,
        direccion: String?
    ) {
        viewModelScope.launch {
            _uiState.value = CreateReportState.Loading
            try {
                // TODO: Reemplazar el "1L" por el ID real del usuario que inició sesión.
                // Por ahora usamos 1L para que la API no falle por "ciudadanoId obligatorio"
                val ciudadanoId = 1L 
                
                val request = ReporteCreateDTO(
                    ciudadanoId = ciudadanoId,
                    categoriaId = categoriaId,
                    descripcion = descripcion,
                    ubicacion = UbicacionDTO(latitud, longitud, direccion)
                )
                
                ApiClient.apiService.createReport(request)
                
                _uiState.value = CreateReportState.Success
            } catch (e: HttpException) {
                val userMsg = when (e.code()) {
                    400 -> "Faltan datos obligatorios para crear el reporte."
                    500, 502, 503 -> "Nuestro servidor está en mantenimiento. Intenta más tarde."
                    else -> "No pudimos enviar tu reporte. Inténtalo de nuevo."
                }
                _uiState.value = CreateReportState.Error(userMsg, isNetworkError = false)
            } catch (e: IOException) {
                _uiState.value = CreateReportState.Error("No hay conexión con el servidor. Revisa tu internet.", isNetworkError = true)
            } catch (e: Exception) {
                _uiState.value = CreateReportState.Error("Ocurrió un error inesperado al enviar el reporte.", isNetworkError = false)
            }
        }
    }
    
    fun resetState() {
        _uiState.value = CreateReportState.Idle
    }
}
