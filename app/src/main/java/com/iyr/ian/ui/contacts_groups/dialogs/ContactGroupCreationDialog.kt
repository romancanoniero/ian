package com.iyr.ian.ui.contacts_groups.dialogs


import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.MediaController
import androidx.appcompat.app.AlertDialog
import com.iyr.ian.R
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.utils.UIUtils.handleTouch

interface OnCreationGroupCallback {
    fun createGroup(groupName: String)
    fun onCanceled()

}

class ContactGroupCreationDialog(
    val activity: Activity,
    callback: OnCreationGroupCallback
) :
    AlertDialog(
        activity
    ) {
    private lateinit var createButton: Button

    //private var textField: EditText
    private var controller: MediaController? = null
    private val mThisDialog: ContactGroupCreationDialog = this
    private var mButton1Callback: OnCompleteCallback? = null
    private val mDialoglayout: View
    private var mTitle: String? = null
    private lateinit var groupNameEditText: EditText
    private val mButton2Caption: String? = null

    init {
        val inflater = activity.layoutInflater
        mDialoglayout = inflater.inflate(R.layout.fragment_contact_group_creation_popup, null)
        this.setView(mDialoglayout)
        createButton = mDialoglayout.findViewById<Button>(R.id.buttonOne)
        val cancelButton = mDialoglayout.findViewById<Button>(R.id.cancel_button)

        groupNameEditText = mDialoglayout.findViewById(R.id.group_name)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window!!.attributes)
        lp.gravity = Gravity.CENTER
        window!!.attributes = lp
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.setGravity(Gravity.CENTER)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        groupNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                createButton.isEnabled = groupNameEditText.text.length >= 4

            }
        })

        createButton.setOnClickListener { view ->
            context.handleTouch()
            //           if (mButton1Callback != null) {
//                mButton1Callback?.onComplete(true, textField.text)
            callback.createGroup(groupNameEditText.text.toString())
            //           }
            dismiss()
        }
        cancelButton.setOnClickListener { view ->
            context.handleTouch()
            dismiss()
        }

    }

    fun setAcceptButtoCallback(callback: OnCompleteCallback) {
        mButton1Callback = callback
    }

}