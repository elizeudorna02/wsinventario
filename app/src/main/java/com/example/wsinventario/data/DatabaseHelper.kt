package com.example.wsinventario.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 8 // Incremented for final schema standardization
        private const val DATABASE_NAME = "EstoqueDatabase.db"

        // Standardized Column Names
        private const val COL_CODIGO = "codigo"
        private const val COL_EAN = "ean"
        private const val COL_NOME = "nome"
        private const val COL_QTD = "qtd"

        // Correct Table Names
        private const val TABLE_PRODUTOS = "produtos"
        private const val TABLE_CONTAGEM = "contagem"
        private const val TABLE_PARAMETROS = "parametros"

        private const val KEY_PARAM_KEY = "param_key"
        private const val KEY_PARAM_VALUE = "param_value"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createProdutosTable = ("CREATE TABLE " + TABLE_PRODUTOS + "("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT," 
                + COL_CODIGO + " INTEGER,"
                + COL_EAN + " TEXT UNIQUE,"
                + COL_NOME + " TEXT," 
                + COL_QTD + " REAL DEFAULT 0" + ")")
        
        val createContagemTable = ("CREATE TABLE " + TABLE_CONTAGEM + "("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT," 
                + COL_CODIGO + " INTEGER,"
                + COL_EAN + " TEXT UNIQUE,"
                + COL_NOME + " TEXT,"
                + COL_QTD + " REAL" + ")")

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

    // --- Product Table Functions ---

    fun createProduto(produto: Produto): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_CODIGO, produto.codigo)
            put(COL_EAN, produto.ean)
            put(COL_NOME, produto.nome)
            put(COL_QTD, produto.qtd)
        }
        return db.insertWithOnConflict(TABLE_PRODUTOS, null, values, SQLiteDatabase.CONFLICT_IGNORE)
    }

    fun replaceAllProdutos(produtos: List<Produto>) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_PRODUTOS, null, null)
            for (produto in produtos) {
                val values = ContentValues().apply {
                    put(COL_CODIGO, produto.codigo)
                    put(COL_EAN, produto.ean)
                    put(COL_NOME, produto.nome)
                    put(COL_QTD, produto.qtd)
                }
                db.insert(TABLE_PRODUTOS, null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun findProdutoByEan(ean: String): Produto? {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_PRODUTOS, null, "$COL_EAN = ?", arrayOf(ean), null, null, null)
        var produto: Produto? = null
        if (cursor.moveToFirst()) {
            produto = Produto(
                codigo = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CODIGO)),
                ean = cursor.getString(cursor.getColumnIndexOrThrow(COL_EAN)),
                nome = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOME)),
                qtd = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_QTD))
            )
        }
        cursor.close()
        return produto
    }

    fun findProdutos(query: String): List<Produto> {
        val productList = mutableListOf<Produto>()
        val selectQuery = "SELECT * FROM $TABLE_PRODUTOS WHERE $COL_EAN LIKE ? OR $COL_NOME LIKE ?"
        val db = this.readableDatabase
        val searchPattern = "%${query}%"
        val cursor = db.rawQuery(selectQuery, arrayOf(searchPattern, searchPattern))
        if (cursor.moveToFirst()) {
            do {
                val produto = Produto(
                    codigo = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CODIGO)),
                    ean = cursor.getString(cursor.getColumnIndexOrThrow(COL_EAN)),
                    nome = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOME)),
                    qtd = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_QTD))
                )
                productList.add(produto)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return productList
    }

    // --- Contagem Functions ---

    fun addOrUpdateContagem(produto: Produto): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_CODIGO, produto.codigo)
            put(COL_EAN, produto.ean)
            put(COL_NOME, produto.nome)
            put(COL_QTD, produto.qtd)
        }
        return db.insertWithOnConflict(TABLE_CONTAGEM, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }
    
    fun getAllContagens(): List<Produto> {
        val contagemList = mutableListOf<Produto>()
        val selectQuery = "SELECT * FROM $TABLE_CONTAGEM"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                val produto = Produto(
                    codigo = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CODIGO)),
                    ean = cursor.getString(cursor.getColumnIndexOrThrow(COL_EAN)),
                    nome = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOME)),
                    qtd = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_QTD))
                )
                contagemList.add(produto)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return contagemList
    }
    
    // --- Parametros Functions ---

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
}
