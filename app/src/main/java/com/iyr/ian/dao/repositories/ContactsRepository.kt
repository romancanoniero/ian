package com.iyr.ian.dao.repositories

import com.iyr.ian.dao.models.Contact
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.flow.Flow


interface ContactsInterface {
    suspend fun putOnPendingInvitationList(userWhoInviteKey: String, phoneNumber: String): Boolean
    suspend fun postContactInvitation( userWhoInvitesKey: String, userToInviteKey: String ): Resource<Boolean?> // Crea invitacion de contacto a un usuario que ya existe en la App
    suspend fun searchContacts( myKey : String , searchString: String): Resource<ArrayList<Contact>?>
    suspend fun contactAcceptInvitation(userWhoAccept: String,userToAcceptKey: String): Resource<Boolean?>
    suspend fun contactsByUserFlow(userKey: String): Flow<ContactsRepository.DataEvent>

    suspend fun searchContactsFromPhone(
        myKey: String,
        searchString: String
    ): Resource<ArrayList<Contact>?>


}

abstract class ContactsRepository : ContactsInterface {

    protected var authManager: Any? = null
    protected val tableReference: Any? = null
    protected val tableName = "users_contacts"


    sealed class DataEvent {
        data class ChildAdded(val data: Contact, val previousChildName: String?) :
            DataEvent()
        {
            val notification = data
        }
        data class ChildChanged(val data: Contact, val previousChildName: String?):
            DataEvent()

        // Añade más eventos si es necesario, por ejemplo ChildRemoved
        data class ChildRemoved(val data: Contact) :
            DataEvent()
        // Añade más eventos si es necesario, por ejemplo ChildRemoved

        data class onChildMoved(val data: Contact, val previousChildName: String?) :
            DataEvent()
        // Añade más eventos si es necesario, por ejemplo onChildMoved
    }



}