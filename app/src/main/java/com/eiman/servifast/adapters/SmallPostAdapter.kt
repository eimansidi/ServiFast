package com.eiman.servifast.adapters

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.eiman.servifast.R
import com.eiman.servifast.api.RetrofitClient
import com.eiman.servifast.api.items.SmallPostItem
import com.eiman.servifast.api.models.FavoriteRequest
import com.eiman.servifast.api.models.GenericResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SmallPostAdapter(
    private val userIdentifier: String,
    private val items: List<SmallPostItem>
) : RecyclerView.Adapter<SmallPostAdapter.VH>() {

    private val likedSet = mutableSetOf<Int>()

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvUserType: TextView      = view.findViewById(R.id.tvUserType)
        val imgPost: ImageView        = view.findViewById(R.id.imgPost)
        val imgStamp: ImageView       = view.findViewById(R.id.imgNegotiableStamp)
        val tvTitle: TextView         = view.findViewById(R.id.tvTitle)
        val tvPrice: TextView         = view.findViewById(R.id.tvPrice)
        val btnLike: ImageButton      = view.findViewById(R.id.btnLike)

        private var isLiked = false

        fun bind(item: SmallPostItem) {
            tvUserType.text = if (item.userType == "particular")
                itemView.context.getString(R.string.particular)
            else
                itemView.context.getString(R.string.profesional)
            val colorRes = if (item.userType == "particular")
                R.color.colorParticular else R.color.colorProfesional
            tvUserType.setTextColor(ContextCompat.getColor(itemView.context, colorRes))

            if (item.imageBase64 != null) {
                val bytes = Base64.decode(item.imageBase64, Base64.DEFAULT)
                imgPost.setImageBitmap(BitmapFactory.decodeByteArray(bytes,0,bytes.size))
            } else {
                imgPost.setImageResource(R.drawable.img_placeholder)
            }

            if (item.negotiable) {
                imgStamp.visibility = View.VISIBLE
                val lang = itemView.context.resources.configuration.locale.language
                val res = if (lang == "en") R.drawable.negotiable else R.drawable.negociable
                imgStamp.setImageResource(res)
            } else {
                imgStamp.visibility = View.GONE
            }

            tvTitle.text = item.title
            tvPrice.text = "${item.price} €"

            isLiked = likedSet.contains(item.id)
            updateLikeIcon()

            btnLike.setOnClickListener {
                if (isLiked) removeFavorite(item.id)
                else addFavorite(item.id)
                isLiked = !isLiked
                updateLikeIcon()
            }
        }

        private fun updateLikeIcon() {
            val tint = if (isLiked) R.color.colorLike else R.color.star_inactive
            btnLike.setColorFilter(
                ContextCompat.getColor(itemView.context, tint),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }

        private fun addFavorite(serviceId: Int) {
            likedSet.add(serviceId)
            val req = FavoriteRequest(user = userIdentifier, serviceId = serviceId)
            RetrofitClient.instance.addFavorite(req)
                .enqueue(object: Callback<GenericResponse> {
                    override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {}
                    override fun onFailure(call: Call<GenericResponse>, t: Throwable) {}
                })
        }

        private fun removeFavorite(serviceId: Int) {
            likedSet.remove(serviceId)
            val req = FavoriteRequest(user = userIdentifier, serviceId = serviceId)
            RetrofitClient.instance.removeFavorite(req)
                .enqueue(object: Callback<GenericResponse> {
                    override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {}
                    override fun onFailure(call: Call<GenericResponse>, t: Throwable) {}
                })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.widget_post_small, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
