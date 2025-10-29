//region Imports y declaración
package com.aplicaciones_android.pruebaaplicacion.repository

import android.util.Log
import com.aplicaciones_android.pruebaaplicacion.model.News
import com.aplicaciones_android.pruebaaplicacion.network.RetrofitClient
import com.aplicaciones_android.pruebaaplicacion.network.TokenProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
//endregion

//region Resultados para operaciones asíncronas
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
}
//endregion

//region Repositorio principal de noticias
class NewsRepository {
    private val api = RetrofitClient.api

    //region Obtener lista de noticias
    suspend fun fetchNews(user: String = TokenProvider.getUsername() ?: "demo"): Result<List<News>> = withContext(Dispatchers.IO) {
        try {
            val list: List<News> = api.getNews(user)
            Result.Success(list)
        } catch (e: HttpException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    //endregion

    //region Crear noticia
    suspend fun createNoticia(user: String, titulo: String, descripcion: String, fuente: String) {
        // Evitar llamadas si no hay token/username: lanzar excepción controlada
        val token = TokenProvider.getToken()
        val usernameStored = TokenProvider.getUsername()
        if (token.isNullOrEmpty() || usernameStored.isNullOrEmpty()) {
            throw IllegalStateException("No autenticado o no hay username. Inicia sesión para crear noticias.")
        }
        // Obtener el next_id antes de crear la noticia
        val nextId = withContext(Dispatchers.IO) {
            try {
                val response = api.getLastId(user)
                response.nextId
            } catch (e: Exception) {
                1 // fallback si falla
            }
        }
        val noticia = News(nextId, titulo, descripcion, fuente)
        withContext(Dispatchers.IO) {
            try {
                // Log debug: token present, username and token_type
                val tokenType = TokenProvider.getTokenType()
                Log.d("NewsRepository", "Creating noticia. userParam=$user, usernameStored=$usernameStored, tokenPresent=${!token.isNullOrEmpty()}, tokenType=$tokenType")

                // Usar username almacenado en el token (forzar que coincida con el subject)
                val userToUse = usernameStored
                Log.d("NewsRepository", "Calling API createNoticia with userPath=$userToUse")
                api.createNoticia(userToUse, noticia)
            } catch (e: HttpException) {
                // El servidor devolvió un estado HTTP (403, 401, 422, etc.). Loguear y retornar.
                try {
                    val err = e.response()?.errorBody()?.string()
                    Log.w("NewsRepository", "HTTP ${e.code()} error body: $err")
                } catch (ex: Exception) {
                    // ignore
                }
                Log.w("NewsRepository", "HttpException al crear noticia: ${e.code()} ${e.message}")
                throw e
            } catch (e: Exception) {
                Log.e("NewsRepository", "Exception creando noticia", e)
                throw e
            }
        }
    }
    //endregion

    //region Actualizar noticia
    suspend fun updateNoticia(user: String, id: Int, titulo: String, descripcion: String, fuente: String) {
        val noticia = News(id, titulo, descripcion, fuente)
        withContext(Dispatchers.IO) {
            api.updateNoticia(user, id, noticia)
        }
    }
    //endregion

    //region Eliminar noticia
    suspend fun deleteNoticia(user: String, id: Int) {
        withContext(Dispatchers.IO) {
            api.deleteNoticia(user, id)
        }
    }
    //endregion

    //region Obtener noticia por id (manejo de error 404)
    suspend fun getNoticiaById(user: String, id: Int): News? = withContext(Dispatchers.IO) {
        try {
            api.getNoticiaById(user, id)
        } catch (e: retrofit2.HttpException) {
            if (e.code() == 404) null else throw e
        } catch (e: Exception) {
            null
        }
    }
    //endregion

    //region Patch noticia
    suspend fun patchNoticia(user: String, id: Int, titulo: String?, descripcion: String?, fuente: String?) {
        val map = mutableMapOf<String, Any?>()
        if (titulo != null) map["titulo"] = titulo
        if (descripcion != null) map["descripcion"] = descripcion
        if (fuente != null) map["fuente_url"] = fuente
        withContext(Dispatchers.IO) {
            api.patchNoticia(user, id, map)
        }
    }
    //endregion
}
//endregion
