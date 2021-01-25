package com.udacity.project4.authentication

import android.app.Application
import androidx.lifecycle.*
import com.udacity.project4.base.BaseViewModel

class AuthenticationViewModel(app: Application) : AndroidViewModel(app) {

    val _navigateToAuthActivity = MutableLiveData<Boolean>()

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