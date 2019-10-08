package com.univapay.views

import android.content.Context
import android.support.annotation.StringRes
import android.support.v4.widget.TextViewCompat
import android.support.v7.widget.AppCompatButton
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout

sealed class ActionButtonStatus {
    object Add: ActionButtonStatus()
    object Next: ActionButtonStatus()
    object Pay: ActionButtonStatus()
    object Finish: ActionButtonStatus()
}

/**
 * Action Button Class
 */
class ButtonAction : LinearLayout{

    lateinit var mActionButton: AppCompatButton

    var isSubscription = false

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initializeViews(context)
    }

    constructor(context: Context) : super(context) {
        initializeViews(context)
    }

    private fun initializeViews(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(com.univapay.R.layout.action_button, this, true)
        mActionButton = findViewById(com.univapay.R.id.action_button)
    }

    override fun setOnClickListener(onClickListener: View.OnClickListener?) {
        mActionButton.setOnClickListener(onClickListener)
    }

    private fun setActionButtonText(@StringRes id: Int) {
        mActionButton.text = resources.getString(id)
    }

    private fun setActionButtonFinish() {
        // Remove Icon
        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(mActionButton, null, null, null, null)
    }

    fun setActionButtonStatus(actionButtonStatus: ActionButtonStatus) {
        when(actionButtonStatus){
            ActionButtonStatus.Next -> {
                setActionButtonText(com.univapay.R.string.checkout_button_next)
            }
            ActionButtonStatus.Pay -> {
                if (isSubscription) {
                    setActionButtonText(com.univapay.R.string.checkout_button_subscribe)
                } else {
                    setActionButtonText(com.univapay.R.string.checkout_button_pay)
                }
            }
            ActionButtonStatus.Finish -> {
                setActionButtonText(com.univapay.R.string.checkout_button_finish)
                setActionButtonFinish()
            }
            ActionButtonStatus.Add -> {
                setActionButtonText(com.univapay.R.string.checkout_button_add)
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        mActionButton.isEnabled = enabled
    }

    override fun isEnabled(): Boolean {
        return mActionButton.isEnabled
    }
}
