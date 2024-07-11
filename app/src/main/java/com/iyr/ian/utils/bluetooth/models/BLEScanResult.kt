package com.iyr.ian.utils.bluetooth.models

class BLEScanResult {

    var id: String = ""
    var name: String?
    var rssi: Int?

    constructor(id: String, name: String, rssi: Int) {
        this.id = id
        this.name = name
        this.rssi = rssi
    }
}
