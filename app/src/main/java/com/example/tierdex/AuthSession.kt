package com.example.tierdex

object AuthSession {
    var currentUserId: String? = "test-user-1"
        private set

    fun setCurrentUserId(userId: String?) {
        currentUserId = userId
    }
}
