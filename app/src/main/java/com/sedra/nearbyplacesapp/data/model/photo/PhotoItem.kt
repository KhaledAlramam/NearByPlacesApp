package com.sedra.nearbyplacesapp.data.model.photo

data class PhotoItem(
    val createdAt: Int,
    val height: Int,
    val id: String,
    val prefix: String,
    val suffix: String,
    val visibility: String,
    val width: Int
)