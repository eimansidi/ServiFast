package com.eiman.servifast.api.items

data class TopUserItem(
    val id: Int,
    val nombre: String,
    val avatarBase64: String?,
    val rating: Float
)