package com.iyr.ian.ui.preloader

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.iyr.ian.BuildConfig
import com.iyr.ian.R
import com.iyr.ian.databinding.ActivityPreloaderBinding
import com.iyr.ian.ui.login.LoginActivity
import com.iyr.ian.ui.preloader.pager_adapter.pager_adapter.PreloaderPagerAdapter
import com.iyr.ian.ui.signup.signup_selector.SignUpSelectorActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PreLoaderFragment : Fragment() {

    private lateinit var pagerAdapter: PreloaderPagerAdapter
    private lateinit var binding: ActivityPreloaderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(requireActivity(), R.layout.activity_preloader)
        //statusBarTransparent()
        setupUI()
    }


    private fun setupUI() {

        var keepScrolling = true
        var lastPageShown = 0

        pagerAdapter = PreloaderPagerAdapter(
            requireContext(),
            requireActivity().supportFragmentManager,
            this.lifecycle
        )
        binding.pager.offscreenPageLimit = 5
        binding.pager.adapter = pagerAdapter
        binding.dotsIndicator.setViewPager2(binding.pager)


        binding.pager.setOnTouchListener { _, _ ->
            keepScrolling = false
            false
        }
        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == pagerAdapter.fragments.size - 1) {
                    binding.buttonContinue.visibility = View.GONE
                    binding.signInButton.visibility = View.VISIBLE
                    binding.signUpButton.visibility = View.VISIBLE
                } else {
                    binding.buttonContinue.visibility = View.VISIBLE
                    binding.signInButton.visibility = View.GONE
                    binding.signUpButton.visibility = View.GONE
                }
                if (position < lastPageShown) {
                    keepScrolling = false
                }
                lastPageShown = position
            }
        })

        binding.buttonContinue.setOnClickListener {
            binding.pager.setCurrentItem(pagerAdapter.fragments.size - 1, true)
        }

        binding.signInButton.setOnClickListener {


            if (BuildConfig.NAVIGATION_HOST_MODE?.toBoolean() == true) {
                var navController = findNavController()

                navController.navigate(R.id.action_preLoaderFragment_to_loginFragment2)
            } else {
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
        }

        binding.signUpButton.setOnClickListener {
            if (BuildConfig.NAVIGATION_HOST_MODE?.toBoolean() == true) {
                var navController = findNavController()
                navController.navigate(R.id.action_preLoaderFragment_to_signUpFragment)
            } else {
                val intent = Intent(requireContext(), SignUpSelectorActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
        }

        // Inicio la animacion de cambio de pagina
        lifecycleScope.launch {
            while (keepScrolling && binding.pager.currentItem < pagerAdapter.fragments.size) {
                binding.pager.currentItem = withContext(Dispatchers.IO) {
                    Thread.sleep(2000)
                    return@withContext binding.pager.currentItem + 1
                }
            }
        }

    }


}
