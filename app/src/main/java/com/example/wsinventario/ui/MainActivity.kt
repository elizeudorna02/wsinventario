package com.example.wsinventario.ui

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wsinventario.data.Produto
import com.example.wsinventario.data.file.FileHandler
import com.example.wsinventario.settings.ApiSettingsActivity
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
                
                val scope = rememberCoroutineScope()
                var showCadastroSheet by remember { mutableStateOf(false) }

                val context = LocalContext.current
                var contentToSave by remember { mutableStateOf<String?>(null) }

                val settingsLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { inventarioViewModel.refreshData() }

                val apiSettingsLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { inventarioViewModel.refreshData() }

                val novoProdutoLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        inventarioViewModel.refreshData()
                    } else if (result.resultCode == NovoProdutoActivity.RESULT_START_CONTAGEM) {
                        val ean = result.data?.getStringExtra(NovoProdutoActivity.EXTRA_EAN)
                        if (ean != null) {
                            inventarioViewModel.findAndStartContagem(ean)
                        }
                    }
                }

                val scannerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        val barcode = result.data?.getStringExtra("BARCODE_RESULT")
                        if (barcode != null) {
                            cadastroViewModel.clearForm()
                            cadastroViewModel.onEanChanged(barcode)
                            showCadastroSheet = true
                        }
                    }
                }
                
                val cameraPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->
                    if (isGranted) {
                        scannerLauncher.launch(Intent(context, ScannerActivity::class.java))
                    } else {
                        Toast.makeText(context, "Permissão da câmera negada", Toast.LENGTH_SHORT).show()
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
                            is InventarioViewModel.UiEvent.StartContagemForProduct -> {
                                cadastroViewModel.loadProdutoParaContagem(it.product)
                                showCadastroSheet = true
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
                             is CadastroViewModel.UiEvent.ShowProductExistsDialog -> { 
                                // This is handled in NovoProdutoActivity
                            }
                            is CadastroViewModel.UiEvent.CadastroSuccess -> {
                                // This is handled in NovoProdutoActivity
                            }
                        }
                    }
                }

                InventarioScreen(
                    viewModel = inventarioViewModel,
                    onAddContagemClick = {
                        cadastroViewModel.clearForm()
                        showCadastroSheet = true
                    },
                    onAddProdutoClick = {
                        novoProdutoLauncher.launch(Intent(context, NovoProdutoActivity::class.java))
                    },
                    onProductClick = { product ->
                        inventarioViewModel.onProductClicked(product)
                    },
                    onContagemClick = { produto ->
                        cadastroViewModel.loadProdutoParaContagem(produto)
                        showCadastroSheet = true
                    },
                    onScanClick = {
                         when (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)) {
                            PackageManager.PERMISSION_GRANTED -> {
                                scannerLauncher.launch(Intent(context, ScannerActivity::class.java))
                            }
                            else -> {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    },
                    onImportClick = { 
                        openFileLauncher.launch(arrayOf("text/csv", "text/plain"))
                    },
                    onExportClick = { inventarioViewModel.onExportClicked() },
                    onSettingsClick = { 
                        settingsLauncher.launch(Intent(context, SettingsActivity::class.java))
                    },
                    onApiSettingsClick = {
                        apiSettingsLauncher.launch(Intent(context, ApiSettingsActivity::class.java))
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

                if (inventarioViewModel.showProductOptions) {
                    ModalBottomSheet(
                        onDismissRequest = { inventarioViewModel.onDismissProductOptions() },
                        sheetState = optionsSheetState
                    ) {
                        ProductOptionsSheetContent(
                            onNovaContagem = {
                                scope.launch {
                                    optionsSheetState.hide()
                                    inventarioViewModel.selectedProduct?.let { 
                                        cadastroViewModel.loadProdutoParaContagem(it)
                                    }
                                    inventarioViewModel.onDismissProductOptions()
                                    showCadastroSheet = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioScreen(
    viewModel: InventarioViewModel,
    onAddContagemClick: () -> Unit,
    onAddProdutoClick: () -> Unit,
    onProductClick: (Produto) -> Unit,
    onContagemClick: (Produto) -> Unit,
    onScanClick: () -> Unit,
    onImportClick: () -> Unit,
    onExportClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onApiSettingsClick: () -> Unit
) {
    var isSearchActive by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contagem de Estoque") },
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
                            DropdownMenuItem(text = { Text("Importar Catálogo (TXT)") }, onClick = onImportClick)
                            DropdownMenuItem(text = { Text("Exportar Contagem (TXT)") }, onClick = onExportClick)
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Configurações Locais") },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                onClick = onSettingsClick
                            )
                            DropdownMenuItem(
                                text = { Text("Configurar API") },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                onClick = onApiSettingsClick
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (viewModel.selectedTabIndex == 0) {
                Column(horizontalAlignment = Alignment.End) {
                    FloatingActionButton(onClick = onScanClick, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Outlined.Inventory2, contentDescription = "Scanner")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FloatingActionButton(onClick = onAddContagemClick) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar Contagem")
                    }
                }
            } else {
                 FloatingActionButton(onClick = onAddProdutoClick) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar Produto ao Catálogo")
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (isSearchActive) {
                TextField(
                    value = viewModel.searchText,
                    onValueChange = { viewModel.onSearchTextChanged(it) },
                    label = { Text("Pesquisar Catálogo...") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            PrimaryTabRow(selectedTabIndex = viewModel.selectedTabIndex) {
                Tab(selected = viewModel.selectedTabIndex == 0, onClick = { viewModel.onTabSelected(0) }, text = { Text("CONTAGEM") })
                Tab(selected = viewModel.selectedTabIndex == 1, onClick = { viewModel.onTabSelected(1) }, text = { Text("PRODUTOS") })
            }

            val contagemProductsCount = viewModel.contagemList.distinctBy { it.ean }.size
            val contagemTotalQuantity = viewModel.contagemList.sumOf { it.qtd }

            when (viewModel.selectedTabIndex) {
                0 -> ContagensTab(contagemProductsCount, contagemTotalQuantity, viewModel.contagemList, onContagemClick)
                1 -> ProdutosTab(viewModel.productCatalogList, onProductClick)
            }
        }
    }
}

@Composable
fun ContagensTab(
    productsCount: Int,
    totalQuantity: Double,
    contagemList: List<Produto>,
    onContagemClick: (Produto) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp)) {
                InfoColumn(title = "PRODUTOS", value = productsCount.toString(), modifier = Modifier.weight(1f))
                InfoColumn(title = "QUANTIDADE TOTAL", value = String.format("%.2f", totalQuantity), modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (contagemList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Inventory2, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                    Text("Nenhum produto na contagem", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                }
            }
        } else {
            LazyColumn {
                items(contagemList) { produto ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onContagemClick(produto) }.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(text = "${produto.codigo}", modifier = Modifier.weight(0.5f))
                        Text(text = produto.ean, modifier = Modifier.weight(1f))
                        Text(text = produto.nome, modifier = Modifier.weight(1f))
                        Text(text = String.format("%.2f", produto.qtd), modifier = Modifier.weight(0.5f), textAlign = TextAlign.End)
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun ProdutosTab(
    productList: List<Produto>,
    onProductClick: (Produto) -> Unit
) {
    if (productList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nenhum produto no catálogo. Importe os dados via API ou arquivo.", textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
        }
    } else {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(productList) { product ->
                Row(
                    modifier = Modifier.padding(8.dp).clickable { onProductClick(product) }
                ) {
                    Text(text = "${product.codigo}", modifier = Modifier.weight(0.5f))
                    Text(text = product.ean, modifier = Modifier.weight(1f))
                    Text(text = product.nome, modifier = Modifier.weight(1f))
                    Text(text = String.format("%.2f", product.qtd), modifier = Modifier.weight(0.5f), textAlign = TextAlign.End)
                }
            }
        }
    }
}

@Composable
fun InfoColumn(title: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Text(text = value, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun CadastroSheetContent(
    viewModel: CadastroViewModel,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp).navigationBarsPadding().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Contagem de Produto", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 8.dp))

        OutlinedTextField(
            value = viewModel.codigoInput,
            onValueChange = {},
            label = { Text("CÓDIGO") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        OutlinedTextField(
            value = viewModel.eanInput,
            onValueChange = viewModel::onEanChanged,
            label = { Text("EAN (Código de Barras)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        
        OutlinedTextField(
            value = viewModel.nomeInput,
            onValueChange = viewModel::onNomeChanged,
            label = { Text("NOME") },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewModel.produtoOriginal == null
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text("QUANTIDADE", style = MaterialTheme.typography.labelMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { viewModel.decrementQuantidade() }, modifier = Modifier.padding(end = 8.dp).size(56.dp)) {
                Icon(Icons.Default.Remove, contentDescription = "Decrementar")
            }
            OutlinedTextField(
                value = viewModel.quantidadeInput,
                onValueChange = viewModel::onQuantidadeChanged,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
            )
            OutlinedButton(onClick = { viewModel.incrementQuantidade() }, modifier = Modifier.padding(start = 8.dp).size(56.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Incrementar")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onCancel) { Text("CANCELAR") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onSave) { Text("SALVAR") }
        }
    }
}

@Composable
fun ProductOptionsSheetContent(
    onNovaContagem: () -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        ListItem(
            headlineContent = { Text("Iniciar nova contagem") },
            leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
            modifier = Modifier.clickable(onClick = onNovaContagem)
        )
    }
}

// --- PREVIEWS ---

@Preview(showBackground = true)
@Composable
fun InventarioScreenPreview() {
    WsinventarioTheme {
        val context = LocalContext.current
        val viewModel = InventarioViewModel(context.applicationContext as Application)
        InventarioScreen(viewModel = viewModel, onAddContagemClick = {}, onAddProdutoClick = {}, onProductClick = {}, onContagemClick = {}, onScanClick = {}, onSettingsClick = {}, onApiSettingsClick = {}, onImportClick = {}, onExportClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun CadastroSheetContentPreview() {
    WsinventarioTheme {
        CadastroSheetContent(
            viewModel = viewModel(factory = CadastroViewModelFactory(Application())),
            onCancel = {}, 
            onSave = {}
        )
    }
}
