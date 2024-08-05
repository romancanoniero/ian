package com.iyr.ian.ui.contatcs

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.zxing.BarcodeFormat
import com.iyr.ian.AppConstants
import com.iyr.ian.R
import com.iyr.ian.dao.models.Contact
import com.iyr.ian.dao.models.ContactGroup
import com.iyr.ian.databinding.FragmentContactsBinding
import com.iyr.ian.sharedpreferences.SessionForProfile
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.ui.contatcs.adapters.ContactsAdapter
import com.iyr.ian.ui.contatcs.adapters.ContactsGroupsAdapter
import com.iyr.ian.utils.capitalizeFirstAndLongWords
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.dp
import com.iyr.ian.utils.generateInvitationLink2
import com.iyr.ian.utils.hideKeyboard
import com.iyr.ian.utils.loadImageFromCache
import com.iyr.ian.utils.loaders.hideLoader
import com.iyr.ian.utils.loaders.showLoader
import com.iyr.ian.utils.sharing_app.SharingContents
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.utils.showSnackBar
import com.iyr.ian.viewmodels.ContactsFragmentViewModel
import com.iyr.ian.viewmodels.MainActivityViewModel
import com.iyr.ian.viewmodels.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER



class ContactsFragment : Fragment() {

    lateinit var binding: FragmentContactsBinding

    val viewModel: ContactsFragmentViewModel by lazy {
        ContactsFragmentViewModel.getInstance(UserViewModel.getInstance().getUser()?.user_key ?: "")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    var contactsGroupsAdapter = ContactsGroupsAdapter(this)
    var contactsAdapter = ContactsAdapter(this)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonQr.setOnClickListener {
            showQRPopup()
        }


        binding.searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {

                if (s.toString().isNotEmpty()) {
                    binding.searchBox.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.quantum_ic_search_grey600_24, 0, R.drawable.ic_round_cancel_24, 0
                    )

                    binding.searchBox.setOnTouchListener { _, event ->
                        try {


                            if (event.action == MotionEvent.ACTION_UP) {
                                if (event.rawX >= (binding.searchBox.right - binding.searchBox.compoundDrawables[2].bounds.width())) {
                                    binding.searchBox.setText("")
                                    return@setOnTouchListener true
                                }
                            }
                        } catch (ex: Exception) {
                        }
                        return@setOnTouchListener false
                    }
                } else {
                    binding.searchBox.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.quantum_ic_search_grey600_24, 0, 0, 0
                    )
                }


                val input = s.toString()
                if (input.isEmpty()) {

                    contactsGroupsAdapter.filter("")
                    contactsGroupsAdapter.notifyDataSetChanged()

                    contactsAdapter.filter("")
                    contactsAdapter.notifyDataSetChanged()
                } else {


//                    updateGroupListViews(viewModel.groupsList.value ?: ArrayList())
                    //updateContactsListViews(viewModel.contactsList.value ?: ArrayList())

                    contactsGroupsAdapter.filter(input)
                    contactsGroupsAdapter.notifyDataSetChanged()


                    contactsAdapter.filter(input)
                    contactsAdapter.notifyDataSetChanged()
                }
            }
        })

        binding.groupsRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.groupsRecyclerView.adapter = contactsGroupsAdapter

        binding.contactsRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.contactsRecyclerView.adapter = contactsAdapter


        val iconSize = resources.getDimensionPixelSize(R.dimen.circle_5)
        val createGroupImage = ResourcesCompat.getDrawable(resources,R.drawable.ic_group_add_by_aleksei_ryazancev,requireContext().theme)?.toBitmap(iconSize,iconSize)?.toDrawable(resources) as Drawable
        binding.optionCreateGroup.setCompoundDrawablesWithIntrinsicBounds(createGroupImage,null,null,null)

        binding.optionCreateGroup.setOnClickListener {
            val userKey = UserViewModel.getInstance().getUser()?.user_key ?: ""
            val groupName = binding.searchBox.text.toString().capitalizeFirstAndLongWords()

            val action =
                ContactsFragmentDirections.actionContactsFragmentToContactsGroupsCreationDialog(
                    groupName
                )
            findNavController().navigate(action)


        }

        val createUserImage = ResourcesCompat.getDrawable(resources,R.drawable.ic_user_add_by_aleksei_ryazancev,requireContext().theme)?.toBitmap(iconSize,iconSize)?.toDrawable(resources) as Drawable
        binding.optionCreateGroup.setCompoundDrawablesWithIntrinsicBounds(createUserImage,null,null,null)

        binding.optionAddContact.setOnClickListener {
            requireContext().hideKeyboard(binding.optionAddContact)
            shareLink()/*
                      var input = binding.searchBox.text.toString()

                      if (input.isValidPhoneNumber() || input.isValidMail()) {
                          val action = ContactsFragmentDirections.actionContactsFragmentToContactsInvitationConfirmationDialog(input )
                          findNavController().navigate(action)
                      }
          */
        }

    }

    private fun shareLink() {

        lifecycleScope.launch(Dispatchers.IO) {

            val sharingParams = SharingContents()


            val call = requireActivity().generateInvitationLink2(
                AppConstants.DYNAMIC_LINK_ACTION_FRIENDSHIP,
                R.string.app_installation_invitation_message,
                sharingParams
            )

            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.i_invite_you_to_try_this_app))
                putExtra(
                    Intent.EXTRA_TEXT, Html.fromHtml(call.data.toString().replace("/n", "<br/>"))
                )
            }


// Verifica que haya una aplicación que pueda manejar este intent
            if (emailIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(emailIntent)
            } else {
                Toast.makeText(
                    requireContext(), "No hay aplicaciones disponibles para eso", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object;


    override fun onResume() {
        super.onResume()
        val appToolbar = (requireActivity() as MainActivity).appToolbar
        appToolbar.enableBackBtn(true)
        appToolbar.updateTitle(getString(R.string.contacts))

        val bottomToolBar = (requireActivity() as MainActivity).binding.bottomToolbar
        bottomToolBar.visibility = View.GONE

        startObservers()
    }

    override fun onPause() {
        super.onPause()
        stopObservers()
    }

    fun startObservers() {

        viewModel.contactsGroupsListFlow.observe(this) { _ ->

        }

        viewModel.contactsListFlow.observe(this) { resource -> }

        viewModel.contactsList.observe(this) { contacts ->

            val contactsToRemove = ArrayList<Contact>()
            for (contact in contactsAdapter.getData()) {
                val index = contacts?.indexOf(contact) ?: -1
                if (index == -1) {
                    contactsToRemove.add(contact)
                }
            }
            contactsToRemove.forEach { contact ->
                val index = contactsAdapter.getData().indexOf(contact)
                contactsAdapter.getData().remove(contact)
                contactsAdapter.notifyItemRemoved(index)
            }
            contacts?.forEach { contact ->
                val index = contactsAdapter.getData().indexOf(contact)
                if (index == -1) {
                    contactsAdapter.getData().add(contact)
                    contactsAdapter.notifyItemInserted(contactsAdapter.getData().size - 1)
                } else {
                    contactsAdapter.getData()[index] = contact
                    contactsAdapter.notifyItemChanged(index)
                }
            }

            contactsAdapter.setData(contacts ?: ArrayList())
            //       updateContactsListViews(contacts ?: ArrayList())

            val searchString = binding.searchBox.text.toString()
            contactsAdapter.filter(searchString)

            if ((contacts?.size ?: 0) > 0) {
                binding.contactsTitle.visibility = View.VISIBLE
                binding.contactsLayout.visibility = View.VISIBLE
            } else {
                binding.contactsTitle.visibility = View.GONE
                binding.contactsLayout.visibility = View.GONE
            }

        }

        viewModel.groupsList.observe(this) { groups ->
            contactsGroupsAdapter.setData(groups ?: ArrayList())
            contactsAdapter.setGroupsData(groups ?: ArrayList())




          //  updateGroupListViews(groups ?: ArrayList())

            if ((groups?.size ?: 0) > 0) {
                binding.groupsTitle.visibility = View.VISIBLE
                binding.groupsLayout.visibility = View.VISIBLE
            } else {
                binding.groupsTitle.visibility = View.GONE
                binding.groupsLayout.visibility = View.GONE
            }


        }

        viewModel.insertContactGroupAction.observe(this) { resource ->
            when (resource) {
                is Resource.Error -> {
                    val error = resource.message.toString()
                    when (error) {
                        "group_name_empty" -> {
                            requireActivity().showErrorDialog("El nombre del grupo no puede estar vacío")
                        }

                        "group_name_exists" -> {
                            requireActivity().showErrorDialog(getString(R.string.group_name_exists))
                        }
                    }
                }

                is Resource.Loading -> {
                    requireActivity().showLoader()
                }

                is Resource.Success -> {
                    resetSearchBox()
                    contactsGroupsAdapter.addGroup(resource.data!!)
                }

                null -> {

                }
            }
        }

        viewModel.contactCancelationAction.observe(this) { resource ->
            when (resource) {
                is Resource.Error -> {
                    requireActivity().hideLoader()
                    when (val error = resource.message.toString()) {

                        else -> {
                            requireActivity().showErrorDialog(error)
                        }
                    }
                }

                is Resource.Loading -> {
                    requireActivity().showLoader()
                }

                is Resource.Success -> {
                    requireActivity().hideLoader()
                    resetSearchBox()
                }

                null -> {

                }
            }
        }

    }

    private fun resetSearchBox() {
        binding.searchBox.setText("")
    }

    fun findContactIndex(contact: Contact): Int {
        return contactsAdapter.getData().indexOf(contact)
    }


    private fun notifyContactGroupInserted(group: ContactGroup) {
        try {
            binding.groupsLayout.findViewWithTag<View>(group).tag
        } catch (e: Exception) {
            val index = contactsGroupsAdapter.getData().indexOf(group)

            if (index > -1) {
                val groupsViewHolder =
                    contactsGroupsAdapter.onCreateViewHolder(binding.groupsRecyclerView, 0)

                contactsGroupsAdapter.onBindViewHolder(groupsViewHolder, index)

                val newView = groupsViewHolder.itemView
                newView.tag = group
                binding.groupsLayout.addView(newView)
            }
        }
    }

    fun stopObservers() {

        viewModel.contactsGroupsListFlow.removeObservers(viewLifecycleOwner)

        viewModel.contactsFlow.removeObservers(viewLifecycleOwner)

        viewModel.insertContactGroupAction.removeObservers(viewLifecycleOwner)
    }

    private lateinit var codeScanner: CodeScanner
    private lateinit var qrPopupWindow: PopupWindow
    fun showQRPopup() {

        val inflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_qr, null)

        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        qrPopupWindow = PopupWindow(popupView, width, height, true)

        val buttonQr = popupView.findViewById<ImageView>(R.id.show_qr_button)
        val buttonScanQr = popupView.findViewById<ImageView>(R.id.scan_qr_button)

        buttonQr.setOnClickListener {
            qrPopupWindow.dismiss()
            if (requireContext().loadImageFromCache("qr_code.png", "images") != null) {
                findNavController().navigate(R.id.qrCodeDisplayPopup)
            }

        }

        buttonScanQr.setOnClickListener {

            qrPopupWindow.dismiss()
            val scannerView = binding.root.findViewById<CodeScannerView>(R.id.scanner_view)
            scannerView.visibility = View.VISIBLE

            if (!::codeScanner.isInitialized) {
                codeScanner = CodeScanner(requireContext(), scannerView)

                // Parameters (default values)
                codeScanner.camera =
                    CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
                codeScanner.formats = listOf(BarcodeFormat.QR_CODE)
                codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
                codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
                codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
                codeScanner.isFlashEnabled = false // Whether to enable flash or not

                // Callbacks
                codeScanner.decodeCallback = DecodeCallback {

                    requireActivity().runOnUiThread {
                        scannerView.visibility = View.GONE

                        FirebaseDynamicLinks.getInstance().getDynamicLink(Uri.parse(it.text))
                            .addOnSuccessListener(
                                requireActivity()
                            ) { pendingDynamicLinkData ->
                                // Get deep link from result (may be null if no link is found)
                                var deepLink: Uri?
                                if (pendingDynamicLinkData != null) {
                                    deepLink = pendingDynamicLinkData.link

                                    val action = deepLink?.getQueryParameter("action")
                                    val key = deepLink?.getQueryParameter("key").toString()

                                    if ((key).compareTo(
                                            SessionForProfile.getInstance(requireContext()).getUserId()
                                        ) == 0
                                    ) {
                                        requireActivity().showErrorDialog(
                                            getString(
                                                R.string.error_cannot_send_it_to_you
                                            )
                                        )
                                    } else {
                                        when (action) {
                                            AppConstants.DYNAMIC_LINK_ACTION_FRIENDSHIP -> {
                                                MainActivityViewModel.getInstance()
                                                    .onContactByUserKey(key)
                                            }

                                            AppConstants.DYNAMIC_LINK_ACTION_FRIENDSHIP_AND_SPEED_DIAL -> {

                                                requireActivity().showSnackBar(
                                                    binding.root,
                                                    "Implementar en el viewmodel onFriendshipRequestAndSpeedDialByUserKey"
                                                )

                                            }
                                        }
                                    }
                                    Toast.makeText(requireContext(), action, Toast.LENGTH_LONG).show()
                                }


                                // Handle the deep link. For example, open the linked content,
                                // or apply promotional credit to the user's account.
                                // ...

                                // ...
                            }.addOnFailureListener(
                            requireActivity()
                        ) { e -> Log.w("DYNAMIC-LINKS", "getDynamicLink:onFailure", e) }
/*
                        MainActivityViewModel.getInstance().onContactByUserKey(key)
                        Toast.makeText(
                            requireContext(),
                            "Scan result: ${it.text}",
                            Toast.LENGTH_LONG
                        ).show()
*/
                    }


                }
                codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS

                    requireActivity().runOnUiThread {

                        scannerView.visibility = View.GONE
                        Toast.makeText(
                            requireContext(), "Camera initialization error: ${it.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            // scannerView.setOnClickListener {
            codeScanner.startPreview()

        }

        val offsetX = binding.buttonQr.width - qrPopupWindow.width
        qrPopupWindow.showAsDropDown(binding.buttonQr, offsetX, 4.dp)

    }
}