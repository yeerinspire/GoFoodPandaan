package com.example.gofoodpandaan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.gofoodpandaan.UiaplikasiIKI.HomeFragment
import com.example.gofoodpandaan.UiaplikasiIKI.NotifikasiFragment
import com.example.gofoodpandaan.UiaplikasiIKI.PesananFragment
import com.example.gofoodpandaan.UiaplikasiIKI.ProfilFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> {
                supportFragmentManager.beginTransaction().replace(R.id.framehome,
                    HomeFragment()
                ).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_pesanan -> {
                supportFragmentManager.beginTransaction().replace(R.id.framehome,
                    PesananFragment()
                ).commit()
                return@OnNavigationItemSelectedListener true

            }
            R.id.nav_notif -> {
                supportFragmentManager.beginTransaction().replace(R.id.framehome,
                    NotifikasiFragment()
                ).commit()
                return@OnNavigationItemSelectedListener true

            }

            R.id.nav_profil -> {
                supportFragmentManager.beginTransaction().replace(R.id.framehome,
                    ProfilFragment()
                ).commit()
                return@OnNavigationItemSelectedListener true

            }

        }

        false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val navView: BottomNavigationView = findViewById(R.id.nav_viewhome)

        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        moveToFragment(HomeFragment())
    }


    private fun moveToFragment(fragment: Fragment)
    {
        val fragmentTrans = supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.framehome, fragment)
        fragmentTrans.commit()
    }
}