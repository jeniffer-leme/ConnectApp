package com.jenifferleme.connectappy.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.Timestamp

data class Post(
    @get:Exclude var id: String = "", // Usaremos para o Delete depois
    val autor: String = "",
    val data: Timestamp? = null, // Usaremos este para a ordenação
    val imagem: String = "",     // A String Base64
    val localizacao: String = "",
    val texto: String = ""
)