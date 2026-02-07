
package com.example.wsinventario.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.wsinventario.data.ContagemItem
import com.example.wsinventario.data.Produto
import com.example.wsinventario.data.ProdutoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CadastroViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProdutoRepository(application)

    var id by mutableStateOf<Long?>(null)
    var codigoDeBarras by mutableStateOf("")
    var nome by mutableStateOf("")
    var quantidade by mutableStateOf("1")

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    private var searchJob: Job? = null

    fun onCodigoDeBarrasChanged(text: String) {
        if (text.all { it.isDigit() }) {
            codigoDeBarras = text
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                delay(500L)
                if (text.isNotBlank()) {
                    val product = withContext(Dispatchers.IO) {
                        repository.findProdutoByExactCodigo(text)
                    }
                    if (product != null) {
                        nome = product.nome
                    } else {
                        if (text == codigoDeBarras) {
                             _uiEvents.emit(UiEvent.ShowToast("Produto não cadastrado"))
                        }
                    }
                }
            }
        }
    }

    fun onNomeChanged(text: String) {
        nome = text
    }

    fun onQuantidadeChanged(text: String) {
        if (text.isEmpty() || text.all { it.isDigit() }) {
            quantidade = text
        }
    }

    fun incrementQuantidade() {
        val currentQuant = quantidade.toIntOrNull() ?: 0
        quantidade = (currentQuant + 1).toString()
    }

    fun decrementQuantidade() {
        val currentQuant = quantidade.toIntOrNull() ?: 1
        if (currentQuant > 1) {
            quantidade = (currentQuant - 1).toString()
        }
    }

    fun clearForm() {
        id = null
        codigoDeBarras = ""
        nome = ""
        quantidade = "1"
    }

    fun loadProduct(product: Produto) {
        id = product.id
        nome = product.nome
        codigoDeBarras = product.codigoDeBarras
        quantidade = "1"
    }

    fun loadContagemItem(item: ContagemItem) {
        id = item.id
        nome = item.nome
        codigoDeBarras = item.codigoDeBarras
        quantidade = item.quantidade.toString()
    }

    fun onSaveContagemClicked(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val quant = quantidade.toIntOrNull()
        if (codigoDeBarras.isBlank()) {
            onFailure("O código de barras é obrigatório.")
            return
        }
        if (quant == null || quant <= 0) {
            onFailure("A quantidade deve ser maior que zero.")
            return
        }

        viewModelScope.launch {
            val product = withContext(Dispatchers.IO) {
                repository.findProdutoByExactCodigo(codigoDeBarras)
            }

            if (product != null) {
                withContext(Dispatchers.IO) { repository.addOrUpdateContagem(codigoDeBarras, product.nome, quant) }
                onSuccess()
            } else {
                if (nome.isBlank()) {
                    onFailure("Produto não cadastrado. Preencha o nome para criá-lo.")
                    return@launch
                }
                _uiEvents.emit(UiEvent.ShowConfirmationDialog)
            }
        }
    }

    fun confirmAndCreateProduct(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val quant = quantidade.toIntOrNull()
        if (nome.isBlank() || codigoDeBarras.isBlank() || quant == null || quant <= 0) {
            onFailure("Ocorreu um erro. Verifique os campos e tente novamente.")
            return
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.createProduto(codigoDeBarras, nome)
                repository.addOrUpdateContagem(codigoDeBarras, nome, quant)
            }
            onSuccess()
        }
    }

    fun createProdutoNoCatalogo(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (nome.isBlank() || codigoDeBarras.isBlank()) {
            onFailure("Nome e código de barras são obrigatórios.")
            return
        }

        viewModelScope.launch {
            val existingProduct = withContext(Dispatchers.IO) {
                repository.findProdutoByExactCodigo(codigoDeBarras)
            }
            if (existingProduct != null) {
                onFailure("Um produto com este código de barras já existe no catálogo.")
                return@launch
            }
            
            withContext(Dispatchers.IO) {
                repository.createProduto(codigoDeBarras, nome)
            }
            onSuccess()
        }
    }

    fun updateProduct(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentId = id
        if (currentId == null) {
            onFailure("ID do produto não encontrado.")
            return
        }
        if (nome.isBlank() || codigoDeBarras.isBlank()) {
            onFailure("Nome e código de barras são obrigatórios.")
            return
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.updateProduct(currentId, nome, codigoDeBarras)
            }
            onSuccess()
        }
    }

    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
        data object ShowConfirmationDialog : UiEvent()
    }
}

class CadastroViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CadastroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CadastroViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
