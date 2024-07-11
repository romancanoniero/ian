package com.iyr.ian.ui.main.dialogs


import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.iyr.ian.AppConstants.Companion.DYNAMIC_LINK_ACTION_FRIENDSHIP_AND_SPEED_DIAL
import com.iyr.ian.Constants

import com.iyr.ian.R
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.databinding.DialogNewUserInvitationByPhoneBinding
import com.iyr.ian.sharedpreferences.SessionForProfile
import com.iyr.ian.utils.SMSUtils
import com.iyr.ian.utils.SMSUtils.sendSMS
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.Validators
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.loaders.LoadingDialogFragment
import com.iyr.ian.utils.playSound
import com.iyr.ian.utils.showAnimatedDialog
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.utils.showSnackBar
import java.util.Locale


/*
interface INewUserInvitationDialog {
    in
}
*/

class NewUserInvitationDialog(private val activity: Activity) :
    AlertDialog(
        activity
    ) {

    private lateinit var viewModel: NewUserInvitationDialogViewModel
    private lateinit var binding: DialogNewUserInvitationByPhoneBinding
    private val lifeCycleOwner: MyLifecycleOwner by lazy { MyLifecycleOwner() }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        activity.playSound(R.raw.plop, null, null)
        binding.phoneNumberLoader.isVisible = false
    }

    init {

        viewModel = NewUserInvitationDialogViewModel(SessionForProfile.getInstance(context).getUserId())

        val inflater = activity.layoutInflater

        binding = DialogNewUserInvitationByPhoneBinding.inflate(inflater)
        this.setView(binding.root)
        this.setCancelable(false)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window!!.attributes)
        lp.gravity = Gravity.CENTER
        window!!.attributes = lp
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.setGravity(Gravity.CENTER)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.name.isEnabled = false

        setupObservers()
        setSendingOff()

        binding.acceptButton.setOnClickListener {
            val me = SessionForProfile.getInstance(activity).getUserProfile()
            with(binding) {
                var name = name.text.toString()
                var phoneNumber = phoneNumber.text.toString()
                viewModel.onAcceptButtonClicked(me, phoneNumber)
            }

            /*
                        if (isDataFilled()) {
                            var name = binding.name.text.toString()
                            var phoneNumber = binding.phoneNumber.text.toString()

                            var callback = object : OnCompleteCallback {
                                override fun onComplete(success: Boolean, result: Any?) {
                                    super.onComplete(success, result)

                                    activity.showAnimatedDialog(
                                        "Atención",
                                        "Avisale a tu nuevo contacto que lo estas invitando y auqe acepte"
                                    )
                                    dismiss()

                                }

                                override fun onError(exception: Exception) {
                                    super.onError(exception)
                                }
                            }

                            sendNewConnectionInvitation(name, phoneNumber, callback)


                        }
            */
        }

        binding.closeButton.setOnClickListener { view ->
            activity.handleTouch()
            dismiss()
        }

        binding.name.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                binding.acceptButton.isEnabled = isDataFilled()
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }
        })

        binding.phoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().length >= 10) {
                    var me = SessionForProfile.getInstance(activity).getUserProfile()
                    if (s.toString()
                            .contains(me.telephone_number?.toString() ?: "******") == false
                    ) {

                        viewModel.searchByPhoneNumber(s.toString())
                    } else {

                        viewModel.setInputStatus(InputStatus.IS_YOUR_NUMBER)
                    }
                }

                binding.acceptButton.isEnabled = isDataFilled()
                /*
                     var callback = object : OnCompleteCallback {
                         override fun onComplete(success: Boolean, result: Any?) {
                             super.onComplete(success, result)

                             var results = result as ArrayList<String>
                             binding.isAlreadyUserMessage.isVisible = !results.isEmpty()

                             Toast.makeText(
                                 context,
                                 "Records that match ${result.size}",
                                 Toast.LENGTH_SHORT
                             ).show()

                         }
                     }
                     ContactsWSClient.instance.getPhoneNumbersThatMatch(s.toString(), 8, callback)
              */
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }
        })

    }

    var loader = LoadingDialogFragment()
    private fun setupObservers() {
        viewModel.inputStatus.observe(lifeCycleOwner) { status ->
            when (status) {
                InputStatus.OK -> {
                    binding.isAlreadyUserMessage.isVisible = false
                    binding.acceptButton.isEnabled = true
                }

                InputStatus.IS_YOUR_NUMBER -> {
                    binding.isAlreadyUserMessage.isVisible = true
                    binding.isAlreadyUserMessage.setTextColor(activity.getColor(R.color.colorPrimary))
                    binding.isAlreadyUserMessage.setText(R.string.cannot_invite_myself)
                }

                InputStatus.USER_ALREADY_REGISTERED -> {
                    binding.isAlreadyUserMessage.isVisible = true
                    binding.isAlreadyUserMessage.setTextColor(activity.getColor(R.color.black))
                    binding.isAlreadyUserMessage.setText(R.string.user_already_registered)

                }

                InputStatus.USER_DOES_NOT_EXIST -> {
                    binding.isAlreadyUserMessage.isVisible = false
                }
            }
        }

        viewModel.invitationActionStatus.observe(lifeCycleOwner) { status ->
            when (status) {
                is Resource.Loading -> {
                    setSendingOn()
                    binding.acceptButton.isEnabled = false
                }

                is Resource.Success -> {
                    setSendingOff()
                    dismiss()
                    dialogSayingCallYourFriend()
                }

                is Resource.Error -> {
                    setSendingOff()
                    activity.showErrorDialog(status.message.toString())
                    binding.acceptButton.isEnabled = true
                }
            }
        }

        viewModel.searchByPhoneNumberStatus.observe(lifeCycleOwner) { status ->

            when (status) {
                is Resource.Loading -> {

                    if (!binding.phoneNumberLoader.isVisible) {
                        binding.phoneNumberLoader.isVisible = true
                    }
//                    binding.isAlreadyUserMessage.isVisible = false

                }

                is Resource.Success -> {
//                        loader.dismiss()

                    //                  binding.isAlreadyUserMessage.isVisible = false
                    binding.phoneNumberLoader.isVisible = false
                    binding.name.isEnabled = status.data?.isEmpty() == true
                    if (!status.data?.isEmpty()!!) {
                        binding.name.setText(status.data.get(0)?.display_name.toString())
                    } else {
                        binding.name.setText("")

                    }
                    /*
                                        Toast.makeText(
                                            context,
                                            "Setup Updated Successfully",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                        */
                    //  onUserAuthenticated(status.data!!)
                }

                is Resource.Error -> {
//                    loader.dismiss()
                    binding.phoneNumberLoader.isVisible = false
                    binding.isAlreadyUserMessage.isVisible = false
                    var errorMessage =
                        when (status.message.toString().lowercase(Locale.getDefault())) {
                            "the password is invalid or the user does not have a password." -> activity.getString(
                                R.string.login_error_invalid_password_or_username
                            )

                            "there is no user record corresponding to this identifier. the user may have been deleted." -> activity.getString(
                                R.string.login_error_user_doest_not_exists
                            )

                            else -> status.message.toString()
                        }

                    //    showErrorDialog(errorMessage)
                }
            }


        }
    }

    private fun dialogSayingCallYourFriend() {
        activity.showAnimatedDialog(
            "Atención",
            "Avisale a tu nuevo contacto que lo estas invitando y auqe acepte"
        )
    }

    private fun sendNewConnectionInvitation(
        name: String,
        phoneNumber: String,
        callback: OnCompleteCallback
    ) {


        var callbackCheckIfExists: OnCompleteCallback = object : OnCompleteCallback {
            override fun onComplete(success: Boolean, result: Any?) {
                var results = result as ArrayList<Any>

                if (results.isEmpty()) {
                    Log.d("CONTACT_CONNECTION", "El usuario no figura en los registros")
                    inviteAndAnnotateNewUser(name, phoneNumber, callback)
                } else {

                    var userKey: String = results[0].toString()
                    Log.d("CONTACT_CONNECTION", "El usuario esta registrado en IAN")

                    activity.showSnackBar(binding.root, "implementar contactSendInvitation")
                    /*
                         ContactsWSClient.instance.contactSendInvitation(
                             userKey,
                             callback
                         )

                     */
                }
            }

        }
        Log.d("CONTACT_CONNECTION", "Voy a verificar si el usuario existe en IAN")


        activity.showSnackBar(binding.root, "Implementar getPhoneNumbersThatMatch")
        //    ContactsWSClient.instance.getPhoneNumbersThatMatch(phoneNumber, 8, callbackCheckIfExists)


    }

    /*
        private fun onTelephoneAddedToPendingList()
        {
            val me = SessionForProfile.getInstance(activity).getUserProfile()
            var userName = ""
            if (me.first_name != null && me.last_name != null) {
                userName = me.first_name + " " + me.last_name

            } else
                if (me.first_name != null) {
                    userName = me.first_name + " " + me.last_name

                } else
                    userName = me.display_name

            var invitationText = String.format(
                context.getText(R.string.app_contact_request_notification_message)
                    .toString(), userName
            )

            invitationText = invitationText.plus(shortlink)

            setSendingOn()

        }
    */
    private fun inviteAndAnnotateNewUser(
        name: String,
        phoneNumber: String,
        callback: OnCompleteCallback
    ) {

        val me = SessionForProfile.getInstance(activity).getUserProfile()

        var shortLinkCallback = object :
            OnCompleteCallback {

            override fun onComplete(success: Boolean, shortlink: Any?) {

                activity.runOnUiThread {

                    Log.d(
                        "CONTACT_CONNECTION",
                        "Genero el contenido de la invitacion  a nivel texto"
                    )
                    val me = SessionForProfile.getInstance(activity).getUserProfile()
                    var userName = ""
                    if (me.first_name != null && me.last_name != null) {
                        userName = me.first_name + " " + me.last_name

                    } else
                        if (me.first_name != null) {
                            userName = me.first_name + " " + me.last_name

                        } else
                            userName = me.display_name

                    var invitationText = String.format(
                        context.getText(R.string.app_contact_request_notification_message)
                            .toString(), userName
                    )

                    invitationText = invitationText.plus(shortlink)

                    setSendingOn()

                    var innerCallback = object : OnCompleteCallback {
                        override fun onComplete(success: Boolean, result: Any?) {
                            setSendingOff()
                            if (activity.sendSMS( phoneNumber, invitationText)) {

                                if (success) {
                                    callback.onComplete(true, null)
                                    dismiss()
                                }
                            }
                        }

                        override fun onError(exception: Exception) {
                            setSendingOff()
                            activity.showErrorDialog(exception.localizedMessage)
                        }
                    }

                    Log.d(
                        "CONTACT_CONNECTION",
                        "Anoto el telefono en la lista de de numeros pendientes"
                    )

activity.showSnackBar(binding.root, "Implementar ContactsWSClient.instance.addToPendingPhonesList")
/*
                    ContactsWSClient.instance.addToPendingPhonesList(
                        me.user_key,
                        phoneNumber,
                        innerCallback
                    )
*/

                }
            }
        }

        Log.d("CONTACT_CONNECTION", "Voy a generar el shortlink")
        SMSUtils.generateShortLink(
            context,
            me,
            DYNAMIC_LINK_ACTION_FRIENDSHIP_AND_SPEED_DIAL,
            shortLinkCallback
        )


    }

    /*
        private fun inviteAndAnnotateNewUser(
            name: String,
            phoneNumber: String
        ) {

            val me = SessionForProfile.getInstance(activity).getUserProfile()

            var shortLinkCallback = object :
                OnCompleteCallback {

                override fun onComplete(success: Boolean, shortlink: Any?) {

                    activity.runOnUiThread {

                        Log.d(
                            "CONTACT_CONNECTION",
                            "Genero el contenido de la invitacion  a nivel texto"
                        )
                        val me = SessionForProfile.getInstance(activity).getUserProfile()
                        var userName = ""
                        if (me.first_name != null && me.last_name != null) {
                            userName = me.first_name + " " + me.last_name

                        } else
                            if (me.first_name != null) {
                                userName = me.first_name + " " + me.last_name

                            } else
                                userName = me.display_name

                        var invitationText = String.format(
                            context.getText(R.string.app_contact_request_notification_message)
                                .toString(), userName
                        )

                        invitationText = invitationText.plus(shortlink)

                        setSendingOn()

                        var innerCallback = object : OnCompleteCallback {
                            override fun onComplete(success: Boolean, result: Any?) {
                                setSendingOff()
                                if (activity.sendSMS(phoneNumber, invitationText)) {

                                    if (success) {
                                        callback.onComplete(true, null)
                                        dismiss()
                                    }
                                }
                            }

                            override fun onError(exception: Exception) {
                                setSendingOff()
                                activity.showErrorDialog(exception.localizedMessage)
                            }
                        }

                        Log.d(
                            "CONTACT_CONNECTION",
                            "Anoto el telefono en la lista de de numeros pendientes"
                        )


                        ContactsWSClient.instance.addToPendingPhonesList(
                            me.user_key,
                            phoneNumber,
                            innerCallback
                        )


                        /*
                                                ContactsWSClient.instance.inviteUserByPhoneNumber(
                                                    name,
                                                    phoneNumber,
                                                    innerCallback
                                                )
                        */
                        //       }
                    }
                }
            }

            Log.d("CONTACT_CONNECTION", "Voy a generar el shortlink")
            lifeCycleOwner.launch(Dispatchers.IO) {}
            SMSUtils.generateShortLink(
                context,
                me,
                Constants.DYNAMIC_LINK_ACTION_FRIENDSHIP_AND_SPEED_DIAL
            )


        }

    */
    private fun setSendingOn() {
        binding.closeButton.visibility = View.GONE

        binding.acceptButton.text = activity.getText(R.string.inviting)
        binding.loader.visibility = View.VISIBLE
    }

    private fun setSendingOff() {
        binding.acceptButton.text = activity.getText(R.string.accept)
        binding.loader.visibility = View.GONE
        binding.closeButton.visibility = View.VISIBLE
    }

    private fun isDataFilled(): Boolean {
        var me = SessionForProfile.getInstance(activity).getUserProfile()
        with(binding) {
            var name = name.text
            var phoneNumber = phoneNumber.text
            var notContinueBecauseIsMyNumber: Boolean =
                me.telephone_number?.toString() ?: "******".contains(phoneNumber.toString()) == true



            return (name.isNotEmpty() && name.length >= 4) &&
                    (phoneNumber.isNotEmpty() &&
                            Validators.isValidPhoneNumber(phoneNumber.toString())
                            && (viewModel.inputStatus.value != InputStatus.IS_YOUR_NUMBER))

        }
    }

    class MyLifecycleOwner : LifecycleOwner {
        private val mLifecycleRegistry: LifecycleRegistry by lazy { LifecycleRegistry(this) }

        init {
            mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        }

        fun stop() {
            mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        }

        fun start() {
            mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        }


        override val lifecycle: Lifecycle
            get() = mLifecycleRegistry
    }
}

