package com.sedra.nearbyplacesapp.view

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
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
import com.sedra.nearbyplacesapp.LocationInteractor
import com.sedra.nearbyplacesapp.R
import com.sedra.nearbyplacesapp.data.model.Venue
import com.sedra.nearbyplacesapp.data.model.photo.PhotosMapper
import com.sedra.nearbyplacesapp.databinding.ActivityMainBinding
import com.sedra.nearbyplacesapp.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var preferences: SharedPreferences
    private var realtimeRequestUpdate: Boolean = true
    private val viewModel by viewModels<PlaceViewModel>()
    private lateinit var binding: ActivityMainBinding
    private val placeAdapter = PlaceAdapter()
    private val locationInteractor = LocationInteractor()

    private lateinit var realtimeOptionsMenuItem: MenuItem
    private lateinit var singleTimeOptionsMenuItem: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        realtimeRequestUpdate = preferences.getBoolean(IS_REAL_TIME, true)
        if (permissionGranted()) {
            getLastLocation()
        } else {
            requestPermissions()
        }
    }


    /**
     * Call this function to start fetching and observing user location
     * @param lat : latitude of user
     * @param lng : longitude of user
     */
    private fun getNearbyPlaces(lat: Double, lng: Double) {
        viewModel.getNearbyPlaces(lat, lng).observe(this) {
            it?.let { resource ->
                when (resource) {
                    is Resource.Error -> showErrorMessage(
                        getString(R.string.wrong_error),
                        R.drawable.connection_error
                    )
                    Resource.Loading -> if (placeAdapter.currentList.isEmpty()) showLoadingIndicator()
                    is Resource.Success -> if (resource.data.isEmpty()) {
                        showErrorMessage(getString(R.string.no_data), R.drawable.no_data)
                    } else {
                        populateAdapter(resource.data)
                    }
                }
            }
        }
    }

    /**
     * call to fill ui with data if valid venue
     * @param venueList: List of places returned by viewModel
     */
    private fun populateAdapter(venueList: List<Venue>) {
        binding.apply {
            placesRv.isVisible = true
            errorGroup.isVisible = false
            loaderGroup.isVisible = false
            placesRv.adapter = placeAdapter.also { it.submitList(venueList) }
        }
        getPlacesPhotos(venueList)
    }

    /**
     * Loop over list of places to fetch each place Image sync
     * @param venueList: current places list
     */
    private fun getPlacesPhotos(venueList: List<Venue>) {
        for (venue in venueList) {
            viewModel.getPlaceImage(venue.id).observe(this) {
                it?.let { resource ->
                    when (resource) {
                        is Resource.Error -> {
                        }
                        Resource.Loading -> {
                        }
                        is Resource.Success -> {
                            val item = PhotosMapper.getPhotoItem(resource.data)
                            if (item != null) {
                                placeAdapter.updateImageList(item, venueList.indexOf(venue))
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * show loader state on ui
     */
    private fun showLoadingIndicator() {
        binding.apply {
            loaderGroup.isVisible = true
            errorGroup.isVisible = false
            placesRv.isVisible = false
        }
    }

    /**
     * Show desired Error message and image on ui
     * @param message: message to be displayed according to situation
     */
    private fun showErrorMessage(message: String, image: Int) {
        binding.apply {
            errorImage.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, image))
            errorText.text = message
            errorGroup.isVisible = true
            loaderGroup.isVisible = false
            placesRv.isVisible = false
        }

    }


    // Observe location data in location interceptor
    private fun getLastLocation() {
        locationInteractor.locationLiveData.observe(this) {
            onLocationChanged(it)
        }
        locationInteractor.getLastLocation(this)
    }


    private fun onLocationChanged(location: Location?) {
        location?.let {
            getNearbyPlaces(location.latitude, location.longitude)
        }
        if (realtimeRequestUpdate) locationInteractor.startLocationUpdates()
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


    /**
     * Permission granted ? get user location
     */
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

    override fun onPause() {
        super.onPause()
        locationInteractor.stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        if (realtimeRequestUpdate) locationInteractor.startLocationUpdates()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        // Show suitable menu item according to mode (Realtime or single-time)
        realtimeOptionsMenuItem = menu.findItem(R.id.realtime_menu_option)
        realtimeOptionsMenuItem.isVisible = !realtimeRequestUpdate
        singleTimeOptionsMenuItem = menu.findItem(R.id.single_time_menu_option)
        singleTimeOptionsMenuItem.isVisible = realtimeRequestUpdate
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.realtime_menu_option -> {
                item.isVisible = false
                singleTimeOptionsMenuItem.isVisible = true
                changeLocationMode(true)
                true
            }
            R.id.single_time_menu_option -> {
                item.isVisible = false
                realtimeOptionsMenuItem.isVisible = true
                changeLocationMode(false)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    /**
     * Used to store current mode in preference and swap between starting
     * and stopping realtime track
     * @param isRealTime: Boolean indicating if user selected realtime mode
     */

    private fun changeLocationMode(isRealTime: Boolean) {
        realtimeRequestUpdate = isRealTime
        if (realtimeRequestUpdate) {
            locationInteractor.startLocationUpdates()
        } else {
            locationInteractor.stopLocationUpdates()
        }
        viewModel.changeLocationMode(isRealTime)
    }

    override fun onDestroy() {
        locationInteractor.onDestroy()
        super.onDestroy()
    }


    companion object {
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
        const val IS_REAL_TIME = "is_realtime"
    }
}