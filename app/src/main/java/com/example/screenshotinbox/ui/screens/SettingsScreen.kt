package com.example.screenshotinbox.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.screenshotinbox.data.local.ThemeMode
import com.example.screenshotinbox.viewmodel.SettingsViewModel
import com.example.screenshotinbox.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    factory: ViewModelFactory,
    onBack: () -> Unit,
    onManageCategories: () -> Unit
) {
    val viewModel: SettingsViewModel = viewModel(factory = factory)
    val themeMode by viewModel.themeMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            Text(
                "Theme",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
            )
            ThemeMode.entries.forEach { mode ->
                ListItem(
                    headlineContent = { Text(mode.label()) },
                    leadingContent = {
                        RadioButton(
                            selected = themeMode == mode,
                            onClick = { viewModel.setThemeMode(mode) }
                        )
                    },
                    modifier = Modifier.padding(0.dp)
                )
            }

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Manage Categories") },
                leadingContent = { Icon(Icons.Filled.Category, contentDescription = null) },
                modifier = Modifier.clickableRow(onManageCategories)
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("About") },
                leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) },
                supportingContent = {
                    Column {
                        Text("Screenshot Inbox", fontWeight = FontWeight.Medium)
                        Text("Version 1.0.0")
                        Text("Your screenshots, organized locally on your device.")
                        Text(
                            "Your data stays on your device.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    }
}

private fun ThemeMode.label(): String = when (this) {
    ThemeMode.SYSTEM -> "System default"
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
}

private fun Modifier.clickableRow(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
