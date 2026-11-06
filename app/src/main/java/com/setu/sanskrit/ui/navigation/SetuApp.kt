package com.setu.sanskrit.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.setu.sanskrit.AppContainer
import com.setu.sanskrit.ui.history.HistoryScreen
import com.setu.sanskrit.ui.history.HistoryViewModel
import com.setu.sanskrit.ui.translate.TranslateScreen
import com.setu.sanskrit.ui.translate.TranslateViewModel

/** To add a screen: add an entry here, a `composable(...)` below, and a screen file in ui/. */
private enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    Translate("translate", "Translate", Icons.Filled.Translate),
    History("history", "History", Icons.Filled.History),
}

@Composable
fun SetuApp(container: AppContainer) {
    val nav = rememberNavController()
    val current by nav.currentBackStackEntryAsState()

    // Created here so both tabs share one Translate state (History opens a result in Translate).
    val translateVm: TranslateViewModel = viewModel(factory = TranslateViewModel.Factory(container))
    val historyVm: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory(container))

    Scaffold(
        contentWindowInsets = WindowInsets(0), // headers draw behind the status bar themselves
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = current?.destination?.route == tab.route,
                        onClick = {
                            nav.navigate(tab.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(nav, startDestination = Tab.Translate.route, modifier = Modifier.padding(padding)) {
            composable(Tab.Translate.route) { TranslateScreen(translateVm) }
            composable(Tab.History.route) {
                HistoryScreen(historyVm, onOpen = { result ->
                    translateVm.showResult(result)
                    nav.navigate(Tab.Translate.route) { launchSingleTop = true }
                })
            }
        }
    }
}
