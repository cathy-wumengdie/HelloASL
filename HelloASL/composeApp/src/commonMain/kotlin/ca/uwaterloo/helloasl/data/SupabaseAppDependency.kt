package ca.uwaterloo.helloasl.data

import ca.uwaterloo.helloasl.data.authRepository.AuthRepository
import ca.uwaterloo.helloasl.data.authRepository.SupabaseAuthRepository
import ca.uwaterloo.helloasl.data.learningRepository.LearningRepository
import ca.uwaterloo.helloasl.data.learningRepository.SupabaseLearningRepository
import ca.uwaterloo.helloasl.data.notificationRepository.SupabaseNotificationRepository
import ca.uwaterloo.helloasl.data.progressTrackerRepository.ProgressTrackerRepository
import ca.uwaterloo.helloasl.data.progressTrackerRepository.SupabaseProgressTrackerRepository
import ca.uwaterloo.helloasl.data.translateRepository.SupabaseTranslateRepository
import ca.uwaterloo.helloasl.data.translateRepository.TranslateRepository
import ca.uwaterloo.helloasl.data.translateRepository.VideoRecognitionService
import ca.uwaterloo.helloasl.data.userRepository.SupabaseUserRepository
import ca.uwaterloo.helloasl.data.userRepository.UserRepository
import ca.uwaterloo.helloasl.data.starRepository.StarRepository
import ca.uwaterloo.helloasl.data.starRepository.SupabaseStarRepository
import io.github.jan.supabase.SupabaseClient

class SupabaseAppDependency(
    client: SupabaseClient,
    anonKey: String,
    videoRecognitionService: VideoRecognitionService? = null
) {
    val learningRepository: LearningRepository = SupabaseLearningRepository(client)
    val authRepository: AuthRepository = SupabaseAuthRepository(client)
    val userRepository: UserRepository = SupabaseUserRepository(client)
    val progressTrackerRepository: ProgressTrackerRepository = SupabaseProgressTrackerRepository(client)
    val translateRepository: TranslateRepository =
        SupabaseTranslateRepository(client, videoRecognitionService)
    val starRepository: StarRepository = SupabaseStarRepository(client)
    val notificationRepository: SupabaseNotificationRepository = SupabaseNotificationRepository(client, anonKey)
}

