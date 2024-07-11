package com.iyr.ian.ui.signup.signup_selector

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.iyr.ian.R
import com.iyr.ian.databinding.ActivitySignUpSelectorBinding
import com.iyr.ian.ui.signup.with_email.SignUpWithEmailActivity
import com.iyr.ian.ui.signup.with_phone.SignUpWithPhoneActivity
import com.iyr.ian.utils.UIUtils.statusBarTransparent


class SignUpSelectorActivity : AppCompatActivity() {

    //private lateinit var mPresenter: SignUpPresenter
    private lateinit var binding: ActivitySignUpSelectorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //    mPresenter = SignUpPresenter(this, this)

        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_sign_up_selector)

        statusBarTransparent()
        setupUI()
        updateUI()
    }

    private fun setupUI() {

        binding.backArrows.setOnClickListener {
            onBackPressed()
        }


        binding.signupWithPhoneButton.setOnClickListener {

            val intent = Intent(this@SignUpSelectorActivity, SignUpWithPhoneActivity::class.java)
        //    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }


        binding.signupWithEmailButton.setOnClickListener {

            val intent = Intent(this@SignUpSelectorActivity, SignUpWithEmailActivity::class.java)
   //         intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }

    private fun updateUI() {


    }


    fun goBack() {
 //       finish()
    }


}
