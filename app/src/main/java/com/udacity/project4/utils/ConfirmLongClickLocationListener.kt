package com.udacity.project4.utils

import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest

interface ConfirmLongClickLocationListener {
    fun onConfirmLocation(dialog: DialogFragment, latLon: LatLng)
    fun onCancelLocation(dialog: DialogFragment, latLon: LatLng)

}