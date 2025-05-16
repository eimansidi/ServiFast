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
import com.eiman.servifast.api.items.TopUserItem

class TopUsersAdapter(
    private val items: List<TopUserItem>,
    private val onClick: (TopUserItem) -> Unit
) : RecyclerView.Adapter<TopUsersAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ImageView = view.findViewById(R.id.userAvatar)
        val name: TextView    = view.findViewById(R.id.tvUsername)
        private val stars = listOf<ImageView>(
            view.findViewById(R.id.star1),
            view.findViewById(R.id.star2),
            view.findViewById(R.id.star3),
            view.findViewById(R.id.star4),
            view.findViewById(R.id.star5)
        )

        init {
            view.setOnClickListener {
                onClick(items[adapterPosition])
            }
        }

        fun bind(item: TopUserItem) {
            if (item.avatarBase64 != null) {
                val bytes = Base64.decode(item.avatarBase64, Base64.DEFAULT)
                avatar.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
            } else {
                avatar.setImageResource(R.drawable.generic_avatar)
            }

            name.text = item.nombre

            val filled = item.rating.toInt()
            for ((i, star) in stars.withIndex()) {
                val color = if (i < filled) R.color.star_active else R.color.star_inactive
                star.setColorFilter(ContextCompat.getColor(star.context, color), android.graphics.PorterDuff.Mode.SRC_IN)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.widget_user, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
