package com.example.wsinventario.data.file

import com.example.wsinventario.data.Produto
import java.io.InputStream
import java.io.OutputStream

class FileHandler {

    /**
     * Cria o conteúdo de texto para exportar uma lista de produtos no formato padrão.
     */
    fun createExportContent(items: List<Produto>): String {
        val header = "CODIGO;EAN;NOME;QTD\n"
        val rows = items.joinToString(separator = "\n") { item ->
            "${item.codigo};${item.ean};${item.nome};${item.qtd}"
        }
        return header + rows
    }

    /**
     * Lê um arquivo de importação de catálogo (com 2, 3 ou 4 colunas) 
     * e o converte em uma lista de produtos no formato padrão.
     */
    fun readProductsFromStream(inputStream: InputStream, delimitador: String): Result<List<Produto>> {
        return try {
            val productList = mutableListOf<Produto>()
            inputStream.bufferedReader().useLines { lines ->
                lines.drop(1).forEach { line -> // Pula o cabeçalho
                    val tokens = line.split(delimitador)
                    var produto: Produto? = null

                    when (tokens.size) {
                        2 -> { // EAN;QTD
                            produto = Produto(
                                codigo = 0, // Sem código no arquivo
                                ean = tokens[0].trim(),
                                nome = "", // Sem nome no arquivo
                                qtd = tokens[1].trim().toDoubleOrNull() ?: 0.0
                            )
                        }
                        3 -> { // EAN;NOME;QTD
                            produto = Produto(
                                codigo = 0, // Sem código no arquivo
                                ean = tokens[0].trim(),
                                nome = tokens[1].trim(),
                                qtd = tokens[2].trim().toDoubleOrNull() ?: 0.0
                            )
                        }
                        4 -> { // CODIGO;EAN;NOME;QTD
                             produto = Produto(
                                codigo = tokens[0].trim().toIntOrNull() ?: 0,
                                ean = tokens[1].trim(),
                                nome = tokens[2].trim(),
                                qtd = tokens[3].trim().toDoubleOrNull() ?: 0.0
                            )
                        }
                    }
                    produto?.let { productList.add(it) }
                }
            }
            Result.success(productList)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun writeContentToStream(content: String, outputStream: OutputStream) {
        outputStream.writer().use {
            it.write(content)
        }
    }
}
