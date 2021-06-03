package com.sedra.nearbyplacesapp.data.model.photo

data class PhotoItem(
    val checkin: Checkin,
    val createdAt: Int,
    val height: Int,
    val id: String,
    val prefix: String,
    val source: Source,
    val suffix: String,
    val user: User,
    val visibility: String,
    val width: Int
)