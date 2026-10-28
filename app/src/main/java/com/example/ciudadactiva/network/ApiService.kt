package com.example.ciudadactiva.network

import com.example.ciudadactiva.network.models.*
import retrofit2.http.*

interface ApiService {

    // =========================================================================
    // 1. AUTENTICACIÓN Y REGISTRO
    // =========================================================================
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/registro")
    suspend fun register(@Body request: RegisterRequest): UsuarioResponse

    // =========================================================================
    // 2. USUARIOS Y ROLES
    // =========================================================================
    @GET("api/usuarios")
    suspend fun getUsers(@Query("rol") rol: String? = null): List<UsuarioResponse>

    @GET("api/usuarios/{id}")
    suspend fun getUserById(@Path("id") id: Long): UsuarioResponse

    @POST("api/usuarios")
    suspend fun createUserByAdmin(
        @Body request: UsuarioCreateDTO,
        @Query("adminId") adminId: Long? = null
    ): UsuarioResponse

    @PUT("api/usuarios/{id}")
    suspend fun updateProfile(
        @Path("id") id: Long,
        @Body request: UsuarioUpdateDTO
    ): UsuarioResponse

    @PATCH("api/usuarios/{id}/estado")
    suspend fun updateUserStatus(
        @Path("id") id: Long,
        @Query("activo") activo: Boolean,
        @Query("adminId") adminId: Long? = null
    ): UsuarioResponse

    @PATCH("api/usuarios/{id}/rol")
    suspend fun updateUserRole(
        @Path("id") id: Long,
        @Query("nuevoRol") nuevoRol: String,
        @Query("adminId") adminId: Long? = null
    ): UsuarioResponse

    @PATCH("api/usuarios/{id}/password")
    suspend fun updatePassword(
        @Path("id") id: Long,
        @Body request: PasswordUpdateDTO
    )

    // =========================================================================
    // 3. CATEGORÍAS DE INCIDENCIA
    // =========================================================================
    @GET("api/categorias")
    suspend fun getCategories(@Query("soloActivas") soloActivas: Boolean = false): List<CategoriaIncidenciaDTO>

    @POST("api/categorias")
    suspend fun createCategory(@Body request: CategoriaCreateDTO): CategoriaIncidenciaDTO

    @PUT("api/categorias/{id}")
    suspend fun updateCategory(
        @Path("id") id: Int,
        @Body request: CategoriaCreateDTO
    ): CategoriaIncidenciaDTO

    @PATCH("api/categorias/{id}/estado")
    suspend fun updateCategoryStatus(
        @Path("id") id: Int,
        @Query("activa") activa: Boolean
    ): CategoriaIncidenciaDTO

    // =========================================================================
    // 4. REPORTES Y FLUJO DE ATENCIÓN
    // =========================================================================
    @POST("api/reportes")
    suspend fun createReport(@Body request: ReporteCreateDTO): ReporteResponse

    @GET("api/feed")
    suspend fun getFeed(
        @Query("categoriaId") categoriaId: Int? = null,
        @Query("estado") estado: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): PageReporteResponse

    @GET("api/reportes/mis-reportes")
    suspend fun getMyReports(
        @Query("ciudadanoId") ciudadanoId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): PageReporteResponse

    @GET("api/reportes/seguidos")
    suspend fun getFollowedReports(
        @Query("usuarioId") usuarioId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): PageReporteResponse

    @GET("api/reportes/bandeja")
    suspend fun getGestorInbox(
        @Query("estado") estado: String = "REGISTRADO",
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): PageReporteResponse

    @GET("api/reportes/vencidos")
    suspend fun getOverdueReports(): List<ReporteResponse>

    @GET("api/reportes/asignados")
    suspend fun getAssignedReports(@Query("operadorId") operadorId: Long): List<ReporteResponse>

    @GET("api/reportes/{id}")
    suspend fun getReportDetail(@Path("id") id: Long): ReporteResponse

    @PUT("api/reportes/{id}")
    suspend fun updateOwnReport(
        @Path("id") id: Long,
        @Query("ciudadanoId") ciudadanoId: Long,
        @Body request: ReporteUpdateDTO
    ): ReporteResponse

    @PATCH("api/reportes/{id}/cancelar")
    suspend fun cancelOwnReport(
        @Path("id") id: Long,
        @Query("ciudadanoId") ciudadanoId: Long,
        @Query("motivo") motivo: String? = "Cancelado por el ciudadano"
    ): ReporteResponse

    @PATCH("api/reportes/{id}/validar")
    suspend fun validateReport(
        @Path("id") id: Long,
        @Query("gestorId") gestorId: Long,
        @Query("admitir") admitir: Boolean,
        @Query("motivo") motivo: String? = null
    ): ReporteResponse

    @POST("api/reportes/{id}/asignar")
    suspend fun assignOperator(
        @Path("id") id: Long,
        @Body request: AsignacionRequestDTO
    ): ReporteResponse

    @PATCH("api/reportes/{id}/iniciar-atencion")
    suspend fun startAttention(
        @Path("id") id: Long,
        @Query("operadorId") operadorId: Long,
        @Query("observacion") observacion: String? = "Inicio de atención"
    ): ReporteResponse

    @POST("api/reportes/{id}/avances")
    suspend fun registerProgress(
        @Path("id") id: Long,
        @Query("operadorId") operadorId: Long,
        @Query("observacion") observacion: String
    ): ReporteResponse

    @PATCH("api/reportes/{id}/cerrar")
    suspend fun closeReport(
        @Path("id") id: Long,
        @Query("operadorId") operadorId: Long,
        @Query("observacion") observacion: String,
        @Query("nombreArchivoEvidencia") nombreArchivoEvidencia: String = "evidencia_cierre.jpg"
    ): ReporteResponse

    @PATCH("api/reportes/{id}/confirmar")
    suspend fun confirmReportResult(
        @Path("id") id: Long,
        @Body request: ConfirmacionReporteDTO
    ): ReporteResponse

    @PATCH("api/reportes/{id}/reabrir")
    suspend fun reopenReport(
        @Path("id") id: Long,
        @Query("ciudadanoId") ciudadanoId: Long,
        @Query("motivo") motivo: String
    ): ReporteResponse

    @POST("api/reportes/{id}/apoyar")
    suspend fun supportReport(
        @Path("id") id: Long,
        @Query("usuarioId") usuarioId: Long
    )

    @DELETE("api/reportes/{id}/apoyar")
    suspend fun unsupportReport(
        @Path("id") id: Long,
        @Query("usuarioId") usuarioId: Long
    )

    @POST("api/reportes/{id}/seguir")
    suspend fun followReport(
        @Path("id") id: Long,
        @Query("usuarioId") usuarioId: Long
    )

    @DELETE("api/reportes/{id}/seguir")
    suspend fun unfollowReport(
        @Path("id") id: Long,
        @Query("usuarioId") usuarioId: Long
    )

    @PATCH("api/reportes/{id}/seguir/notificaciones")
    suspend fun configureFollowNotifications(
        @Path("id") id: Long,
        @Query("usuarioId") usuarioId: Long,
        @Query("activo") activo: Boolean
    )

    // =========================================================================
    // 5. HISTORIAL, COMENTARIOS, NOTIFICACIONES E INDICADORES
    // =========================================================================
    @GET("api/reportes/{id}/historial")
    suspend fun getReportTimeline(@Path("id") id: Long): List<SeguimientoResponse>

    @GET("api/reportes/{id}/comentarios")
    suspend fun getReportComments(
        @Path("id") id: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): PageComentarioResponse

    @POST("api/reportes/{id}/comentarios")
    suspend fun createComment(
        @Path("id") id: Long,
        @Body request: ComentarioRequestDTO
    ): ComentarioResponse

    @PATCH("api/comentarios/{id}")
    suspend fun editComment(
        @Path("id") id: Long,
        @Query("autorId") autorId: Long,
        @Body nuevoContenido: String
    ): ComentarioResponse

    @DELETE("api/comentarios/{id}")
    suspend fun deleteComment(
        @Path("id") id: Long,
        @Query("autorId") autorId: Long
    )

    @PATCH("api/comentarios/{id}/moderar")
    suspend fun moderateComment(
        @Path("id") id: Long,
        @Query("moderadorId") moderadorId: Long,
        @Query("motivo") motivo: String
    )

    @GET("api/notificaciones")
    suspend fun getNotifications(
        @Query("usuarioId") usuarioId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): PageNotificacionResponse

    @PATCH("api/notificaciones/{id}/leida")
    suspend fun markNotificationRead(@Path("id") id: Long)

    @GET("api/indicadores")
    suspend fun getIndicators(): Map<String, Any>
}
