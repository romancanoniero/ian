package com.iyr.ian.utils

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore

const val TRADEAPP_PICK_IMAGE_FROM_GALLARY = 111
const val TRADEAPP_PICK_VIDEO_FROM_GALLARY = 222
const val TRADEAPP_PICK_AUDIO_FROM_GALLARY = 333
const val TRADEAPP_PICK_FILE__FROM_GALLARY = 444

class MediaFileExtensions

fun Activity.pickImageFromGallery() {
    permissionsReadWriteWithCamera()
    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    intent.type = "image/*"
    intent.action = Intent.ACTION_GET_CONTENT
    startActivityForResult(
        Intent.createChooser(intent, "Select Image"),
        TRADEAPP_PICK_IMAGE_FROM_GALLARY
    )
}
/*
fun Activity.pickImageAndCrop() {
    this.pickImageAndCrop(null)
}
*/
/*
fun Activity.pickImageAndCrop(cropOptions: Int?, callback: ImagePickerCallback? = null) {

    var pickerPath = ""
    if (callback == null) {
        var callback = object : ImagePickerCallback {

            override fun onError(p0: String?) {
                TODO("Not yet implemented")
            }

            override fun onImagesChosen(p0: MutableList<ChosenImage>?) {
                /*
                                UCrop.of(Uri.parse(pickerPath), Uri.parse(pickerPath))
                                    .withAspectRatio(16F, 9F)
                                    .start(this@pickImageAndCrop);
                  */
                TODO("Not yet implemented")
            }


        }
    }


    //val imagePicker = CameraImagePicker(this)
    val imagePicker = ImagePicker(this)
    imagePicker.setRequestId(1234)
    if (callback != null)
        imagePicker.setImagePickerCallback(callback)
    //  imagePicker.shouldGenerateMetadata(true)
    /*
    imagePicker.setCacheLocation(CacheLocation.INTERNAL_APP_DIR)
    val extras = Bundle()
    extras.putInt("android.intent.extras.CAMERA_FACING", 1)
    imagePicker.setExtras(extras)

    imagePicker.setDebugglable(true);
    // imagePicker.shouldGenerateThumbnails(true)

     */

    pickerPath = imagePicker.pickImage().toString()


    //---
    /*
        imagePicker.setFolderName("Random")
        imagePicker.setRequestId(1234)
        imagePicker.ensureMaxSize(500, 500)
        imagePicker.shouldGenerateMetadata(true)

    //    imagePicker.setImagePickerCallback(this)
     */
    /*
        var imagePicker = ImagePicker(this)
        imagePicker.shouldGenerateMetadata(true)
        imagePicker.setCacheLocation(CacheLocation.INTERNAL_APP_DIR)
        imagePicker.setFolderName(cacheDir.toString())

        val extras = Bundle()
        // For capturing Low quality videos; Default is 1: HIGH
        extras.putInt(MediaStore.EXTRA_VIDEO_QUALITY, 0)
        imagePicker.setExtras(extras)
        imagePicker.setImagePickerCallback(callback)
      imagePicker.pickImage()
    */
    /*
        var picker : com.github.dhaval2404.imagepicker.ImagePicker.Builder  = com.github.dhaval2404.imagepicker.ImagePicker.with(this)
        picker.compress(100) // era 512
        picker.maxResultSize(1024, 1024)
        when (cropOptions) {
            CROP_SQUARED -> {
                picker.cropSquare()
            }
            CROP_RECTANGLE_16_X_9 -> {
                picker.crop(16f, 9f)
            }
        }
        picker.start()
    */


}
*/