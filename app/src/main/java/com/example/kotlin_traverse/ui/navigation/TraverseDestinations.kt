package com.example.kotlin_traverse.ui.navigation

sealed class TraverseDestination(val route: String) {
    data object Auth : TraverseDestination("auth")
    data object Home : TraverseDestination("home")
}

enum class HomeSection(val route: String, val label: String) {
    Dashboard("dashboard", "Dashboard"),
    Problems("problems", "Problems"),
    Integrations("integrations", "Integrations"),
    Friends("friends", "Friends"),
    Admin("admin", "Admin")
}
