package com.example.truekita.repository

import com.example.truekita.model.Publicacion
import com.example.truekita.utils.BotModerador

class PublicacionRepository {

    fun prepararPublicacion(
        usuarioId: String,
        titulo: String,
        descripcion: String,
        categoria: String,
        precio: Double,
        imagenUrl: String
    ): Publicacion {

        val resultadoBot = BotModerador.revisarPublicacion(
            titulo = titulo,
            descripcion = descripcion,
            categoria = categoria,
            precio = precio
        )

        val estadoFinal = if (resultadoBot.aceptada) {
            "aceptada"
        } else {
            "rechazada"
        }

        return Publicacion(
            usuarioId = usuarioId,
            titulo = titulo,
            descripcion = descripcion,
            categoria = categoria,
            precio = precio,
            imagenUrl = imagenUrl,
            estado = estadoFinal,
            motivoRechazo = resultadoBot.motivo
        )
    }
}