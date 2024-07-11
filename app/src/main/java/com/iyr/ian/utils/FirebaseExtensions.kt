package com.iyr.ian.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
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
            // Obtener el nombre del archivo

            var _name = ""

            if (internalSubPath == null) _name = name
            else _name = internalSubPath
            // Verificar si el archivo existe en el caché
            var subPath: String = this@downloadUrlWithCache.path.substringBeforeLast("/")
            //if (!internalSubPath.isNullOrEmpty()) subPath = "${internalSubPath}"


            var fileFinalPath = "${subPath}/${name}"
            var cacheFile = File(context.cacheDir, fileFinalPath)



            Log.d("DOWNLOADURLWITHCHACHE", "cacheFile = $cacheFile")
            //   cacheFile = "file:$cacheFile"

            if (cacheFile.exists()) {
                // Si el archivo existe en el caché, retornar su ubicación local
                return@withContext "file:" + cacheFile.absolutePath
            } else {
                // Si no existe en el caché, descargarlo y guardarlo

                val storage = FirebaseStorage.getInstance()
                var storageRef = storage.reference


                var dirsToCreate = this@downloadUrlWithCache.path.substringBeforeLast("/").split("/")
                var path = context.cacheDir.toString()
                dirsToCreate.forEach { dir ->
                    if (dir.isNotEmpty()) {

                        try {

                            storageRef = storageRef.child(dir)
                        } catch (e: Exception) {
                            var pp = 2
                        }

                    }
                }
                storageRef = storageRef.child(name)

                var downloadedFile: File? = null
                // Descargar el archivo
                try {
                    val url = storageRef.downloadUrl.await().toString()
                    downloadedFile = File(context.cacheDir, fileFinalPath)

                    if (!File(cacheFile.path.substringBeforeLast("/")).exists()) {
                        var dirsToCreate = fileFinalPath.substringBeforeLast("/").split("/")
                        var path = context.cacheDir.toString()
                        dirsToCreate.forEach { dir ->
                            try {
                                path = path + "/" + dir
                                File(path).mkdir()
                            } catch (e: Exception) {
                                var pp = 2
                            }
                        }
                    }

                    FirebaseStorage.getInstance().getReferenceFromUrl(url).getFile(downloadedFile)
                        .await()
                    // Guardar el archivo en el caché
                    // Retornar la ubicación local del archivo
                } catch (exception: Exception) {
                    var pp = 3
                }
//                withContext(Dispatchers.IO) {
                    "file:" + downloadedFile?.absolutePath
  //              }


                //         return@withContext null.toString()
            }
        }
    }


    fun Context.getCurrentAuthenticationMethod(): String {
        var choosenProvider = FirebaseAuth.getInstance().getAccessToken(false).result.toString()
        Toast.makeText(this, "Authentication = $choosenProvider", Toast.LENGTH_SHORT).show()
        return choosenProvider
    }


}

