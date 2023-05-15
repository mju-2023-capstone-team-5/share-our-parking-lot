package org.sopar.data.remote.response


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class ParkingLot(
    @field:Json(name = "address")
    val address: String,
    @field:Json(name = "availableDay")
    val availableDay: List<String>,
    @field:Json(name = "availableTime")
    val availableTime: List<AvailableTime>,
    @field:Json(name = "freeInformation")
    val freeInformation: String?,
    @field:Json(name = "hourly")
    val hourly: Hourly?,
    @field:Json(name = "latitude")
    val latitude: Double,
    @field:Json(name = "longitude")
    val longitude: Double,
    @field:Json(name = "monthly")
    val monthly: Monthly?,
    @field:Json(name = "name")
    val name: String,
    @field:Json(name = "ownerId")
    val ownerId: Int,
    @field:Json(name = "phoneNumber")
    val phoneNumber: String,
    @field:Json(name = "remainingSpace")
    val remainingSpace: Int,
    @field:Json(name = "totalSpace")
    val totalSpace: Int,
    @field:Json(name = "type")
    val type: List<String>,
    @field:Json(name = "id")
    val id: Int,
    @field:Json(name = "imageUrl")
    val imageUrl: String?
): Parcelable