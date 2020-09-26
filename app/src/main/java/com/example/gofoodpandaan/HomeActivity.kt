package com.example.gofoodpandaan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.alfanshter.udinlelangfix.Session.SessionManager
import com.example.gofoodpandaan.ui.FoodDelivery.FoodFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                supportFragmentManager.beginTransaction().replace(R.id.frame,
                    FoodFragment()
                ).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {

            }
            R.id.navigation_notifications -> {

             }

        }

        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        lateinit var sessionManager: SessionManager

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        moveToFragment(FoodFragment())

    }


    private fun moveToFragment(fragment: Fragment)
    {
        val fragmentTrans = supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.frame, fragment)
        fragmentTrans.commit()
    }
}