package com.iyr.ian.ui.friends

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import com.iyr.ian.app.AppClass
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.dao.models.UserMinimum
import com.iyr.ian.utils.showSnackBar

interface CustomAutoCompleteChangedListener {
    fun onDataChanged(data: ArrayList<UserMinimum>)

}

class CustomAutoCompleteTextChangedListener(context: Context,val  callback: CustomAutoCompleteChangedListener) : TextWatcher {
    var context: Context
    override fun afterTextChanged(s: Editable) {
        // TODO Auto-generated method stub
    }

    override fun beforeTextChanged(
        s: CharSequence, start: Int, count: Int,
        after: Int
    ) {
        // TODO Auto-generated method stub
    }

    override fun onTextChanged(userInput: CharSequence, start: Int, before: Int, count: Int) {
        if (userInput.length >= 5) {
            // if you want to see in the logcat what the user types
            Log.e(TAG, "User input: $userInput")

Toast.makeText(context, "Implementar UsersWSClient.instance.searchUsers", Toast.LENGTH_LONG).show()
            /*
            UsersWSClient.instance.searchUsers(userInput.toString(), object : OnCompleteCallback {
                override fun onComplete(success: Boolean, result: Any?) {

                    if (callback is CustomAutoCompleteChangedListener) {
                        callback.onDataChanged(result as ArrayList<UserMinimum>)
                    }
                }
            })
*/
        }

    }

    companion object {
        const val TAG = "CustomAutoCompleteTextChangedListener.java"
    }

    init {
        this.context = context
    }
}