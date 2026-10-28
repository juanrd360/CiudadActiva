package com.example.ciudadactiva.network.models

data class ReporteCreateDTO(
    val ciudadanoId: Long,
    val categoriaId: Int,
    val descripcion: String,
    val ubicacion: UbicacionDTO
)

data class ReporteUpdateDTO(
    val descripcion: String,
    val categoriaId: Int? = null,
    val ubicacion: UbicacionDTO? = null
)

data class UbicacionDTO(
    val latitud: Double,
    val longitud: Double,
    val direccion: String? = null,
    val origen: String = "GPS",
    val precision: Float? = 1.0f
)

data class CategoriaIncidenciaDTO(
    val id: Int,
    val nombre: String,
    val descripcion: String? = null,
    val prioridadInicial: String? = "MEDIA",
    val activa: Boolean = true
)

data class CategoriaCreateDTO(
    val nombre: String,
    val descripcion: String? = null,
    val prioridadInicial: String
)

data class UbicacionResponse(
    val id: Long? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val precision: Float? = null,
    val origen: String? = null,
    val direccion: String? = null,
    val fechaCaptura: String? = null
)

data class ReporteResponse(
    val id: Long,
    val codigo: String? = null,
    val descripcion: String? = null,
    val estado: String? = null,
    val prioridad: String? = null,
    val motivoRechazo: String? = null,
    val reabierto: Boolean? = false,
    val calificacion: Int? = null,
    val fechaCreacion: String? = null,
    val fechaActualizacion: String? = null,
    val fechaCierre: String? = null,
    val fechaConfirmacion: String? = null,
    val categoria: CategoriaIncidenciaDTO? = null,
    val ciudadano: UsuarioResponse? = null,
    val ubicacion: UbicacionResponse? = null
)

data class PageReporteResponse(
    val content: List<ReporteResponse> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0
)

data class SeguimientoResponse(
    val id: Long,
    val autor: UsuarioResponse? = null,
    val tipoAccion: String? = null,
    val estadoAnterior: String? = null,
    val estadoNuevo: String? = null,
    val observacion: String? = null,
    val fechaRegistro: String? = null
)

data class ComentarioResponse(
    val id: Long,
    val autor: UsuarioResponse? = null,
    val comentarioPadre: ComentarioResponse? = null,
    val contenido: String? = null,
    val tipo: String? = null,
    val estado: String? = null,
    val fechaCreacion: String? = null,
    val fechaEdicion: String? = null
)

data class PageComentarioResponse(
    val content: List<ComentarioResponse> = emptyList(),
    val totalElements: Long = 0
)

data class ComentarioRequestDTO(
    val autorId: Long,
    val comentarioPadreId: Long? = null,
    val contenido: String
)

data class AsignacionRequestDTO(
    val gestorId: Long,
    val operadorId: Long,
    val nuevaPrioridad: String? = null,
    val motivoCambioPrioridad: String? = null,
    val motivoReasignacion: String? = null
)

data class ConfirmacionReporteDTO(
    val ciudadanoId: Long,
    val calificacion: Int? = 5,
    val comentarioResultado: String? = null
)

data class NotificacionResponse(
    val id: Long,
    val reporte: ReporteResponse? = null,
    val mensaje: String? = null,
    val leida: Boolean = false,
    val fecha: String? = null
)

data class PageNotificacionResponse(
    val content: List<NotificacionResponse> = emptyList(),
    val totalElements: Long = 0
)
