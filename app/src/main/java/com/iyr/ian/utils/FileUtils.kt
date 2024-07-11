package com.iyr.ian.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.iyr.ian.app.AppClass
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
        val file =
            File(AppClass.instance.getExternalFilesDir("/")?.absolutePath, uri.toString())

        return if (file != null)
            file.extension
        else
            null
    }

    fun getFileExtension(context: Context, filePath: String): String? {
        if (filePath == null) // checks if the String is null {
            return null

        val file =
            File(context.getExternalFilesDir("/")?.absolutePath, filePath)
        return if (file != null)
            file.extension
        else
            null
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
        val file =
            File(filePath)
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

                    if ( !File(currentPath).exists())
                    {
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

fun Context.getFileExtension(filePath: String): String? {
    return FileUtils().getFileExtension(this, filePath)
}
