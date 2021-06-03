package com.sedra.nearbyplacesapp.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.sedra.nearbyplacesapp.R
import com.sedra.nearbyplacesapp.data.model.Group
import com.sedra.nearbyplacesapp.data.model.Venue
import com.sedra.nearbyplacesapp.databinding.ActivityMainBinding
import com.sedra.nearbyplacesapp.util.Status.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var preferences: SharedPreferences
    private var realtimeRequestUpdate: Boolean = true
    private val viewModel by viewModels<PlaceViewModel>()
    private var binding: ActivityMainBinding? = null
    private val placeAdapter = PlaceAdapter()
    private var fusedLocationProvider: FusedLocationProviderClient? = null
    private var realtimeOptionsMenuItem: MenuItem? = null
    private var singleTimeOptionsMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        realtimeRequestUpdate = preferences.getBoolean(IS_REAL_TIME, true)
        Log.e("TAG", "onCreate: $realtimeRequestUpdate")
        if (permissionGranted()) {
            getLastLocation()
        } else {
            requestPermissions()
        }

    }

    // call api to get places after fetching location
    private fun getNearbyPlaces(lat: Double, lng: Double) {
        viewModel.getNearbyPlaces(lat, lng).observe(this) {
            it?.let { resource ->
                when (resource.status) {
                    SUCCESS -> {
                        resource.data?.let { nearByPlacesResponse ->
                            if (nearByPlacesResponse.response.groups.isEmpty()) {
                                showErrorMessage(getString(R.string.no_data), R.drawable.no_data)
                            } else {
                                populateAdapter(nearByPlacesResponse.response.groups)
                            }
                        }
                    }
                    ERROR -> {
                        showErrorMessage(
                            getString(R.string.wrong_error),
                            R.drawable.connection_error
                        )
                    }
                    LOADING -> {
                        if (placeAdapter.currentList.isEmpty()) showLoadingIndicator()
                    }
                }
            }
        }
    }

    // fill adapter with data, Hide all views except recyclerView
    private fun populateAdapter(groups: List<Group>) {
        val venueList = groups[0].items.map {
            it.venue
        }
        binding?.apply {
            placesRv.isVisible = true
            errorGroup.isVisible = false
            loaderGroup.isVisible = false
            placesRv.adapter = placeAdapter.also { it.submitList(venueList) }
        }
        getPlacesPhotos(venueList)
    }


    /**
     * take places list, loop over them and get each place photo
     * call placeAdapter function to notify each place item about its new image
     * */
    private fun getPlacesPhotos(venueList: List<Venue>) {
        for (venue in venueList) {
            viewModel.getPlaceImage(venue.id).observe(this) {
                it?.let { resource ->
                    when (resource.status) {
                        SUCCESS -> {
                            resource.data?.let { photoResponse ->
                                if (photoResponse.response.photos.items.isNotEmpty()) {
                                    placeAdapter.updateImageList(
                                        photoResponse.response.photos.items[0],
                                        venueList.indexOf(venue)
                                    )
                                }
                            }
                        }
                        ERROR -> {
                            Log.e("TAG", "getPlacesPhotos: ${resource.message}")
                        }
                        LOADING -> {
                        }
                    }
                }
            }
        }
    }

    // Show Loading state and hide other views
    private fun showLoadingIndicator() {
        binding?.apply {
            loaderGroup.isVisible = true
            errorGroup.isVisible = false
            placesRv.isVisible = false
        }

    }

    // Helper function to show error message according to parameters
    private fun showErrorMessage(message: String, image: Int) {
        binding?.apply {
            errorImage.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, image))
            errorText.text = message
            errorGroup.isVisible = true
            loaderGroup.isVisible = false
            placesRv.isVisible = false
        }

    }


    @SuppressLint("MissingPermission")
    fun getLastLocation() {
        fusedLocationProvider = getFusedLocationProviderClient(this)
        fusedLocationProvider!!.lastLocation
            .addOnSuccessListener { location -> // GPS location can be null if GPS is switched off
                onLocationChanged(location)
            }
            .addOnFailureListener { e ->
                showErrorMessage(getString(R.string.wrong_error), R.drawable.connection_error)
                e.printStackTrace()
            }
    }

    private fun onLocationChanged(location: Location?) {
        location?.let {
            getNearbyPlaces(location.latitude, location.longitude)
        }
    }

    //Helper function to check if location permission is granted

    private fun permissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            MY_PERMISSIONS_REQUEST_LOCATION
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        getLastLocation()
                    }

                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        realtimeOptionsMenuItem = menu.findItem(R.id.realtime_menu_option)
        realtimeOptionsMenuItem?.isVisible = !realtimeRequestUpdate
        singleTimeOptionsMenuItem = menu.findItem(R.id.single_time_menu_option)
        singleTimeOptionsMenuItem?.isVisible = realtimeRequestUpdate
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.realtime_menu_option -> {
                item.isVisible = false
                singleTimeOptionsMenuItem?.isVisible = true
                changeLocationMode(true)
                true
            }
            R.id.single_time_menu_option -> {
                item.isVisible = false
                realtimeOptionsMenuItem?.isVisible = true
                changeLocationMode(false)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    // Used to store current mode in preference and swap between starting
    // and stopping realtime track

    private fun changeLocationMode(isRealTime: Boolean) {
        realtimeRequestUpdate = isRealTime
        val editor = preferences.edit()
        editor.putBoolean(IS_REAL_TIME, isRealTime)
        editor.apply()
    }

    override fun onDestroy() {
        binding = null
        realtimeOptionsMenuItem = null
        singleTimeOptionsMenuItem = null
        fusedLocationProvider = null
        super.onDestroy()
    }


    companion object {
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
        private const val IS_REAL_TIME = "is_realtime"
    }
}