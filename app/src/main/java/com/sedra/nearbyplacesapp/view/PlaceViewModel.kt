package com.sedra.nearbyplacesapp.view

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.sedra.nearbyplacesapp.data.PlaceRepository
import com.sedra.nearbyplacesapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class PlaceViewModel @Inject constructor(
    private val repository: PlaceRepository,
    private val preferences: SharedPreferences
) : ViewModel() {

    fun getNearbyPlaces(latitude: Double, longitude: Double) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = repository.getRecommendedPlaces(
                        "W1BBIACSZHMTRJ4M5TJ5R2SR2YINIS5AO2WYGUV5OO3QW4BE",
                        "HSVOREBK5XNHLHPZ0AS10QIHOPWSJPJTDOJKRFZDTJREYTME",
                        "$latitude,$longitude",
                        "20210602",
                        1000
                    )
                )
            )
        } catch (exception: Exception) {
            emit(
                Resource.error(
                    data = null,
                    message = exception.localizedMessage ?: "Error Occurred"
                )
            )
        }
    }

    fun changeLocationMode(isRealTime: Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(MainActivity.IS_REAL_TIME, isRealTime)
        editor.apply()
    }

    fun getPlaceImage(venueId: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(
                Resource.success(
                    data = repository.getPlaceImage(
                        "W1BBIACSZHMTRJ4M5TJ5R2SR2YINIS5AO2WYGUV5OO3QW4BE",
                        "HSVOREBK5XNHLHPZ0AS10QIHOPWSJPJTDOJKRFZDTJREYTME",
                        "20210602",
                        venueId
                    )
                )
            )
        } catch (exception: Exception) {
            emit(
                Resource.error(
                    data = null,
                    message = exception.localizedMessage ?: "Error Occurred"
                )
            )
        }
    }

}