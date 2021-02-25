package com.udacity.project4.authentication

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.google.firebase.auth.FirebaseAuth

class AuthenticationViewModel(app: Application) : AndroidViewModel(app) {

    private val TAG = "AuthViewModel"
    val _navigateToAuthActivity = MutableLiveData<Boolean>()

    init{
        _navigateToAuthActivity.value = FirebaseAuth.getInstance().currentUser == null
        Log.d(TAG, "navToAuth value: ${_navigateToAuthActivity.value}")
    }

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
    }

    val authenticationState = FirebaseUserLiveData().map{
            user ->
        if (user != null){
            AuthenticationState.AUTHENTICATED
        } else{
            AuthenticationState.UNAUTHENTICATED
        }
    }
}