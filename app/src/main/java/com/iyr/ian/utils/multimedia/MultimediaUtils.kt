package com.iyr.ian.utils.multimedia

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.common.io.Files.getFileExtension
import com.iyr.ian.AppConstants
import com.iyr.ian.Constants
import com.iyr.ian.app.AppClass
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.repository.implementations.databases.realtimedatabase.StorageRepositoryImpl
import com.iyr.ian.ui.dialogs.ImageViewerDialog
import com.iyr.ian.ui.dialogs.TextDialog
import com.iyr.ian.ui.dialogs.VideoPlayerDialog
import com.iyr.ian.utils.FileUtils
import com.iyr.ian.utils.StringUtils
import com.iyr.ian.utils.getFileExtension
import com.iyr.ian.utils.getJustFileName
import com.iyr.ian.utils.support_models.MediaFile
import com.iyr.ian.utils.support_models.MediaTypesEnum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.Locale


interface MediaPlayerInterface {
    fun onBeforePlay()
    fun onAfterPlay()
}

private const val RESOLVE_HINT = 1008


class MultimediaUtils(val context: Context) {

    var storageRepositoryImpl: StorageRepositoryImpl = StorageRepositoryImpl()


    companion object {

        @Volatile
        var instance: MultimediaUtils? = null

        fun getInstance(context: Context): MultimediaUtils =
            instance ?: synchronized(this) {
                instance ?: MultimediaUtils(context).also {
                    instance = it

                }
            }


    }

    fun playSound(
        context: Context,
        soundResId: Int,
        callback: MediaPlayerInterface?
        /*
         beforePlayCallback: OnCompleteCallback?,
         afterPlayCallback: OnCompleteCallback?*/
    ) {
        // int resID = context.getResources().getIdentifier(playSoundName,"raw",context.getPackageName());
        var resourceName = context.resources.getResourceName(soundResId)
        val resourcePath = resourceName.split("/").toTypedArray()
        resourceName = resourcePath[resourcePath.size - 1] + ".mp3"
        val mediaFile = File(context.filesDir, resourceName)
        // Get a handler that can be used to post to the main thread
        val mainHandler = Handler(Looper.getMainLooper())
        //             player.start();
        val runnable = Runnable {
            val player = MediaPlayer.create(context, soundResId)
            callback?.onBeforePlay()
            //             player.start();
            player.setOnCompletionListener {
                player.release()
                callback?.onAfterPlay()
            }
            player.start()
        }
        mainHandler.post(runnable)
    }

    fun playSound(context: Context, audioFilePath: String) {
        this.playSound(context, audioFilePath, null)
    }
    /*
        fun playSound(audioFilePath: String, playerCallback: MediaPlayerInterface?) {
            this.playSound(audioFilePath,playerCallback)
        }
    */

    fun playSound(
        context: Context,
        audioFilePath: String,
        playerCallback: MediaPlayerInterface?
    ) {

        if (!audioFilePath.startsWith("file:") && !File(audioFilePath).exists()) {
            /*
                        val callback: DownloadFileListener = object : DownloadFileListener {
                            override fun onDownloadStatusChange(
                                referenceTag: String?,
                                downloadStatus: MediaFileDownloadStatusEnum
                            ) {
                                Log.d("ASYNC_DOWNLOAD", downloadStatus.name)
                            }

                            override fun onDownloadStatusChange(
                                referenceTag: String?,
                                downloadStatus: MediaFileDownloadStatusEnum,
                                locatUri: Uri?
                            ) {

                                playLocalSoundByUri(locatUri, playerCallback)
                            }

                            override fun onError(exception: Exception) {
                                throw exception
                            }
                        }
                        FirebaseStorageUtils().getStorageObject(audioFilePath, null, callback)
            */


            CoroutineScope(Dispatchers.IO).launch {
                var localFileName = storageRepositoryImpl.downloadStoredItem(
                    AppConstants.PROFILE_IMAGES_STORAGE_PATH,
                    "tempDirectory"
                ).data

                playSound(context, localFileName!!)
//                playLocalSoundByUri(audioFilePath, localFileName)

            }


        } else {
            playLocalSoundByUri(context, Uri.parse(audioFilePath), playerCallback)
        }

    }


    private fun playLocalSound(
        context: Context,
        locatUrl: String,
        playerCallback: MediaPlayerInterface?
    ) {
        playLocalSoundByUri(context, Uri.parse(locatUrl), playerCallback)
    }

    private fun playLocalSoundByUri(
        context: Context,
        locatUri: Uri?,
        playerCallback: MediaPlayerInterface?
    ) {

        //AppClass.instance.getCurrentActivity().run {
        //     activity.run {

        val mediaPlayer = MediaPlayer()
        AppClass.instance.setCurrentMediaPlayer(mediaPlayer)
        mediaPlayer.setDataSource(locatUri.toString())
        playerCallback?.onBeforePlay()
        mediaPlayer.setOnCompletionListener {
            mediaPlayer.release()
            AppClass.instance.setCurrentMediaPlayer(null)
            playerCallback?.onAfterPlay()
        }
        mediaPlayer.setOnErrorListener(object : MediaPlayer.OnErrorListener {
            override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
                AppClass.instance.setCurrentMediaPlayer(null)
                return true
            }
        })
        mediaPlayer.prepare()
        mediaPlayer.start()
        //     }


    }

    fun startRecording(activity: Activity, localFilePath: String): MediaRecorder? {

        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(activity)
        } else {
            MediaRecorder()
        }

        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                Constants.RECORD_AUDIO
            )

        } else {

            try {


                try {
                    /*
                                      localFilePath?.let {



                                      }
                  */


                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    recorder.setOutputFile(localFilePath)
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    recorder.prepare()
                } catch (e: IOException) {
                    Log.e("RECORDING", "prepare() failed")
                    return null
                }
            } catch (e: Exception) {
                Log.e("RECORDING", "prepare() failed")
                return null
            }


            recorder.start()
        }
        return recorder
    }

    fun stopRecording(recorder: MediaRecorder?, localFilePath: String): File {

        recorder!!.stop()
        recorder.release()

        //    mStatusTv.setText(getString(R.string.record_finished))
        //   uploadAudio()
        return File(localFilePath)
    }


    fun Activity.playSound(
        soundResId: Int
    ) {
        playSound(soundResId, null, null)
    }

    fun Activity.playSound(
        soundResId: Int,
        beforePlayCallback: OnCompleteCallback?,
        afterPlayCallback: OnCompleteCallback?
    ) {
        /*
            var resourceName: String = getResources().getResourceName(soundResId)
            var resourcePath = resourceName.split("/")
            resourceName = resourcePath[resourcePath.size - 1] + ".mp3";

            var mediaFile: File = File(this.getFilesDir(), resourceName);
        */

        // Get a handler that can be used to post to the main thread
        val mainHandler: Handler = Handler(Looper.getMainLooper())
        mainHandler.post {
            val player: MediaPlayer = MediaPlayer.create(this, soundResId)
            if (beforePlayCallback != null) {
                beforePlayCallback.onComplete(true, null)
            }
            //             player.start();

            if (afterPlayCallback != null) {
                player.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
                    override fun onCompletion(p0: MediaPlayer?) {
                        player.release()
                        afterPlayCallback.onComplete(true, null)
                    }
                })
            }
            player.start()
        }


    }


    /*
        public fun getLocalPath(fileName : String): String?
        {
            val basePath =
                FacebookSdk.getApplicationContext().getDir(
                    "cache",
                    Context.MODE_PRIVATE
                ).toString()

            var filePath = basePath + "/" + fileName
            var cameraPath =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+"/Camera/"+fileName;
            if (FileUtils.fileExists(cameraPath))
            {
                return cameraPath
            }


            return null
        }
    */

    fun getDurationFormated(uri: Uri): String? {
        val duration: Int = getDuration(uri)
        return StringUtils.getDurationString(duration / 1000)
    }


    fun getDuration(uri: Uri): Int {
        var durationStr = "00:00:00"
        try {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(context, uri)
            durationStr =
                mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    .toString()

        } catch (ex: Exception) {
            throw ex
        }
        return durationStr!!.toInt()
    }

    fun getDimentions(uri: Uri): HashMap<String, Int> {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(context, uri)
        val width: Int =
            Integer.valueOf(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
        val height: Int =
            Integer.valueOf(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))

        val map = HashMap<String, Int>()
        map["width"] = width
        map["height"] = height

        return map
    }

    fun convertFileToBase64(_parse: Uri): Any {
        var parse = _parse
        if (!parse.toString().startsWith("file:")) {
            val path = parse.path
            parse = Uri.parse("file:$path")
        }
        val encoded = mediaToBase64(context, parse)
        return encoded!!
    }


    private fun mediaToBase64(context: Context, uri: Uri): String? {
        var sBytes: String? = null
        val fileExtension = getFileExtension(uri.path)
        when (fileExtension.lowercase(Locale.getDefault())) {
            "jpg", "jpeg", "png", "bmp" -> {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                // initialize byte stream
                // initialize byte stream
                val stream = ByteArrayOutputStream()
                // compress Bitmap
                // compress Bitmap
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                // Initialize byte array
                // Initialize byte array
                val bytes: ByteArray = stream.toByteArray()
                // get base64 encoded string
                // get base64 encoded string
                sBytes = Base64.encodeToString(bytes, Base64.DEFAULT)
            }

            "mp4" -> {


                val tempFile = File(
                    uri.toString().replace("file:", "")
                )


                return encodeFile(tempFile)



                var encodedString: String? = null
                var inputStream: InputStream? = null

                try {
                    inputStream = FileInputStream(tempFile)
                } catch (e: java.lang.Exception) {
                    // TODO: handle exception
                }

                val localUri: Uri = Uri.fromFile(File(uri.toString()))
                val bytes: ByteArray
                val buffer = ByteArray(8192)
                var bytesRead: Int
                val output = ByteArrayOutputStream()
                try {
                    while (inputStream!!.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                sBytes = Base64.encodeToString(output.toByteArray(), Base64.DEFAULT)

            }

            "3gp" -> {

                val tempFile = File(
                    uri.toString()
                )

     return encodeFile(tempFile)

                var encodedString: String? = null
                var inputStream: InputStream? = null

                try {
                    inputStream = FileInputStream(tempFile)
                } catch (e: java.lang.Exception) {
                    // TODO: handle exception
                }

                val localUri: Uri = Uri.fromFile(File(uri.toString()))
                val bytes: ByteArray
                val buffer = ByteArray(8192)
                var bytesRead: Int
                val output = ByteArrayOutputStream()
                try {
                    while (inputStream!!.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                sBytes = Base64.encodeToString(output.toByteArray(), Base64.DEFAULT)

            }
        }
        return sBytes
    }


    fun encodeFile(file: File?): String? {
        val encodedString = StringBuilder()

        try {
            val fileInputStream = FileInputStream(file)

            val fileContent = ByteArray(3000)

            while (fileInputStream.read(fileContent) >= 0) {
                encodedString.append(Base64.encodeToString(fileContent, Base64.NO_WRAP))
            }
            fileInputStream.close()
            return encodedString.toString()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return null
        } catch (e: Error) {
            e.printStackTrace()
            return null
        }
    }
}





fun Activity.showTextDialog(
    text: String
) {
    val dialog = TextDialog(this, this)
    dialog.setText(text)
    dialog.show()
}

fun Activity.editTextDialog(
    activity: Activity,
    text: String,
    callback: OnCompleteCallback
) {
    val dialog = TextDialog(activity, activity)
    dialog.setAcceptButtoCallback(callback)
    dialog.setText(text)
    dialog.show()

}


fun AppCompatActivity.showImage(
    imagePath: String
) {
    val dialog = ImageViewerDialog(this, this)
    dialog.setImageUrl(imagePath)
    dialog.show()
}

fun Activity.playVideo(
    videoPath: String
): VideoPlayerDialog? {

    try {
        val dialog = VideoPlayerDialog(this, this)
        dialog.show()
        dialog.setVideoUrl(videoPath)
        return dialog

    } catch (ex: java.lang.Exception) {
        var pp = 2
    }
    return null
}

fun Context.getDuration(uri: Uri): Int {
    return MultimediaUtils(this).getDuration(uri)
}

fun Context.getDimentions(uri: Uri): HashMap<String, Int> {
    return MultimediaUtils(this).getDimentions(uri)
}

/**
 * Prepara un objeto de tipo multimedia
 * @param mediaType Tipo de multimedia
 * @param fileName Nombre del archivo
 * @param localFullPath Ruta local del archivo sin el nombre de archivo.
 */

fun Context.prepareMediaMessage(
    mediaType: MediaTypesEnum, fileName: String, localFullPath: String, subPath: String = ""
): Any {

    if (fileName.compareTo(fileName.getJustFileName()) != 0) {
        throw Exception("El parametro fileName debe contener solo el nombre del archivo")
    }

    try {

        val media = MediaFile(mediaType, localFullPath, fileName.toString())


        if (mediaType == MediaTypesEnum.VIDEO || mediaType == MediaTypesEnum.AUDIO || mediaType == MediaTypesEnum.IMAGE) {
            val fileExtension = media.file_name.getFileExtension(this)
            var fileUri = media.localFullPath
            if (fileExtension?.lowercase(Locale.getDefault()) == "jpg" || fileExtension?.lowercase(
                    Locale.getDefault()
                ) == "png"
            ) {
                fileUri = "file:" + media.localFullPath
            }
            var mediaFileEncoded: String? = null
            if (fileExtension?.lowercase(Locale.getDefault()) == "jpg" || fileExtension?.lowercase(
                    Locale.getDefault()
                ) == "png" || fileExtension?.lowercase(Locale.getDefault()) == "mp4" || fileExtension?.lowercase(
                    Locale.getDefault()
                ) == "3gp"
            ) {


                mediaFileEncoded = MultimediaUtils(this).convertFileToBase64(
                    Uri.parse(
                        fileUri
                    )
                ).toString()

                if (!localFullPath.contains(this.cacheDir.toString() + "/" + AppConstants.CHAT_FILES_STORAGE_PATH +"/"+ subPath.toString())) {
                    var destination =
                        this.cacheDir.toString() + "/" + AppConstants.CHAT_FILES_STORAGE_PATH + subPath.toString()
                    FileUtils().copyFile(
                        media.localFullPath.substringBeforeLast("/"),
                        media.localFullPath.getJustFileName(),
                        destination
                    )
                }
            }
            media.bytesB64 = mediaFileEncoded
        }

        return media
    } catch (ex: Exception) {
        return ex
    }

}

fun BitmapDescriptor.toBase64(): String {
    val byteArrayOutputStream = ByteArrayOutputStream()


    this.toBitmap()?.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

// Método de extensión para convertir BitmapDescriptor a Bitmap
fun BitmapDescriptor.toBitmap(): Bitmap? {
    val drawable: Drawable = BitmapDrawable()
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
