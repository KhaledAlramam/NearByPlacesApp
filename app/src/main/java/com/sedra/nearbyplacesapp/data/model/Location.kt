package com.sedra.nearbyplacesapp.data.model

data class Location(
    val address: String,
    val cc: String,
    val city: String,
    val country: String,
    val crossStreet: String,
    val distance: Int,
    val formattedAddress: List<String>,
    val lat: Double,
    val lng: Double,
    val postalCode: String,
    val state: String
)