package com.eiman.servifast.api.items

data class UserItem(
    val id: Int,
    val nombre: String,
    val avatarBase64: String?,
    val rating: Float,
    val totalValoraciones: Int
)