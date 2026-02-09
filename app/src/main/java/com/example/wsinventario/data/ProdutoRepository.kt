package com.example.wsinventario.data

import android.content.Context
import com.example.wsinventario.data.file.FileHandler
import java.io.InputStream

class ProdutoRepository(context: Context) {

    private val dbHelper = DatabaseHelper(context.applicationContext)
    private var apiService: ApiService
    private val fileHandler = FileHandler()

    companion object {
        const val KEY_API_URL = "api_url"
    }

    init {
        apiService = createApiServiceFromSettings()
    }

    private fun createApiServiceFromSettings(): ApiService {
        val apiUrl = dbHelper.getParametro(KEY_API_URL, "http://127.0.0.1:5000/api/produtos")
        return ApiService(apiUrl)
    }

    // --- Catalog Functions (produtos table) ---

    suspend fun importarCatalogoDaApi(): Result<Int> {
        apiService = createApiServiceFromSettings() // Refresh service with latest settings
        val result = apiService.getProdutos()
        return result.fold(
            onSuccess = { produtosDaApi ->
                dbHelper.replaceAllProdutos(produtosDaApi)
                Result.success(produtosDaApi.size)
            },
            onFailure = { 
                it.printStackTrace()
                Result.failure(it)
            }
        )
    }

    fun importarCatalogoDeArquivo(inputStream: InputStream, delimitador: String): Result<Int> {
        val result = fileHandler.readProductsFromStream(inputStream, delimitador)
        return result.fold(
            onSuccess = { produtosDoArquivo ->
                dbHelper.replaceAllProdutos(produtosDoArquivo)
                Result.success(produtosDoArquivo.size)
            },
            onFailure = { 
                it.printStackTrace()
                Result.failure(it)
            }
        )
    }

    fun findProdutoByEan(ean: String): Produto? {
        return dbHelper.findProdutoByEan(ean)
    }

    fun findProdutos(query: String): List<Produto> {
        return dbHelper.findProdutos(query)
    }

    fun createProduto(produto: Produto): Long {
        return dbHelper.createProduto(produto)
    }

    // --- Contagem Functions (contagem table) ---

    fun addOrUpdateContagem(produto: Produto): Long {
        return dbHelper.addOrUpdateContagem(produto)
    }

    fun getAllContagens(): List<Produto> {
        return dbHelper.getAllContagens()
    }

    // --- Parameter Functions ---

    fun getParametro(key: String, defaultValue: String): String {
        return dbHelper.getParametro(key, defaultValue)
    }

    fun setParametro(key: String, value: String) {
        dbHelper.setParametro(key, value)
    }
}
