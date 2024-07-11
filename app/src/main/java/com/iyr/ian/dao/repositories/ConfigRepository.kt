package com.iyr.ian.dao.repositories


import com.iyr.ian.dao.models.SystemConfig
import com.iyr.ian.dao.models.User
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.chat.models.Message
import com.iyr.ian.utils.support_models.MediaFile
import kotlinx.coroutines.flow.Flow

interface ConfigRepositoryInterface {
    fun getConfigFlow(): Flow<Resource<ConfigRepository.ConfigDataEvent>>?
}


abstract class ConfigRepository : ConfigRepositoryInterface {
    private var authManager: Any? = null
    private val tableReference: Any? = null
    protected val tableName = "config"

    sealed class ConfigDataEvent {
        data class OnChildAdded(val data  : SystemConfig, val previousChildName: String?)   : ConfigDataEvent()
        data class OnChildChanged(val data: SystemConfig, val previousChildName: String?) : ConfigDataEvent()
        data class OnChildRemoved(val data: SystemConfig) : ConfigDataEvent()
        data class OnChildMoved(val data  : SystemConfig, val previousChildName: String?) : ConfigDataEvent()
        data class OnError(val exception: Exception) : ConfigDataEvent()
    }


}