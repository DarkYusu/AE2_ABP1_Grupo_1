package com.aplicaciones_android.pruebaaplicacion.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.aplicaciones_android.pruebaaplicacion.R
import com.aplicaciones_android.pruebaaplicacion.viewmodel.NewsViewModel


class DetalleNoticiaFragment : Fragment() {
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
        return inflater.inflate(R.layout.fragment_detalle_noticia, container, false)
    }
    //endregion

    //region Inicialización de UI y obtención de datos
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val titulo = view.findViewById<TextView>(R.id.txtTitulo)
        val descripcion = view.findViewById<TextView>(R.id.txtDescripcion)
        val fuente = view.findViewById<TextView>(R.id.txtFuente)
        val viewModel = ViewModelProvider(requireActivity()).get(NewsViewModel::class.java)
        viewModel.getNoticiaById(noticiaId) { noticia ->
            if (noticia == null) {
                Toast.makeText(requireContext(), "La noticia fue eliminada.", Toast.LENGTH_SHORT)
                    .show()
                requireActivity().supportFragmentManager.popBackStack()
            } else {
                titulo.text = noticia.title
                descripcion.text = noticia.description
                fuente.text = noticia.url
            }
        }
    }
    //endregion
}
