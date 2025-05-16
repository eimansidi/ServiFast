package com.eiman.servifast.api.items

data class ServicePostItem(
    val id: Int,
    val title: String,
    val description: String?,
    val imageBase64: String?,
    val price: Double,
    val negotiable: Boolean,
    val location: String?,
    val userType: String,
    val sellerAvatarBase64: String?,
    val sellerName: String,
    val sellerRating: Float
)