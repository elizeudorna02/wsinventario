
package com.example.wsinventario.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 3
        private const val DATABASE_NAME = "InventarioDatabase.db"

        private const val TABLE_PRODUTOS = "produtos"
        private const val KEY_PROD_ID = "id"
        private const val KEY_PROD_CODIGO_BARRAS = "codigo_de_barras"
        private const val KEY_PROD_NOME = "nome"
        private const val KEY_PROD_QUANTIDADE_TOTAL = "quantidade_total"

        private const val TABLE_CONTAGEM = "contagem"
        private const val KEY_CONT_ID = "id"
        private const val KEY_CONT_CODIGO_BARRAS = "codigo_de_barras"
        private const val KEY_CONT_NOME = "nome_produto"
        private const val KEY_CONT_QUANTIDADE = "quantidade"

        private const val TABLE_PARAMETROS = "parametros"
        private const val KEY_PARAM_KEY = "param_key"
        private const val KEY_PARAM_VALUE = "param_value"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createProdutosTable = ("CREATE TABLE " + TABLE_PRODUTOS + "("
                + KEY_PROD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
                + KEY_PROD_CODIGO_BARRAS + " TEXT UNIQUE,"
                + KEY_PROD_NOME + " TEXT," 
                + KEY_PROD_QUANTIDADE_TOTAL + " INTEGER DEFAULT 0" + ")")
        
        val createContagemTable = ("CREATE TABLE " + TABLE_CONTAGEM + "("
                + KEY_CONT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
                + KEY_CONT_CODIGO_BARRAS + " TEXT,"
                + KEY_CONT_NOME + " TEXT,"
                + KEY_CONT_QUANTIDADE + " INTEGER" + ")")

        val createParametrosTable = ("CREATE TABLE " + TABLE_PARAMETROS + "("
                + KEY_PARAM_KEY + " TEXT PRIMARY KEY,"
                + KEY_PARAM_VALUE + " TEXT" + ")")

        db?.execSQL(createProdutosTable)
        db?.execSQL(createContagemTable)
        db?.execSQL(createParametrosTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUTOS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CONTAGEM")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PARAMETROS")
        onCreate(db)
    }

    fun getParametro(key: String, defaultValue: String): String {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_PARAMETROS, arrayOf(KEY_PARAM_VALUE), "$KEY_PARAM_KEY = ?", arrayOf(key), null, null, null)
        var value = defaultValue
        if (cursor.moveToFirst()) {
            value = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PARAM_VALUE))
        }
        cursor.close()
        return value
    }

    fun setParametro(key: String, value: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_PARAM_KEY, key)
            put(KEY_PARAM_VALUE, value)
        }
        db.insertWithOnConflict(TABLE_PARAMETROS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun addOrUpdateContagem(codigoDeBarras: String, nome: String, quantidadeToAdd: Int): Long {
        val db = this.writableDatabase
        var result: Long = -1
        db.beginTransaction()
        try {
            val cursor = db.query(TABLE_CONTAGEM, arrayOf(KEY_CONT_ID, KEY_CONT_QUANTIDADE),
                "$KEY_CONT_CODIGO_BARRAS = ?", arrayOf(codigoDeBarras), null, null, null)

            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_CONT_ID))
                val currentQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CONT_QUANTIDADE))
                val newQuantity = currentQuantity + quantidadeToAdd

                val values = ContentValues().apply { put(KEY_CONT_QUANTIDADE, newQuantity) }
                result = db.update(TABLE_CONTAGEM, values, "$KEY_CONT_ID = ?", arrayOf(id.toString())).toLong()
            } else {
                val values = ContentValues().apply {
                    put(KEY_CONT_CODIGO_BARRAS, codigoDeBarras)
                    put(KEY_CONT_NOME, nome)
                    put(KEY_CONT_QUANTIDADE, quantidadeToAdd)
                }
                result = db.insert(TABLE_CONTAGEM, null, values)
            }
            cursor.close()
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return result
    }

    fun getAllContagens(): List<ContagemItem> {
        val contagemList = mutableListOf<ContagemItem>()
        val selectQuery = "SELECT * FROM $TABLE_CONTAGEM"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                val contagem = ContagemItem(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_CONT_ID)),
                    codigoDeBarras = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CONT_CODIGO_BARRAS)),
                    nome = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CONT_NOME)),
                    quantidade = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CONT_QUANTIDADE))
                )
                contagemList.add(contagem)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return contagemList
    }
    
    fun getContagemProductsCount(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(DISTINCT $KEY_CONT_CODIGO_BARRAS) FROM $TABLE_CONTAGEM", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    fun getContagemTotalQuantity(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT SUM($KEY_CONT_QUANTIDADE) FROM $TABLE_CONTAGEM", null)
        var total = 0
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0)
        }
        cursor.close()
        return total
    }

    fun findProdutoByExactCodigo(codigoDeBarras: String): Produto? {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_PRODUTOS, null, "$KEY_PROD_CODIGO_BARRAS = ?", arrayOf(codigoDeBarras), null, null, null)
        var produto: Produto? = null
        if (cursor.moveToFirst()) {
            produto = Produto(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_PROD_ID)),
                codigoDeBarras = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROD_CODIGO_BARRAS)),
                nome = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROD_NOME)),
                quantidade = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PROD_QUANTIDADE_TOTAL))
            )
        }
        cursor.close()
        return produto
    }

    fun findProdutos(query: String): List<Produto> {
        val productList = mutableListOf<Produto>()
        val selectQuery = "SELECT * FROM $TABLE_PRODUTOS WHERE $KEY_PROD_CODIGO_BARRAS LIKE ? OR $KEY_PROD_NOME LIKE ?"
        val db = this.readableDatabase
        val searchPattern = "%$query%"
        val cursor = db.rawQuery(selectQuery, arrayOf(searchPattern, searchPattern))
        if (cursor.moveToFirst()) {
            do {
                val produto = Produto(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_PROD_ID)),
                    codigoDeBarras = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROD_CODIGO_BARRAS)),
                    nome = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROD_NOME)),
                    quantidade = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PROD_QUANTIDADE_TOTAL))
                )
                productList.add(produto)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return productList
    }

    fun createProduto(codigoDeBarras: String, nome: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_PROD_CODIGO_BARRAS, codigoDeBarras)
            put(KEY_PROD_NOME, nome)
            put(KEY_PROD_QUANTIDADE_TOTAL, 0)
        }
        return db.insert(TABLE_PRODUTOS, null, values)
    }

    fun replaceAllProdutos(produtos: List<Produto>) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_PRODUTOS, null, null)
            for (produto in produtos) {
                val values = ContentValues().apply {
                    put(KEY_PROD_CODIGO_BARRAS, produto.codigoDeBarras)
                    put(KEY_PROD_NOME, produto.nome)
                    put(KEY_PROD_QUANTIDADE_TOTAL, produto.quantidade)
                }
                db.insert(TABLE_PRODUTOS, null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
    
    fun updateProduct(id: Long, nome: String, codigoDeBarras: String): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_PROD_NOME, nome)
        contentValues.put(KEY_PROD_CODIGO_BARRAS, codigoDeBarras)
        return db.update(TABLE_PRODUTOS, contentValues, "$KEY_PROD_ID = ?", arrayOf(id.toString()))
    }
}
