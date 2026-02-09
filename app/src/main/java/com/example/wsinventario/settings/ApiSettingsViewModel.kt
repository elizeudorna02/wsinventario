package com.example.wsinventario.settings

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wsinventario.data.ProdutoRepository

class ApiSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProdutoRepository(application)

    var apiUrl by mutableStateOf("")

    companion object {
        // Acessado pelo ProdutoRepository
        const val KEY_API_URL = "api_url" 
    }

    init {
        apiUrl = repository.getParametro(KEY_API_URL, "http://127.0.0.1:5000/api/produtos") // Default URL
    }

    fun onUrlChange(newUrl: String) {
        apiUrl = newUrl
    }

    fun saveSettings() {
        repository.setParametro(KEY_API_URL, apiUrl)
    }
}

class ApiSettingsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ApiSettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ApiSettingsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
