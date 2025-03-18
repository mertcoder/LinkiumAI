package api

import response.UserProfileResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface LinkedInGetProfileApi {
    @GET("v2/userinfo")
    suspend fun getUserProfile(
        @Header("Authorization") authToken: String
    ): Response<UserProfileResponse>
}