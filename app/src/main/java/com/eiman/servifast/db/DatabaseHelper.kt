package com.eiman.servifast.db

import java.sql.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object DatabaseHelper {
    private const val JDBC_URL = "jdbc:mariadb://localhost:3306/servifast"
    private const val DB_USER = "admin"
    private const val DB_PASSWORD = "admin1234"

    init {
        Class.forName("org.mariadb.jdbc.Driver")
    }

    private fun getConnection(): Connection =
        DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)

    // mapeos genéricos
    private fun mapUsuario(rs: ResultSet) = Usuario(
        id          = rs.getInt("id"),
        nombre      = rs.getString("nombre"),
        apellidos   = rs.getString("apellidos"),
        email       = rs.getString("email"),
        telefono    = rs.getString("telefono"),
        contrasena  = rs.getString("contraseña"),
        tipoUsuario = rs.getString("tipo_usuario"),
        avatar      = rs.getBytes("avatar")
    )

    private fun mapCategoria(rs: ResultSet) = Categoria(
        id     = rs.getInt("id"),
        nombre = rs.getString("nombre")
    )

    private fun mapServicio(rs: ResultSet) = Servicio(
        id                = rs.getInt("id"),
        titulo            = rs.getString("titulo"),
        descripcion       = rs.getString("descripcion"),
        imagen            = rs.getBytes("imagen"),
        precio            = rs.getBigDecimal("precio"),
        negociable        = rs.getBoolean("negociable"),
        ubicacion         = rs.getString("ubicacion"),
        tipoUsuario       = rs.getString("tipo_usuario"),
        idUsuario         = rs.getInt("id_usuario"),
        idCategoria       = rs.getInt("id_categoria"),
        fechaPublicacion  = rs.getTimestamp("fecha_publicacion")?.let { ts ->
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(ts.time),
                ZoneId.systemDefault()
            )
        }
    )

    private fun mapValoracion(rs: ResultSet) = Valoracion(
        id          = rs.getInt("id"),
        idValorado  = rs.getInt("id_valorado"),
        idValorador = rs.getInt("id_valorador"),
        puntuacion  = rs.getInt("puntuacion"),
        comentario  = rs.getString("comentario"),
        fecha       = rs.getTimestamp("fecha")?.let { ts ->
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(ts.time),
                ZoneId.systemDefault()
            )
        }
    )

    private fun mapFavorito(rs: ResultSet) = Favorito(
        id         = rs.getInt("id"),
        idUsuario  = rs.getInt("id_usuario"),
        idServicio = rs.getInt("id_servicio"),
        fecha      = rs.getTimestamp("fecha")?.let { ts ->
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(ts.time),
                ZoneId.systemDefault()
            )
        }
    )

    private fun mapCompra(rs: ResultSet) = Compra(
        id           = rs.getInt("id"),
        idComprador  = rs.getInt("id_comprador"),
        idServicio   = rs.getInt("id_servicio"),
        precioPagado = rs.getBigDecimal("precio_pagado"),
        fecha        = rs.getTimestamp("fecha")?.let { ts ->
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(ts.time),
                ZoneId.systemDefault()
            )
        }
    )

    // ---------- CRUD Usuarios ----------
    fun insertUsuario(u: Usuario): Int {
        val sql = """
            INSERT INTO usuarios (nombre, apellidos, email, telefono, contraseña, tipo_usuario, avatar)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """
        getConnection().use { conn ->
            conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { st ->
                st.setString(1, u.nombre)
                st.setString(2, u.apellidos)
                st.setString(3, u.email)
                st.setString(4, u.telefono)
                st.setString(5, u.contrasena)
                st.setString(6, u.tipoUsuario)
                st.setBytes(7, u.avatar)
                st.executeUpdate()
                st.generatedKeys.use { keys ->
                    if (keys.next()) return keys.getInt(1)
                }
            }
        }
        throw SQLException("No se pudo insertar usuario")
    }

    fun getUsuarioById(id: Int): Usuario? {
        val sql = "SELECT * FROM usuarios WHERE id = ?"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { st ->
                st.setInt(1, id)
                st.executeQuery().use { rs ->
                    if (rs.next()) return mapUsuario(rs)
                }
            }
        }
        return null
    }

    fun getAllUsuarios(): List<Usuario> {
        val list = mutableListOf<Usuario>()
        val sql = "SELECT * FROM usuarios"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { st ->
                st.executeQuery().use { rs ->
                    while (rs.next()) list += mapUsuario(rs)
                }
            }
        }
        return list
    }

    fun updateUsuario(u: Usuario): Boolean {
        val sql = """
            UPDATE usuarios SET
              nombre = ?, apellidos = ?, email = ?, telefono = ?, contraseña = ?, tipo_usuario = ?, avatar = ?
            WHERE id = ?
        """
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { st ->
                st.setString(1, u.nombre)
                st.setString(2, u.apellidos)
                st.setString(3, u.email)
                st.setString(4, u.telefono)
                st.setString(5, u.contrasena)
                st.setString(6, u.tipoUsuario)
                st.setBytes(7, u.avatar)
                u.id?.let { st.setInt(8, it) } ?: return false
                return st.executeUpdate() > 0
            }
        }
    }

    fun deleteUsuario(id: Int): Boolean {
        val sql = "DELETE FROM usuarios WHERE id = ?"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { st ->
                st.setInt(1, id)
                return st.executeUpdate() > 0
            }
        }
    }

    // ---------- CRUD Categorías ----------
    fun insertCategoria(c: Categoria): Int {
        val sql = "INSERT INTO categorias (nombre) VALUES (?)"
        getConnection().use { conn ->
            conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { st ->
                st.setString(1, c.nombre)
                st.executeUpdate()
                st.generatedKeys.use { if (it.next()) return it.getInt(1) }
            }
        }
        throw SQLException("No se pudo insertar categoría")
    }

    fun getCategoriaById(id: Int): Categoria? {
        val sql = "SELECT * FROM categorias WHERE id = ?"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { st ->
                st.setInt(1, id)
                st.executeQuery().use { if (it.next()) return mapCategoria(it) }
            }
        }
        return null
    }

    fun getAllCategorias(): List<Categoria> {
        val list = mutableListOf<Categoria>()
        val sql = "SELECT * FROM categorias"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { st ->
                st.executeQuery().use { rs ->
                    while (rs.next()) list += mapCategoria(rs)
                }
            }
        }
        return list
    }

    fun updateCategoria(c: Categoria): Boolean {
        val sql = "UPDATE categorias SET nombre = ? WHERE id = ?"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { st ->
                st.setString(1, c.nombre)
                c.id?.let { st.setInt(2, it) } ?: return false
                return st.executeUpdate() > 0
            }
        }
    }

    fun deleteCategoria(id: Int): Boolean {
        val sql = "DELETE FROM categorias WHERE id = ?"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { st ->
                st.setInt(1, id)
                return st.executeUpdate() > 0
            }
        }
    }

    // ---------- CRUD Servicios ----------
    fun insertServicio(s: Servicio): Int {
        val sql = """
            INSERT INTO servicios
              (titulo, descripcion, imagen, precio, negociable, ubicacion, tipo_usuario, id_usuario, id_categoria)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
        getConnection().use { conn ->
            conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { st ->
                st.setString(1, s.titulo)
                st.setString(2, s.descripcion)
                st.setBytes(3, s.imagen)
                st.setBigDecimal(4, s.precio)
                st.setBoolean(5, s.negociable)
                st.setString(6, s.ubicacion)
                st.setString(7, s.tipoUsuario)
                st.setInt(8, s.idUsuario)
                st.setInt(9, s.idCategoria)
                st.executeUpdate()
                st.generatedKeys.use { if (it.next()) return it.getInt(1) }
            }
        }
        throw SQLException("No se pudo insertar servicio")
    }

    fun getServicioById(id: Int): Servicio? {
        val sql = "SELECT * FROM servicios WHERE id = ?"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { st ->
                st.setInt(1, id)
                st.executeQuery().use { if (it.next()) return mapServicio(it) }
            }
        }
        return null
    }

    fun getAllServicios(): List<Servicio> {
        val list = mutableListOf<Servicio>()
        val sql = "SELECT * FROM servicios"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { st ->
                st.executeQuery().use { rs ->
                    while (rs.next()) list += mapServicio(rs)
                }
            }
        }
        return list
    }

    fun updateServicio(s: Servicio): Boolean {
        val sql = """
            UPDATE servicios SET
              titulo = ?, descripcion = ?, imagen = ?, precio = ?, negociable = ?,
              ubicacion = ?, tipo_usuario = ?, id_usuario = ?, id_categoria = ?
            WHERE id = ?
        """
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { st ->
                st.setString(1, s.titulo)
                st.setString(2, s.descripcion)
                st.setBytes(3, s.imagen)
                st.setBigDecimal(4, s.precio)
                st.setBoolean(5, s.negociable)
                st.setString(6, s.ubicacion)
                st.setString(7, s.tipoUsuario)
                st.setInt(8, s.idUsuario)
                st.setInt(9, s.idCategoria)
                s.id?.let { st.setInt(10, it) } ?: return false
                return st.executeUpdate() > 0
            }
        }
    }

    fun deleteServicio(id: Int): Boolean {
        val sql = "DELETE FROM servicios WHERE id = ?"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { st ->
                st.setInt(1, id)
                return st.executeUpdate() > 0
            }
        }
    }

    // ---------- CRUD Valoraciones ----------
    fun insertValoracion(v: Valoracion): Int {
        val sql = """
            INSERT INTO valoraciones (id_valorado, id_valorador, puntuacion, comentario)
            VALUES (?, ?, ?, ?)
        """
        getConnection().use { conn ->
            conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { st ->
                st.setInt(1, v.idValorado)
                st.setInt(2, v.idValorador)
                st.setInt(3, v.puntuacion)
                st.setString(4, v.comentario)
                st.executeUpdate()
                st.generatedKeys.use { if (it.next()) return it.getInt(1) }
            }
        }
        throw SQLException("No se pudo insertar valoración")
    }

    fun getValoracionById(id: Int): Valoracion? {
        val sql = "SELECT * FROM valoraciones WHERE id = ?"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { st ->
                st.setInt(1, id)
                st.executeQuery().use { if (it.next()) return mapValoracion(it) }
            }
        }
        return null
    }

    fun getAllValoraciones(): List<Valoracion> {
        val list = mutableListOf<Valoracion>()
        val sql = "SELECT * FROM valoraciones"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { st ->
                st.executeQuery().use { rs ->
                    while (rs.next()) list += mapValoracion(rs)
                }
            }
        }
        return list
    }

    fun updateValoracion(v: Valoracion): Boolean {
        val sql = """
            UPDATE valoraciones SET
              id_valorado = ?, id_valorador = ?, puntuacion = ?, comentario = ?
            WHERE id = ?
        """
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { st ->
                st.setInt(1, v.idValorado)
                st.setInt(2, v.idValorador)
                st.setInt(3, v.puntuacion)
                st.setString(4, v.comentario)
                v.id?.let { st.setInt(5, it) } ?: return false
                return st.executeUpdate() > 0
            }
        }
    }

    fun deleteValoracion(id: Int): Boolean {
        val sql = "DELETE FROM valoraciones WHERE id = ?"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { st ->
                st.setInt(1, id)
                return st.executeUpdate() > 0
            }
        }
    }

    // ---------- CRUD Favoritos ----------
    fun insertFavorito(f: Favorito): Int {
        val sql = "INSERT INTO favoritos (id_usuario, id_servicio) VALUES (?, ?)"
        getConnection().use { conn ->
            conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { st ->
                st.setInt(1, f.idUsuario)
                st.setInt(2, f.idServicio)
                st.executeUpdate()
                st.generatedKeys.use { if (it.next()) return it.getInt(1) }
            }
        }
        throw SQLException("No se pudo insertar favorito")
    }

    fun getFavoritoById(id: Int): Favorito? {
        val sql = "SELECT * FROM favoritos WHERE id = ?"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { st ->
                st.setInt(1, id)
                st.executeQuery().use { if (it.next()) return mapFavorito(it) }
            }
        }
        return null
    }

    fun getAllFavoritos(): List<Favorito> {
        val list = mutableListOf<Favorito>()
        val sql = "SELECT * FROM favoritos"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { st ->
                st.executeQuery().use { rs ->
                    while (rs.next()) list += mapFavorito(rs)
                }
            }
        }
        return list
    }

    fun updateFavorito(f: Favorito): Boolean {
        val sql = "UPDATE favoritos SET id_usuario = ?, id_servicio = ? WHERE id = ?"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { st ->
                st.setInt(1, f.idUsuario)
                st.setInt(2, f.idServicio)
                f.id?.let { st.setInt(3, it) } ?: return false
                return st.executeUpdate() > 0
            }
        }
    }

    fun deleteFavorito(id: Int): Boolean {
        val sql = "DELETE FROM favoritos WHERE id = ?"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { st ->
                st.setInt(1, id)
                return st.executeUpdate() > 0
            }
        }
    }

    // ---------- CRUD Compras ----------
    fun insertCompra(c: Compra): Int {
        val sql = """
            INSERT INTO compras (id_comprador, id_servicio, precio_pagado)
            VALUES (?, ?, ?)
        """
        getConnection().use { conn ->
            conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { st ->
                st.setInt(1, c.idComprador)
                st.setInt(2, c.idServicio)
                st.setBigDecimal(3, c.precioPagado)
                st.executeUpdate()
                st.generatedKeys.use { if (it.next()) return it.getInt(1) }
            }
        }
        throw SQLException("No se pudo insertar compra")
    }

    fun getCompraById(id: Int): Compra? {
        val sql = "SELECT * FROM compras WHERE id = ?"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { st ->
                st.setInt(1, id)
                st.executeQuery().use { if (it.next()) return mapCompra(it) }
            }
        }
        return null
    }

    fun getAllCompras(): List<Compra> {
        val list = mutableListOf<Compra>()
        val sql = "SELECT * FROM compras"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { st ->
                st.executeQuery().use { rs ->
                    while (rs.next()) list += mapCompra(rs)
                }
            }
        }
        return list
    }

    fun updateCompra(c: Compra): Boolean {
        val sql = """
            UPDATE compras SET id_comprador = ?, id_servicio = ?, precio_pagado = ?
            WHERE id = ?
        """
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { st ->
                st.setInt(1, c.idComprador)
                st.setInt(2, c.idServicio)
                st.setBigDecimal(3, c.precioPagado)
                c.id?.let { st.setInt(4, it) } ?: return false
                return st.executeUpdate() > 0
            }
        }
    }

    fun deleteCompra(id: Int): Boolean {
        val sql = "DELETE FROM compras WHERE id = ?"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use { st ->
                st.setInt(1, id)
                return st.executeUpdate() > 0
            }
        }
    }
}
