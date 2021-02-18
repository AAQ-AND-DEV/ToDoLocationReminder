package com.udacity.project4

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.runBlocking

class FakeAndroidTestRepository : ReminderDataSource{

    var remindersServiceData: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()

    private var shouldReturnError = false

    private val TAG = "FakeAndroidTestRepo"

    private val observableReminders = MutableLiveData<Result<List<ReminderDTO>>>()

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    suspend fun refreshReminders() {

        observableReminders.value = getReminders()
    }

    fun observeReminders(): LiveData<Result<List<ReminderDTO>>> {
        runBlocking {
            refreshReminders()
        }
        return observableReminders
    }

    fun observeReminder(reminderId: String): LiveData<Result<ReminderDTO>> {
        runBlocking { refreshReminders() }
        return observableReminders.map { reminders ->
            when (reminders) {
                is Result.Error -> Result.Error(reminders.message)
                is Result.Success -> {
                    val reminder = reminders.data.firstOrNull() {
                        it.id == reminderId
                    } ?: return@map Result.Error("Not found")
                    Result.Success(reminder)
                }
            }
        }
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Test exception")
        }
        return Result.Success(remindersServiceData.values.toList())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersServiceData[reminder.id] = reminder
        Log.d(TAG, "${remindersServiceData[reminder.id]}")
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("Test exception")
        }
        remindersServiceData[id]?.let {
            return Result.Success(it)
        }
        return Result.Error("could not find task")
    }

    override suspend fun deleteAllReminders() {
        remindersServiceData.clear()
        refreshReminders()
    }

    fun addReminders(vararg reminders: ReminderDTO) {
        for (reminder in reminders) {
            remindersServiceData[reminder.id] = reminder
        }
        runBlocking { refreshReminders() }
    }
}