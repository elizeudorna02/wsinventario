package com.example.wsinventario.data.file

import com.example.wsinventario.data.Produto
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.abs

class FileHandler {

    fun createExportContent(items: List<Produto>, delimitador: String, fieldCount: Int, includeHeader: Boolean): String {
        val header = if (includeHeader) {
            when (fieldCount) {
                2 -> "EAN${delimitador}QTD\n"
                3 -> "EAN${delimitador}NOME${delimitador}QTD\n"
                else -> "CODIGO${delimitador}EAN${delimitador}NOME${delimitador}QTD\n"
            }
        } else {
            ""
        }

        val rows = items.joinToString(separator = "\n") { item ->
            val qtyInt = item.qtd.toInt()
            val isNegative = qtyInt < 0
            val formattedQty = if (isNegative) {
                "-" + abs(qtyInt).toString().padStart(7, '0')
            } else {
                qtyInt.toString().padStart(7, '0')
            }

            when (fieldCount) {
                2 -> "${item.ean}${delimitador}${formattedQty}"
                3 -> "${item.ean}${delimitador}${item.nome}${delimitador}${formattedQty}"
                else -> "${item.codigo}${delimitador}${item.ean}${delimitador}${item.nome}${delimitador}${formattedQty}"
            }
        }
        return header + rows
    }

    /**
     * Lê um arquivo de importação no formato padrão e o converte em uma lista de produtos.
     */
    fun readProductsFromStream(inputStream: InputStream, delimitador: String): Result<List<Produto>> {
        return try {
            val productList = mutableListOf<Produto>()
            inputStream.bufferedReader().useLines { lines ->
                lines.drop(1).forEach { line -> // Pula o cabeçalho
                    val tokens = line.split(delimitador)
                    if (tokens.size >= 4) {
                         val produto = Produto(
                            codigo = tokens[0].trim().toIntOrNull() ?: 0,
                            ean = tokens[1].trim(),
                            nome = tokens[2].trim(),
                            qtd = tokens[3].trim().toDoubleOrNull() ?: 0.0
                        )
                        productList.add(produto)
                    }
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
