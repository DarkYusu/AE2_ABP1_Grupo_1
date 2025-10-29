//region Imports
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
//endregion


class CreateNoticiaFragment : Fragment() {
    //region Declaración de clase y variables
    private lateinit var viewModel: NewsViewModel
    //endregion

    //region Inflado de layout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_noticia, container, false)
    }
    //endregion

    //region Inicialización de UI y lógica de creación
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(NewsViewModel::class.java)
        val titulo = view.findViewById<EditText>(R.id.editTitulo)
        val descripcion = view.findViewById<EditText>(R.id.editDescripcion)
        val fuente = view.findViewById<EditText>(R.id.editFuente)
        val btnCrear = view.findViewById<Button>(R.id.btnCrear)
        btnCrear.setOnClickListener {
            viewModel.createNoticia(
                titulo.text.toString(),
                descripcion.text.toString(),
                fuente.text.toString()
            )
            Toast.makeText(requireContext(), "Noticia creada", Toast.LENGTH_SHORT).show()
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
    //endregion
}
