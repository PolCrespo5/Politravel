package com.example.politravel

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class ImatgesDestinsAdapter (private val context: Context,
                             private val imgsDestins: Array<File>,
                             private val listPaquets: MutableList<Paquet>):
    RecyclerView.Adapter<ImatgesDestinsAdapter.ImatgesViewHolder>(),
    View.OnClickListener
{
    private val layout = R.layout.llista_imatges
    private var clickListener: View.OnClickListener? = null


    class ImatgesViewHolder(val view: View):
        RecyclerView.ViewHolder(view)
    {
        var imgDestiImatge: ImageView

        init {
            imgDestiImatge = view.findViewById(R.id.ImgAdded)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImatgesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        view.setOnClickListener(this)
        return ImatgesViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImatgesViewHolder, position: Int) {
        val imgDesti = imgsDestins[position]
        bindVideojoc(holder, imgDesti)
    }

    fun bindVideojoc(holder: ImatgesViewHolder, imgDesti: File) {
        val imatgePaquet = imgDesti.absolutePath
        val bitmap = BitmapFactory.decodeFile(imatgePaquet)
        holder.imgDestiImatge?.setImageBitmap(bitmap)
    }
    override fun getItemCount() = imgsDestins.size

    fun setOnClickListener(listener: View.OnClickListener) {
        clickListener = listener
    }

    override fun onClick(view: View?) {
        clickListener?.onClick(view)
    }
}