package com.mihnati.app.data.cloud

import com.mihnati.app.data.model.Application
import com.mihnati.app.data.model.EmploiPublicConcours
import com.mihnati.app.data.model.UserAccount
import com.mihnati.app.data.model.UserProfile
import com.mihnati.app.data.repository.EmploiPublicRepository
import com.mihnati.app.data.repository.MihnatiRepository

/**
 * Fallback & Local Cloud Service Implementation.
 * Used when running offline, during initial setup, or as a standalone local mock.
 */
class FallbackCloudService : CloudDataService {
    override val providerName: String = "OfflineLocal"

    override suspend fun registerUser(
        email: String,
        passwordHash: String,
        displayName: String,
        cin: String
    ): Result<UserAccount> {
        val account = UserAccount(
            email = email,
            displayName = displayName,
            isSignedInWithGoogle = false,
            provider = "email",
            isVerified = true,
            lastLoginDate = "À l'instant (Local)"
        )
        return Result.success(account)
    }

    override suspend fun loginUser(
        email: String,
        passwordHash: String
    ): Result<UserAccount> {
        val user = MihnatiRepository.userProfile.value.account
        return if (user.email.equals(email, ignoreCase = true)) {
            Result.success(user)
        } else {
            Result.failure(Exception("Compte introuvable en local."))
        }
    }

    override suspend fun logout(): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun syncUserProfile(profile: UserProfile): Result<Boolean> {
        return Result.success(true)
    }

    override suspend fun getUserProfile(userId: String): Result<UserProfile?> {
        return Result.success(MihnatiRepository.userProfile.value)
    }

    override suspend fun fetchAllConcours(): Result<List<EmploiPublicConcours>> {
        val list = EmploiPublicRepository.concours.value
        return Result.success(list)
    }

    override suspend fun syncConcoursBatch(concoursList: List<EmploiPublicConcours>): Result<Int> {
        return Result.success(concoursList.size)
    }

    override suspend fun submitApplication(application: Application): Result<Boolean> {
        MihnatiRepository.addApplication(application)
        return Result.success(true)
    }

    override suspend fun getApplications(userId: String): Result<List<Application>> {
        return Result.success(MihnatiRepository.applications.value)
    }

    override suspend fun uploadDocument(
        userId: String,
        category: String,
        fileName: String,
        bytes: ByteArray
    ): Result<String> {
        return Result.success("local://storage/$category/$fileName")
    }

    override suspend fun getDocumentDownloadUrl(
        userId: String,
        documentId: String
    ): Result<String> {
        return Result.success("local://storage/$documentId")
    }
}
