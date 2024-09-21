package com.iyr.ian.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.StorageReference
import com.iyr.ian.repository.implementations.databases.realtimedatabase.StorageRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

// Función de extensión para FirebaseStorageReference


object FirebaseExtensions {

    /**
     * Descarga un archivo de Firebase Storage y lo guarda en el caché de la aplicación.
     * Si el archivo ya existe en el caché, retorna su ubicación local.
     * @param context Contexto de la aplicación
     * @param internalSubPath Subcarpeta interna del caché donde se guardará el archivo
     * @return La ubicación del archivo
     */
    suspend fun StorageReference.downloadUrlWithCache(
        context: Context, internalSubPath: String? = null
    ): String {
        return withContext(Dispatchers.IO) {
            path
            Log.d("DOWNLOADURLWITHCACHE", "entre a obtener = ${path}")
            val fileName =  name.getJustFileName()
            val subPath = this@downloadUrlWithCache.path.substringBeforeLast("/")
            val fileFinalPath = "$subPath/$fileName"
            val cacheFile = File(context.cacheDir, fileFinalPath)

            Log.d("DOWNLOADURLWITHCACHE", "cacheFile = $cacheFile")

            if (cacheFile.exists()) {
                return@withContext "file:${cacheFile.absolutePath}"
            } else {
                try {
                    val url = this@downloadUrlWithCache.downloadUrl.await().toString()
                    val downloadedFile = File(context.cacheDir, fileFinalPath).apply {
                        parentFile?.mkdirs()
                    }
                      StorageRepositoryImpl().downloadStoredItem(url, downloadedFile.absolutePath)
//                    FirebaseStorage.getInstance().getReferenceFromUrl(url).getFile(downloadedFile).await()
                    return@withContext "file:${downloadedFile.absolutePath}"
                } catch (exception: Exception) {
                    Log.e("DOWNLOADURLWITHCACHE", "Error downloading file", exception)
                    throw exception
                }
            }
        }
    }

    fun Context.getCurrentAuthenticationMethod(): String {
        var choosenProvider = FirebaseAuth.getInstance().getAccessToken(false).result.toString()
        Toast.makeText(this, "Authentication = $choosenProvider", Toast.LENGTH_SHORT).show()
        return choosenProvider
    }


}

