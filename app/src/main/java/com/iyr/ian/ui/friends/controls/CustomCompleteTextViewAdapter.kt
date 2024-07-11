package com.iyr.ian.ui.friends.controls

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.iyr.ian.R
import com.iyr.ian.dao.models.Contact
import com.iyr.ian.dao.models.UserMinimum
import com.iyr.ian.utils.UIUtils.handleTouch
import de.hdodenhof.circleimageview.CircleImageView

/*
interface OnActionSelected {
    fun onAddFriendAction(user: UserMinimum)
}
*/
class CustomCompleteTextViewAdapter(
    context: Context,
    resource: Int) :
    ArrayAdapter<UserMinimum?>(context, resource),Filterable {
    // private var callback: OnActionSelected? = null
    var items: ArrayList<UserMinimum>? = null
    private val currentContacts: ArrayList<Contact> by lazy { ArrayList<Contact>() }

    //   var context: Context
    override fun getCount(): Int {
        return items?.size ?: 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view: View? = convertView
        if (convertView == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.row_friends_search_adapter, parent, false)
        }
        val user = items?.get(position)!!
        if (user != null) {
            val userName = view!!.findViewById(R.id.user_name) as TextView
            val userImage = view.findViewById(R.id.profile_image) as CircleImageView
            val actionImage = view.findViewById(R.id.action_image) as ImageView

            userName.text = user.display_name
            Glide.with(context)
                .asBitmap()
                .load(user.image.file_name)
                .into(userImage)

            var index = -1
            currentContacts.forEach { existing ->
                index += 1
                if (existing.user_key == user.user_key) {
                    return@forEach
                }
            }

            if (index == -1) {
                Glide.with(context)
                    .asBitmap()
                    .load(R.drawable.ic_friend_add)
                    .into(actionImage)

                actionImage.setOnClickListener {
//                    UIUtils.vibrateOnTouch(context)
                    context.handleTouch()
                    /*
                    if (callback != null && callback is OnActionSelected) {
                        callback!!.onAddFriendAction(user)
                    }

                     */
                }
            } else {
                Glide.with(context)
                    .asBitmap()
                    .load(R.drawable.ic_friend_already_friend)
                    .into(actionImage)
            }


        }
        return view!!
    }


    override fun getFilter(): Filter {
        return nameFilter
    }
/*
    fun setCallback(callback: OnActionSelected) {
        this.callback = callback
    }
*/

    /**
     * Custom Filter implementation for custom suggestions we provide.
     */

    private var nameFilter: Filter = object : Filter() {

        override fun convertResultToString(resultValue: Any): CharSequence {
            return (resultValue as UserMinimum).display_name
        }


        override fun performFiltering(constraint: CharSequence?): FilterResults {
            if (constraint != null) {
                val suggestions = ArrayList<UserMinimum>()
                suggestions.clear()
                for (user in (this@CustomCompleteTextViewAdapter).items!!) {

                    var index = -1
                    currentContacts.forEach { existing ->
                        index += 1
                        if (existing.user_key == user.user_key) {
                            return@forEach
                        }
                    }
                    if (index == -1) {
                        suggestions.add(user)
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = suggestions
                filterResults.count = (filterResults.values as ArrayList<UserMinimum>).size
                return filterResults
            } else {
                return FilterResults()
            }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results != null && results.count > 0) {
                items?.clear()
                items?.addAll(results.values as ArrayList<UserMinimum>)
                notifyDataSetChanged()
            }
        }
    }

    init {
        this.items = ArrayList<UserMinimum>()
    }
}