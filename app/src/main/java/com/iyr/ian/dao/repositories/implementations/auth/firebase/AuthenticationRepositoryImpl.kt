package com.iyr.ian.dao.repositories.implementations.auth.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.functions.FirebaseFunctions
import com.iyr.ian.dao.models.User
import com.iyr.ian.dao.repositories.AuthenticationRepository
import com.iyr.ian.repository.implementations.databases.realtimedatabase.UsersRepositoryImpl
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.coroutines.safeCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthenticationRepositoryImpl : AuthenticationRepository() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    override suspend fun isEmailVerified(email: String): Boolean {
        return try {
            FirebaseAuth.getInstance().currentUser?.isEmailVerified ?: false
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun loginWithEmailAndPassword(
        email: String, password: String
    ): Resource<User?> {
        return withContext(Dispatchers.IO) {
            safeCall {

                val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                var isVerified = false

                authResult.user?.let { user ->
                    isVerified = user.isEmailVerified
                }
                if (!isVerified) {
                    FirebaseAuth.getInstance().currentUser?.sendEmailVerification()!!.await()
                    Resource.Error("Email not Verified")
                } else {
                    var usersRepository: UsersRepositoryImpl = UsersRepositoryImpl()
                    try {
                        var userToReturn = usersRepository.getUserRemote(authResult.user!!.uid)
                        userToReturn

                    } catch (exception: Exception) {
                        Resource.Error(exception.localizedMessage.toString())
                    }
                }
            }
        }
    }/*
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        checkIfEmailVerified(object : OnCompleteCallback {
                            override fun onError(exception: java.lang.Exception) {
                                super.onError(exception)
                                mCallback.onLoginError(exception)
                                FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
                                    ?.addOnCompleteListener { task ->
                                        if (!task.isSuccessful) {
                                            mCallback.onError(task.exception!!)
                                        }

                                    }
                            }

                            override fun onComplete(success: Boolean, result: Any?) {
                                if (success) {
                                    Log.d("LOGIN", "signInWithEmail:success")
                                    val userKey = auth.currentUser!!.uid
                                    UsersWSClient.instance.getUserProfile(
                                        userKey,
                                        object : OnCompleteCallback {
                                            override fun onComplete(success: Boolean, result: Any?) {
                                                if (success) {
                                                    val user: User = result as User
                                                    mCallback.afterUserAuthCheckCompletion(user)
                                                }
                                            }

                                            override fun onError(exception: Exception) {
                                                super.onError(exception)
                                                //   mCallback.onLoginError(exception)
                                                val user: User = User()
                                                user.user_key =
                                                    FirebaseAuth.getInstance().uid.toString()
                                                user.user_type = UserType.COMMON_USER.toString()
                                                user.telephone_number =
                                                    FirebaseAuth.getInstance().currentUser?.phoneNumber
                                                user.email_address =
                                                    FirebaseAuth.getInstance().currentUser?.email.toString()
                                                mCallback.afterUserAuthCheckCompletion(user)
                                            }
                                        })

                                } else {
                                    mCallback.onLoginError(Exception("ERROR_EMAIL_NOT_VERIFIED"))


                                }
                            }
                        })
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("LOGIN", "signInWithEmail:failure", task.exception)
                        mCallback.onLoginError(task.exception!!)
    //                    updateUI(null)
                    }
                }
            */

    override suspend fun getAuthToken(): Resource<String?> {
        return withContext(Dispatchers.IO) {
            safeCall {
                try {
                    val call = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
                    return@safeCall Resource.Success(call.token.toString())
                } catch (exception: Exception) {
                    Resource.Error(exception.localizedMessage.toString())
                }
            }
        }
    }

    override suspend fun verifyIfPhoneNumberExists(phoneNumber: String): Resource<Boolean?> {
        return withContext(Dispatchers.IO) {
            safeCall {
                try {
                    val data: MutableMap<String, Any> = HashMap()
                    data["phone_number"] = phoneNumber
                    try {
                        var call = FirebaseFunctions.getInstance()
                            .getHttpsCallable("checkIfUserExistsByPhoneNumber").call(data).await()

                        val result: HashMap<String, Any?> = call.data as HashMap<String, Any?>
                        val userExists = result["data"] as Boolean
                        Resource.Success<Boolean?>(userExists)

                    } catch (exception: Exception) {
                        Resource.Error<Boolean?>(exception.message.toString())
                    }
                    //--------------------
                } catch (exception: Exception) {
                    Resource.Error<Boolean?>(exception.message.toString())
                }
            }
        }
    }

    override suspend fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential): Resource<FirebaseUser?> {
        return try {
            val call = FirebaseAuth.getInstance().signInWithCredential(credential).await()
            Resource.Success<FirebaseUser?>(call.user)
        } catch (exception: Exception) {
            Resource.Error<FirebaseUser?>(exception.message.toString())
        }


    }

    override suspend fun createUserWithEmailPassword(
        email: String, password: String
    ): Resource<FirebaseUser?> {
        return try {

            val call =
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).await()

            try {
                Resource.Success<FirebaseUser?>(call.user)
            }
            catch (exception: Exception) {
                Resource.Error<FirebaseUser?>(exception.message.toString())
            }

        } catch (exception: Exception) {
            Resource.Error<FirebaseUser?>(exception.message.toString())
        }
    }


    override suspend fun sendEmailVerification(firebaseUser : FirebaseUser) : Resource<Boolean?> {

        return try {
            val call = firebaseUser.sendEmailVerification().await()
            Resource.Success<Boolean?>(true)
        } catch (exception: Exception) {
            Resource.Error<Boolean?>(exception.message.toString())
        }
    }
}
