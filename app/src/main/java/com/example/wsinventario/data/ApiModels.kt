package com.example.wsinventario.data

import com.google.gson.annotations.SerializedName

/**
 * Modelo para a resposta completa da API.
 */
data class ApiResponse(
    val mensagem: String,
    val dados: List<Produto>
)

/**
 * Modelo de dados PADRÃO para um produto. 
 * Usado em todo o aplicativo (API, Banco de Dados e UI).
 */
data class Produto(
    @SerializedName("CODIGO") val codigo: Int,
    @SerializedName("EAN") val ean: String,
    @SerializedName("NOME") val nome: String,
    @SerializedName("QTD") var qtd: Double
)
