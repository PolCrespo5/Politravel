package com.example.politravel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.FileReader
import java.io.FileWriter

class PantallaLlistat: AppCompatActivity() {

    var dialogConfirmacion: AlertDialog? = null

    private fun getPaquets(): MutableList<Paquet> {
        val jsonFilePath = "$filesDir/JSONS/paquets.json"
        val jsonFile = FileReader(jsonFilePath)
        val listPaquets = object: TypeToken<MutableList<Paquet>>() {}.type
        val paquets: MutableList<Paquet> = Gson().fromJson(jsonFile, listPaquets)

        return paquets
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_llistat)

        val paquets = getPaquets()
        val lstPaquets = findViewById<RecyclerView>(R.id.LstItems)
        val fabButton = findViewById<View>(R.id.fab)

        val adapter = AgenciaViatgesAdapter(this, paquets)
        lstPaquets.hasFixedSize()
        lstPaquets.layoutManager = LinearLayoutManager(this)
        lstPaquets.adapter = adapter

        fabButton.setOnClickListener {
            val intent = Intent(this, PantallaDonarAlta::class.java)
            intent.putExtra(PaquetsConstants.LLISTA_ALTA, ArrayList(paquets))
            startActivity(intent)
        }

        adapter.setOnClickListener() {
            val paquet = paquets[lstPaquets.getChildAdapterPosition(it)]
            val intent = Intent(this, PantallaDetall::class.java)
            intent.putExtra(PaquetsConstants.PAQUET_VIATGE, paquet)
            startActivity(intent)
        }

        adapter.setOnLongClickListener() {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(paquets[lstPaquets.getChildAdapterPosition(it)].nom)
            builder.setMessage("Que vols fer amb el pàquet?")
            builder.setPositiveButton("EDITAR") { dialog, which ->
                val paquet = paquets[lstPaquets.getChildAdapterPosition(it)]
                val intent = Intent(this, PantallaModificacio::class.java)
                intent.putExtra(PaquetsConstants.PAQUET_VIATGE, paquet)
                startActivity(intent)
            }
            builder.setNegativeButton("ELIMINAR") { dialog, which ->
                val builderConfirmacion = AlertDialog.Builder(ContextThemeWrapper(this, R.style.Theme_Politravel))
                builderConfirmacion.setTitle("Confirmar eliminació")
                builderConfirmacion.setMessage("Estas segur de que vols eliminar l'element seleccionat?")
                builderConfirmacion.setPositiveButton("Sí") { dialogConfirmacion, whichConfirmacion ->
                    paquets.removeAt(lstPaquets.getChildAdapterPosition(it))
                    deletePaquet(this, paquets)
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this, "Paquet eliminat correctament", Toast.LENGTH_SHORT).show()
                }
                builderConfirmacion.setNegativeButton("No") { dialogConfirmacion, whichConfirmacion ->
                    dialogConfirmacion.dismiss()
                }
                dialogConfirmacion = builderConfirmacion.create()
                dialogConfirmacion?.show()

            }
            val dialog = builder.create()
            dialog.show()
            false
        }
    }
    private fun deletePaquet(context: Context, paquets: MutableList<Paquet>) {
        val jsonFilePath = context.filesDir.toString() + "//JSONS/paquets.json"
        val jsonFile = FileWriter(jsonFilePath)
        var gson = Gson()
        var jsonElement = gson.toJson(paquets)
        jsonFile.write(jsonElement)
        jsonFile.close()
    }
    override fun onBackPressed() {
        if (dialogConfirmacion?.isShowing == true) {
            dialogConfirmacion?.dismiss()
        } else {
            super.onBackPressed()
        }
    }
}