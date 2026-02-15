package com.menulens.app.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ScanApiService {
    @Multipart
    @POST("v1/scan_menu")
    suspend fun scanMenu(
        @Part image: MultipartBody.Part,
        @Part("target_lang") targetLang: RequestBody,
        @Part("device_id") deviceId: RequestBody,
        @Part("app_version") appVersion: RequestBody,
        @Part("timezone") timezone: RequestBody
    ): ScanMenuResponseDto
}
