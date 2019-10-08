package com.univapay.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.univapay
        .models.Country
import com.univapay
        .utils.bind
import java.util.*

/**
 * Country Adapter Class
 */
class CountryListAdapter(private val mContext: Context?, private val mCountryList: List<Country>) : BaseAdapter() {
    private val mInflater: LayoutInflater = this.mContext?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private fun getResId(drawableName: String): Int {

        try {
            val res = com.univapay.R.drawable::class.java
            val field = res.getField(drawableName)
            return field.getInt(null)
        } catch (e: Exception) {
            Log.e("CountryListAdapter", "Failure to get drawable appId.", e)
        }

        return -1
    }

    override fun getCount(): Int {
        return mCountryList.size
    }

    override fun getItem(arg0: Int): Any? {
        return null
    }

    override fun getItemId(arg0: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var viewCountryItem = convertView
        val countryViewModel: CountryViewModel
        val country = mCountryList[position]

        if (convertView == null) {
            countryViewModel = CountryViewModel()
            viewCountryItem = mInflater.inflate(com.univapay.R.layout.item_country, null)
            // set References
            countryViewModel.textView = bind(viewCountryItem, com.univapay.R.id.row_title)
            countryViewModel.imageView = bind(viewCountryItem, com.univapay.R.id.row_icon)

            viewCountryItem.tag = countryViewModel
        } else {
            countryViewModel = viewCountryItem?.tag as CountryViewModel
        }

        // Flag
        val drawableName = "flag_" + country.code.toLowerCase(Locale.ENGLISH)
        val drawableId = getResId(drawableName)
        country.flag = drawableId

        // Update viewModel
        try {
            countryViewModel.imageView?.setImageResource(drawableId)
        } catch (e: Exception) {
            // Flag is not available in the Resources
        }

        countryViewModel.textView?.text = country.name

        return viewCountryItem!!
    }

    inner class CountryViewModel {
        var textView: TextView? = null
        var imageView: ImageView? = null
    }
}
