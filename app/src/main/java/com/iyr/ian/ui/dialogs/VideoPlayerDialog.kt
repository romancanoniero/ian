package com.iyr.ian.ui.dialogs


import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer.OnPreparedListener
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.iyr.ian.R
import com.iyr.ian.utils.UIUtils.handleTouch


class VideoPlayerDialog(mContext: Context, mActivity: Activity) :
    AlertDialog(
        mContext
    ) {
    //private var whiteZone: View?
    private var playButton: ImageView?
    private var playButtonLayout: FrameLayout?

    //private var ctlr: MediaController
    private var controller: MediaController? = null

    // private var mc: MediaController
    private val mThisDialog: VideoPlayerDialog = this
    private val videoViewer: VideoView
    private var mButton1Callback: View.OnClickListener? = null
    private val mDialoglayout: View
    private var mTitle: String? = null
    private var mLegend: String? = null
    private var mButton1Caption: String? = null
    private val mButton2Caption: String? = null


    fun setVideoUrl(videoPath: String) {

        videoViewer.setVideoPath(videoPath)

//        val (width, height) = myFile.getMediaDimensions(context)

        // videoViewer.start();
        /*
            MediaPlayer.initialize(AppClass.instance.applicationContext)
            MediaPlayer.exoPlayer?.preparePlayer(videoViewer, true)
            var _videoPath = "file:"+videoPath
            MediaPlayer.exoPlayer?.setSource(AppClass.instance.applicationContext, _videoPath)
            MediaPlayer.startPlayer()

    */
    }

    init {
        val inflater = mActivity.layoutInflater
        mDialoglayout = inflater.inflate(R.layout.dialog_video_player, null)
        this.setView(mDialoglayout)
        val buttonOne = mDialoglayout.findViewById<Button>(R.id.buttonOne)

        playButtonLayout = mDialoglayout.findViewById<FrameLayout>(R.id.play_button_layout)
      //  whiteZone = mDialoglayout.findViewById<View>(R.id.white_zone)
        playButton = mDialoglayout.findViewById<ImageView>(R.id.play_button)
        videoViewer = mDialoglayout.findViewById(R.id.videoView)

        playButtonLayout?.visibility = View.VISIBLE

        playButton?.setOnClickListener {
            playButtonLayout?.visibility = View.GONE
            videoViewer.start()


        }


        videoViewer.setOnCompletionListener {
            playButtonLayout?.visibility = View.VISIBLE

        }

        videoViewer.setOnPreparedListener(OnPreparedListener { mediaPlayer -> // TODO Auto-generated method stub
            mediaPlayer.setOnVideoSizeChangedListener { mediaPlayer, width, height -> /*
                         * add media controller
                         */


                    val videoRatio = mediaPlayer.videoWidth / mediaPlayer.videoHeight.toFloat()
                    val screenRatio = videoViewer.width / videoViewer.height.toFloat()
                    val scaleX = videoRatio / screenRatio
                    if (scaleX >= 1f) {
                        videoViewer.scaleX = scaleX
                    } else {
                        videoViewer.scaleY = 1f / scaleX
                    }


            }
        })

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window!!.attributes)
        lp.gravity = Gravity.CENTER
        window!!.attributes = lp
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.setGravity(Gravity.CENTER)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        buttonOne.setOnClickListener { view ->
            context.handleTouch()
            if (mButton1Callback != null) {
                mButton1Callback!!.onClick(view)
            }
            dismiss()
        }
    }


}