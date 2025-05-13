package com.eiman.servifast.models

import java.math.BigDecimal
import java.time.LocalDateTime

data class Usuario(
    val id: Int? = null,
    val nombre: String,
    val apellidos: String,
    val email: String,
    val telefono: String?,
    val contrasena: String,
    val tipoUsuario: String,
    val avatar: ByteArray?
)

data class Categoria(
    val id: Int? = null,
    val nombre: String
)

data class Servicio(
    val id: Int? = null,
    val titulo: String,
    val descripcion: String?,
    val imagen: ByteArray?,
    val precio: BigDecimal,
    val negociable: Boolean,
    val ubicacion: String?,
    val tipoUsuario: String?,
    val idUsuario: Int,
    val idCategoria: Int,
    val fechaPublicacion: LocalDateTime? = null
)

data class Valoracion(
    val id: Int? = null,
    val idValorado: Int,
    val idValorador: Int,
    val puntuacion: Int,
    val comentario: String?,
    val fecha: LocalDateTime? = null
)

data class Favorito(
    val id: Int? = null,
    val idUsuario: Int,
    val idServicio: Int,
    val fecha: LocalDateTime? = null
)

data class Compra(
    val id: Int? = null,
    val idComprador: Int,
    val idServicio: Int,
    val precioPagado: BigDecimal,
    val fecha: LocalDateTime? = null
)
