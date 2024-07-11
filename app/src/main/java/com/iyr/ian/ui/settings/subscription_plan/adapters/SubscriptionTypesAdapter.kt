package com.iyr.ian.ui.settings.subscription_plan.adapters

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.iyr.ian.R
import com.iyr.ian.dao.models.SubscriptionPlans
import com.iyr.ian.dao.models.SubscriptionTypes


interface ISubscriptionTypesAdapter {
    fun onSelected(plan: SubscriptionPlans)
    fun onSubscriptionTypesAdapterRendered(maxHeight: Int)
}

class SubscriptionTypesAdapter(
    val context: Context,
    val callback: ISubscriptionTypesAdapter
) :
    RecyclerView.Adapter<SubscriptionTypesAdapter.ViewHolder>() {
    private val viewBinderHelper = ViewBinderHelper()
    private var data: java.util.ArrayList<SubscriptionTypes> =
        ArrayList<SubscriptionTypes>()


    fun saveStates(outState: Bundle?) {
        viewBinderHelper.saveStates(outState)
    }

    fun restoreStates(inState: Bundle?) {
        viewBinderHelper.restoreStates(inState)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subscription_types_adapter, parent, false)


        // Ajusta el ancho del ítem al 80% del ancho de la pantalla
        val displayMetrics = Resources.getSystem().displayMetrics
        val widthPx = displayMetrics.widthPixels * 0.75
        itemView.layoutParams =
            RecyclerView.LayoutParams(widthPx.toInt(), RecyclerView.LayoutParams.WRAP_CONTENT)

        return ViewHolder(itemView)
    }


    override fun getItemCount() = data.size


    private var maxHeight = 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val record: SubscriptionTypes = data[position]

        when(record.access_level){
            0 -> {

                holder.title.text   = context.getString(R.string.subscription_plan_free)
                holder.func1.visibility = View.VISIBLE
                holder.func2.visibility = View.GONE
                holder.func3.visibility = View.GONE
                holder.func4.visibility = View.GONE
                holder.func5.visibility = View.GONE
                holder.func6.visibility = View.GONE
                holder.func7.visibility = View.GONE
            }
            1 -> {
                holder.title.text   = context.getString(R.string.subscription_plan_solidary)
                holder.func1.visibility = View.VISIBLE
                holder.func2.visibility = View.VISIBLE
                holder.func3.visibility = View.VISIBLE
                holder.func4.visibility = View.VISIBLE
                holder.func5.visibility = View.VISIBLE
                holder.func6.visibility = View.VISIBLE
                holder.func7.visibility = View.VISIBLE
            }
            2 -> {
                holder.title.text   = context.getString(R.string.subscription_plan_vip)
                holder.title.visibility = View.VISIBLE
                holder.func1.visibility = View.VISIBLE
                holder.func2.visibility = View.VISIBLE
                holder.func3.visibility = View.VISIBLE
                holder.func4.visibility = View.VISIBLE
                holder.func5.visibility = View.VISIBLE
                holder.func6.visibility = View.VISIBLE
                holder.func7.visibility = View.GONE
            }
        }

    }


    fun getData(): ArrayList<SubscriptionTypes> {
        return data
    }

    fun setData(plans: List<SubscriptionTypes>) {
        data.clear()
        data.addAll(plans)
        notifyDataSetChanged();
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var buyButton: Button = view.findViewById<Button>(R.id.buy_button)
        var title: TextView = view.findViewById(R.id.plan_title)
        var func1: TextView = view.findViewById(R.id.functionality_1)
        var func2: TextView = view.findViewById(R.id.functionality_2)
        var func3: TextView = view.findViewById(R.id.functionality_3)
        var func4: TextView = view.findViewById(R.id.functionality_4)
        var func5: TextView = view.findViewById(R.id.functionality_5)
        var func6: TextView = view.findViewById(R.id.functionality_6)
        var func7: TextView = view.findViewById(R.id.functionality_7)
    }


}