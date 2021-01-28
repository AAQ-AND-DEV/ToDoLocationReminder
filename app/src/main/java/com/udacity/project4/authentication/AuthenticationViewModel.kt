package com.udacity.project4.authentication

import android.app.Application
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.base.BaseViewModel

class AuthenticationViewModel(app: Application) : AndroidViewModel(app) {

    val _navigateToAuthActivity = MutableLiveData<Boolean>()

    init{
        _navigateToAuthActivity.value = FirebaseAuth.getInstance().currentUser == null
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