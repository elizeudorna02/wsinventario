package com.example.wsinventario.data.file

import com.example.wsinventario.data.ContagemItem
import com.example.wsinventario.data.Produto
import java.io.InputStream
import java.io.OutputStream

/**
 * Classe especialista em manipular arquivos de importação/exportação.
 */
class FileHandler {

    /**
     * Cria o conteúdo de texto para exportar a CONTAGEM ATUAL.
     * @param items A lista de itens da contagem.
     * @param delimitador O caractere a ser usado para separar os campos.
     */
    fun createContagemExportContent(items: List<ContagemItem>, delimitador: String): String {
        val header = "codigo_de_barras${delimitador}nome_produto${delimitador}quantidade_contada\n"
        val rows = items.joinToString(separator = "\n") { item ->
            "${item.codigoDeBarras}$delimitador${item.nome}$delimitador${item.quantidade}"
        }
        return header + rows
    }

    /**
     * Lê um arquivo de importação e o converte em uma lista de produtos.
     * @param inputStream O fluxo de dados do arquivo selecionado.
     * @param delimitador O caractere que separa os campos no arquivo.
     * @param fieldCount O número de colunas esperado no arquivo (2 ou 3).
     * @return Uma lista de produtos lidos do arquivo.
     */
    fun readProductsFromStream(inputStream: InputStream, delimitador: String, fieldCount: Int): Result<List<Produto>> {
        return try {
            val productList = mutableListOf<Produto>()
            inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val tokens = line.split(delimitador)
                    
                    // Lógica para 3 colunas: codigo;nome;quantidade
                    if (fieldCount == 3 && tokens.size >= 3) {
                        val quantidade = tokens[2].trim().toIntOrNull()
                        if (quantidade != null) { // Ignora o cabeçalho
                            val product = Produto(
                                id = 0,
                                codigoDeBarras = tokens[0].trim(),
                                nome = tokens[1].trim(),
                                quantidade = quantidade
                            )
                            productList.add(product)
                        }
                    }
                    // Lógica para 2 colunas: codigo;quantidade
                    else if (fieldCount == 2 && tokens.size >= 2) {
                        val quantidade = tokens[1].trim().toIntOrNull()
                        if (quantidade != null) { // Ignora o cabeçalho
                            val product = Produto(
                                id = 0,
                                codigoDeBarras = tokens[0].trim(),
                                nome = "", // Nome será buscado no catálogo depois
                                quantidade = quantidade
                            )
                            productList.add(product)
                        }
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
