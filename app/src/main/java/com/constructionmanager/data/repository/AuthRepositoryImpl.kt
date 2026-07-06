package com.constructionmanager.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.constructionmanager.domain.model.AuthToken
import com.constructionmanager.domain.model.SubscriptionTier
import com.constructionmanager.domain.model.User
import com.constructionmanager.domain.model.UserPreferences
import com.constructionmanager.domain.model.UserRole
import com.constructionmanager.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Auth backed by Firebase Authentication (email/password) against the nextgenbuildpro project.
 *
 * The built-in **demo** account stays a fully local session so the app remains usable offline and
 * without console setup; real email/password accounts go through Firebase and get a profile document
 * at `users/{uid}`. The signed-in user's uid is the workspace key the cloud repositories scope to.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val prefs: SharedPreferences
) : AuthRepository {

    override suspend fun login(email: String, password: String): User {
        if (email.trim().equals(DEMO_EMAIL, ignoreCase = true) && password == DEMO_PASSWORD) {
            return startDemoSession()
        }
        try {
            val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
            clearDemoFlag()
            return result.user?.toDomainUser() ?: throw IllegalStateException("No user returned")
        } catch (e: Exception) {
            throw Exception(e.message ?: "Invalid email or password")
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        company: String
    ): User {
        try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = result.user ?: throw IllegalStateException("No user returned")
            firebaseUser.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName("$firstName $lastName".trim())
                    .build()
            ).await()
            // Persist a profile document (best-effort).
            runCatching {
                firestore.collection("users").document(firebaseUser.uid).set(
                    mapOf(
                        "email" to email.trim(),
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "company" to company,
                        "createdAt" to System.currentTimeMillis()
                    )
                ).await()
            }
            clearDemoFlag()
            return firebaseUser.toDomainUser(firstName, lastName, company)
        } catch (e: Exception) {
            throw Exception(e.message ?: "Registration failed")
        }
    }

    override suspend fun loginAsDemo(): User = startDemoSession()

    override suspend fun logout() {
        auth.signOut()
        clearDemoFlag()
    }

    override suspend fun isAuthenticated(): Boolean =
        auth.currentUser != null || prefs.getBoolean(KEY_IS_DEMO, false)

    override suspend fun getCurrentUser(): User? = when {
        prefs.getBoolean(KEY_IS_DEMO, false) -> createDemoUser()
        auth.currentUser != null -> auth.currentUser!!.toDomainUser()
        else -> null
    }

    override suspend fun refreshToken(): AuthToken {
        val token = auth.currentUser?.getIdToken(true)?.await()?.token.orEmpty()
        return AuthToken(
            accessToken = token,
            refreshToken = "",
            expiresAt = now()
        )
    }

    override suspend fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email.trim()).await()
    }

    override suspend fun updateProfile(user: User): User {
        runCatching {
            firestore.collection("users").document(user.id).set(
                mapOf(
                    "email" to user.email,
                    "firstName" to user.firstName,
                    "lastName" to user.lastName,
                    "company" to user.company
                )
            ).await()
        }
        return user
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String) {
        auth.currentUser?.updatePassword(newPassword)?.await()
    }

    private fun startDemoSession(): User {
        prefs.edit { putBoolean(KEY_IS_DEMO, true) }
        return createDemoUser()
    }

    private fun clearDemoFlag() = prefs.edit { remove(KEY_IS_DEMO) }

    private fun now() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    private fun FirebaseUser.toDomainUser(
        firstName: String? = null,
        lastName: String? = null,
        company: String = "Construction Company"
    ): User {
        val parts = (displayName ?: "").split(" ")
        return User(
            id = uid,
            email = email ?: "",
            firstName = firstName ?: parts.getOrNull(0) ?: "",
            lastName = lastName ?: parts.getOrNull(1) ?: "",
            company = company,
            role = UserRole.PROJECT_MANAGER,
            isActive = true,
            createdAt = now(),
            lastLoginAt = now(),
            subscriptionTier = SubscriptionTier.PROFESSIONAL,
            preferences = UserPreferences()
        )
    }

    private fun createDemoUser(): User = User(
        id = DEMO_WORKSPACE,
        email = DEMO_EMAIL,
        firstName = "Demo",
        lastName = "Manager",
        company = "Demo Construction Co.",
        role = UserRole.PROJECT_MANAGER,
        isActive = true,
        createdAt = now(),
        lastLoginAt = now(),
        subscriptionTier = SubscriptionTier.PROFESSIONAL,
        preferences = UserPreferences(
            defaultRegion = "Midwest",
            notificationsEnabled = true,
            emailNotifications = false
        )
    )

    companion object {
        const val DEMO_EMAIL = "demo@constructionmanager.com"
        const val DEMO_PASSWORD = "demo123"
        const val DEMO_WORKSPACE = "demo_user_001"
        private const val KEY_IS_DEMO = "is_demo"
    }
}
