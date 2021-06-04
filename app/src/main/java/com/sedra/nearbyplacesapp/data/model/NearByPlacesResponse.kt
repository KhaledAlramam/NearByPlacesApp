package com.sedra.nearbyplacesapp.data.model

data class NearByPlacesResponse(
    val meta: Meta,
    val response: Response
)
object ItemsMapper {
    fun getItems(response: Response): List<Venue> {
        if (response.groups.isEmpty() || response.groups[0].items.isEmpty()) return emptyList()
        return response.groups[0].items.map {
            it.venue
        }
    }
}