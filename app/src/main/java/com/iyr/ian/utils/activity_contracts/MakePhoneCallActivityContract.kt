package com.iyr.ian.utils.activity_contracts

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.SyncStateContract.Columns.DATA
import androidx.activity.result.contract.ActivityResultContract


class MakePhoneCallActivityContract : ActivityResultContract<String, String?>() {

    override fun createIntent(context: Context, phoneNumber: String): Intent {
        //val bundle = Bundle()
        //bundle.putString(key, input)
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$phoneNumber")
        //intent.putExtra(key, input)
        //intent.putExtra(bundleKey, bundle)
        return intent
    }


    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        return when (resultCode) {
            // Process the data received from second activity.
            RESULT_OK -> intent?.getStringExtra(DATA)
            else -> null
        }
    }
}