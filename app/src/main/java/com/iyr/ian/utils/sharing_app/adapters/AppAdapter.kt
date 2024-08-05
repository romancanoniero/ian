package com.iyr.ian.utils.sharing_app.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iyr.ian.R

interface SharingContentAdapterInterface {
    fun onAppSelected(packageName : String)
}
class AppAdapter( val apps: List<AppInfo>, val callback : SharingContentAdapterInterface? = null) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val layout : View = view
        val icon: ImageView = view.findViewById(R.id.app_icon)
        val name: TextView = view.findViewById(R.id.app_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.app_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.icon.setImageDrawable(app.icon)
        holder.name.text = app.name
        holder.layout.setOnClickListener {
           callback?.onAppSelected(app.packageName)
        }
    }

    override fun getItemCount() = apps.size
}

