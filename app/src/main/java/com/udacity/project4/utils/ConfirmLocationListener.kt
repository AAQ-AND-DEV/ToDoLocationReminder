package com.udacity.project4.utils

import androidx.fragment.app.DialogFragment

interface ConfirmLocationListener {
    fun onConfirmLocation(dialog:DialogFragment)
    fun onCancelLocation(dialog: DialogFragment)
}