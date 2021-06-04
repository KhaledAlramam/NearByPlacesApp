package com.sedra.nearbyplacesapp.view

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.sedra.nearbyplacesapp.data.PlaceRepository
import com.sedra.nearbyplacesapp.data.model.ItemsMapper
import com.sedra.nearbyplacesapp.util.CLIENT_ID
import com.sedra.nearbyplacesapp.util.CLIENT_SECRET
import com.sedra.nearbyplacesapp.util.Resource
import com.sedra.nearbyplacesapp.util.VERSION
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class PlaceViewModel @Inject constructor(
    private val repository: PlaceRepository,
    private val preferences: SharedPreferences
) : ViewModel() {

    fun getNearbyPlaces(latitude: Double, longitude: Double) = liveData(Dispatchers.IO) {
        emit(Resource.Loading)
        try {
            val response = repository.getRecommendedPlaces(
                CLIENT_ID,
                CLIENT_SECRET,
                "$latitude,$longitude",
                VERSION,
                1000
            )
            emit(
                Resource.Success(
                    data = ItemsMapper.getItems(response.response)
                )
            )
        } catch (exception: Exception) {
            emit(
                Resource.Error(
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
        emit(Resource.Loading)
        try {
            emit(
                Resource.Success(
                    data = repository.getPlaceImage(
                        CLIENT_ID,
                        CLIENT_SECRET,
                        VERSION,
                        venueId
                    )
                )
            )
        } catch (exception: Exception) {
            emit(
                Resource.Error(
                    message = exception.localizedMessage ?: "Error Occurred"
                )
            )
        }
    }

}