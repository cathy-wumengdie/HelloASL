package ca.uwaterloo.helloasl.data.translateRepository

import android.content.Context
import android.net.Uri
import ca.uwaterloo.helloasl.domain.translateModel.AslRecognitionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class AndroidVideoRecognitionService(
    private val appContext: Context
) : VideoRecognitionService {

    override suspend fun recognize(videoUri: String): AslRecognitionResult = withContext(Dispatchers.IO) {
        val uri = Uri.parse(videoUri)
        val tempFile = uri.toTempFile(appContext)
            ?: throw IllegalStateException("Failed to read recorded video.")

        try {
            val requestFile = tempFile.asRequestBody("video/mp4".toMediaType())
            val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

            val response = ApiConfig.apiService.translateVideo(body)

            if (response.status != "success") {
                throw IllegalStateException(response.message ?: "Translation failed.")
            }

            AslRecognitionResult(
                recognizedText = response.gloss ?: "",
                confidence = (response.confidence ?: 0.0).toFloat()
            )
        } finally {
            tempFile.delete()
        }
    }
}