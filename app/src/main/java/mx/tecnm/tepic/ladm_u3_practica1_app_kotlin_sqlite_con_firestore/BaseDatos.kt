package mx.tecnm.tepic.ladm_u3_practica1_app_kotlin_sqlite_con_firestore

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BaseDatos(
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE Apartado(IdApartado INTEGER PRIMARY KEY AUTOINCREMENT,NombreCliente VARCHAR(200),Producto VARCHAR(200),Precio FLOAT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }
}
