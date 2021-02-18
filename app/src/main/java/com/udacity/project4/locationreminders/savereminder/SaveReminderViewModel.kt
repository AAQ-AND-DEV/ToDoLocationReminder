package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.*

class SaveReminderViewModel(val app: Application, val dataSource: ReminderDataSource) :
    BaseViewModel(app) {
    val reminderTitle = MutableLiveData<String>()
    val reminderDescription = MutableLiveData<String>()
    val reminderSelectedLocationStr = MutableLiveData<String>()
    val selectedPOI = MutableLiveData<PointOfInterest>()
    val latitude = MutableLiveData<Double>()
    val longitude = MutableLiveData<Double>()

    private val TAG = this.javaClass.simpleName

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminderTitle.value = null
        reminderDescription.value = null
        reminderSelectedLocationStr.value = null
        selectedPOI.value = null
        latitude.value = null
        longitude.value = null
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    fun validateAndSaveReminder(reminderData: ReminderDataItem) {
        Log.d(TAG,"calling validateEnteredData()")
        val validated = validateEnteredData(reminderData)
        Log.d(TAG, "validated: $validated")
        if (validated) {
            Log.d(TAG,"data validated")
            saveReminder(reminderData)
        }
    }

    fun checkReminderPresent(reminderData: ReminderDataItem): Boolean {
        Log.d(TAG, "id is ${reminderData.id}")

        var present = false
        val result = viewModelScope.launch {
            present = getReminderStatus(reminderData.id)
        }

        //Log.d(TAG, "$data is data, $present is present")

        return present
    }

    suspend fun getReminderStatus(id: String): Boolean {
        var data: Result<ReminderDTO>?
        var present = false
        data = runBlocking {
            dataSource.getReminder(id)
        }
        val actualData = data
        present = if (actualData is Result.Success<*>) {
            Log.d(TAG, "$actualData is Result.Success, present: $present")
            true
        } else {
            false
        }
        Log.d(TAG, "$actualData is Result.Success, present: $present")
        return present
    }

    /**
     * Save the reminder to the data source
     */
    fun saveReminder(reminderData: ReminderDataItem) {
        showLoading.value = true
        viewModelScope.launch {
            dataSource.saveReminder(
                ReminderDTO(
                    reminderData.title,
                    reminderData.description,
                    reminderData.location,
                    reminderData.latitude,
                    reminderData.longitude,
                    reminderData.id
                )
            )
            Log.d(TAG, "data should be saved now")
            showLoading.value = false
            showToast.value = app.getString(R.string.reminder_saved)

        }
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    fun validateEnteredData(reminderData: ReminderDataItem): Boolean {
        if (reminderData.title.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

//        if (reminderData.location.isNullOrEmpty()) {
//            showSnackBarInt.value = R.string.err_select_location
//            return false
//        }
        return true
    }
}