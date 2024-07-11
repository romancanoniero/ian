package com.iyr.ian.ui.signup.phone_contacts

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.people.v1.PeopleService
import com.google.api.services.people.v1.PeopleServiceScopes
import com.google.api.services.people.v1.model.ListConnectionsResponse
import com.google.api.services.people.v1.model.Person
import com.google.gson.Gson
import com.iyr.fewtouchs.ui.views.signup.phone_contacts.adapters.PhoneContactsAdapter
import com.iyr.ian.AppConstants.Companion.PERMISSIONS_REQUEST_READ_CONTACTS
import com.iyr.ian.Constants.Companion.RC_SIGN_IN_GOOGLE
import com.iyr.ian.R
import com.iyr.ian.dao.models.User
import com.iyr.ian.databinding.ActivityAddContactsFromPhoneBinding
import com.iyr.ian.ui.setup.location.LocationRequiredActivity
import com.iyr.ian.ui.signup.signup_completed.SignUpCompletedActivity
import com.iyr.ian.utils.PhoneContact
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.UIUtils.statusBarTransparent
import com.iyr.ian.utils.areLocationPermissionsGranted
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.getContactList2
import com.iyr.ian.utils.loaders.hideLoader
import com.iyr.ian.utils.loaders.showLoader
import com.iyr.ian.utils.permissionsReadContacts
import com.iyr.ian.utils.showErrorDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

interface PhoneContactListener {
    fun onContactAdded(contact: PhoneContact)
    fun onFinishedRetrieve()
    fun onPercentChanged(percent: Int)
}

class AddContactsFromPhoneActivity : AppCompatActivity() {

    private val viewModel: AddContactsFromPhoneViewModel by lazy { AddContactsFromPhoneViewModel() }

    private lateinit var contactAdapter: PhoneContactsAdapter

    private var isRetrievingContacts: Boolean = false

    // private lateinit var mErrorDialog: NotificationDialog
    private var originalObject: User? = null
    private lateinit var currentObject: User

    // private lateinit var mPresenter: SignUpPresenter
    private lateinit var binding: ActivityAddContactsFromPhoneBinding
    private var buttonPressed: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //    mPresenter = SignUpPresenter(this, this)
        binding = ActivityAddContactsFromPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)
        statusBarTransparent()
        contactAdapter = PhoneContactsAdapter(this)
        getIntentData()
        setupUI()


    }


    override fun onResume() {
        super.onResume()
        setupObservers()


        //      loginGoogle()

        if (!isRetrievingContacts) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
                // Permiso no concedido, solicítalo.
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    PERMISSIONS_REQUEST_READ_CONTACTS)
            } else {
                // P
            // ermiso ya concedido, puedes proceder.
                retrieveContacts()
            }

        }

    }
/*
    // agrega el mecanismo que controle la respuesta despues de haber solicitado los permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val grantResults = grantResults
        when (requestCode) {
            MY_PERMISSION_REQUEST_READ_CONTACTS -> {
                val _grantResults = IntArray(2)
                _grantResults[0] = grantResults[0]

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    retrieveContacts()

                }
            }
        }
    }
    */
    override fun onPause() {
        super.onPause()
        removeObservers()
    }


    private fun setupObservers() {
        viewModel.inviting.observe(this) {
            when (it) {
                is Resource.Loading -> {
                    showLoader()
                }

                is Resource.Success -> {
                    hideLoader()/*
                                        lifecycleScope.launch(Dispatchers.IO) {
                                            val sharingContents = SharingContents(SharingTargets.GENERIC, null, null)

                                          val sharingLink =  this@AddContactsFromPhoneActivity.generateInvitationLink(
                                                DYNAMIC_LINK_ACTION_FRIENDSHIP_AND_SPEED_DIAL,
                                                R.string.app_speed_dial_addition_notification_message,
                                                sharingContents
                                            )


                                            var contactsToInvite = it.data


                                            contactsToInvite?.forEach() { contact ->
                                                sendWhatsAppMessage(
                                                    contact.telephone_number.toString(),
                                                    "Hola amorcito"
                                                )
                                            }
                                        }
                    */
                    binding.skipButton.performClick()
                }

                is Resource.Error -> {
                    hideLoader()
                    showErrorDialog(
                        getString(R.string.error), it.message, getString(R.string.close)
                    )
                }

                else -> {}
            }

        }
    }

    private fun removeObservers() {
        viewModel.inviting.removeObservers(this)
    }


    private fun getIntentData() {
        if (intent.hasExtra("data_object")) {
            currentObject = Gson().fromJson(
                intent.getStringExtra("data_object"), User::class.java
            )
            originalObject = Gson().fromJson(
                intent.getStringExtra("data_object"), User::class.java
            )
        }
    }

    private fun setupUI() {
        binding.backArrows.setOnClickListener {
            onBackPressed()
        }

        binding.skipButton.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("data_object", Gson().toJson(currentObject))

            var intent: Intent? = null
            intent = if (!areLocationPermissionsGranted()) {
                Intent(this@AddContactsFromPhoneActivity, LocationRequiredActivity::class.java)
            } else {
                Intent(this@AddContactsFromPhoneActivity, SignUpCompletedActivity::class.java)
            }
            intent.putExtras(bundle)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        binding.recyclerContacts.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerContacts.adapter = contactAdapter

        binding.selectionCheckbox.setOnClickListener {
            if (binding.selectionCheckbox.text == getString(R.string.all)) {
                selectedAll()
            } else {
                deselectAll()
            }
        }

        binding.searchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //    TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //         TODO("Not yet implemented")
            }

            override fun afterTextChanged(s: android.text.Editable?) {
                contactAdapter.setFilter(s.toString())
            }
        })

        binding.inviteButton.setOnClickListener {
            // TODO : HACER QUE EL LINK DE INVITACION SE GENERE UNA SOLA VEZ AL CREAR AL USUARIO.
            //   generateInvitationLink()
            // crea un array de contactos seleccionados
            handleTouch()
            val selectedContacts = contactAdapter.getSelectedContacts()
            if (selectedContacts.size > 0) {
                viewModel.addAsSpeedDialContact(selectedContacts)
            } else {

                showErrorDialog(
                    getString(R.string.empty),
                    getString(R.string.no_contacts_selected),
                    getString(R.string.close)
                )
            }
        }

    }


    /*
        private fun updateUI() {
            binding.searchText.isVisible = contactAdapter.getData().size > 0
            binding.selectionCheckbox.isVisible = contactAdapter.getData().size > 0
            binding.inviteButton.isVisible = contactAdapter.getSelectedCound() > 0

        }
    */
    private fun retrieveContacts() {
        isRetrievingContacts = true
        if (permissionsReadContacts()) {


            binding.progressBar.visibility = android.view.View.VISIBLE
            lifecycleScope.launch(Dispatchers.IO) {

                val listener = object : PhoneContactListener {
                    override fun onContactAdded(contact: PhoneContact) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            contactAdapter.addContact(contact)
                        }
                    }

                    override fun onPercentChanged(percent: Int) {
                        lifecycleScope.launch(Dispatchers.Main) {
//                            binding.progressIndicator.progress = percent
//                            binding.percentIndicator.text = "$percent%"
                            if (percent === 100) {
                                binding.progressBar.visibility = android.view.View.GONE
                                binding.progressBar.setProgress(percent, true)
                            }
                        }
                    }

                    override fun onFinishedRetrieve() {
                        contactAdapter.sortAlphabetically()
                        lifecycleScope.launch(Dispatchers.Main) {
                            contactAdapter.notifyDataSetChanged()
                            //                          binding.frameLoader.isVisible = false
                            isRetrievingContacts = false
                            binding.progressBar.setProgress(0, true)
                            binding.progressBar.visibility = android.view.View.GONE


                        }
                    }
                }

                getContactList2(listener)
            }

        }


    }


    private fun loginGoogle() {
        lifecycleScope.launch(Dispatchers.IO) {

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail()
                .requestScopes(Scope(Scopes.PROFILE), Scope(Scopes.EMAIL)).build()

            val googleSignInClient1 = GoogleSignIn.getClient(this@AddContactsFromPhoneActivity, gso)
            googleSignInClient1.signOut().addOnCompleteListener {

                val account = GoogleSignIn.getLastSignedInAccount(this@AddContactsFromPhoneActivity)
                if (account != null) {

                    val scopes = "oauth2:profile email"
                    var accessToken: String? = null
                    try {

                        accessToken = GoogleAuthUtil.getToken(
                            this@AddContactsFromPhoneActivity.getApplicationContext(),
                            account.account!!,
                            scopes!!
                        )
                    } catch (e: IOException) {
                        Log.d("TAG", e.message.toString())
                    }




                    fetchGoogleContacts(accessToken)
                } else {
                    // El usuario no se ha registrado con Google
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id)).requestEmail()
                        .requestScopes(Scope(Scopes.PROFILE), Scope(Scopes.EMAIL)).build()

                    val googleSignInClient =
                        GoogleSignIn.getClient(this@AddContactsFromPhoneActivity, gso)
                    val signInIntent: Intent = googleSignInClient.signInIntent
                    startActivityForResult(signInIntent, RC_SIGN_IN_GOOGLE)

                }


            }

        }

    }


    private fun selectedAll() {
        contactAdapter.selectAll()
        binding.selectionCheckbox.text = getString(R.string.none)
    }

    private fun deselectAll() {
        contactAdapter.deselectAll()
        binding.selectionCheckbox.text = getString(R.string.all)

    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val grantResults = grantResults
        when (requestCode) {
            PERMISSIONS_REQUEST_READ_CONTACTS -> {
                val _grantResults = IntArray(2)
                _grantResults[0] = grantResults[0]

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    retrieveContacts()
                }
                else
                {
                    binding.skipButton.performClick()
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RC_SIGN_IN_GOOGLE -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)

                    lifecycleScope.launch(Dispatchers.IO) {
                        fetchGoogleContacts(account.idToken)
                    }

                } catch (e: ApiException) {


                    when (e.statusCode) {
                        12501 -> {
                            // No hago nada porque quiere decir que el Usuario Cancelo la operacion
                        }

                        12500 -> {
                            showErrorDialog(getString(R.string.error_google_sign_in_failed))
                        }

                        else -> {
                            showErrorDialog(e.localizedMessage)
                        }
                    }

                }
            }


        }

    }

    private fun fetchGoogleContacts(idToken: String?) {
        val HTTP_TRANSPORT = NetHttpTransport()
        val JSON_FACTORY = JacksonFactory.getDefaultInstance()


        //  val credential = GoogleCredential().setAccessToken(idToken!!)

// Crea una instancia de GoogleAccountCredential


        val credential = GoogleAccountCredential.usingOAuth2(
            applicationContext, listOf(PeopleServiceScopes.CONTACTS_READONLY)
        )

// Establece el nombre de la cuenta de Google
        credential.selectedAccountName = "romancanoniero@gmail.com"


        // Crea una instancia de PeopleService
        val peopleService = PeopleService.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
            .setApplicationName("IANS").build()


        try {
            // fetching the target website
            val connectionsResponse: ListConnectionsResponse =
                peopleService.people().connections().list("people/me")
                    .setPersonFields("names,telephoneNumbers").execute()


            val connections: List<Person> = connectionsResponse.connections
            for (person in connections) {
                Log.d("GLOGLE", "Name: ${person.names[0].displayName}")
                Log.d("GLOGLE", "Email: ${person.emailAddresses[0].value}")
            }


        } catch (e: IOException) {
            throw RuntimeException(e);
        }

    }

}
