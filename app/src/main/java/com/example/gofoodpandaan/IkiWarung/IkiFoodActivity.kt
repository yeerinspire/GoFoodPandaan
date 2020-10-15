package com.example.gofoodpandaan.IkiWarung

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.alfanshter.udinlelangfix.Session.SessionManager
import com.example.gofoodpandaan.HomeActivity
import com.example.gofoodpandaan.R
import com.example.gofoodpandaan.IkiWarung.FoodDelivery.FoodFragment
import com.example.gofoodpandaan.IkiWarung.FoodDelivery.history.HistoryFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.jetbrains.anko.startActivity

class IkiFoodActivity : AppCompatActivity() {
    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.frame,
                    FoodFragment()
                ).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.frame,
                    HistoryFragment()
                ).commit()
                return@OnNavigationItemSelectedListener true

            }
            R.id.navigation_notifications -> {

             }

        }

        false
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity<HomeActivity>()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        lateinit var sessionManager: SessionManager

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ikifood)

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