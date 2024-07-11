package com.iyr.ian.ui.map.fragments

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.DialogInterface
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.iyr.ian.AppConstants
import com.iyr.ian.AppConstants.Companion.SLIDE_FRAGMENT_HIDE_SPEED
import com.iyr.ian.AppConstants.Companion.SLIDE_FRAGMENT_SHOW_SPEED
import com.iyr.ian.R
import com.iyr.ian.dao.models.Event
import com.iyr.ian.databinding.FragmentUsersParticipatingBinding
import com.iyr.ian.receivers.EventChangesReceiver
import com.iyr.ian.ui.base.PulseRequestTarget
import com.iyr.ian.ui.base.ValidationPulsePayload
import com.iyr.ian.ui.map.MapSituationFragment
import com.iyr.ian.ui.map.adapters.UsersParticipatingAdapter
import com.iyr.ian.ui.map.event_header.EventHeaderFragment
import com.iyr.ian.ui.map.event_header.adapter.EventHeaderCallback
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.broadcastMessage
import com.iyr.ian.utils.connectivity.NetworkStateReceiver
import com.iyr.ian.utils.showSnackBar
import com.iyr.ian.viewmodels.MapSituationFragmentViewModel

class UsersParticipatingFragment(
    val parentFragment: MapSituationFragment,
    val mapSituationFragmentViewModel: MapSituationFragmentViewModel
) : Fragment(),
    NetworkStateReceiver.NetworkStateReceiverListener,
    DialogInterface.OnClickListener {

    private var isBussy: Boolean = false
    private var hasConnectivity: Boolean = true
    private lateinit var networkStateReceiver: NetworkStateReceiver
    private var isAuthor: Boolean = false
    private var eventChangesReceiver: EventChangesReceiver? = null
    private var eventKey: String? = null
    private val adapter: UsersParticipatingAdapter by lazy {
        UsersParticipatingAdapter(
            parentFragment.requireActivity()
        )
    }
    lateinit var eventData: Event

    private lateinit var binding: FragmentUsersParticipatingBinding

    fun UsersParticipatingFragment() {

    }

    init {
        initAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startNetworkBroadcastReceiver(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentUsersParticipatingBinding.inflate(layoutInflater, container, false)

        setupUI()
        //  closeBottomSheet()
        openBottomSheet()
        return binding.root
    }


    private fun openBottomSheet() {
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        displayMetrics.widthPixels
        val anim =
            ValueAnimator.ofInt(binding.layoutCard.measuredHeight, (height * .8).toInt())
        anim.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            val layoutParams: ViewGroup.LayoutParams = binding.layoutCard.layoutParams!!
            layoutParams.height = `val`
            binding.layoutCard.layoutParams = layoutParams
        }
        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {
                binding.layoutCard.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(p0: Animator) {
//                    binding.layoutCard.visibility = View.GONE
            }

            override fun onAnimationCancel(p0: Animator) {
//                    TODO("Not yet implemented")
            }

            override fun onAnimationRepeat(p0: Animator) {
                //                  TODO("Not yet implemented")
            }
        })

        anim.duration = SLIDE_FRAGMENT_SHOW_SPEED
        anim.interpolator = AccelerateDecelerateInterpolator()
        anim.start()

    }

    fun closeBottomSheet() {
        val anim = ValueAnimator.ofInt(
            binding.layoutCard.measuredHeight, 0
        )
        anim.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            val layoutParams: ViewGroup.LayoutParams = binding.layoutCard.layoutParams
            layoutParams.height = `val`
            binding.layoutCard.layoutParams = layoutParams
        }
        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {

            }

            override fun onAnimationEnd(p0: Animator) {
                binding.layoutCard.visibility = View.GONE
                if (parentFragment is EventHeaderCallback) {
                    (parentFragment as EventHeaderCallback).onFragmentFromBottomClose()
                }
            }

            override fun onAnimationCancel(p0: Animator) {
//                    TODO("Not yet implemented")
            }

            override fun onAnimationRepeat(p0: Animator) {
                //                  TODO("Not yet implemented")
            }
        })
        anim.duration = SLIDE_FRAGMENT_HIDE_SPEED
        anim.interpolator = AccelerateDecelerateInterpolator()
        anim.start()

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param event Parameter 1.
         * @return A new instance of fragment FriendsFragment.
         */
        @JvmStatic
        fun newInstance(
            event: Event,
            mapSituationFragmentViewModel: MapSituationFragmentViewModel
        ) =
            EventHeaderFragment(this as EventHeaderCallback, event, mapSituationFragmentViewModel)
    }

    override fun onResume() {
        registerNetworkBroadcastReceiver(requireContext())
        setupObservers()
        super.onResume()

    }

    override fun onPause() {
        unregisterNetworkBroadcastReceiver(requireContext())
        super.onPause()
removeObservers()
    }



    private fun setupObservers() {

        mapSituationFragmentViewModel.followersList


        mapSituationFragmentViewModel.followers.observe(viewLifecycleOwner) { list ->
            adapter.getData().clear()
            var eventAuthorKey: String? = null
            list.forEach { follower ->
                if (follower.is_author)
                    eventAuthorKey = follower.user_key

                if (FirebaseAuth.getInstance().uid.toString()
                        .compareTo(follower.user_key) != 0
                )
                    adapter.getData().add(follower)
            }
            adapter.notifyDataSetChanged()

            if (eventAuthorKey == FirebaseAuth.getInstance().uid.toString()) {
                binding.buttonAction.text = getString(R.string.close_event)
            } else
                binding.buttonAction.text = getString(R.string.leave_event)

        }

    }

    private fun removeObservers() {
        mapSituationFragmentViewModel.followers.removeObservers(viewLifecycleOwner)
    }



    private fun setupUI() {

        binding.closeButton.setOnClickListener {
            requireActivity().showSnackBar(binding.root, "Click!!")
            requireActivity().handleTouch()
            closeBottomSheet()
        }

        binding.layoutCard.visibility = View.GONE
        binding.recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = adapter


        binding.buttonAction.setOnClickListener {

            if (!isBussy) {
                if (hasConnectivity) {
                    isBussy = true

                    closeBottomSheet()
                    if (eventData.author_key.toString()
                            .compareTo(FirebaseAuth.getInstance().uid.toString()) == 0
                    )
                        onCloseEventRequest()
                    else
                        onLeaveEventRequest()
                } else {
                    requireActivity().showSnackBar(
                        binding.root,
                        getString(R.string.no_connectivity)
                    )

                }
            }
            binding.closeButton.setOnClickListener {
                requireActivity().showSnackBar(binding.root, "Click!!")

                requireActivity().handleTouch()
                closeBottomSheet()
            }
        }
    }

    private fun onCloseEventRequest() {

        var payload = ValidationPulsePayload()
        payload.validationType = PulseRequestTarget.VALIDATION_BEFORE_CLOSE_EVENT
        payload.key = eventKey ?: "error"

        requireContext().broadcastMessage(payload, AppConstants.BROADCAST_ACTION_REQUEST_PIN)


        /*
        val validatorCallback = object : PulseValidationCallback {
            override fun onWrongCode(dialog: PulseValidatorDialog, securityPIN: String) {
                super.onWrongCode(dialog, securityPIN)
                isBussy = false
                requireActivity().showErrorDialog(
                    getString(R.string.error_wrong_security_code),
                    getString(R.string.error_wrong_security_code_message),
                    getString(R.string.close),
                    null
                )
            }

            override fun onSilentAlarmCode(dialog: PulseValidatorDialog, securityPIN: String) {
                super.onSilentAlarmCode(dialog, securityPIN)

                requireActivity().showSnackBar(binding.root, "Implementar el close event")
                /*
                EventsWSClient.instance.closeEvent(
                    AppClass.instance.getPanicEventKey()!!,
                    FirebaseAuth.getInstance().uid.toString(),
                    securityPIN,
                    object : OnCompleteCallback {
                        override fun onComplete(success: Boolean, result: Any?) {
                            isBussy = false
                        }
                    }
                )
                */


            }

            override fun onValidationOK(dialog: PulseValidatorDialog, securityPIN: String) {



                requireActivity().showSnackBar(binding.root, "Implementar el onValidationOK")

                /*
                val wsCallback = object : OnCompleteCallback {
                    override fun onComplete(success: Boolean, result: Any?) {
                        isBussy = false
                        closeBottomSheet()
                        parentFragment.selectLastEvent()
                        requireActivity().showAnimatedDialog(
                            getString(R.string.closing_event_title),
                            getString(R.string.event_sucessfully_close)
                        )
                    }
                }

                EventsWSClient.instance.closeEvent(
                    eventKey!!,
                    FirebaseAuth.getInstance().uid.toString(),
                    securityPIN,
                    wsCallback
                )
                */

            }

            override fun onCancel(dialog: PulseValidatorDialog) {
                isBussy = false
            }
        }
        requireActivity().requestStatusConfirmationSingleton(
            PulseRequestTarget.VALIDATION_BEFORE_CLOSE_EVENT,
            validatorCallback
        )
        */


    }

    private fun onLeaveEventRequest() {

        requireActivity().showSnackBar(binding.root, "Implementar el onLeaveEventRequest()")

        /*
        val callback = object : OnCompleteCallback {
            override fun onComplete(success: Boolean, result: Any?) {
                super.onComplete(success, result)
                isBussy = false
                closeBottomSheet()
            }

            override fun onError(exception: Exception) {
                super.onError(exception)
                isBussy = false
            }
        }



        EventsWSClient.instance.leaveEvent(
            eventKey!!,
            callback
        )
        */


    }


    private fun initAdapter() {
        //   registerReceivers()
    }

    private fun resetAdapter() {
        adapter.getData().clear()
        adapter.notifyDataSetChanged()
    }


    override fun onClick(p0: DialogInterface?, p1: Int) {
        TODO("Not yet implemented")
    }

    fun setEventKey(eventKey: String) {
        this.eventKey = eventKey
    }

    //------------------- networkStatus
    override fun networkAvailable() {
        hasConnectivity = true

    }

    override fun networkUnavailable() {
        hasConnectivity = false

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

