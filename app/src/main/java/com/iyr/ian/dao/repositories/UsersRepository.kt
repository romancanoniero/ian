package com.iyr.ian.dao.repositories

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseUser
import com.iyr.ian.dao.models.User
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.support_models.MediaFile
import kotlinx.coroutines.flow.Flow

interface UsersInterface {
    suspend fun getUserLoginStatus(): Resource<FirebaseUser?>
    suspend fun getUserRemote(userKey: String): Resource<User?>
    fun getUserDataAsFlow(userKey: String): Flow<Resource<User>>?
    suspend fun listUsersByPhoneNumber(phoneNumber: String): Resource<ArrayList<User?>>
    suspend fun saveUser(_currentData: User, _originalData: User?): Resource<User?>
    suspend fun onDeleteAccount(): Resource<Boolean?>
    suspend fun updateUserStatus(userKey: String, pin: String, latLng: LatLng): Resource<Boolean?>
    suspend fun insertUser(user: User): Resource<Boolean?>
    suspend fun updateUserDataByMap(
        userKey: String, dataMap: HashMap<String, Any>
    ): Resource<Boolean?>

    suspend fun updateSubscriptionType(
        userKey: String, subscriptionTypeKey: String?
    ): Resource<Boolean?>

    suspend fun onUpdateUserImage(userKey: String, image: MediaFile) : Resource<Boolean?>
}

abstract class UsersRespository : UsersInterface {

    private var authManager: Any? = null
    protected var tableName = "users"
    private val tableReference: Any? = null


}