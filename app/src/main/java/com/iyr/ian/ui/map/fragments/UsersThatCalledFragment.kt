package com.iyr.ian.ui.map.fragments

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.DialogInterface
import android.content.IntentFilter
import android.graphics.Rect
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.iyr.ian.AppConstants.Companion.SLIDE_FRAGMENT_HIDE_SPEED
import com.iyr.ian.AppConstants.Companion.SLIDE_FRAGMENT_SHOW_SPEED
import com.iyr.ian.R
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.EventFollower
import com.iyr.ian.dao.repositories.EventFollowersRepository
import com.iyr.ian.databinding.FragmentUsersThatAlreadyCalledBinding
import com.iyr.ian.receivers.EventChangesReceiver
import com.iyr.ian.ui.map.MapSituationFragment
import com.iyr.ian.ui.map.adapters.UserWhoCalledAdapter
import com.iyr.ian.ui.map.event_header.EventHeaderFragment
import com.iyr.ian.ui.map.event_header.adapter.EventHeaderCallback
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.connectivity.NetworkStateReceiver
import com.iyr.ian.utils.makeAPhoneCall
import com.iyr.ian.utils.showSnackBar
import com.iyr.ian.viewmodels.MapSituationFragmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class UsersThatCalledFragment(
    val parentFragment: MapSituationFragment,
    val mapSituationFragmentViewModel: MapSituationFragmentViewModel
) : Fragment(),
    NetworkStateReceiver.NetworkStateReceiverListener,
    DialogInterface.OnClickListener {

    private var me: EventFollower? = null
    private var isBussy: Boolean = false
    private var hasConnectivity: Boolean = true
    private lateinit var networkStateReceiver: NetworkStateReceiver
    private var eventChangesReceiver: EventChangesReceiver? = null
    private var eventKey: String? = null
    private val adapter: UserWhoCalledAdapter by lazy { UserWhoCalledAdapter(parentFragment.requireActivity()) }
    lateinit var eventData: Event

    private lateinit var binding: FragmentUsersThatAlreadyCalledBinding

    private val globalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {

            var buttonRect = Rect()
            binding?.callButton?.getGlobalVisibleRect(buttonRect)
            val callButtonHeight = buttonRect.height()

            var callButtonParams: ViewGroup.LayoutParams?
            callButtonParams = LinearLayout.LayoutParams(
                callButtonHeight,
                callButtonHeight,
                0f
            )
            binding.callButton.layoutParams = callButtonParams
            binding?.root?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
        }
    }

    fun UsersThatCalledFragment() {

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
        binding = FragmentUsersThatAlreadyCalledBinding.inflate(layoutInflater, container, false)
        setupUI()

        binding.layoutCard.visibility = View.GONE
        binding.recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = adapter

        binding.buttonCallStatus.setOnClickListener {
            updateCallStatusButton(me?.call_time == null)
            mapSituationFragmentViewModel.onToggleCallStatus(
                FirebaseAuth.getInstance().uid.toString(),
                eventKey!!

            )
        }


        binding.callButton.setOnClickListener {
            requireActivity().makeAPhoneCall("911")
        }
        binding.closeButton.setOnClickListener {
            requireActivity().showSnackBar(binding.root, "Click!!")
            requireActivity().handleTouch()
            closeBottomSheet()
        }

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
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FriendsFragment.
         */
        @JvmStatic
        fun newInstance(
            event: Event,
            mapSituationFragmentViewModel: MapSituationFragmentViewModel,
            param1: String,
            param2: String
        ) =
            EventHeaderFragment(this as EventHeaderCallback, event, mapSituationFragmentViewModel)

    }

    override fun onResume() {
        super.onResume()
        binding?.root?.viewTreeObserver?.addOnGlobalLayoutListener(
            globalLayoutListener
        )
        setupObservers()
        updateUI()
    }

    override fun onPause() {
        super.onPause()
        removeObservers()
    }

    private fun setupObservers() {

        lifecycleScope.launch(Dispatchers.Main) {
            adapter.getData().clear()
            mapSituationFragmentViewModel.followersList.forEach { follower ->
                if (follower.user_key != FirebaseAuth.getInstance().uid.toString()) {
                    if (follower.call_time != null)
                        adapter.getData().add(follower)
                } else {
                    me = follower

                    updateCallStatusButton(me?.call_time != null)
                }
            }
            adapter.notifyDataSetChanged()

            mapSituationFragmentViewModel.eventFollowersConnector.collect { event ->
                processFollowersEvents(event)
            }

        }



        /*
        mapSituationFragmentViewModel.followers.observe(viewLifecycleOwner) { list ->
            adapter.getData().clear()


            var eventAuthorKey: String? = null
            list.forEach { follower ->
                if (follower.is_author)
                    eventAuthorKey = follower.user_key

                if (FirebaseAuth.getInstance().uid.toString().compareTo(follower.user_key) != 0) {

                    if (follower.call_time != null)
                        adapter.getData().add(follower)
                    else {
                        if (adapter.getData().contains(follower)) {
                            adapter.getData().remove(follower)
                        }
                    }
                } else {
                    me = follower
                    updateCallStatusButton(me?.call_time != null)
                }

            }
            adapter.notifyDataSetChanged()


        }
*/
    }

    private fun removeObservers() {
        mapSituationFragmentViewModel.followers.removeObservers(viewLifecycleOwner)
    }


    /**
     * Procesa los eventos de la lista de seguidores.
     * Se lo llama tanto tomando el cache de los anteriores como asi tambien con los que vengan
     * despues de inicializado
     */
    private fun processFollowersEvents(event: EventFollowersRepository.EventFollowerDataEvent?) {
        when (event) {
            is EventFollowersRepository.EventFollowerDataEvent.OnChildAdded -> {
                var follower = event.data!!
                var followerIndex = getFollowerIndex(follower)
                if (follower.user_key != FirebaseAuth.getInstance().uid.toString()) {
                    if (follower.call_time != null && followerIndex == -1) {
                        adapter.getData().add(follower)
                    }
                } else {
                    me = follower
                    updateCallStatusButton(me?.call_time != null)
                }
            }

            is EventFollowersRepository.EventFollowerDataEvent.OnChildChanged -> {
                var follower = event.data!!
                var followerIndex = getFollowerIndex(follower)
                if (follower.user_key != FirebaseAuth.getInstance().uid.toString()) {
                    if (follower.call_time != null) {
                        if (followerIndex == -1) {
                            adapter.getData().add(follower)
                        } else {
                            adapter.getData().set(followerIndex, follower)
                        }
                    } else {
                        if (followerIndex != -1) {
                            adapter.getData().removeAt(followerIndex)
                        }
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    me = follower
                    updateCallStatusButton(me?.call_time != null)
                }
            }

            is EventFollowersRepository.EventFollowerDataEvent.OnChildMoved -> TODO()
            is EventFollowersRepository.EventFollowerDataEvent.OnChildRemoved -> {
            }

            is EventFollowersRepository.EventFollowerDataEvent.OnError -> TODO()
            null -> TODO()
        }
    }

    /**
     * Devuelve el indice de una key de usuario en el array que puebla el adapter
     */
    fun getFollowerIndex(follower: EventFollower): Int {
        var iter = -1
        var index = -1
        adapter.getData().forEach { record ->
            iter++
            if (record.user_key == follower.user_key) {
                index = iter
                return@forEach
            }
        }
        return index
    }

    private fun updateUI() {


    }

    private fun setupUI() {


    }

    private fun updateCallStatusButton(didICall: Boolean) {

        if (::binding.isInitialized) {
            if (!didICall)
                binding.buttonCallStatus.setText(R.string.i_already_call)
            else
                binding.buttonCallStatus.setText(R.string.i_didnt_call_yet)
        }
    }


    private fun initAdapter() {


    }

    private fun resetAdapter() {
        adapter.getData().clear()
        adapter.notifyDataSetChanged()
    }


    override fun onClick(p0: DialogInterface?, p1: Int) {
        TODO("Not yet implemented")
    }


    private fun subscribe() {

    }


    fun updateAdapter(viewers: HashMap<String, EventFollower>) {
        val adapterData = adapter.getData()
        var imGoing: Boolean = false
        adapterData.forEach { viewer ->
            if (!viewers.contains(viewer.user_key) || viewers[viewer.user_key]?.call_time == null) {
                if (viewer.user_key != FirebaseAuth.getInstance().uid.toString()) {
                    val index = adapterData.indexOf(viewers[viewer.user_key]!!)
                    adapterData.remove(viewers[viewer.user_key]!!)
                    adapter.notifyItemRemoved(index)
                }
            }
        }
        viewers.forEach { (key, value) ->
            val eventFollower: EventFollower = value
            if (eventFollower.user_key != FirebaseAuth.getInstance().uid.toString())  // Si es otro usuario
            {
                if (!adapterData.contains(eventFollower)) {
                    if (eventFollower.call_time != null) {
                        adapterData.add(eventFollower)
                        adapter.notifyItemInserted(adapterData.size - 1)
                    }
                } else {
                    val index = adapterData.indexOf(eventFollower)
                    if (eventFollower.call_time != null) {
                        adapterData[index] = eventFollower
                        adapter.notifyItemChanged(index)
                    } else {
                        adapterData.removeAt(index)
                        adapter.notifyItemRemoved(index)
                    }
                }
            } else {
                imGoing = eventFollower.call_time != null
            }
            updateCallStatusButton(imGoing)
        }
    }

    fun setEventKey(eventKey: String) {
        this.eventKey = eventKey
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

