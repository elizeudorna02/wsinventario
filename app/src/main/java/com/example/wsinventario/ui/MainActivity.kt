
package com.example.wsinventario.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wsinventario.data.file.FileHandler
import com.example.wsinventario.settings.SettingsActivity
import com.example.wsinventario.viewmodel.CadastroViewModel
import com.example.wsinventario.viewmodel.CadastroViewModelFactory
import com.example.wsinventario.viewmodel.InventarioViewModel
import com.example.wsinventario.viewmodel.InventarioViewModelFactory
import com.example.wsinventario.ui.theme.WsinventarioTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WsinventarioTheme {
                val inventarioViewModel: InventarioViewModel = viewModel(factory = InventarioViewModelFactory(application))
                val cadastroViewModel: CadastroViewModel = viewModel(factory = CadastroViewModelFactory(application))

                val cadastroSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val optionsSheetState = rememberModalBottomSheetState()
                val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val catalogoSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                val scope = rememberCoroutineScope()
                var showCadastroSheet by remember { mutableStateOf(false) }
                var showEditSheet by remember { mutableStateOf(false) }
                var showCatalogoSheet by remember { mutableStateOf(false) }
                var showConfirmationDialog by remember { mutableStateOf(false) }

                val context = LocalContext.current
                var contentToSave by remember { mutableStateOf<String?>(null) }

                val settingsLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { 
                    inventarioViewModel.refreshData()
                }

                val scannerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        val barcode = result.data?.getStringExtra("BARCODE_RESULT")
                        if (barcode != null) {
                            cadastroViewModel.clearForm()
                            cadastroViewModel.onCodigoDeBarrasChanged(barcode)
                            showCadastroSheet = true
                        }
                    }
                }

                val openFileLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument(),
                    onResult = { uri: Uri? ->
                        uri?.let { inventarioViewModel.onImportFileSelected(it) }
                    }
                )

                val createFileLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument("text/csv"),
                    onResult = { uri: Uri? ->
                        uri?.let {
                            contentToSave?.let { content ->
                                context.contentResolver.openOutputStream(it)?.use {
                                    FileHandler().writeContentToStream(content, it)
                                }
                                contentToSave = null
                            }
                        }
                    }
                )
                
                val onSaveSuccess: () -> Unit = {
                    scope.launch {
                        if (showCadastroSheet) {
                            cadastroSheetState.hide()
                            showCadastroSheet = false
                        }
                        if (showEditSheet) {
                            editSheetState.hide()
                             showEditSheet = false
                        }
                        if (showCatalogoSheet) {
                            catalogoSheetState.hide()
                            showCatalogoSheet = false
                        } 
                        inventarioViewModel.onProductSaveSuccess()
                        Toast.makeText(context, "Operação bem-sucedida!", Toast.LENGTH_SHORT).show()
                    }
                }

                LaunchedEffect(Unit) {
                    inventarioViewModel.uiEvents.collectLatest {
                        when (it) {
                            is InventarioViewModel.UiEvent.ShowToast -> {
                                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                            }
                            is InventarioViewModel.UiEvent.SaveFile -> {
                                contentToSave = it.content
                                createFileLauncher.launch(it.fileName)
                            }
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    cadastroViewModel.uiEvents.collectLatest {
                        when (it) {
                            is CadastroViewModel.UiEvent.ShowToast -> {
                                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                            }
                            is CadastroViewModel.UiEvent.ShowConfirmationDialog -> {
                                showConfirmationDialog = true
                            }
                        }
                    }
                }

                if (showConfirmationDialog) {
                    AlertDialog(
                        onDismissRequest = { showConfirmationDialog = false },
                        title = { Text("Produto não cadastrado") },
                        text = { Text("Deseja cadastrar este novo produto?") },
                        confirmButton = {
                            TextButton(onClick = { 
                                showConfirmationDialog = false
                                cadastroViewModel.confirmAndCreateProduct(onSuccess = onSaveSuccess, onFailure = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                })
                            }) {
                                Text("SIM")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showConfirmationDialog = false }) {
                                Text("NÃO")
                            }
                        }
                    )
                }

                InventarioScreen(
                    viewModel = inventarioViewModel,
                    onAddContagemClick = {
                        cadastroViewModel.clearForm()
                        showCadastroSheet = true
                    },
                    onAddProdutoClick = {
                        context.startActivity(Intent(context, NovoProdutoActivity::class.java))
                    },
                    onProductClick = { product ->
                        inventarioViewModel.onProductClicked(product)
                    },
                    onContagemClick = { item ->
                        cadastroViewModel.loadContagemItem(item)
                        showCadastroSheet = true
                    },
                    onScanClick = {
                        scannerLauncher.launch(Intent(context, ScannerActivity::class.java))
                    },
                    onImportClick = { 
                        openFileLauncher.launch(arrayOf("text/csv", "text/plain"))
                    },
                    onExportClick = { inventarioViewModel.onExportClicked() },
                    onSettingsClick = { 
                        settingsLauncher.launch(Intent(context, SettingsActivity::class.java))
                    }
                )

                if (showCadastroSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showCadastroSheet = false },
                        sheetState = cadastroSheetState
                    ) {
                        CadastroSheetContent(
                            viewModel = cadastroViewModel,
                            onCancel = {
                                scope.launch {
                                    cadastroSheetState.hide()
                                    showCadastroSheet = false
                                    cadastroViewModel.clearForm()
                                }
                            },
                            onSave = {
                                cadastroViewModel.onSaveContagemClicked(onSuccess = onSaveSuccess, onFailure = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                })
                            }
                        )
                    }
                }

                if (showCatalogoSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showCatalogoSheet = false },
                        sheetState = catalogoSheetState
                    ) {
                        NovoProdutoSheetContent(
                            viewModel = cadastroViewModel,
                            onCancel = {
                                scope.launch {
                                    catalogoSheetState.hide()
                                    showCatalogoSheet = false
                                    cadastroViewModel.clearForm()
                                }
                            },
                            onSave = {
                                cadastroViewModel.createProdutoNoCatalogo(onSuccess = onSaveSuccess, onFailure = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                })
                            }
                        )
                    }
                }

                if (inventarioViewModel.showProductOptions) {
                    ModalBottomSheet(
                        onDismissRequest = { inventarioViewModel.onDismissProductOptions() },
                        sheetState = optionsSheetState
                    ) {
                        ProductOptionsSheetContent(
                            onNovaContagem = {
                                scope.launch {
                                    optionsSheetState.hide()
                                    inventarioViewModel.selectedProduct?.let { cadastroViewModel.loadProduct(it) }
                                    inventarioViewModel.onDismissProductOptions()
                                    showCadastroSheet = true
                                }
                            },
                            onEditarCadastro = {
                                scope.launch {
                                    optionsSheetState.hide()
                                    inventarioViewModel.selectedProduct?.let { cadastroViewModel.loadProduct(it) }
                                    inventarioViewModel.onDismissProductOptions()
                                    showEditSheet = true
                                }
                            }
                        )
                    }
                }

                if (showEditSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showEditSheet = false },
                        sheetState = editSheetState
                    ) {
                        EditarProdutoSheetContent(
                            viewModel = cadastroViewModel,
                            onCancel = {
                                scope.launch {
                                    editSheetState.hide()
                                    showEditSheet = false
                                    cadastroViewModel.clearForm()
                                }
                            },
                            onSave = {
                                cadastroViewModel.updateProduct(onSuccess = onSaveSuccess, onFailure = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                })
                            }
                        )
                    }
                }
            }
        }
    }
}
