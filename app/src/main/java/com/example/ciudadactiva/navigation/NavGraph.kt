package com.example.ciudadactiva.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ciudadactiva.ui.screens.*

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object MapList : Screen("map_list")
    object CreateReport : Screen("create_report")
    object ReportDetail : Screen("report_detail")
    object MyReports : Screen("my_reports")
    object ManagementInbox : Screen("management_inbox")
    object DashboardStats : Screen("dashboard_stats")
}

@Composable
fun CiudadActivaNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.MapList.route) { MapListScreen(navController) }
        composable(Screen.CreateReport.route) { CreateReportScreen(navController) }
        composable(Screen.ReportDetail.route) { ReportDetailScreen(navController) }
        composable(Screen.MyReports.route) { MyReportsScreen(navController) }
        composable(Screen.ManagementInbox.route) { ManagementInboxScreen(navController) }
        composable(Screen.DashboardStats.route) { DashboardStatsScreen(navController) }
    }
}
