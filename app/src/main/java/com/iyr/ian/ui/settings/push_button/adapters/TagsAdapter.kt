package com.iyr.ian.ui.settings.push_button.adapters

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.iyr.ian.BuildConfig
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.itag.ITag
import com.iyr.ian.itag.ITagInterface
import com.iyr.ian.itag.ITagsStoreInterface
import com.iyr.ian.itag.Notifications
import com.iyr.ian.itag.TagColor
import com.iyr.ian.utils.bluetooth.ble.AlertVolume
import com.iyr.ian.utils.bluetooth.ble.BLEConnectionInterface
import com.iyr.ian.utils.bluetooth.ble.BLEConnectionState
import com.iyr.ian.utils.bluetooth.ble.BLEState
import com.iyr.ian.utils.bluetooth.ble.rasat.java.DisposableBag
import com.iyr.ian.utils.bluetooth.views.RssiView
import com.iyr.ian.utils.multimedia.MediaPlayerUtils

class TagsAdapter(
    val activity: Activity, val dataSet: ITagsStoreInterface
) : RecyclerView.Adapter<TagsAdapter.ViewHolder>() {

    private val viewBinderHelper = ViewBinderHelper()
    var currentPosition: Int = -1
    val disposableBag = DisposableBag()
    private var mainView: View? = null
    private var isEnabled: Boolean = false

    init {
        viewBinderHelper.setOpenOnlyOne(true);
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(_activity: Activity, val disposableBag: DisposableBag, view: View) :
        RecyclerView.ViewHolder(view) {
        private var bleAdapterIsEnabled: Boolean = false

        //  private var buttonPressed: Boolean = false
        private var mITagAnimation: Animation? = null
        val tagName: TextView = view.findViewById<TextView>(R.id.text_tag_name)
        val swipeContainer: SwipeRevealLayout =
            view.findViewById<SwipeRevealLayout>(R.id.swipe_container)

        //   val tagNumber: TextView = view.findViewById<TextView>(R.id.tag_number)
        //   val tagsCount: TextView = view.findViewById<TextView>(R.id.tags_count)
        //   val tagsCounterSection: View = view.findViewById<View>(R.id.tags_counter_section)
        val btnForget: LinearLayout = view.findViewById<LinearLayout>(R.id.btn_forget)
        val btnAlert: ImageView = view.findViewById<ImageView>(R.id.btn_alert)
        val imageITag: ImageView = view.findViewById<ImageView>(R.id.image_itag)

        //        val btnForget: View = view.findViewById<View>(R.id.btn_forget)
        val btnColor: View = view.findViewById<View>(R.id.btn_color)
        val textTagName: EditText = view.findViewById<EditText>(R.id.text_tag_name)

        //  val btnEditTagName: View = view.findViewById<View>(R.id.edit_button)
        val rssiView: RssiView = view.findViewById<RssiView>(R.id.rssi)
        val imgStatus = view.findViewById<ImageView>(R.id.bt_status)
        //     val txtDeactivatedMark = view.findViewById<TextView>(R.id.deactivated_mark)

        val textStatus = view.findViewById<TextView>(R.id.text_status)
        val activity = _activity

        init {

            mITagAnimation = AnimationUtils.loadAnimation(activity, R.anim.shake_itag)
            // Define click listener for the ViewHolder's View.
            //   textView = view.findViewById(R.id.textView)
        }

        fun setupButtons(itag: ITagInterface, isEnabled: Boolean) {
            // val activity: Activity = getActivity() ?: return
            //
            bleAdapterIsEnabled = isEnabled
            if (bleAdapterIsEnabled) {
                // btnForget.tag = itag
                btnColor.tag = itag
                btnColor.setOnClickListener { v -> onChangeColor(v) }
                textTagName.tag = itag
                textTagName.setOnEditorActionListener { textView, i, keyEvent ->
                    if (i == EditorInfo.IME_ACTION_DONE) {
                        ITag.store.setName(itag.id(), textTagName.text.toString())
                        textTagName.isEnabled = false
                    }
                    false
                }

                textTagName.setOnClickListener {
                    if (!textTagName.isEnabled) {
                        textTagName.isEnabled = true
                    } else {
                        //  btnEditTagName.isEnabled = true
                        //   btnEditTagName.alpha = 1f
                        textTagName.isEnabled = false
                    }
                }

                btnAlert.tag = itag
                btnAlert.setOnClickListener { v -> onDisconnectAlert(v) }


                btnForget.tag = itag
                btnForget.setOnClickListener { v -> onForget(v) }

                /*
                                btnForget.tag = itag
                                btnForget.setOnClickListener { v -> onForget(v) }
                  */
            } else {
                btnColor.setOnClickListener { v -> null }
                //btnEditTagName.setOnClickListener { v -> null }
                btnAlert.setOnClickListener { v -> null }
                //            btnForget.setOnClickListener { v -> null }

            }

        }

        fun onForget(sender: View) {
            swipeContainer.close(true)
            val itag = sender.tag as ITagInterface
            if (itag != null) {
                val builder = AlertDialog.Builder(activity)
                builder.setMessage(R.string.confirm_forget).setTitle(R.string.confirm_title)
                    .setPositiveButton(android.R.string.yes,
                        DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                            ITag.store.forget(
                                itag
                            )
                        }).setNegativeButton(android.R.string.no,
                        DialogInterface.OnClickListener { dialog: DialogInterface, which: Int -> dialog.cancel() })
                    .show()
            }
        }

        fun onChangeColor(sender: View) {
            val itag = sender.tag as ITagInterface
            if (itag == null) {
                AppClass.instance.handleError(Exception("No itag"))
                return
            }

            val popupMenu = PopupMenu(activity, sender)
            try {
                val fields = popupMenu.javaClass.declaredFields
                for (field in fields) {
                    if ("mPopup" == field.name) {
                        field.isAccessible = true
                        val menuPopupHelper = field[popupMenu]
                        val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                        val setForceIcons = classPopupHelper.getMethod(
                            "setForceShowIcon", Boolean::class.javaPrimitiveType
                        )
                        setForceIcons.invoke(menuPopupHelper, true)
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            popupMenu.inflate(R.menu.color)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                popupMenu.setForceShowIcon(true)
            }
            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.black -> ITag.store.setColor(
                        itag.id(), TagColor.black
                    )

                    R.id.white -> ITag.store.setColor(
                        itag.id(), TagColor.white
                    )

                    R.id.red -> ITag.store.setColor(
                        itag.id(), TagColor.red
                    )

                    R.id.green -> ITag.store.setColor(
                        itag.id(), TagColor.green
                    )

                    R.id.gold -> ITag.store.setColor(
                        itag.id(), TagColor.gold
                    )

                    R.id.blue -> ITag.store.setColor(
                        itag.id(), TagColor.blue
                    )
                }
                updateITagImage(itag)
                true
            }

            popupMenu.show()
        }


        fun updateAlertButton(
            isAlertDisconnected: Boolean, isConnected: Boolean
        ) {
            //
            if (BuildConfig.DEBUG) {
                Log.d(
                    "Tags",
                    "updateAlertButton2 isAlertDisconnected=$isAlertDisconnected isConnected=$isConnected"
                )
            }
            btnAlert.setImageResource(if (isAlertDisconnected || isConnected) R.drawable.linked else R.drawable.keyfinder)
        }


        fun onDisconnectAlert(sender: View) {
            val itag = sender.tag as ITagInterface
            if (itag == null) {
                AppClass.instance.handleError(java.lang.Exception("No itag"))
                return
            }
            val connection = ITag.ble.connectionById(itag.id())
            if (itag.isAlertDisconnected) {
                ITag.store.setAlert(itag.id(), false)
                Thread { connection.disconnect() }.start()
            } else {
                if (connection.isConnected) {
                    Thread { connection.disconnect() }.start()
                } else {
                    ITag.store.setAlert(itag.id(), true)
                    ITag.connectAsync(connection)
                }
            }
            if (itag.isAlertDisconnected) {
                Toast.makeText(activity, R.string.mode_alertdisconnect, Toast.LENGTH_SHORT).show()
                //    ITagApplication.faUnmuteTag();
            } else {
                Toast.makeText(activity, R.string.mode_keyfinder, Toast.LENGTH_SHORT).show()
            }
        }

        fun updateITagImage(itag: ITagInterface) {

            val imageId: Int

            if (bleAdapterIsEnabled) {
                imageId = when (itag.color()) {
                    TagColor.black -> R.drawable.itag_black
                    TagColor.red -> R.drawable.itag_red
                    TagColor.green -> R.drawable.itag_green
                    TagColor.gold -> R.drawable.itag_gold
                    TagColor.blue -> R.drawable.itag_blue
                    TagColor.blue -> R.drawable.itag_blue
                    else -> R.drawable.itag_white
                }
                imageITag.setImageResource(imageId)
                imageITag.tag = itag
                imageITag.setOnClickListener { onITagClick(imageITag) }

            } else {
                imageId = R.drawable.itag_disabled
            }


        }

        fun onITagClick(sender: View) {
            val itag = sender.tag as ITagInterface
            //MediaPlayerUtils.instance!!.stopSound()
            MediaPlayerUtils.getInstance(activity).stopSound()
            val connection = ITag.ble.connectionById(itag.id())
            Notifications.cancelDisconnectNotification(activity)
            if (connection.isFindMe) {
                connection.resetFindeMe()
            } else if (connection.isConnected) {
                Thread {
                    if (connection.isAlerting) {
                        connection.writeImmediateAlert(AlertVolume.NO_ALERT, ITag.BLE_TIMEOUT)
                    } else {
                        connection.writeImmediateAlert(AlertVolume.HIGH_ALERT, ITag.BLE_TIMEOUT)
                    }
                }.start()
            } else {
                if (!itag.isAlertDisconnected) {
                    // there's no sense to communicate if the connection
                    // in the connecting state
                    ITag.connectAsync(connection, false) {
                        if (connection.isAlerting) {
                            connection.writeImmediateAlert(AlertVolume.NO_ALERT, ITag.BLE_TIMEOUT)
                        } else {
                            connection.writeImmediateAlert(AlertVolume.HIGH_ALERT, ITag.BLE_TIMEOUT)
                        }
                    }
                }
            }
        }

        fun updateITagImageAnimation(
            itag: ITagInterface, connection: BLEConnectionInterface
        ) {
            if (mITagAnimation == null) {
                return
            }
            var animShake: Animation? = null
            if (BuildConfig.DEBUG) {
                Log.d(
                    "Tags",
                    "updateITagImageAnimation isFindMe:" + connection.isFindMe + " isAlerting:" + connection.isAlerting + " isAlertDisconnected:" + itag.isAlertDisconnected + " not connected:" + !connection.isConnected
                )
            }
            if (connection.isAlerting || connection.isFindMe || itag.isAlertDisconnected && !connection.isConnected) {
                animShake =
                    mITagAnimation //AnimationUtils.loadAnimation(getActivity(), R.anim.shake_itag);
            }
            if (animShake == null) {
                if (BuildConfig.DEBUG) {
                    Log.d("Tags", "updateITagImageAnimation: No animations appointed")
                }
                animShake = imageITag.animation
                if (animShake != null) {
                    if (BuildConfig.DEBUG) {
                        Log.d("Tags", "updateITagImageAnimation: Stop previous animation")
                    }
                    animShake.cancel()
                }
            } else {
                if (BuildConfig.DEBUG) {
                    Log.d("Tags", "updateITagImageAnimation: Start animation")
                }
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    imageITag.startAnimation(animShake)
                } else {
                    val anim: Animation = animShake

                    activity.runOnUiThread(Runnable {
                        imageITag.startAnimation(anim)

                    })
                }
            }
        }

        private fun updateState(id: String, state: BLEConnectionState) {
            Log.d("Tags", "updateState $id $state")

            if (id != ITag.ble.connectionById(id).id()) {
                Log.d("Tags", "Rechazo porque no es para este Tag")
                return
            } else {
                Log.d("Tags", "Es para este Tag")
            }
            rssiView.visibility = View.INVISIBLE


            val statusDrawableId: Int
            val statusTextId: Int
            if (ITag.ble.state() == BLEState.OK) {
                when (state) {
                    BLEConnectionState.connected -> {
                        statusDrawableId = R.drawable.bt
                        statusTextId = R.string.bt_connected
                        rssiView.visibility = View.VISIBLE
                    }

                    BLEConnectionState.connecting, BLEConnectionState.disconnecting -> {
                        val itag = ITag.store.byId(id)
                        if (itag != null && itag.isAlertDisconnected) {
                            statusDrawableId = R.drawable.bt_connecting
                            statusTextId = R.string.bt_lost
                        } else {
                            statusDrawableId = R.drawable.bt_setup
                            statusTextId =
                                if (state == BLEConnectionState.connecting) R.string.bt_connecting else R.string.bt_disconnecting
                        }
                    }

                    BLEConnectionState.writting, BLEConnectionState.reading -> {
                        statusDrawableId = R.drawable.bt_call
                        statusTextId = R.string.bt_call
                    }

                    BLEConnectionState.disconnected -> {
                        rssiView.visibility = View.INVISIBLE
                        statusDrawableId = R.drawable.bt_disabled
                        statusTextId = R.string.bt_disabled
                        imgStatus.setColorFilter(activity.resources.getColor(R.color.gray_400))
                    }


                    else -> {
                        statusDrawableId = R.drawable.bt_disabled
                        statusTextId = R.string.bt_disabled
                    }

                }
            } else {
                statusDrawableId = R.drawable.bt_disabled
                statusTextId = R.string.bt_disabled
            }
            /*
                        GlideApp.with(AppClass.instance)
                            .load(statusDrawableId)
                            .into(imgStatus)
            */
            imgStatus.setImageResource(statusDrawableId)
            textStatus.setText(statusTextId)
        }


        fun setupRSSI(itag: ITagInterface) {

            // val disposableBag = DisposableBag()

            val id = itag.id()
            if (BuildConfig.DEBUG) {
                Log.d("Tags", "onResume connectionById $id")
            }
            val connection = ITag.ble.connectionById(id)
            disposableBag.add(connection.observableRSSI().subscribe { rssi: Int? ->
                updateRSSI(
                    rssi!!
                )
            })
            disposableBag.add(
                connection.observableImmediateAlert().subscribe { state: AlertVolume? ->
                    updateITagImageAnimation(
                        itag, connection
                    )
                })
            // Comienzo a escuchar
            disposableBag.add(connection.observableState().subscribe { state: BLEConnectionState? ->
                updateState(id, state!!)
                updateITagImageAnimation(itag, connection)
                if (connection.state() == BLEConnectionState.connected) { //isConnected()) {
                    connection.enableRSSI()
                } else {
                    connection.disableRSSI()
                    updateRSSI(id, -999)
                }
            })


            // Tomo el valor inicial para que este actualizado al comenzar
            updateState(id, connection.observableState().value())
            updateITagImageAnimation(itag, connection)
            if (connection.state() == BLEConnectionState.connected) { //isConnected()) {
                connection.enableRSSI()
            } else {
                connection.disableRSSI()
                updateRSSI(id, -999)
            }


            /*
                      disposableBag.add(connection.observableState().subscribe { state: BLEConnectionState? ->
                          activity.runOnUiThread(Runnable {
                              if (BuildConfig.DEBUG) {
                                  Log.d(
                                      "Tags",
                                      "connection " + id + " state changed " + connection.state().toString()
                                  )
                              }

                              updateAlertButton(ITag.store.byId(id)!!.isAlertDisconnected, true)
                              updateState(id, state!!)
                              updateITagImageAnimation(itag, connection)
                              if (connection.state() == BLEConnectionState.connected) { //isConnected()) {
                                  connection.enableRSSI()
                              } else {
                                  connection.disableRSSI()
                                  updateRSSI(id, -999)
                              }
                          })
                      })
          */
            /*
                  disposableBag.add(connection.observableClick().subscribe { event: Int? ->

                      //    buttonPressed = event ?: 0 == 1

                      if (connection.isFindMe) {
                          MediaPlayerUtils.instance!!.startFindPhone(activity)
                          updateITagImageAnimation(
                              itag, connection
                          )
                          Toast.makeText(
                              activity, "El Boton de Pánico está funcional!", Toast.LENGTH_SHORT
                          ).show()
                      }
                  })
            */

            //}
        }

        private fun updateRSSI(rssi: Int) {
            rssiView.setRssi(rssi)
 /*
            Log.d("RSSI_", "RSSI: $rssi")
            Log.d("RSSI_", "Distance: "+calculateItagDistance(rssi))
            Log.d("RSSI_", "otrafunc: "+calcDistbyRSSI(rssi))
*/
        }
/*
        fun calcDistbyRSSI(rssi: Int, measurePower: Int = -69): String? {
            val iRssi = Math.abs(rssi)
            val iMeasurePower = Math.abs(measurePower)
            val power:Double = (iRssi - iMeasurePower)/(10*2.0)
            // ft = m * 3.2808
            if (Math.pow(10.0,power) * 3.2808 < 1.0){
                return String.format("%.2f ft(Immediate)", Math.pow(10.0,power) * 3.2808)
            }else if (Math.pow(10.0,power) * 3.2808 > 1.0 && Math.pow(10.0,power) * 3.2808 < 10.0){
                return String.format("%.2f ft(Near)", Math.pow(10.0,power) * 3.2808)
            }else{
                return String.format("%.2f ft(Far)", Math.pow(10.0,power) * 3.2808)
            }
        }

        fun calculateItagDistance(rssi: Int, measuredPower: Int = -70, n: Double = 1.0): Double {
            return 10.0.pow((measuredPower - rssi) / (10 * n))
        }
*/
        private fun updateRSSI(id: String, rssi: Int) {
            if (id == tagName.tag) {
                updateRSSI(rssi)
            } else {
                Log.d("Tags", "Rechazo porque no es para este Tag")
            }
        }

    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.itag_item_horizontal, viewGroup, false)

        mainView = view

        return ViewHolder(activity, disposableBag, view)
    }

    fun saveStates(outState: Bundle?) {
        viewBinderHelper.saveStates(outState)
    }

    fun restoreStates(inState: Bundle?) {
        viewBinderHelper.restoreStates(inState)
    }


    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {


        // get your data object first.
        val dataObject = dataSet.byPos(position)


        // Save/restore the open/close state.
        // You need to provide a String id which uniquely defines the data object.
        viewBinderHelper.bind(viewHolder.swipeContainer, dataObject.id())


        this.currentPosition = position
        val itag = dataSet.byPos(position)
        val connection = ITag.ble.connectionById(itag.id())
        viewHolder.tagName.text = itag.name()
        viewHolder.tagName.tag = itag.id()
        viewHolder.setupButtons(itag, isEnabled)
        //  viewHolder.updateTagsNumberIndicator(position, dataSet.count())
        viewHolder.updateAlertButton(itag.isAlertDisconnected, connection.isConnected)
        viewHolder.updateITagImage(itag)
        viewHolder.updateITagImageAnimation(itag, connection)
        viewHolder.setupRSSI(itag)


        /*
                if (!isEnabled) {
                    viewHolder.txtDeactivatedMark.visibility = View.VISIBLE
                    viewHolder.txtDeactivatedMark.text = "No Habilitado "
                    viewHolder.itemView.alpha = .5f
                } else {
                    if (connection.isConnected) {
        //                viewHolder.txtDeactivatedMark.visibility = View.GONE
                        viewHolder.itemView.alpha = 1.0f
                    } else {
                        viewHolder.txtDeactivatedMark.text = "Desconectado"
                        viewHolder.txtDeactivatedMark.visibility = View.VISIBLE
                        viewHolder.itemView.alpha = 0.5f
                    }
                }
                */
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.count()
    fun onBluetoothOff() {
        mainView?.alpha = .5f
        this.isEnabled = false
        notifyDataSetChanged()
    }

    fun whenBluetoothDevicesDeactivated() {
        mainView?.alpha = .5f
        this.isEnabled = false
        notifyDataSetChanged()
    }

    fun whenBluetoothDevicesIsActive() {
        isEnabled = true
        mainView?.alpha = 1.0f
        notifyDataSetChanged()

    }


    // Funciones de los TAGS


}