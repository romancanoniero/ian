package com.iyr.ian.ui.preloader.pager_adapter.pager_adapter.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iyr.ian.dao.models.Event
import com.iyr.ian.databinding.ItemPreloaderPage4AdapterBinding


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PreloaderPageFourFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PreloaderPageFourFragment(context: Context) : Fragment() {

    var eventData: Event = Event()

    private lateinit var binding: ItemPreloaderPage4AdapterBinding

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    fun PreloaderPageFourFragment() {

    }

    init {
        //       adapter= FriendsAdapter(AppClass.instance)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        binding = ItemPreloaderPage4AdapterBinding.inflate(layoutInflater)
        setupUI()
        updateUI()

        return binding.root
    }


    companion object {


        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param event Parameter 1.
         * @param param1 Parameter 2.
         * @param param2 Parameter 3.
         * @return A new instance of fragment FriendsFragment.
         */
        // TODO: Rename and change types and number of parameters


        @JvmStatic
        fun newInstance(event: Event, param1: String, param2: String) =
            PreloaderPageFourFragment(this as Context).apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    @SuppressLint("MissingPermission")
    private fun updateUI() {

    }

    private fun setupUI() {


    }


    fun setData(event: Event) {
        eventData = event


        //   viewersAdapter.notifyDataSetChanged()
        updateUI()
    }

    fun setEventKey(eventKey: String) {

    }

}