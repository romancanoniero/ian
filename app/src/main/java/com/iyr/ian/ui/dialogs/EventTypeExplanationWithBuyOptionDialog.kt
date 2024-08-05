package com.iyr.ian.ui.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.navigation.fragment.findNavController
import com.iyr.ian.R
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.EventNotificationModel
import com.iyr.ian.enums.EventTypesEnum
import com.iyr.ian.utils.UIUtils.handleTouch




enum class DialogFunctionEnum {
    SUBSCRIPTION_REQUIRED,
    INFORMATION,
    AS_PREVIOUS
}

class EventTypeExplanationDialog(

) : AppCompatDialogFragment() {

    private var mButton1Callback: View.OnClickListener? = null

    //private val mDialoglayout: View
    private var mTitle: String? = null
    private var mLegend: String? = null
    private var mButton1Caption: String? = null
    private val mButton2Caption: String? = null
    //  private   val args : EventTypeExplanationDialogArgs by navArgs()


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
    }

    lateinit var dialogView: View
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        dialogView = inflater.inflate(R.layout.event_type_explanation_popup, container, false)

        buttonOne = dialogView?.findViewById<Button>(R.id.button_one)
        buttonTwo = dialogView?.findViewById<Button>(R.id.button_two)
        legend = dialogView?.findViewById<TextView>(R.id.legend)
        buySuggestionLegend = dialogView?.findViewById<TextView>(R.id.buy_suggestion_text)
        checkboxNoShowAgain = dialogView?.findViewById<Button>(R.id.checkbox_no_show_again)


        var window = this.dialog?.window
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window!!.attributes)
        lp.gravity = Gravity.CENTER
        window!!.attributes = lp
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.setGravity(Gravity.CENTER)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        var eventType = arguments?.getSerializable("eventType")
        var eventAvatar: Int? = null
        when (eventType) {
            EventTypesEnum.SEND_POLICE -> {
                setAvatarRes(R.drawable.poli)
                setFunctionalityDescription(R.string.functionality_description_send_police)
            }

            EventTypesEnum.SEND_FIREMAN -> {
                setAvatarRes(R.drawable.fireman_big)
                setFunctionalityDescription(R.string.functionality_description_send_fireman)
            }

            EventTypesEnum.ROBBER_ALERT -> {
                setAvatarRes(R.drawable.suspicius_big)
                setFunctionalityDescription(R.string.functionality_description_robber_alert)
            }

            EventTypesEnum.PERSECUTION -> {
                setAvatarRes(R.drawable.persecution_big)
                setFunctionalityDescription(R.string.functionality_description_notify_persecution)
            }

            EventTypesEnum.SCORT_ME -> {
                setAvatarRes(R.drawable.scortme_big)
                setFunctionalityDescription(R.string.functionality_description_scort_me)
            }

            EventTypesEnum.SEND_AMBULANCE -> {
                setAvatarRes(R.drawable.ambulance_big)
                setFunctionalityDescription(R.string.functionality_description_send_ambulance)
            }

            EventTypesEnum.KID_LOST -> {
                setAvatarRes(R.drawable.kid_lot_big)
                setFunctionalityDescription(R.string.functionality_description_kid_lost)
            }

            EventTypesEnum.PET_LOST -> {
                setAvatarRes(R.drawable.pet_lost_big)
                setFunctionalityDescription(R.string.functionality_description_pet_lost)
            }

            EventTypesEnum.MECANICAL_AID -> {
                setAvatarRes(R.drawable.mecanical_aid_big)
                setFunctionalityDescription(R.string.functionality_description_mechanical_aid)
            }

            else -> {}
        }

        var dialogType = arguments?.getSerializable("dialogType")
        when (dialogType) {
            DialogFunctionEnum.SUBSCRIPTION_REQUIRED -> {
                checkboxNoShowAgain?.visibility = View.GONE
            }

            DialogFunctionEnum.INFORMATION -> {
                checkboxNoShowAgain?.visibility = View.GONE
                buttonOne?.visibility = View.GONE
                checkboxNoShowAgain?.visibility = View.VISIBLE
            }

            DialogFunctionEnum.AS_PREVIOUS -> {
                buttonOne?.text = requireContext().getString(R.string.lets_continue)
            }
        }

        buttonOne?.setOnClickListener { view ->
/*
            requireContext().handleTouch()
            if (mButton1Callback != null) {
                mButton1Callback!!.onClick(view)
            }
            dismiss()
*/

            var action =
                EventTypeExplanationDialogDirections.actionEventTypeExplanationDialogToPlanSubscriptionFragment()
            findNavController().navigate(action)


        }

        buttonTwo?.setOnClickListener { view ->
            requireContext().handleTouch()
            dismiss()
        }


        if (mTitle != null) {
            val title = dialogView?.findViewById<TextView>(R.id.title)
            title?.text = mTitle
        }
        if (mLegend != null) {
            val legend = dialogView?.findViewById<TextView>(R.id.legend)
            legend?.text = mLegend
        }
        if (mButton1Caption != null) {
            //      val buttonOne = mDialoglayout.findViewById<Button>(R.id.buttonOne)
            buttonOne?.text = mButton1Caption
        }
        if (mButton1Callback != null) {
            //      val buttonOne = mDialoglayout.findViewById<Button>(R.id.buttonOne)
            buttonOne?.setOnClickListener(mButton1Callback)
        }



        return dialogView
    }

    /*
   override fun onAttachedToWindow() {
       super.onAttachedToWindow()
       if (mTitle != null) {
           val title = mDialoglayout.findViewById<TextView>(R.id.title)
           title.text = mTitle
       }
       if (mLegend != null) {
           val legend = mDialoglayout.findViewById<TextView>(R.id.legend)
           legend.text = mLegend
       }
       if (mButton1Caption != null) {
     //      val buttonOne = mDialoglayout.findViewById<Button>(R.id.buttonOne)
           buttonOne?.text = mButton1Caption
       }
       if (mButton1Callback != null) {
     //      val buttonOne = mDialoglayout.findViewById<Button>(R.id.buttonOne)
           buttonOne?.setOnClickListener(mButton1Callback)
       }
   }
*/
    fun setAvatarRes(resId: Int) {


        val avatar = dialogView?.findViewById<ImageView>(R.id.avatar_image)
        avatar?.setImageResource(resId)
    }

    fun setTitle(resId: Int) {
        mTitle = getString(resId)
    }

    fun setTitle(title: String?) {
        mTitle = title
    }

    fun setLegend(resId: Int) {
        mLegend = getString(resId)
    }

    fun setLegend(message: String?) {
        mLegend = message
    }

    fun setButton1Caption(resId: Int) {
        mButton1Caption = getString(resId)
    }

    fun setButton1Caption(text: String?) {
        mButton1Caption = text
    }

    fun setButton1Callback(onClickListener: View.OnClickListener) {
        mButton1Callback = onClickListener
    }

    var buttonOne: Button? = null
    var buttonTwo: Button? = null
    var legend: TextView? = null
    var buySuggestionLegend: TextView? = null
    var checkboxNoShowAgain: Button? = null


    init {
        /*
              val inflater = mActivity.layoutInflater
              mDialoglayout = inflater.inflate(R.layout.event_type_explanation_popup, null)
              this.setView(mDialoglayout)
      */

    }

    private fun setFunctionalityDescription(functionalityDescriptionSendPolice: Int) {
        mLegend = getString(functionalityDescriptionSendPolice)

    }


}