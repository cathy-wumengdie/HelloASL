package ca.uwaterloo.helloasl.data.translateRepository

import ca.uwaterloo.helloasl.domain.translateModel.AslRecognitionResult

interface VideoRecognitionService {
    suspend fun recognize(videoUri: String): AslRecognitionResult
}