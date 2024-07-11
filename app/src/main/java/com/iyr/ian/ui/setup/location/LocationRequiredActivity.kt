package com.iyr.ian.ui.setup.location

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.iyr.ian.Constants
import com.iyr.ian.R
import com.iyr.ian.dao.models.User
import com.iyr.ian.databinding.ActivityLocationRequiredBinding
import com.iyr.ian.sharedpreferences.SessionForProfile
import com.iyr.ian.ui.signup.signup_completed.SignUpCompletedActivity
import com.iyr.ian.utils.loaders.hideLoader
import com.iyr.ian.utils.permissions.PermissionsEnablingDialog
import com.iyr.ian.utils.permissions.PermissionsRationaleDialog
import com.iyr.ian.utils.requestLocationRequirements
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.utils.tts.speak

interface LocationRequiredActivityCallback {
    fun onSaveDone()
    fun onError(exception: Exception)
}


class LocationRequiredActivity : AppCompatActivity(), LocationRequiredActivityCallback {

    private var buttonClicked: Boolean = false
    private var isFirstSetup: Boolean = true
    private lateinit var binding: ActivityLocationRequiredBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

   //     speak("Conocer tu ubicacion en tiempo real es fundamental para que te puedan asistir y puedas asistir a otros. Nadie sabrá tu ubicación si no te encuentras en un evento activo")

        binding = ActivityLocationRequiredBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        getIntentData()
        setupUI()
    }

    override fun onResume() {
        super.onResume()
        buttonClicked = false
        updateUI()
    }


    fun setupUI() {

        binding.backArrows.setOnClickListener {
            onBackPressed()
        }


        binding.allowButton.setOnClickListener(View.OnClickListener {

            if (requestLocationRequirements()) {
                onSaveDone()
            }

        })
    }

    private fun updateUI() {

    }


    override fun onSaveDone() {
        val intent = Intent(this@LocationRequiredActivity, SignUpCompletedActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private var originalObject: User? = null
    private lateinit var currentObject: User
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

    override fun onError(exception: Exception) {
        buttonClicked = false
        hideLoader()
        showErrorDialog(exception.localizedMessage)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var grantResults = grantResults
        when (requestCode) {
            Constants.LOCATION_REQUEST_CODE -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    val _grantResults = IntArray(3)
                    _grantResults[0] = grantResults[0]
                    _grantResults[1] = grantResults[1]
                    _grantResults[2] = PackageManager.PERMISSION_GRANTED
                    grantResults = _grantResults
                }
                SessionForProfile.getInstance(this).setProfileProperty("RTLocationEnabled", false)
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    SessionForProfile.getInstance(this).setProfileProperty("RTLocationEnabled", true)
                    onSaveDone()
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) ||
                        shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                        shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    ) {
                        val mRationale = PermissionsRationaleDialog(
                            this, this, R.string.rationale_pemission_location, arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            ),
                            Constants.LOCATION_REQUEST_CODE
                        )
                        mRationale.show()
                    } else {
                        Toast.makeText(
                            this,
                            "Debes activar el permiso manualmente",
                            Toast.LENGTH_LONG
                        ).show()
                        val mPermissionEnablingDialog = PermissionsEnablingDialog(
                            this,
                            this,
                            R.string.rationale_pemission_location_manual_activation, arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            ),
                            Constants.LOCATION_REQUEST_CODE
                        )
                        mPermissionEnablingDialog.show()

                    }
                }
            }
        }

    }
}