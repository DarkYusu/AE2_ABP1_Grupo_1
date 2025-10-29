//region Imports y declaración
package com.aplicaciones_android.pruebaaplicacion.repository

import com.aplicaciones_android.pruebaaplicacion.model.News
import com.aplicaciones_android.pruebaaplicacion.network.RetrofitClient
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
    suspend fun fetchNews(user: String = "demo"): Result<List<News>> = withContext(Dispatchers.IO) {
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
            api.createNoticia(user, noticia)
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
