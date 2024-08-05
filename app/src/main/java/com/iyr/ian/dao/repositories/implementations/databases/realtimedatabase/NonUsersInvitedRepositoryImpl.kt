package com.iyr.ian.repository.implementations.databases.realtimedatabase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.iyr.ian.dao.models.UserMinimum
import com.iyr.ian.dao.repositories.NonUsersInvitedRepository
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.tasks.await

class NonUsersInvitedRepositoryImpl : NonUsersInvitedRepository() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference = FirebaseDatabase.getInstance().getReference(tableName)

    override suspend fun addNonUserInvitation(
        key: String,
        contact: UserMinimum
    ): Resource<Boolean?> {
        return try {
            databaseReference.child(key).child(contact.user_key).setValue(contact).await()
            Resource.Success<Boolean?>(true)
        } catch (exception: Exception) {
            Resource.Error<Boolean?>(exception.message.toString())
        }

    }

    override suspend fun cancelNonUserInvitation(key: String, userKey: String): Resource<Boolean?> {
        return try {
            databaseReference.child(key).child(userKey).removeValue().await()
            Resource.Success<Boolean?>(true)
        } catch (exception: Exception) {
            Resource.Error<Boolean?>(exception.message.toString())
        }
    }


}

