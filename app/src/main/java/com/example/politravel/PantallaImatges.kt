package com.example.politravel

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader

class PantallaImatges : AppCompatActivity() {

    private lateinit var listImatges: Array<File>
    private lateinit var listPaquets: MutableList<Paquet>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_imatges)

        val intent = intent
        listPaquets = intent.getSerializableExtra(PaquetsConstants.ALTA_IMATGES) as MutableList<Paquet>
        val imatges = getImages()
        val lstImatges = findViewById<RecyclerView>(R.id.ListImg)

        val adapter = ImatgesDestinsAdapter(this, imatges as Array<File>, listPaquets)
        adapter.setOnClickListener(){
            val imatge = imatges[lstImatges.getChildAdapterPosition(it)]
            val intent = Intent(this, PantallaDonarAlta::class.java)
            intent.putExtra(PaquetsConstants.IMATGES_ALTA, imatge.absolutePath)
            setResult(RESULT_OK, intent)
            finish()
        }
        lstImatges.hasFixedSize()
        lstImatges.layoutManager = GridLayoutManager(this,2)
        lstImatges.adapter = adapter
    }

    private fun getImages(): Array<out File>? {
        val path: String = filesDir.absolutePath + "/IMGS/"
        val directory = File(path)
        listImatges = imageReader(directory) as Array<File>

        return listImatges
    }

    private fun imageReader(directory: File): Array<out File>? {
        val fileList: ArrayList<File> = ArrayList()
        val listAllFiles = directory.listFiles()

        if (listAllFiles != null && listAllFiles.isNotEmpty()) {
            for (currentFile in listAllFiles) {
                if (currentFile.name.endsWith(".jpg")) {
                    // File Name
                    Log.e("downloadFileName", currentFile.name)
                    fileList.add(currentFile.absoluteFile)
                }
            }
            Log.w("fileList", "" + fileList.size)
        }
        return listAllFiles
    }
}