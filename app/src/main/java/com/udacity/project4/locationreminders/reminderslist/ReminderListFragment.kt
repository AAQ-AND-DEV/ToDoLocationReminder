package com.udacity.project4.locationreminders.reminderslist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat.invalidateOptionsMenu
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.authentication.AuthenticationViewModel
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {
    //use Koin to retrieve the ViewModel instance
    private val TAG = "ReminderListFragment"
    override val _viewModel: RemindersListViewModel by viewModel()
    val authViewModel: AuthenticationViewModel by inject()
    private lateinit var binding: FragmentRemindersBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_reminders, container, false
            )
        binding.viewModel = _viewModel

        if (authViewModel.authenticationState.value != AuthenticationViewModel.AuthenticationState.AUTHENTICATED){
            navigateToAuthActivity()
        }

        authViewModel._navigateToAuthActivity.observe(viewLifecycleOwner){
            if (it){
                navigateToAuthActivity()
            }
        }
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))

        binding.refreshLayout.setOnRefreshListener { _viewModel.loadReminders() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }
    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        _viewModel.loadReminders()
//        this.requireActivity().invalidateOptionsMenu()
    }

    private fun navigateToAddReminder() {
        //use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder()
            )
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {
        }

//        setup the recycler view using the extension function
        binding.reminderssRecyclerView.setup(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
//              logout implementation
//                if (item.title == getString(R.string.logout)) {

                    AuthUI.getInstance().signOut(this.requireContext())
                        .addOnCompleteListener {
                            Log.i(TAG, "user successfully signed out")

                            authViewModel._navigateToAuthActivity.value = true
                            //item.title = getString(R.string.login)
                        }
//                }
//                 else{
//                    Log.d(TAG, "authState: ${authViewModel.authenticationState.value}")
//                    if (authViewModel.authenticationState.value != AuthenticationViewModel.AuthenticationState.AUTHENTICATED){
//
//                    }
//                }

            }
        }
        return super.onOptionsItemSelected(item)

    }

    private fun navigateToAuthActivity(){
        val intent =
            Intent(this.requireContext(), AuthenticationActivity::class.java)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
//        display logout as menu item
        inflater.inflate(R.menu.main_menu, menu)
    }

//    override fun onPrepareOptionsMenu(menu: Menu) {
//        val item = menu.findItem(R.id.logout)
//        if (authViewModel.authenticationState.value == AuthenticationViewModel.AuthenticationState.AUTHENTICATED){
//            item.title = getString(R.string.logout)
//        } else{
//            item.title = getString(R.string.login)
//        }
//        return super.onPrepareOptionsMenu(menu)
//    }
}
