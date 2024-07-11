package com.iyr.ian.ui.signup.dialogs

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.iyr.ian.databinding.SignupErrorDialogBinding
import com.iyr.ian.ui.base.OnConfirmationButtonsListener
import com.iyr.ian.utils.UIUtils.handleTouch


class SignupErrorDialog(context: Context, errorKeys: ArrayList<String>) :

    AlertDialog(context) {
    private lateinit var binding: SignupErrorDialogBinding
    private val mThisDialog: SignupErrorDialog
    private val mContext: Context
    private lateinit var mActivity: Activity
    private var mCallback: OnConfirmationButtonsListener? = null
    private var mTitle: String? = null
    private var mLegend: String? = null
    private var mButton1Caption: String? = null
    private var mButton2Caption: String? = null


    init {
        mContext = context
        mThisDialog = this
        binding = SignupErrorDialogBinding.inflate(layoutInflater)

//        mDialoglayout = binding.root

        this.setView(binding.root)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window!!.attributes)
        lp.gravity = Gravity.CENTER
        window!!.attributes = lp
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.setGravity(Gravity.CENTER)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding.acceptButton.setOnClickListener {
            context.handleTouch()
            dismiss()
        }

        binding.reqFieldDisplayName.isVisible = errorKeys.contains("display_name")
        binding.reqFieldFirstName.isVisible = errorKeys.contains("first_name")
        binding.reqFieldLastName.isVisible = errorKeys.contains("last_name")
        binding.reqFieldEmailOrPhoneNumberEmpty.isVisible =
            errorKeys.contains("email_or_phone_number_empty")
        binding.reqFieldEmailOrPhoneNumberUnknownFormat.isVisible =
            errorKeys.contains("email_or_phone_number_unknown_format")
        binding.reqFieldPasswordInvalid.isVisible = errorKeys.contains("password_invalid")
        binding.reqFieldSecurityCodeInvalid.isVisible = errorKeys.contains("security_code_invalid")
        binding.reqFieldSecurityCodeIsEmpty.isVisible = errorKeys.contains("security_code_empty")


    }
}