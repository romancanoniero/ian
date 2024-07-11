package com.iyr.ian.ui.notifications

import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.iyr.ian.AppConstants
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.callbacks.INotifications
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.dao.models.EventNotificationModel
import com.iyr.ian.databinding.FragmentNotificationsBinding
import com.iyr.ian.ui.notifications.adapters.NotificationsAdapter
import com.iyr.ian.utils.broadcastMessage
import com.iyr.ian.utils.connectivity.NetworkStateReceiver
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.utils.showSnackBar





class NotificationsFragment(context : Context, val callback: INotifications) :
    Fragment(),
    NetworkStateReceiver.NetworkStateReceiverListener {

    fun NotificationsFragment() {}

    private var isBussy: Boolean = false
    private var hasConnectivity: Boolean = true
    private lateinit var networkStateReceiver: NetworkStateReceiver
    private lateinit var binding: FragmentNotificationsBinding
    private val notificationsRecyclerViewAdapter by lazy { NotificationsAdapter(context,this) }

//    private var usersSearchAdapter: CustomCompleteTextViewAdapter? = null

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    companion object {


        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FriendsFragment.
         */
        // TODO: Rename and change types and number of parameters


        @JvmStatic
        fun newInstance(param1: String, param2: String,context: Context,callback: INotifications) =
            NotificationsFragment(context, callback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startNetworkBroadcastReceiver(requireContext())
        Log.d("EVENT_CREATION", this.javaClass.name)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireContext().broadcastMessage(null,
            AppConstants.ServiceCode.BROADCAST_ACTION_HIDE_FOOTER_TOOLBAR)
        binding = FragmentNotificationsBinding.inflate(layoutInflater, container, false)
        setupUI()
        return binding.root
    }


    override fun onResume() {
        registerNetworkBroadcastReceiver(requireContext())
        super.onResume()
      //  AppClass.instance.addViewToStack(IANModulesEnum.NOTIFICATIONS, this)

    }

    override fun onPause() {
        unregisterNetworkBroadcastReceiver(requireContext())
        super.onPause()
  //      AppClass.instance.removeViewFromStack(this)
    }

    private fun setupUI() {
        binding.clearAll.setOnClickListener {
            if (!isBussy) {
                if (hasConnectivity) {
                    isBussy = true
                    val callback: OnCompleteCallback = object : OnCompleteCallback {
                        override fun onComplete(success: Boolean, result: Any?) {
                            super.onComplete(success, result)
                            isBussy = false
                       //     (requireActivity() as MainActivity).switchToModule(0, "home")


                            Toast.makeText(requireContext(), "Enviar mensaje para que cambie de module", Toast.LENGTH_SHORT).show()

                        }

                        override fun onError(exception: Exception) {
                            super.onError(exception)
                            isBussy = false
                            requireActivity().showErrorDialog(exception.localizedMessage.toString())
                        }
                    }

                    requireActivity().showSnackBar(binding.root, "Implementar notificationsRemoveAll")
                    //NotificationsWSClient.instance.notificationsRemoveAll(callback)
                    AppClass.instance.getCurrentActivity()?.onBackPressed()
                } else {
                    requireActivity().showSnackBar(
                        binding.root,
                        getString(R.string.no_connectivity)
                    )
                }
            }
        }

        binding.recyclerNotifications.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerNotifications.adapter = notificationsRecyclerViewAdapter
    }


    fun getData(): ArrayList<EventNotificationModel> {
        return notificationsRecyclerViewAdapter.getData()
    }

    @JvmName("getAdapter1")
    fun getAdapter(): NotificationsAdapter {
        return notificationsRecyclerViewAdapter
    }

    fun showDialog(title: String, message: String, actionButton: String) {

        requireActivity().showErrorDialog(
            title,
            message,
            actionButton,
            null
        )
    }

    fun updateUI() {

    }

    //------------------- networkStatus
    override fun networkAvailable() {
        hasConnectivity = true
        updateUI()
    }

    override fun networkUnavailable() {
        hasConnectivity = false
        updateUI()
    }


    private fun startNetworkBroadcastReceiver(currentContext: Context) {
        networkStateReceiver = NetworkStateReceiver()
        networkStateReceiver.addListener(this)
        registerNetworkBroadcastReceiver(currentContext)
    }

    /**
     * Register the NetworkStateReceiver with your activity
     * @param currentContext
     */
    private fun registerNetworkBroadcastReceiver(currentContext: Context) {

        Log.d("NetworkBroadcasReceiver - register", this.javaClass.name)
        currentContext.registerReceiver(
            networkStateReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
    }

    /**
     * Unregister the NetworkStateReceiver with your activity
     * @param currentContext
     */
    private fun unregisterNetworkBroadcastReceiver(currentContext: Context) {
        Log.d("NetworkBroadcasReceiver - unregister", this.javaClass.name)
        currentContext.unregisterReceiver(networkStateReceiver)
    }



}