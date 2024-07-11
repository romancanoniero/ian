package com.iyr.ian.ui.contacts_groups.multi_select_spinner.model



class KeyPairBoolData {
    /**
     * @return the id
     */
    /**
     * @param id the id to set
     */
    var id: Long = 0
    /**
     * @return the name
     */
    /**
     * @param name the name to set
     */
    var name: String? = null
    /**
     * @return the isSelected
     */
    /**
     * @param isSelected the isSelected to set
     */
    var isSelected = false

    var `theObject`: Any? = null

    constructor()
    constructor(name: String?, isSelected: Boolean) {
        this.name = name
        this.isSelected = isSelected
    }
}