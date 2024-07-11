package com.iyr.ian.utils

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.facebook.FacebookSdk.getApplicationContext
import com.google.firebase.storage.FirebaseStorage
import com.iyr.ian.app.AppClass
import com.iyr.ian.repository.implementations.databases.realtimedatabase.StorageRepositoryImpl
import kotlinx.coroutines.tasks.await
import java.io.File


enum class MediaFileDownloadStatus {
    READY, DOWNLOADING, ERROR
}

interface DownloadFileListener {
    fun onDownloadStatusChange(referenceTag: String?, downloadStatus: MediaFileDownloadStatus) {
        this.onDownloadStatusChange(referenceTag, downloadStatus, null)
    }

    fun onDownloadStatusChange(
        referenceTag: String?,
        downloadStatus: MediaFileDownloadStatus,
        locatUri: Uri?
    )

    fun onError(exception: Exception)

}


class FirebaseStorageUtils {

    private var storageRepository = StorageRepositoryImpl()

    fun getStorageObject(filePath: String, referenceTag: String?, callback: DownloadFileListener) {
        val appsInternalStorageFolder: File = getApplicationContext().getDir(
            "cache",
            Context.MODE_PRIVATE
        )

        val fileName = filePath.getJustFileName()
        if (appsInternalStorageFolder.list().contains(fileName)) {

            val localFile = Uri.parse(
                "file:" + getApplicationContext().getDir(
                    "cache",
                    Context.MODE_PRIVATE
                ).toString() + "/" + fileName
            )

            callback.onDownloadStatusChange(referenceTag, MediaFileDownloadStatus.READY, localFile)
        } else {
            callback.onDownloadStatusChange(referenceTag, MediaFileDownloadStatus.DOWNLOADING)
            val fileRef = FirebaseStorage.getInstance().reference.child(filePath)
            val localFile = File(
                getApplicationContext().getDir(
                    "cache",
                    Context.MODE_PRIVATE
                ).toString() + "/" + fileName
            )


            fileRef.getFile(localFile).addOnSuccessListener {
                // Local temp file has been created
                val localUri: Uri = localFile.toUri()
                callback.onDownloadStatusChange(
                    referenceTag,
                    MediaFileDownloadStatus.READY,
                    localUri
                )
            }.addOnFailureListener {
                // Handle any errors
                callback.onError(it)

            }

        }
    }


    /***
     * Devuelve la Url local de un archivo de almacenamiento; primero lo busca local y si no
     *  esta, lo descarga y devuelve su path local
     */
    suspend fun getStorageObject(filePath: String, referenceTag: String?): String? {


        val appsInternalStorageFolder: File = File(AppClass.instance.cacheDir.toString(), filePath.substringBeforeLast("/"))


/*
        if (!appsInternalStorageFolder.exists()) {
            appsInternalStorageFolder.mkdir()
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }
        */
        val fileName = filePath.getJustFileName()

        if (appsInternalStorageFolder.list()!=null && appsInternalStorageFolder.list().contains(fileName)) {
            val localFile = Uri.parse(
                "file:" + AppClass.instance.cacheDir.toString() + "/" + filePath
            )

            return localFile.path
//            callback.onDownloadStatusChange(referenceTag, MediaFileDownloadStatus.READY, localFile)
        } else {


            var localPath = (AppClass.instance.cacheDir.toString()+"/"+filePath.toString()).trim()
            if (localPath.startsWith("/"))
                localPath = localPath.substring(1)

            var call = storageRepository.downloadStoredItem(
                filePath,
                localPath
            )
//
            return call.data
            /*
                      val fileRef = FirebaseStorage.getInstance().reference.child(filePath)
                      val localFile = File(
                          getApplicationContext().getDir(
                              "cache",
                              Context.MODE_PRIVATE
                          ).toString() + "/" + fileName
                      )

                      var call = fileRef.getFile(localFile).await()

                      return call.toString())
          */
            TODO("Hacer que descargue pero desde los repositorios")
            /*
                        fileRef.getFile(localFile).addOnSuccessListener {
                            // Local temp file has been created
                            val localUri: Uri = localFile.toUri()
                            callback.onDownloadStatusChange(
                                referenceTag,
                                MediaFileDownloadStatus.READY,
                                localUri
                            )
                        }.addOnFailureListener {
                            // Handle any errors
                            callback.onError(it)
                        }
            */
            //     }

            //   return Resource.Success<String>()


        }


    }


}


suspend fun Context.uploadFileToFirebaseStorage(filePath: Uri, destinationFolder : String): String? {
    val storageRef = FirebaseStorage.getInstance().reference
    val fileRef = storageRef.child("${destinationFolder}")
    return try {
        fileRef.putFile(filePath).await()
        fileRef.downloadUrl.await().toString()
    } catch (e: Exception) {
        null
    }
}