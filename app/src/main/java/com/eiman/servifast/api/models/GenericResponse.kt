package com.eiman.servifast.api.models

data class GenericResponse(
    val success: Boolean,
    val error: String? = null
)
