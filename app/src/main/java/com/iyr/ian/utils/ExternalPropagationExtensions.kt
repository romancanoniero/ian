package com.iyr.ian.utils

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.iyr.ian.R
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.sharing_app.SharingContents

class ExternalPropagationExtensions {

//    Constants.DYNAMIC_LINK_ACTION_FRIENDSHIP



    /*
    fun Context.generateInvitationLink() {

    val sharingInfo = SharingContents(SharingTargets.GENERIC, null, null)

    generateInvitationLink(sharingInfo)
}
*/
}


suspend fun Activity.generateInvitationLink(
    action: String,
    stringResId: Int,
    sharingInfo: SharingContents
) {
    val map: java.util.HashMap<String, String> = java.util.HashMap<String, String>()
    map["action"] = action
    map["key"] = FirebaseAuth.getInstance().uid.toString()
    val dinamicLink = UIUtils.createShortDynamicLink(this, map)
    when (dinamicLink) {
        is Resource.Success -> {
            val invitationText = String.format(
                getText(stringResId).toString(),
                getString(R.string.app_name)
            )
            invitationText.plus(System.getProperty("line.separator"))
            invitationText.plus(System.getProperty("line.separator"))
            invitationText.plus(dinamicLink.data)
        }
        is Resource.Error -> {
            showErrorDialog(dinamicLink.message.toString())
        }

        else -> {}
    }
    UIUtils.createShortDynamicLink(this, map, object :
        OnCompleteCallback {

        override fun onComplete(success: Boolean, shortlink: Any?) {

            var invitationText = String.format(
                getText(R.string.app_installation_invitation_message)
                    .toString(),
                getString(R.string.app_name)
            )

            invitationText =
                invitationText.plus(System.getProperty("line.separator"))
            invitationText =
                invitationText.plus(System.getProperty("line.separator"))
            invitationText = invitationText.plus(shortlink)
        }

        override fun onError(exception: Exception) {
            showErrorDialog(exception.localizedMessage)
        }
    }
    )


}
