package com.aplicaciones_android.pruebaaplicacion.model

import com.google.gson.annotations.SerializedName

data class LastIdResponse(
    @SerializedName("last_id") val lastId: Int,
    @SerializedName("next_id") val nextId: Int
)

