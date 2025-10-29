package com.aplicaciones_android.pruebaaplicacion

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.aplicaciones_android.pruebaaplicacion.ui.ListNoticiasFragment
import com.aplicaciones_android.pruebaaplicacion.ui.CreateNoticiaFragment
import com.aplicaciones_android.pruebaaplicacion.viewmodel.NewsViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: NewsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_list -> {
                    replaceFragmentWithAnimation(ListNoticiasFragment())
                    true
                }
                R.id.nav_create -> {
                    replaceFragmentWithAnimation(CreateNoticiaFragment())
                    true
                }
                else -> false
            }
        }
        // Mostrar el fragmento de lista por defecto
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_list
        }

        // Inicializar ViewModel
        viewModel = androidx.lifecycle.ViewModelProvider(this).get(NewsViewModel::class.java)

        // Actualización automática cada 3 segundos
        val handler = Handler(Looper.getMainLooper())
        val updateRunnable = object : Runnable {
            override fun run() {
                viewModel.refresh()
                handler.postDelayed(this, 3000)
            }
        }
        handler.post(updateRunnable)
    }

    private fun replaceFragmentWithAnimation(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}