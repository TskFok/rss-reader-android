package com.example.rssreader.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rssreader.core.ui.RssReaderTheme
import com.example.rssreader.feature.auth.AuthViewModel

class MainActivity : ComponentActivity() {
    private val authViewModel by viewModels<AuthViewModel> {
        val app = application as RssReaderApp
        ContainerViewModelFactory { AuthViewModel(app.container) }
    }

    private var pendingDeepLink: Uri? by mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingDeepLink = intent?.data

        setContent {
            val app = application as RssReaderApp
            val settings by app.container.settingsRepository.settingsFlow.collectAsStateWithLifecycle(
                initialValue = com.example.rssreader.core.datastore.AppSettings(),
            )

            RssReaderTheme(dynamicColor = settings.dynamicColorEnabled) {
                AppNavHost(
                    container = app.container,
                    authViewModel = authViewModel,
                    pendingDeepLink = pendingDeepLink,
                    onDeepLinkConsumed = { pendingDeepLink = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingDeepLink = intent.data
    }
}
