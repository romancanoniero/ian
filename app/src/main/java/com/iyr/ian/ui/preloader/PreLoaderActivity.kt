package com.iyr.ian.ui.preloader

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.iyr.ian.ui.preloader.pager_adapter.pager_adapter.PreloaderPagerAdapter
import com.iyr.ian.R
import com.iyr.ian.databinding.ActivityPreloaderBinding
import com.iyr.ian.ui.login.LoginActivity
import com.iyr.ian.ui.signup.signup_selector.SignUpSelectorActivity
import com.iyr.ian.utils.UIUtils.statusBarTransparent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PreLoaderActivity : AppCompatActivity() {

    private lateinit var pagerAdapter: PreloaderPagerAdapter
    private lateinit var binding: ActivityPreloaderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_preloader)
        statusBarTransparent()
        setupUI()
    }


    private fun setupUI() {

        var keepScrolling = true
        var lastPageShown = 0

        pagerAdapter = PreloaderPagerAdapter(this, supportFragmentManager, this.lifecycle)
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
                if (position<lastPageShown)
                {
                    keepScrolling = false
                }
                lastPageShown = position
            }
        })

        binding.buttonContinue.setOnClickListener {
            binding.pager.setCurrentItem(pagerAdapter.fragments.size - 1, true)
        }

        binding.signInButton.setOnClickListener {
            val intent = Intent(this@PreLoaderActivity, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        binding.signUpButton.setOnClickListener {
            val intent = Intent(this@PreLoaderActivity, SignUpSelectorActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
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
