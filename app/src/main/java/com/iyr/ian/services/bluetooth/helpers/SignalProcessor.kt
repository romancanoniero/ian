package com.iyr.ian.services.bluetooth.helpers


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.iyr.ian.services.bluetooth.interfaces.Processor


class SignalProcessor(private val context: Context) : Processor {
 //   private val sms: Sms
 //   private val contactManager: ContactManager

    override fun process(signal: String?) {
        Log.i(TAG, signal!!)
        severity = signal
  /*
       val message = if (LocationUpdater.getAddress() == null) null else """$severityMessage${
            LocationUpdater.getAddress().toString()
        }
$MESSAGE_FOOTER ${LocationUpdater.getMapsURL()}"""
  */
  /*
        when (severity) {
            SIGNAL_1 -> {
                if (contactManager.hasFavoriteContact()) {
                    sms.sendSMSs(contactManager.getFavoriteContacts(), message)
                } else {
                    sms.sendSMSs(contactManager.getContacts(), message)
                }
                updateLocation()
            }
            SIGNAL_2 -> {
                sms.sendSMSs(contactManager.getContacts(), message)
                updateLocation()
            }
            SIGNAL_3 -> {
                sms.sendSMSs(contactManager.getContacts(), message)
                emergencyCall()
                updateLocation()
            }
        }
   */

    }


    private fun emergencyCall() {
        Log.i(TAG, "emergency call activated")
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$EMERGENCY_NUMBER")
        context.startActivity(intent)
    }
/*
    private fun updateLocation() {
        val url =
            BLUEMIX_URL + BLUEMIX_PARAM_1 + LocationUpdater.getLatitude() + BLUEMIX_PARAM_2 + LocationUpdater.getLongitude() + BLUEMIX_PARAM_3 + severity
        Log.i(TAG, "bluemix url: $url")
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response -> Log.i(TAG, "response: $response") }
        ) { error ->
            Log.e(
                TAG,
                "error: " + error.message
            )
        }
        Singleton.getInstance(context).addToRequestQueue(jsonObjectRequest)
    }
*/
    companion object {
        private const val TAG = "PROCESSOR"
        private const val EMERGENCY_NUMBER = "6692219715"
        private const val MESSAGE_FOOTER = "\nSent from Panic Button application"
        private const val SIGNAL_1 = "1"
        private const val SIGNAL_2 = "2"
        private const val SIGNAL_3 = "3"
        private const val SEVERITY_MESSAGE_SIGNAL_1 = "INFO\n"
        private const val SEVERITY_MESSAGE_SIGNAL_2 = "WARNING!\n"
        private const val SEVERITY_MESSAGE_SIGNAL_3 = "DANGER!!\n"
        private const val BLUEMIX_URL = "https://nrupatest1.mybluemix.net/log?"
        private const val BLUEMIX_PARAM_1 = "lat="
        private const val BLUEMIX_PARAM_2 = "&long="
        private const val BLUEMIX_PARAM_3 = "&sev="
        private var severity: String? = null
        val severityMessage: String
            get() = when (severity) {
                SIGNAL_1 -> {
                    SEVERITY_MESSAGE_SIGNAL_1
                }
                SIGNAL_2 -> {
                    SEVERITY_MESSAGE_SIGNAL_2
                }
                SIGNAL_3 -> {
                    SEVERITY_MESSAGE_SIGNAL_3
                }
                else -> {
                    SEVERITY_MESSAGE_SIGNAL_1
                }
            }
    }

    init {
        /*
            sms = Sms(context)
            contactManager = ContactManager(context)
        */
    }
}