package com.aplicaciones_android.pruebaaplicacion.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aplicaciones_android.pruebaaplicacion.R
import com.aplicaciones_android.pruebaaplicacion.viewmodel.NewsViewModel
import com.aplicaciones_android.pruebaaplicacion.viewmodel.UiState


class ListNoticiasFragment : Fragment() {
    //region Declaración de clase y variables
    private lateinit var viewModel: NewsViewModel
    private lateinit var adapter: NewsAdapter
    //endregion

    //region Inflado de layout y configuración de RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list_noticias, container, false)
        val recycler = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler)
        adapter = NewsAdapter(emptyList()) {}
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter
        return view
    }
    //endregion

    //region Observación de datos y actualización de UI
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(NewsViewModel::class.java)
        val emptyView = view.findViewById<View>(R.id.emptyView)
        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (state is UiState.Success) {
                // Ordena la lista por id descendente para evitar saltos visuales
                val orderedList = state.data.sortedByDescending { it.id ?: 0 }
                adapter.update(orderedList)
                val isEmpty = orderedList.isEmpty()
                emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
            } else {
                emptyView.visibility = View.GONE
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Forzar recarga cuando el fragment se vuelva visible (incluye inicio de la app)
        viewModel.refresh()
    }
    //endregion
}
