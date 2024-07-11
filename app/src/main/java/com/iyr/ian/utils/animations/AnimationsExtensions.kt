package com.iyr.ian.utils.animations

import android.app.Activity
import android.view.View

fun Activity.incorrectAnimation(view: View) {
// hacer que esto sea respecto a un viewgroup


    ShakeAnimator(view).apply {
        this.shakeMaxWidth = 80
        startDelay = 40.toLong()
        /*
                addListener(object : Animator.AnimatorListener {


                    override fun onAnimationEnd() {

                    }
                })
        */
        start()
    }

}

fun Activity.correctAnimation(view: View) {


    val DEFAULT_CORRECT_ANIMATION_DURATION: Long = 150
    val DEFAULT_INCORRECT_ANIMATION_DURATION: Long = 400

    SpringAnimator(view).run {
        this.duration = DEFAULT_CORRECT_ANIMATION_DURATION

        moveTopY = 10F
        moveBottomY = 10F

        start()
    }


}

