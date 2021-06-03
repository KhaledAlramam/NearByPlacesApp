package com.sedra.nearbyplacesapp.data.model

data class Meta(
    val code: Int,
    val requestId: String,
    val errorType: String?,
    val errorDetails: String?
)