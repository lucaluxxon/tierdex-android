package com.example.tierdex

import com.google.firebase.auth.FirebaseAuth

object AuthSession {
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    var currentUserId: String? = "test-user-1"
        private set

    fun setCurrentUserId(userId: String?) {
        currentUserId = userId
    }

    fun getCurrentFirebaseUserId(): String? = firebaseAuth.currentUser?.uid

    fun registerWithEmail(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        firebaseAuth
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, firebaseAuth.currentUser?.uid)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun loginWithEmail(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        firebaseAuth
            .signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, firebaseAuth.currentUser?.uid)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }
}
