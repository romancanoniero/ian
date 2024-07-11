package com.iyr.fewtouchs.ui.views.signup.subscription_plan

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.iyr.ian.dao.models.User
import com.iyr.ian.databinding.ActivitySubscriptionCurrentPlanBinding
import com.iyr.ian.ui.setup.press_or_tap_setup.PressOrTapSetupActivity
import com.iyr.ian.utils.UIUtils.statusBarTransparent


class SubscriptionCurrentPlanActivity : AppCompatActivity() {

    private var originalObject: User? = null
    private lateinit var currentObject: User
 //   private lateinit var mPresenter: SignUpPresenter
    private lateinit var binding: ActivitySubscriptionCurrentPlanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //    mPresenter = SignUpPresenter(this, this)

        binding = ActivitySubscriptionCurrentPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)
//            DataBindingUtil.setContentView(this, R.layout.activity_sign_up_selector)

        statusBarTransparent()
        getIntentData()
        setupUI()
        updateUI()
    }

    private fun getIntentData() {
        if (intent.hasExtra("data_object")) {
            currentObject = Gson().fromJson(
                intent.getStringExtra("data_object"),
                User::class.java
            )
            originalObject = Gson().fromJson(
                intent.getStringExtra("data_object"),
                User::class.java
            )
        }

    }

    private fun setupUI() {

        binding.backArrows.setOnClickListener {
            onBackPressed()
        }


        binding.nextButton.setOnClickListener {

            val bundle = Bundle()
            bundle.putString("data_object", Gson().toJson(currentObject))
            val intent =
                Intent(this@SubscriptionCurrentPlanActivity, PressOrTapSetupActivity::class.java)
            intent.putExtras(bundle)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
/*
        binding.signupWithPhoneButton.setOnClickListener {

            val intent = Intent(this@SubscriptionCurrentPlanActivity, SignUpWithPhoneActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }


        binding.signupWithEmailButton.setOnClickListener {

            val intent = Intent(this@SubscriptionCurrentPlanActivity, SignUpWithEmailActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

 */
    }

    private fun updateUI() {


    }


    fun goBack() {
        finish()
    }


}
