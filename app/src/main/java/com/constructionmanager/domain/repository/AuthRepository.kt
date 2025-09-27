package com.constructionmanager.domain.repository

import com.constructionmanager.domain.model.User
import com.constructionmanager.domain.model.AuthToken

interface AuthRepository {
    
    suspend fun login(email: String, password: String): User
    
    suspend fun register(
        email: String, 
        password: String, 
        firstName: String, 
        lastName: String, 
        company: String
    ): User
    
    suspend fun loginAsDemo(): User
    
    suspend fun logout()
    
    suspend fun isAuthenticated(): Boolean
    
    suspend fun getCurrentUser(): User?
    
    suspend fun refreshToken(): AuthToken
    
    suspend fun resetPassword(email: String)
    
    suspend fun updateProfile(user: User): User
    
    suspend fun changePassword(currentPassword: String, newPassword: String)
}