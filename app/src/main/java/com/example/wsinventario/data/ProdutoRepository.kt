
package com.example.wsinventario.data

import android.content.Context

/**
 * Repositório que abstrai o acesso aos dados, servindo como uma ponte
 * entre os ViewModels e as fontes de dados (neste caso, o DatabaseHelper).
 */
class ProdutoRepository(context: Context) {

    private val dbHelper = DatabaseHelper(context.applicationContext)

    // --- Funções da Tabela de Contagem ---

    fun getAllContagens(): List<ContagemItem> {
        return dbHelper.getAllContagens()
    }

    fun getContagemProductsCount(): Int {
        return dbHelper.getContagemProductsCount()
    }

    fun getContagemTotalQuantity(): Int {
        return dbHelper.getContagemTotalQuantity()
    }

    fun addOrUpdateContagem(codigoDeBarras: String, nome: String, quantidade: Int): Long {
        return dbHelper.addOrUpdateContagem(codigoDeBarras, nome, quantidade)
    }

    // --- Funções da Tabela de Produtos (Catálogo Mestre) ---

    fun findProdutos(query: String): List<Produto> {
        return dbHelper.findProdutos(query)
    }
    
    fun findProdutoByExactCodigo(codigoDeBarras: String): Produto? {
        return dbHelper.findProdutoByExactCodigo(codigoDeBarras)
    }

    fun createProduto(codigoDeBarras: String, nome: String): Long {
        return dbHelper.createProduto(codigoDeBarras, nome)
    }

    fun updateProduct(id: Long, nome: String, codigoDeBarras: String): Int {
        return dbHelper.updateProduct(id, nome, codigoDeBarras)
    }

    fun replaceAllProdutos(produtos: List<Produto>) {
        dbHelper.replaceAllProdutos(produtos)
    }

    // --- Funções da Tabela de Parâmetros ---

    fun getParametro(key: String, defaultValue: String): String {
        return dbHelper.getParametro(key, defaultValue)
    }

    fun setParametro(key: String, value: String) {
        dbHelper.setParametro(key, value)
    }
}
