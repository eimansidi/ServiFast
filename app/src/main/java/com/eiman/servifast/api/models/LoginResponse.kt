package com.eiman.servifast.api.models

data class LoginResponse(
    val success: Boolean,
    val id: Int? = null,
    val nombre: String? = null,
    val apellidos: String? = null,
    val avatar: String? = null,
    val error: String? = null
)