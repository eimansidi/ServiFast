package com.eiman.servifast.api.models

data class UpdateUserRequest(
    val email: String? = null,
    val telefono: String? = null,
    val tipo_usuario: String? = null,
    val user: String
)
