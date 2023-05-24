package com.example.politravel

import android.content.Context
import android.graphics.BitmapFactory
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.bold
import androidx.recyclerview.widget.RecyclerView

class AgenciaViatgesAdapter (private val context: Context,
                             private val paquets: MutableList<Paquet>):
    RecyclerView.Adapter<AgenciaViatgesAdapter.PaquetsViewHolder>(),
    View.OnClickListener,
    View.OnLongClickListener
{
    private val layout = R.layout.llista_viatges
    private var clickListener: View.OnClickListener? = null
    private var longClickListener: View.OnLongClickListener? = null


    class PaquetsViewHolder(val view: View):
        RecyclerView.ViewHolder(view)
    {
        var imgPaquetImatge: ImageView
        var lblPaquetNom: TextView
        var lblNumDies: TextView
        var imgMitjaTransport: ImageView

        init {
            imgPaquetImatge = view.findViewById(R.id.ImgListPaquet)
            lblPaquetNom = view.findViewById(R.id.LblListNomPaquet)
            lblNumDies = view.findViewById(R.id.LblLitsNumDies)
            imgMitjaTransport = view.findViewById(R.id.LblLitsMitjaTransport)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaquetsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        view.setOnClickListener(this)
        view.setOnLongClickListener(this)
        return PaquetsViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaquetsViewHolder, position: Int) {
        val paquet = paquets[position]
        bindVideojoc(holder, paquet)
    }

    fun bindVideojoc(holder: PaquetsViewHolder, paquet: Paquet) {
        val ndTextView = TextView(context)

        val imatgePaquetPath = context.filesDir.toString() + "/IMGS/" + paquet.imatge
        val bitmapImatgePaquet = BitmapFactory.decodeFile(imatgePaquetPath)
        holder.imgPaquetImatge?.setImageBitmap(bitmapImatgePaquet)

        holder.lblPaquetNom?.text = paquet.nom

        val numDies = SpannableStringBuilder()
            .append(holder.lblNumDies?.text.toString())
            .bold { append(paquet.numDies.toString()) }
        ndTextView.text = numDies

        holder.lblNumDies?.text = ndTextView.text

        if (paquet.mitjaTransport == "airplane") {
            holder.imgMitjaTransport?.setImageResource(R.drawable.airplane)
        } else if(paquet.mitjaTransport == "bus") {
            holder.imgMitjaTransport?.setImageResource(R.drawable.bus)
        } else {
            holder.imgMitjaTransport?.setImageResource(R.drawable.boat)
        }
    }
    override fun getItemCount() = paquets.size

    fun setOnClickListener(listener: View.OnClickListener) {
        clickListener = listener
    }

    override fun onClick(view: View?) {
        clickListener?.onClick(view)
    }

    fun setOnLongClickListener(listener: View.OnLongClickListener) {
        longClickListener = listener
    }

    override fun onLongClick(view: View?): Boolean {
        longClickListener?.onLongClick(view)
        return false
    }
}
