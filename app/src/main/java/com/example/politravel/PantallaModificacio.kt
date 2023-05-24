package com.example.politravel

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.FileReader
import java.io.FileWriter
import java.util.*


class PantallaModificacio: AppCompatActivity(), OnMapReadyCallback {

    private lateinit var paquets: MutableList<Paquet>
    private var paquetModificant: Int? = null
    private lateinit var btnAfegirFoto: ImageView
    private lateinit var btnSavePaquet: Button
    private lateinit var llocInicial: TextView
    private lateinit var llocFinal: TextView
    private var imatge = ""
    private lateinit var lblNumDies: TextView
    private lateinit var paquet: Paquet
    private var polylineList = mutableListOf<Polyline>()
    private lateinit var map: GoogleMap
    private var markers = arrayListOf<Marker?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_donar_alta)

        val intent = intent
        paquet = intent.getSerializableExtra(PaquetsConstants.PAQUET_VIATGE) as Paquet
        getPaquets()
        paquetModificant = searchPaquet()
        insertarImatgePaquet()
        val editTextName = findViewById<TextInputLayout>(R.id.EtNamePaquet)
        val radioGroupTransport = findViewById<RadioGroup>(R.id.radioGroupTransport)
        llocInicial = findViewById(R.id.LblLlocInicial)
        llocFinal = findViewById(R.id.LblLlocFinal)
        inicializateDestinations()
        btnSavePaquet = findViewById(R.id.BtnAddPaquet)
        btnSavePaquet.text = "SAVE TRAVEL"
        lblNumDies = findViewById(R.id.LblCountDies)
        editTextName.editText?.text = Editable.Factory.getInstance().newEditable(paquet.nom)
        when (paquet.mitjaTransport) {
            "airplane" ->  (radioGroupTransport.getChildAt(0) as RadioButton).isChecked = true
            "boat" -> (radioGroupTransport.getChildAt(1) as RadioButton).isChecked = true
            "bus" -> (radioGroupTransport.getChildAt(2) as RadioButton).isChecked = true
        }
        lblNumDies.text = paquet.numDies.toString()

        //Funcionalitats un cop posat la informació del paquet
        createFragment()
        contadorDies()
        afegirFoto()
        veureRuta()
        guardarPaquet()
    }

    private fun searchPaquet(): Int {
        var index = -1
        var arrayCount = 0
        for (_paquet in paquets) {
            if (_paquet.nom == paquet.nom) {
                index = arrayCount
            }
            arrayCount++
        }
        return index
    }

    private fun guardarPaquet() {
        val editTextName = findViewById<TextInputLayout>(R.id.EtNamePaquet)
        var transport = ""
        lblNumDies = findViewById(R.id.LblCountDies)
        val radioGroupTransport = findViewById<RadioGroup>(R.id.radioGroupTransport)
        btnSavePaquet.setOnClickListener() {
            var nomImatge = ""
            if (imatge.isNotEmpty()) {
                nomImatge = imatge.substring(imatge.lastIndexOf("/")+1)
            }
            val radioButtonSeleccionado = findViewById<RadioButton>(radioGroupTransport.checkedRadioButtonId)
            var idTransport = radioButtonSeleccionado.id
            when (idTransport) {
                1 -> transport = "airplane"
                2 -> transport = "boat"
                3 -> transport = "bus"
                else -> transport = "bus"
            }
            if (validacions(nomImatge, editTextName.editText?.text.toString(), transport, paquet.recorregutPaquet)) {
                paquets[paquetModificant!!] = Paquet(nomImatge, editTextName.editText?.text.toString(), Integer.valueOf(lblNumDies.text.toString()), transport, paquet.recorregutPaquet)
                savePaquet(this, paquets)
                val intent = Intent(this, PantallaLlistat::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
    private fun validacions(nomImatge: String, nomPaquet: String, transport: String, listRecorregut: MutableList<Lloc>
    ): Boolean {
        var validated = false
        if (nomImatge.isNullOrEmpty()) {
            Toast.makeText(this, "No has introduit cap imatge del paquet", Toast.LENGTH_LONG).show()
            btnAfegirFoto.background = ContextCompat.getDrawable(this, R.drawable.border_error)
        } else {
            if (nomPaquet.isNullOrEmpty()) {
                Toast.makeText(this, "No has introduit el nom del paquet", Toast.LENGTH_LONG).show()
                val til = findViewById<View>(R.id.EtNamePaquet) as TextInputLayout
                til.error = "Falta aquest camp"
            }
            else {
                if (transport.isNullOrEmpty()) {
                    Toast.makeText(this, "No has introduit cap transport", Toast.LENGTH_LONG).show()
                } else {
                    if (listRecorregut.size < 2) {
                        Toast.makeText(this, "No has introduit el recorregut", Toast.LENGTH_LONG).show()
                    }
                    else {
                        validated = true
                    }
                }
            }
        }
        return validated
    }

    private fun savePaquet(context: Context, paquets: MutableList<Paquet>) {
        val jsonFilePath = context.filesDir.toString() + "//JSONS/paquets.json"
        val jsonFile = FileWriter(jsonFilePath)
        var gson = Gson()
        var jsonElement = gson.toJson(paquets)
        jsonFile.write(jsonElement)
        jsonFile.close()
    }

    private fun createFragment() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapDonarAlta) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val btnAddMarker = findViewById<FloatingActionButton>(R.id.add_marker_fab)
        val btnDeleteMarker = findViewById<FloatingActionButton>(R.id.delete_marker_fab)
        var addMarker = false
        map = googleMap
        map.clear()
        generateLinesPoints()
        map.setOnMapClickListener { latlng ->
            if (markers.isNotEmpty() && !addMarker) {
                markers.last()?.remove()
            }
            addMarker = false
            val location = LatLng(latlng.latitude, latlng.longitude)
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses: List<Address> = geocoder.getFromLocation(latlng.latitude, latlng.longitude, 1) as List<Address>
            if (addresses.isNotEmpty()) {
                val countryName: String = addresses[0].countryName
                markers.add(map.addMarker(MarkerOptions().position(location).title(countryName))!!)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 5f))
                btnAddMarker.setOnClickListener() {
                    paquet.recorregutPaquet.add(Lloc(countryName, location.longitude, location.latitude))
                    changeDestinations()
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 1f))
                    addMarker = true
                }
            }
            map.setOnMarkerClickListener() {
                var selectedMarker = it
                btnDeleteMarker.setOnClickListener() {
                    var position = 0
                    var found = false
                    while (!found && position != markers.size) {
                        if (selectedMarker.title.equals(paquet.recorregutPaquet[position].nomLloc)) {
                            paquet.recorregutPaquet.removeAt(position)
                            if (position == 0) {
                                polylineList[position].remove()
                                polylineList.removeAt(position)
                            } else if (position > 0 && position != markers.size - 1) {
                                polylineList[position - 1].remove()
                                polylineList[position].remove()
                                polylineList.removeAt(position-1)
                                var ultimLloc = (Lloc(markers[position + 1]?.title!!, markers[position + 1]!!.position.longitude, markers[position + 1]!!.position.latitude))
                                var penultimLloc = (Lloc(markers[position - 1]?.title!!, markers[position - 1]!!.position.longitude, markers[position - 1]!!.position.latitude))
                                createLines(ultimLloc, penultimLloc, position)
                            } else{
                                polylineList[position - 1].remove()
                                polylineList.removeAt(position-1)
                            }
                            found = true
                        }
                        position++
                    }
                    selectedMarker.remove()
                    markers.remove(selectedMarker)
                    changeDestinations()
                }
                false
            }
        }
    }
    private fun inicializateDestinations() {
        llocInicial.text = paquet.recorregutPaquet.first().nomLloc
        llocFinal.text = paquet.recorregutPaquet.last().nomLloc
    }
    private fun changeDestinations() {
        if (paquet.recorregutPaquet.size == 1) {
            llocInicial.text = paquet.recorregutPaquet.first().nomLloc
            llocFinal.text = ""
        } else {
            llocFinal.text = paquet.recorregutPaquet.last().nomLloc
            //last -1
            createLines(paquet.recorregutPaquet.last(), paquet.recorregutPaquet[paquet.recorregutPaquet.size - 2], -1)
        }
    }

    private fun createLines(ultimLloc: Lloc, penultimLloc: Lloc, position: Int) {
        var coordinatesUltimLloc = LatLng(ultimLloc.lat, ultimLloc.long)
        var coordinatesPenultimLloc = LatLng(penultimLloc.lat, penultimLloc.long)
        val pattern = listOf(
            Dot(), Gap(10f), Dash(50f), Gap(10f)
        )
        if (position == -1) {
            polylineList.add(map.addPolyline(PolylineOptions()
                .add(coordinatesUltimLloc)
                .add(coordinatesPenultimLloc)
            ))
            polylineList.last().pattern = pattern
        } else {
            polylineList[position-1] = map.addPolyline(PolylineOptions()
                .add(coordinatesUltimLloc)
                .add(coordinatesPenultimLloc)
            )
            polylineList[position-1].pattern = pattern
        }
    }

    private fun generateLinesPoints() {
        val pattern = listOf(
            Dot(), Gap(10f), Dash(50f), Gap(10f)
        )
        for (parada in paquet.recorregutPaquet) {
            var proximaParada = paquet.recorregutPaquet.indexOf(parada)+1
            var latlngParadaActual = LatLng(parada.lat, parada.long)
            if (paquet.recorregutPaquet.size > proximaParada) {
                var latlngProximaParada = LatLng(paquet.recorregutPaquet[proximaParada].lat, paquet.recorregutPaquet[proximaParada].long)
                polylineList.add(map.addPolyline(PolylineOptions()
                    .add(latlngProximaParada)
                    .add(latlngParadaActual)
                ))
                polylineList.last().pattern = pattern
            }
            map.addMarker(
                MarkerOptions()
                    .position(latlngParadaActual)
                    .title(parada.nomLloc)
            )
            markers.add(map.addMarker(MarkerOptions().position(latlngParadaActual).title(parada.nomLloc))!!)
        }
    }

    private fun veureRuta() {
        val lblPopUpWindowItinerari = findViewById<TextView>(R.id.PopUpWindowItinerariAlta)
        lblPopUpWindowItinerari.setOnClickListener() {
            val alertDialog = AlertDialog.Builder(this)
            val rowList: View = layoutInflater.inflate(R.layout.popup_window_itinerari, null)
            val listView = rowList.findViewById(R.id.LstItinerariRecorreguts) as ListView
            val adapter = ItinerariRecoregutAdapter(this, R.layout.llista_itinerari_recorregut, paquet.recorregutPaquet)
            listView.adapter = adapter
            adapter.notifyDataSetChanged()
            alertDialog.setView(rowList)
            val dialog = alertDialog.create()
            dialog.show()

        }
    }

    private fun insertarImatgePaquet() {
        btnAfegirFoto = findViewById(R.id.ImgPaquetAdd)
        imatge = filesDir.absolutePath + "/IMGS/" + paquet.imatge
        val bitmap = BitmapFactory.decodeFile(imatge)
        btnAfegirFoto?.setImageBitmap(bitmap)
    }

    private fun contadorDies() {
        val btnDisminuir = findViewById<Button>(R.id.Btn_less)
        val btnAugmentar = findViewById<Button>(R.id.Btn_more)
        var numDies = Integer.parseInt(lblNumDies.text.toString())

        btnDisminuir.setOnClickListener() {
            if (numDies > 1) {
                numDies--
            }
            lblNumDies.text = numDies.toString()
        }
        btnAugmentar.setOnClickListener() {
            numDies++
            lblNumDies.text = numDies.toString()
        }
    }

    private fun afegirFoto() {
        btnAfegirFoto = findViewById(R.id.ImgPaquetAdd)
        btnAfegirFoto.setOnClickListener() {
            val intent = Intent(this, PantallaImatges::class.java)
            intent.putExtra(PaquetsConstants.ALTA_IMATGES, ArrayList(paquets))
            getResult.launch(intent)
        }
    }
    private fun getPaquets() {
        val jsonFilePath = "$filesDir/JSONS/paquets.json"
        val jsonFile = FileReader(jsonFilePath)
        val listPaquets = object: TypeToken<MutableList<Paquet>>() {}.type
        paquets = Gson().fromJson(jsonFile, listPaquets)
    }

    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult())
        {
            if (it.resultCode == RESULT_OK) {
                btnAfegirFoto = findViewById(R.id.ImgPaquetAdd)
                btnAfegirFoto.background = null
                imatge = it.data?.getStringExtra(PaquetsConstants.IMATGES_ALTA)!!
                val bitmap = BitmapFactory.decodeFile(imatge)
                btnAfegirFoto?.setImageBitmap(bitmap)
            }
        }
}