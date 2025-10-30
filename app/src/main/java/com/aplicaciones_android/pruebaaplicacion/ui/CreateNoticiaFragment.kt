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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
        // Ajustar ScrollView para IME: padding bottom y desplazamiento al campo enfocado
        try {
            val scroll = view.findViewById<android.widget.ScrollView>(R.id.scroll_create)
            ViewCompat.setOnApplyWindowInsetsListener(scroll) { v, insets ->
                val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
                v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, ime.bottom)
                // Si IME visible, desplazar al view actual con focus para asegurar visibilidad
                if (ime.bottom > 0) {
                    v.post {
                        val focused = v.findFocus()
                        if (focused != null) {
                            // calcular offset y scrollear
                            try {
                                val y = focused.top
                                (v as android.widget.ScrollView).smoothScrollTo(0, y)
                            } catch (_: Exception) {}
                        } else {
                            // si no hay focus, scrollear al final para mostrar botones
                            (v as android.widget.ScrollView).smoothScrollTo(0, v.bottom)
                        }
                    }
                }
                insets
            }
        } catch (_: Exception) {}
        viewModel = ViewModelProvider(requireActivity()).get(NewsViewModel::class.java)
        val titulo = view.findViewById<EditText>(R.id.editTitulo)
        val descripcion = view.findViewById<EditText>(R.id.editDescripcion)
        val fuente = view.findViewById<EditText>(R.id.editFuente)
        val btnCrear = view.findViewById<Button>(R.id.btnCrear)

        // Limpiar cualquier resultado previo para evitar que se dispare al abrir el fragment
        viewModel.clearCreateResult()

        // Observar resultado específico de creación (evita confundir con refresh success)
        viewModel.createResult.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { result ->
                if (result == "SUCCESS") {
                    Toast.makeText(requireContext(), "Noticia creada", Toast.LENGTH_SHORT).show()
                    // limpiar inputs
                    titulo.text.clear()
                    descripcion.text.clear()
                    fuente.text.clear()
                    // Navegar explícitamente a la lista usando MainActivity para evitar inconsistencias del back stack
                    (activity as? com.aplicaciones_android.pruebaaplicacion.MainActivity)?.navigateToList()
                } else {
                    Toast.makeText(requireContext(), "Error al crear noticia: $result", Toast.LENGTH_LONG).show()
                }
            }
        }

        btnCrear.setOnClickListener {
            // Validación mínima de campos
            val t = titulo.text.toString().trim()
            val d = descripcion.text.toString().trim()
            val f = fuente.text.toString().trim()
            if (t.isEmpty()) {
                Toast.makeText(requireContext(), "Introduce el título", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Lanzar la creación y dejar que el observer maneje el resultado
            viewModel.createNoticia(t, d, f)
        }
    }
    //endregion
}
