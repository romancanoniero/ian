package com.iyr.ian.ui.settings.subscription_plan

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.iyr.ian.R
import com.iyr.ian.dao.models.SubscriptionPlans
import com.iyr.ian.dao.models.SubscriptionTypes
import com.iyr.ian.databinding.FragmentPlanSubscriptionBinding
import com.iyr.ian.enums.IANModulesEnum
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.ui.interfaces.MainActivityInterface
import com.iyr.ian.ui.settings.ISettingsFragment
import com.iyr.ian.ui.settings.subscription_plan.adapters.ISubscriptionPlansAdapter
import com.iyr.ian.ui.settings.subscription_plan.adapters.ISubscriptionTypesAdapter
import com.iyr.ian.ui.settings.subscription_plan.adapters.SubscriptionPlansAdapter
import com.iyr.ian.ui.settings.subscription_plan.adapters.SubscriptionTypesAdapter
import com.iyr.ian.ui.signup.subscription_plan.SubscriptionPlanViewModel
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.loaders.hideLoader
import com.iyr.ian.utils.loaders.showLoader
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.utils.showSuccessDialog


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


@SuppressLint("MissingPermission")
class PlanSubscriptionFragment(private val _interface: ISettingsFragment) : Fragment(),
    ISubscriptionPlansAdapter, ISubscriptionTypesAdapter {
    private val viewModel: SubscriptionPlanViewModel by viewModels()

    private var adapter: SubscriptionPlansAdapter? = null
    private var subscriptionTypesAdapter: SubscriptionTypesAdapter? = null
    private lateinit var binding: FragmentPlanSubscriptionBinding

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("EVENT_CREATION", this.javaClass.name)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        subscriptionTypesAdapter = SubscriptionTypesAdapter(this.requireContext(), this)
        adapter = SubscriptionPlansAdapter(requireContext(), this)

    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPlanSubscriptionBinding.inflate(layoutInflater, container, false)
        setupUI()
        updateUI()
        binding.recyclerSubscriptionType.scrollToPosition(1)
        return binding.root
    }


    companion object {
        @JvmStatic
        fun newInstance(context: Context, _interface: ISettingsFragment) =
            PlanSubscriptionFragment(_interface)
    }

    @SuppressLint("MissingPermission")
    private fun setupUI() {

        // Usa SnapHelper para centrar el ítem actual
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerSubscriptionType)

        binding.recyclerSubscriptionType.adapter = subscriptionTypesAdapter
        binding.subscriptionTypeDotsIndicator.attachToRecyclerView(binding.recyclerSubscriptionType);
        binding.recyclerSubscriptionType.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val view = snapHelper.findSnapView(recyclerView.layoutManager)
                    val position = recyclerView.layoutManager?.getPosition(view!!)
                    // 'position' es la posición del ítem actualmente en foco
                    var subscriptionKey =
                        subscriptionTypesAdapter!!.getData()[position!!].subscription_type_key
                    viewModel.onSubscriptionTypeSelected(subscriptionKey)
                }
            }
        })

        binding.recycler.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recycler.adapter = adapter

    }

    private fun updateUI() {
    }


    override fun onResume() {
        super.onResume()
        if (requireActivity() is MainActivityInterface) {
            (requireActivity() as MainActivityInterface).setToolbarTitle(getString(R.string.my_plan))
        }
        startObservers()

        binding.recyclerSubscriptionType.getViewTreeObserver().addOnGlobalLayoutListener {
            val height = binding.recyclerSubscriptionType.height
           if (height > 0) {
               onSubscriptionTypesAdapterRendered(height)
           }
        }
    }

    override fun onSubscriptionTypesAdapterRendered(maxHeight: Int) {
        val desiredHeight = maxHeight // Reemplaza esto con la altura deseada

        binding.recyclerSubscriptionType.children.forEach { view ->

            val container = view.findViewById<View>(R.id.container)
            val containerParams = container.layoutParams
            containerParams.height = desiredHeight
            container.layoutParams = containerParams
            container.forceLayout()


            val mainCardView = view.findViewById<View>(R.id.main_card_view)
            val mainCardViewParams = mainCardView.layoutParams
            mainCardViewParams.height = desiredHeight
            mainCardView.layoutParams = mainCardViewParams
            mainCardView.forceLayout()
        }

    }

    override fun onPause() {
        super.onPause()
        stopObservers()
    }

    private fun startObservers() {
        viewModel.subscriptionTypeslist.observe(viewLifecycleOwner, { resource ->
            when (resource) {
                is Resource.Loading -> {
                    requireActivity().showLoader()
                }

                is Resource.Success -> {
                    requireActivity().hideLoader()
                    var response = resource.data as List<SubscriptionTypes>
                    subscriptionTypesAdapter?.setData(response)
                    viewModel.onSubscriptionTypeSelected(response[0].subscription_type_key)
                }

                is Resource.Error -> {
                    requireActivity().hideLoader()
                    requireActivity().showErrorDialog(resource.message.toString())
                }
            }
        })


        viewModel.subscriptionPlansList.observe(viewLifecycleOwner, { resource ->
            when (resource) {
                is Resource.Loading -> {
                    requireActivity().showLoader()
                }

                is Resource.Success -> {
                    requireActivity().hideLoader()
                    var response = resource.data as List<SubscriptionPlans>
                    adapter?.setData(response)
                    adapter?.notifyDataSetChanged()
                }

                is Resource.Error -> {
                    requireActivity().hideLoader()
                    requireActivity().showErrorDialog(resource.message.toString())
                }
            }
        })

        viewModel.buyingStatus.observe(viewLifecycleOwner, { resource ->
            when (resource) {
                is Resource.Loading -> {
                    requireActivity().showLoader()
                }

                is Resource.Success -> {
                    requireActivity().hideLoader()
                    requireActivity().showSuccessDialog(null,
                        getString(R.string.your_suscription_was_updated),
                        null,
                        object : OnClickListener{
                            override fun onClick(v: View?) {
                                (requireActivity() as MainActivity).switchToModule(IANModulesEnum.MAIN.ordinal, "Home")
                            }
                        })
                    viewModel.resetBuyingStatus()
                }

                is Resource.Error -> {
                    requireActivity().hideLoader()
                    requireActivity().showErrorDialog(resource.message.toString())
                }

                else -> {}
            }
        })

        viewModel.fetchSubscriptionTypesList()
    }

    private fun stopObservers() {
        viewModel.subscriptionTypeslist.removeObservers(viewLifecycleOwner)
    }

    override fun onBuy(plan: SubscriptionPlans) {
        //    TODO("Not yet implemented")
        viewModel.onBuy(plan)
    }


    override fun onSelected(plan: SubscriptionPlans) {
//        _interface.onSubscriptionPlanSelected(plan)
    }
}