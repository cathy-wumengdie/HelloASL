package ca.uwaterloo.helloasl.data

import ca.uwaterloo.helloasl.data.learningRepository.LearningRepository
import ca.uwaterloo.helloasl.data.learningRepository.SupabaseLearningRepository
import io.github.jan.supabase.SupabaseClient

class SupabaseAppDependency(
    client: SupabaseClient
) {
    val learningRepository: LearningRepository = SupabaseLearningRepository(client)
}

