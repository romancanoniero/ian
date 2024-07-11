package com.iyr.ian.ui.interfaces

import com.iyr.ian.dao.models.User


interface IProfileSettingsFragment {
    fun onSaveDone(user: User)
    fun onError(exception: Exception)


}