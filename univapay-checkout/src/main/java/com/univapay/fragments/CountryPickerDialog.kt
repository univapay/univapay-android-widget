package com.univapay.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ListView
import com.univapay.adapters.CountryListAdapter
import com.univapay.models.Country
import com.univapay.utils.bind
import java.util.*

/**
 * Country PickerDialog
 */
class CountryPickerDialog : DialogFragment(), Comparator<Country> {

    private lateinit var mCountryListAdapter: CountryListAdapter
    private lateinit var mCountryList: MutableList<Country>
    private lateinit var mCountrySelectListener: (Country) -> Unit
    private lateinit var mContext: Context

    private lateinit var mSelectedCountryCode: String
    private lateinit var mCountryDialog: View

    private var mCountryListView: ListView? = null


    fun setCountrySelectListener(listener: (Country) -> Unit) {
        this.mCountrySelectListener = listener
    }

    private fun loadAllCountries() {
        if (!this::mCountryList.isInitialized) {
            try {
                mCountryList = ArrayList()
                Locale.getISOCountries().mapTo(mCountryList, {countryCode->
                    val objCountry = Locale("", countryCode)
                    val country = Country()
                    country.code = objCountry.country
                    country.name = objCountry.displayCountry
                    country
                })

                Collections.sort(mCountryList, this)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mCountryDialog = inflater.inflate(com.univapay.R.layout.dialog_country_picker, container, true)
        arguments?.let {args->
            val dialogTitle = args.getString("dialogTitle")
            if (!TextUtils.isEmpty(dialogTitle)) {
                dialog.setTitle(dialogTitle)
            } else {
                val window = dialog.window
                window?.requestFeature(Window.FEATURE_NO_TITLE)
            }

            val width = resources.getDimensionPixelSize(com.univapay.R.dimen.cp_dialog_width)
            val height = resources.getDimensionPixelSize(com.univapay.R.dimen.cp_dialog_height)
            dialog.window.setLayout(width, height)
        }
        loadAllCountries()

        val mSearchEditText: EditText? = bind(mCountryDialog, com.univapay.R.id.country_code_picker_text_view_search)
        mCountryListView = bind(mCountryDialog, com.univapay.R.id.country_code_picker_list_view)

        // Set Country List Adapter
        mCountryListAdapter = CountryListAdapter(context, mCountryList)
        mCountryListView?.adapter = mCountryListAdapter

        if (!TextUtils.isEmpty(mSelectedCountryCode)) {
            mCountryList.forEachIndexed{idx, country->
                if (country.code.equals(mSelectedCountryCode, ignoreCase = true)) {
                    mCountryListView?.setItemChecked(idx, true)
                    mCountryListView?.setSelection(idx)
                }
            }
        }

        mCountryListView?.onItemClickListener = AdapterView.OnItemClickListener{ _, _, position, _ ->
            val country = mCountryList[position]
            mCountrySelectListener(country)
        }

        mSearchEditText?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {
                search(s.toString())
            }
        })

        return mCountryDialog
    }

    @SuppressLint("DefaultLocale")
    private fun search(text: String) {
        val mCountryListView: ListView? = bind(mCountryDialog, com.univapay.R.id.country_code_picker_list_view)
        mCountryList.mapIndexedNotNull{idx, country->
            if(country.name.toLowerCase(Locale.ENGLISH).startsWith(text.toLowerCase())){
                idx
            } else null
        }.firstOrNull()?.let{idx->
            mCountryListView?.setItemChecked(idx, true)
            mCountryListView?.setSelection(idx)
        }
    }

    override fun compare(lhs: Country, rhs: Country): Int {
        return lhs.name.compareTo(rhs.name)
    }

    fun setSelectedCountry(countryCode: String) {
        mSelectedCountryCode = countryCode
    }

    fun getUserCountryInfo(context: Context): Country {
        mContext = context
        loadAllCountries()
        val countryIsoCode: String
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (telephonyManager.simState != TelephonyManager.SIM_STATE_ABSENT) {
            countryIsoCode = telephonyManager.simCountryIso

            mCountryList.indices.forEach{i->
                val country = mCountryList[i]
                if (country.code.equals(countryIsoCode, ignoreCase = true)) {
                    country.flag = getFlagResId(country.code)
                    return country
                }
            }
        }
        return defaultCountry()
    }

    fun getSelectedCountry(): Country?{
        val idx = mCountryListView?.checkedItemPosition
        return mCountryList.filterIndexed {index, _ ->
            index == idx
        }.firstOrNull()
    }

    private fun defaultCountry(): Country {
        val country = Country()
        country.code = "JP"
        country.flag = com.univapay.R.drawable.flag_jp
        return country
    }

    private fun getFlagResId(drawable: String?): Int {
        try {
            return mContext.resources
                    .getIdentifier("flag_" + drawable?.toLowerCase(Locale.ENGLISH), "drawable",
                            mContext.packageName)
        } catch (e: Exception) {
            e.printStackTrace()
            return -1
        }

    }

    companion object {

        /**
         * To support show as dialog
         */
        fun newInstance(): CountryPickerDialog {
            return newInstance("")
        }

        fun newInstance(dialogTitle: String): CountryPickerDialog {
            val picker = CountryPickerDialog()
            val bundle = Bundle()
            bundle.putString("dialogTitle", dialogTitle)
            picker.arguments = bundle
            return picker
        }
    }
}

