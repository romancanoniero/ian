package com.iyr.ian.dao.models

import java.io.Serializable

class AppModules :  Serializable {


     var resId: Int = 0
     var title: String =""


    constructor(resId: Int, title: String) {
        this.resId = resId
        this.title = title
    }
}