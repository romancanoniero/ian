package com.iyr.ian.ui.signup.signup_completed

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import com.iyr.ian.R
import com.iyr.ian.dao.models.User
import com.iyr.ian.databinding.ActivitySignUpCompletedBinding
import com.iyr.ian.utils.UIUtils.handleTouch


class SignUpCompletedFragment : Fragment() {

    private lateinit var binding: ActivitySignUpCompletedBinding


    private val args : SignUpCompletedFragmentArgs by navArgs()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //    mPresenter = SignUpPresenter(this, this)
        binding =
            DataBindingUtil.setContentView(requireActivity(), R.layout.activity_sign_up_completed)

        getIntentData()
        setupUI()
        updateUI()
    }


    private var originalObject: User? = null
    private lateinit var currentObject: User
    private fun getIntentData() {
        if (args.bundle?.containsKey("data_object") == true) {
            currentObject = Gson().fromJson(
                args.bundle?.getString("data_object"), User::class.java
            )
            originalObject = Gson().fromJson(
                args.bundle?.getString("data_object"), User::class.java
            )
        }
    }


    private fun setupUI() {

        binding.continueButton.setOnClickListener {

            requireActivity().handleTouch()
            /*
            lifecycleScope.launch(Dispatchers.Main) {
                val intent = Intent(this@SignUpCompletedFragment, LoginActivity::class.java)
                val bundle = Bundle()
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.putExtras(bundle)
                startActivity(intent)
                finish()
            }
*/
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
