package com.example.rssreader.app

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.rssreader.feature.articles.ArticlesScreen
import com.example.rssreader.feature.articles.ArticlesViewModel
import com.example.rssreader.feature.auth.AuthViewModel
import com.example.rssreader.feature.auth.LoginScreen
import com.example.rssreader.feature.auth.ServerConfigScreen
import com.example.rssreader.feature.feeds.FeedsScreen
import com.example.rssreader.feature.feeds.FeedsViewModel
import com.example.rssreader.feature.history.HistoryScreen
import com.example.rssreader.feature.history.HistoryViewModel
import com.example.rssreader.feature.settings.SettingsSheet
import com.example.rssreader.feature.settings.SettingsViewModel

private object Route {
    const val ServerConfig = "server-config"
    const val Login = "login"
    const val Home = "home"
    const val Articles = "articles/{feedId}/{feedTitle}"
}

private enum class HomeTab(val title: String) {
    FEEDS("订阅"),
    HISTORY("总结历史"),
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AppNavHost(
    container: AppContainer,
    authViewModel: AuthViewModel,
    pendingDeepLink: Uri?,
    onDeepLinkConsumed: () -> Unit,
) {
    val navController = rememberNavController()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(authState.isLoggedIn, authState.baseUrlSaved) {
        when {
            authState.isLoggedIn -> navController.navigate(Route.Home) {
                popUpTo(navController.graph.id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
            !authState.baseUrlSaved -> navController.navigate(Route.ServerConfig) {
                popUpTo(navController.graph.id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
            else -> navController.navigate(Route.Login) {
                popUpTo(navController.graph.id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(pendingDeepLink) {
        val uri = pendingDeepLink ?: return@LaunchedEffect
        if (uri.scheme == "rssreader" && uri.host == "auth") {
            authViewModel.handleFeishuCallback(uri)
            onDeepLinkConsumed()
        }
    }

    NavHost(navController = navController, startDestination = Route.ServerConfig) {
        composable(Route.ServerConfig) {
            ServerConfigScreen(
                currentUrl = authState.baseUrl,
                loading = authState.isLoading,
                error = authState.error,
                onSave = authViewModel::saveBaseUrl,
                onErrorConsumed = authViewModel::clearError,
            )
        }
        composable(Route.Login) {
            val context = LocalContext.current
            LoginScreen(
                loading = authState.isLoading,
                error = authState.error,
                onLogin = authViewModel::login,
                onFeishuLogin = {
                    authViewModel.beginFeishuLogin { url ->
                        CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(url))
                    }
                },
                onErrorConsumed = authViewModel::clearError,
            )
        }
        composable(Route.Home) {
            HomeScreen(
                container = container,
                onOpenArticles = { feedId, feedTitle ->
                    navController.navigate("articles/$feedId/${Uri.encode(feedTitle)}")
                },
                onLogout = {
                    navController.navigate(Route.Login) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(
            route = Route.Articles,
            arguments = listOf(
                navArgument("feedId") { type = NavType.LongType },
                navArgument("feedTitle") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val feedId = backStackEntry.arguments?.getLong("feedId") ?: 0L
            val feedTitle = backStackEntry.arguments?.getString("feedTitle")?.let(Uri::decode).orEmpty()
            val vm: ArticlesViewModel = viewModel(factory = ContainerViewModelFactory { ArticlesViewModel(container, feedId) })

            Scaffold(topBar = { TopAppBar(title = { Text(feedTitle) }) }) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    ArticlesScreen(viewModel = vm)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    container: AppContainer,
    onOpenArticles: (Long, String) -> Unit,
    onLogout: () -> Unit,
) {
    var currentTab by remember { mutableStateOf(HomeTab.FEEDS) }
    var showSettings by remember { mutableStateOf(false) }

    val feedsVm: FeedsViewModel = viewModel(factory = ContainerViewModelFactory { FeedsViewModel(container) })
    val historyVm: HistoryViewModel = viewModel(factory = ContainerViewModelFactory { HistoryViewModel(container) })
    val settingsVm: SettingsViewModel = viewModel(factory = ContainerViewModelFactory { SettingsViewModel(container) })

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "RSS Reader · ${currentTab.title}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == HomeTab.FEEDS,
                    onClick = { currentTab = HomeTab.FEEDS },
                    icon = { Icon(Icons.Default.Article, contentDescription = "订阅") },
                    label = { Text("订阅") },
                )
                NavigationBarItem(
                    selected = currentTab == HomeTab.HISTORY,
                    onClick = { currentTab = HomeTab.HISTORY },
                    icon = { Icon(Icons.Default.History, contentDescription = "总结历史") },
                    label = { Text("总结") },
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentTab) {
                HomeTab.FEEDS -> FeedsScreen(viewModel = feedsVm, onFeedClick = onOpenArticles)
                HomeTab.HISTORY -> HistoryScreen(viewModel = historyVm)
            }
        }
    }

    if (showSettings) {
        ModalBottomSheet(onDismissRequest = { showSettings = false }) {
            SettingsSheet(viewModel = settingsVm) {
                showSettings = false
                onLogout()
            }
        }
    }
}
