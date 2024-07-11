package com.iyr.ian.utils.avatarsviewgroup

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.makeMeasureSpec
import android.widget.FrameLayout
import com.iyr.ian.R
import com.iyr.ian.glide.GlideApp
import com.iyr.ian.utils.dp
import de.hdodenhof.circleimageview.CircleImageView


class AvatarGroupView : FrameLayout {


    class Avatar {

        private var caption: String = ""

        fun setCaption(text: String) {
            caption = text
        }

        fun getCaption(): String {
            return caption
        }


        private var image_src: String = ""
        fun getImageSrc(): String {
            return image_src
        }

        fun setImageSrc(src: String) {
            image_src = src
        }


        private var extras: HashMap<String, String>? = null

        fun setExtras(map: HashMap<String, String>?) {
            extras = map
        }

        fun getExtras(): HashMap<String, String>? {
            return extras
        }
    }

    private var _previewItems: Int = 0
    private var _avatarBorderWidth: Int = 0
    private var _avatarBorderColor: Int = context.getColor(R.color.black)
    private var _maxVisibleAvatars: Int = 4 // Número máximo de avatares visibles
    private var _avatarSize: Int = 200.dp

    private var avatars: ArrayList<Avatar> = ArrayList<Avatar>()

    // Lista de avatares predeterminados
    private var containerWidth = 0


    init {
        if (isInEditMode) {
            previewAvatars()
        } else {
            avatars.clear()
        }
        updateUI()
    }

    private fun previewAvatars() {
        for (i in 0 until _previewItems) {
            avatars.add(Avatar())
        }
    }
    /*
        constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

        }
    */

    constructor(context: Context) : super(context) {
        init(null, 0)
        updateUI()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    fun setAvatarSizeValue(value: Int) {
        _avatarSize = value
        // Aquí puedes realizar cualquier lógica adicional necesaria cuando cambia el atributo.
        // Por ejemplo, invalidar la vista para forzar una actualización.
        updateUI()
    }

    fun getAvatarSizeValue(): Int {
        return _avatarSize
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes

        //   this.setBackgroundColor(Color.YELLOW);

        val array = context.obtainStyledAttributes(
            attrs, R.styleable.AvatarGroupView, defStyle, 0
        )

        var i = 0
        // iterate the array and check for specific attributes
        while (i < array.indexCount) {
            val attributes = array.getIndex(i)

            //check if the current attribute is dateTimeText
            if (attributes == R.styleable.AvatarGroupView_avatarSizeValue) {
                // set datetime
                _avatarSize = array.getDimensionPixelSize(i, 220)
            }

            if (attributes == R.styleable.AvatarGroupView_avatarMaxVisibleAvatarsValue) {
                // set datetime
                _maxVisibleAvatars = array.getInt(i, 4)
            }

            if (attributes == R.styleable.AvatarGroupView_avatarBorderColorValue) {
                // set datetime
                _avatarBorderColor = array.getColor(i, _avatarBorderColor)
            }
            if (attributes == R.styleable.AvatarGroupView_avatarBorderWidth) {
                _avatarBorderWidth = array.getDimensionPixelSize(i, 2.dp)
            }

            if (attributes == R.styleable.AvatarGroupView_avatarPreviewItems) {
                // set datetime
                _previewItems = array.getInt(i, 4)
            }

            i++
        }

        //  if (isInEditMode)
        //  {
        previewAvatars()
        //  }
        if (avatars.size > 0) {
            var incomplete = false
            avatars.forEach { avatar ->
                if (avatar.getImageSrc() == "") {
                    incomplete = true
                }
            }

            if (!incomplete) {
                updateUI()
            }
        }
    }

    fun updateUI() {
        removeAllViews() // Limpia cualquier vista anterior
        var leftMargin = 0f
        containerWidth = 0

        for (i in 0 until _maxVisibleAvatars) {
            if (i < avatars.size) {
                if (i > 0) {
                    leftMargin = (((_avatarSize * .75) * (i))).toFloat()
                }

                val avatar = CircleImageView(context)
                avatar.setImageResource(R.drawable.profile_dummy)
                avatar.borderColor = _avatarBorderColor
                avatar.borderWidth = _avatarBorderWidth
                avatar.elevation = 6.dp.toFloat()
                var newAvatar: Avatar = avatars[i]

                if (!isInEditMode) {
                    GlideApp.with(this).asBitmap()
                        .load(newAvatar.getImageSrc())
                        .placeholder(context.getDrawable(R.drawable.progress_animation))
                        .into(avatar)
                }

                // Personaliza el tamaño del avatar según tus necesidades
                val layoutParams = LayoutParams(_avatarSize, _avatarSize)
                avatar.layoutParams = layoutParams
                avatar.x = leftMargin
                if (i == 0) {
                    containerWidth = _avatarSize
                } else {
                    //   if (i == 1) {
                    containerWidth = (leftMargin + _avatarSize).toInt()
                    /*
                           } else
                               containerWidth = (containerWidth + leftMargin + side).toInt()
                      */
                }
                Log.d("AvatarGroupView", "leftMargin: $leftMargin")
                //avatar.gravity = Gravity.CENTER_HORIZONTAL
                addView(avatar)


            }


        }

        // Si hay más avatares que el número máximo visible, agrega un "+" u otro indicador
        if (avatars.size > _maxVisibleAvatars) {
            val plusIndicator = CircleImageView(context)
            plusIndicator.setImageResource(R.drawable.ic_baseline_add_24)
            plusIndicator.circleBackgroundColor = context.getColor(R.color.gray_600)
            // Personaliza el tamaño del indicador "+" según tus necesidades
            val layoutParams = LayoutParams(_avatarSize, _avatarSize)
            plusIndicator.layoutParams = layoutParams
            //   plusIndicator.gravity = Gravity.CENTER_HORIZONTAL


            leftMargin = (((_avatarSize * .75) * (_maxVisibleAvatars))).toFloat()
            plusIndicator.x = leftMargin
            addView(plusIndicator)
            containerWidth = (leftMargin + _avatarSize).toInt()
            // Configura un OnClickListener para el indicador "+"
            plusIndicator.setOnClickListener {
                // Implementa la lógica para mostrar todos los avatares o abrir una lista completa
                // de avatares cuando se hace clic en el "+"
            }
        }
        this.post(Runnable {
            //measure(0,0)
            /*
             val mainLayoutParams = layoutParams
             mainLayoutParams.width = 1000
             mainLayoutParams.height = getMeasuredHeight()
             this.layoutParams = mainLayoutParams
             */
            invalidate()

            forceLayout()

        })
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(
            makeMeasureSpec(containerWidth, EXACTLY), makeMeasureSpec(measuredHeight, EXACTLY)
        )
    }

    fun updateAvatars(viewers: ArrayList<Avatar>?) {
        this.avatars = viewers!!

        updateUI()
    }


}
