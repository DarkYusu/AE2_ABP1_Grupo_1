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
import com.aplicaciones_android.pruebaaplicacion.ui.NewsAdapter
import com.aplicaciones_android.pruebaaplicacion.viewmodel.NewsViewModel
import com.aplicaciones_android.pruebaaplicacion.viewmodel.UiState

class ListNoticiasFragment : Fragment() {
    private lateinit var viewModel: NewsViewModel
    private lateinit var adapter: NewsAdapter

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(NewsViewModel::class.java)
        val emptyView = view.findViewById<TextView>(R.id.emptyView)
        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (state is UiState.Success) {
                val orderedList = state.data.sortedByDescending { it.id ?: 0 }
                adapter.update(orderedList)
                val isEmpty = orderedList.isEmpty()
                emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
            } else {
                emptyView.visibility = View.GONE
            }
        }
    }
}
