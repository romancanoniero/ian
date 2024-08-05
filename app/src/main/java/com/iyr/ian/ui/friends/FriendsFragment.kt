package com.iyr.ian.ui.friends


import android.app.Activity
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.OnTouchListener
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.iyr.fewtouchs.ui.views.home.fragments.friends.adapters.FriendsAdapter
import com.iyr.ian.AppConstants
import com.iyr.ian.R
import com.iyr.ian.app.AppClass
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.dao.models.Contact
import com.iyr.ian.dao.models.UserMinimum
import com.iyr.ian.dao.repositories.ContactsRepository
import com.iyr.ian.databinding.FragmentFriendsBinding
import com.iyr.ian.sharedpreferences.SessionForProfile
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.ui.friends.adapters.SearchUsersListAdapter
import com.iyr.ian.ui.friends.enums.FriendshipStatusEnums
import com.iyr.ian.ui.interfaces.FriendsFragmentInterface
import com.iyr.ian.utils.UIUtils
import com.iyr.ian.utils.Validators
import com.iyr.ian.utils.broadcastMessage
import com.iyr.ian.utils.capitalizeWords
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.hideKeyboard
import com.iyr.ian.utils.px
import com.iyr.ian.utils.share
import com.iyr.ian.utils.sharing_app.SharingContents
import com.iyr.ian.utils.sharing_app.SharingTargets
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.utils.showSnackBar
import com.iyr.ian.viewmodels.FriendsFragmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

interface FriendsFragmentCallback {
    fun onContactAdded(snapshot: DataSnapshot, previousChildName: String?)
    fun onContactChanged(snapshot: DataSnapshot, previousChildName: String?)
    fun onContactRemoved(snapshot: DataSnapshot)
    fun showDialog(title: String, message: String, actionButton: String)
    fun onContactInvitationSent(user: Contact)
    fun clearSearchBox()
    fun inviteNewFriend(invitationText: String, sharingInfo: SharingContents)
    fun inviteNewFriend(invitationText: String)
    fun inviteExistingUser(contact: Contact)
    fun contactRemove(contact: Contact)
    fun cancelInvitation(contact: Contact)
    fun contactRequestAccept(contact: Contact)
    fun contactRefuse(contact: Contact, notificationKey: String) {}
    fun contactRefuse(contact: Contact)
    fun onContactChangedInternal(contact: Contact)
}

/**
 * A simple [Fragment] subclass.
 * Use the [FriendsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FriendsFragment(val activity: Activity) : Fragment(),

    FriendsFragmentInterface,
    FriendsFragmentCallback {


    val drawableLeft = 0
    val drawableTop = 1
    val drawableRight = 2
    val drawableBottom = 3

    private var keyboardWasShownOnce: Boolean = false
    private var invisibleViewBottomDefault: Int? = null

    private val onGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        var keyboardVisible = false
        val r = Rect()
        this.binding.root.getGlobalVisibleRect(r)

        if (this.invisibleViewBottomDefault == null) {
            val transparentViewRect = Rect()
            this.binding?.transparentView?.getGlobalVisibleRect(transparentViewRect)
            this.invisibleViewBottomDefault = this.binding?.transparentView?.bottom
            keyboardWasShownOnce = false
        } else {
            val transparentViewRect = Rect()
            this.binding.transparentView?.getGlobalVisibleRect(transparentViewRect)
            keyboardVisible =
                this.invisibleViewBottomDefault!! >= this.binding.transparentView?.bottom!!
        }
        if (keyboardVisible) {
            this.requireActivity().currentFocus?.let { objeto ->
                val keypadHeight = this.binding.root.height!! - r.bottom
                val scrollY = (objeto.bottom + keypadHeight) - r.bottom
                /*
                      this.binding?.avatarArea?.layoutParams?.height = 80.px
                      this.binding?.avatarArea?.requestLayout()
                      this.binding?.scrollView?.smoothScrollTo(0, scrollY)
                      this.binding?.confirmButton?.visibility = View.GONE
                  */
                keyboardWasShownOnce = true
            }
        } else {
            if (keyboardWasShownOnce == true) {
                /*
                   this.binding?.confirmButton?.visibility = View.VISIBLE
                   this.binding.avatarArea?.layoutParams?.height =
                       this.requireContext().resources.getDimension(R.dimen.box_xsuperbig).toInt()
                   this.binding?.avatarArea?.requestLayout()
              */
            }
        }
    }


    private var isInForeground: Boolean = false

    //  private var mPresenter: FriendsFragmentPresenter = FriendsFragmentPresenter(this, this)
    private lateinit var binding: FragmentFriendsBinding
    private var viewModel = FriendsFragmentViewModel(FirebaseAuth.getInstance().uid.toString())

    private val usersRecyclerViewAdapter: FriendsAdapter by lazy {
        FriendsAdapter(
            activity,
            activity,
            this
        )
    }
    private lateinit var autocompleteTextAdapter: SearchUsersListAdapter

    //    private var usersSearchAdapter: CustomCompleteTextViewAdapter? = null
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FriendsFragment.
         */
        // TODO: Rename and change types and number of parameters


        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FriendsFragment(AppClass.instance.getCurrentActivity()!!).apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    constructor() : this(AppClass.instance.getCurrentActivity()!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createAnimatedLoader()
    }

    val loaderAnimation = AnimationDrawable()

    private fun createAnimatedLoader() {
        try {
            val assets = requireContext().assets
            val frames =
                assets.list("loader_frames") // Asume que tus imágenes están en la carpeta "loader_frames" dentro de "assets"

            if (frames != null) {
                for (frame in frames) {
                    val assetFileDescriptor = assets.openFd("loader_frames/$frame")
                    val bitmapDrawable = BitmapDrawable(
                        resources,
                        BitmapFactory.decodeStream(assetFileDescriptor.createInputStream())
                    )
                    loaderAnimation.addFrame(bitmapDrawable, 100) // Asume que cada frame dura 100ms
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    var originalSearchDrawables: Array<Drawable>? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        requireContext().broadcastMessage(
            null,
            AppConstants.ServiceCode.BROADCAST_ACTION_HIDE_FOOTER_TOOLBAR
        )
        binding = FragmentFriendsBinding.inflate(layoutInflater, container, false)
        setupUI()
        isInForeground = true

        originalSearchDrawables = binding.searchBox.compoundDrawablesRelative
        return binding.root
    }


    override fun onResume() {
        super.onResume()
        AppClass.instance.setCurrentFragment(this)
//        (requireActivity() as MainActivity).setTitleBarTitle(R.string.friends)

        val appToolbar = (requireActivity() as MainActivity).appToolbar
        appToolbar.enableBackBtn(true)
        appToolbar.updateTitle(getString(R.string.friends))

        val bottomToolBar = (requireActivity() as MainActivity).binding.bottomToolbar
        bottomToolBar.visibility = View.GONE



        this.binding.root.viewTreeObserver.addOnGlobalLayoutListener(this.onGlobalLayoutListener)

        AppClass.instance.setSoftMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)





        if (userVisibleHint) {
            startObservers()
        }
    }

    private fun startObservers() {

    viewModel.updateOperation.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Loading -> {
                    showTextLoader()
                }

                is Resource.Success -> {
                    hideTextLoader()
                }

                is Resource.Error -> {
                    hideTextLoader()
                    requireActivity().showErrorDialog(it.message!!)
                }

                null -> {}
            }
        })

        viewModel.contactsFlow.observe(viewLifecycleOwner, { resource ->
            var data = usersRecyclerViewAdapter.getData()
            when (resource) {
                is ContactsRepository.DataEvent.ChildAdded -> {
                    if (data.contains(resource.data)) {
                        data[data.indexOf(resource.data)] = resource.data
                        usersRecyclerViewAdapter.notifyItemChanged(data.indexOf(resource.data))
                    } else {
                        data.add(resource.data)
                        usersRecyclerViewAdapter.notifyItemInserted(data.size - 1)
                    }
                    hideNoRecordLabel()

                }

                is ContactsRepository.DataEvent.ChildChanged -> {
                    if (data.contains(resource.data)) {
                        var index = data.indexOf(resource.data)
                        data[index] = resource.data
                        usersRecyclerViewAdapter.notifyItemChanged(index)
                    }
                }

                is ContactsRepository.DataEvent.ChildRemoved -> {
                    if (data.contains(resource.data)) {
                        var index = data.indexOf(resource.data)
                        data.removeAt(index)
                        usersRecyclerViewAdapter.notifyItemRemoved(index)
                    }
                }

                is ContactsRepository.DataEvent.onChildMoved -> {

                }
            }

        })

        viewModel.autoCompleteSearch.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Loading -> {
                    showTextLoader()

                }

                is Resource.Success -> {
                    hideTextLoader()
                    lifecycleScope.launch(Dispatchers.Main) {
                        autocompleteTextAdapter.updateDate(
                            binding.searchBox.text.toString(),
                            it.data!!
                        )
                        //             autocompleteTextAdapter.addAll(it.data!!)
                    }
                }

                is Resource.Error -> {
                    hideTextLoader()
                    requireActivity().showErrorDialog(it.message!!)
                }
            }
        })
    }


    override fun onPause() {
        super.onPause()
        this.binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this.onGlobalLayoutListener)
        AppClass.instance.restoreSoftMode()
        stopObservers()
    }

    private fun stopObservers() {
        viewModel.contactsFlow.removeObservers(viewLifecycleOwner)
        viewModel.autoCompleteSearch.removeObservers(viewLifecycleOwner)
    }


    private fun setupUI() {

        if (usersRecyclerViewAdapter.getData().size == 0)
            showNoRecordLabel()
        else
            hideNoRecordLabel()

        autocompleteTextAdapter = SearchUsersListAdapter(
            requireContext(),
            R.layout.item_user_search_autocomplete
        )

        binding.searchBox.threshold = AppConstants.USERNAME_MINIMUM_LENGTH
        binding.searchBox.setAdapter(autocompleteTextAdapter)
        binding.searchBox.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(p0: AdapterView<*>?, view: View?, index: Int, p3: Long) {
                val contact = autocompleteTextAdapter?.getItem(index)!!
                when (contact.status) {
                    FriendshipStatusEnums.NOT_A_FRIEND_BUT_EXISTS.name -> {
                        contact.let {
                            it.author_key =
                                SessionForProfile.getInstance(requireContext()).getUserId()
                            inviteExistingUser(it)
                        }
                    }

                    FriendshipStatusEnums.USER_NOT_FOUND.name -> {

                        val sharingParams = SharingContents()
                        var invitationType = SharingTargets.GENERIC

                        if (contact.telephone_number != "") {
                            invitationType = SharingTargets.SMS
                            sharingParams.contactAddress = contact.telephone_number ?: ""
                        } else {
                            invitationType = SharingTargets.EMAIL
                            sharingParams.contactAddress = contact.email ?: ""
                            sharingParams.title = "Proba esta aplicacion nueva"
                        }
                        sharingParams.sharingMethod = invitationType
                        generateInvitationLink(sharingParams)
                        //                        Toast.makeText(requireContext(), "Invitar a este usuario a unirse", Toast.LENGTH_SHORT).show()
                    }
                }
                binding.searchBox.text.clear()
            }
        }
        binding.searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {


                if (text.toString().length >= AppConstants.USERNAME_MINIMUM_LENGTH) {
                    binding.searchBoxLowerHint.visibility = GONE
                } else {

                    binding.searchBoxLowerHint.text = String.format(
                        requireContext().getText(R.string.search_box_minimum_lenght)
                            .toString(),
                        AppConstants.USERNAME_MINIMUM_LENGTH,
                        AppConstants.USERNAME_MINIMUM_LENGTH - text.toString().length
                    )
                    binding.searchBoxLowerHint.visibility = VISIBLE
                }
            }

            override fun afterTextChanged(text: Editable?) {

                /*
                        if (text.toString().isNotEmpty()) {
                            binding.searchBox.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                AppCompatResources.getDrawable(
                                    requireContext(),
                                    R.drawable.ic_magnifying_glass
                                ),
                                null,
                                AppCompatResources.getDrawable(requireContext(), R.drawable.ic_text_clear),
                                null
                            )
                            if (text.toString().length == 1) {
                                setupClearTextDrawableButtonListener()
                            } else
                                if (text.toString()
                                        .isNotEmpty() && text.toString().length < AppConstants.USERNAME_MINIMUM_LENGTH
                                ) {
                                    binding.searchBoxLowerHint.text = String.format(
                                        requireContext().getText(R.string.search_box_minimum_lenght)
                                            .toString(),
                                        AppConstants.USERNAME_MINIMUM_LENGTH,
                                        AppConstants.USERNAME_MINIMUM_LENGTH - text.toString().length
                                    )
                                    binding.searchBoxLowerHint.visibility = VISIBLE
                                } else {
                                    binding.searchBoxLowerHint.visibility = GONE

                                    viewModel.onSearchBoxChange(text.toString())
                                }
                        } else {
                            binding.searchBox.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                AppCompatResources.getDrawable(
                                    requireContext(),
                                    R.drawable.ic_magnifying_glass
                                ),
                                null,
                                null,
                                null
                            )
                            removeClearTextDrawableButtonListener()
                        }

                 */
                /*
                                val d = resources.getDrawable(com.iyr.ian.R.drawable.loader_background) as AnimationDrawable
                                binding.searchBox.setCompoundDrawables(d, null, null, null)
                                d.start()
                */

                if (text.toString().length >= AppConstants.USERNAME_MINIMUM_LENGTH) {
                    viewModel.onSearchBoxChange(text.toString().lowercase().capitalizeWords())
                    //         binding.searchBoxLowerHint.visibility = GONE
                } else {
                    /*
                                        binding.searchBoxLowerHint.text = String.format(
                                            requireContext().getText(R.string.search_box_minimum_lenght)
                                                .toString(),
                                            AppConstants.USERNAME_MINIMUM_LENGTH,
                                            AppConstants.USERNAME_MINIMUM_LENGTH - text.toString().length
                                        )
                                        binding.searchBoxLowerHint.visibility = VISIBLE

                     */
                }
            }
        })

        binding.recyclerFriends.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerFriends.adapter = usersRecyclerViewAdapter

        binding.inviteButton.setOnClickListener {
            generateInvitationLink()
        }
    }

    private fun generateInvitationLink() {
        generateInvitationLink(SharingContents(SharingTargets.GENERIC, null, null))
    }

    private fun generateInvitationLink(sharingInfo: SharingContents) {
        val map: java.util.HashMap<String, String> = java.util.HashMap<String, String>()
        map["action"] = AppConstants.DYNAMIC_LINK_ACTION_FRIENDSHIP
        map["key"] = FirebaseAuth.getInstance().uid.toString()
        UIUtils.createShortDynamicLink(requireContext(), map, object :
            OnCompleteCallback {

            override fun onComplete(success: Boolean, shortlink: Any?) {

                var invitationText = String.format(
                    requireContext().getText(R.string.app_installation_invitation_message)
                        .toString(),
                    requireContext().getString(R.string.app_name)
                )

                invitationText =
                    invitationText.plus(System.getProperty("line.separator"))
                invitationText =
                    invitationText.plus(System.getProperty("line.separator"))
                invitationText = invitationText.plus(shortlink)

                inviteNewFriend(invitationText, sharingInfo)
            }

            override fun onError(exception: Exception) {
                requireActivity().showErrorDialog(exception.localizedMessage)
            }
        }
        )


    }

    private fun setupClearTextDrawableButtonListener() {
        binding.searchBox.setOnTouchListener(OnTouchListener { v, event ->
            @Suppress("UNUSED_VARIABLE") val DRAWABLE_LEFT = 0
            @Suppress("DRAWABLE_TOP") val DRAWABLE_TOP = 1
            @Suppress("DRAWABLE_RIGHT") val DRAWABLE_RIGHT = 2
            @Suppress("DRAWABLE_BOTTOM") val DRAWABLE_BOTTOM = 3
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= binding.searchBox.right - binding.searchBox.compoundDrawables[DRAWABLE_RIGHT].bounds.width()
                ) {
                    // your action here

                    binding.searchBox.text.clear()

                    return@OnTouchListener true
                }
            }
            false
        })
    }

    private fun removeClearTextDrawableButtonListener() {
        binding.searchBox.setOnTouchListener(null)
    }


    private fun openSharingFunctions(message: String, sharingInfo: SharingContents) {
        requireActivity().share(message, sharingInfo)

    }

    private fun updateInputTypeMode(text: String) {
        if (Validators.isValidPhoneNumber(text)) {
            if (binding.searchBox.inputType != InputType.TYPE_CLASS_PHONE) {
                binding.searchBox.inputType = InputType.TYPE_CLASS_PHONE
            }
        } else {
            binding.searchBox.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }

    }

    private fun hideTextLoader() {
        binding.loadingIndicator.visibility = GONE
        binding.auxIcon.visibility = VISIBLE

        binding.searchBox.compoundDrawablePadding = 10.px
        binding.searchBox.setCompoundDrawablesWithIntrinsicBounds(originalSearchDrawables?.get(drawableLeft), null, null, null)
        lifecycleScope.launch(Dispatchers.Main) {
            loaderAnimation.stop()
        }

    }

    private fun showTextLoader() {
        binding.auxIcon.visibility = GONE
        binding.loadingIndicator.visibility = VISIBLE

        binding.searchBox.compoundDrawablePadding = 10.px
        binding.searchBox.setCompoundDrawablesWithIntrinsicBounds(originalSearchDrawables?.get(drawableLeft), null, loaderAnimation, null)
        lifecycleScope.launch(Dispatchers.Main) {
            loaderAnimation.start()
        }

    }

    fun inviteUser(user: UserMinimum) {
    }


    fun getData(): ArrayList<Contact> {
        return usersRecyclerViewAdapter.getData()
    }

    @JvmName("getAdapter1")
    fun getAdapter(): FriendsAdapter {
        return usersRecyclerViewAdapter
    }

    private fun hideNoRecordLabel() {
        if (::binding.isInitialized) {
            binding.noFriendsLegend.visibility = GONE
        }
    }

    private fun showNoRecordLabel() {
        if (::binding.isInitialized) {
            binding.noFriendsLegend.visibility = VISIBLE
        }
    }

    override fun onContactInvitationSent(contact: Contact) {
        val title = getString(R.string.done_exclamation)
        val message = String.format(
            getString(R.string.friendship_invitation_message_done),
            contact.display_name.toString()
        )
        val actionButton = getString(R.string.lets_continue)
        showDialog(title, message, actionButton)
    }

    override fun showDialog(title: String, message: String, actionButton: String) {

        requireActivity().showErrorDialog(
            title,
            message,
            actionButton,
            null
        )


    }

    override fun clearSearchBox() {
        requireActivity().hideKeyboard()
        binding.searchBox.text.clear()

    }


    override fun inviteNewFriend(message: String) {
        openSharingFunctions(message, SharingContents(SharingTargets.GENERIC, null, null))
    }

    override fun inviteNewFriend(message: String, sharingInfo: SharingContents) {
        openSharingFunctions(message, sharingInfo)
    }


    // Propio del FriendsFragment
    override fun inviteExistingUser(contact: Contact) {
        binding.textSearch.setText("")
        requireActivity().hideKeyboard()
        contact.status = FriendshipStatusEnums.PENDING.name
        onContactAddedInternal(contact)
        requireActivity().showSnackBar(
            binding.root,
            "Implementar en el ViewModel el metodo contactInvite"
        )
        viewModel.contactInvite(contact)
    }

    override fun contactRemove(contact: Contact) {

        requireActivity().showSnackBar(
            binding.root,
            "Implementar en el ViewModel el metodo onFriendshipRevoked"
        )

        /*
                val confirmationCallback: OnConfirmationButtonsListener =
                    object : OnConfirmationButtonsListener {
                        override fun onAccept() {
                            onContactRemovedInternal(contact)
                            mPresenter.onFriendshipRevoked(contact.user_key!!, object : OnCompleteCallback {
                                override fun onComplete(success: Boolean, result: Any?) {
                                    //TODO("Not yet implemented")
                                }

                                override fun onError(exception: java.lang.Exception) {
                                    super.onError(exception)
                                    onContactAddedInternal(contact)
                                }
                            })
                        }
                    }

                requireActivity().showConfirmationDialog(
                    getString(R.string.delete_friendship),
                    String.format(
                        getString(R.string.delete_friendship_message),
                        contact.display_name
                    ),
                    getString(R.string.yes),
                    getString(R.string.no),
                    confirmationCallback
                )
                */

    }

    override fun cancelInvitation(user: Contact) {

        ///aca---

        requireActivity().showSnackBar(
            binding.root,
            "Implementar en el ViewModel el metodo cancelInvitation"
        )


        /*

        val confirmationCallback: OnConfirmationButtonsListener =
            object : OnConfirmationButtonsListener {
                override fun onAccept() {
                    onContactRemovedInternal(user)
                    mPresenter.cancelInvitation(user.user_key!!, object : OnCompleteCallback {
                        override fun onComplete(success: Boolean, result: Any?) {
                            //TODO("Not yet implemented")
                        }

                        override fun onError(exception: java.lang.Exception) {
                            super.onError(exception)
                            onContactAddedInternal(user)
                        }
                    })

                }
            }

        requireActivity().showConfirmationDialog(
            getString(R.string.cancel_friendship_invitation),
            String.format(
                getString(R.string.cancel_friendship_invitation_message),
                user.display_name
            ),
            getString(R.string.yes),
            getString(R.string.no),
            confirmationCallback
        )

         */

    }

    override fun contactRequestAccept(contact: Contact) {


        requireActivity().showSnackBar(
            binding.root,
            "Implementar en el ViewModel el metodo contactRequestAccept"
        )

        viewModel.acceptContactRequest(contact)

        /*
        val newObject = contact.copy()
        newObject.status = FriendshipStatusEnums.ACCEPTED.name
        onContactChangedInternal(newObject)
        mPresenter.contactRequestAccept(
            contact.user_key.toString(),
            object : OnCompleteCallback {
                override fun onComplete(success: Boolean, result: Any?) {

                }

                override fun onError(exception: java.lang.Exception) {
                    super.onError(exception)
                    onContactChangedInternal(contact)
                }
            })

         */
    }

    override fun resendInvitation(record: Contact) {
        TODO("Not yet implemented")
    }

    // General

    fun onContactAddedInternal(contact: Contact) {
        val list = usersRecyclerViewAdapter.list
        val index = list.indexOf(contact)
        if (index == -1) {
            list.add(contact)
            usersRecyclerViewAdapter.notifyItemInserted(list.size - 1)
            hideNoRecordLabel()
        } else {
            list[index] = contact
            usersRecyclerViewAdapter.notifyItemChanged(index)
        }
    }

    override fun onContactChangedInternal(contact: Contact) {
        val list = usersRecyclerViewAdapter.list
        val index = list.indexOf(contact)
        if (index > -1) {
            list[index] = contact
            usersRecyclerViewAdapter.notifyItemChanged(index)
        }
    }

    fun onContactRemovedInternal(contact: Contact) {
        val list = usersRecyclerViewAdapter.list
        val index = list.indexOf(contact)
        if (index > -1) {
            list.removeAt(index)
            usersRecyclerViewAdapter.notifyItemRemoved(index)
            if (list.size == 0) {
                showNoRecordLabel()
            }
        }
    }

    override fun onContactAdded(snapshot: DataSnapshot, previousChildName: String?) {
        val contact = snapshot.getValue(Contact::class.java)!!
        contact.user_key = snapshot.key
        onContactAddedInternal(contact)
    }

    override fun onContactChanged(snapshot: DataSnapshot, previousChildName: String?) {
        var data = getData()
        val contact = snapshot.getValue(Contact::class.java)!!
        contact.user_key = snapshot.key
        onContactChangedInternal(contact)
    }

    override fun onContactRemoved(snapshot: DataSnapshot) {
        var data = getData()
        val contact = snapshot.getValue(Contact::class.java)!!
        contact.user_key = snapshot.key
        onContactRemovedInternal(contact)
    }


    /*
        override fun cancelInvitation(userInvited: Contact) {

            var confirmationCallback: OnConfirmationButtonsListener =
                object : OnConfirmationButtonsListener {
                    override fun onAccept() {

                        mPresenter.cancelInvitation(
                            userInvited,
                            object : OnCompleteCallback {
                                override fun onComplete(success: Boolean, result: Any?) {
                                    if (success) {
                                        var pp = 3
                                    }
                                }
                            })

                    }
                }

            showConfirmationDialog(
                getString(R.string.cancel_friendship_invitation),
                String.format(
                    getString(R.string.cancel_friendship_invitation_message),
                    userInvited.display_name
                ),
                getString(R.string.yes),
                getString(R.string.no),
                confirmationCallback
            )


        }
    */

    override fun contactRefuse(contact: Contact) {
        onContactRemovedInternal(contact)

        requireActivity().showSnackBar(
            binding.root,
            "Implementar en el ViewModel el metodo contactRefuse"
        )

        /*
            mPresenter.contactRefuse(contact.user_key.toString())
          */


    }


}