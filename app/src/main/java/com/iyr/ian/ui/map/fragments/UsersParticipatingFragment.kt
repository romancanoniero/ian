package com.iyr.ian.ui.map.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iyr.ian.R
import com.iyr.ian.dao.models.Event
import com.iyr.ian.databinding.FragmentUsersParticipatingBinding
import com.iyr.ian.ui.base.PulseRequestTarget
import com.iyr.ian.ui.map.adapters.UsersParticipatingAdapter
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.viewmodels.MapSituationFragmentViewModel
import com.iyr.ian.viewmodels.UserViewModel

class UsersParticipatingFragment : BottomSheetDialogFragment() {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private var eventKey: String? = null
    private val adapter: UsersParticipatingAdapter by lazy { UsersParticipatingAdapter(requireActivity()) }
    lateinit var eventData: Event
    private lateinit var binding: FragmentUsersParticipatingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentUsersParticipatingBinding.inflate(layoutInflater, container, false)

        binding.layoutCard.visibility = View.GONE
        binding.recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = adapter

        binding.buttonAction.setOnClickListener {

            val currentEvent = MapSituationFragmentViewModel.getInstance().eventFlow.value?.data!!
            val userKey = UserViewModel.getInstance().getUser()?.user_key
            val bundle = Bundle()

            if (currentEvent.author?.author_key.toString() == userKey) {
                // si el presente usuario es el autor del evento
                bundle.putSerializable("action", PulseRequestTarget.VALIDATION_BEFORE_CLOSE_EVENT)
                bundle.putSerializable("user_key", userKey)
                bundle.putSerializable("event_key", currentEvent.event_key)
                findNavController().navigate(R.id.validatorDialog, bundle)
            } else {
                var pp = 3
//                aca, salir del evento
            }

        }

        binding.closeButton.setOnClickListener {
            requireActivity().handleTouch()
            findNavController().popBackStack()
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
        super.onPause()
        removeObservers()
    }

    val viewModel = MapSituationFragmentViewModel.getInstance()

    private fun startObservers() {


        viewModel.followers.observe(viewLifecycleOwner) { list ->
            adapter.getData().clear()
            var eventAuthorKey: String? = null
            list.forEach { follower ->
                if (follower.is_author) eventAuthorKey = follower.user_key
                if (
                    follower.user_key.compareTo(UserViewModel.getInstance().getUser()?.user_key?:"") != 0
                ) adapter.getData().add(follower)
            }
            adapter.notifyDataSetChanged()

            if (eventAuthorKey == UserViewModel.getInstance().getUser()?.user_key) {
                binding.buttonAction.text = getString(R.string.close_event)
            } else binding.buttonAction.text = getString(R.string.leave_event)

        }

    }

    private fun removeObservers() {
        viewModel.followers.removeObservers(viewLifecycleOwner)
    }


}

