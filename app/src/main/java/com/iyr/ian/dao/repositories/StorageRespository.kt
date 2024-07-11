package com.iyr.ian.dao.repositories

import com.iyr.ian.utils.coroutines.Resource


interface StorageInterface {
    suspend fun downloadStoredItem(path: String, key: String, fileName : String, localPath : String): Resource<String?>
    suspend fun downloadStoredItem(path: String, localPath : String): Resource<String?>

    /**
     * Obtiene la ubicacion del archivo en el servidor para que pueda ser descargado directamente
     */
    suspend fun getFileUrl(path: String,
                           subFolder: String,
                           fileName: String): Resource<String?>

}

abstract class StorageRepository : StorageInterface {

    protected var authManager: Any? = null
    protected val tableReference: Any? = null
//    protected val tableName = "phone_to_contacts"


}