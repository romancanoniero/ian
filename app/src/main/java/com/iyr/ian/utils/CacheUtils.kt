package com.iyr.ian.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.bumptech.glide.Glide
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CacheUtils
/*
Context.isFileInCache(fileName: String): String? {

    return null
}
*/

fun Bitmap.saveImageToCache(context: Context, imageName: String, folderName: String): Boolean {
    val cacheDir = File(context.cacheDir, folderName)
    if (!cacheDir.exists()) {
        cacheDir.mkdirs()
    }

    val imageFile = File(cacheDir, imageName)

    return try {
        val out = FileOutputStream(imageFile)
        this.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.flush()
        out.close()
        true
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}

/**
 * Load image from cache
 */
fun Context.loadImageFromCache( imageName: String, folderName: String): Bitmap? {
    val cacheDir = File(this.cacheDir, folderName)
    val imageFile = File(cacheDir, imageName)

    return if (imageFile.exists()) {
        BitmapFactory.decodeFile(imageFile.absolutePath)
    } else {
        null
    }
}


suspend fun Context.getImageFromUrl(url: String): Bitmap? {
    // Construye la URL de la imagen estática del mapa con un marcador

    // Descarga la imagen utilizando una biblioteca como Picasso o Glide
    var target = Glide.with(this)
        .asBitmap()
        .load(url)
        .fitCenter()
        .submit()
    try {
        val bitmap = target.get() // Esto bloqueará el hilo hasta que la descarga esté completa
        // Usa el bitmap aquí
    } catch (e: Exception) {
        // Maneja la excepción aquí
    }
    return target.get()
}
