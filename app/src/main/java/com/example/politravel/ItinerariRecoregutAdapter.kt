package com.example.politravel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class ItinerariRecoregutAdapter (context: Context, val layout: Int, val recorregut: MutableList<Lloc>):
    ArrayAdapter<Lloc>(context, layout, recorregut)
{
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view: View

        if(convertView != null) {
            view = convertView
        } else {
            view = LayoutInflater.from(context).inflate(layout, parent, false)
        }

        bindVideojoc(view, recorregut[position])

        return view
    }

    fun bindVideojoc(view: View, nomRecorregut: Lloc){
        val lblNom = view.findViewById(R.id.nomLlocRecorregut) as TextView
        val lblLat = view.findViewById(R.id.LblLat) as TextView
        val lblLong = view.findViewById(R.id.LblLong) as TextView
        lblNom.text = nomRecorregut.nomLloc
        lblLat.text = "Lat:\n ${nomRecorregut.lat}"
        lblLong.text = "Long:\n ${nomRecorregut.long}"
    }

}