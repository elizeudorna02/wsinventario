package com.example.wsinventario.data

import com.google.gson.Gson
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ApiService(private val apiUrl: String) {

    fun getProdutos(): Result<List<Produto>> {
        if (apiUrl.isBlank()) {
            return Result.failure(Exception("URL da API não configurada."))
        }

        return try {
            val url = URL(apiUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000 // 5 segundos para conectar
            connection.readTimeout = 5000 // 5 segundos para ler
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = InputStreamReader(connection.inputStream)
                val response = Gson().fromJson(reader, ApiResponse::class.java)
                reader.close()
                Result.success(response.dados ?: emptyList())
            } else {
                Result.failure(Exception("Falha na conexão com a API: Código ${connection.responseCode}"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
