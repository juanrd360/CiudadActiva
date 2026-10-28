package com.example.ciudadactiva.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ciudadactiva.data.CiudadActivaRepository
import com.example.ciudadactiva.network.ApiClient
import com.example.ciudadactiva.network.models.ReporteCreateDTO
import com.example.ciudadactiva.network.models.ReporteUpdateDTO
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
        categoriaId: Int,
        descripcion: String,
        latitud: Double,
        longitud: Double,
        direccion: String?,
        origen: String = "GPS"
    ) {
        viewModelScope.launch {
            _uiState.value = CreateReportState.Loading
            try {
                val ciudadanoId = CiudadActivaRepository.currentUserId

                if (ciudadanoId == null) {
                    _uiState.value = CreateReportState.Error("Debes iniciar sesión primero.")
                    return@launch
                }

                val safeLat = latitud.coerceIn(-90.0, 90.0)
                val safeLng = longitud.coerceIn(-180.0, 180.0)

                val request = ReporteCreateDTO(
                    ciudadanoId = ciudadanoId,
                    categoriaId = categoriaId,
                    descripcion = descripcion.trim(),
                    ubicacion = UbicacionDTO(
                        latitud = safeLat,
                        longitud = safeLng,
                        direccion = direccion?.trim(),
                        origen = origen,
                        precision = 1.0f
                    )
                )

                val created = ApiClient.apiService.createReport(request)
                CiudadActivaRepository.upsertReports(listOf(created))

                _uiState.value = CreateReportState.Success
            } catch (e: HttpException) {
                val userMsg = when (e.code()) {
                    400 -> "Datos inválidos para crear el reporte. Revisa categoría, descripción y ubicación."
                    404 -> "Usuario o categoría no encontrada en el servidor."
                    500, 502, 503 -> "Error en el servidor de Render (${e.code()}). Intenta de nuevo."
                    else -> "No pudimos enviar tu reporte (Código ${e.code()})."
                }
                _uiState.value = CreateReportState.Error(userMsg, isNetworkError = false)
            } catch (e: IOException) {
                _uiState.value = CreateReportState.Error(
                    "No hay conexión con el servidor. Revisa tu internet.",
                    isNetworkError = true
                )
            } catch (e: Exception) {
                _uiState.value = CreateReportState.Error(
                    "Ocurrió un error inesperado al enviar el reporte.",
                    isNetworkError = false
                )
            }
        }
    }

    fun updateReport(
        reportId: Long,
        categoriaId: Int,
        descripcion: String,
        latitud: Double,
        longitud: Double,
        direccion: String?
    ) {
        viewModelScope.launch {
            _uiState.value = CreateReportState.Loading
            try {
                val ciudadanoId = CiudadActivaRepository.currentUserId
                if (ciudadanoId == null) {
                    _uiState.value = CreateReportState.Error("Debes iniciar sesión primero.")
                    return@launch
                }
                val request = ReporteUpdateDTO(
                    descripcion = descripcion.trim(),
                    categoriaId = categoriaId,
                    ubicacion = UbicacionDTO(
                        latitud = latitud.coerceIn(-90.0, 90.0),
                        longitud = longitud.coerceIn(-180.0, 180.0),
                        direccion = direccion?.trim(),
                        origen = "MANUAL",
                        precision = 1.0f
                    )
                )
                val updated = ApiClient.apiService.updateOwnReport(reportId, ciudadanoId, request)
                CiudadActivaRepository.upsertReports(listOf(updated))
                _uiState.value = CreateReportState.Success
            } catch (e: HttpException) {
                _uiState.value = CreateReportState.Error("No se pudo actualizar el reporte (Código ${e.code()}).")
            } catch (e: IOException) {
                _uiState.value = CreateReportState.Error("No hay conexión con el servidor.", isNetworkError = true)
            } catch (e: Exception) {
                _uiState.value = CreateReportState.Error("Error inesperado al actualizar el reporte.")
            }
        }
    }

    fun resetState() {
        _uiState.value = CreateReportState.Idle
    }
}
