package com.example.wsinventario.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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

    // State for the form
    var eanInput by mutableStateOf("")
    var nomeInput by mutableStateOf("")
    var quantidadeInput by mutableStateOf("1") // Changed to integer
    var codigoInput by mutableStateOf("")
    var produtoOriginal by mutableStateOf<Produto?>(null)

    private var searchJob: Job? = null

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    fun onEanChanged(text: String) {
        if (text.length <= 14) {
            eanInput = text
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                delay(300L) // Debounce
                if (text.isNotBlank()) {
                    val productFromRepo = withContext(Dispatchers.IO) {
                        repository.findProdutoByEan(text)
                    }
                    produtoOriginal = productFromRepo
                    nomeInput = productFromRepo?.nome ?: ""
                    codigoInput = productFromRepo?.codigo?.toString() ?: ""
                }
            }
        }
    }

    fun onNomeChanged(text: String) {
        nomeInput = text
    }

    fun onCodigoChanged(text: String) {
        codigoInput = text
    }

    fun onQuantidadeChanged(text: String) {
        // Allow only digits or an empty string
        if (text.isEmpty() || text.all { it.isDigit() }) {
            quantidadeInput = text
        }
    }

    fun incrementQuantidade() {
        val currentQuant = quantidadeInput.toIntOrNull() ?: 0
        quantidadeInput = (currentQuant + 1).toString()
    }

    fun decrementQuantidade() {
        val currentQuant = quantidadeInput.toIntOrNull() ?: 1
        if (currentQuant > 0) {
            quantidadeInput = (currentQuant - 1).toString()
        }
    }

    fun clearForm() {
        produtoOriginal = null
        eanInput = ""
        nomeInput = ""
        quantidadeInput = "1"
        codigoInput = ""
    }

    fun loadProdutoParaContagem(produto: Produto) {
        produtoOriginal = produto
        eanInput = produto.ean
        nomeInput = produto.nome
        quantidadeInput = produto.qtd.toInt().toString() // Display as integer
        codigoInput = produto.codigo.toString()
    }

    fun onSaveContagemClicked(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val quant = quantidadeInput.toDoubleOrNull()
        if (eanInput.isBlank()) {
            onFailure("O código EAN é obrigatório.")
            return
        }
        if (quant == null || quant < 0) {
            onFailure("A quantidade não pode ser negativa.")
            return
        }

        viewModelScope.launch {
            val codigoInt = codigoInput.toIntOrNull() ?: produtoOriginal?.codigo ?: 0
            val produtoParaSalvar = produtoOriginal?.copy(qtd = quant, codigo = codigoInt, nome = nomeInput)
                ?: Produto(codigo = codigoInt, ean = eanInput, nome = nomeInput.takeIf { it.isNotBlank() } ?: "PRODUTO SEM NOME", qtd = quant)

            withContext(Dispatchers.IO) {
                repository.addOrUpdateContagem(produtoParaSalvar)
            }
            onSuccess()
        }
    }

    fun submitNewProduto() {
        if (eanInput.isBlank() || nomeInput.isBlank()) {
            viewModelScope.launch { _uiEvents.emit(UiEvent.ShowToast("EAN e Nome são obrigatórios.")) }
            return
        }

        viewModelScope.launch {
            val existingProduct = withContext(Dispatchers.IO) {
                repository.findProdutoByEan(eanInput)
            }
            if (existingProduct != null) {
                _uiEvents.emit(UiEvent.ShowProductExistsDialog(existingProduct))
                return@launch
            }

            val newProduto = Produto(codigo = codigoInput.toIntOrNull() ?: 0, ean = eanInput, nome = nomeInput, qtd = 0.0)
            val result = withContext(Dispatchers.IO) {
                repository.createProduto(newProduto)
            }

            if (result > -1) {
                _uiEvents.emit(UiEvent.CadastroSuccess)
            } else {
                _uiEvents.emit(UiEvent.ShowToast("Erro ao criar o produto."))
            }
        }
    }
    
    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
        data class ShowProductExistsDialog(val product: Produto) : UiEvent()
        object CadastroSuccess : UiEvent()
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
