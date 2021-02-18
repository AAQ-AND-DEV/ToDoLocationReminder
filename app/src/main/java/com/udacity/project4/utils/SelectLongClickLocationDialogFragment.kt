package com.udacity.project4.utils

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.R
import java.lang.ClassCastException
import java.lang.IllegalStateException

class SelectLongClickLocationDialogFragment(private val latLong: LatLng) :  DialogFragment() {

    private lateinit var mListener: ConfirmLongClickLocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            mListener =  getTargetFragment() as ConfirmLongClickLocationListener
        } catch (e : ClassCastException){
            throw ClassCastException(context.toString() +
                    " must implement ConfirmLongClickLocationListener")
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let{
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.alert_dialog_confirm_location)
                .setPositiveButton(R.string.confirm)
                { dialog, which -> mListener.onConfirmLocation(this, latLong) }
                .setNegativeButton(R.string.cancel)
                { dialog, which -> mListener.onCancelLocation(this, latLong) }

            builder.create()
        } ?:throw IllegalStateException("Activity cannot be null")
    }
}