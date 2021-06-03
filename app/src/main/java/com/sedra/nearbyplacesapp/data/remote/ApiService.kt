package com.sedra.nearbyplacesapp.data.remote

import com.sedra.nearbyplacesapp.data.model.NearByPlacesResponse
import com.sedra.nearbyplacesapp.data.model.photo.PhotoResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    companion object {
        const val BASE_URL = "https://api.foursquare.com/"
    }

    @GET("/v2/venues/explore")
    suspend fun getRecommendedPlaces(
        @Query("client_id") client_id: String,
        @Query("client_secret") client_secret: String,
        @Query("ll") latLng: String,
        @Query("v") version: String,
        @Query("radius") radius: Int,
    ): NearByPlacesResponse


    @GET("/v2/venues/{venueId}/photos")
    suspend fun getPlaceImage(
        @Path("venueId") venueId: String,
        @Query("client_id") client_id: String,
        @Query("client_secret") client_secret: String,
        @Query("v") version: String
    ): PhotoResponse

}