package mx.tecnm.tepic.ladm_u3_practica1_app_kotlin_sqlite_con_firestore

import android.content.Intent
import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var baseSQLite = BaseDatos(this, "muebleria", null, 1)
    var baseFirestore = FirebaseFirestore.getInstance()
    var listaIDSQLite = ArrayList<String>()
    var listaIDFirestore = ArrayList<String>()
    var apartadosSQLite = ArrayList<String>()
    var apartadosFirestore = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cargarApartadosSQLite()
        cargarApartadosFirestore()

        btnInsertar.setOnClickListener {
            insertarSQLite()
        }
        btnSincronizar.setOnClickListener {
            sincronizar()
        }
    }

    private fun cargarApartadosSQLite() {
        try {
            var select = baseSQLite.readableDatabase
            var SQL = "SELECT * FROM Apartado"

            var cursor = select.rawQuery(SQL, null)
            listaIDSQLite.clear()
            apartadosSQLite.clear()
            if (cursor.moveToFirst()) {
                do {
                    var data =
                            "[ " + cursor.getString(1) + "]  -- " + cursor.getString(2) + " --- Precio: " + cursor.getString(
                                    3
                            )
                    apartadosSQLite.add(data)
                    listaIDSQLite.add(cursor.getInt(0).toString())
                } while (cursor.moveToNext())
            } else {
                apartadosSQLite.add("NO HAY APARTADOS")
            }
            select.close()
            listaSQLite.adapter =
                    ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, apartadosSQLite)
            listaSQLite.setOnItemClickListener { parent, view, position, id ->
                dialogoEliminaActualizaSQLite(position)
            }
        } catch (err: SQLiteException) {
            mensaje(err.message!!)
        }
    }

    private fun cargarApartadosFirestore() {
        baseFirestore.collection("Apartado")
                .addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
                    if(firebaseFirestoreException != null){
                        mensaje(firebaseFirestoreException.message!!)
                        return@addSnapshotListener
                    }

                    apartadosFirestore.clear()
                    listaIDFirestore.clear()

                    for (document in querySnapshot!!){
                        var cadena = "[${document.getString("NombreCliente")}] - Producto: ${document.get("Producto")} - Precio: ${document.get("Precio")}"
                        apartadosFirestore.add(cadena)
                        listaIDFirestore.add(document.id.toString())
                    }
                    listaFirestore.adapter = ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1,apartadosFirestore)
                    listaFirestore.setOnItemClickListener { parent, view, position, id ->
                        dialogoEliminaActualizaFirestore(position)
                    }
                }
    }

    private fun sincronizar() {
        try {
            var select = baseSQLite.readableDatabase
            var SQL = "SELECT * FROM Apartado"

            var nombre = ""
            var producto = ""
            var precio = 0f

            var cursor = select.rawQuery(SQL, null)

            if (cursor.moveToFirst()) {
                do {
                    nombre = cursor.getString(1)
                    producto = cursor.getString(2)
                    precio = cursor.getString(3).toFloat()

                    //Insercion en Firestore
                    var datosInsertar = hashMapOf(
                            "NombreCliente" to nombre,
                            "Producto" to producto,
                            "Precio" to precio
                    )

                    baseFirestore.collection("Apartado")
                            .add(datosInsertar)
                            .addOnSuccessListener {
                                alerta("SE INSERTO CORRECTAMENTE EN LA NUBE")
                                limpiarCampos()
                            }
                            .addOnFailureListener {
                                mensaje("ERROR: ${it.message!!}")
                            }

                } while (cursor.moveToNext())
                eliminarTodosSQLite()
                cargarApartadosFirestore()
                select.close()
            } else {
                mensaje("NO HAY APARTADOS PARA SINCRONIZAR")
            }
        } catch (err: SQLiteException) {
            mensaje(err.message!!)
        }
    }

    private fun eliminarTodosSQLite() {
        try {
            var eliminar = baseSQLite.writableDatabase
            var SQL = "DELETE FROM Apartado"
            eliminar.execSQL(SQL)
            cargarApartadosSQLite()
            eliminar.close()
        } catch (err: SQLiteException) {
            mensaje(err.message!!)
        }
    }

    private fun dialogoEliminaActualizaFirestore(position: Int) {
        var idElegido = listaIDFirestore.get(position)
        AlertDialog.Builder(this).setTitle("ATENCION!!")
                .setMessage("QUE DESEAS HACER CON \n ${apartadosFirestore.get(position)}?")
                .setPositiveButton("ELIMINAR"){d, i->
                    eliminarFirestore(idElegido)
                }
                .setNeutralButton("ACTUALIZAR"){d,i->
                    var intent = Intent(this,MainActivity2::class.java)
                    intent.putExtra("idElegido",idElegido)
                    intent.putExtra("bandera",true)
                    startActivity(intent)
                }
                .setNegativeButton("CANCELAR"){d,i->}
                .show()
    }

    private fun dialogoEliminaActualizaSQLite(position: Int) {
        var idElegido = listaIDSQLite.get(position)
        AlertDialog.Builder(this).setTitle("ATENCION!!")
                .setMessage("QUE DESEAS HACER CON \n ${apartadosSQLite.get(position)}?")
                .setPositiveButton("ELIMINAR"){d, i->
                    eliminarSQLite(idElegido)
                }
                .setNeutralButton("ACTUALIZAR"){d,i->
                    var intent = Intent(this,MainActivity2::class.java)
                    intent.putExtra("idElegido",idElegido)
                    intent.putExtra("bandera",false)
                    startActivity(intent)
                }
                .setNegativeButton("CANCELAR"){d,i->}
                .show()
    }

    private fun eliminarFirestore(idElegido: String) {
        baseFirestore.collection("Apartado")
                .document(idElegido)
                .delete()
                .addOnFailureListener {
                    mensaje("ERROR! ${it.message!!}")
                }
                .addOnSuccessListener {
                    mensaje("SE ELIMINO CON EXITO")
                }
    }

    private fun insertarSQLite() {
        try {
            var insertar = baseSQLite.writableDatabase
            var SQL = "INSERT INTO Apartado VALUES(NULL,'${txtNombre.text.toString()}','${txtProducto.text.toString()}','${txtPrecio.text.toString().toFloat()}')"
            insertar.execSQL(SQL)
            cargarApartadosSQLite()
            limpiarCampos()
            insertar.close()
        }catch (err: SQLiteException){
            mensaje(err.message!!)
        }
    }

    private fun limpiarCampos() {
        txtNombre.setText("")
        txtProducto.setText("")
        txtPrecio.setText("")
    }

    private fun eliminarSQLite(idBorrar: String) {
        try {
            var eliminar = baseSQLite.writableDatabase
            var SQL = "DELETE FROM Apartado WHERE IdApartado = ${idBorrar}"
            eliminar.execSQL(SQL)
            cargarApartadosSQLite()
            eliminar.close()
        }catch (err: SQLiteException){
            mensaje(err.message!!)
        }
    }

    private fun mensaje(s: String) {
        AlertDialog.Builder(this)
                .setTitle("ATENCION")
                .setMessage(s)
                .setPositiveButton("OK"){
                    d,i->
                }
                .show()
    }

    private fun alerta(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    }

}