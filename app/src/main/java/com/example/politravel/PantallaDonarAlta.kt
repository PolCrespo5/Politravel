package com.example.politravel

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
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
import java.io.FileWriter
import java.util.*

class PantallaDonarAlta: AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map:GoogleMap
    private lateinit var paquets: MutableList<Paquet>
    private lateinit var btnAfegirFoto: ImageView
    private var listRecorregut = mutableListOf<Lloc>()
    private var polylinesList = mutableListOf<Polyline>()
    private lateinit var lblNumDies: TextView
    private var imatge = ""

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_donar_alta)
        val intent = intent
        paquets= intent.getSerializableExtra(PaquetsConstants.LLISTA_ALTA) as MutableList<Paquet>
        createFragment()
        afegirFoto()
        contadorDies()
        veureRuta()
        crearPaquet(paquets)
    }

    private fun afegirFoto() {
        btnAfegirFoto = findViewById(R.id.ImgPaquetAdd)
        btnAfegirFoto.setOnClickListener() {
            val intent = Intent(this, PantallaImatges::class.java)
            intent.putExtra(PaquetsConstants.ALTA_IMATGES, ArrayList(paquets))
            getResult.launch(intent)
        }
    }

    private fun contadorDies() {
        val btnDisminuir = findViewById<Button>(R.id.Btn_less)
        val btnAugmentar = findViewById<Button>(R.id.Btn_more)
        lblNumDies = findViewById(R.id.LblCountDies)
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

    private fun veureRuta() {
        val lblPopUpWindowItinerari = findViewById<TextView>(R.id.PopUpWindowItinerariAlta)
        lblPopUpWindowItinerari.setOnClickListener() {
            val alertDialog = AlertDialog.Builder(this)
            val rowList: View = layoutInflater.inflate(R.layout.popup_window_itinerari, null)
            val listView = rowList.findViewById(R.id.LstItinerariRecorreguts) as ListView
            val adapter = ItinerariRecoregutAdapter(this, R.layout.llista_itinerari_recorregut, listRecorregut)
            listView.adapter = adapter
            adapter.notifyDataSetChanged()
            alertDialog.setView(rowList)
            val dialog = alertDialog.create()
            dialog.show()

        }
    }
    private fun crearPaquet(paquets: MutableList<Paquet>) {
        val btnAddPaquet = findViewById<Button>(R.id.BtnAddPaquet)
        val editTextName = findViewById<TextInputLayout>(R.id.EtNamePaquet)
        val radioGroupTransport = findViewById<RadioGroup>(R.id.radioGroupTransport)
        var transport = ""
        lblNumDies = findViewById(R.id.LblCountDies)

        btnAddPaquet.setOnClickListener() {
            var nomImatge = ""
            if (imatge.isNotEmpty()) {
                nomImatge = imatge.substring(imatge.lastIndexOf("/")+1)
            }
            val radioButtonSeleccionado = findViewById<RadioButton>(radioGroupTransport.checkedRadioButtonId)
            if (radioButtonSeleccionado != null) {
                var idTransport = radioButtonSeleccionado.id
                when (idTransport) {
                    1 -> transport = "airplane"
                    2 -> transport = "boat"
                    3 -> transport = "bus"
                    else -> transport = "bus"
                }
            }
            if (validacions(nomImatge, editTextName.editText?.text.toString(), transport, listRecorregut)) {
                paquets.add( Paquet(nomImatge, editTextName.editText?.text.toString(), Integer.valueOf(lblNumDies.text.toString()), transport, listRecorregut))
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
        var markers = arrayListOf<Marker?>()
        var addMarker = false
        map = googleMap
        map.clear()
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
                    listRecorregut.add(Lloc(countryName, location.longitude, location.latitude))
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
                        if (selectedMarker.title.equals(listRecorregut[position].nomLloc)) {
                            listRecorregut.removeAt(position)
                            if (position == 0) {
                                polylinesList[position].remove()
                                polylinesList.removeAt(position)
                            } else if (position > 0 && position != markers.size - 1) {
                                polylinesList[position - 1].remove()
                                polylinesList[position].remove()
                                polylinesList.removeAt(position-1)
                                var ultimLloc = (Lloc(markers[position + 1]?.title!!, markers[position + 1]!!.position.longitude, markers[position + 1]!!.position.latitude))
                                var penultimLloc = (Lloc(markers[position - 1]?.title!!, markers[position - 1]!!.position.longitude, markers[position - 1]!!.position.latitude))
                                createLines(ultimLloc, penultimLloc, position)
                            } else{
                                polylinesList[position - 1].remove()
                                polylinesList.removeAt(position-1)
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

    private fun createLines(ultimLloc: Lloc, penultimLloc: Lloc, position: Int) {
        var coordinatesUltimLloc = LatLng(ultimLloc.lat, ultimLloc.long)
        var coordinatesPenultimLloc = LatLng(penultimLloc.lat, penultimLloc.long)
        val pattern = listOf(
            Dot(), Gap(10f), Dash(50f), Gap(10f)
        )
        if (position == -1) {
            polylinesList.add(map.addPolyline(PolylineOptions()
                .add(coordinatesUltimLloc)
                .add(coordinatesPenultimLloc)
            ))
            polylinesList.last().pattern = pattern
        } else {
            polylinesList[position-1] = map.addPolyline(PolylineOptions()
                .add(coordinatesUltimLloc)
                .add(coordinatesPenultimLloc)
            )
            polylinesList[position-1].pattern = pattern
        }
    }

    private fun changeDestinations() {
        val llocInicial = findViewById<TextView>(R.id.LblLlocInicial)
        val llocFinal = findViewById<TextView>(R.id.LblLlocFinal)

        if (listRecorregut.size == 1) {
            llocInicial.text = listRecorregut.first().nomLloc
            llocFinal.text = ""
        } else {
            llocFinal.text = listRecorregut.last().nomLloc
            //last -1
            createLines(listRecorregut.last(), listRecorregut[listRecorregut.size - 2], -1)
        }
    }
}