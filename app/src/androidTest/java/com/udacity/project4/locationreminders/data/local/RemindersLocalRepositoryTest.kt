package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.succeeded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    lateinit var localDataSource: RemindersLocalRepository
    lateinit var db: RemindersDatabase

    @Before
    fun setUp(){
        db = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries()
            .build()

        localDataSource = RemindersLocalRepository(
            db.reminderDao(), Dispatchers.Main
        )
    }

    @After
    fun tearDown(){
        db.close()
    }
    suspend fun addReminders() {

        val reminderList = mutableListOf<ReminderDTO>()
        val reminder = ReminderDTO("title1", "desc1", null, null, null)
        val reminder2 = ReminderDTO("title2", "desc2", null, null, null)
        val reminder3 = ReminderDTO("title3", "desc3", null, null, null)

        reminderList.add(reminder)
        reminderList.add(reminder2)
        reminderList.add(reminder3)

        for (remind in reminderList) {
            db.reminderDao().saveReminder(remind)
        }
    }

    @Test
    fun saveReminder_retrieveReminder() = runBlocking{
        //Given: new reminder saved in db
        val reminder = ReminderDTO("title1", "desc1", null, null, null)
        localDataSource.saveReminder(reminder)

        //When: Reminder retrieved by ID
        val result = localDataSource.getReminder(reminder.id)

        //Then: same reminder returned
        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        assertThat(result.data.title, `is`("title1"))
        assertThat(result.data.description, `is`("desc1"))
    }

    @Test
    fun getAllReminders_clearAllReminders() = runBlockingTest {

        //Given: save 3 reminders to db
        addReminders()
        //get list; confirm list size
        var loadedReminders: MutableList<ReminderDTO> =
            db.reminderDao().getReminders() as MutableList<ReminderDTO>
        assertThat(loadedReminders.size, `is`(3))

        //When: deleteAllReminders called
        db.reminderDao().deleteAllReminders()
        loadedReminders = db.reminderDao().getReminders() as MutableList<ReminderDTO>
        assertThat(loadedReminders.size, `is`(0))
    }

    @Test
    fun tryToGetNonExistentReminderById() = runBlocking{
        //Given: empty Reminders
        //When: try retrieve Reminder by Id
        val id = "5"
        val result = localDataSource.getReminder(id)
        //Then: Result.Error returned
        assertThat(result.succeeded, `is`(false))
        result as Result.Error
        assertThat(result.message, `is`("Reminder not found!"))
    }

}