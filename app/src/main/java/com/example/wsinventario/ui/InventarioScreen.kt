package com.example.wsinventario.ui

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wsinventario.data.ContagemItem
import com.example.wsinventario.data.Produto
import com.example.wsinventario.viewmodel.InventarioViewModel
import com.example.wsinventario.ui.theme.WsinventarioTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioScreen(
    viewModel: InventarioViewModel,
    onAddContagemClick: () -> Unit,
    onAddProdutoClick: () -> Unit,
    onProductClick: (Produto) -> Unit,
    onContagemClick: (ContagemItem) -> Unit,
    onScanClick: () -> Unit,
    onImportClick: () -> Unit,
    onExportClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var isSearchActive by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contestoque") },
                actions = {
                    IconButton(onClick = { isSearchActive = !isSearchActive }) {
                        Icon(Icons.Default.Search, contentDescription = "Pesquisar")
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Mais opções")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Importar") },
                                onClick = onImportClick
                            )
                            DropdownMenuItem(
                                text = { Text("Exportar") },
                                onClick = onExportClick
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Configurações") },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                onClick = onSettingsClick
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            when (viewModel.selectedTabIndex) {
                0 -> {
                     Column(horizontalAlignment = Alignment.End) {
                        FloatingActionButton(
                            onClick = onScanClick,
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(Icons.Outlined.Inventory2, contentDescription = "Scanner")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        FloatingActionButton(onClick = onAddContagemClick) {
                            Icon(Icons.Default.Add, contentDescription = "Adicionar Contagem")
                        }
                    }
                }
                1 -> {
                    FloatingActionButton(onClick = onAddProdutoClick) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar Produto ao Catálogo")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (isSearchActive) {
                TextField(
                    value = viewModel.searchText,
                    onValueChange = viewModel::onSearchTextChanged,
                    label = { Text("Pesquisar produto...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            PrimaryTabRow(selectedTabIndex = viewModel.selectedTabIndex) {
                Tab(selected = viewModel.selectedTabIndex == 0, onClick = { viewModel.onTabSelected(0) }, text = { Text("CONTAGENS") })
                Tab(selected = viewModel.selectedTabIndex == 1, onClick = { viewModel.onTabSelected(1) }, text = { Text("PRODUTOS") })
            }

            when (viewModel.selectedTabIndex) {
                0 -> ContagensTab(viewModel.contagemProductsCount, viewModel.contagemTotalQuantity, viewModel.contagemList, onContagemClick)
                1 -> ProdutosTab(viewModel.productCatalogList, onProductClick)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InventarioScreenPreview() {
    WsinventarioTheme {
        val context = LocalContext.current
        val viewModel = InventarioViewModel(context.applicationContext as Application)
        InventarioScreen(viewModel = viewModel, onAddContagemClick = {}, onAddProdutoClick = {}, onProductClick = {}, onContagemClick = {}, onScanClick = {}, onImportClick = {}, onExportClick = {}, onSettingsClick = {})
    }
}
