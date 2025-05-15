package com.eiman.servifast.api.models

import com.eiman.servifast.api.items.RatingItem

data class UserRatingsResponse(
    val success: Boolean,
    val ratings: List<RatingItem>
)