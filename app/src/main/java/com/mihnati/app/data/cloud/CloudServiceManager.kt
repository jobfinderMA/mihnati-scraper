package com.mihnati.app.data.cloud

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Cloud Service Manager & Dependency Provider.
 *
 * Central registry for the active cloud backend.
 * Switching backends in the future is as simple as calling:
 * `CloudServiceManager.switchBackend(SupabaseCloudService(context))`
 */
object CloudServiceManager {

    private var _activeService: CloudDataService = FallbackCloudService()
    private val _currentProvider = MutableStateFlow(_activeService.providerName)
    val currentProvider: StateFlow<String> = _currentProvider.asStateFlow()

    fun getService(): CloudDataService = _activeService

    /**
     * Swaps the cloud backend at runtime without restarting the app.
     */
    fun switchBackend(newService: CloudDataService) {
        _activeService = newService
        _currentProvider.value = newService.providerName
    }

    /**
     * Initializes the default cloud backend.
     */
    fun init(context: Context, preferredProvider: String = "Firebase") {
        _activeService = when (preferredProvider) {
            "Firebase" -> FirebaseCloudService(context)
            else -> FallbackCloudService()
        }
        _currentProvider.value = _activeService.providerName
    }
}
