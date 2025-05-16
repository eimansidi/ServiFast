package com.eiman.servifast.api.models

import com.eiman.servifast.api.items.ServicePostItem

data class LatestServicesResponse(
    val success: Boolean,
    val services: List<ServicePostItem>
)