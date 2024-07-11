package com.iyr.ian.utils

class InvitationExtensions {
}

/*
fun Context.generateInvitationLink() {
    generateInvitationLink(SharingContents(SharingTargets.GENERIC, null, null))
}

private fun generateInvitationLink(sharingInfo: SharingContents) {
    val map: java.util.HashMap<String, String> = java.util.HashMap<String, String>()
    map["action"] = Constants.DYNAMIC_LINK_ACTION_FRIENDSHIP
    map["key"] = FirebaseAuth.getInstance().uid.toString()
    UIUtils.createShortDynamicLink(requireContext(), map, object :
        OnCompleteCallback {

        override fun onComplete(success: Boolean, shortlink: Any?) {

            var invitationText = String.format(
                requireContext().getText(R.string.app_installation_invitation_message)
                    .toString(),
                requireContext().getString(R.string.app_name)
            )

            invitationText =
                invitationText.plus(System.getProperty("line.separator"))
            invitationText =
                invitationText.plus(System.getProperty("line.separator"))
            invitationText = invitationText.plus(shortlink)

            inviteNewFriend(invitationText, sharingInfo)
        }

        override fun onError(exception: Exception) {
            requireActivity().showErrorDialog(exception.localizedMessage)
        }
    }
    )


}
*/