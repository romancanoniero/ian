package com.iyr.ian.dao.repositories

import com.iyr.ian.dao.models.ContactGroup
import com.iyr.ian.utils.coroutines.Resource


interface ContactGroupsInterface {

    suspend fun postContactGroup(userKey: String, groupName: String) : Resource<ContactGroup?>
}

abstract class ContactGroupsRepository : ContactGroupsInterface {

    protected var authManager: Any? = null
    protected val tableReference: Any? = null
    protected val tableName = "phone_to_contacts"


}