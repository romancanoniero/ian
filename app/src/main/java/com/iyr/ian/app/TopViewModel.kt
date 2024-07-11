package com.iyr.ian.app

import com.iyr.ian.enums.IANModulesEnum

/*
* Clase de uso interno para indicar la vista visible en este momento
 */
class TopViewModel {
    constructor(eventsTracking: IANModulesEnum, view: Any, eventKey: String?)
    {
        this.module = eventsTracking
        this.view = view
        this.primaryKey = eventKey
    }

    var module : IANModulesEnum? = null
    var view : Any? = null
    var primaryKey : String? = null
}