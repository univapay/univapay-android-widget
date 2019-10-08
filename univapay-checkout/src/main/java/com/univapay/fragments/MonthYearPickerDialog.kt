package com.univapay.fragments

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.DatePicker
import android.widget.NumberPicker
import com.univapay.utils.DateValidator
import com.univapay.utils.bind
import java.text.SimpleDateFormat
import java.util.*

/**
 * MonthYear PickerDialog class
 */
class MonthYearPickerDialog : DialogFragment() {
    private var mOnDateSetListener: ((datePicker: DatePicker?, year: Int?, month: Int?, day: Int?) -> Unit)? = null
    private var mSelectedMonth = -1
    private var mSelectedYear = -1

    fun setDateSetListener(listener: (datePicker: DatePicker?, year: Int?, month: Int?, day: Int?) -> Unit) {
        this.mOnDateSetListener = listener
    }

    fun setSelectedMonth(month: Int) {
        mSelectedMonth = month
    }

    fun setSelectedYear(year: Int) {
        mSelectedYear = year
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)
        // Get the layout inflater
        val inflater = activity!!.layoutInflater

        val cal = Calendar.getInstance()

        val dateDialog: View = inflater.inflate(com.univapay.R.layout.dialog_month_year, null)

        val currentYear = cal.get(Calendar.YEAR)

        val month = if (mSelectedMonth != -1) mSelectedMonth else cal.get(Calendar.MONTH) + 1
        val year = if (mSelectedYear != -1) mSelectedYear else currentYear

        // Get Month List
        val displayMonth = ArrayList<String>()
        val monthDate = SimpleDateFormat("MMM", Locale.getDefault())
        (0..11).mapTo(displayMonth) {idx->
            cal.set(Calendar.MONTH, idx)
            monthDate.format(cal.time)
        }

        val mNumberPickerMonth: NumberPicker? = bind(dateDialog, com.univapay.R.id.number_picker_month)
        val mNumberPickerYear: NumberPicker? = bind(dateDialog, com.univapay.R.id.number_picker_year)

        fun getMaxYear(): Int{
            return currentYear + DateValidator.MAXIMUM_VALID_YEAR_DIFFERENCE
        }

        // Set Month Picker
        mNumberPickerMonth?.apply {
            displayedValues = displayMonth.toTypedArray()
            minValue = 1
            maxValue = 12
            value = month
        }


        // Set Year Picker
        mNumberPickerYear?.apply {
            minValue = currentYear
            maxValue = getMaxYear()
            value = year
            wrapSelectorWheel = false
        }

        builder.setTitle(com.univapay.R.string.checkout_carddetails_expiry_dialog_title)
                .setView(dateDialog)
                // Add action buttons
                .setPositiveButton(com.univapay.R.string.checkout_carddetails_expiry_dialog_positive) { _, _->
                    mOnDateSetListener?.let{listener->
                        listener(null, mNumberPickerYear?.value, mNumberPickerMonth?.value, 0)
                    }
                }
                .setNegativeButton(com.univapay.R.string.checkout_carddetails_expiry_dialog_negative) { _, _->
                    this@MonthYearPickerDialog.dialog.cancel()
                }
        return builder.create()
    }

}
