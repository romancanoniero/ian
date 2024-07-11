package com.iyr.ian.core

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.iyr.fewtouchs.utils.*
import com.iyr.ian.AppConstants
import com.iyr.ian.AppConstants.Companion.BROADCAST_ACTION_REFRESH_PANIC_BUTTON
import com.iyr.ian.AppConstants.Companion.BROADCAST_ACTION_REQUEST_PIN
import com.iyr.ian.AppConstants.ServiceCode.BROADCAST_ACTION_GO_TO_MAIN_ACTIVITY
import com.iyr.ian.AppConstants.ServiceCode.BROADCAST_AFTER_EVENT_CLOSE
import com.iyr.ian.AppConstants.ServiceCode.BROADCAST_EVENT_CLOSE_EVENT
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.dao.models.Subscription
import com.iyr.ian.dao.models.User
import com.iyr.ian.repository.implementations.databases.realtimedatabase.EventRepositoryImpl
import com.iyr.ian.sharedpreferences.SessionApp
import com.iyr.ian.sharedpreferences.SessionForProfile
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.ui.base.PulseRequestTarget
import com.iyr.ian.ui.base.PulseValidationCallback
import com.iyr.ian.ui.base.PulseValidatorDialog
import com.iyr.ian.ui.base.ValidationPulsePayload
import com.iyr.ian.utils.broadcastMessage
import com.iyr.ian.utils.loaders.hideLoader
import com.iyr.ian.utils.loaders.showLoader
import com.iyr.ian.utils.showAnimatedDialog
import com.iyr.ian.utils.showErrorDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat


class Core(val context: Context) {


    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val action = intent.action
            when (action) {
                BROADCAST_ACTION_GO_TO_MAIN_ACTIVITY -> {

                    val nextIntent = Intent(context, MainActivity::class.java)
                    nextIntent.action = intent.action
                    intent.extras?.let { info ->
                        nextIntent.putExtras(info)
                    }
                    nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    Log.d("INGRESO", "Va a llamar a MainActity")
                    context.startActivity(nextIntent)
                }
                BROADCAST_AFTER_EVENT_CLOSE -> {
                    val dataAsJson = intent.getStringExtra("data")
                    val eventKey = Gson().fromJson(dataAsJson, String::class.java)
                    //   mainActivity.onEventCloseDone(it.event_key)
                    context.broadcastMessage(eventKey, BROADCAST_AFTER_EVENT_CLOSE)
                }

                AppConstants.ServiceCode.BROADCAST_ACTION_REQUEST_PIN -> {

                    var payloadAsJson = intent.getStringExtra("data")
                    var payload = Gson().fromJson<ValidationPulsePayload>(
                        payloadAsJson, ValidationPulsePayload::class.java
                    )


                    var validationCallback: PulseValidationCallback? = null
                    when (payload.validationType) {
                        PulseRequestTarget.VALIDATION_BEFORE_CLOSE_EVENT -> {
                            // CUANDO SE QUIERE CANCELAR UN EVENTO
                            validationCallback = object : PulseValidationCallback {

                                override fun onWrongCode(
                                    dialog: PulseValidatorDialog, securityPIN: String
                                ) {
                                    super.onWrongCode(dialog, securityPIN)

                                    if (!AppClass.instance.isFreeUser()) {
                                        AppClass.instance.getCurrentActivity()?.showErrorDialog(
                                            context.getString(R.string.error_wrong_security_code),
                                            context.getString(R.string.error_wrong_security_code_message),
                                            context.getString(R.string.close),
                                            null
                                        )
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Codigo Incorrecto. Hay que Notificar a todos",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }


                                override fun onValidationOK(
                                    dialog: PulseValidatorDialog, code: String
                                ) {

                                    if (AppClass.instance.isFreeUser() == false) {
                                        AppClass.instance.getCurrentActivity()
                                            ?.showLoader(context.getString(R.string.closing_event_wait))



                                        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {


                                            val eventRepository =
                                                EventRepositoryImpl()

                                            var call = eventRepository.closeEvent(
                                                payload.key,
                                                FirebaseAuth.getInstance().uid.toString(),
                                                code
                                            )

                                            if (call.message == null) {
                                                AppClass.instance.getCurrentActivity()?.runOnUiThread {
                                                    AppClass.instance.getCurrentActivity()?.hideLoader()
                                                    context.broadcastMessage(
                                                        payload.key, BROADCAST_EVENT_CLOSE_EVENT
                                                    )

                                                    AppClass.instance.getCurrentActivity()
                                                        ?.showAnimatedDialog(
                                                            context.getString(R.string.closing_event_title),
                                                            context.getString(R.string.event_sucessfully_close)
                                                        )
                                                }
                                            } else {
                                                AppClass.instance.getCurrentActivity()?.runOnUiThread {

                                                    AppClass.instance.getCurrentActivity()?.hideLoader()
                                                    AppClass.instance.getCurrentActivity()
                                                        ?.showErrorDialog(call.message.toString())
                                                }
                                            }
                                        }

                                    /*
                                        hacerlo como rutina suspend
                                                EventsWSClient.instance.closeEvent(
                                                    AppClass.instance.getPanicEventKey()!!,
                                                    FirebaseAuth.getInstance().uid.toString(),
                                                    code,
                                                    object : OnCompleteCallback {
                                                        override fun onComplete(
                                                            success: Boolean,
                                                            result: Any?
                                                        ) {
                                                            context.getCurrentActivity()
                                                                ?.hideLoader()
                                                            AppClass.instance.getPanicEvent()
                                                                ?.let {
                                                                    //                     mainActivity.onEventCloseDone(it.event_key)
                                                                    context.broadcastMessage(
                                                                        it.event_key,
                                                                        BROADCAST_EVENT_CLOSE_EVENT
                                                                    )
                                                                }

                                                            context.getCurrentActivity()
                                                                ?.showAnimatedDialog(
                                                                    context.getString(R.string.closing_event_title),
                                                                    context.getString(R.string.event_sucessfully_close)
                                                                )

                                                        }
                                                    }
                                                )

*/
                                    }

                                    /*
                                                                      when (context.versionLevel()) {
                                                                          VersionsEnum.DISCONNECTED -> {

                                                                          }
                                                                          else -> {

                                                                              context.getCurrentActivity()
                                                                                  ?.showLoader(context.getString(R.string.closing_event_wait))

                                                                              EventsWSClient.instance.closeEvent(
                                                                                  AppClass.instance.getPanicEventKey()!!,
                                                                                  FirebaseAuth.getInstance().uid.toString(),
                                                                                  code,
                                                                                  object : OnCompleteCallback {
                                                                                      override fun onComplete(
                                                                                          success: Boolean,
                                                                                          result: Any?
                                                                                      ) {
                                                                                          context.getCurrentActivity()?.hideLoader()
                                                                                          AppClass.instance.getPanicEvent()
                                                                                              ?.let {
                                                                                                  //                     mainActivity.onEventCloseDone(it.event_key)
                                                                                                  context.broadcastMessage(
                                                                                                      it.event_key,
                                                                                                      BROADCAST_EVENT_CLOSE_EVENT
                                                                                                  )
                                                                                              }

                                                                                          context.getCurrentActivity()
                                                                                              ?.showAnimatedDialog(
                                                                                                  context.getString(R.string.closing_event_title),
                                                                                                  context.getString(R.string.event_sucessfully_close)
                                                                                              )

                                                                                      }
                                                                                  }
                                                                              )


                                                                          }
                                                                      }
                                  */
                                    SessionApp.getInstance(context).isInPanic(false)
                                    context.broadcastMessage(
                                        null, BROADCAST_ACTION_REFRESH_PANIC_BUTTON
                                    )

                                }
                            }

                        }

                        PulseRequestTarget.PULSE_VALIDATION -> TODO()
                        PulseRequestTarget.VALIDATE_USER -> TODO()
                        PulseRequestTarget.ON_FALLING_VALIDATION -> TODO()
                    }



                    /*
                    TODO : ARREGLAR ESTO
                    AppClass.instance.getCurrentActivity()?.requestStatusConfirmation(
                        PulseRequestTarget.VALIDATION_BEFORE_CLOSE_EVENT, validationCallback
                    )
*/

                }

            }


        }
    }


    init {
    }

    fun registerReceivers() {

        Log.d("booty", "core registers registered")
        registerReceiver()
    }

    fun unregisterReceivers() {
        unregisterReceiver()
    }


    @SuppressLint("MissingPermission")
    private fun registerReceiver() {

        val intentFilter = IntentFilter()
        intentFilter.addAction(BROADCAST_ACTION_GO_TO_MAIN_ACTIVITY)
        intentFilter.addAction(BROADCAST_ACTION_REQUEST_PIN)
        LocalBroadcastManager.getInstance(context).registerReceiver(
            broadcastReceiver, intentFilter
        )

    }


    private fun unregisterReceiver() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(
            broadcastReceiver
        )
    }


    fun onOkToMainScreen(user: User, extras: Bundle? = null) {

        Log.d("INGRESO", "onOkToMainScreen - 1")


        SessionForProfile.getInstance(context).storeUserProfile(user)
        // TODO SACAR ESTO y TOMARLO DESDE EL SERVIDOR


        // TODO -> arreglar esto del lado del servidor
        val auxSubscription = Subscription()
        auxSubscription.subscription_type_key = user.subscriptionTypeKey
        val dateSubscription = SimpleDateFormat("dd-mm-yyyy").parse("01-03-2022").time
        auxSubscription.subscripted_on = dateSubscription
        val dateExpiration = SimpleDateFormat("dd-mm-yyyy").parse("30-06-2022").time
        auxSubscription.expires_on = dateExpiration
      // TODO: Arregglar esto ->  AppClass.instance.setCurrentSubscription(auxSubscription)

        val callback = object : OnCompleteCallback {
            override fun onComplete(success: Boolean, result: Any?) {
                if (success) {
                    //        if (permissionsLocation() == true) {
                    Log.d("INGRESO", "onOkToMainScreen - 3")

                    context.broadcastMessage(
                        extras, BROADCAST_ACTION_GO_TO_MAIN_ACTIVITY
                    )
                    //      }
                }
            }
        }

        Log.d("INGRESO", "onOkToMainScreen - 2")

        AppClass.instance.fetchCurrentSubscriptionType(callback)

//--------------------------------------------------


    }

}