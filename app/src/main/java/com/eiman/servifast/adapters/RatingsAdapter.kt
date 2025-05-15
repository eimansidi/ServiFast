package com.eiman.servifast.adapters

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.eiman.servifast.R
import com.eiman.servifast.api.items.RatingItem

class RatingsAdapter(
    private val items: List<RatingItem>
) : RecyclerView.Adapter<RatingsAdapter.RatingViewHolder>() {

    inner class RatingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ImageView = view.findViewById(R.id.userAvatar)
        val nombre: TextView = view.findViewById(R.id.tvUsername)
        private val stars: List<ImageView> = listOf(
            view.findViewById(R.id.star1),
            view.findViewById(R.id.star2),
            view.findViewById(R.id.star3),
            view.findViewById(R.id.star4),
            view.findViewById(R.id.star5)
        )
        val comentario: TextView = view.findViewById(R.id.tvComment)

        fun bind(item: RatingItem) {
            item.avatarBase64?.let {
                val bytes = Base64.decode(it, Base64.DEFAULT)
                avatar.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
            } ?: avatar.setImageResource(R.drawable.generic_avatar)

            nombre.text = item.nombre.capitalize()

            val active = item.puntuacion
            for ((i, star) in stars.withIndex()) {
                val colorRes = if (i < active) R.color.star_active else R.color.star_inactive
                star.setColorFilter(ContextCompat.getColor(star.context, colorRes), android.graphics.PorterDuff.Mode.SRC_IN)
            }

            comentario.text = item.comentario ?: ""
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.widget_rating, parent, false)
        return RatingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RatingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
