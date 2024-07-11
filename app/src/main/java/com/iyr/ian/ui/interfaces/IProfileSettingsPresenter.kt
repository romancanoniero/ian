package com.iyr.ian.ui.interfaces

import com.iyr.ian.dao.models.User


interface IProfileSettingsPresenter {
    fun onSaveDone(userReturned: User)
    fun onError(exception: Exception)


}