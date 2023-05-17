package org.sopar.domain.repository

import org.sopar.data.remote.response.ParkingLot
import retrofit2.Response

interface ParkingLotRepository {
    suspend fun registerParkingLot(parkingLot: ParkingLot): Response<ParkingLot>
}