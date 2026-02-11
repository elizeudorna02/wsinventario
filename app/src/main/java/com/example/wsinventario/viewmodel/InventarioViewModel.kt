package com.example.wsinventario.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
    var contagemList by mutableStateOf<List<Produto>>(emptyList())
    var selectedTabIndex by mutableStateOf(0)
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
        if (index == 1) { // Quando for para a aba de produtos, força a busca
            onSearchTextChanged(searchText)
        }
    }

    fun onSearchTextChanged(text: String) {
        searchText = text
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300L) // Debounce para evitar buscas a cada tecla digitada
            productCatalogList = withContext(Dispatchers.IO) {
                repository.findProdutos(text)
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            contagemList = withContext(Dispatchers.IO) { repository.getAllContagens() }
            onSearchTextChanged(searchText)
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

    fun findAndStartContagem(ean: String) {
        viewModelScope.launch {
            val product = withContext(Dispatchers.IO) {
                repository.findProdutoByEan(ean)
            }
            if (product != null) {
                _uiEvents.emit(UiEvent.StartContagemForProduct(product))
            } else {
                _uiEvents.emit(UiEvent.ShowToast("Produto com EAN $ean não encontrado."))
            }
        }
    }

    fun onExportClicked() {
        viewModelScope.launch {
            val itemsToExport = withContext(Dispatchers.IO) { repository.getAllContagens() }
            if (itemsToExport.isEmpty()) {
                _uiEvents.emit(UiEvent.ShowToast("Nenhuma contagem para exportar."))
                return@launch
            }

            val exportWithoutDelimiter = withContext(Dispatchers.IO) {
                repository.getParametro(SettingsViewModel.KEY_EXPORT_NO_DELIMITER, "false").toBoolean()
            }
            
            val delimitador = if (exportWithoutDelimiter) {
                ""
            } else {
                withContext(Dispatchers.IO) {
                    repository.getParametro(SettingsViewModel.KEY_EXPORT_DELIMITADOR, ";")
                }
            }

            val fieldCount = withContext(Dispatchers.IO) {
                repository.getParametro(SettingsViewModel.KEY_EXPORT_FIELD_COUNT, "4").toIntOrNull() ?: 4
            }

            val csvContent = fileHandler.createExportContent(itemsToExport, delimitador, fieldCount)
            _uiEvents.emit(UiEvent.SaveFile(csvContent, "contagem.csv"))
        }
    }

    fun onImportFileSelected(uri: Uri) {
        viewModelScope.launch {
            val delimitador = withContext(Dispatchers.IO) {
                repository.getParametro(SettingsViewModel.KEY_IMPORT_DELIMITADOR, ";")
            }
            try {
                getApplication<Application>().contentResolver.openInputStream(uri)?.use { inputStream ->
                    val result = repository.importarCatalogoDeArquivo(inputStream, delimitador)
                    result.onSuccess {
                        _uiEvents.emit(UiEvent.ShowToast("$it produtos importados com sucesso!"))
                        refreshData()
                    }
                    result.onFailure {
                         _uiEvents.emit(UiEvent.ShowToast("Erro ao ler o arquivo: ${it.message}"))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiEvents.emit(UiEvent.ShowToast("Falha ao abrir o arquivo."))
            }
        }
    }

    fun importarProdutosDaApi() {
        viewModelScope.launch {
            _uiEvents.emit(UiEvent.ShowToast("Iniciando importação da API..."))
            val result = withContext(Dispatchers.IO) {
                repository.importarCatalogoDaApi()
            }
            result.fold(
                onSuccess = {
                    _uiEvents.emit(UiEvent.ShowToast("$it produtos importados com sucesso!"))
                    refreshData()
                },
                onFailure = {
                    _uiEvents.emit(UiEvent.ShowToast("Falha ao importar: ${it.message}"))
                }
            )
        }
    }

    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
        data class SaveFile(val content: String, val fileName: String) : UiEvent()
        data class StartContagemForProduct(val product: Produto) : UiEvent()
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
