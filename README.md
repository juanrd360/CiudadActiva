# 🏙️ CiudadActiva — App Móvil Android (Colombia)

Aplicación móvil nativa para Android desarrollada en **Kotlin + Jetpack Compose** para el reporte ciudadano, gestión municipal, seguimiento en tiempo real y visualización sobre el **mapa interactivo de Colombia**, conectada en vivo con el backend en **Render (Spring Boot + PostgreSQL)**.

---

## 📲 Descarga Directa del APK (Instalador Android)

Haz clic en el botón o en el enlace de abajo para **iniciar la descarga directa del archivo `.apk`** e instalarlo en tu teléfono Android o emulador:

[![Descargar APK CiudadActiva](https://img.shields.io/badge/%E2%AC%87%EF%B8%8F_Descargar_APK_Directo-CiudadActiva.apk-009688?style=for-the-badge&logo=android&logoColor=white)](https://github.com/juanrd360/CiudadActiva/raw/master/apk/CiudadActiva.apk)

🔗 **Link directo de descarga (1 clic):**  
👉 **[https://github.com/juanrd360/CiudadActiva/raw/master/apk/CiudadActiva.apk](https://github.com/juanrd360/CiudadActiva/raw/master/apk/CiudadActiva.apk)**

---

## ✨ Características Principales

- 🗺️ **Mapa Real e Interactivo de Colombia (`osmdroid` + OpenStreetMap HOT)**:
  - Visualización de todos los reportes ciudadanos sobre el mapa real de Colombia (`Reportes · Vista Mapa`).
  - Ubicación exacta a nivel de calle, carrera y localidad en el detalle de cada reporte (`Detalle del reporte`).
  - Selección de punto exacto tocando directamente el mapa de Colombia o mediante GPS en tiempo real (`Crear / Editar reporte`).
- 🔐 **Inicio de Sesión Tradicional y con Google (`Continuar con Google`)**:
  - Autenticación con JWT contra el servidor en Render (`/api/auth/login` y `/api/auth/registro`).
  - Inicio de sesión con cuenta de Google sincronizado automáticamente con el backend sin requerir configuración manual.
- 👥 **Flujo Completo por Roles (`Ciudadano`, `Gestor`, `Operador`, `Administrador`)**:
  - **Ciudadano**: Crear reportes con evidencia fotográfica (cámara/galería) y GPS, editar/cancelar reportes propios, apoyar y seguir reportes, comentar y calificar/confirmar cierres o solicitar reapertura.
  - **Gestor**: Bandeja de gestión, admisión/rechazo de reportes, asignación y reasignación de operadores con control de carga y prioridad oficial, tablero de indicadores.
  - **Operador**: Atención de reportes asignados, registro de avances en la línea de tiempo y cierre con evidencia.
  - **Administrador**: Gestión de categorías, administración de usuarios internos y moderación de comentarios.

---

## 🌐 Backend Conectado en Vivo

- **API Base URL (Render)**: `https://ciudadactiva-wo1c.onrender.com/`
- **Documentación Swagger UI**: [https://ciudadactiva-wo1c.onrender.com/swagger-ui/index.html](https://ciudadactiva-wo1c.onrender.com/swagger-ui/index.html)

### Credenciales de Prueba Disponibles

| Rol | Correo | Contraseña |
| :--- | :--- | :--- |
| **Ciudadano** | `prueba.android.6986@ciudadactiva.com` | `Prueba1234` |
| **Google Sign-In** | Botón **"Continuar con Google"** en la pantalla de inicio | *(Automático)* |

---

## 🛠️ Tecnologías Utilizadas

- **Lenguaje**: Kotlin (JVM 11, Android SDK 37, Min SDK 24)
- **UI**: Jetpack Compose + Material Design 3
- **Mapas**: `org.osmdroid:osmdroid-android:6.1.18` (OpenStreetMap Colombia)
- **Red**: Retrofit 2 + OkHttp 4 + Gson (Interceptor JWT `Bearer`)
- **Ubicación y Auth**: Google Play Services Location & Auth (`21.2.0`)
- **Imágenes**: Coil Compose (`coil-compose`)
