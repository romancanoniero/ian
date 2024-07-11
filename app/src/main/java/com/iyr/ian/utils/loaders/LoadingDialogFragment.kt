package com.iyr.ian.utils.loaders

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.airbnb.lottie.LottieAnimationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iyr.ian.R
import kotlin.math.roundToInt


enum class LoaderScaleTypeEnum {
    ORIGINAL,
    WIDTHEST,
    HEIGHTEST
}

class LoadingDialogFragment : DialogFragment() {

    private var mAnimationResId: Int = R.raw.lottie_loader_jake_parrotta
    private var mLoaderMessage: String = ""
    private var loaderMessage: TextView? = null
    private var nLoaderMessage: String = ""
    private var mAdjustType: LoaderScaleTypeEnum = LoaderScaleTypeEnum.ORIGINAL
    private lateinit var layout: View

    private lateinit var mLottieAnimation: LottieAnimationView

    init {
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(requireContext().getDrawable(R.color.transparent))
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(requireActivity().window!!.attributes)
        lp.gravity = Gravity.CENTER
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT

        requireActivity().window!!.attributes = lp
        //    requireActivity().window!!.setBackgroundDrawable(requireContext().getDrawable(R.color.transparent))
        requireActivity().window!!.setGravity(Gravity.CENTER)
        requireActivity().window!!.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )


        isCancelable = false
        layout = inflater.inflate(R.layout.fragment_loader, container, false)
        loaderMessage = layout.findViewById<TextView>(R.id.loader_message)
        mLottieAnimation = layout.findViewById<LottieAnimationView>(R.id.lottie_animation)
        mLottieAnimation.setAnimation(mAnimationResId)


        val inputStream = resources.openRawResource(mAnimationResId)
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val gson = Gson()
        val map: Map<String, Any?> =
            gson.fromJson(jsonString, object : TypeToken<Map<String, Any?>>() {}.type)

        var newWidth = map["w"] as Double
        var newHeight = map["h"] as Double

        when (mAdjustType) {
            LoaderScaleTypeEnum.ORIGINAL -> {


            //                mLottieAnimation.scaleType = LottieAnimationView.ScaleType.CENTER_INSIDE
            }
            LoaderScaleTypeEnum.WIDTHEST -> {
            //    mLottieAnimation.scaleType = LottieAnimationView.ScaleType.FIT_XY
                val displayMetrics = DisplayMetrics()
                requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
                val screenWidth = displayMetrics.widthPixels

                val resizeRatio = (screenWidth / map["w"] as Double).roundToInt()
                newWidth = (map["w"] as Double * resizeRatio)
                newHeight = (map["h"] as Double * resizeRatio)

            }
            LoaderScaleTypeEnum.HEIGHTEST -> {
//                mLottieAnimation.scaleType = LottieAnimationView.ScaleType.FIT_CENTER
            }
        }

        val params = mLottieAnimation.layoutParams
        params.width =
            newWidth.roundToInt() // Reemplaza 'nuevoAncho' con el valor del ancho que desees
        params.height =
            newHeight.roundToInt() // Reemplaza 'nuevoAlto' con el valor del alto que desees
        mLottieAnimation.layoutParams = params

        //   mLottObjectieAnimation.playAnimation()



        if (mLoaderMessage.isEmpty()) {
            loaderMessage?.visibility = GONE
        } else {
            loaderMessage?.text = mLoaderMessage
            loaderMessage?.visibility = VISIBLE
        }
        return layout
    }

    //over rid  this due to some issues that occur when trying to show a the dialog after onSaveInstanceState
    override fun show(manager: FragmentManager, tag: String?) {
        try {
            val ft = manager.beginTransaction()
            ft.add(this, tag)
            ft.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            ft.commitAllowingStateLoss()
        } catch (ignored: IllegalStateException) {
            var pp = 3
        }
    }


    override fun onStart() {
        super.onStart()
        mLottieAnimation.playAnimation()
    }

    override fun onStop() {
        super.onStop()
        mLottieAnimation.cancelAnimation()
    }

    fun setLoadingMessage(text: String) {
        mLoaderMessage = text
        loaderMessage?.text = mLoaderMessage

    }

    fun setAnimationResource(animationID: Int) {
        mAnimationResId = animationID
        if (::mLottieAnimation.isInitialized) {
            mLottieAnimation.clearAnimation()
            mLottieAnimation.setAnimation(mAnimationResId)
        }

    }

    fun setAdjustType(adjustType: LoaderScaleTypeEnum) {
        mAdjustType = adjustType
    }
}
