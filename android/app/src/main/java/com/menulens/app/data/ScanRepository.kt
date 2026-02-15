package com.menulens.app.data

import android.content.Context
import android.provider.Settings
import com.menulens.app.BuildConfig
import com.menulens.app.model.MenuItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class ScanRepository(private val context: Context) {
    private val api: ScanApiService by lazy {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logger)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .callTimeout(120, TimeUnit.SECONDS)
            .build()
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ScanApiService::class.java)
    }

    suspend fun scanMenu(imageBytes: ByteArray): List<MenuItem> = withContext(Dispatchers.IO) {
        val imageBody = imageBytes.toRequestBody("image/jpeg".toMediaType())
        val imagePart = MultipartBody.Part.createFormData("image", "menu.jpg", imageBody)

        val response = api.scanMenu(
            image = imagePart,
            targetLang = "en".toRequestBody("text/plain".toMediaType()),
            deviceId = getDeviceId().toRequestBody("text/plain".toMediaType()),
            appVersion = BuildConfig.VERSION_NAME.toRequestBody("text/plain".toMediaType()),
            timezone = TimeZone.getDefault().id.toRequestBody("text/plain".toMediaType())
        )
        response.toMenuItems()
    }

    private fun getDeviceId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown-device"
    }
}
