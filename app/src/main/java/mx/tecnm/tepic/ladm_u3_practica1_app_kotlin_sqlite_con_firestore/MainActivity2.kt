package mx.tecnm.tepic.ladm_u3_practica1_app_kotlin_sqlite_con_firestore

import android.content.ContentValues
import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main2.*

class MainActivity2 : AppCompatActivity() {
    var baseSQLite = BaseDatos(this,"muebleria",null,1)
    var baseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        var extra = intent.extras
        var bandera = extra!!.getBoolean("bandera")

        var idActualizar = extra!!.getString("idActualizar")!!

        if (bandera){
            cargarFirestore(extra,idActualizar)

        }else{
            cargarSQLite(extra,idActualizar)
        }

        btnActualizar.setOnClickListener {
            if (bandera){
                actualizarFirestore(idActualizar)

            }else{
                actualizarSQLite(idActualizar)
            }
        }
        btnRegresar.setOnClickListener {
            finish()
        }
    }

    private fun actualizarFirestore(idNoSQL: String) {
        baseFirestore.collection("Apartado")
            .document(idNoSQL)
            .update("NombreCliente",txtActNombre.text.toString(), "Producto",
                txtActProducto.text.toString(), "Precio",txtActPrecio.text.toString().toFloat())
            .addOnSuccessListener {
                alerta("EXITO SE ACTUALIZO")
            }
            .addOnFailureListener {
                mensaje("ERROR NO SE PUDO ACTUALIZAR")
            }
    }

    private fun actualizarSQLite(idSQL: String) {
        try {
            var transaccion = baseSQLite.writableDatabase
            var valores = ContentValues()
            valores.put("NombreCliente",txtActNombre.text.toString())
            valores.put("Producto",txtActProducto.text.toString())
            valores.put("Precio",txtActPrecio.text.toString().toFloat())

            var resultado = transaccion.update("Apartado",valores,"IdApartado=?", arrayOf(idSQL))
            if (resultado > 0){
                alerta("Se ACTUALIZO correctamente")
                //finish()
            }else{
                mensaje("ERROR! no se pudo actualizar el dato")
            }
            transaccion.close()
        }catch (err: SQLiteException){
            mensaje(err.message!!)
        }
    }

    private fun cargarFirestore(extra: Bundle, idActualizar: String) {
        baseFirestore.collection("Apartado")
            .document(idActualizar)
            .get()
            .addOnSuccessListener {
                txtActNombre.setText(it.getString("NombreCliente"))
                txtActProducto.setText(it.getString("Producto"))
                txtActPrecio.setText(it.get("Precio").toString())
            }
            .addOnFailureListener {
                mensaje("ERROR: ${it.message!!}")
            }
    }

    private fun cargarSQLite(extra: Bundle, idActualizar: String) {
        try{
            var select = baseSQLite.readableDatabase
            var SQL = "SELECT * FROM Apartado WHERE IdApartado = '${idActualizar}'"

            var cursor = select.rawQuery(SQL,null)
            if (cursor.moveToFirst()){
                txtActNombre.setText(cursor.getString(1))
                txtActProducto.setText(cursor.getString(2))
                txtActPrecio.setText(cursor.getString(3))

            }else{
                mensaje("ERROR!, No se pudieron recuperar los datos de ${idActualizar}")
            }
            select.close()
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
        Toast.makeText(this,s, Toast.LENGTH_LONG).show()
    }
}