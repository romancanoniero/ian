package com.iyr.ian.ui.events.fragments.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.iyr.ian.R
import com.iyr.ian.utils.getBitmapFromVectorDrawable
import com.iyr.ian.utils.support_models.MediaFile
import com.iyr.ian.utils.support_models.MediaTypesEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


interface MediaHandlingCallback {

    fun onPlaySoundFile(url: String) {}
    fun onPlayVideoFile(url: String) {}
    fun onShowImageFile(url: String) {}
    fun onShowTextMessage(text: String) {}
    fun openMediaSelector() {}

    fun onDeleteMediaButtonPressed(mediaFile: MediaFile) {}
}

class EventMediaAdapter(val con: Context, val additionalCallback: MediaHandlingCallback) :
    RecyclerView.Adapter<EventMediaAdapter.UserViewHolder>() {

    private var mContext: Context = con
    private var mList: java.util.ArrayList<MediaFile> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = UserViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_multimedia_adapter, parent, false)
    )

    override fun getItemCount() = mList.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val record: MediaFile = mList[position]

        holder.trashButton.setOnClickListener {
            additionalCallback.onDeleteMediaButtonPressed(record)
        }


        when (record.media_type) {
            MediaTypesEnum.TEXT -> {
                GlobalScope.launch(Dispatchers.Default)
                {
                    val iconBitmap = con.getBitmapFromVectorDrawable(
                        R.drawable.ic_icon_chat_h
                    )
                    launch(Dispatchers.Main) {

                        holder.previewImage.setImageBitmap(iconBitmap)
                        holder.caption.text = ""
                    }
                }
            }

            MediaTypesEnum.AUDIO -> {
                GlobalScope.launch(Dispatchers.Default)
                {

                    val iconBitmap = con.getBitmapFromVectorDrawable(
                        R.drawable.ic_play_audio
                    )
                    launch(Dispatchers.Main) {
                        holder.previewImage.setImageBitmap(iconBitmap)
                        val duration = String.format(
                            "%02d min, %02d sec",
                            TimeUnit.MILLISECONDS.toMinutes(record.duration.toLong()),
                            TimeUnit.MILLISECONDS.toSeconds(record.duration.toLong()) -
                                    TimeUnit.MINUTES.toSeconds(
                                        TimeUnit.MILLISECONDS.toMinutes(
                                            record.duration.toLong()
                                        )
                                    )
                        )
                        holder.caption.text = duration
                    }
                }

            }

            MediaTypesEnum.VIDEO -> {


                val requestOptions = RequestOptions()
                requestOptions.isMemoryCacheable
                requestOptions.override(70, 70)
                Glide.with(con).setDefaultRequestOptions(requestOptions).load(record.file_name)
                    .into(holder.previewImage)

                val duration = String.format(
                    "%02d min, %02d sec",
                    TimeUnit.MILLISECONDS.toMinutes(record.duration.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(record.duration.toLong()) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(record.duration.toLong()))
                )
                holder.caption.text = duration
            }

            MediaTypesEnum.IMAGE -> {


                Glide
                    .with(con)
                    .asBitmap()
                    .load(record.file_name)
                    .into(holder.previewImage)

                holder.caption.text = ""
                holder.caption.visibility = View.VISIBLE

            }
        }


        holder.layout.setOnClickListener {
            when (record.media_type) {
                MediaTypesEnum.AUDIO -> {
                    if ((con is MediaHandlingCallback)) {
                        (con as MediaHandlingCallback).onPlaySoundFile(record.file_name)
                    }
                }

                MediaTypesEnum.VIDEO -> {
                    if ((con is MediaHandlingCallback)) {
                        (con as MediaHandlingCallback).onPlayVideoFile(record.file_name)
                    }
                }

                MediaTypesEnum.IMAGE -> {
                    if ((con is MediaHandlingCallback)) {
                        (con as MediaHandlingCallback).onShowImageFile(record.file_name)
                    }
                }

                MediaTypesEnum.TEXT -> {
                    if ((con is MediaHandlingCallback)) {
                        (con as MediaHandlingCallback).onShowTextMessage(record.text)
                    }
                }
            }
        }

    }

    fun getData(): java.util.ArrayList<MediaFile> {
        return mList
    }

    fun setData(events: ArrayList<MediaFile>) {
        mList = events
    }

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var layout: View = view.findViewById(R.id.layout)
        var previewImage: ImageView = view.findViewById(R.id.previewImage)
        var trashButton: ImageView = view.findViewById(R.id.trash_icon)
        var caption: TextView = view.findViewById(R.id.caption)
//        var eventTypeIcon = view.event_type_icon
    }
}