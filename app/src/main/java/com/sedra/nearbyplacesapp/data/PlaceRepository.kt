package com.sedra.nearbyplacesapp.data

import com.sedra.nearbyplacesapp.data.remote.ApiService
import javax.inject.Inject

class PlaceRepository @Inject constructor(
    private val service: ApiService
) {


    suspend fun getRecommendedPlaces(
        clientId: String,
        clientSecret: String,
        latLng: String,
        version: String,
        radius: Int
    ) = service.getRecommendedPlaces(clientId, clientSecret, latLng, version, radius)

    suspend fun getPlaceImage(
        clientId: String,
        clientSecret: String,
        version: String,
        venueId: String
    ) = service.getPlaceImage(venueId, clientId, clientSecret, version)
}