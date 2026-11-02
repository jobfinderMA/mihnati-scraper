package com.mihnati.app.data.cloud

import com.mihnati.app.data.model.Application
import com.mihnati.app.data.model.EmploiPublicConcours
import com.mihnati.app.data.model.UserAccount
import com.mihnati.app.data.model.UserProfile

/**
 * Universal Cloud Backend Interface (Clean Architecture / Adapter Pattern).
 *
 * This interface completely decouples the Mihnati application from Firebase.
 * If you ever decide to migrate to Supabase, AWS, PostgreSQL, or a custom REST API,
 * you only need to create a new implementation of this interface (e.g. SupabaseCloudService)
 * and update the CloudServiceManager binding.
 *
 * ZERO changes will be required in your Compose UI, ViewModels, or business logic.
 */
interface CloudDataService {
    /**
     * Name of the active cloud backend (e.g., "Firebase", "Supabase", "CustomREST", "OfflineLocal")
     */
    val providerName: String

    // ==========================================
    // 1. User Authentication & Account Storage
    // ==========================================
    suspend fun registerUser(
        email: String,
        passwordHash: String,
        displayName: String,
        cin: String
    ): Result<UserAccount>

    suspend fun sendPasswordlessSignInEmail(email: String): Result<Boolean> = Result.success(true)

    suspend fun signInWithGoogleAccount(
        email: String,
        displayName: String,
        photoUrl: String?,
        idToken: String?
    ): Result<UserAccount> = Result.success(
        UserAccount(
            email = email,
            displayName = displayName,
            photoUrl = photoUrl,
            isSignedInWithGoogle = true,
            provider = "Google OAuth (Vérifié)",
            isVerified = true
        )
    )

    suspend fun loginUser(
        email: String,
        passwordHash: String
    ): Result<UserAccount>

    suspend fun logout(): Result<Unit>

    suspend fun syncUserProfile(profile: UserProfile): Result<Boolean>

    suspend fun getUserProfile(userId: String): Result<UserProfile?>

    // ==========================================
    // 2. Concours (Scraped Public Data)
    // ==========================================
    suspend fun fetchAllConcours(): Result<List<EmploiPublicConcours>>

    suspend fun syncConcoursBatch(concoursList: List<EmploiPublicConcours>): Result<Int>

    // ==========================================
    // 3. Applications (Tracking & Candidatures)
    // ==========================================
    suspend fun submitApplication(application: Application): Result<Boolean>

    suspend fun getApplications(userId: String): Result<List<Application>>

    // ==========================================
    // 4. File / Document Storage (CV, CIN, Diplomas)
    // ==========================================
    suspend fun uploadDocument(
        userId: String,
        category: String,
        fileName: String,
        bytes: ByteArray
    ): Result<String>

    suspend fun getDocumentDownloadUrl(
        userId: String,
        documentId: String
    ): Result<String>
}
