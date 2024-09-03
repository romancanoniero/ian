package com.iyr.ian.ui.map.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iyr.ian.R
import com.iyr.ian.dao.models.Event
import com.iyr.ian.databinding.FragmentUsersGoingBinding
import com.iyr.ian.ui.map.adapters.UsersGoingAdapter
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.openNavigatorTo
import com.iyr.ian.viewmodels.MapSituationFragmentViewModel
import com.iyr.ian.viewmodels.UserViewModel


class UsersGoingFragment() : BottomSheetDialogFragment() {
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var eventKey: String? = null
    private val adapter: UsersGoingAdapter by lazy { UsersGoingAdapter(requireActivity()) }
    lateinit var eventData: Event
    private var viewModel: MapSituationFragmentViewModel =
        MapSituationFragmentViewModel.getInstance()

    private lateinit var binding: FragmentUsersGoingBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentUsersGoingBinding.inflate(layoutInflater, container, false)

        binding.layoutCard.visibility = View.GONE
        binding.recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = adapter


        binding.goButton.setOnClickListener {
            viewModel.lastEventUpdate.let { event ->
                requireActivity().openNavigatorTo(
                    LatLng(
                        event.location?.latitude!!,
                        event.location?.longitude!!
                    )
                )
            }
        }
        binding.closeButton.setOnClickListener {
            requireActivity().handleTouch()
            findNavController().popBackStack()
        }

        val meAsUser = UserViewModel.getInstance().getUser()!!
        viewModel.lastEventUpdate.let { event ->
        binding.buttonAction.setBackgroundResource(R.drawable.primary_button_border)
            if ((event.author?.author_key
                    ?: "") == meAsUser.user_key && event.event_location_type == "REALTIME"
            ) {
                binding.buttonAction.visibility = View.GONE
                binding.goButton.visibility = View.GONE
                binding.buttonAction.setOnClickListener(null)
            } else {
                binding.buttonAction.setOnClickListener {
/*
                    val meAsFollower =
                        viewModel.followers.value?.find { it.user_key == meAsUser.user_key }
                    updateGoingStatusButton(meAsFollower?.going_time == null)
                 */
                    viewModel.onToggleGoingStatus(
                        meAsUser.user_key,
                        event.event_key
                    )
                }

            }
        }


        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // Handle state changes if needed
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                adjustRecyclerViewHeight(slideOffset)
            }
        })

        binding.bottomSheet.visibility = View.VISIBLE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun adjustRecyclerViewHeight(slideOffset: Float) {
        val params = binding.recyclerView.layoutParams
        params.height =
            (bottomSheetBehavior.peekHeight + (bottomSheetBehavior.maxHeight - bottomSheetBehavior.peekHeight) * slideOffset).toInt()
        binding.recyclerView.layoutParams = params
    }

    override fun onResume() {
        startObservers()
        super.onResume()
    }


    override fun onPause() {
        stopObservers()
        super.onPause()
    }


    private fun startObservers() {
        viewModel.followers.observe(viewLifecycleOwner) { list ->
            adapter.getData().clear()
            list.forEach { follower ->
                if ((UserViewModel.getInstance()
                        .getUser()?.user_key ?: "").compareTo(follower.user_key) != 0
                ) {
                    if (follower.going_time != null)
                        adapter.getData().add(follower)
                }
                else
                {
                    updateGoingStatusButton(follower.going_time != null)
                }
            }
            adapter.notifyDataSetChanged()

        }

    }


 
    private fun stopObservers() {
        viewModel.followers.removeObservers(viewLifecycleOwner)
    }


  


    private fun updateGoingStatusButton(imGoing: Boolean) {
        if (!imGoing)
            binding.buttonAction.setText(R.string.im_going)
        else
            binding.buttonAction.setText(R.string.im_not_going)

    }

    fun setEventKey(eventKey: String) {
        this.eventKey = eventKey
    }


}

