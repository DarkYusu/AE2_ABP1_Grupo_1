package com.aplicaciones_android.pruebaaplicacion.network

import com.aplicaciones_android.pruebaaplicacion.model.LastIdResponse
import com.aplicaciones_android.pruebaaplicacion.model.News
import retrofit2.http.*

interface NewsApi {
    @GET("{user}/noticias")
    suspend fun getNews(@Path("user") user: String): List<News>

    @POST("{user}/noticias")
    suspend fun createNoticia(@Path("user") user: String, @Body noticia: News): News

    @PUT("{user}/noticias/{noticia_id}")
    suspend fun updateNoticia(@Path("user") user: String, @Path("noticia_id") id: Int, @Body noticia: News): News

    @PATCH("{user}/noticias/{noticia_id}")
    suspend fun patchNoticia(@Path("user") user: String, @Path("noticia_id") id: Int, @Body noticia: Map<String, Any?>): News

    @DELETE("{user}/noticias/{noticia_id}")
    suspend fun deleteNoticia(@Path("user") user: String, @Path("noticia_id") id: Int): retrofit2.Response<Unit>

    @GET("{user}/noticias/{noticia_id}")
    suspend fun getNoticiaById(@Path("user") user: String, @Path("noticia_id") id: Int): News?

    @GET("{user}/noticias/lastid")
    suspend fun getLastId(@Path("user") user: String): LastIdResponse
}
