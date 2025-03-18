package client

import api.LinkedInAuthApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object AuthRetrofitClient {
    private const val BASE_URL = "https://www.linkedin.com/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS) // Bağlantı süresi
        .readTimeout(60, TimeUnit.SECONDS) // Okuma süresi
        .writeTimeout(60, TimeUnit.SECONDS) // Yazma süresi
        .build()

    val instance: LinkedInAuthApi by lazy{
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LinkedInAuthApi::class.java)
    }
}