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
import com.aplicaciones_android.pruebaaplicacion.network.TokenProvider
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.aplicaciones_android.pruebaaplicacion.ui.LoginFragment


class MainActivity : AppCompatActivity() {
    //region Declaración de clase y variables
    private lateinit var viewModel: NewsViewModel

    // Handler y Runnable a nivel de clase para controlar el refresco periódicamente
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            try {
                viewModel.refresh()
            } catch (t: Throwable) {
                Log.w("MainActivity", "Error refreshing news", t)
            }
            handler.postDelayed(this, 3000)
        }
    }

    private var isAutoRefreshRunning = false

    private fun startAutoRefresh() {
        if (!isAutoRefreshRunning) {
            handler.post(updateRunnable)
            isAutoRefreshRunning = true
            Log.d("MainActivity", "Auto refresh started")
        }
    }

    private fun stopAutoRefresh() {
        if (isAutoRefreshRunning) {
            handler.removeCallbacks(updateRunnable)
            isAutoRefreshRunning = false
            Log.d("MainActivity", "Auto refresh stopped")
        }
    }

    private val authStateListener: () -> Unit = {
        runOnUiThread {
            val token = TokenProvider.getToken()
            val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
            val fabCreate = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabCreate)
            if (token.isNullOrEmpty()) {
                Log.d("MainActivity", "authStateListener: no token -> showing LoginFragment now")
                bottomNav.visibility = android.view.View.GONE
                fabCreate.visibility = android.view.View.GONE
                val loginHost = findViewById<android.view.View>(R.id.login_host)
                loginHost.visibility = android.view.View.VISIBLE
                supportFragmentManager.beginTransaction()
                    .replace(R.id.login_host, LoginFragment())
                    .commitNow()
                Log.d("MainActivity", "authStateListener: LoginFragment committed")
                // detener refresco si se estaba ejecutando
                stopAutoRefresh()
            } else {
                bottomNav.visibility = android.view.View.VISIBLE
                fabCreate.visibility = android.view.View.VISIBLE
                val loginHost = findViewById<android.view.View>(R.id.login_host)
                loginHost.visibility = android.view.View.GONE
                // Si acabamos de autenticarnos, asegurar que la lista de noticias se muestre
                try {
                    val current = supportFragmentManager.findFragmentById(R.id.fragment_container)
                    if (current !is ListNoticiasFragment) {
                        navigateToList()
                    }
                } catch (_: Throwable) {
                }
            }
        }
    }
    //endregion

    //region Ciclo de vida: onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Evitar que el layout se reajuste cuando aparece el teclado (prevenir desplazamiento de EditText)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Inicializar TokenProvider
        TokenProvider.initialize(applicationContext)
        TokenProvider.addAuthStateListener(authStateListener)

        // Inicialización de ViewModel temprano para evitar que el refresco automático acceda a null
        viewModel = androidx.lifecycle.ViewModelProvider(this).get(NewsViewModel::class.java)

        //region Inicialización de UI y navegación
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        val fabCreate = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabCreate)
        // Asignar listener del FAB aquí (antes estaba duplicado en otro lugar)
        fabCreate.setOnClickListener {
            Log.d("MainActivity", "FAB clicked -> opening CreateNoticiaFragment")
            val loginHost = findViewById<android.view.View>(R.id.login_host)
            loginHost.visibility = android.view.View.GONE
            stopAutoRefresh()
            replaceFragmentWithAnimation(CreateNoticiaFragment(), true)
        }

        // Leer token actual
        val token = TokenProvider.getToken()

        // Listener del bottom nav: manejar lista y logout
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_list -> {
                    val loginHost = findViewById<android.view.View>(R.id.login_host)
                    loginHost.visibility = android.view.View.GONE
                    replaceFragmentWithAnimation(ListNoticiasFragment())
                    startAutoRefresh()
                    true
                }
                R.id.nav_create -> {
                    Log.d("MainActivity", "nav_create selected -> opening CreateNoticiaFragment")
                    val loginHost = findViewById<android.view.View>(R.id.login_host)
                    loginHost.visibility = android.view.View.GONE
                    stopAutoRefresh()
                    // Añadir al backstack para mantener navegación coherente
                    replaceFragmentWithAnimation(CreateNoticiaFragment(), true)
                    true
                }
                R.id.nav_logout -> {
                    Log.d("MainActivity", "nav_logout selected -> performing logout")
                    performLogout()
                    true
                }
                else -> false
            }
        }

        // Manejar re-selección (ej., pulsar el mismo item otra vez)
        bottomNav.setOnItemReselectedListener { item ->
            if (item.itemId == R.id.nav_logout) {
                Log.d("MainActivity", "nav_logout reselected -> performing logout")
                performLogout()
            }
        }

        // Fallback: asignar un click directo a la vista del item del menú (algunas versiones/skins no llaman al listener si el item ya está seleccionado)
        bottomNav.post {
            try {
                val logoutView = bottomNav.findViewById<android.view.View>(R.id.nav_logout)
                logoutView?.setOnClickListener {
                    Log.d("MainActivity", "nav_logout view clicked (fallback) -> performing logout")
                    performLogout()
                }
            } catch (t: Throwable) {
                Log.w("MainActivity", "No se pudo asignar fallback click al nav_logout view: ${t.message}")
            }
        }

        // Fallback adicional: asignar listener directamente al MenuItem del BottomNavigationView
        try {
            val menuItem = bottomNav.menu.findItem(R.id.nav_logout)
            menuItem?.setOnMenuItemClickListener {
                Log.d("MainActivity", "nav_logout menuItem clicked -> performing logout (menuItem listener)")
                performLogout()
                true
            }
        } catch (t: Throwable) {
            Log.w("MainActivity", "No se pudo asignar menuItem click a nav_logout: ${t.message}")
        }

        // Si no hay token, mostrar LoginFragment en login_host y ocultar bottomNav
        if (token.isNullOrEmpty()) {
            bottomNav.visibility = android.view.View.GONE
            val loginHost = findViewById<android.view.View>(R.id.login_host)
            loginHost.visibility = android.view.View.VISIBLE
            supportFragmentManager.beginTransaction()
                .replace(R.id.login_host, LoginFragment())
                .commit()
            return
        }


        if (savedInstanceState == null) {
            // Forzar navegación síncrona a la lista en el arranque para evitar que la vista quede vacía
            navigateToList()
        }
        //endregion

        // Asegurar que al navegar con back se gestione el refresco si el fragment visible es la lista
        supportFragmentManager.addOnBackStackChangedListener {
            val current = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (current is ListNoticiasFragment) {
                startAutoRefresh()
            } else {
                stopAutoRefresh()
            }
        }
    }
    //endregion

    //region Navegación con animación
    private fun replaceFragmentWithAnimation(fragment: androidx.fragment.app.Fragment, addToBackStack: Boolean = false) {
        val tx = supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            .replace(R.id.fragment_container, fragment)
        if (addToBackStack) tx.addToBackStack(null)
        tx.commit()
    }
    //endregion

    override fun onDestroy() {
        super.onDestroy()
        TokenProvider.removeAuthStateListener(authStateListener)
        stopAutoRefresh()
    }

    // Acción centralizada de logout (detener refresh, borrar token, limpiar backstack y mostrar login)
    private fun performLogout() {
        Log.d("MainActivity", "performLogout() called")
        stopAutoRefresh()
        TokenProvider.clear()
        try {
            supportFragmentManager.popBackStackImmediate(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        } catch (_: Throwable) {
            // ignore
        }
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.visibility = android.view.View.GONE
        val fabCreate = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabCreate)
        fabCreate.visibility = android.view.View.GONE
        val loginHost = findViewById<android.view.View>(R.id.login_host)
        loginHost.visibility = android.view.View.VISIBLE
        Log.d("MainActivity", "performLogout: replacing with LoginFragment now")
        supportFragmentManager.beginTransaction()
            .replace(R.id.login_host, LoginFragment())
            .commitNow()
        Log.d("MainActivity", "performLogout: LoginFragment committed")
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
    }

    // Navegar explícitamente a la lista de noticias (usado por CreateNoticiaFragment)
    fun navigateToList() {
        runOnUiThread {
            try {
                // Asegurar visibilidad de los contenedores
                val loginHost = findViewById<android.view.View>(R.id.login_host)
                val fragmentContainer = findViewById<android.view.View>(R.id.fragment_container)
                loginHost.visibility = android.view.View.GONE
                fragmentContainer.visibility = android.view.View.VISIBLE

                // Limpiar back stack para evitar fragments antiguos que tapen la vista
                try {
                    supportFragmentManager.popBackStackImmediate(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
                } catch (_: Throwable) {
                    // ignore
                }

                // Reemplazar de forma síncrona el fragmento de lista
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ListNoticiasFragment())
                    .commitNow()

                // Actualizar la selección del bottom nav visualmente
                val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
                bottomNav.selectedItemId = R.id.nav_list

                // Arrancar refresco
                startAutoRefresh()
            } catch (t: Throwable) {
                Log.w("MainActivity", "navigateToList failed: ${t.message}")
            }
        }
    }
}
