package com.example.ciudadactiva.network.models

// TODO: Ajusta los campos según lo que espera y responde tu API en Render

data class LoginRequest(
    val correo: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val tipoToken: String,
    val usuarioId: Long,
    val nombreCompleto: String,
    val correo: String,
    val rol: String
)

data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String
)

data class RegisterRequest(
    val nombre: String,
    val apellido: String,
    val correo: String,
    val password: String,
    val telefono: String
)
