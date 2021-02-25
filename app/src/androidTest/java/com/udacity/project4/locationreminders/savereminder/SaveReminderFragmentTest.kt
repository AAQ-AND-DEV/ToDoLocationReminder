package com.udacity.project4.locationreminders.savereminder

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.FakeAndroidTestRepository
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationViewModel
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.koin.test.KoinTest

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.inject
import org.koin.test.inject
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class SaveReminderFragmentTest : KoinTest{

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule
        .grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @get:Rule
    val activityRule: ActivityTestRule<RemindersActivity> =
        ActivityTestRule(RemindersActivity::class.java)

    val viewModel : SaveReminderViewModel by inject()
    //private lateinit var viewModel: SaveReminderViewModel

    private lateinit var auth: FirebaseAuth
    val authViewModel: AuthenticationViewModel by inject()

    val repo: FakeAndroidTestRepository by inject()

    val testModule = module{
        viewModel{
            SaveReminderViewModel(
                get(), FakeAndroidTestRepository() as ReminderDataSource
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
//        single { RemindersLocalRepository(get()) as ReminderDataSource}
        single {
            LocalDB.createRemindersDao(androidContext())
        }
    }

    @Before
    fun setUp() {
        stopKoin()
        startKoin {
            androidContext(getApplicationContext())
            modules(testModule)
            //modules(listOf(testModule))
        }
//        auth = FirebaseAuth.getInstance()
//        auth.signInWithEmailAndPassword("test@aaqanddev.com", "password")
//        authViewModel._navigateToAuthActivity.value = false
        //viewModel = SaveReminderViewModel(getApplicationContext(),FakeAndroidTestRepository())
    }

    @After
    fun tearDown() {
        runBlocking{
            repo.deleteAllReminders()
        }
        stopKoin()
    }

    //Test Navigation
    @Test
    fun clickChooseLocation_navigateToSelectLocationFragment(){
        //Given: SaveReminderFragment
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController )
        }
        //When: click choose location
        onView(withId(R.id.selectLocation)).perform(click())
        //Then: verify navigates to map fragment
        verify(navController).navigate(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
    }

    @Test
    fun saveReminder_showToast_navigateToReminderListFragment(){
        //Given: launch SaveReminderFragment
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        navController.setGraph(R.navigation.nav_graph)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.reminderTitle)).perform((typeText("title1")), closeSoftKeyboard())
        onView(withId(R.id.reminderDescription)).perform((typeText("desc1")), closeSoftKeyboard())
        //When: saveReminderClicked
        onView(withId(R.id.saveReminder)).perform(click())
        //Then: shows reminder saved toast
        //following relies on activityRule, which seemed to cause listFragment to open
        onView(withText(R.string.reminder_saved))
            .inRoot(withDecorView(not(activityRule.activity
            .window.decorView
            ))) .check(matches(isDisplayed()))
        verify(navController).popBackStack()

    }
}