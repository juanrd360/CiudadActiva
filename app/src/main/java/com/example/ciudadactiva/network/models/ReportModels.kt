package com.example.ciudadactiva.network.models

// TODO: Ajusta los campos según la estructura exacta del JSON que devuelve tu API de Render

data class ReporteCreateDTO(
    val ciudadanoId: Long,
    val categoriaId: Short,
    val descripcion: String,
    val ubicacion: UbicacionDTO
)

data class UbicacionDTO(
    val latitud: Double,
    val longitud: Double,
    val direccion: String? = null
)
