package com.iyr.ian.ui.contatcs.dialogs


import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Html
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.iyr.ian.R
import com.iyr.ian.databinding.FragmentContactInsertionConfirmationPopupBinding
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.coroutines.Resource
import com.iyr.ian.utils.generateInvitationLink2
import com.iyr.ian.utils.sharing_app.SharingContents
import com.iyr.ian.utils.sharing_app.SharingTargets
import com.iyr.ian.utils.showErrorDialog
import com.iyr.ian.viewmodels.ContactsFragmentViewModel
import com.iyr.ian.viewmodels.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactsInvitationConfirmationDialog() :
    AppCompatDialogFragment() {


    val viewModel by lazy {
        ContactsFragmentViewModel.getInstance(
            UserViewModel.getInstance().getUser()?.user_key.toString()
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.context.setTheme(R.style.DialogStyle)
        return dialog
    }

    protected lateinit var binding: FragmentContactInsertionConfirmationPopupBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentContactInsertionConfirmationPopupBinding.inflate(
            requireActivity().layoutInflater,
            container,
            false
        )

        arguments?.getString("input")?.let {
            binding.contactName.text = arguments?.getString("input")
        }

        binding.yesButton.setOnClickListener { view ->
            requireContext().handleTouch()
            //          viewModel.inviteNonUser(arguments?.getString("input").toString())
            //           (requireActivity() as MainActivity).showSharingBottomSheet()
            findNavController().navigate(ContactsInvitationConfirmationDialogDirections.actionContactsInvitationConfirmationDialogToSharingBottomSheetFragment())
        }

        binding.noButton.setOnClickListener { view ->
            requireContext().handleTouch()
            findNavController().popBackStack()
            dismiss()
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Observa el resultado
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("sharing_package_destination")
            ?.observe(
                viewLifecycleOwner
            ) { result ->
                // Haz algo con el resultado
                // Por ejemplo, puedes mostrar un Toast con el nombre del paquete seleccionado

                val input = arguments?.getString("input").toString()
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.inviteNonUser(input)
                    when (result) {
                        "com.google.android.gm" -> {
                            // Aquí puedes implementar la lógica para compartir la información
                            // Establece el resultado

                            val sharingParams = SharingContents()
                            var invitationType = SharingTargets.GENERIC
                            invitationType = SharingTargets.EMAIL
                            sharingParams.contactAddress = input
                            sharingParams.title = "Proba esta aplicacion nueva"
                            sharingParams.sharingMethod = invitationType

                          val call =  requireActivity().generateInvitationLink2(
                                "INVITATION",
                                R.string.app_installation_invitation_message,
                                sharingParams
                            )

                            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
//                                putExtra(Intent.EXTRA_EMAIL, arrayOf("correo_destino@example.com"))
                                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.i_invite_you_to_try_this_app))
                                putExtra(Intent.EXTRA_TEXT, Html.fromHtml(call.data.toString().replace("/n", "<br/>")))
                            }



// Verifica que haya una aplicación que pueda manejar este intent
                            if (emailIntent.resolveActivity(requireActivity().packageManager) != null) {
                                startActivity(emailIntent)
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "No hay aplicaciones disponibles para enviar correos",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        "com.whatsapp" -> {
                            // Aquí puedes implementar la lógica para compartir la información
                            // Establece el resultado
                            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                                "sharing_package_destination",
                                result
                            )

                            // Navega hacia atrás
                            findNavController().popBackStack()
                        }

                        "com.facebook" -> {
                            // Aquí puedes implementar la lógica para compartir la información
                            // Establece el resultado
                            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                                "sharing_package_destination",
                                result
                            )

                            // Navega hacia atrás
                            findNavController().popBackStack()
                        }

                        "com.twitter" -> {
                            // Aquí puedes implementar la lógica para compartir la información
                            // Establece el resultado
                            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                                "sharing_package_destination",
                                result
                            )

                            // Navega hacia atrás
                            findNavController().popBackStack()
                        }

                        "com.instagram" -> {
                            // Aquí puedes implementar la lógica para compartir la información
                            // Establece el resultado
                            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                                "sharing_package_destination",
                                result
                            )

                            // Navega hacia atrás
                            findNavController().popBackStack()
                        }

                        "com.linkedin" -> {
                            // Aquí puedes implementar la lógica para compartir la información
                            // Establece el resultado
                            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                                "sharing_package_destination",
                                result
                            )

                            // Navega hacia atrás
                            findNavController().popBackStack()
                        }

                        "com.snapchat" -> {
                            // Aquí puedes implementar la lógica para compartir la información
                            // Establece el resultado
                            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                                "sharing_package_destination",
                                result
                            )

                            // Navega hacia atrás
                            findNavController().popBackStack()
                        }

                        "com.tiktok" -> {
                            // Aquí puedes implementar la lógica para compartir la información
                            // Establece el resultado
                            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                                "sharing_package_destination",
                                result
                            )

                            // Navega hacia atrás
                            findNavController().popBackStack()
                        }

                        "com.pinterest" -> {
                            // Aqu
                        }
                    }
                }
            }

    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val lp = WindowManager.LayoutParams()
            lp.copyFrom(dialog.window!!.attributes)
            lp.gravity = Gravity.CENTER
            dialog.window!!.attributes = lp
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window!!.setGravity(Gravity.CENTER)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setLayout(width, height)
        }

        startObservers()
    }


    override fun onStop() {
        super.onStop()
        stopObservers()
    }


    override fun onResume() {
        super.onResume()
    }

    private fun startObservers() {
        viewModel.contactCancelationAction.observe(this) { resource ->

            when (resource) {
                is Resource.Error -> {
                    requireActivity().showErrorDialog(resource.message.toString())
                }

                is Resource.Loading -> {
                }

                is Resource.Success -> {
                    findNavController().popBackStack()
                    viewModel.resetOperations()
                }

                null -> {
                    var tt = 0
                }
            }
        }
    }

    private fun stopObservers() {
        viewModel.contactCancelationAction.removeObservers(this)
    }
}

