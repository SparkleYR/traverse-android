package com.example.kotlin_traverse.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.kotlin_traverse.data.model.GithubProfile
import com.example.kotlin_traverse.data.model.Session
import com.example.kotlin_traverse.data.model.User
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val Context.traverseDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "traverse_prefs"
)

class TokenStore(private val context: Context, private val json: Json) {

    private val tokenKey = stringPreferencesKey("token")
    private val userKey = stringPreferencesKey("user")
    private val themeKey = booleanPreferencesKey("dark_theme")

    val preferencesFlow: Flow<TokenPreferences> = context.traverseDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            val token = prefs[tokenKey]
            val userJson = prefs[userKey]
            val isDark = prefs[themeKey] ?: false
            val session = if (token != null && !userJson.isNullOrEmpty()) {
                runCatching {
                    json.decodeFromString(StoredUser.serializer(), userJson).toSession(token)
                }.getOrNull()
            } else {
                null
            }
            TokenPreferences(session = session, isDarkTheme = isDark)
        }

    suspend fun saveSession(session: Session) {
        val storedUser = StoredUser.fromUser(session.user)
        val encodedUser = json.encodeToString(StoredUser.serializer(), storedUser)
        context.traverseDataStore.edit { prefs ->
            prefs[tokenKey] = session.token
            prefs[userKey] = encodedUser
        }
    }

    suspend fun clearSession() {
        context.traverseDataStore.edit { prefs ->
            prefs.remove(tokenKey)
            prefs.remove(userKey)
        }
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.traverseDataStore.edit { prefs ->
            prefs[themeKey] = isDark
        }
    }

    suspend fun updateSessionUser(user: User) {
        context.traverseDataStore.edit { prefs ->
            val token = prefs[tokenKey]
            if (token != null) {
                prefs[userKey] = json.encodeToString(StoredUser.serializer(), StoredUser.fromUser(user))
            }
        }
    }

    suspend fun updateSessionToken(token: String) {
        context.traverseDataStore.edit { prefs ->
            prefs[tokenKey] = token
        }
    }

    suspend fun setSession(session: Session?) {
        if (session == null) {
            clearSession()
        } else {
            saveSession(session)
        }
    }

    suspend fun readToken(): String? = context.traverseDataStore.data
        .map { it[tokenKey] }
        .firstOrNull()

    fun getDataStore(): DataStore<Preferences> = context.traverseDataStore
}

@Serializable
private data class StoredUser(
    val id: String,
    val username: String,
    val email: String,
    val role: String,
    val github: StoredGithubProfile? = null
) {
    fun toSession(token: String): Session = Session(
        token = token,
        user = User(
            id = id,
            username = username,
            email = email,
            role = role,
            github = github?.toGithubProfile()
        )
    )

    companion object {
        fun fromUser(user: User): StoredUser = StoredUser(
            id = user.id,
            username = user.username,
            email = user.email,
            role = user.role,
            github = user.github?.let { StoredGithubProfile.fromGithubProfile(it) }
        )
    }
}

@Serializable
private data class StoredGithubProfile(
    val id: String,
    val username: String,
    val repo: String,
    val branch: String,
    val linked: Boolean
) {
    fun toGithubProfile(): GithubProfile = GithubProfile(
        id = id,
        username = username,
        repo = repo,
        branch = branch,
        linked = linked
    )

    companion object {
        fun fromGithubProfile(profile: GithubProfile): StoredGithubProfile = StoredGithubProfile(
            id = profile.id,
            username = profile.username,
            repo = profile.repo,
            branch = profile.branch,
            linked = profile.linked
        )
    }
}

data class TokenPreferences(
    val session: Session?,
    val isDarkTheme: Boolean
)
