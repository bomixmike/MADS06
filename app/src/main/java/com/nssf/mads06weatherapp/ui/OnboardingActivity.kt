package com.nssf.mads06weatherapp.ui

import android.Manifest
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import com.github.appintro.AppIntroPageTransformerType
import com.nssf.mads06weatherapp.MainActivity
import com.nssf.mads06weatherapp.R
import com.nssf.mads06weatherapp.utils.Utility

class OnboardingActivity : AppIntro() {
    private var colorSkyBlueOnboarding = Color.parseColor("#4191ed");
    private var backPressedTime: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addSlide(
            AppIntroFragment.createInstance(
            title = "Check Real-Time Weather",
            description = "This Weather in One Convenient place",
            imageDrawable = R.drawable.undraw_weather,
            titleColorRes = R.color.colored_black_87,
            descriptionColorRes = R.color.colored_black_87,
            titleTypefaceFontRes = R.font.arbutus_slab,
            descriptionTypefaceFontRes = R.font.arbutus_slab
        ))

        addSlide(AppIntroFragment.createInstance(
            title = "Get Potential Weather",
            description = "View weather in one easy place. Know what to expect on the go as well as get tips on how to deal with whats coming your way",
            imageDrawable = R.drawable.weather_notification,
            titleColorRes = R.color.colored_black_87,
            descriptionColorRes = R.color.colored_black_87,
            titleTypefaceFontRes = R.font.arbutus_slab,
            descriptionTypefaceFontRes = R.font.arbutus_slab

        ))

        addSlide(AppIntroFragment.createInstance(
            title = "Get the Weather and Stay Safe",
            description = "With the sun, rain, storms etc comes challenges, keep your day going with our special tips",
            imageDrawable = R.drawable.weather_tip,
            titleColorRes = R.color.colored_black_87,
            descriptionColorRes = R.color.colored_black_87,
            titleTypefaceFontRes = R.font.arbutus_slab,
            descriptionTypefaceFontRes = R.font.arbutus_slab
        ))

        setTransformer(AppIntroPageTransformerType.Depth)
        showStatusBar(false)
        setNextArrowColor(colorSkyBlueOnboarding)
        setColorDoneText(colorSkyBlueOnboarding)
        setColorSkipButton(colorSkyBlueOnboarding)
        setIndicatorColor(colorSkyBlueOnboarding,Color.LTGRAY)

        requestLocPermission()
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        openMainActivity()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        openMainActivity()
    }

    private fun openMainActivity(){
        Utility.setOnboardingPreferences(this,true)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
        if(backPressedTime + 2000 > System.currentTimeMillis()){
            super.onBackPressed()
            finish()
        }else{
            Toast.makeText(this,"Tap Again to Exit",Toast.LENGTH_LONG).show()
        }
        backPressedTime = System.currentTimeMillis()
    }

    private fun requestLocPermission(){
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION), 92
        )
    }
}