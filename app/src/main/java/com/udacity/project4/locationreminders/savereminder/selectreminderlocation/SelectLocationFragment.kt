package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
import com.udacity.project4.locationreminders.savereminder.REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.*
import org.koin.android.ext.android.inject
import java.math.BigDecimal
import java.math.RoundingMode


const val CONFIRM_DIALOG_TAG = 47
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback, ConfirmLongClickLocationListener, ConfirmLocationListener {

    private val DEFAULT_ZOOM = 13f
    private val TAG = SelectLocationFragment::class.java.simpleName


    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private var map: GoogleMap? = null
    private var selectedMarker : Marker? = null

    //for locationService
    private var lastKnownLocation: Location? = null
    private val defaultLocation = LatLng(75.45, 242.34)
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    // GeoDataClient deprecated (and probably not necessary for this project
    // for GeoDataClient
    // private lateinit var mGeoDataClient : GeoDataClient

    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    override fun onResume() {
        super.onResume()
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            //zoom to the user location if granted
            getDeviceLocation()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        //TODO Add savedInstanceState fetch of lastKnownLocation, selectedMarker?

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//      map setup implementation
        val mapFrag = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFrag.getMapAsync(this)

        mFusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this.requireActivity())

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap


        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this.requireContext(), R.raw.cool_style
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }

        if (foregroundAndBackgroundLocationPermissionApproved()) {
            //zoom to the user location if granted
            getDeviceLocation()
        } else{
            checkPermissionsAndGetDeviceLocation()
        }

//  taking user permission
        enableMyLocation()

        setPoiClick(map!!)

        setLongClick(map!!)
    }

    /**
     * Starts the permission check and Geofence process only if the Geofence associated with the
     * current hint isn't yet active.
     */
    private fun checkPermissionsAndGetDeviceLocation() {

        if (foregroundAndBackgroundLocationPermissionApproved()) {
            getDeviceLocation()
        } else {
            requestForegroundAndBackgroundLocationPermissions()
        }
    }

    override fun onConfirmLocation(dialog: DialogFragment, poi: PointOfInterest) {

        onLocationSelected(poi)
    }

    override fun onCancelLocation(dialog: DialogFragment, poi: PointOfInterest) {
        selectedMarker?.remove()
    }

    private fun setLongClick(map: GoogleMap){
        map.setOnMapLongClickListener { latLon ->
            val longClickMarker = map.addMarker(
                MarkerOptions()
                    .position(latLon)
                    .title(latLon.toString())
            )
            selectedMarker = longClickMarker
            longClickMarker?.showInfoWindow()
            confirmLongClick(latLon)
        }

    }
    override fun onConfirmLocation(dialog: DialogFragment, latLon: LatLng) {
        onLocationSelected(latLon)
    }

    override fun onCancelLocation(dialog: DialogFragment, latLon: LatLng) {
        selectedMarker?.remove()
    }

    private fun setPoiClick(map: GoogleMap) {
//       put a marker to location that the user selected

        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            selectedMarker = poiMarker
            poiMarker?.showInfoWindow()
            confirmClick(poi)
        }
    }

    private fun confirmClick(myPoi: PointOfInterest) {
        showConfirmDialog(myPoi)
    }

    private fun showConfirmDialog(myPoi: PointOfInterest){
        val confirmFragment = SelectLocationDialogFragment(myPoi)
        confirmFragment.setTargetFragment(this, CONFIRM_DIALOG_TAG)
        confirmFragment.show(parentFragmentManager, "confirmation")

    }
    private fun confirmLongClick(latLng: LatLng) {
        showConfirmDialog(latLng)
    }

    private fun showConfirmDialog(latLng: LatLng){
        val confirmFragment = SelectLongClickLocationDialogFragment(latLng)
        confirmFragment.setTargetFragment(this, CONFIRM_DIALOG_TAG)
        confirmFragment.show(parentFragmentManager, "confirmation")

    }

    private fun onLocationSelected(poi: PointOfInterest) {
        // send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        _viewModel.selectedPOI.value = poi
        _viewModel.reminderSelectedLocationStr.value = poi.name
        _viewModel.latitude.value = poi.latLng.latitude
        _viewModel.longitude.value = poi.latLng.longitude
        findNavController().popBackStack()
    }

    private fun onLocationSelected(latLng: LatLng){
        _viewModel.selectedLocation.value = latLng

        _viewModel.reminderSelectedLocationStr.value = latLng.clipString()
        _viewModel.latitude.value = latLng.latitude
        _viewModel.longitude.value = latLng.longitude
        findNavController().popBackStack()
    }

    fun LatLng.clipString(): String{
        val roundedLat = BigDecimal(this.latitude).setScale(5, RoundingMode.HALF_EVEN)
        val roundedLon = BigDecimal(this.longitude).setScale(5, RoundingMode.HALF_EVEN)
        return "$roundedLat, $roundedLon"

    }

    //Code provided from documentation:
    // https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial#get-the-location-of-the-android-device-and-position-the-map
    private fun getDeviceLocation() {
        try {
            if (foregroundAndBackgroundLocationPermissionApproved()) {
                val locationResult = mFusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this.requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            map?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    ), DEFAULT_ZOOM
                                )
                            )
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        map?.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM)
                        )
                        map?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // Change the map type based on the user's selection.
        R.id.normal_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

//    private fun isPermissionGranted(): Boolean {
//
//        val res = this.context?.let {
//            ContextCompat.checkSelfPermission(
//                it,
//                android.Manifest.permission.ACCESS_FINE_LOCATION
//            )
//        } == PackageManager.PERMISSION_GRANTED
//        if (res) {
//            locationPermissionGranted = true
//        } else {
//            this.activity?.let {
//                ActivityCompat.requestPermissions(
//                    it,
//                    arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION),
//                    REQUEST_LOCATION_PERMISSION
//                )
//            }
//        }
//        return res
//    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (map == null) {
            return
        }
        try {

            if (foregroundAndBackgroundLocationPermissionApproved()) {
                val locMap = map
                locMap?.isMyLocationEnabled = true
            } else {
                map?.isMyLocationEnabled = false
                map?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null

            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
//        if (requestCode == REQUEST_LOCATION_PERMISSION) {
//            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                enableMyLocation()
//            }
//        }
        Log.d(TAG, "onRequestPermissionResult")

        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED)) {
                        Log.d(TAG, "permission has been denied")
            Snackbar.make(
                binding.root,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else {
            checkPermissionsAndGetDeviceLocation()
        }
    }

    /*
    *  Determines whether the app has the appropriate permissions across Android 10+ and all other
    *  Android versions.
    */
    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            this.requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ))
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            this.requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    /*
    *  Requests ACCESS_FINE_LOCATION and (on Android 10+ (Q) ACCESS_BACKGROUND_LOCATION.
    */
    @TargetApi(29)
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (foregroundAndBackgroundLocationPermissionApproved())
            return
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = when {
            runningQOrLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
        Log.d(TAG, "Request foreground or fg and bg location permission")
        requestPermissions(
            permissionsArray,
            resultCode
        )
    }


}
