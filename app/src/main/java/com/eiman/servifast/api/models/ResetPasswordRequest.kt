package com.eiman.servifast.api.models

data class ResetPasswordRequest(
    val user: String,
    val new_password: String
)