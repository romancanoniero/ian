package com.iyr.ian.ui.map.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iyr.ian.R
import com.iyr.ian.dao.models.Event
import com.iyr.ian.databinding.FragmentUsersThatAlreadyCalledBinding
import com.iyr.ian.ui.map.adapters.UserWhoCalledAdapter
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.makeAPhoneCall
import com.iyr.ian.viewmodels.MapSituationFragmentViewModel
import com.iyr.ian.viewmodels.UserViewModel


class UsersThatCalledFragment : BottomSheetDialogFragment() {

    private var eventKey: String? = null
    private val adapter: UserWhoCalledAdapter by lazy { UserWhoCalledAdapter(requireActivity()) }
    lateinit var eventData: Event
    private val viewModel: MapSituationFragmentViewModel =  MapSituationFragmentViewModel.getInstance()

    private lateinit var binding: FragmentUsersThatAlreadyCalledBinding


    init {
        initAdapter()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val meAsUser = UserViewModel.getInstance().getUser()!!
        // Inflate the layout for this fragment
        binding = FragmentUsersThatAlreadyCalledBinding.inflate(layoutInflater, container, false)

        binding.recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = adapter

        binding.buttonAction.setOnClickListener {
            viewModel.lastEventUpdate.let { event ->
                 viewModel.onToggleCallStatus(
                    meAsUser.user_key,
                    event.event_key
                )
            }
        }

        binding.callButton.setOnClickListener {
            requireActivity().makeAPhoneCall("911")
        }
        binding.closeButton.setOnClickListener {
            requireActivity().handleTouch()
            findNavController().popBackStack()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        startObservers()
    }

    override fun onPause() {
        super.onPause()
        stopObservers()
    }

    private fun startObservers() {

        viewModel.followers.observe(viewLifecycleOwner) { list ->
            adapter.getData().clear()
            list.forEach { follower ->
                if ((UserViewModel.getInstance()
                        .getUser()?.user_key ?: "").compareTo(follower.user_key) != 0
                ) {
                    if (follower.call_time != null)
                        adapter.getData().add(follower)
                }
                else
                {
                    updateCallStatusButton(follower.call_time != null)
                }
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun stopObservers() {
        viewModel.followers.removeObservers(viewLifecycleOwner)
    }



    private fun updateCallStatusButton(didICall: Boolean) {

        if (::binding.isInitialized) {
            if (!didICall)
                binding.buttonAction.setText(R.string.i_already_call)
            else
                binding.buttonAction.setText(R.string.i_didnt_call_yet)
        }
    }


    private fun initAdapter() {


    }


    fun setEventKey(eventKey: String) {
        this.eventKey = eventKey
    }



}

