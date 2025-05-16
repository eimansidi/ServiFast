package com.eiman.servifast.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.eiman.servifast.R
import com.eiman.servifast.api.models.CategoryResponse

class CategoryAdapter(
    private val items: List<CategoryResponse>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val txt: TextView = view.findViewById(R.id.tvCategory)
        init {
            view.setOnClickListener { onClick(adapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val context = holder.itemView.context
        val category = items[position]
        val key = category.nombre.lowercase()
        val resId = context.resources.getIdentifier(key, "string", context.packageName)
        holder.txt.text = if (resId != 0) context.getString(resId)
        else "Para ${category.nombre}"
    }

    override fun getItemCount(): Int = items.size
}
