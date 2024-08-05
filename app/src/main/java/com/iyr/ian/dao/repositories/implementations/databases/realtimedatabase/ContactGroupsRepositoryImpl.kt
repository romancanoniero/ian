package com.iyr.ian.repository.implementations.databases.realtimedatabase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.iyr.ian.dao.models.Contact
import com.iyr.ian.dao.models.ContactGroup
import com.iyr.ian.dao.repositories.ContactGroupsRepository
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.tasks.await

class ContactGroupsRepositoryImpl : ContactGroupsRepository() {


    override suspend fun postContactGroup(userKey: String, groupName: String) : Resource<ContactGroup?>{
        return try {
            val token = FirebaseAuth.getInstance().currentUser!!.getIdToken(false).await()
            if (token != null) {
                val data: MutableMap<String, Any> = HashMap()
                data["auth_token"] = token.token.toString()
                data["user_key"] = userKey
                data["group_name"] = groupName
                try {
                    val contactsFound = ArrayList<Contact?>()
                    var call = FirebaseFunctions.getInstance()
                        .getHttpsCallable("notificationListCreate")
                        .call(data).await()

                    if (call.data != null) {
                        var newGroup = call.data as ContactGroup
                        Resource.Success<ContactGroup?>(newGroup)
                    } else {
                        Resource.Error<ContactGroup?>("Error: Error looking for contacts")
                    }
                } catch (exception: Exception) {
                    Resource.Error<ContactGroup?>(exception.message.toString())
                }
            } else {
                Resource.Error<ContactGroup?>("error_getting_token")
            }
        } catch (exception: Exception) {
            Resource.Error<ContactGroup?>(exception.message.toString())
        }
    }


}