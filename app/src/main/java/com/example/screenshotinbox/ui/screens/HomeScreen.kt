package com.example.screenshotinbox.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.screenshotinbox.data.local.ScreenshotEntity
import com.example.screenshotinbox.domain.model.SortOrder
import com.example.screenshotinbox.ui.components.CategoryFilterRow
import com.example.screenshotinbox.ui.components.EmptyInboxState
import com.example.screenshotinbox.ui.components.EmptySearchState
import com.example.screenshotinbox.ui.components.ScreenshotCard
import com.example.screenshotinbox.ui.components.ScreenshotSearchBar
import com.example.screenshotinbox.viewmodel.HomeViewModel
import com.example.screenshotinbox.viewmodel.ViewModelFactory

private fun queryFileName(context: android.content.Context, uri: android.net.Uri): String {
    return runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
        }
    }.getOrNull() ?: uri.lastPathSegment ?: "screenshot"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    factory: ViewModelFactory,
    onOpenDetail: (Long) -> Unit,
    onOpenCategories: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val viewModel: HomeViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 50)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val resolved = uris.map { uri ->
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                uri.toString() to queryFileName(context, uri)
            }
            viewModel.importUris(resolved)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.lastDeleted) {
        uiState.lastDeleted?.let { deleted ->
            val result = snackbarHostState.showSnackbar(
                message = "Screenshot deleted",
                actionLabel = "Undo"
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            } else {
                viewModel.clearLastDeleted()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Screenshot Inbox") },
                actions = {
                    IconButton(onClick = { sortMenuExpanded = true }) {
                        Icon(Icons.Filled.SortByAlpha, contentDescription = "Sort")
                    }
                    DropdownMenu(expanded = sortMenuExpanded, onDismissRequest = { sortMenuExpanded = false }) {
                        DropdownMenuItem(text = { Text("Newest first") }, onClick = {
                            viewModel.onSortOrderChange(SortOrder.NEWEST); sortMenuExpanded = false
                        })
                        DropdownMenuItem(text = { Text("Oldest first") }, onClick = {
                            viewModel.onSortOrderChange(SortOrder.OLDEST); sortMenuExpanded = false
                        })
                        DropdownMenuItem(text = { Text("Favorites first") }, onClick = {
                            viewModel.onSortOrderChange(SortOrder.FAVORITES); sortMenuExpanded = false
                        })
                        DropdownMenuItem(text = { Text("Manage categories") }, onClick = {
                            sortMenuExpanded = false; onOpenCategories()
                        })
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                pickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Screenshots")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                ScreenshotSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    modifier = Modifier.padding(top = 8.dp)
                )
                CategoryFilterRow(
                    categories = uiState.categories,
                    activeFilter = uiState.filter,
                    onFilterSelected = viewModel::onFilterChange,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            when {
                uiState.isEmpty -> EmptyInboxState(onAddClick = {
                    pickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                })
                uiState.isSearchEmptyResult -> EmptySearchState()
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.screenshots, key = { it.id }) { shot: ScreenshotEntity ->
                        ScreenshotCard(
                            screenshot = shot,
                            onClick = { onOpenDetail(shot.id) }
                        )
                    }
                }
            }
        }
    }
}
