package com.eiman.servifast.api.items

import com.google.gson.annotations.SerializedName

data class RatingItem(
    @SerializedName("avatar")      val avatarBase64: String?,
    @SerializedName("nombre")      val nombre: String,
    @SerializedName("puntuacion")  val puntuacion: Int,
    @SerializedName("comentario")  val comentario: String?
)
