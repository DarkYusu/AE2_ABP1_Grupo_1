package com.aplicaciones_android.pruebaaplicacion.network

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TokenProvider {
    private var appContext: Context? = null
    private val listeners = mutableSetOf<() -> Unit>()
    fun initialize(context: Context) {
        appContext = context.applicationContext
        // Notificar el estado de autenticación al inicializar
        notifyAuthChanged()
    }

    fun getToken(): String? {
        val ctx = appContext ?: return null
        val prefs = ctx.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString("jwt_token", null)
    }

    fun getTokenType(): String? {
        val ctx = appContext ?: return null
        val prefs = ctx.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString("token_type", null)
    }

    fun getUsername(): String? {
        val ctx = appContext ?: return null
        val prefs = ctx.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString("username", null)
    }

    fun setUsername(username: String) {
        val ctx = appContext ?: return
        val prefs = ctx.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("username", username).apply()
        notifyAuthChanged()
    }

    fun clear() {
        val ctx = appContext ?: return
        val prefs = ctx.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("jwt_token").remove("token_type").remove("username").apply()
        notifyAuthChanged()
    }

    fun addAuthStateListener(listener: () -> Unit) {
        listeners.add(listener)
    }

    fun removeAuthStateListener(listener: () -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyAuthChanged() {
        listeners.forEach { runCatching { it() } }
    }
}

object RetrofitClient {
    private const val BASE_URL = "https://test-poke-jwt-341597259134.europe-west1.run.app/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Interceptor que añade Authorization si hay token y limpia token si la respuesta es 401/403
    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val token = TokenProvider.getToken()
        val request: Request = if (!token.isNullOrEmpty()) {
            val tokenType = TokenProvider.getTokenType()?.takeIf { it.isNotEmpty() } ?: "Bearer"
            // Normalizar tipo de token para usar 'Bearer' (algunas APIs son sensibles a la capitalización)
            val normalizedType = tokenType.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            val headerValue = "$normalizedType $token"
            val newRequest: Request = original.newBuilder()
                .header("Authorization", headerValue)
                .build()
            newRequest
        } else {
            original
        }

        val response: Response = chain.proceed(request)
        if (response.code == 401 || response.code == 403) {
            Log.w("RetrofitClient", "Auth failed with ${response.code}, clearing stored token")
            TokenProvider.clear()
        }
        response
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: NewsApi by lazy {
        retrofit.create(NewsApi::class.java)
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }
}
