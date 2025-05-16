package com.eiman.servifast.api.items

data class SmallPostItem(
    val id: Int,
    val userType: String,
    val imageBase64: String?,
    val negotiable: Boolean,
    val title: String,
    val price: Double
)
