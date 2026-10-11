package com.example.data.remote

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class FonnteRepository {

    private val api: FonnteApi

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.fonnte.com/")
            .client(client)
            .build()

        api = retrofit.create(FonnteApi::class.java)
    }

    suspend fun sendMessage(
        token: String,
        target: String,
        message: String,
        countryCode: String = "62"
    ): FonnteResult<FonnteSendResponse> {
        return try {
            val response = api.sendMessage(
                token = token.trim(),
                target = target.trim(),
                message = message,
                countryCode = countryCode
            )
            val raw = response.body()?.string() ?: response.errorBody()?.string() ?: ""
            Log.d("FonnteRepository", "sendMessage raw response: $raw")

            if (raw.isNotBlank()) {
                val json = JSONObject(raw)
                val status = json.optBoolean("status", false)
                val detail = json.optString("detail", "")
                val msg = json.optString("message", "")
                val reason = json.optString("reason", "")
                val process = json.optString("process", "")

                val sendResponse = FonnteSendResponse(
                    status = status,
                    detail = if (detail.isNotEmpty()) detail else null,
                    message = if (msg.isNotEmpty()) msg else null,
                    reason = if (reason.isNotEmpty()) reason else null,
                    process = if (process.isNotEmpty()) process else null
                )

                if (status) {
                    FonnteResult.Success(sendResponse)
                } else {
                    val errMsg = reason.ifEmpty { msg.ifEmpty { detail.ifEmpty { "Gagal mengirim pesan via Fonnte." } } }
                    FonnteResult.Error(errMsg)
                }
            } else {
                FonnteResult.Error("HTTP ${response.code()}: Tidak ada respons dari server Fonnte.")
            }
        } catch (e: Exception) {
            Log.e("FonnteRepository", "Error sending message", e)
            FonnteResult.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi ke server Fonnte.")
        }
    }

    suspend fun checkDevice(token: String): FonnteResult<FonnteDeviceResponse> {
        return try {
            val response = api.checkDevice(token = token.trim())
            val raw = response.body()?.string() ?: response.errorBody()?.string() ?: ""
            Log.d("FonnteRepository", "checkDevice raw response: $raw")

            if (raw.isNotBlank()) {
                val json = JSONObject(raw)
                val status = json.optBoolean("status", false)
                val deviceStatus = json.optString("device_status", if (status) "connect" else "disconnect")
                val device = json.optString("device", "")
                val name = json.optString("name", "")
                val quota = json.opt("quota")?.toString() ?: ""
                val messages = json.opt("messages")?.toString() ?: ""
                val detail = json.optString("detail", "")
                val reason = json.optString("reason", "")

                val deviceResponse = FonnteDeviceResponse(
                    status = status,
                    deviceStatus = deviceStatus,
                    device = device,
                    name = name,
                    quota = quota,
                    messages = messages,
                    detail = detail,
                    reason = reason
                )

                if (status || deviceStatus.isNotEmpty()) {
                    FonnteResult.Success(deviceResponse)
                } else {
                    FonnteResult.Error(reason.ifEmpty { detail.ifEmpty { "Gagal verifikasi perangkat." } })
                }
            } else {
                FonnteResult.Error("HTTP ${response.code()}: Server Fonnte tidak merespons.")
            }
        } catch (e: Exception) {
            Log.e("FonnteRepository", "Error checking device", e)
            FonnteResult.Error(e.localizedMessage ?: "Gagal terhubung ke Fonnte.")
        }
    }

    suspend fun getWhatsAppGroups(token: String): FonnteResult<List<FonnteGroupItem>> {
        return try {
            val response = api.getWhatsAppGroups(token = token.trim())
            val raw = response.body()?.string() ?: response.errorBody()?.string() ?: ""
            Log.d("FonnteRepository", "getWhatsAppGroups raw: $raw")

            if (raw.isNotBlank()) {
                val groupList = mutableListOf<FonnteGroupItem>()
                val trimmed = raw.trim()
                if (trimmed.startsWith("[")) {
                    val arr = org.json.JSONArray(trimmed)
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val id = obj.optString("id", "")
                        val name = obj.optString("name", id)
                        val member = obj.opt("member")?.toString() ?: ""
                        if (id.isNotEmpty()) {
                            groupList.add(FonnteGroupItem(id = id, name = name, member = member))
                        }
                    }
                } else {
                    val json = JSONObject(trimmed)
                    val status = json.optBoolean("status", true)
                    val data = json.opt("data")
                    if (data is org.json.JSONArray) {
                        for (i in 0 until data.length()) {
                            val obj = data.getJSONObject(i)
                            val id = obj.optString("id", "")
                            val name = obj.optString("name", id)
                            val member = obj.opt("member")?.toString() ?: ""
                            if (id.isNotEmpty()) {
                                groupList.add(FonnteGroupItem(id = id, name = name, member = member))
                            }
                        }
                    } else if (!status) {
                        val reason = json.optString("reason", json.optString("detail", "Gagal mengambil daftar grup WA."))
                        return FonnteResult.Error(reason)
                    }
                }
                FonnteResult.Success(groupList)
            } else {
                FonnteResult.Error("HTTP ${response.code()}: Respons server Fonnte kosong.")
            }
        } catch (e: Exception) {
            Log.e("FonnteRepository", "Error fetching groups", e)
            FonnteResult.Error(e.localizedMessage ?: "Gagal mengambil daftar grup dari Fonnte.")
        }
    }

    suspend fun refreshGroupList(token: String): FonnteResult<String> {
        return try {
            val response = api.fetchWhatsAppGroups(token = token.trim())
            val raw = response.body()?.string() ?: response.errorBody()?.string() ?: ""
            Log.d("FonnteRepository", "fetchWhatsAppGroups raw: $raw")
            if (raw.isNotBlank()) {
                val json = JSONObject(raw)
                val status = json.optBoolean("status", false)
                val detail = json.optString("detail", json.optString("message", "Perintah pembaruan grup diproses."))
                if (status) {
                    FonnteResult.Success(detail)
                } else {
                    FonnteResult.Error(detail)
                }
            } else {
                FonnteResult.Error("HTTP ${response.code()}: Tidak ada respon.")
            }
        } catch (e: Exception) {
            Log.e("FonnteRepository", "Error refreshing groups", e)
            FonnteResult.Error(e.localizedMessage ?: "Gagal memperbarui grup di Fonnte.")
        }
    }
}
