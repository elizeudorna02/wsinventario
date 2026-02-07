
package com.example.wsinventario.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.wsinventario.data.ContagemItem
import com.example.wsinventario.data.Produto
import com.example.wsinventario.data.ProdutoRepository
import com.example.wsinventario.data.file.FileHandler
import com.example.wsinventario.settings.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InventarioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProdutoRepository(application)
    private val fileHandler = FileHandler()

    var searchText by mutableStateOf("")
    var productCatalogList by mutableStateOf<List<Produto>>(emptyList())
    var contagemList by mutableStateOf<List<ContagemItem>>(emptyList())
    var contagemProductsCount by mutableIntStateOf(0)
    var contagemTotalQuantity by mutableIntStateOf(0)
    var selectedTabIndex by mutableIntStateOf(0)
    var showProductOptions by mutableStateOf(false)
    var selectedProduct by mutableStateOf<Produto?>(null)

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()
    
    private var searchJob: Job? = null

    init {
        refreshData()
    }

    fun onTabSelected(index: Int) {
        selectedTabIndex = index
    }

    fun onSearchTextChanged(text: String) {
        searchText = text
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300L)
            productCatalogList = withContext(Dispatchers.IO) {
                repository.findProdutos(text)
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            contagemProductsCount = withContext(Dispatchers.IO) { repository.getContagemProductsCount() }
            contagemTotalQuantity = withContext(Dispatchers.IO) { repository.getContagemTotalQuantity() }
            contagemList = withContext(Dispatchers.IO) { repository.getAllContagens() }
            productCatalogList = withContext(Dispatchers.IO) { repository.findProdutos(searchText) }
        }
    }

    fun onProductSaveSuccess() {
        selectedTabIndex = 0
        refreshData()
    }

    fun onProductClicked(product: Produto) {
        selectedProduct = product
        showProductOptions = true
    }

    fun onDismissProductOptions() {
        showProductOptions = false
        selectedProduct = null
    }

    // --- Lógica de Importação/Exportação ---

    fun onExportClicked() {
        viewModelScope.launch {
            val delimitador = withContext(Dispatchers.IO) {
                repository.getParametro(SettingsViewModel.KEY_EXPORT_DELIMITADOR, ";")
            }
            val itemsToExport = withContext(Dispatchers.IO) { repository.getAllContagens() }
            if (itemsToExport.isEmpty()) {
                _uiEvents.emit(UiEvent.ShowToast("Nenhuma contagem para exportar."))
                return@launch
            }
            val csvContent = fileHandler.createContagemExportContent(itemsToExport, delimitador)
            _uiEvents.emit(UiEvent.SaveFile(csvContent, "contagem.csv"))
        }
    }

    fun onImportFileSelected(uri: Uri) {
        viewModelScope.launch {
            val delimitador = withContext(Dispatchers.IO) { repository.getParametro(SettingsViewModel.KEY_IMPORT_DELIMITADOR, ";") }
            val fieldCount = withContext(Dispatchers.IO) { repository.getParametro(SettingsViewModel.KEY_IMPORT_FIELD_COUNT, "3").toIntOrNull() ?: 3 }

            try {
                getApplication<Application>().contentResolver.openInputStream(uri)?.use { inputStream ->
                    val result = fileHandler.readProductsFromStream(inputStream, delimitador, fieldCount)
                    result.onSuccess { produtos ->
                        if (produtos.isNotEmpty()) {
                            val produtosParaSalvar = if (fieldCount == 2) {
                                val produtosCompletos = mutableListOf<Produto>()
                                withContext(Dispatchers.IO) {
                                    for (item in produtos) {
                                        val produtoDoCatalogo = repository.findProdutoByExactCodigo(item.codigoDeBarras)
                                        produtosCompletos.add(
                                            item.copy(nome = produtoDoCatalogo?.nome ?: "NOME NÃO ENCONTRADO")
                                        )
                                    }
                                }
                                produtosCompletos
                            } else {
                                produtos
                            }
                            withContext(Dispatchers.IO) { repository.replaceAllProdutos(produtosParaSalvar) }
                            _uiEvents.emit(UiEvent.ShowToast("${produtosParaSalvar.size} produtos importados com sucesso!"))
                            refreshData()
                        } else {
                            _uiEvents.emit(UiEvent.ShowToast("Nenhum produto válido encontrado no arquivo."))
                        }
                    }
                    result.onFailure {
                         _uiEvents.emit(UiEvent.ShowToast("Erro ao ler o arquivo."))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiEvents.emit(UiEvent.ShowToast("Falha ao abrir o arquivo."))
            }
        }
    }

    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
        data class SaveFile(val content: String, val fileName: String) : UiEvent()
    }
}

class InventarioViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventarioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventarioViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
