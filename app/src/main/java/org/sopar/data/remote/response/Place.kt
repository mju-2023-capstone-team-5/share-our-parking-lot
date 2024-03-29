package org.sopar.data.remote.response

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Place(
    @field:Json(name = "place_name")
    val place_name: String,
    @field:Json(name = "road_address_name")
    val road_address_name: String,
    @field:Json(name = "x")
    val x: String,
    @field:Json(name = "y")
    val y: String
): Parcelable