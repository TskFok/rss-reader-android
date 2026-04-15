package com.example.rssreader.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.rssreader.core.ui.LoadingOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerConfigScreen(
    currentUrl: String,
    loading: Boolean,
    error: String?,
    onSave: (String) -> Unit,
    onErrorConsumed: () -> Unit,
) {
    var input by remember(currentUrl) { mutableStateOf(currentUrl) }
    val snackBarState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        if (!error.isNullOrBlank()) {
            snackBarState.showSnackbar(error)
            onErrorConsumed()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("配置服务地址") }) },
        snackbarHost = { SnackbarHost(snackBarState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("输入网关地址，应用会自动补全 /api", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = input,
                        onValueChange = { input = it },
                        label = { Text("服务端地址") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                    )
                    Button(onClick = { onSave(input) }, modifier = Modifier.fillMaxWidth()) {
                        Text("保存并继续")
                    }
                }
            }
        }
        if (loading) LoadingOverlay()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    loading: Boolean,
    error: String?,
    onLogin: (String, String) -> Unit,
    onFeishuLogin: () -> Unit,
    onErrorConsumed: () -> Unit,
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val snackBarState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        if (!error.isNullOrBlank()) {
            snackBarState.showSnackbar(error)
            onErrorConsumed()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("登录") }) },
        snackbarHost = { SnackbarHost(snackBarState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("用户名") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("密码") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = null) },
                    )
                    Button(
                        onClick = { onLogin(username, password) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = username.isNotBlank() && password.length >= 6,
                    ) {
                        Text("账号密码登录")
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = onFeishuLogin, modifier = Modifier.fillMaxWidth()) {
                            Text("飞书快速登录")
                        }
                    }
                }
            }
        }
        if (loading) LoadingOverlay()
    }
}
