package com.iyr.ian.repository.implementations.databases.realtimedatabase

import android.net.Uri
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.iyr.ian.dao.repositories.StorageRepository
import com.iyr.ian.utils.FileUtils
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.tasks.await
import java.io.File

class StorageRepositoryImpl : StorageRepository() {

    override suspend fun downloadStoredItem(
        path: String,
        key: String,
        fileName: String,
        localPath: String
    ): Resource<String?> {
        return try {

            val storageReference = StorageRepositoryImpl().generateStorageReference("${path}${key}/${fileName}") as StorageReference

            var destinationFile = File(localPath + fileName)
            var response = storageReference.getFile(destinationFile).await()
            Resource.Success<String?>(destinationFile.path)
        } catch (ex: Exception) {
            Resource.Success<String?>(ex.message)
        }
    }


    override suspend fun downloadStoredItem(path: String, localPath: String): Resource<String?> {
        return try {
            val storageReference = FirebaseStorage.getInstance()
                .getReference(path)

            var destinationFile = File(localPath)

            FileUtils().createDirectoriesStructure(localPath)

            var response = storageReference.getFile(destinationFile).await()
            Resource.Success<String?>(destinationFile.path)

        } catch (ex: Exception) {
            Resource.Success<String?>(ex.message)

        }


    }

    override suspend fun getFileUrl(
        path: String,
        subFolder: String,
        fileName: String
    ): Resource<String?> {
        return try {
            val ref = FirebaseStorage.getInstance()
                .getReference(path)
                .child(subFolder)
                .child(fileName).path
            Resource.Success<String?>(ref)

        } catch (ex: Exception) {
            Resource.Error<String?>(ex.message.toString())
        }
    }


    override fun uploadFileWithRetry(
        file: File,
        folder: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val storageReference: StorageReference =
            FirebaseStorage.getInstance().reference.child("$folder/${file.name}")
        val uploadTask: UploadTask = storageReference.putFile(Uri.fromFile(file))

        uploadTask.addOnSuccessListener {
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                onComplete(true, uri.toString())
            }.addOnFailureListener { exception ->
                onComplete(false, exception.message)
            }
        }.addOnFailureListener { exception ->
            // Handle failure and retry
            retryUpload(file, folder, onComplete)
        }.addOnPausedListener {
            // Handle paused state if needed
        }
    }

    override fun retryUpload(
        file: File,
        folder: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val storageReference: StorageReference =
            FirebaseStorage.getInstance().reference.child("$folder/${file.name}")
        val uploadTask: UploadTask = storageReference.putFile(Uri.fromFile(file))

        uploadTask.addOnSuccessListener {
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                onComplete(true, uri.toString())
            }.addOnFailureListener { exception ->
                onComplete(false, exception.message)
            }
        }.addOnFailureListener { exception ->
            // Retry logic can be added here, e.g., exponential backoff
            onComplete(false, exception.message)
        }
    }


    fun File.uploadMediaWithRetry(
        folder: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        StorageRepositoryImpl().uploadFileWithRetry(this, folder, onComplete)
    }


    @UnstableApi
    override fun generateStorageReference(path: String): Any? {
       val reference = FirebaseStorage.getInstance().getReference(path)
       Log.d("STORAGE", "Reference: ${reference.toString()  }")
        return reference
    }

}