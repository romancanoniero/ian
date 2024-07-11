package com.iyr.ian.repository.implementations.databases.realtimedatabase

import com.google.firebase.storage.FirebaseStorage
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
            val storageReference = FirebaseStorage.getInstance()
                .getReference(path)
                .child(key)
                .child(fileName)

            var destinationFile = File(localPath + fileName)
            var response = storageReference.getFile(destinationFile).await()




            Resource.Success<String?>(destinationFile.path)

        } catch (ex: Exception) {
            Resource.Success<String?>(ex.message)

        }
    }


    override suspend fun downloadStoredItem(path: String, localPath: String): Resource<String?>{
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



}