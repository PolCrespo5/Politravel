package com.example.politravel

import android.graphics.BitmapFactory
import android.location.Geocoder
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.bold
import androidx.core.text.scale
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*


class PantallaDetall: AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapDetail:GoogleMap
    private lateinit var paquet: Paquet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_detall)

        val intent = intent
        paquet = intent.getSerializableExtra(PaquetsConstants.PAQUET_VIATGE) as Paquet

        val imgPaquet = findViewById<ImageView>(R.id.ImgPaquet)
        val lblNomPaquet = findViewById<TextView>(R.id.LblNomPaquet)
        val imgTansport = findViewById<ImageView>(R.id.ImgTransport)
        val lblNumDies = findViewById<TextView>(R.id.LblNumDies)
        val lblLlocInicial = findViewById<TextView>(R.id.LblLlocInicial)
        val lblLlocFinal = findViewById<TextView>(R.id.LblLlocFinal)
        val lblPopupWindowItinerari = findViewById<TextView>(R.id.PopUpWindowItinerari)

        val paquetImagePath = filesDir.toString() + "/IMGS/" + paquet.imatge
        val bitmap = BitmapFactory.decodeFile(paquetImagePath)
        imgPaquet.setImageBitmap(bitmap)
        lblNomPaquet.text = paquet.nom

        when (paquet.mitjaTransport) {
            "airplane" -> imgTansport.setBackgroundResource(R.drawable.airplane)
            "boat" -> imgTansport.setBackgroundResource(R.drawable.boat)
            "bus" -> imgTansport.setBackgroundResource(R.drawable.bus)
        }


        val numDies = SpannableStringBuilder()
            .append(lblNumDies.text.toString() + "\n")
            .bold { scale(2.0F) { append(paquet.numDies.toString()) } }
        lblNumDies.text = numDies

        val llocInicial = SpannableStringBuilder()
            .append(lblLlocInicial.text.toString() + "\n")
            .bold { scale(2.0F) { append(paquet.recorregutPaquet.first().nomLloc) } }
        lblLlocInicial.text = llocInicial

        val llocFinal = SpannableStringBuilder()
            .append(lblLlocFinal.text.toString() + "\n")
            .bold { scale(2.0F) { append(paquet.recorregutPaquet.last().nomLloc) } }
        lblLlocFinal.text = llocFinal

        createFragment()

        lblPopupWindowItinerari.setOnClickListener() {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("ITINERARI DEL RECORREGUT")
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
    private fun createFragment() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapDetail) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mapDetail = googleMap
        createMarkers()
    }
    private fun createMarkers() {
        var i = 0
        var coordinatesLast = LatLng(0.0, 0.0)
        for (lloc in paquet.recorregutPaquet) {
            var coordinates = LatLng(lloc.lat, lloc.long)
            val marker = MarkerOptions().position(coordinates).title(lloc.nomLloc)
            mapDetail.addMarker(marker)
            if (i != 0) {
                createLines(coordinatesLast, coordinates)
            }
            coordinatesLast = coordinates
            i++
        }
    }
    private fun createLines(coordinatesLast: LatLng, coordinates: LatLng) {
        val polylineOptions = PolylineOptions()
            .add(coordinatesLast)
            .add(coordinates)
        val line = mapDetail.addPolyline(polylineOptions)
        val pattern = listOf(
            Dot(), Gap(10f), Dash(50f), Gap(10f)
        )
        line.pattern = pattern
    }

}