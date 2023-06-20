package com.nssf.mads06weatherapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.nssf.mads06weatherapp.MainActivity
import com.nssf.mads06weatherapp.databinding.ActivitySplashBinding
import com.nssf.mads06weatherapp.utils.Utility

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val TIME_OUT: Long = 3000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        Handler().postDelayed({
                Log.e("onboarding", Utility.getOnboardingComplete(this).toString())
            if (Utility.getOnboardingComplete(this)){
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }else{
                startActivity(Intent(this, OnboardingActivity::class.java))
                finish()
            }

        }, TIME_OUT)



    }
}