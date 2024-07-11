package com.iyr.ian.dao.repositories

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.iyr.ian.dao.models.User
import com.iyr.ian.utils.coroutines.Resource

interface AuthenticationInterface {
    suspend fun isEmailVerified(email: String): Boolean
    suspend fun loginWithEmailAndPassword(email: String, password: String): Resource<User?>
    suspend fun getAuthToken(): Resource<String?>
    suspend fun verifyIfPhoneNumberExists(phoneNumber: String): Resource<Boolean?>
    suspend fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential): Resource<FirebaseUser?>

    suspend fun createUserWithEmailPassword(email: String,password: String  ): Resource<FirebaseUser?>
    suspend fun sendEmailVerification(firebaseUser: FirebaseUser): Resource<Boolean?>
}


abstract class AuthenticationRepository : AuthenticationInterface {

    private var authManager: Any? = null
    private val tableReference: Any? = null
    protected val tableName = ""

}