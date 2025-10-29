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
import com.aplicaciones_android.pruebaaplicacion.network.TokenProvider
import com.aplicaciones_android.pruebaaplicacion.util.Event
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

    // LiveData específica para el resultado de crear noticia: mensaje de error o "SUCCESS" envuelto en Event
    private val _createResult = MutableLiveData<Event<String>?>()
    val createResult: LiveData<Event<String>?> get() = _createResult

    // Ya no guardamos currentUser en la inicialización porque puede crearse antes del login.
    private fun currentUser(): String = TokenProvider.getUsername() ?: "demo"
    //endregion

    //region Métodos públicos
    init {
        refresh()
    }

    fun refresh(user: String = currentUser()) {
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
        val user = currentUser()
        viewModelScope.launch {
            try {
                repository.createNoticia(user, titulo, descripcion, fuente)
                // Señalar éxito solo para crear (evitar confundir con refresh success)
                _createResult.value = Event("SUCCESS")
                // Actualizar lista
                refresh()
            } catch (e: Exception) {
                // Evitar crash: exponer estado de error para que la UI pueda mostrarlo
                _state.value = UiState.Error(e.localizedMessage ?: "Error al crear noticia")
                _createResult.value = Event(e.localizedMessage ?: "Error al crear noticia")
            }
        }
    }

    // Limpiar evento de creación (solo en caso de necesitar reset manual)
    fun clearCreateResult() {
        _createResult.value = null
    }

    fun updateNoticia(id: Int, titulo: String, descripcion: String, fuente: String) {
        val user = currentUser()
        viewModelScope.launch {
            repository.updateNoticia(user, id, titulo, descripcion, fuente)
            refresh()
        }
    }

    fun deleteNoticia(id: Int) {
        val user = currentUser()
        viewModelScope.launch {
            repository.deleteNoticia(user, id)
            refresh()
        }
    }

    fun getNoticiaById(id: Int, onResult: (News?) -> Unit) {
        val user = currentUser()
        viewModelScope.launch {
            val noticia = repository.getNoticiaById(user, id)
            onResult(noticia)
        }
    }

    fun patchNoticia(id: Int, titulo: String?, descripcion: String?, fuente: String?) {
        val user = currentUser()
        viewModelScope.launch {
            repository.patchNoticia(user, id, titulo, descripcion, fuente)
            refresh()
        }
    }
    //endregion
}