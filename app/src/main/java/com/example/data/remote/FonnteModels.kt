package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FonnteSendResponse(
    @Json(name = "status")
    val status: Boolean = false,
    @Json(name = "target")
    val target: Any? = null,
    @Json(name = "message")
    val message: String? = null,
    @Json(name = "detail")
    val detail: String? = null,
    @Json(name = "process")
    val process: String? = null,
    @Json(name = "reason")
    val reason: String? = null
)

@JsonClass(generateAdapter = true)
data class FonnteDeviceResponse(
    @Json(name = "status")
    val status: Boolean = false,
    @Json(name = "device_status")
    val deviceStatus: String? = null,
    @Json(name = "device")
    val device: String? = null,
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "quota")
    val quota: String? = null,
    @Json(name = "messages")
    val messages: String? = null,
    @Json(name = "detail")
    val detail: String? = null,
    @Json(name = "reason")
    val reason: String? = null
)

sealed class FonnteResult<out T> {
    data class Success<T>(val data: T) : FonnteResult<T>()
    data class Error(val message: String) : FonnteResult<Nothing>()
}

data class FonnteGroupItem(
    val id: String,
    val name: String,
    val member: String = ""
)

