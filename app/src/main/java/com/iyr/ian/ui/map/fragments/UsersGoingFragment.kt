package com.iyr.ian.ui.map.fragments

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.iyr.ian.AppConstants.Companion.SLIDE_FRAGMENT_HIDE_SPEED
import com.iyr.ian.AppConstants.Companion.SLIDE_FRAGMENT_SHOW_SPEED
import com.iyr.ian.R
import com.iyr.ian.dao.models.Event
import com.iyr.ian.dao.models.EventFollower
import com.iyr.ian.dao.models.EventLocationType
import com.iyr.ian.dao.repositories.EventFollowersRepository
import com.iyr.ian.databinding.FragmentUsersGoingBinding
import com.iyr.ian.receivers.EventChangesReceiver
import com.iyr.ian.ui.map.MapSituationFragment
import com.iyr.ian.ui.map.adapters.UsersGoingAdapter
import com.iyr.ian.ui.map.event_header.EventHeaderFragment
import com.iyr.ian.ui.map.event_header.adapter.EventHeaderCallback
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.connectivity.NetworkStateReceiver
import com.iyr.ian.utils.openNavigatorTo
import com.iyr.ian.viewmodels.MapSituationFragmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class UsersGoingFragment(
    val parentFragment: MapSituationFragment,
    val mapSituationFragmentViewModel: MapSituationFragmentViewModel
) : Fragment(),
    NetworkStateReceiver.NetworkStateReceiverListener,
    DialogInterface.OnClickListener {

    private var me: EventFollower? = null
    private var isBussy: Boolean = false
    private var hasConnectivity: Boolean? = null
    private lateinit var networkStateReceiver: NetworkStateReceiver
    private var eventChangesReceiver: EventChangesReceiver? = null
    private var referenceView: View? = null
    private var popupView: View? = null
    private var eventKey: String? = null
    private val adapter: UsersGoingAdapter by lazy { UsersGoingAdapter(parentFragment.requireActivity()) }
    lateinit var eventData: Event

    private val globalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {

            var buttonRect = Rect()
            binding?.goButton?.getGlobalVisibleRect(buttonRect)
            var goButtonHeight = buttonRect.height()


            val currentEvent = mapSituationFragmentViewModel.lastEventUpdate
            currentEvent?.let { event ->
                if (event.author?.author_key?.compareTo(FirebaseAuth.getInstance().uid.toString()) == 0 &&
                    event.event_location_type.compareTo(EventLocationType.REALTIME.name) == 0
                ) {
                    binding?.buttonGoingStatus?.visibility = View.GONE
                    var goButtonParams = binding?.goButton?.layoutParams
                    goButtonParams = LinearLayout.LayoutParams(
                        goButtonHeight,
                        goButtonHeight,
                        1f
                    )
                    binding?.goButton?.layoutParams = goButtonParams
                    // modify the button layout weight to 1
                } else {
                    binding?.buttonGoingStatus?.visibility = View.VISIBLE
                    var goButtonParams = binding?.goButton?.layoutParams
                    goButtonParams = LinearLayout.LayoutParams(
                        goButtonHeight,
                        goButtonHeight,
                        0f
                    )
                    binding?.goButton?.layoutParams = goButtonParams
                }
                binding?.goButton?.forceLayout()
            }


            Log.d("REFRESH", "Entro al global layout")
            //   refreshTimes++
            //    Log.d("GLOBAL_LAYOUT", refreshTimes.toString())

            // Tomo los datos de la pantalla total.
            binding?.root?.viewTreeObserver?.removeOnGlobalLayoutListener(this)


        }
    }


    private var binding: FragmentUsersGoingBinding? = null

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
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUsersGoingBinding.inflate(layoutInflater, container, false)

        binding?.buttonGoingStatus?.setOnClickListener {

            updateGoingStatusButton(me?.going_time == null)


            mapSituationFragmentViewModel.onToggleGoingStatus(
                FirebaseAuth.getInstance().uid.toString(),
                eventKey!!
            )
        }

        setupUI()
        //  hideBottomSheet()
        openBottomSheet()
        return binding?.root
    }

    private fun hideBottomSheet() {
    }

    private fun openBottomSheet() {
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        displayMetrics.widthPixels
        val anim =
            ValueAnimator.ofInt(binding?.layoutCard?.measuredHeight!!, (height * .8).toInt())
        anim.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            val layoutParams: ViewGroup.LayoutParams = binding?.layoutCard?.layoutParams!!
            layoutParams.height = `val`
            binding?.layoutCard?.layoutParams = layoutParams
        }
        anim.addListener(object : Animator.AnimatorListener {

            override fun onAnimationStart(p0: Animator) {
                binding?.layoutCard?.visibility = View.VISIBLE
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
            binding?.layoutCard?.measuredHeight!!, 0
        )
        anim.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            val layoutParams: ViewGroup.LayoutParams = binding?.layoutCard!!.layoutParams
            layoutParams.height = `val`
            binding?.layoutCard?.layoutParams = layoutParams
        }
        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {

            }

            override fun onAnimationEnd(p0: Animator) {
                binding?.layoutCard?.visibility = View.GONE
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
            EventHeaderFragment(
                this as EventHeaderCallback,
                event,
                mapSituationFragmentViewModel
            ).apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    override fun onResume() {
        registerNetworkBroadcastReceiver(requireContext())
        super.onResume()
        updateUI()
        binding?.root?.viewTreeObserver?.addOnGlobalLayoutListener(
            globalLayoutListener
        )
        setupObservers()
    }


    override fun onPause() {
        unregisterNetworkBroadcastReceiver(requireContext())
        removeObservers()
        super.onPause()

    }


    private fun setupObservers() {

        lifecycleScope.launch(Dispatchers.Main) {
            adapter.getData().clear()
            mapSituationFragmentViewModel.followersList.forEach { follower ->
                if (follower.user_key != FirebaseAuth.getInstance().uid.toString()) {
                    if (follower.going_time != null)
                        adapter.getData().add(follower)
                } else {
                    me = follower
                    updateGoingStatusButton(me?.going_time != null)
                }
            }
            adapter.notifyDataSetChanged()

            mapSituationFragmentViewModel.eventFollowersConnector.collect { event ->
                processFollowersEvents(event)
            }

        }

    }


    /**
     * Procesa los eventos de la lista de seguidores.
     * Se lo llama tanto tomando el cache de los anteriores como asi tambien con los que vengan
     * despues de inicializado
     */
    private fun UsersGoingFragment.processFollowersEvents(event: EventFollowersRepository.EventFollowerDataEvent?) {
        when (event) {
            is EventFollowersRepository.EventFollowerDataEvent.OnChildAdded -> {
                var follower = event.data!!
                var followerIndex = getFollowerIndex(follower)
                if (follower.user_key != FirebaseAuth.getInstance().uid.toString()) {
                    if (follower.going_time != null && followerIndex == -1) {
                        adapter.getData().add(follower)
                    }
                } else {
                    me = follower
                    updateGoingStatusButton(me?.going_time != null)
                }
            }

            is EventFollowersRepository.EventFollowerDataEvent.OnChildChanged -> {
                var follower = event.data!!
                var followerIndex = getFollowerIndex(follower)
                if (follower.user_key != FirebaseAuth.getInstance().uid.toString()) {
                    if (follower.going_time != null) {
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
                    updateGoingStatusButton(me?.going_time != null)
                }
            }

            is EventFollowersRepository.EventFollowerDataEvent.OnChildMoved -> TODO()
            is EventFollowersRepository.EventFollowerDataEvent.OnChildRemoved -> {
            }

            is EventFollowersRepository.EventFollowerDataEvent.OnError -> TODO()
            null -> TODO()
        }
    }

    /*
            mapSituationFragmentViewModel.eventFollowersConnector.observe(viewLifecycleOwner) { list ->
                adapter.getData().clear()
                var eventAuthorKey: String? = null
                list.forEach { follower ->
                    if (follower.is_author)
                        eventAuthorKey = follower.user_key

                    if (FirebaseAuth.getInstance().uid.toString().compareTo(follower.user_key) != 0) {

                        if (follower.going_time != null)
                            adapter.getData().add(follower)
                        else {
                            if (adapter.getData().contains(follower)) {
                                adapter.getData().remove(follower)
                            }
                        }
                    } else {
                        me = follower
                        updateGoingStatusButton(me?.going_time != null)
                    }

                }
                adapter.notifyDataSetChanged()


            }
    */


    private fun removeObservers() {
        mapSituationFragmentViewModel.followers.removeObservers(viewLifecycleOwner)
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


    /**
     * Devuelve el indice de una key de usuario en el array que puebla el adapter
     */
    fun getFollowerIndex(userKey: String): Int {
        var iter = -1
        var index = -1
        adapter.getData().forEach { record ->
            iter++
            if (record.user_key == userKey) {
                index = iter
                return@forEach
            }
        }
        return index
    }


    private fun updateUI() {


    }

    private fun setupUI() {


        binding?.layoutCard?.visibility = View.GONE
        binding?.recyclerView?.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding?.recyclerView?.adapter = adapter


        binding?.goButton?.setOnClickListener {
            requireActivity().openNavigatorTo(
                LatLng(
                    eventData.location?.latitude!!,
                    eventData.location?.longitude!!
                )
            )
        }
        binding?.closeButton?.setOnClickListener {
            requireActivity().handleTouch()
            closeBottomSheet()
        }


    }

    private fun updateGoingStatusButton(imGoing: Boolean) {
        if (!imGoing)
            binding?.buttonGoingStatus?.setText(R.string.im_going)
        else
            binding?.buttonGoingStatus?.setText(R.string.im_not_going)

    }

    private fun initAdapter() {
        //       registerReceivers()
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


    private fun registerReceivers() {
        if (eventChangesReceiver == null) {
            eventChangesReceiver = object : EventChangesReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    super.onReceive(context, intent)
                    val data = intent?.extras
                    if (data != null) {
                        val event: Event = data.get("data") as Event
                        updateAdapter(event.viewers!!)
                    }
                }
            }

        }


        Log.d("RECEIVER", "Class" + this.javaClass.name + " Registro Receiver ")
        val intentFilter = IntentFilter()
        intentFilter.addAction("CHANGES_IN_EVENT")
        LocalBroadcastManager.getInstance(parentFragment.requireActivity()).registerReceiver(
            eventChangesReceiver!!,
            intentFilter
        )
    }

    fun unRegisterReceivers() {
        Log.d("RECEIVER", "Class" + this.javaClass.name + " desRegistro Receiver ")
        if (eventChangesReceiver != null) {
            try {
                parentFragment.requireActivity().unregisterReceiver(eventChangesReceiver)
            } catch (ex: Exception) {
                Log.d("RECEIVER", "EL Receiver no se puede desregistrar correctamente")
            }
            eventChangesReceiver = null
        }

    }

    fun updateAdapter(viewers: HashMap<String, EventFollower>) {
        val adapterData = adapter.getData()
        var imGoing: Boolean = false
        adapterData.forEach { viewer ->
            if (!viewers.contains(viewer.user_key) || viewers[viewer.user_key]!!.going_time == null) {
                if (viewer.user_key != FirebaseAuth.getInstance().uid.toString()) {
                    try {
                        val index = adapterData.indexOf(viewers[viewer.user_key]!!)
                        adapterData.remove(viewers[viewer.user_key]!!)
                        adapter.notifyItemRemoved(index)
                    } catch (ex: Exception) {
                        Toast.makeText(requireContext(), ex.localizedMessage, Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        }

        viewers.forEach { (key, value) ->
            val eventFollower: EventFollower = value
            if (eventFollower.user_key != FirebaseAuth.getInstance().uid.toString())  // Si es otro usuario
            {
                if (!adapterData.contains(eventFollower)) {
                    if (eventFollower.going_time != null) {
                        adapterData.add(eventFollower)
                        adapter.notifyItemInserted(adapterData.size - 1)
                    }
                } else {
                    val index = adapterData.indexOf(eventFollower)
                    if (eventFollower.going_time != null) {
                        adapterData[index] = eventFollower
                        adapter.notifyItemChanged(index)
                    } else {
                        adapterData.removeAt(index)
                        adapter.notifyItemRemoved(index)
                    }
                }
            } else {
                imGoing = eventFollower.going_time != null
            }
            updateGoingStatusButton(imGoing)
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

