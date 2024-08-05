package com.iyr.ian.utils.sharing_app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iyr.ian.databinding.FragmentBottomSheetDialogShareWithAppsBinding
import com.iyr.ian.utils.sharing_app.adapters.AppAdapter
import com.iyr.ian.utils.sharing_app.adapters.AppInfo
import com.iyr.ian.utils.sharing_app.adapters.SharingContentAdapterInterface

class SharingBottomSheetFragment : BottomSheetDialogFragment() {




    private lateinit var binding: FragmentBottomSheetDialogShareWithAppsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBottomSheetDialogShareWithAppsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


       val callback = object : SharingContentAdapterInterface {
            override fun onAppSelected(packageName: String) {
                // Aquí puedes implementar la lógica para compartir la información
                // Establece el resultado
                findNavController().previousBackStackEntry?.savedStateHandle?.set("sharing_package_destination", packageName)

                // Navega hacia atrás
                findNavController().popBackStack()
            }
        }

        // Aquí va la lógica de showSharingBottomSheet()
        // Por ejemplo:
        val apps = getInstalledApps() // Esta es una función que debes implementar para obtener las aplicaciones instaladas
        val adapter = AppAdapter(apps, callback)

        binding.appsRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.appsRecyclerView.adapter = adapter

        binding.cancelButton.setOnClickListener {
            dismiss()
        }



    }

    fun getInstalledApps(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain" // Aquí puedes cambiar el tipo de contenido que deseas compartir
        val resolveInfoList = requireContext().packageManager.queryIntentActivities(intent, 0)

        return resolveInfoList.map { resolveInfo ->
            val icon = resolveInfo.loadIcon(requireContext().packageManager)
            val name = resolveInfo.loadLabel(requireContext().packageManager).toString()
            val packageName = resolveInfo.activityInfo.packageName
            AppInfo(icon, name, packageName)
        }
    }
}