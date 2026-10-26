package com.example.ciudadactiva.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ciudadactiva.navigation.Screen

@Composable
fun LoginScreen(navController: NavController) {
    ScreenMockup(title = "Inicio de sesión y perfil", navController = navController, nextRoute = Screen.MapList.route)
}

@Composable
fun MapListScreen(navController: NavController) {
    ScreenMockup(title = "Mapa o lista de reportes con filtros", navController = navController, nextRoute = Screen.CreateReport.route)
}

@Composable
fun CreateReportScreen(navController: NavController) {
    ScreenMockup(title = "Crear reporte y adjuntar evidencia", navController = navController, nextRoute = Screen.ReportDetail.route)
}

@Composable
fun ReportDetailScreen(navController: NavController) {
    ScreenMockup(title = "Detalle y línea de tiempo", navController = navController, nextRoute = Screen.MyReports.route)
}

@Composable
fun MyReportsScreen(navController: NavController) {
    ScreenMockup(title = "Mis reportes", navController = navController, nextRoute = Screen.ManagementInbox.route)
}

@Composable
fun ManagementInboxScreen(navController: NavController) {
    ScreenMockup(title = "Bandeja de gestión y asignación", navController = navController, nextRoute = Screen.DashboardStats.route)
}

@Composable
fun DashboardStatsScreen(navController: NavController) {
    ScreenMockup(title = "Atención, cierre y panel estadístico", navController = navController, nextRoute = Screen.Login.route)
}

@Composable
fun ScreenMockup(title: String, navController: NavController, nextRoute: String) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = { navController.navigate(nextRoute) }) {
                Text("Ir a la siguiente pantalla")
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = { navController.popBackStack() }) {
                Text("Volver")
            }
        }
    }
}
