package com.iyr.ian.ui.signup.signup_completed

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.iyr.ian.dao.models.User
import com.iyr.ian.databinding.ActivitySignUpCompletedBinding
import com.iyr.ian.ui.login.LoginActivity
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.UIUtils.statusBarTransparent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SignUpCompletedActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpCompletedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //    mPresenter = SignUpPresenter(this, this)

        binding = ActivitySignUpCompletedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        statusBarTransparent()
        getIntentData()
        setupUI()
        updateUI()
    }


    private var originalObject: User? = null
    private lateinit var currentObject: User
    private fun getIntentData() {
        if (intent.hasExtra("data_object")) {
            currentObject = Gson().fromJson(
                intent.getStringExtra("data_object"), User::class.java
            )
            originalObject = Gson().fromJson(
                intent.getStringExtra("data_object"), User::class.java
            )
        }
    }


    private fun setupUI() {

        binding.continueButton.setOnClickListener {

            handleTouch()
            lifecycleScope.launch(Dispatchers.Main) {
                val intent = Intent(this@SignUpCompletedActivity, LoginActivity::class.java)
                val bundle = Bundle()
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.putExtras(bundle)
                startActivity(intent)
                finish()
            }

        }

        /*

                binding.signupWithPhoneButton.setOnClickListener {

                    val intent = Intent(this@SignUpCompletedActivity, SignUpWithPhoneActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }


                binding.signupWithEmailButton.setOnClickListener {

                    val intent = Intent(this@SignUpCompletedActivity, SignUpWithEmailActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }

        */
    }

    private fun updateUI() {


    }


}
