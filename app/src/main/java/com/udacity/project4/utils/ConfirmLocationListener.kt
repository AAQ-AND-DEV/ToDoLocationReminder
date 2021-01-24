package com.udacity.project4.utils

import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.model.PointOfInterest

interface ConfirmLocationListener {
    fun onConfirmLocation(dialog:DialogFragment, poi: PointOfInterest)
    fun onCancelLocation(dialog: DialogFragment, poi: PointOfInterest)
}