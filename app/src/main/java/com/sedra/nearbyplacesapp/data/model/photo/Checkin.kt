package com.sedra.nearbyplacesapp.data.model.photo

data class Checkin(
    val createdAt: Int,
    val id: String,
    val timeZoneOffset: Int,
    val type: String
)