package ca.uwaterloo.helloasl.data.translateRepository

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("/translate")
    suspend fun translateVideo(
        @Part file: MultipartBody.Part
    ): TranslateResponse
}