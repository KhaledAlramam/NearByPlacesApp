package com.sedra.nearbyplacesapp.data.model

data class Response(
    val groups: List<Group>,
    val headerFullLocation: String,
    val headerLocation: String,
    val headerLocationGranularity: String,
    val suggestedRadius: Int,
    val totalResults: Int
)