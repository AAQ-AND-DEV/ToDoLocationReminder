package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.FakeAndroidTestRepository
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationViewModel
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : KoinTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    val repo: FakeAndroidTestRepository by inject()

    val authViewModel: AuthenticationViewModel by inject()
//    @get:Rule
//    val activityRule = ActivityTestRule(RemindersActivity::class.java)

    private lateinit var auth: FirebaseAuth

    val testModule = module {
        viewModel {

            RemindersListViewModel(
                get(),
                get() as FakeAndroidTestRepository
            )
        }
        viewModel {
            AuthenticationViewModel(get())
        }
        single {
            Room.inMemoryDatabaseBuilder(
                get(),
                RemindersDatabase::class.java
            )
                .allowMainThreadQueries()
                .build()
        }
        single {
            FakeAndroidTestRepository()
        }
        single {
            LocalDB.createRemindersDao(androidContext())
        }
    }


    @Before
    fun setupKoin() {
        stopKoin()
        startKoin {
            androidContext(getApplicationContext())
            loadKoinModules(testModule)
        }
        auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword("test@aaqanddev.com", "password")
        authViewModel._navigateToAuthActivity.value = false

    }

//    @Before
//    fun loginToAuth() {
//       }

    @After
    fun cleanup() {
        runBlocking {
            repo.deleteAllReminders()

        }
        stopKoin()
    }
//    test the navigation of the fragments.

    @Test
    fun clickNewReminderButton_navigateToAddReminder() = runBlockingTest{
        //Given: in RemindersFragment
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        //When: newReminder Button clicked
        onView(withId(R.id.addReminderFAB)).perform(click())
        //Then: verify navigation to SaveReminderFragment
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    //    test the displayed data on the UI.
    @Test
    fun noData_showNoDataIcon() {
        //Given: no data added to repo
        runBlocking {
            repo.deleteAllReminders()
        }

        //When: launch fragment
        launchFragmentInContainer<ReminderListFragment>(
            Bundle(), R.style.AppTheme
        )
        //Then: noDataTextView displayed
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

    @Test
    fun loadReminders_verifyMatches() {
        //Given: 3 items added to repo
        addRemindersToRepo()

        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(withText("title1")).check(matches(isDisplayed()))
        onView(withText("title2")).check(matches(isDisplayed()))
    }

       //testing for the error messages.
    @Test
    fun repoReturnsError() {
        //Given: Repo set to return error
        repo.setReturnError(true)
        //When: launchFragment
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        //Then:
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText("Test exception")))

    }

    private fun addRemindersToRepo() {

        val reminder = ReminderDTO("title1", "desc1", null, null, null)
        val reminder2 = ReminderDTO("title2", "desc2", null, null, null)
        val reminder3 = ReminderDTO("title3", "desc3", null, null, null)
        repo.addReminders(reminder, reminder2, reminder3)
    }
}