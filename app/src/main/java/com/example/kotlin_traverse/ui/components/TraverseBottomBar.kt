package com.example.kotlin_traverse.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.kotlin_traverse.ui.navigation.HomeSection

@Composable
fun TraverseBottomBar(
    sections: List<HomeSection>,
    current: HomeSection,
    onSelect: (HomeSection) -> Unit
) {
    NavigationBar {
        sections.forEach { section ->
            val icon = when (section) {
                HomeSection.Dashboard -> Icons.Default.SpaceDashboard
                HomeSection.Problems -> Icons.AutoMirrored.Filled.ListAlt
                HomeSection.Integrations -> Icons.Outlined.Code
                HomeSection.Friends -> Icons.Default.Group
                HomeSection.Admin -> Icons.Default.AdminPanelSettings
            }
            NavigationBarItem(
                selected = section == current,
                onClick = { onSelect(section) },
                icon = { androidx.compose.material3.Icon(imageVector = icon, contentDescription = section.label) },
                label = { Text(section.label) }
            )
        }
    }
}
