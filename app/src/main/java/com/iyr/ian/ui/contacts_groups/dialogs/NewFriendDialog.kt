package com.iyr.ian.ui.contacts_groups.dialogs

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.BounceInterpolator
import androidx.appcompat.app.AppCompatDialogFragment
import com.iyr.ian.AppConstants
import com.iyr.ian.R
import com.iyr.ian.dao.models.Contact
import com.iyr.ian.databinding.NewFriendPopupBinding
import com.iyr.ian.utils.UIUtils.handleTouch
import com.iyr.ian.utils.assignFileImageTo
import com.iyr.ian.utils.playSound
import com.iyr.ian.viewmodels.UserViewModel

class NewFriendDialog() :
    AppCompatDialogFragment() {


    protected lateinit var binding: NewFriendPopupBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.context.setTheme(R.style.DialogStyle)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = NewFriendPopupBinding.inflate(
            requireActivity().layoutInflater,
            container,
            false
        )


        val contactsHashMap =
            arguments?.getSerializable("contactsHashMap") as HashMap<String, Contact>

        val me = UserViewModel.getInstance().getUser()!!

        // buscar en contactsHashMap el otro usuario, es decir aquel cuya key sea distinta a me.user_key
        var theOtherUser: Contact? = null
        try {
            theOtherUser = contactsHashMap.values.first { it.user_key != me.user_key }

        } catch (e: Exception) {

            theOtherUser = Contact()
            theOtherUser.user_key = me.user_key
            theOtherUser.display_name = me.display_name
            theOtherUser.image = me.image

        }


        requireContext().assignFileImageTo(
            me.image.file_name,
            "${AppConstants.PROFILE_IMAGES_STORAGE_PATH}/${me.user_key}",
            binding.image1
        )

        requireContext().assignFileImageTo(
            theOtherUser?.image?.file_name ?: "",
            "${AppConstants.PROFILE_IMAGES_STORAGE_PATH}/${theOtherUser?.user_key}",
            binding.image2
        )


        binding.buttonClose.visibility = View.INVISIBLE
        binding.buttonClose.setOnClickListener { view ->
            requireContext().handleTouch()
            dismiss()
        }

        return binding.root
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

            binding.root.post {
                animateUserImages()

            }
        }
    }

    private fun showLottieAnimation() {
        val animation = binding.lottieAnimation
        animation.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                requireContext().playSound(R.raw.bell)
            }
            override fun onAnimationEnd(animation: Animator) {
                   binding.buttonClose.visibility = View.VISIBLE
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        animation.playAnimation()
    }


    var animationsFinished = 0

    val animationListener = object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {}

        override fun onAnimationEnd(animation: Animator) {
            animationsFinished++
            if (animationsFinished == 2) {
                // Aquí puedes realizar la acción que necesitas cuando ambas animaciones terminen
                showLottieAnimation()
            }
        }

        override fun onAnimationCancel(animation: Animator) {}

        override fun onAnimationRepeat(animation: Animator) {}
    }
    fun animateUserImages() {

        val dialogCardView = binding.dialogCard
        val image1 = binding.image1
        val image2 = binding.image2

        val image1Width = image1?.width?.toFloat() ?: 0f
        val image2Width = image2?.width?.toFloat() ?: 0f

        val screenWidth = dialogCardView?.width?.toFloat() ?: 0f
        val bounceOffset = screenWidth * 0.25f

// Calculamos el desplazamiento para que las imágenes se superpongan en el centro
        val bounceOffset1 = (screenWidth / 2) - (image1Width / 2) + (image1Width * 0.25f)
        val bounceOffset2 = (screenWidth / 2) - (image2Width / 2) + (image1Width * 0.25f)
// Animación para image1
        val animator1 = ObjectAnimator.ofFloat(image1, "translationX", bounceOffset1)
        animator1.interpolator = BounceInterpolator()
        animator1.duration = 1000 // Duración en milisegundos
        animator1.addListener(animationListener)
        animator1.start()

// Animación para image2
        val animator2 = ObjectAnimator.ofFloat(image2, "translationX", -bounceOffset2)
        animator2.interpolator = BounceInterpolator()
        animator2.duration = 1000 // Duración en milisegundos
        animator2.addListener(animationListener)
        animator2.start()
    }

}
