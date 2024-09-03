package com.iyr.ian.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.iyr.ian.R
import com.iyr.ian.callbacks.INotifications
import com.iyr.ian.dao.models.EventNotificationModel
import com.iyr.ian.dao.repositories.NotificationsRepository
import com.iyr.ian.databinding.FragmentNotificationsBinding
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.ui.notifications.adapters.NotificationsAdapter
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.connectivity.NetworkStateReceiver
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.loaders.hideLoader
import com.iyr.ian.utils.loaders.showLoader
import com.iyr.ian.utils.showConfirmationDialog
import com.iyr.ian.utils.showErrorDialog
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
    private val adapter by lazy { NotificationsAdapter(requireActivity(), this) }

    private lateinit var viewModel: NotificationsFragmentViewModel


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

        binding.clearAll.visibility = View.INVISIBLE


        binding.clearAll.setOnClickListener {
            requireContext().handleTouch()
            requireActivity().showConfirmationDialog(
                getString(R.string.action_delete_all_notifications),
                getString(R.string.action_delete_all_notifications_message),
                getString(R.string.yes),
                getString(R.string.no),
                object : OnClickListener {
                    override fun onClick(v: View?) {
                        viewModel.onDeleteAllNotificationsRequested()
                    }
                }
            )
        }

        binding.recyclerNotifications.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerNotifications.adapter = adapter
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
        MainActivityViewModel.getInstance().notifications.observe(viewLifecycleOwner, { it ->
            adapter.setData(it)
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
                        viewModel.updateNotificationsAsRead(listOf(dataEvent.data.notification_key))

                    }
                    if (adapter.getData().size > 0) {
                        binding.clearAll.visibility = View.VISIBLE
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
                        adapter.getData().removeAt(index)
                        adapter.notifyItemRemoved(index)
                        if (adapter.getData().size == 0) {
                            findNavController().popBackStack()
                        }
                    }
                }

                is NotificationsRepository.DataEvent.onChildMoved -> {

                }

                else -> {
                }
            }
        }

        viewModel.deletionLivaData.observe(this) { resource ->
            when (resource) {
                is Resource.Error -> {
                    requireActivity().hideLoader()
                    requireActivity().showErrorDialog(resource.message.toString())
                }

                is Resource.Loading -> {
                    requireActivity().showLoader()
                }

                is Resource.Success -> {
                    requireActivity().hideLoader()
                    adapter.getData().clear()
                    adapter.notifyDataSetChanged()
                    findNavController().popBackStack()
                }

                else -> {

                }
            }
        }
    }

    private fun stopObservers() {
        MainActivityViewModel.getInstance().notifications.removeObservers(viewLifecycleOwner)
        MainActivityViewModel.getInstance().notificationsFlow.removeObservers(viewLifecycleOwner)
        viewModel.deletionLivaData.removeObservers(viewLifecycleOwner)
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

    override fun onAgreeToAssist(notificationKey: String, eventKey: String) {
        viewModel.agreeToAssist(notificationKey, eventKey)
    }

    override fun notificationDeleteByKey(
        notification: EventNotificationModel,
        viewPressed: View
    ) {
        viewModel.onDeleteNotificationByKeyRequested(notification.notification_key)
    }
}