package org.sopar.presentation.signUp

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sopar.data.api.RetrofitApi
import org.sopar.data.remote.request.UserRegisterRequest
import org.sopar.domain.entity.NetworkState
import org.sopar.domain.repository.AuthRepository
import javax.inject.Inject
import java.lang.Exception

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val api: RetrofitApi,
    private val authRepository: AuthRepository
): ViewModel() {
    private val _registerState = MutableLiveData(NetworkState.LOADING)
    val registerState: LiveData<NetworkState> get() = _registerState


    fun userRegister(userRegisterRequest: UserRegisterRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _registerState.postValue(NetworkState.SUCCESS)
            } catch(e: Exception) {
                _registerState.postValue(NetworkState.FAIL)
            }
        }
    }

    suspend fun getUserId() = authRepository.getUId().first()
}