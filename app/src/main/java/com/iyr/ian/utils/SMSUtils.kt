package com.iyr.ian.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.dao.models.User
import com.iyr.ian.utils.coroutines.Resource


object SMSUtils {


    fun sendLocationSMS(contactName: String, phoneNumber: String?, currentLocation: Location) {
        val smsManager: SmsManager = SmsManager.getDefault()
        val smsBody = StringBuffer()

        try {
            var message =
                "%1s ha pulsado su boton de Panico. Si quieres ver donde se encuentra, pulsa en el Link."
            smsBody.append(String.format(message, contactName))
            smsBody.append("\\n")
            smsBody.append(" http://maps.google.com?q=")
            smsBody.append(currentLocation.latitude)
            smsBody.append(",")
            smsBody.append(currentLocation.longitude)
            smsManager.sendTextMessage(phoneNumber, null, smsBody.toString(), null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

        fun generateShortLink(
            context: Context, user: User, action: String, callback: OnCompleteCallback
        ) {
            val map: HashMap<String, String> = HashMap()
            map["action"] = action
            map["key"] = FirebaseAuth.getInstance().uid.toString()
            UIUtils.createShortDynamicLink(context, map, callback)
        }


        suspend fun generateShortLink(
            context: Context, user: User, action: String
        ): Resource<String?> {
            val map: HashMap<String, String> = HashMap()
            map["action"] = action
            map["key"] = FirebaseAuth.getInstance().uid.toString()
            return UIUtils.createShortDynamicLink(context, map)
        }


        fun Context.sendSMS(phoneNo: String?, msg: String?): Boolean {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.SEND_SMS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    var smsManager: SmsManager? = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        smsManager =
                            applicationContext.getSystemService<SmsManager>(SmsManager::class.java)
                    } else {
                        smsManager = SmsManager.getDefault()
                    }
                    smsManager?.sendTextMessage(phoneNo, null, msg, null, null)
                    return true
                } catch (ex: java.lang.Exception) {
                    ex.printStackTrace()
                    return false
                }

            }
            return false

        }


    }




