package com.iyr.ian.repository.implementations.databases.realtimedatabase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.functions.FirebaseFunctions
import com.iyr.ian.dao.models.Contact
import com.iyr.ian.dao.models.NotificationList
import com.iyr.ian.dao.repositories.ContactGroupsRepository
import com.iyr.ian.dao.repositories.ContactsRepository
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.tasks.await

class ContactGroupsRepositoryImpl : ContactGroupsRepository() {


    override suspend fun postContactGroup(userKey: String, groupName: String) : Resource<NotificationList?>{
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
                        var newGroup = call.data as NotificationList
                        Resource.Success<NotificationList?>(newGroup)
                    } else {
                        Resource.Error<NotificationList?>("Error: Error looking for contacts")
                    }
                } catch (exception: Exception) {
                    Resource.Error<NotificationList?>(exception.message.toString())
                }
            } else {
                Resource.Error<NotificationList?>("error_getting_token")
            }
        } catch (exception: Exception) {
            Resource.Error<NotificationList?>(exception.message.toString())
        }
    }


}