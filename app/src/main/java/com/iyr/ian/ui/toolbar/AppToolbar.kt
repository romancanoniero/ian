package com.iyr.ian.ui.toolbar

import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import com.iyr.ian.databinding.ToolbarBinding

/**
 * This class represent the Custom Toolbar API for clients.
 * Lets make the constructor private because we will use
 * Builder Design Pattern to initialize the API
 */
class AppToolbar private constructor() {

    companion object {
        /*
        Custom toolbar binding reference
         */
        private var binding: ToolbarBinding? = null

        /**
         * To initialize [AppToolbar] client have to use this      [Builder]
         * class to provide the custom toolbar reference [ContentCustomToolbarBinding]
         *
         */
        class Builder {

            /**
             * Set the binding provided by client
             *
             * @param binding, [ContentCustomToolbarBinding]
             * @return [Builder]
             */
            fun withBinding(binding: ToolbarBinding): Builder {
                AppToolbar.binding = binding
                return this
            }

            /**
             * This method check for the binding reference validation.
             * If validation is not valid, `throw` a new [Exception]
             * Else initialize the [AppToolbar] class and return the instance
             *
             * @return [AppToolbar]
             */
            @Throws(Exception::class)
            fun build(): AppToolbar {
                when (binding) {
                    null -> {
                        throw Exception("Binding reference can not be null")
                    }
                }
                return AppToolbar()
            }
        }
    }

    /**
     * Hide or show the toolbar
     *
     * @param isEnabled, [Boolean]
     * @return [AppToolbar]
     */
    fun enableToolbar(isEnabled: Boolean): AppToolbar {
        binding?.toolbarRootLayout?.isVisible = isEnabled
        return this
    }

    /**
     * Update the title
     *
     * @param title, [String]
     * @return [AppToolbar]
     */
    fun updateTitle(title: String): AppToolbar {
        binding?.titleText?.text = title
        return this
    }

    /***
     * Hide the title
     * @return [AppToolbar]
     */
    fun hideTitle(): AppToolbar {
        binding?.titleText?.isVisible = false
        return this
    }

    /***
     * Show the title
     * @return [AppToolbar]
     */
    fun showTitle(): AppToolbar {
        binding?.titleText?.isVisible = true
        return this
    }

    /**
     * Show or hide the back button
     *
     * @param isEnabled , [Boolean]
     * @return [AppToolbar]
     */
    fun enableBackBtn(isEnabled: Boolean): AppToolbar {
        binding?.backArrow?.isVisible = isEnabled
        return this
    }

    /**
     * Handle back btn action when the back button is pressed
     *
     * @param onAction, [Unit]
     * @return [AppToolbar]
     */
    fun onBackPressed(onAction: () -> Unit): AppToolbar {
        binding?.backArrow?.setOnClickListener {
            onAction.invoke()
        }
        return this
    }
/*
    fun updateChatCount(count: Int): AppToolbar {
        if (count >= 0) {
            binding?.chatIndicator?.isVisible = true
            binding?.messagesPendingCounterText?.text = count.toString()
            binding?.messagesPendingCounterText?.isVisible = true
        } else {
            binding?.chatIndicator?.isVisible = false
            binding?.messagesPendingCounterText?.isVisible = false
        }
        return this
    }
*/
    /***
     * Hide the tags miniatures
     * @return [AppToolbar]
     */
    fun hideTagsMiniatures(): AppToolbar {
        binding?.tagsMiniaturesContainer?.isVisible = false
        return this
    }

    /***
     * Show the tags miniatures
     * @return [AppToolbar]
     */
    fun showTagsMiniatures(): AppToolbar {
        binding?.tagsMiniaturesContainer?.isVisible = true
        return this
    }

    fun clearTagsMiniatures(): AppToolbar {
        binding?.tagsMiniaturesContainer?.removeAllViews()
        return this
    }

    fun setBellOnClickListener(listener: View.OnClickListener) {
        binding?.bell?.setOnClickListener(listener)
    }

    fun setSettingsOnClickListener(listener: View.OnClickListener) {
        binding?.settings?.setOnClickListener(listener)
    }

    fun getUserAvatarRef(): ImageView {
        return binding?.userImage!!
    }


    fun updateNotificationsBell(totalNotifications : Int, unReads: Int): AppToolbar {
        if (totalNotifications >0) {
            binding?.bellControl?.visibility = View.VISIBLE
            if (unReads == 0) {
                binding?.notificationCounter?.isVisible = false
                binding?.notificationCounterText?.isVisible = false
            } else {
                binding?.notificationCounter?.isVisible = true
                binding?.notificationCounterText?.text = unReads.toString()
                binding?.notificationCounterText?.isVisible = true
            }
        } else {
            binding?.bellControl?.visibility = View.GONE
            binding?.notificationCounter?.isVisible = false
            binding?.notificationCounterText?.isVisible = false
        }
        return this
    }

    fun showNotificationsBell(): AppToolbar {
        binding?.bellControl?.visibility = View.VISIBLE
        return this
    }

    fun hideNotificationsBell(): AppToolbar {
        binding?.bellControl?.visibility = View.GONE
        return this
    }


}