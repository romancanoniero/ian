package com.iyr.ian.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.iyr.ian.R
import com.iyr.ian.glide.GlideApp
import com.iyr.ian.repository.implementations.databases.realtimedatabase.StorageRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

class ImageExtensions

fun Context.resizeDrawable(image: Drawable, with: Int, height: Int): Drawable? {
    var b: Any? = null
    if (image is BitmapDrawable) {
        val b = image.bitmap
        val bitmapResized = Bitmap.createScaledBitmap(b, with, height, false)
        return BitmapDrawable(resources, bitmapResized)
    } else if (image is VectorDrawable) {
        image.setBounds(0, 0, with, height)
        return image
    }
    return null
}

fun Context.resizeVectorDrawable(image: VectorDrawable, with: Int, height: Int): Drawable {
    val b = (image as BitmapDrawable).bitmap
    val bitmapResized = Bitmap.createScaledBitmap(b, with, height, false)
    return BitmapDrawable(resources, bitmapResized)

}

fun Context.getBitmapFromVectorDrawable(drawableId: Int): Bitmap {
    var drawable = ContextCompat.getDrawable(this, drawableId)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        assert(drawable != null)
        drawable = DrawableCompat.wrap(drawable!!).mutate()
    }
    val bitmap = Bitmap.createBitmap(
        drawable!!.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}


fun Context.loadBitmapFromView(view: View, width: Int, height: Int): Bitmap {
    val dm = this.resources.displayMetrics
    view.measure(
        View.MeasureSpec.makeMeasureSpec(
            width, View.MeasureSpec.EXACTLY
        ), View.MeasureSpec.makeMeasureSpec(
            height, View.MeasureSpec.EXACTLY
        )
    )
    view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    val bitmap = Bitmap.createBitmap(
        view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    view.layout(view.left, view.top, view.right, view.bottom)
    view.draw(canvas)
    return bitmap
}

fun Bitmap.toGrayscale(): Bitmap {/*
      val width: Int
      val height: Int
      height = bmpOriginal.height
      width = bmpOriginal.width
  */
    val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmpGrayscale)
    val paint = Paint()
    val cm = ColorMatrix()
    cm.setSaturation(0f)
    val f = ColorMatrixColorFilter(cm)
    paint.colorFilter = f
    c.drawBitmap(this, 0f, 0f, paint)
    return bmpGrayscale
}


/***
 * Assigns an image to an ImageView from cache of from a file in Firebase Storage
 * @param context The context of the activity or fragment
 * @param fileName The name of the file to be loaded
 * @param folder The folder where the file is stored ( Fullpath)
 * @param destination The ImageView where the image will be assigned
 */
suspend fun Context.assignFileImageTo(
    fileName: String, folder: String, destination: ImageView
) {
    var imageBitmap = loadImageFromCache(
        fileName, folder
    )
    if (imageBitmap != null) {

        if (coroutineContext.isMainContext()) {
            destination.setImageBitmap(imageBitmap)
        } else {
            withContext(Dispatchers.Main) {
                destination.setImageBitmap(imageBitmap)
            }
        }
        return
    } else {
        try {
            val storageReference =   StorageRepositoryImpl().generateStorageReference("$folder/${fileName}")

            //   GlobalScope.launch(Dispatchers.IO) {
            try {
                imageBitmap =
                    GlideApp.with(this@assignFileImageTo).asBitmap().load(storageReference)
                        .placeholder(this@assignFileImageTo.getDrawable(R.drawable.progress_animation))
                        .error(this@assignFileImageTo.getDrawable(R.drawable.ic_error)).submit()
                        .get()

                if (imageBitmap != null) {
                    imageBitmap!!.saveImageToCache(
                        this@assignFileImageTo, fileName, folder
                    )
                    withContext(Dispatchers.Main) {
                        destination.setImageBitmap(imageBitmap)
                        destination.visibility = View.VISIBLE
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        destination.setImageDrawable(
                            this@assignFileImageTo.getDrawable(
                                R.drawable.ic_error
                            )
                        )
                        destination.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    destination.setImageDrawable(this@assignFileImageTo.getDrawable(R.drawable.ic_error))
                    destination.visibility = View.VISIBLE
                }
            }
        } catch (e: Exception) {
            destination.setImageDrawable(this@assignFileImageTo.getDrawable(R.drawable.ic_error))
            destination.visibility = View.VISIBLE
        } catch (e: StorageException) {
            destination.setImageDrawable(this@assignFileImageTo.getDrawable(R.drawable.ic_error))
            destination.visibility = View.VISIBLE
        }
    }
}



///--------------------------------

//-------------------------

suspend fun Context.getFileImage(
    fileName: String, folder: String
): Bitmap {
    var imageBitmap = loadImageFromCache(
        fileName, folder
    )

    if (imageBitmap != null) {
        return imageBitmap
    } else {

        try {
            val storageReference =  StorageRepositoryImpl().generateStorageReference("$folder/${fileName}")
                FirebaseStorage.getInstance().getReference(folder).child(fileName)

            val imageBitmap = GlobalScope.launch(Dispatchers.IO) {
                try {
                    imageBitmap =
                        GlideApp.with(this@getFileImage).asBitmap().load(storageReference)
                            .error(this@getFileImage.getDrawable(R.drawable.ic_error)).submit()
                            .get()

                    if (imageBitmap != null) {
                        imageBitmap!!.saveImageToCache(
                            this@getFileImage, fileName, folder
                        )
                        imageBitmap
                    } else {
                        imageBitmap = getBitmapFromVectorDrawable(R.drawable.ic_error)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        imageBitmap = getBitmapFromVectorDrawable(R.drawable.ic_error)
                    }
                }

            }
        } catch (e: Exception) {
            imageBitmap = getBitmapFromVectorDrawable(R.drawable.ic_error)
        } catch (e: StorageException) {
            imageBitmap = getBitmapFromVectorDrawable(R.drawable.ic_error)
        }
    }
    return imageBitmap!!
}