package com.iyr.ian.utils.components.multi_select_spinner

import com.iyr.ian.ui.contacts_groups.multi_select_spinner.model.KeyPairBoolData

interface MultiSpinnerListener {
    fun onItemsSelected(selectedItems: List<KeyPairBoolData?>?)
}