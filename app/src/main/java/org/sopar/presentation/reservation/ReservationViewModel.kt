package org.sopar.presentation.reservation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.sopar.data.remote.response.ParkingLot
import org.sopar.domain.entity.NetworkState
import org.sopar.domain.repository.MapRepository
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ReservationViewModel @Inject constructor(
    private val mapRepository: MapRepository
): ViewModel() {
    private val _getParkingLotState = MutableLiveData(NetworkState.LOADING)
    val getParkingLotState: LiveData<NetworkState> = _getParkingLotState

    fun getParkingLotsById(id: Int): ParkingLot {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = mapRepository.getParkingLotsById(id)
                Log.e("getParkingLotById", response.body().toString())
            } catch (e: IOException) {
                _getParkingLotState.postValue(NetworkState.FAIL)
            }
        }
    }


}