package com.example.wsinventario.settings

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.wsinventario.data.ProdutoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProdutoRepository(application)

    companion object {
        // Chaves para salvar os parâmetros no banco de dados
        const val KEY_IMPORT_DELIMITADOR = "import_delimitador"
        const val KEY_IMPORT_FIELD_COUNT = "import_field_count"
        const val KEY_EXPORT_DELIMITADOR = "export_delimitador"
    }

    // Estados que guardam os valores na UI
    var importDelimiter by mutableStateOf(";")
        private set
    var importFieldCount by mutableStateOf("3")
        private set
    var exportDelimiter by mutableStateOf(";") // NOVO: Estado para o delimitador de exportação
        private set

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            importDelimiter = withContext(Dispatchers.IO) {
                repository.getParametro(KEY_IMPORT_DELIMITADOR, ";")
            }
            importFieldCount = withContext(Dispatchers.IO) {
                repository.getParametro(KEY_IMPORT_FIELD_COUNT, "3")
            }
            exportDelimiter = withContext(Dispatchers.IO) { // NOVO: Carrega o delimitador de exportação
                repository.getParametro(KEY_EXPORT_DELIMITADOR, ";")
            }
        }
    }

    fun onImportDelimiterChanged(newDelimiter: String) {
        if (newDelimiter.length <= 1) {
            importDelimiter = newDelimiter
        }
    }

    fun onFieldCountChanged(newCount: String) {
        importFieldCount = newCount
    }

    // NOVO: Função para alterar o delimitador de exportação
    fun onExportDelimiterChanged(newDelimiter: String) {
        if (newDelimiter.length <= 1) {
            exportDelimiter = newDelimiter
        }
    }

    /**
     * Salva todas as configurações atuais no banco de dados.
     */
    fun saveSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setParametro(KEY_IMPORT_DELIMITADOR, importDelimiter)
            repository.setParametro(KEY_IMPORT_FIELD_COUNT, importFieldCount)
            repository.setParametro(KEY_EXPORT_DELIMITADOR, exportDelimiter) // NOVO: Salva o novo parâmetro
        }
    }
}

class SettingsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
