package com.iyr.ian.ui.map

import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.iyr.ian.ui.KeyboardStatusEnum
import com.iyr.ian.ui.chat.ChatWindowStatus
import com.iyr.ian.ui.chat.MessagesInEventFragment
import com.iyr.ian.utils.UIUtils.getStatusBarHeight
import com.iyr.ian.utils.px
import com.visualizer.amplitude.dp

/***
 * Resize the chat window according several status
 */
internal fun MessagesInEventFragment.resizeChatWindow(
    screenHeight: Int,
    visibleAreaHeight: Int,
    keyboardStatus: KeyboardStatusEnum,
    chatWindowStatus: ChatWindowStatus
) {
  //  if (viewModel.chatWindowStatus.value == chatWindowStatus && viewModel.keyboardStatus.value == keyboardStatus) return

    val heightDiff = screenHeight - visibleAreaHeight
    var newHeight = 0.0

    when (keyboardStatus) {
        KeyboardStatusEnum.OPEN -> {when (chatWindowStatus) {
            ChatWindowStatus.CLOSED -> newHeight = 0.0
            ChatWindowStatus.NORMAL -> {
                newHeight =
                    (standardChatWindowHeight - heightDiff + requireContext().getStatusBarHeight()).toDouble()
                binding.cardView.radius = 20.dp()

            }

            ChatWindowStatus.FULLSCREEN -> {
                newHeight =
                    (visibleAreaHeight + requireContext().getStatusBarHeight()).toDouble()
                (binding.messageInputFix.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin =
                    200  //(visibleAreaHeight - requireContext().getStatusBarHeight()).toDouble()
                binding.cardView.radius = 0.0f


            }
        }
}
        KeyboardStatusEnum.CLOSED -> when (chatWindowStatus) {
            ChatWindowStatus.CLOSED -> newHeight = 0.0
            ChatWindowStatus.NORMAL -> {
                // ok
                newHeight = (standardChatWindowHeight).toDouble()
                binding.cardView.radius = 20.px.toFloat()
            }

            ChatWindowStatus.FULLSCREEN -> {
                // ok
                newHeight = (screenHeight - requireContext().getStatusBarHeight()).toDouble()
                binding.cardView.radius = 0.toFloat()
                (binding.messageInputFix.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin =
                    0
            }
        }
    }




    binding.cardView.visibility = View.GONE
    binding.cardView.layoutParams.height = newHeight.toInt()

    binding.cardView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            binding.cardView.viewTreeObserver.removeOnPreDrawListener(this)
      binding.messageInputFix.inputEditText.requestFocus()
       return true
        }

    })

    binding.cardView.visibility = View.VISIBLE


    viewModel.onChatWindowStatusChange(chatWindowStatus)
    viewModel.onKeyboardStatusChange(keyboardStatus)
    binding.messageInputFix.requestFocus()
}
