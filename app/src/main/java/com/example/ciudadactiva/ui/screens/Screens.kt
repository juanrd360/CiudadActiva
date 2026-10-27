package com.example.ciudadactiva.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.ciudadactiva.ui.theme.CiudadActivaTheme

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "01 Splash")
@Composable
fun PreviewSplash() {
    CiudadActivaTheme {
        SplashScreen(rememberNavController())
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "02 Login")
@Composable
fun PreviewLogin() {
    CiudadActivaTheme {
        LoginScreen(rememberNavController())
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "06 Registro")
@Composable
fun PreviewRegister() {
    CiudadActivaTheme {
        RegisterScreen(rememberNavController())
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "09 Perfil")
@Composable
fun PreviewProfile() {
    CiudadActivaTheme {
        ProfileScreen(rememberNavController())
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Reportes Comunidad")
@Composable
fun PreviewCitizenReports() {
    CiudadActivaTheme {
        CitizenReportsScreen(rememberNavController())
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Detalle Reporte")
@Composable
fun PreviewReportDetail() {
    CiudadActivaTheme {
        ReportDetailScreen(rememberNavController(), reportId = "rep-0012")
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Gestor Indicadores")
@Composable
fun PreviewIndicators() {
    CiudadActivaTheme {
        IndicatorsScreen(rememberNavController())
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Operador Asignados")
@Composable
fun PreviewOperadorAssigned() {
    CiudadActivaTheme {
        OperadorAssignedScreen(rememberNavController())
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Admin Categorías")
@Composable
fun PreviewAdminCategories() {
    CiudadActivaTheme {
        AdminCategoriesScreen(rememberNavController())
    }
}
