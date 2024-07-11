package com.iyr.ian.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.iyr.ian.Constants
import com.iyr.ian.Constants.Companion.MY_PERMISSION_REQUEST_READ_CONTACTS
import com.iyr.ian.Constants.Companion.PHONE_CALL_REQUEST_CODE
import com.iyr.ian.Constants.Companion.SMS_PERMISSION_REQUEST_CODE
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.utils.permissions.PermissionsRationaleDialog


interface LocationRequirementsCallback {
    fun onRequirementsComplete()
}



fun Activity.requestPermissionsLocation() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        requestPermissionsLocationQ(this)
    } else {
        requestPermissionsLocation(this)

    }
}

private fun requestPermissionsLocation(activity: Activity): Boolean {
    if (ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            Constants.LOCATION_REQUEST_CODE
        )
        return false
    }
    return true
}

fun Context.requestPermissionsLocation(): Boolean {
    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        if (AppClass.instance.getCurrentActivity() != null) {
            ActivityCompat.requestPermissions(
                AppClass.instance.getCurrentActivity()!!,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                Constants.LOCATION_REQUEST_CODE
            )
        }
        return false
    }
    return true
}


private fun requestPermissionsLocationQ(activity: Activity) {
    if (ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        ||
        ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) != PackageManager.PERMISSION_GRANTED

    ) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ),
            Constants.LOCATION_REQUEST_CODE
        )
        return
    }
}

fun Activity.showPermissionsRationaleDialogQ() {
    PermissionsRationaleDialog(
        this, this, R.string.rationale_pemission_location, arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ),
        Constants.LOCATION_REQUEST_CODE
    )
}


fun Context.haveBackgroundLocationPermissions(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    } else {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
}

fun Context.arePermissionsGranted(requireAll: Boolean = true, vararg permissions: String): Boolean {
    val permissionChecker = { permission: String ->
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }
    return if (requireAll) permissions.all(permissionChecker) else permissions.any(permissionChecker)
}

fun Context.areLocationPermissionsGranted(requireBackground: Boolean = false): Boolean {
    val foregroundPermissionsGranted = arePermissionsGranted(
        requireAll = false,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    val backgroundPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arePermissionsGranted(
            requireAll = true,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    } else {
        true
    }
    return if (!requireBackground) foregroundPermissionsGranted else foregroundPermissionsGranted && backgroundPermissionGranted
}


fun Activity.permissionsReadWrite(): Boolean {
    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            1
        )
        return false
    }
    return true
}

fun Activity.permissionsForImages(): Boolean {

    val version = Build.VERSION.SDK_INT
    if (Build.VERSION.SDK_INT <= 32) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
           return true
        }
/*
       else
       {
           ActivityCompat.requestPermissions(
               this,
               arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
       }
*/
    } else {
        val isAllowPermissionApi33 = Environment.isExternalStorageManager()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED)
        {
            return true
        }
  /*
        else
        {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES), 1)
        }

   */
    }
    return false
}

fun Activity.permissionsForVideo(): Boolean {

    val version = Build.VERSION.SDK_INT
    if (Build.VERSION.SDK_INT <= 32) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED  )
        {
            return true
        }
    } else {
        val isAllowPermissionApi33 = Environment.isExternalStorageManager()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED)
        {
            return true
        }
    }
    return false
}


fun Activity.makeAPhoneCall(phoneNumber: String) {
    if (permissionsForVoiceCall()) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$phoneNumber")
        startActivityForResult(intent, PHONE_CALL_REQUEST_CODE)
    }
}


fun Activity.permissionsForVoiceCall(): Boolean {
    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CALL_PHONE
        ) != PackageManager.PERMISSION_GRANTED )
    {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.CALL_PHONE
            ),

            1
        )
        return false
    }
    return true
}


fun Context.permissionsVibration(): Boolean {

    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.VIBRATE
    ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
}

fun Activity.permissionsReadContacts(): Boolean {
    return if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_CONTACTS
            ),
            MY_PERMISSION_REQUEST_READ_CONTACTS
        )
        false
    } else
        true

}

fun Activity.permissionsReadWriteWithCamera(): Boolean {
    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.MODIFY_AUDIO_SETTINGS
            ), 1

        )
        return false

    } else {
        return true
    }
}


fun Activity.permissionsForSMSSend(): Boolean {
    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.SEND_SMS
        ) != PackageManager.PERMISSION_GRANTED ) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.SEND_SMS
            ), SMS_PERMISSION_REQUEST_CODE

        )
        return false

    } else {
        return true
    }
}
