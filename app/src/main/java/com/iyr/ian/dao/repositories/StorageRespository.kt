package com.iyr.ian.dao.repositories

import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.support_models.MediaTypesEnum
import java.io.File


interface StorageInterface {
    suspend fun downloadStoredItem(path: String, key: String, fileName : String, localPath : String): Resource<String?>
    suspend fun downloadStoredItem(path: String, localPath : String): Resource<String?>

    /**
     * Obtiene la ubicacion del archivo en el servidor para que pueda ser descargado directamente
     */
    suspend fun getFileUrl(path: String,
                           subFolder: String,
                           fileName: String): Resource<String?>


    fun uploadFileWithRetry(file: File, folder: String, onComplete: (Boolean, String?) -> Unit)

    fun retryUpload(file: File, folder: String, onComplete: (Boolean, String?) -> Unit)

    /***
     * Genera una referencia al archivo en el servidor
     */
    fun generateStorageReference(path: String): Any?


}

abstract class StorageRepository : StorageInterface {

    protected var authManager: Any? = null
    protected val tableReference: Any? = null
//    protected val tableName = "phone_to_contacts"


    fun uploadMediaFile(file: File, destinationFolder : String, mediaType: MediaTypesEnum, onComplete: (Boolean, String?) -> Unit) {

        when (mediaType) {

            MediaTypesEnum.IMAGE -> {
                // Compress image if needed
//                val compressedFile = compressImage(context, file)
                uploadFileWithRetry(file, destinationFolder, onComplete)
            }
            MediaTypesEnum.VIDEO -> {
                // Compress video if needed
  //              val compressedFile = compressVideo(context, file)
                uploadFileWithRetry(file, destinationFolder, onComplete)
            }
            MediaTypesEnum.AUDIO -> {
                uploadFileWithRetry(file, destinationFolder, onComplete)
            }
            else -> {
                onComplete(false, "Unsupported media type")
            }
        }
    }

}

