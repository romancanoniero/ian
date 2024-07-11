package com.iyr.ian.utils.models

class ViewAttributes() {

    var x: Int = -1
    var y: Int = -1
    var width: Int = -1
    var height: Int = -1

    constructor(x: Int, y: Int, width: Int, height: Int) : this() {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
    }
}