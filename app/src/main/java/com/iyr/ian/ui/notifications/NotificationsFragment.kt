package com.iyr.ian.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.callbacks.INotifications
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.dao.models.EventNotificationModel
import com.iyr.ian.dao.repositories.NotificationsRepository
import com.iyr.ian.databinding.FragmentNotificationsBinding
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.ui.notifications.adapters.NotificationsAdapter
import com.iyr.ian.utils.connectivity.NetworkStateReceiver
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.utils.showSnackBar
import com.iyr.ian.viewmodels.MainActivityViewModel
import com.iyr.ian.viewmodels.NotificationsFragmentViewModel


class NotificationsFragment() :
    Fragment(),
    INotifications {

    fun NotificationsFragment() {}

    private var isBussy: Boolean = false
    private var hasConnectivity: Boolean = true
    private lateinit var networkStateReceiver: NetworkStateReceiver
    private lateinit var binding: FragmentNotificationsBinding
    private val adapter by lazy { NotificationsAdapter(requireContext(), this) }

    private lateinit var viewModel: NotificationsFragmentViewModel


//    private var usersSearchAdapter: CustomCompleteTextViewAdapter? = null


    companion object {
        @JvmStatic
        fun newInstance() =
            NotificationsFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = NotificationsFragmentViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentNotificationsBinding.inflate(layoutInflater, container, false)
        setupUI()
        return binding.root
    }


    override fun onResume() {
        (requireActivity() as MainActivity).appToolbar.updateTitle(getString(R.string.action_notifications))
        (requireActivity() as MainActivity).appToolbar.enableBackBtn(true)
        (requireActivity() as MainActivity).hideFooterToolBar()
        super.onResume()
        startObservers()
    }


    override fun onPause() {
        super.onPause()
        stopObservers()
    }

    private fun startObservers() {
        MainActivityViewModel.getInstance().notifications.observe(viewLifecycleOwner, {
            adapter.setData(it)

//            NotificationsRepository.getInstance().setNotificationsAsRead(it.map { it.notification_key })
            //generar un mapa con solamente notification_key donde read sea true


            val notReadNotifications = it.filter { !it.read || it.read == null }
            if (notReadNotifications.isNotEmpty()) {
                viewModel.updateNotificationsAsRead(notReadNotifications.map { it.notification_key })
            }
            MainActivityViewModel.getInstance().notifications.removeObservers(viewLifecycleOwner)
        })


        MainActivityViewModel.getInstance().notificationsFlow.observe(this) { dataEvent ->
            when (dataEvent) {
                is NotificationsRepository.DataEvent.ChildAdded -> {
                    if (!adapter.getData().contains(dataEvent.data)) {
                        dataEvent.data.read = true
                        adapter.getData().add(dataEvent.data)
                        adapter.notifyItemInserted(adapter.getData().size - 1)
                        viewModel.updateNotificationsAsRead(listOf(dataEvent.data.notification_key) )
                    }
                }

                is NotificationsRepository.DataEvent.ChildChanged -> {
                    val index = adapter.getData().indexOf(dataEvent.data)
                    if (index == -1) {
                        adapter.getData().add(dataEvent.data)
                        adapter.notifyItemInserted(adapter.getData().size - 1)
                    } else {
                        adapter.getData()[index] = dataEvent.data
                        adapter.notifyItemChanged(index)
                    }
                }

                is NotificationsRepository.DataEvent.ChildRemoved -> {
                    val index = adapter.getData().indexOf(dataEvent.data)
                    if (index > -1) {
                        adapter.getData().removeAt(-1)
                        adapter.notifyItemRemoved(index)
                    }
                }

                is NotificationsRepository.DataEvent.onChildMoved -> {

                }

                else -> {
                }
            }
        }

    }


    private fun stopObservers() {
        MainActivityViewModel.getInstance().notifications.removeObservers(viewLifecycleOwner)
        MainActivityViewModel.getInstance().notificationsFlow.removeObservers(viewLifecycleOwner)
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


                            Toast.makeText(
                                requireContext(),
                                "Enviar mensaje para que cambie de module",
                                Toast.LENGTH_SHORT
                            ).show()

                        }

                        override fun onError(exception: Exception) {
                            super.onError(exception)
                            isBussy = false
                            requireActivity().showErrorDialog(exception.localizedMessage.toString())
                        }
                    }

                    requireActivity().showSnackBar(
                        binding.root,
                        "Implementar notificationsRemoveAll"
                    )
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
        binding.recyclerNotifications.adapter = adapter
    }


    fun getData(): ArrayList<EventNotificationModel> {
        return adapter.getData()
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

    override fun onError(exception: Exception) {
        requireActivity().showErrorDialog(exception.localizedMessage.toString())
    }
}