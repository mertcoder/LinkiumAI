package api

import UgcPostRequest
import UgcPostResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface LinkedInShareApi {
    @POST("v2/ugcPosts")
    suspend fun createPost(
        @Header("Authorization") authToken: String,
        @Body shareContent: UgcPostRequest
        ): Response<UgcPostResponse>

}