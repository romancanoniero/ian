package com.iyr.ian.dao.repositories

import com.iyr.ian.dao.models.SpeedDialContact
import com.iyr.ian.utils.PhoneContact
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.flow.Flow

interface SpeedDialInterface {
    /**
     * Agrega los contactos de la lista a la lista de marcación rápida
     */
    suspend fun addSpeedDialContacts(contacts: List<PhoneContact>): Resource<List<SpeedDialContact>>
    suspend fun getSpeedDialContactsFlow(userKey: String): Flow<Resource<ArrayList<SpeedDialContact>?>>
}

abstract class SpeedDialRepository : SpeedDialInterface {

    protected var authManager: Any? = null
    protected val tableReference: Any? = null
    protected val tableName = "users_speed_dial"

    sealed class DataEvent {
        data class OnChildAdded(val data: SpeedDialContact, val previousChildName: String?) :
            DataEvent()

        data class OnChildChanged(val data: SpeedDialContact, val previousChildName: String?) :
            DataEvent()

        // Añade más eventos si es necesario, por ejemplo ChildRemoved
        data class OnChildRemoved(val data: SpeedDialContact) : DataEvent()
        // Añade más eventos si es necesario, por ejemplo ChildRemoved

        data class OnChildMoved(val data: SpeedDialContact, val previousChildName: String?) :
            DataEvent()
        // Añade más eventos si es necesario, por ejemplo onChildMoved

        data class OnError(val exception: Exception) : DataEvent()
    }
}