package com.iyr.ian.ui.settings.subscription_plan.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.iyr.ian.R
import com.iyr.ian.dao.models.SubscriptionPlans
import com.iyr.ian.utils.UIUtils.handleTouch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


interface ISubscriptionPlansAdapter {
    fun onBuy(plan: SubscriptionPlans)
}

class SubscriptionPlansAdapter(
    val context: Context,
    val callback: ISubscriptionPlansAdapter
) :
    RecyclerView.Adapter<SubscriptionPlansAdapter.ViewHolder>() {
    private val viewBinderHelper = ViewBinderHelper()
    private var data: java.util.ArrayList<SubscriptionPlans> =
        ArrayList<SubscriptionPlans>()

    init {
    }

    fun saveStates(outState: Bundle?) {
        viewBinderHelper.saveStates(outState)
    }

    fun restoreStates(inState: Bundle?) {
        viewBinderHelper.restoreStates(inState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subscription_plan_adapter, parent, false)
    )

    override fun getItemCount() = data.size //data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val record: SubscriptionPlans = data[position]


        holder.buyButton.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                context.handleTouch()
                callback.onBuy(record)
            }
        }


        /*
          val record = data[position]

                GlideApp.with(context)
                    .asBitmap()
                    .load(storageReference)
                    .placeholder(AppCompatResources.getDrawable(context, R.drawable.progress_animation))
                    .error(AppCompatResources.getDrawable(context, R.drawable.ic_error_red_24dp))
                    .into(holder.userImage)


                holder.userName.text = record.display_name

                Glide.with(context)
                    .asBitmap()
                    .load(R.drawable.ic_delete)
                    .into(holder.primaryActionButton)


                //               holder.primaryActionText.text = context.getText(R.string.remove)
                holder.primaryActionButton.setOnClickListener {
                    //   callback.clearSearchBox()
                    context.handleTouch()
                    callback.removeMember(record.user_key)
                }

                holder.switchSpeedDial.setOnCheckedChangeListener { _, enabled ->
                    ContactsWSClient.instance.updateSpeedDialStatus(
                        record.user_key,
                        enabled,
                        object : OnCompleteCallback {
                            override fun onComplete(success: Boolean, result: Any?) {

                            }
                        })
                }
        */

    }


    fun getData(): ArrayList<SubscriptionPlans> {
        return data
    }

    fun setData(plans: List<SubscriptionPlans>) {
        data.clear()
        data.addAll(plans)
        notifyDataSetChanged();
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var buyButton: Button = view.findViewById<Button>(R.id.buy_button)

        /*
                var userImage: ImageView = view.findViewById<ImageView>(R.id.user_image)
                var userName: TextView = view.findViewById<TextView>(R.id.user_name)
                var primaryActionButton: ImageView =
                    view.findViewById<ImageView>(R.id.primary_action_button)


                var switchSpeedDial: Switch = view.findViewById<Switch>(R.id.switch_speed_dial)
                var switchSpeedDialSection: LinearLayout =
                    view.findViewById<LinearLayout>(R.id.speed_dial_section)
                var secondLine: TextView = view.findViewById<TextView>(R.id.second_line)
        */
    }


}