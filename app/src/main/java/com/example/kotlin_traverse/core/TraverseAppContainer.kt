package com.example.kotlin_traverse.core

import android.content.Context
import com.example.kotlin_traverse.data.local.TokenStore
import com.example.kotlin_traverse.data.remote.NetworkModule
import com.example.kotlin_traverse.data.repository.TraverseRepository
import com.example.kotlin_traverse.viewmodel.TraverseViewModelFactory
import kotlinx.serialization.json.Json

class TraverseAppContainer(context: Context) {

    private val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val tokenStore: TokenStore = TokenStore(context.applicationContext, json)
    private val networkModule: NetworkModule = NetworkModule(tokenStore, json)

    val repository: TraverseRepository = TraverseRepository(
        api = networkModule.api,
        tokenStore = tokenStore
    )

    val viewModelFactory: TraverseViewModelFactory = TraverseViewModelFactory(
        repository = repository,
        tokenStore = tokenStore
    )
}
