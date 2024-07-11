package com.iyr.ian.utils.sharing_app


enum class SharingTargets {
    EMAIL,
    SMS,
    GENERIC
}


class SharingContents() {

    constructor(method: SharingTargets, address: String?, title: String?) : this() {
        this.sharingMethod = method
        address?.let {
            this.contactAddress = it
        }
        title?.let {
            this.title = it
        }

    }

    var sharingMethod: SharingTargets = SharingTargets.GENERIC
    var contactAddress: String = ""
    var title: String = ""
}