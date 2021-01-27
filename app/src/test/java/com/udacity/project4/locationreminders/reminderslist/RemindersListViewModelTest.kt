package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.MyApp
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeTestRepository
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersRepo: FakeTestRepository

    private lateinit var remindersListViewModel: RemindersListViewModel

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel(){
        val app = getApplicationContext<MyApp>()
        remindersRepo = FakeTestRepository()
        val reminder1 = ReminderDTO("Title1", "Desc1", "Loc1", 45.5, 45.5)
        val reminder2 = ReminderDTO("Title2", "Desc2", "Loc2", 48.5, 48.5)
        val reminder3 = ReminderDTO("Title3", "Desc3", "Loc3", 42.5, 42.5)
        remindersRepo.addReminders(reminder1, reminder2, reminder3)
        remindersListViewModel = RemindersListViewModel(app, remindersRepo)
    }

    @Test
    fun loadReminders(){
        //Given: repo set up with 3 reminders

        //When: loadReminders called
        remindersListViewModel.loadReminders()
        //Then: viewModel contains data
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue()?.size, `is`(3))
    }

    @Test
    fun clearRepo_checkShowNoData(){
        //Given: repo reset
        runBlocking {
        remindersRepo.deleteAllReminders()

        }
        //When: loadReminders(calls invalidateShowNoData())
        remindersListViewModel.loadReminders()
        //Then: viewModel showNoData true
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true) )
    }

    @Test
    fun showLoading(){
        //Given: repo with 3 reminders
        mainCoroutineRule.pauseDispatcher()
        //When: loadReminders called
        remindersListViewModel.loadReminders()
        //Then: show loading true
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))
        //Then: when dispatcher resumed, showLoading is false
        mainCoroutineRule.resumeDispatcher()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun showToastOnError(){
        //Given: Repo set to return error
        remindersRepo.setReturnError(true)

        //When: loadReminders() called
        remindersListViewModel.loadReminders()
        //Then: showSnackbar true
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`("Test exception"))
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().isNullOrEmpty(), `is`(true))
    }
}