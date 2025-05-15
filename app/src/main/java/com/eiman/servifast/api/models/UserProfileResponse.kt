package com.eiman.servifast.api.models

data class UserProfileResponse(
    val success: Boolean,
    val nombre: String,
    val apellidos: String,
    val avatar: String?,
    val ratingPromedio: Float,
    val totalValoraciones: Int
)