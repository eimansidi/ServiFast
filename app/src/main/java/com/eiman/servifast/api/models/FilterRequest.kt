package com.eiman.servifast.api.models

data class FilterRequest(
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val location: String? = null,
    val userType: String? = null,
    val categoryId: Int? = null
)