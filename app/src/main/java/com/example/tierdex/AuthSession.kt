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
}
