package com.eiman.servifast.api.models

data class ChangePasswordRequest(
    val user: String,
    val new_password: String
)