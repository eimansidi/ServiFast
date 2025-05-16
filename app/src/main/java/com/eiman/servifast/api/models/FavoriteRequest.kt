package com.eiman.servifast.api.models

data class FavoriteRequest(
    val user: String,
    val serviceId: Int
)