package com.udacity.project4.utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import java.lang.ClassCastException
import java.lang.IllegalStateException

class SelectLocationDialogFragment(private val poi: PointOfInterest) : DialogFragment() {

    private lateinit var mListener: ConfirmLocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            mListener =  getTargetFragment() as ConfirmLocationListener
        } catch (e : ClassCastException){
            throw ClassCastException(context.toString() +
            " must implement ConfirmLocationListener")
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let{
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.alert_dialog_confirm_location)
                .setPositiveButton(R.string.confirm)
                    { dialog, which -> mListener.onConfirmLocation(this, poi) }
                .setNegativeButton(R.string.cancel)
                    { dialog, which -> mListener.onCancelLocation(this, poi) }

            builder.create()
        } ?:throw IllegalStateException("Activity cannot be null")
    }
}