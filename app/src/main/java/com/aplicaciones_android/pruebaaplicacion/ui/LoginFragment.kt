package com.aplicaciones_android.pruebaaplicacion.ui

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.aplicaciones_android.pruebaaplicacion.R
import com.aplicaciones_android.pruebaaplicacion.network.RetrofitClient
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

class LoginFragment : Fragment() {

    private lateinit var etUser: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        etUser = view.findViewById(R.id.etUser)
        etPassword = view.findViewById(R.id.etPassword)
        btnLogin = view.findViewById(R.id.btnLogin)
        progressBar = view.findViewById(R.id.progressBar)

        // Asegurar que al pulsar "Done/Enter" en el teclado se dispare el login y se oculte el teclado
        try {
            etPassword.imeOptions = EditorInfo.IME_ACTION_DONE
            etPassword.setOnEditorActionListener { _, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                    hideKeyboard()
                    btnLogin.performClick()
                    true
                } else false
            }
        } catch (_: Exception) {
        }

        btnLogin.setOnClickListener {
            val user = etUser.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (user.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Introduce usuario y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Ocultar el teclado antes de iniciar la petición de login
            hideKeyboard()
            doLogin(user, password)
        }

        return view
    }

    // Oculta el teclado virtual si está visible
    private fun hideKeyboard() {
        try {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            // quitar focus de los campos para que no vuelvan a abrir el teclado
            try { etUser.clearFocus() } catch (_: Exception) {}
            try { etPassword.clearFocus() } catch (_: Exception) {}
            val token = requireActivity().currentFocus?.windowToken ?: requireView().windowToken
            imm.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS)
        } catch (_: Exception) {
            // ignore
        }
    }

    private fun doLogin(user: String, password: String) {
        progressBar.visibility = View.VISIBLE
        btnLogin.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.authApi.login(user, password)
                // Guardar token en SharedPreferences
                val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                prefs.edit().putString("jwt_token", response.accessToken).apply()
                prefs.edit().putString("token_type", response.tokenType).apply()
                // Guardar username a través de TokenProvider para notificar listeners
                com.aplicaciones_android.pruebaaplicacion.network.TokenProvider.setUsername(user)
                prefs.edit().putBoolean("open_create", true).apply()

                Toast.makeText(requireContext(), "Login correcto", Toast.LENGTH_SHORT).show()
                // En lugar de recrear la Activity, pedir a MainActivity que muestre la lista inmediatamente
                (activity as? com.aplicaciones_android.pruebaaplicacion.MainActivity)?.let { act ->
                    act.runOnUiThread {
                        try {
                            act.navigateToList()
                        } catch (_: Throwable) {
                        }
                    }
                }
            } catch (e: HttpException) {
                // Intentar parsear el body para mostrar error detallado
                val errorBody = e.response()?.errorBody()?.string()
                val message = try {
                    if (!errorBody.isNullOrEmpty()) {
                        val json = JSONObject(errorBody)
                        if (json.has("detail")) json.getJSONArray("detail").toString() else json.toString()
                    } else {
                        e.localizedMessage
                    }
                } catch (_: Exception) {
                    e.localizedMessage
                }
                Toast.makeText(requireContext(), "Error al iniciar sesión: $message", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error al iniciar sesión: ${'$'}{e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true
            }
        }
    }
}
