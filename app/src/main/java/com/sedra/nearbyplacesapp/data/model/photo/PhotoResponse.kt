package com.sedra.nearbyplacesapp.data.model.photo

data class PhotoResponse(
    val meta: Meta,
    val response: Response
)


object PhotosMapper {
    fun getPhotoItem(response: PhotoResponse): PhotoItem? {
        if (response.response.photos.items.isEmpty()) return null
        return response.response.photos.items[0]
    }
}