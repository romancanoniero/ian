package com.iyr.ian.utils.smsretriever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status


class SMSRetriever : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
            val extras = intent.extras
            val status = extras?.get(SmsRetriever.EXTRA_STATUS) as Status

            when (status.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    // Obtén el mensaje SMS del intent.
                    val message = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String
                    // Extrae el código de verificación del mensaje SMS.
                    val code = extractVerificationCode(message)
                }
                CommonStatusCodes.TIMEOUT -> {
                    // Se agotó el tiempo de espera para el mensaje SMS.
                }
            }
        }
    }

    private fun extractVerificationCode(message: String): String {
        // Aquí debes implementar la lógica para extraer el código de verificación del mensaje SMS.
        // Esto dependerá del formato de tu mensaje SMS.
        return "pp =33"

    }
}