package com.example.kotlin_traverse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kotlin_traverse.data.local.TokenStore
import com.example.kotlin_traverse.data.repository.TraverseRepository

class TraverseViewModelFactory(
    private val repository: TraverseRepository,
    private val tokenStore: TokenStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TraverseViewModel::class.java)) {
            return TraverseViewModel(repository, tokenStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class ${modelClass.simpleName}")
    }
}
