package com.eiman.servifast.api.models

data class UpdateUserRequest(
    val user: String,
    val email: String? = null,
    val telefono: String? = null,
    val tipo_usuario: String? = null,
    val avatar: String? = null
)