package ca.uwaterloo.helloasl.data

import io.github.jan.supabase.SupabaseClient

expect object SupabaseClientFactory {
    fun create(url: String, key: String): SupabaseClient
}