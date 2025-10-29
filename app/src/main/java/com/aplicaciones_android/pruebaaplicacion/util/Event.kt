package com.aplicaciones_android.pruebaaplicacion.util

/**
 * Wrapper para eventos LiveData que deben consumirse una sola vez (SingleLiveEvent pattern).
 */
open class Event<out T>(private val content: T) {
    private var hasBeenHandled = false

    /**
     * Devuelve el contenido sólo si no fue manejado antes.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Devuelve el contenido sin marcarlo como manejado.
     */
    fun peekContent(): T = content
}

