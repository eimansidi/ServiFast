package com.eiman.servifast.api.models

data class CreateServiceRequest(
    val user: String,
    val titulo: String,
    val descripcion: String,
    val imagen: String?,
    val precio: Double,
    val negociable: Boolean,
    val ubicacion: String,
    val idCategoria: Int
)