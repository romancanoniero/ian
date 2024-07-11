package com.iyr.fewtouchs.ui.views.home.fragments.friends.controls

import android.content.Context
import android.util.AttributeSet


class CustomAutoCompleteView : androidx.appcompat.widget.AppCompatAutoCompleteTextView {
    constructor(context: Context) : super(context) {        // TODO Auto-generated constructor stub
    }

    constructor(context: Context, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {        // TODO Auto-generated constructor stub
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {        // TODO Auto-generated constructor stub
    }

    // this is how to disable AutoCompleteTextView filter
    override fun performFiltering(text: CharSequence, keyCode: Int) {
      /*
        val filterText = ""
        super.performFiltering(filterText, keyCode)
   */
    }

    /*
     * after a selection we have to capture the new value and append to the existing text
     */
    override fun replaceText(text: CharSequence) {
        super.replaceText(text)
    }



}