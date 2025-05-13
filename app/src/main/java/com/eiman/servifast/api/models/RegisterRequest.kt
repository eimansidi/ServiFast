package com.eiman.servifast.api.models

data class RegisterRequest(
    val nombre: String,
    val apellidos: String,
    val email: String,
    val telefono: String,
    val password: String,
    val tipo_usuario: String
)
