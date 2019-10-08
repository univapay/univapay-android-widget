package com.univapay.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class KonbiniSpinnerAdapter(context: Context?, val resource: Int): ArrayAdapter<KonbiniAdapter>(context, resource, KonbiniAdapter.values()){

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createItemView(position, parent)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createItemView(position, parent)
    }

    private fun createItemView(position: Int, parent: ViewGroup?): View{
        val view = LayoutInflater.from(context).inflate(resource, parent, false)

        val image: ImageView = view.findViewById(com.univapay.R.id.konbini_spinner_element_image)
        val text: TextView = view.findViewById(com.univapay.R.id.konbini_spinner_element_text)

        val element = KonbiniAdapter.values()[position]

        image.setImageResource(element.imageResId)
        text.setText(element.nameResId)

        return view
    }
}
