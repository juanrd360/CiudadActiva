package com.example.ciudadactiva.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ciudadactiva.ui.screens.*

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Profile : Screen("profile")
    object CitizenReports : Screen("citizen_reports")
    object CreateReport : Screen("create_report")
    object EditReport : Screen("edit_report/{reportId}") {
        fun createRoute(reportId: String) = "edit_report/$reportId"
    }
    object MyReports : Screen("my_reports")
    object Notifications : Screen("notifications")
    object ReportDetail : Screen("report_detail/{reportId}") {
        fun createRoute(reportId: String) = "report_detail/$reportId"
    }
    object GestorInbox : Screen("gestor_inbox")
    object Indicators : Screen("indicators")
    object OperadorAssigned : Screen("operador_assigned")
    object OperadorProgressForm : Screen("operador_progress/{reportId}") {
        fun createRoute(reportId: String) = "operador_progress/$reportId"
    }
    object OperadorCloseReport : Screen("operador_close/{reportId}") {
        fun createRoute(reportId: String) = "operador_close/$reportId"
    }
    object AdminCategories : Screen("admin_categories")
    object AdminCategoryForm : Screen("admin_category_form")
    object AdminUsers : Screen("admin_users")
    object AdminUserForm : Screen("admin_user_form")
    object AdminReports : Screen("admin_reports")
}

@Composable
fun CiudadActivaNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }
        composable(Screen.CitizenReports.route) {
            CitizenReportsScreen(navController)
        }
        composable(Screen.CreateReport.route) {
            CreateOrEditReportScreen(navController, reportIdToEdit = null)
        }
        composable(
            route = Screen.EditReport.route,
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId") ?: "rep-0016"
            CreateOrEditReportScreen(navController, reportIdToEdit = reportId)
        }
        composable(Screen.MyReports.route) {
            MyReportsScreen(navController)
        }
        composable(Screen.Notifications.route) {
            NotificationsScreen(navController)
        }
        composable(
            route = Screen.ReportDetail.route,
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId") ?: "rep-0012"
            ReportDetailScreen(navController, reportId = reportId)
        }
        composable(Screen.GestorInbox.route) {
            GestorInboxScreen(navController)
        }
        composable(Screen.Indicators.route) {
            IndicatorsScreen(navController)
        }
        composable(Screen.OperadorAssigned.route) {
            OperadorAssignedScreen(navController)
        }
        composable(
            route = Screen.OperadorProgressForm.route,
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId") ?: "rep-0012"
            OperadorProgressFormScreen(navController, reportId = reportId)
        }
        composable(
            route = Screen.OperadorCloseReport.route,
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId") ?: "rep-0012"
            OperadorCloseReportScreen(navController, reportId = reportId)
        }
        composable(Screen.AdminCategories.route) {
            AdminCategoriesScreen(navController)
        }
        composable(Screen.AdminCategoryForm.route) {
            AdminCategoryFormScreen(navController)
        }
        composable(Screen.AdminUsers.route) {
            AdminUsersScreen(navController)
        }
        composable(Screen.AdminUserForm.route) {
            AdminUserFormScreen(navController)
        }
        composable(Screen.AdminReports.route) {
            AdminReportsScreen(navController)
        }
    }
}
