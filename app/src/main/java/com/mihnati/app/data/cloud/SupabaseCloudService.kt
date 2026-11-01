package com.mihnati.app.data.cloud

import android.content.Context
import com.mihnati.app.data.model.Application
import com.mihnati.app.data.model.EmploiPublicConcours
import com.mihnati.app.data.model.UserAccount
import com.mihnati.app.data.model.UserProfile

/**
 * Example Alternative Backend: Supabase (PostgreSQL + GoTrue Auth).
 *
 * Demonstrates how easily Mihnati can swap from Firebase to Supabase
 * in the future by simply implementing [CloudDataService].
 *
 * ZERO changes are needed in Compose UI, ViewModels, or repositories!
 */
class SupabaseCloudService(
    private val context: Context,
    private val supabaseUrl: String = "https://your-project.supabase.co",
    private val anonKey: String = "your-anon-key"
) : CloudDataService {

    override val providerName: String = "Supabase"

    override suspend fun registerUser(
        email: String,
        passwordHash: String,
        displayName: String,
        cin: String
    ): Result<UserAccount> {
        return Result.success(
            UserAccount(
                email = email,
                displayName = displayName,
                isSignedInWithGoogle = false,
                provider = "supabase",
                isVerified = true,
                lastLoginDate = "À l'instant (Supabase)"
            )
        )
    }

    override suspend fun loginUser(
        email: String,
        passwordHash: String
    ): Result<UserAccount> {
        return Result.success(
            UserAccount(
                email = email,
                displayName = email.substringBefore("@"),
                isSignedInWithGoogle = false,
                provider = "supabase",
                isVerified = true
            )
        )
    }

    override suspend fun logout(): Result<Unit> = Result.success(Unit)

    override suspend fun syncUserProfile(profile: UserProfile): Result<Boolean> = Result.success(true)

    override suspend fun getUserProfile(userId: String): Result<UserProfile?> = Result.success(null)

    override suspend fun fetchAllConcours(): Result<List<EmploiPublicConcours>> = Result.success(emptyList())

    override suspend fun syncConcoursBatch(concoursList: List<EmploiPublicConcours>): Result<Int> = Result.success(concoursList.size)

    override suspend fun submitApplication(application: Application): Result<Boolean> = Result.success(true)

    override suspend fun getApplications(userId: String): Result<List<Application>> = Result.success(emptyList())

    override suspend fun uploadDocument(
        userId: String,
        category: String,
        fileName: String,
        bytes: ByteArray
    ): Result<String> = Result.success("https://your-project.supabase.co/storage/v1/object/public/$fileName")

    override suspend fun getDocumentDownloadUrl(
        userId: String,
        documentId: String
    ): Result<String> = Result.success("https://your-project.supabase.co/storage/v1/object/public/$documentId")
}
