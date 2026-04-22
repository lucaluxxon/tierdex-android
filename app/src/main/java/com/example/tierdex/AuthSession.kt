package com.example.tierdex

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

object AuthSession {
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    var currentUserId: String? = firebaseAuth.currentUser?.uid ?: "test-user-1"
        private set

    fun setCurrentUserId(userId: String?) {
        currentUserId = userId
    }

    fun getCurrentFirebaseUserId(): String? = firebaseAuth.currentUser?.uid

    fun getCurrentDisplayName(): String? = firebaseAuth.currentUser?.displayName

    fun registerWithEmail(
        displayName: String,
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            onResult(false, "E-Mail und Passwort dürfen nicht leer sein")
            return
        }

        try {
            firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val currentUser = firebaseAuth.currentUser
                        if (currentUser == null) {
                            onResult(false, "Registrierung fehlgeschlagen")
                            return@addOnCompleteListener
                        }

                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(displayName.takeIf { it.isNotBlank() })
                            .build()

                        currentUser
                            .updateProfile(profileUpdates)
                            .addOnCompleteListener { profileTask ->
                                if (profileTask.isSuccessful) {
                                    val firebaseUserId = currentUser.uid
                                    setCurrentUserId(firebaseUserId)
                                    onResult(true, firebaseUserId)
                                } else {
                                    onResult(false, profileTask.exception?.message)
                                }
                            }
                    } else {
                        onResult(false, task.exception?.message)
                    }
                }
        } catch (e: Exception) {
            onResult(false, e.message ?: "Registrierung fehlgeschlagen")
        }
    }

    fun loginWithEmail(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            onResult(false, "E-Mail und Passwort dürfen nicht leer sein")
            return
        }

        try {
            firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUserId = firebaseAuth.currentUser?.uid
                        setCurrentUserId(firebaseUserId)
                        onResult(true, firebaseUserId)
                    } else {
                        onResult(false, task.exception?.message)
                    }
                }
        } catch (e: Exception) {
            onResult(false, e.message ?: "Login fehlgeschlagen")
        }
    }
}
