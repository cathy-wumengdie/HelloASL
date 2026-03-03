package data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.text.get

private val Context.dataStore by preferencesDataStore(name = "helloasl_prefs")

object HelloAslDataStoreKeys {
    val HAS_SEEN_PERMISSION_GATE = booleanPreferencesKey("has_seen_permission_gate")
}

class HelloAslDataStore(private val context: Context) {
    val hasSeenPermissionGate: Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[HelloAslDataStoreKeys.HAS_SEEN_PERMISSION_GATE] ?: false
        }

    suspend fun setHasSeenPermissionGate(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[HelloAslDataStoreKeys.HAS_SEEN_PERMISSION_GATE] = value
        }
    }
}