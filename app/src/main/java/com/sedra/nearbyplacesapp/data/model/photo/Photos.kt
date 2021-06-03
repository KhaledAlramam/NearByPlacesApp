package com.sedra.nearbyplacesapp.data.model.photo

data class Photos(
    val count: Int,
    val dupesRemoved: Int,
    val items: List<PhotoItem>
)