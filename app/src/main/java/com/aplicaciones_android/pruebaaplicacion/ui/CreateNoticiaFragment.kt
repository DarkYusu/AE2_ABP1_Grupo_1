package com.aplicaciones_android.pruebaaplicacion.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.aplicaciones_android.pruebaaplicacion.R
import com.aplicaciones_android.pruebaaplicacion.viewmodel.NewsViewModel

class CreateNoticiaFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_noticia, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val titulo = view.findViewById<EditText>(R.id.editTitulo)
        val descripcion = view.findViewById<EditText>(R.id.editDescripcion)
        val fuente = view.findViewById<EditText>(R.id.editFuente)
        val btnCrear = view.findViewById<Button>(R.id.btnCrear)
        val viewModel = ViewModelProvider(requireActivity()).get(NewsViewModel::class.java)
        btnCrear.setOnClickListener {
            viewModel.createNoticia(
                titulo.text.toString(),
                descripcion.text.toString(),
                fuente.text.toString()
            )
            Toast.makeText(requireContext(), "Noticia creada", Toast.LENGTH_SHORT).show()
            titulo.text.clear()
            descripcion.text.clear()
            fuente.text.clear()
        }
    }
}

