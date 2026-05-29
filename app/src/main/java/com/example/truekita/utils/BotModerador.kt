package com.example.truekita.utils

object BotModerador {

    private val palabrasProhibidas = listOf(
        "arma",
        "armas",
        "droga",
        "drogas",
        "alcohol",
        "cigarro",
        "cigarros",
        "ilegal",
        "robado",
        "robada",
        "estafa",
        "falso",
        "falsificado",
        "clon",
        "pirata"
    )

    fun revisarPublicacion(
        titulo: String,
        descripcion: String,
        categoria: String,
        precio: Double
    ): ResultadoRevision {

        if (titulo.isBlank()) {
            return ResultadoRevision(
                aceptada = false,
                motivo = "El título no puede estar vacío."
            )
        }

        if (descripcion.isBlank()) {
            return ResultadoRevision(
                aceptada = false,
                motivo = "La descripción no puede estar vacía."
            )
        }

        if (categoria.isBlank()) {
            return ResultadoRevision(
                aceptada = false,
                motivo = "Debe seleccionar una categoría."
            )
        }

        if (precio < 0) {
            return ResultadoRevision(
                aceptada = false,
                motivo = "El precio no puede ser negativo."
            )
        }

        val textoCompleto = "$titulo $descripcion $categoria".lowercase()

        for (palabra in palabrasProhibidas) {
            if (textoCompleto.contains(palabra)) {
                return ResultadoRevision(
                    aceptada = false,
                    motivo = "La publicación contiene contenido no permitido: $palabra"
                )
            }
        }

        return ResultadoRevision(
            aceptada = true,
            motivo = "Publicación aprobada automáticamente."
        )
    }
}

data class ResultadoRevision(
    val aceptada: Boolean,
    val motivo: String
)