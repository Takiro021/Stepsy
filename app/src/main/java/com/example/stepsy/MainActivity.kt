package com.example.stepsy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.stepsy.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {


    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navigateFragment(HomeFragment())
        // Navigating the fragments
        binding.bottomNavView.setOnItemSelectedListener {

            when(it.itemId){

                R.id.home -> navigateFragment(HomeFragment())
                R.id.statics -> navigateFragment(StaticsFragment())
                R.id.settings -> navigateFragment(SettingsFragment())

                else ->{

                }
            }
            true

            }
        }

    private fun navigateFragment(fragment: Fragment){

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()

    }

}