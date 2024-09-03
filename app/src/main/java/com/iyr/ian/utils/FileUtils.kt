package com.iyr.ian.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.iyr.ian.app.AppClass
import com.iyr.ian.utils.multimedia.MultimediaUtils
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

fun String.getFileExtension(context: Context): String? {
    return FileUtils().getFileExtension(context, this)
}

fun Uri.getFileExtension(context: Context): String? {
    return FileUtils().getFileExtension(this)
}

fun Context.deleteFile(deletePath: String) {
    return FileUtils().deleteFile(deletePath)
}


fun Context.copyFile(inputPath: String, inputFile: String, outputPath: String) {
    FileUtils().copyFile(inputPath, inputFile, outputPath)
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

    if (!this.absolutePath.startsWith(destinationFolder)) {
        operationFile = copyTo(context, destinationFolder)
    } else {
        if (!this.absolutePath.startsWith(destinationFolder)) {
            // Si el archivo esta en cache , pero no en la carpeta de destino
            operationFile =
                moveTo(context, destinationFolder.replace(context.cacheDir.absolutePath, ""))
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

        val encodedFile: String = when (this.absolutePath.getFileExtension(context)) {
            "jpg", "jpeg", "png", "mp4", "3gp" -> {



                 MultimediaUtils(context).convertFileToBase64(
                    Uri.parse(
                        operationFile?.absolutePath
                    )
                ).toString()

            }

            else -> ({
            }).toString()
        }

        val media = MediaFile(mediaType, operationFile?.absolutePath?.getJustFileName())
        if (encodedFile.isNotEmpty()) {
            media.bytesB64 = encodedFile
        }
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

    var cacheDir: File? = null
    cacheDir = File(this.cacheDir, folder)
    val file = File(cacheDir, fileName)

    if (file.exists()) {
        return file.absolutePath
    } else {
        return try {
            runBlocking {
                val storageReference = FirebaseStorage.getInstance().getReference(folder).child(fileName)

                // Descarga el archivo desde Firebase Storage y almacénalo en el directorio de caché
                withContext(Dispatchers.IO) {
                    val localFile = File.createTempFile(fileName, null, cacheDir)
                    storageReference.getFile(localFile).await()
                    localFile.absolutePath
                }
            }
        } catch (e: Exception) {
            throw e
        }
     /*   try {
            val storageReference =
                FirebaseStorage.getInstance().getReference(folder).child(fileName)

            //descarga el archivo (que puede ser de cualquier tipo) desde storage de firebase y almacenalo en el directorio de cache
            val file = try {
                val localFile = File.createTempFile(fileName, null, cacheDir)
                var call = storageReference.getFile(localFile).await()
                localFile.absolutePath
            } catch (e: Exception) {
                null
            }
        } catch (e: Exception) {
            return throw e
        }
        */

    }
    return throw Exception("Error al obtener el archivo")
}