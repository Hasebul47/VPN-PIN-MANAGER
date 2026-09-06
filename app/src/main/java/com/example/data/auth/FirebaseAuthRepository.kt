package com.example.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseAuthRepository {

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth?.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        auth?.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    val isFirebaseInitialized: Boolean
        get() = auth != null

    suspend fun signUpWithEmail(
        name: String,
        email: String,
        password: String
    ): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val firebaseAuth = auth ?: return@withContext Result.failure(
            IllegalStateException("Firebase কনফিগার করা হয়নি। অনুগ্রহ করে google-services.json ফাইলটি app/ ফোল্ডারে যোগ করুন। (Firebase not initialized. Please add google-services.json to app/ directory.)")
        )

        try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user ?: throw Exception("ব্যবহারকারী তৈরি করা যায়নি। (User creation failed.)")

            // Update user display name if provided
            if (name.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name.trim())
                    .build()
                user.updateProfile(profileUpdates).await()
            }

            _currentUser.value = firebaseAuth.currentUser
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception(getFriendlyErrorMessage(e)))
        }
    }

    suspend fun signInWithEmail(
        email: String,
        password: String
    ): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val firebaseAuth = auth ?: return@withContext Result.failure(
            IllegalStateException("Firebase কনফিগার করা হয়নি। অনুগ্রহ করে google-services.json ফাইলটি app/ ফোল্ডারে যোগ করুন।")
        )

        try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user ?: throw Exception("লগইন ব্যর্থ হয়েছে। (Login failed.)")
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception(getFriendlyErrorMessage(e)))
        }
    }

    fun signOut() {
        auth?.signOut()
        _currentUser.value = null
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        val firebaseAuth = auth ?: return@withContext Result.failure(
            IllegalStateException("Firebase কনফিগার করা হয়নি।")
        )

        try {
            firebaseAuth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(getFriendlyErrorMessage(e)))
        }
    }

    private fun getFriendlyErrorMessage(exception: Exception): String {
        return when (exception) {
            is FirebaseAuthException -> {
                when (exception.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "ইমেইল অ্যাড্রেসের ফরম্যাট সঠিক নয়। (Invalid email format.)"
                    "ERROR_WRONG_PASSWORD" -> "ভুল পাসওয়ার্ড দেওয়া হয়েছে। (Incorrect password.)"
                    "ERROR_USER_NOT_FOUND" -> "এই ইমেইলে কোনো অ্যাকাউন্ট পাওয়া যায়নি। (No account found with this email.)"
                    "ERROR_USER_DISABLED" -> "এই অ্যাকাউন্টটি নিষ্ক্রিয় করা হয়েছে। (Account has been disabled.)"
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "এই ইমেইলটি ইতিমধ্যে নিবন্ধিত রয়েছে। অনুগ্রহ করে লগইন করুন। (Email is already registered.)"
                    "ERROR_WEAK_PASSWORD" -> "পাসওয়ার্ড অন্তত ৬ অক্ষরের হতে হবে। (Password must be at least 6 characters.)"
                    "ERROR_OPERATION_NOT_ALLOWED" -> "ইমেইল/পাসওয়ার্ড লগইন ফায়ারবেসে সক্রিয় নেই। (Email/password login is not enabled in Firebase Console.)"
                    else -> exception.localizedMessage ?: "প্রমাণীকরণ ব্যর্থ হয়েছে। (Authentication failed.)"
                }
            }
            else -> {
                val msg = exception.localizedMessage ?: ""
                if (msg.contains("network", ignoreCase = true)) {
                    "ইন্টারনেট সংযোগ চেক করুন। (Network error. Please check your connection.)"
                } else if (msg.contains("FirebaseApp", ignoreCase = true)) {
                    "Firebase প্রজেক্ট সেটআপ করা হয়নি। google-services.json প্রয়োজন। (Firebase project not configured.)"
                } else {
                    msg.ifBlank { "একটি অপ্রত্যাশিত ত্রুটি ঘটেছে। (An unexpected error occurred.)" }
                }
            }
        }
    }
}
