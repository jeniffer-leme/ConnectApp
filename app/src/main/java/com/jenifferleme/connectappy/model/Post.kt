package com.jenifferleme.connectappy.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Post(
    val texto: String = "",
    val imagem: String = "", // Aqui será armazenada a String Base64
    val localizacao: String = "",
    val autor: String = "",
    @ServerTimestamp val data: Date? = null
)