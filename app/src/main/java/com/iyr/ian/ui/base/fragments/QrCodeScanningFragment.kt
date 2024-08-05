package com.iyr.ian.ui.base.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.zxing.BarcodeFormat
import com.iyr.ian.AppConstants
import com.iyr.ian.R
import com.iyr.ian.databinding.FragmentQrScannerDisplayBinding
import com.iyr.ian.sharedpreferences.SessionForProfile
import com.iyr.ian.ui.MainActivity
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.utils.showSnackBar
import com.iyr.ian.viewmodels.MainActivityViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [QrCodeScanningFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class QrCodeScanningFragment : Fragment() {


    private lateinit var binding: FragmentQrScannerDisplayBinding
    private lateinit var codeScanner: CodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentQrScannerDisplayBinding.inflate(inflater, container, false)

        val scannerView = binding.scannerView
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
        return binding.root
    }


    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).hideFooterToolBar()
        (requireActivity() as MainActivity).hideTitleBar()
    }

    override fun onPause() {
        super.onPause()
        (requireActivity() as MainActivity).showFooterToolBar()
        (requireActivity() as MainActivity).showTitleBar()
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment QrCodeDisplayFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            QrCodeScanningFragment()
    }
}