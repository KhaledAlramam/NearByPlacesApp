package com.sedra.nearbyplacesapp.util

/**
 * Class to represent the 3 states of network call
 * each will be emitted to live data and handled according to logic
 */
sealed class Resource<T> {
    object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
}