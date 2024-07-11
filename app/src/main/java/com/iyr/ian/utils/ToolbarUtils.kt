package com.iyr.ian.utils

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.setPadding
import com.iyr.ian.R
import com.iyr.ian.glide.GlideApp


class ToolbarUtils {

    fun createToolbarButton(context: Context, controlResId: Int, image: Any): View {

        val iconWidth = 28.px
        val iconHeight = 28.px

        val outerFrame = ConstraintLayout(context)
        val outerFrameLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        outerFrame.setPadding(4.px)
        outerFrame.tag = controlResId
        outerFrame.layoutParams = outerFrameLayoutParams
        //  outerFrame.setBackgroundColor(context.getColor(R.color.colorRed))
        outerFrameLayoutParams.marginStart = 10.px


        val imageButton = ImageView(context)
        //     imageButton.adjustViewBounds = true
//        imageButton.scaleType = ImageView.ScaleType.CENTER_CROP
        val imageButtonLayoutParams = ConstraintLayout.LayoutParams(
            iconWidth,
            iconHeight
        )
        /*
             imageButtonLayoutParams.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
             imageButtonLayoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
             imageButtonLayoutParams.rightToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
             imageButtonLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
          */
        imageButton.layoutParams = imageButtonLayoutParams

/*
        var text = TextView(context)
        text.text = "sorete"
        var textLayoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        textLayoutParams.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        textLayoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        textLayoutParams.rightToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        textLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;

        text.layoutParams = textLayoutParams
*/

        outerFrame.addView(imageButton)
        if (image is Int) {
            GlideApp.with(context)
                .asBitmap()
                .load(image)
                .fitCenter()
                .placeholder(getDrawable(context, R.drawable.progress_animation))
                .error(getDrawable(context, R.drawable.ic_error))
                .into(imageButton)
        } else
            if (image is Uri?) {
                GlideApp.with(context)
                    .asBitmap()
                    .load(image)
                    .fitCenter()
                    .placeholder(getDrawable(context, R.drawable.progress_animation))
                    .error(getDrawable(context, R.drawable.ic_error))
                    .into(imageButton)

            }
/*
        Picasso.get()
            .load(R.drawable.ic_bell)
            .resize(iconWidth, iconHeight )
            .into(imageButton)
*/
//        outerFrame.addView(text)


        return outerFrame


    }


}