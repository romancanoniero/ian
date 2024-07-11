package com.iyr.ian.utils.permissions

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.iyr.ian.R
import com.iyr.ian.ui.MainActivity


interface RationaleDialogCallback {

    fun onTryAgain()
    fun onDeny()


}


class PermissionsRationaleDialog(private var mContext: Context, private var mActivity: Activity) :
    AlertDialog(
        mContext
    ) {
    private var mRequestCode = 0
    private lateinit var mPermissionsRequired: Array<String>
    private var mResMessageId = 0
    private val mDialoglayout: View
    private val mOnOpenSettingsButtonCallback: View.OnClickListener? = null


    constructor(
        context: Context,
        activity: Activity,
        resMessageId: Int,
        permissions: Array<String>,
        requestCode: Int
    ) : this(
        context,
        activity,
        resMessageId,
        permissions,
        null,
        requestCode
    )


    constructor(
        context: Context,
        activity: Activity,
        resMessageId: Int,
        permissions: Array<String>,
        callback: RationaleDialogCallback?,
        requestCode: Int
    ) : this(context, activity) {
        //super(context);
        mActivity = activity
        mContext = context
        mResMessageId = resMessageId
        mPermissionsRequired = permissions
        mRequestCode = requestCode
        val rationaleExplanation = mDialoglayout.findViewById<TextView>(R.id.permissionExplanation)
        val tryAgainButton = mDialoglayout.findViewById<Button>(R.id.tryAgainButton)
        val cancelButton = mDialoglayout.findViewById<Button>(R.id.cancelButton)
        rationaleExplanation.setText(mResMessageId)
        tryAgainButton.setOnClickListener {
            dismiss()
/*            mActivity.requestPermissions(
                mPermissionsRequired,
                mRequestCode
            )*/
            (mActivity as MainActivity).requestPermissions()
        }


        cancelButton.setOnClickListener {
            dismiss()
            callback?.onDeny()
            //                activity.finish();
        }
    }

    init {
        val inflater = mActivity.layoutInflater
        mDialoglayout = inflater.inflate(R.layout.permission_rationale_popup, null)
        this.setView(mDialoglayout)
        val tryAgainButton = mDialoglayout.findViewById<Button>(R.id.tryAgainButton)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window!!.attributes)
        lp.gravity = Gravity.CENTER
        window!!.attributes = lp
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.setGravity(Gravity.CENTER)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        tryAgainButton.setOnClickListener { dismiss() }
    }
}