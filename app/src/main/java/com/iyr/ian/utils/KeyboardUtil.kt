package com.iyr.ian.utils

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment

/*
 * Copyright 2015 Mike Penz All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



/**
 * Created by mikepenz on 14.03.15.
 * This class implements a hack to change the layout padding on bottom if the keyboard is shown
 * to allow long lists with editTextViews
 * Basic idea for this solution found here: http://stackoverflow.com/a/9108219/325479
 */
class KeyboardUtil(val act: Activity, private val contentView: View) {
    private var decorView: View = act.window.decorView


    fun enable() {
        if (Build.VERSION.SDK_INT >= 19) {
            decorView.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
        }
    }

    fun disable() {
        if (Build.VERSION.SDK_INT >= 19) {
            decorView.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayoutListener)
        }
    }

    //a small helper to allow showing the editText focus
    var onGlobalLayoutListener = OnGlobalLayoutListener {
        val r = Rect()
        //r will be populated with the coordinates of your view that area still visible.

        decorView.getWindowVisibleDisplayFrame(r)

        //get screen height and calculate the difference with the useable area from the r
        val height = decorView.context.resources.displayMetrics.heightPixels
        val diff = height - r.bottom

        //if it could be a keyboard add the padding to the view
        if (diff != 0) {
            // if the use-able screen height differs from the total screen height we assume that it shows a keyboard now
            //check if the padding is 0 (if yes set the padding for the keyboard)
            if (contentView.paddingBottom != diff) {
                //set the padding of the contentView for the keyboard
                contentView.setPadding(0, 0, 0, diff)
            }
        } else {
            //check if the padding is != 0 (if yes reset the padding)
            if (contentView.paddingBottom != 0) {
                //reset the padding of the contentView
                contentView.setPadding(0, 0, 0, 0)
            }
        }
    }

    init {
//        decorView = act.window.decorView

        //only required on newer android versions. it was working on API level 19
        if (Build.VERSION.SDK_INT >= 19) {
            decorView.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
        }
    }

    companion object {
        /**
         * Helper to hide the keyboard
         *
         * @param act
         */
        fun hideKeyboard(act: Activity?) {
            if (act != null && act.currentFocus != null) {
                val inputMethodManager =
                    act.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(act.currentFocus!!.windowToken, 0)
            }
        }
    }
}


fun Context.showKeyboard(view: View) {
    if (view.requestFocus()) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}


fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}
