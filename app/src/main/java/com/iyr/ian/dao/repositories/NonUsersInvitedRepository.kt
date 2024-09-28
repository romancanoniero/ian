package com.iyr.ian.dao.repositories

import com.iyr.ian.dao.models.UserMinimum
import com.iyr.ian.utils.coroutines.Resource

interface NonUsersInvitedInterface {
    suspend fun addNonUserInvitation(userKey: String, contact: UserMinimum): Resource<Boolean?>
    suspend fun cancelNonUserInvitation(key: String, userKey: String): Resource<Boolean?>

}

abstract class NonUsersInvitedRepository : NonUsersInvitedInterface {

    private var authManager: Any? = null
    protected var tableName = "non_users_invited"
    private val tableReference: Any? = null


}