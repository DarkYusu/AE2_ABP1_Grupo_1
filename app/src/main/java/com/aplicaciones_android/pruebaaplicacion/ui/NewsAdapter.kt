package com.aplicaciones_android.pruebaaplicacion.ui

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aplicaciones_android.pruebaaplicacion.R
import com.aplicaciones_android.pruebaaplicacion.model.News

class NewsAdapter(private var items: List<News>, private val onClick: (News) -> Unit) : RecyclerView.Adapter<NewsAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val description: TextView = view.findViewById(R.id.description)
        val link: TextView = view.findViewById(R.id.link)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val titleText = if (item.title.isBlank()) "Sin título" else item.title
        var descText = item.description?.takeIf { it.isNotBlank() } ?: ""

        // Si no hay título ni descripción, mostrar el objeto completo para depuración
        if (titleText == "Sin título" && descText.isEmpty()) {
            descText = item.toString()
        }

        holder.title.text = titleText
        holder.title.visibility = View.VISIBLE

        holder.description.text = descText
        holder.description.visibility = if (descText.isEmpty()) View.GONE else View.VISIBLE

        // Mostrar el enlace si existe
        if (!item.url.isNullOrBlank()) {
            holder.link.text = item.url
            holder.link.visibility = View.VISIBLE
            holder.link.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(Intent.ACTION_VIEW, item.url.toUri())
                context.startActivity(intent)
            }
        } else {
            holder.link.visibility = View.GONE
        }

        holder.btnEdit.setOnClickListener {
            val fragment = EditNoticiaFragment()
            fragment.arguments = android.os.Bundle().apply { putInt("noticia_id", item.id ?: -1) }
            val activity = holder.itemView.context as androidx.fragment.app.FragmentActivity
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        holder.btnDelete.setOnClickListener {
            val activity = holder.itemView.context as androidx.fragment.app.FragmentActivity
            val viewModel = androidx.lifecycle.ViewModelProvider(activity).get(com.aplicaciones_android.pruebaaplicacion.viewmodel.NewsViewModel::class.java)
            item.id?.let { id ->
                AlertDialog.Builder(activity)
                    .setTitle("¿Eliminar noticia?")
                    .setMessage("¿Seguro que deseas eliminar la noticia con id $id?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        viewModel.deleteNoticia(id)
                        Toast.makeText(activity, "Noticia eliminada (id: $id)", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }

        holder.itemView.setOnClickListener {
            val fragment = DetalleNoticiaFragment()
            fragment.arguments = android.os.Bundle().apply { putInt("noticia_id", item.id ?: -1) }
            val activity = holder.itemView.context as androidx.fragment.app.FragmentActivity
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        // Animación de aparición (fade in)
        holder.itemView.alpha = 0f
        holder.itemView.animate().alpha(1f).setDuration(400).start()
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<News>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size
            override fun getNewListSize() = newItems.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return items[oldItemPosition].id == newItems[newItemPosition].id
            }
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return items[oldItemPosition] == newItems[newItemPosition]
            }
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        items = newItems
        diffResult.dispatchUpdatesTo(this)
    }
}
