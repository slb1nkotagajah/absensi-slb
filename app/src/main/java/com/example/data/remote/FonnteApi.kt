package com.example.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface FonnteApi {

    @FormUrlEncoded
    @POST("send")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Field("target") target: String,
        @Field("message") message: String,
        @Field("countryCode") countryCode: String = "62"
    ): Response<ResponseBody>

    @POST("device")
    suspend fun checkDevice(
        @Header("Authorization") token: String
    ): Response<ResponseBody>

    @POST("get-whatsapp-group")
    suspend fun getWhatsAppGroups(
        @Header("Authorization") token: String
    ): Response<ResponseBody>

    @POST("fetch-group")
    suspend fun fetchWhatsAppGroups(
        @Header("Authorization") token: String
    ): Response<ResponseBody>
}
