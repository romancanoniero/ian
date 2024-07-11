package com.iyr.ian.ui.events.fragments.dialogs.network_selection


import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.MediaController
import android.widget.RadioButton
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import com.iyr.ian.R
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.dao.models.NotificationList
import com.iyr.ian.utils.UIUtils.handleTouch

interface OnNetworkListSelection {
    fun onSelected(listKey: String)
    fun onCanceled()

}

class NetworkSelectionDialog(
    mContext: Context,
    val mActivity: Activity,
    callback: OnNetworkListSelection
) :
    AlertDialog(
        mContext
    ) {

    private lateinit var spinnerAdapter: ArrayAdapter<NotificationList>

    /*
      private lateinit var notificationGroupsRef: DatabaseReference
      private var notificationGroupsListener: ChildEventListener = object : ChildEventListener {
          override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
              var newObject = snapshot.getValue(NotificationList::class.java)
              if (spinnerAdapter.getPosition(newObject) == -1) {
                  spinnerAdapter.add(newObject)
                  spinnerAdapter.notifyDataSetChanged()
              }
          }

          override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {


          }

          override fun onChildRemoved(snapshot: DataSnapshot) {
              var newObject = snapshot.getValue(NotificationList::class.java)
              var index = spinnerAdapter.getPosition(newObject)
              if (index != -1) {
                  spinnerAdapter.remove(spinnerAdapter.getItem(index))
              }
          }

          override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
              // TODO("Not yet implemented")
          }

          override fun onCancelled(error: DatabaseError) {
              // TODO("Not yet implemented")
          }
      }
  */
    private var acceptButton: Button?
    private lateinit var spinnerNotificationGroups: Spinner
    private lateinit var rbSelectorAll: RadioButton
    private lateinit var rbSelectorOneGroup: RadioButton
    private var spinnerData: ArrayList<NotificationList> = ArrayList()
    private var controller: MediaController? = null
    private val mThisDialog: NetworkSelectionDialog = this
    private var mButton1Callback: OnCompleteCallback? = null
    private val mDialoglayout: View
    private var mTitle: String? = null
    private var mLegend: String? = null
    private val mButton2Caption: String? = null
    private var notificationListKey: String? = null


    init {
        val inflater = mActivity.layoutInflater
        mDialoglayout = inflater.inflate(R.layout.fragment_network_selection_popup, null)
        this.setView(mDialoglayout)

        rbSelectorAll = mDialoglayout.findViewById<RadioButton>(R.id.radio_button_all_contacts)
        rbSelectorOneGroup =
            mDialoglayout.findViewById<RadioButton>(R.id.radio_button_select_a_group)

        spinnerNotificationGroups = mDialoglayout.findViewById<Spinner>(R.id.groups_spinner)
        acceptButton = mDialoglayout.findViewById<Button>(R.id.buttonOne)

        val cancelButton = mDialoglayout.findViewById<Button>(R.id.cancel_button)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window!!.attributes)
        lp.gravity = Gravity.CENTER
        window!!.attributes = lp
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.setGravity(Gravity.CENTER)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        rbSelectorAll.setOnClickListener { view ->
            rbSelectorOneGroup.isChecked = false
            spinnerNotificationGroups.isEnabled = false
        }

        rbSelectorOneGroup.setOnClickListener { view ->
            rbSelectorAll.isChecked = false
            spinnerNotificationGroups.isEnabled = true

        }

        rbSelectorAll.isChecked = true
        acceptButton!!.setOnClickListener { view ->
            context.handleTouch()

            var groupKey: String? = null
            if (rbSelectorOneGroup.isChecked) {
                var position = spinnerNotificationGroups.selectedItemPosition
                groupKey =
                    (spinnerNotificationGroups.adapter?.getItem(position) as NotificationList).list_key
                groupKey = notificationListKey
            }
            else
            {
                groupKey = "all"
            }
            if (callback != null) {
                callback.onSelected(groupKey!!)
                dismiss()
            }
            dismiss()
        }

        /*
                spinnerNotificationGroups.setOnTouchListener( object : OnTouchListener{
                    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                        rbSelectorOneGroup.isChecked = true
                        rbSelectorAll.isChecked = false
                        return false
                    }
                })
        */
        cancelButton.setOnClickListener { view ->
            context.handleTouch()
            dismiss()
            if (callback != null) {
                callback.onCanceled()
            }
        }

        /*
                mDialoglayout.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View) {
                        subscribeToNotificationLists()

                        //TODO("Not yet implemented")
                    }

                    override fun onViewDetachedFromWindow(v: View) {
                        //TODO("Not yet implemented")
                        unsubscribeNotificationLists()
                    }
                })
          */
    }

    /*
        private fun subscribeToNotificationLists() {
            notificationGroupsRef = FirebaseDatabase.getInstance()
                .getReference(TABLE_USERS_NOTIFICATIONS_GROUPS)
                .child(FirebaseAuth.getInstance().uid.toString())

            notificationGroupsRef.addChildEventListener(notificationGroupsListener)
        }

        private fun unsubscribeNotificationLists() {
            notificationGroupsRef.removeEventListener(notificationGroupsListener)
        }
    */

    fun setAcceptButtoCallback(callback: OnCompleteCallback) {
        mButton1Callback = callback
    }


    fun show(list: ArrayList<NotificationList>) {
        spinnerAdapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, list)
        spinnerNotificationGroups.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                var position = spinnerNotificationGroups.selectedItemPosition
                notificationListKey =
                    (spinnerNotificationGroups.adapter?.getItem(position) as NotificationList).list_key
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
        spinnerNotificationGroups.adapter = spinnerAdapter
        /*
                spinnerAdapter.clear()
                spinnerAdapter.addAll(list)
                spinnerAdapter.notifyDataSetInvalidated()

         */
        show()
    }
}