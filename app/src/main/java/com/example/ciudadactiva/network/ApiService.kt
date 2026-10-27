package com.example.ciudadactiva.network

import com.example.ciudadactiva.network.models.LoginRequest
import com.example.ciudadactiva.network.models.LoginResponse
import com.example.ciudadactiva.network.models.RegisterRequest

import com.example.ciudadactiva.network.models.ReporteCreateDTO
import com.example.ciudadactiva.network.models.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    
    // Endpoint para Iniciar Sesión

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/registro")
    suspend fun register(@Body request: RegisterRequest): UserDto

    // Endpoint para obtener los reportes

    // @GET("api/reports")
    // suspend fun getReports(): List<ReportResponse>
    
    @POST("api/reportes")
    suspend fun createReport(@Body request: ReporteCreateDTO)
}
