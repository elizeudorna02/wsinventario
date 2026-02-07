package com.example.wsinventario.data

/**
 * Representa um único item na lista de contagem atual (tabela 'contagem').
 */
data class ContagemItem(
    val id: Long,
    val codigoDeBarras: String,
    val nome: String,
    val quantidade: Int
)
