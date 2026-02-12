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

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProdutoRepository(application)

    var exportDelimiter by mutableStateOf("")
    var importDelimiter by mutableStateOf("")
    var importFieldCount by mutableStateOf("")
    var exportFieldCount by mutableStateOf("")
    var exportWithoutDelimiter by mutableStateOf(false)
    var exportWithHeader by mutableStateOf(true)

    companion object {
        const val KEY_EXPORT_DELIMITADOR = "export_delimiter"
        const val KEY_IMPORT_DELIMITADOR = "import_delimiter"
        const val KEY_IMPORT_FIELD_COUNT = "import_field_count"
        const val KEY_EXPORT_FIELD_COUNT = "export_field_count"
        const val KEY_EXPORT_NO_DELIMITER = "export_no_delimiter"
        const val KEY_EXPORT_WITH_HEADER = "export_with_header"
    }

    init {
        exportDelimiter = repository.getParametro(KEY_EXPORT_DELIMITADOR, ";")
        importDelimiter = repository.getParametro(KEY_IMPORT_DELIMITADOR, ";")
        importFieldCount = repository.getParametro(KEY_IMPORT_FIELD_COUNT, "4")
        exportFieldCount = repository.getParametro(KEY_EXPORT_FIELD_COUNT, "4")
        exportWithoutDelimiter = repository.getParametro(KEY_EXPORT_NO_DELIMITER, "false").toBoolean()
        exportWithHeader = repository.getParametro(KEY_EXPORT_WITH_HEADER, "true").toBoolean()
    }

    fun onExportDelimiterChanged(newDelimiter: String) {
        exportDelimiter = newDelimiter
    }

    fun onImportDelimiterChanged(newDelimiter: String) {
        importDelimiter = newDelimiter
    }

    fun onFieldCountChanged(newCount: String) {
        importFieldCount = newCount
    }

    fun onExportFieldCountChanged(newCount: String) {
        exportFieldCount = newCount
    }
    
    fun onExportWithoutDelimiterChanged(isChecked: Boolean) {
        exportWithoutDelimiter = isChecked
    }

    fun onExportWithHeaderChanged(isChecked: Boolean) {
        exportWithHeader = isChecked
    }

    fun saveSettings() {
        repository.setParametro(KEY_EXPORT_DELIMITADOR, exportDelimiter)
        repository.setParametro(KEY_IMPORT_DELIMITADOR, importDelimiter)
        repository.setParametro(KEY_IMPORT_FIELD_COUNT, importFieldCount)
        repository.setParametro(KEY_EXPORT_FIELD_COUNT, exportFieldCount)
        repository.setParametro(KEY_EXPORT_NO_DELIMITER, exportWithoutDelimiter.toString())
        repository.setParametro(KEY_EXPORT_WITH_HEADER, exportWithHeader.toString())
    }

    fun clearProdutosTable() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearProdutosTable()
        }
    }

    fun clearContagemTable() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearContagemTable()
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
