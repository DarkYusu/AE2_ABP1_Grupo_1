//region Imports
package com.aplicaciones_android.pruebaaplicacion.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aplicaciones_android.pruebaaplicacion.model.News
import com.aplicaciones_android.pruebaaplicacion.repository.NewsRepository
import com.aplicaciones_android.pruebaaplicacion.repository.Result
import kotlinx.coroutines.launch
//endregion

//region Estados de UI
sealed class UiState {
    data class Success(val data: List<News>) : UiState()
    data class Error(val message: String) : UiState()
    object Loading : UiState()
}
//endregion


class NewsViewModel : ViewModel() {
    //region Declaración de clase y variables
    private val repository = NewsRepository()
    private val _state = MutableLiveData<UiState>()
    val state: LiveData<UiState> get() = _state

    private var currentUser: String = "antonio"
    //endregion

    //region Métodos públicos
    init {
        refresh()
    }

    fun refresh(user: String = currentUser) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            when (val res = repository.fetchNews(user)) {
                is Result.Success -> {
                    // Loguear para depuración: tamaño y títulos
                    try {
                        val titles = res.data.map { it.title }
                        Log.d("NewsViewModel", "Fetched ${res.data.size} news items: $titles")
                    } catch (t: Throwable) {
                        Log.d("NewsViewModel", "Fetched ${res.data.size} items but failed to map titles: ${t.message}")
                    }

                    _state.value = UiState.Success(res.data)
                }
                is Result.Error -> _state.value = UiState.Error(res.exception.localizedMessage ?: "Error desconocido")
            }

        }
    }

    fun createNoticia(titulo: String, descripcion: String, fuente: String) {
        viewModelScope.launch {
            repository.createNoticia(currentUser, titulo, descripcion, fuente)
            refresh()
        }
    }

    fun updateNoticia(id: Int, titulo: String, descripcion: String, fuente: String) {
        viewModelScope.launch {
            repository.updateNoticia(currentUser, id, titulo, descripcion, fuente)
            refresh()
        }
    }

    fun deleteNoticia(id: Int) {
        viewModelScope.launch {
            repository.deleteNoticia(currentUser, id)
            refresh()
        }
    }

    fun getNoticiaById(id: Int, onResult: (News?) -> Unit) {
        viewModelScope.launch {
            val noticia = repository.getNoticiaById(currentUser, id)
            onResult(noticia)
        }
    }

    fun patchNoticia(id: Int, titulo: String?, descripcion: String?, fuente: String?) {
        viewModelScope.launch {
            repository.patchNoticia(currentUser, id, titulo, descripcion, fuente)
            refresh()
        }
    }
    //endregion
}