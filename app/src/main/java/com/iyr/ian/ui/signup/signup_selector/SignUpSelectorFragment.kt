package com.iyr.ian.ui.signup.signup_selector

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.iyr.ian.R
import com.iyr.ian.databinding.ActivitySignUpSelectorBinding


class SignUpSelectorFragment : Fragment() {

    private lateinit var binding: ActivitySignUpSelectorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            DataBindingUtil.setContentView(requireActivity(), R.layout.activity_sign_up_selector)

        setupUI()
    }

    private fun setupUI() {

        binding.signupWithPhoneButton.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_signUpWithPhoneFragment)
        }

        binding.signupWithEmailButton.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_signUpWithEmailActivity)
        }
    }



}
