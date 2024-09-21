package com.iyr.ian.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.StorageReference
import com.iceteck.silicompressorr.SiliCompressor
import com.iyr.ian.app.AppClass
import com.iyr.ian.repository.implementations.databases.realtimedatabase.StorageRepositoryImpl
import com.iyr.ian.utils.support_models.MediaFile
import com.iyr.ian.utils.support_models.MediaTypesEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


class FileUtils {

    fun appsInternalStorageFolder(context: Context): File {
        return context.getDir("cache", Context.MODE_PRIVATE)
    }

    fun getFileExtension(uri: Uri?): String? {
        if (uri == null) // checks if the String is null {
            return null
        val file = File(AppClass.instance.getExternalFilesDir("/")?.absolutePath, uri.toString())

        return if (file != null) file.extension
        else null
    }

    fun getFileExtension(context: Context, filePath: String): String? {
        if (filePath == null) // checks if the String is null {
            return null

        val file = File(context.getExternalFilesDir("/")?.absolutePath, filePath)
        return if (file != null) file.extension
        else null
    }


    fun getJustFileName(url: String): String {
        val filename = url.substring(url.lastIndexOf('/') + 1)
        return filename
    }

    fun moveFile(inputPath: String, inputFile: String, outputPath: String): String {
        var `in`: InputStream? = null
        var out: OutputStream? = null
        try {

            //create output directory if it doesn't exist
            val dir = File(outputPath)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            `in` = FileInputStream("$inputPath/$inputFile")
            out = FileOutputStream("$outputPath/$inputFile")
            val buffer = ByteArray(1024)
            var read: Int
            while (`in`.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
            `in`.close()
            `in` = null

            // write the output file
            out.flush()
            out.close()
            out = null


            // delete the original file
            File("$inputPath/$inputFile").delete()

            return "$outputPath/$inputFile"

        } catch (fnfe1: FileNotFoundException) {
            Log.e("tag", fnfe1.message.toString())
        } catch (e: Exception) {
            Log.e("tag", e.message.toString())
        }
        return ""
    }


    fun deleteFile(deletePath: String) {
        try {
            // delete the original file
            File("$deletePath").delete()
        } catch (fnfe1: FileNotFoundException) {
            Log.e("tag", fnfe1.message.toString())
        } catch (e: Exception) {
            Log.e("tag", e.message.toString())
        }
    }


    fun copyFile(inputPath: String, inputFile: String, outputPath: String): String? {
        var `in`: InputStream? = null
        var out: OutputStream? = null
        try {

            //create output directory if it doesn't exist
            val dir = File(outputPath)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            `in` = FileInputStream("$inputPath/$inputFile")
            out = FileOutputStream("$outputPath/$inputFile")
            val buffer = ByteArray(1024)
            var read: Int
            while (`in`.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
            `in`.close()
            `in` = null

            // write the output file
            out.flush()
            out.close()
            out = null

            // delete the original file
            //File(inputPath + "/" + inputFile).delete()
            return "$outputPath/$inputFile"
        } catch (fnfe1: FileNotFoundException) {
            Log.e("tag", fnfe1.message.toString())
        } catch (e: Exception) {
            Log.e("tag", e.message.toString())
        }
        return null
    }


    fun fileExists(filePath: String): Boolean {
        val file = File(filePath)
        try {
            return file.exists()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    fun createDirectoriesStructure(fullPath: String) {
        var currentPath = ""

        var dirsToCreate = fullPath.substringBeforeLast("/").split("/")
       /*
        // agrego el ultimo directorio despues de la ultima barra
        if (fullPath.substringAfterLast("/") != "") {
           dirsToCreate += fullPath.substringAfterLast("/")
        }
     */
        dirsToCreate.forEach { dir ->
            try {
                if (dir != "") {
                    try {
                        currentPath = currentPath + "/" + dir

                        if (!File(currentPath).exists()) {
                            File(currentPath).mkdir()

                        }
                    } catch (e: Exception) {
                        var pp = 2
                    }
                }
            } catch (e: Exception) {
                var pp = 2
            }
        }
    }

}


fun Context.createDirectoryStructure(fileName: String) {
    FileUtils().createDirectoriesStructure(fileName)
}

fun Context.appsInternalStorageFolder(): File {
    return FileUtils().appsInternalStorageFolder(this)
}

fun String.getJustFileName(): String {
    return FileUtils().getJustFileName(this)
}

fun String.getJustPath(): String {
    return this.substringBeforeLast("/")
}

fun String.getFileExtension(context: Context): String? {
    return FileUtils().getFileExtension(context, this)
}

fun Uri.getFileExtension(context: Context): String? {
    return FileUtils().getFileExtension(this)
}

fun Context.deleteFile(deletePath: String) {
    return FileUtils().deleteFile(deletePath)
}


fun Context.copyFile(inputPath: String, inputFile: String, outputPath: String) : String? {
  return  FileUtils().copyFile(inputPath, inputFile, outputPath)
}

/***
 * Copy a file to a new location
 * @param context
 * @param outputPath
 * @return
 */
fun File.copyTo(context: Context, outputPath: String): File? {
    var `in`: InputStream? = null
    var out: OutputStream? = null
    try {
        //create output directory if it doesn't exist
        val dir = File(outputPath)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        var source = this.absolutePath
        var output = "${outputPath}/${this.absolutePath.getJustFileName()}"
        `in` = FileInputStream(source)
        out = FileOutputStream(output)
        val buffer = ByteArray(1024)
        var read: Int
        while (`in`.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
        `in`.close()
        `in` = null

        // write the output file
        out.flush()
        out.close()
        out = null
        // delete the original file
        //File(inputPath + "/" + inputFile).delete()
        return File(output)
    } catch (fnfe1: FileNotFoundException) {
        Log.e("tag", fnfe1.message.toString())
    } catch (e: Exception) {
        Log.e("tag", e.message.toString())
    }
    return null
}

/**
 * Move a file to a new location
 * @param context
 * @param outputPath
 * @return
 *
 * */
fun File.moveTo(context: Context, outputPath: String): File? {
    var `in`: InputStream? = null
    var out: OutputStream? = null
    try {
        //create output directory if it doesn't exist
        val dir = File(outputPath)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        var source = this.absolutePath
        var output = "${context.cacheDir}/${outputPath}/${this.absolutePath.getJustFileName()}"
        `in` = FileInputStream(source)
        out = FileOutputStream(output)
        val buffer = ByteArray(1024)
        var read: Int
        while (`in`.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
        `in`.close()
        `in` = null

        // write the output file
        out.flush()
        out.close()
        out = null
        // delete the original file
        //File(inputPath + "/" + inputFile).delete()
        this.delete()
        return File(output)
    } catch (fnfe1: FileNotFoundException) {
        Log.e("tag", fnfe1.message.toString())
    } catch (e: Exception) {
        Log.e("tag", e.message.toString())
    }
    return null
}


fun Context.getFileExtension(filePath: String): String? {
    return FileUtils().getFileExtension(this, filePath)
}

suspend fun File.toMediaFile(context: Context, prefix: String? = null): MediaFile? {
    var pp = 33
    var operationFile: File? = null
    var destinationFolderBuilder: StringBuilder = StringBuilder()
    destinationFolderBuilder.append(context.cacheDir.absolutePath)
    if (prefix != null) {
        destinationFolderBuilder.append("/")
        destinationFolderBuilder.append(prefix)
    }
    var destinationFolder = destinationFolderBuilder.toString()

    // si no esta en el cache, lo copio
    if (!this.absolutePath.startsWith(destinationFolder)) {
        when (this.absolutePath.getFileExtension(context)) {
            "jpg", "jpeg", "png" -> {
                operationFile = File(
                    SiliCompressor.with(context)
                        .compress("file:" + this.absolutePath, File(destinationFolder))
                )
            }

            "mp4", "3gp" -> {
                operationFile = File(this.absolutePath).copyTo(context, destinationFolder)
            }

            else -> {
                operationFile = copyTo(context, destinationFolder)
            }
        }

    } else {
        if (!this.absolutePath.startsWith(destinationFolder)) {
            // Si el archivo esta en cache , pero no en la carpeta de destino
            /*
                      operationFile =
                          moveTo(context, destinationFolder.replace(context.cacheDir.absolutePath, ""))
          */
            when (this.absolutePath.getFileExtension(context)) {
                "jpg", "jpeg", "png" -> {
                    operationFile = File(
                        SiliCompressor.with(context)
                            .compress(this.absolutePath, File(destinationFolder))
                    )
                }

                "mp4", "3gp" -> {
                    operationFile = File(
                        SiliCompressor.with(context)
                            .compressVideo(this.absolutePath, destinationFolder)
                    )
                }

                else -> {
                    operationFile = copyTo(context, destinationFolder)
                }
            }

        }
        else
        {
            operationFile = File(this.absolutePath)
        }
    }

    try {
        val mediaType: MediaTypesEnum = when (this.absolutePath.getFileExtension(context)) {
            "jpg", "jpeg", "png" -> {
                MediaTypesEnum.IMAGE
            }

            "3gp" -> {
                MediaTypesEnum.AUDIO
            }

            "mp4" -> {
                MediaTypesEnum.VIDEO
            }

            else -> {
                MediaTypesEnum.TEXT
            }
        }

        /*
        when (this.absolutePath.getFileExtension(context)) {
            "jpg", "jpeg", "png" -> {
                // Comprimo el archivo antes de convertirlo.

                val fileDest = File(operationFile?.absolutePath?.substringBeforeLast("/"))
                val filePath =
                    SiliCompressor.with(context)
                        .compress("file:" + operationFile?.absolutePath?.toString(), fileDest)

                val file = File(filePath)
                val newFileName = operationFile?.absolutePath?.getJustFileName()
                val newFile = File(file.parent, newFileName)
                file.renameTo(newFile)
/*
                MultimediaUtils(context).convertFileToBase64(
                    Uri.parse(
                        operationFile?.absolutePath?.toString()
                    )
                ).toString()
*/
            }

            "mp4", "3gp" -> {

                // Comprimo el archivo antes de convertirlo.
                /*
                                val fileDest = File(operationFile?.absolutePath?.substringBeforeLast("/"))
                                val filePath =
                                    SiliCompressor.with(context).compressVideo("file:"+operationFile?.absolutePath?.toString(), fileDest.absolutePath)

                                val file = File(filePath)
                                val newFileName = operationFile?.absolutePath?.getJustFileName()
                                val newFile = File(file.parent, newFileName)
                                file.renameTo(newFile)
                */
  /*
                MultimediaUtils(context).convertFileToBase64(
                    Uri.parse(
                        operationFile?.absolutePath?.toString()
                    )
                ).toString()
*/

            }

            else -> ({
            }).toString()
        }
        */
        val media = MediaFile(mediaType, operationFile?.absolutePath?.getJustFileName())
        media.localFullPath = operationFile?.absolutePath
     /*
        if (encodedFile.isNotEmpty()) {
            media.bytesB64 = encodedFile
        }
        */
        return media
    } catch (ex: Exception) {
        return throw ex
    }
}

/***
 * Obtiene la ubicacion local de un archivo luego de buscarla
 * @param fileName El nombre del archivo a buscar
 * @param folder La carpeta donde se encuentra el archivo
 * @return La ubicacion del archivo en el almacenamiento local
 */
fun Context.getStoredFile(
    fileName: String, folder: String
): String {
    val filePath = when (fileName.substringAfterLast('.', "").lowercase()) {
        "jpg", "jpeg", "png" -> "${cacheDir.absolutePath}/${folder}/$fileName"
        "mp4", "3gp" -> "${cacheDir.absolutePath}/${folder}/$fileName"
        else -> "${cacheDir.absolutePath}/${folder}/$fileName"
    }
    val file = File(filePath)


    if (file.exists()) {
        return file.absolutePath
    } else {
        return try {
            runBlocking {
                val storageReference = ( StorageRepositoryImpl().generateStorageReference("${folder}/${fileName}") as StorageReference)

                // Descarga el archivo desde Firebase Storage y almacénalo en el directorio de caché
                withContext(Dispatchers.IO) {
                    FileUtils().createDirectoriesStructure(cacheDir.absolutePath + "/" + folder)
                    //val localFile = File.createTempFile(fileName, null, File(cacheDir, folder))
                    val localFile = File(cacheDir.absolutePath + "/" + folder, fileName)
                    storageReference.getFile(localFile).await()
                    localFile.absolutePath
                }
            }
        } catch (e: Exception) {
            throw e
        }


    }
    return throw Exception("Error al obtener el archivo")
}


suspend fun Context.getStoredFileSuspend(
    fileName: String, folder: String
): String {
    val filePath = when (fileName.substringAfterLast('.', "").lowercase()) {
        "jpg", "jpeg", "png" -> "${cacheDir.absolutePath}/${folder}/$fileName"
        "mp4", "3gp" -> "${cacheDir.absolutePath}/${folder}/$fileName"
        else -> "${cacheDir.absolutePath}/${folder}/$fileName"
    }
    val file = File(filePath)


    if (file.exists()) {
        return file.absolutePath
    } else {
        return try {
            val storageReference = ( StorageRepositoryImpl().generateStorageReference("${folder}/${fileName}") as StorageReference)
            // Descarga el archivo desde Firebase Storage y almacénalo en el directorio de caché
            FileUtils().createDirectoriesStructure(cacheDir.absolutePath + "/" + folder)
            //val localFile = File.createTempFile(fileName, null, File(cacheDir, folder))
            val localFile = File(cacheDir.absolutePath + "/" + folder, fileName)
      //      storageReference.getFile(localFile).await()
            StorageRepositoryImpl().downloadStoredItem("${folder}/${fileName}", localFile.absolutePath)
            localFile.absolutePath

        } catch (e: Exception) {
           Log.d("STORAGE_ERROR", e.message.toString())
            throw e
        }


    }
    return throw Exception("Error al obtener el archivo")
}