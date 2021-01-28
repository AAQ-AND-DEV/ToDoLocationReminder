package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: RemindersDatabase

    @Before
    fun setupDb() {
        db = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
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

    @After
    fun tearDown() = db.close()

    @Test
    fun saveReminderAndGetById() = runBlockingTest {
        //Given: save Reminder
        val reminder = ReminderDTO("title1", "desc1", null, null, null)
        db.reminderDao().saveReminder(reminder)

        //When: get reminder by id
        val loaded = db.reminderDao().getReminderById(reminder.id)

        //Then: loaded contains expected values
        assertThat(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
    }

    @Test
    fun clearAllReminders() = runBlockingTest {

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
}