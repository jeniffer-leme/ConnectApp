package com.jenifferleme.connectappy.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.Timestamp

data class Post(
    @get:Exclude var id: String = "",
    val autor: String = "",
    val data: Timestamp? = null,
    val imagem: String = "",
    val localizacao: String = "",
    val texto: String = ""
)