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


class EditNoticiaFragment : Fragment() {
    //region Declaración de clase y variables
    private var noticiaId: Int = -1
    //endregion

    //region Ciclo de vida: onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noticiaId = arguments?.getInt("noticia_id") ?: -1
    }
    //endregion

    //region Inflado de layout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_noticia, container, false)
    }
    //endregion

    //region Inicialización de UI y lógica de edición
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val titulo = view.findViewById<EditText>(R.id.editTitulo)
        val descripcion = view.findViewById<EditText>(R.id.editDescripcion)
        val fuente = view.findViewById<EditText>(R.id.editFuente)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardar)
        val viewModel = ViewModelProvider(requireActivity()).get(NewsViewModel::class.java)

        // Cargar datos de la noticia
        viewModel.getNoticiaById(noticiaId) { noticia ->
            if (noticia == null) {
                Toast.makeText(requireContext(), "La noticia fue eliminada.", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
            } else {
                titulo.setText(noticia.title)
                descripcion.setText(noticia.description)
                fuente.setText(noticia.url)
            }
        }

        btnGuardar.setOnClickListener {
            viewModel.updateNoticia(
                noticiaId,
                titulo.text.toString(),
                descripcion.text.toString(),
                fuente.text.toString()
            )
            Toast.makeText(requireContext(), "Noticia actualizada", Toast.LENGTH_SHORT).show()
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
    //endregion
}
