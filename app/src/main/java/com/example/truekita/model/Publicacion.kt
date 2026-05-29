package com.example.truekita.model

data class Publicacion(
    val id: String = "",
    val usuarioId: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val precio: Double = 0.0,
    val imagenUrl: String = "",
    val estado: String = "pendiente",
    val motivoRechazo: String = "",
    val fecha: Long = System.currentTimeMillis()
)