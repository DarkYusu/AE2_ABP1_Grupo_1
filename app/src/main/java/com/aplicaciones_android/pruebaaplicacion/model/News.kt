package com.aplicaciones_android.pruebaaplicacion.model

import com.google.gson.annotations.SerializedName

// Modelo que mapea el JSON de la API: { id, titulo, descripcion, fuente_url }
data class News(
    @SerializedName("id") val id: Int?,
    @SerializedName("titulo") val title: String,
    @SerializedName("descripcion") val description: String?,
    @SerializedName("fuente_url") val url: String?
)
