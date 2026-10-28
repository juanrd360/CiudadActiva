package com.example.ciudadactiva.network.models

data class LoginRequest(
    val correo: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val tipoToken: String? = "Bearer",
    val usuarioId: Long,
    val nombreCompleto: String,
    val correo: String,
    val rol: String
)

data class RolResponse(
    val id: Int? = null,
    val nombre: String? = null
)

data class UsuarioResponse(
    val id: Long,
    val nombre: String? = null,
    val apellido: String? = null,
    val correo: String? = null,
    val telefono: String? = null,
    val activo: Boolean = true,
    val fechaRegistro: String? = null,
    val rol: RolResponse? = null
) {
    val fullName: String
        get() = listOfNotNull(nombre, apellido).joinToString(" ").ifBlank { correo ?: "Usuario #$id" }
}

typealias UserDto = UsuarioResponse

data class RegisterRequest(
    val nombre: String,
    val apellido: String,
    val correo: String,
    val password: String,
    val telefono: String
)

data class UsuarioCreateDTO(
    val nombre: String,
    val apellido: String,
    val correo: String,
    val password: String,
    val telefono: String,
    val rol: String
)

data class UsuarioUpdateDTO(
    val nombre: String,
    val apellido: String,
    val correo: String? = null,
    val telefono: String? = null
)

data class PasswordUpdateDTO(
    val passwordActual: String,
    val passwordNueva: String
)
