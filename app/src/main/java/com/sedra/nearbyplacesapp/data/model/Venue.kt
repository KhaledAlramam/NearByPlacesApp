package com.sedra.nearbyplacesapp.data.model

data class Venue(
    val categories: List<Category>,
    val id: String,
    val location: Location,
    val name: String,
)