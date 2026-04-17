package com.example.truekita

// Solo un archivo con esto, así todos lo comparten
data class ProductUi(
    val title: String,
    val price: String,
    val seller: String,
    val condition: String,
    val status: String,
    val imageRes: Int,
    val type: String // <--- AGREGA ESTA LÍNEA
)