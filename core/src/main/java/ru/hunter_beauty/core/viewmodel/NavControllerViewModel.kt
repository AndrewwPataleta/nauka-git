package com.nauchat.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import com.nauchat.core.util.Event
import com.nauchat.core.viewstate.NavState
import javax.inject.Inject

class NavControllerViewModel @Inject constructor() : ViewModel() {

    companion object {
        var currentPage: NavState? = null
    }

    val currentNavController = MutableLiveData<Event<NavController?>>()

    private var navState: NavState? = null

    private val _navigationState = MutableSharedFlow<NavState>()
    val navigationState: SharedFlow<NavState> = _navigationState.asSharedFlow()


}
