package com.iyr.ian.utils.components.multi_select_spinner


import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import com.iyr.ian.ui.contacts_groups.multi_select_spinner.model.KeyPairBoolData
import com.iyr.ian.R
import java.util.*


interface MultiSpinnerInterface {
    fun onAccept(selecteds: ArrayList<KeyPairBoolData>)
    fun onCancel() {}

}

class MultiSpinnerSearch(val activity: Activity, val layoutInflater: LayoutInflater) : AppCompatSpinner(layoutInflater.context), DialogInterface.OnCancelListener {
    private var notificationListKey: String = ""
    private var callback: MultiSpinnerInterface? = null
   // private val contacts: ArrayList<Contact> = ArrayList<Contact>()
    private lateinit var buttonClearAll: Button
    private lateinit var buttonCancel: Button
    private lateinit var buttonOk: Button
    private var highlightSelected = false
    private var highlightColor = ContextCompat.getColor(context, R.color.material_orange500)
    private var textColor = Color.GRAY
    private var limit = -1
    private var selected = 0
    private var defaultText: String? = ""
    private var spinnerTitle: String? = ""
    private var emptyTitle = "Not Found!"
    private var searchHint = "Type to search"
    private var clearText: String? = "Clear All"
    var isColorSeparation = false
    var isShowSelectAllButton = false
    private var listener: MultiSpinnerListener? = null
    private var limitListener: LimitExceedListener? = null
    private var adapter: MyAdapter? = null
    private var items: kotlin.collections.ArrayList<KeyPairBoolData>? = null

    var isSearchEnabled = true

/*
    val contactsRef = FirebaseDatabase.getInstance()
        .getReference(TABLE_USERS_CONTACTS)
        .child(FirebaseAuth.getInstance().uid.toString())

    val contactsRefListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            items?.clear()
            snapshot.children.forEach { child ->
                val contact: Contact = child.getValue(Contact::class.java)!!
                contact.user_key = child.key
                var newRecord = KeyPairBoolData(contact.display_name, false)
                newRecord.theObject = contact

                items?.add(newRecord)
            }
            subscribeToMembers()
        }

        override fun onCancelled(error: DatabaseError) {
            //TODO("Not yet implemented")
        }
    }


    lateinit var membersListRef: DatabaseReference

    val membersListListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val selected = snapshot.getValue(Contact::class.java)!!
            items?.forEach { record ->
                if ((record.theObject as Contact).user_key == selected.user_key) {
                    record.isSelected = true

                }
            }
            adapter?.notifyDataSetChanged()
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            //          TODO("Not yet implemented")
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {

            val unSelected = snapshot.getValue(Contact::class.java)!!
            items?.forEach { record ->
                if ((record.theObject as Contact).user_key == unSelected.user_key) {
                    record.isSelected = true
                    adapter?.notifyDataSetChanged()
                }
            }

        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            //          TODO("Not yet implemented")
        }

        override fun onCancelled(error: DatabaseError) {
            //        TODO("Not yet implemented")
        }
    }
*/
/*
    constructor(context: Context?) : super(context!!) {}
  */



    /*
    constructor(context: Context, arg1: AttributeSet?) :  super(context!!,arg1) {

        val a = context.obtainStyledAttributes(arg1, R.styleable.MultiSpinnerSearch)
        for (i in 0 until a.indexCount) {
            val attr = a.getIndex(i)
            if (attr == R.styleable.MultiSpinnerSearch_hintText) {
                hintText = a.getString(attr)
                spinnerTitle = hintText
                defaultText = spinnerTitle
                break
            } else if (attr == R.styleable.MultiSpinnerSearch_highlightSelected) {
                highlightSelected = a.getBoolean(attr, false)
            } else if (attr == R.styleable.MultiSpinnerSearch_highlightColor) {
                highlightColor =
                    a.getColor(attr, ContextCompat.getColor(context, R.color.secondaryTextColor))
            } else if (attr == R.styleable.MultiSpinnerSearch_textColor) {
                textColor = a.getColor(attr, Color.GRAY)
            } else if (attr == R.styleable.MultiSpinnerSearch_clearText) {
                setClearText(a.getString(attr))
            }
        }
        a.recycle()
    }
*/


    /*
    private fun subscribeToContacts() {
        contactsRef.addValueEventListener(contactsRefListener)
    }

    private fun unSubscribeToContacts() {

        contactsRef.removeEventListener(contactsRefListener)

    }


    private fun subscribeToMembers() {

        items?.forEach { record ->
            record.isSelected = false
        }
        adapter?.notifyDataSetChanged()
        membersListRef = FirebaseDatabase.getInstance()
            .getReference(TABLE_USERS_NOTIFICATIONS_GROUPS)
            .child(FirebaseAuth.getInstance().uid.toString())
            .child(notificationListKey)
            .child("members")
        membersListRef.addChildEventListener(membersListListener)
    }


    private fun unSubscribeToMembers() {
        membersListRef.removeEventListener(membersListListener)
    }
*/
/*
    constructor(arg0: Context?, arg1: AttributeSet?, arg2: Int) : super(
        arg0!!, arg1, arg2
    ) {
    }
*/
    var hintText: String?
        get() = spinnerTitle
        set(hintText) {
            spinnerTitle = hintText
            defaultText = spinnerTitle
        }

    fun setClearText(clearText: String?) {
        this.clearText = clearText
    }

    fun setLimit(limit: Int, listener: LimitExceedListener?) {
        this.limit = limit
        limitListener = listener
        isShowSelectAllButton = false // if its limited, select all default false.
    }

    val selectedItems: List<KeyPairBoolData>
        get() {
            val selectedItems: MutableList<KeyPairBoolData> = ArrayList()
            for (item in items!!) {
                if (item.isSelected) {
                    selectedItems.add(item)
                }
            }
            return selectedItems
        }
    val selectedIds: List<Long>
        get() {
            val selectedItemsIds: MutableList<Long> = ArrayList()
            for (item in items!!) {
                if (item.isSelected) {
                    selectedItemsIds.add(item.id)
                }
            }
            return selectedItemsIds
        }

    override fun onCancel(dialog: DialogInterface) {
        // refresh text on spinner
        val spinnerBuffer = StringBuilder()
        val selectedData = ArrayList<KeyPairBoolData>()


        /*
           for (i in items!!.indices) {
               val currentData = items!![i]
               if (currentData.isSelected) {
                   selectedData.add(currentData)
                   spinnerBuffer.append(currentData.name)
                   spinnerBuffer.append(", ")
               }
           }


           var spinnerText: String? = spinnerBuffer.toString()
           spinnerText =
               if (spinnerText!!.length > 2) spinnerText.substring(
                   0,
                   spinnerText.length - 2
               ) else hintText




           val adapterSpinner =
               ArrayAdapter(context, R.layout.textview_for_spinner, arrayOf(spinnerText))
           setAdapter(adapterSpinner)

           if (adapter != null) adapter!!.notifyDataSetChanged()
           listener!!.onItemsSelected(selectedData)
   */
        onDetachedFromWindow()
    }

    override fun performClick(): Boolean {
        super.performClick()

        val displayMetrics = DisplayMetrics()
        activity.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels

        builder = AlertDialog.Builder(context)
        builder!!.setTitle(spinnerTitle)

        val view: View = layoutInflater.inflate(R.layout.alert_dialog_listview_search, null)

        buttonOk = view.findViewById<Button>(R.id.button_ok)
        buttonCancel = view.findViewById<Button>(R.id.button_cancel)
        buttonClearAll = view.findViewById<Button>(R.id.button_clear_all)


        val listView = view.findViewById<ListView>(R.id.alertSearchListView)
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        listView.isFastScrollEnabled = false
        adapter = MyAdapter(context, items!!)
        listView.adapter = adapter
        val emptyText = view.findViewById<TextView>(R.id.empty)
        emptyText.text = emptyTitle
        listView.emptyView = emptyText
        val editText = view.findViewById<EditText>(R.id.alertSearchEditText)
        if (isSearchEnabled) {
            editText.visibility = VISIBLE
            editText.hint = searchHint
            editText.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    adapter!!.filter.filter(s.toString())
                }

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun afterTextChanged(s: Editable) {}
            })
        } else {
            editText.visibility = GONE
        }


        // For selected items
        //selected = 0
        for (i in items!!.indices) {
            if (items!![i].isSelected) selected++
        }


        // Added Select all Dialog Button.


        if (isShowSelectAllButton && limit == -1) {
            builder!!.setNeutralButton(
                R.string.selectAll
            ) { dialog: DialogInterface, which: Int ->
                for (i in adapter!!.arrayList.indices) {
                    adapter!!.arrayList[i].isSelected = true
                }
                adapter!!.notifyDataSetChanged()
                // To call onCancel listner and set title of selected items.
                dialog.cancel()
            }
        }

        buttonOk.setOnClickListener { dialog ->

            callback?.onAccept(selectedItems as ArrayList<KeyPairBoolData>)
            ad.cancel()
        }


        buttonClearAll.setOnClickListener { dialog ->
            for (i in adapter!!.arrayList.indices) {
                adapter!!.arrayList[i].isSelected = false
            }
            adapter!!.notifyDataSetChanged()
            ad.cancel()
        }

        buttonCancel.setOnClickListener { dialog ->
            ad.cancel()
        }

        builder!!.setOnCancelListener(this)


        ad = builder!!.create()
        ad.show()
        ad.window?.setContentView(view)


        ad.window?.setLayout((width * .90).toInt(), (height * .90).toInt())

        view.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                //subscribeToContacts()
            }

            override fun onViewDetachedFromWindow(v: View) {
              //  unSubscribeToContacts()
              //  unSubscribeToMembers()
                ad.dismiss()
            }
        })

        //     ad.getWindow()?.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        return true
    }


    fun setItems(items: ArrayList<KeyPairBoolData>, listener: MultiSpinnerListener?) {
        this.items = items
        this.listener = listener
        val spinnerBuffer = StringBuilder()
        for (i in items.indices) {
            if (items[i].isSelected) {
                spinnerBuffer.append(items[i].name)
                spinnerBuffer.append(", ")
            }
        }
        if (spinnerBuffer.length > 2) defaultText =
            spinnerBuffer.toString().substring(0, spinnerBuffer.toString().length - 2)
        val adapterSpinner =
            ArrayAdapter(context, R.layout.textview_for_spinner, arrayOf(defaultText))
        setAdapter(adapterSpinner)
    }

    fun setEmptyTitle(emptyTitle: String) {
        this.emptyTitle = emptyTitle
    }

    fun setSearchHint(searchHint: String) {
        this.searchHint = searchHint
    }

    fun setCallbackListener(callback: MultiSpinnerInterface) {
        this.callback = callback

    }

    fun setNotificationListKey(notificationListKey: String) {
        this.notificationListKey = notificationListKey
    }

    interface LimitExceedListener {
        fun onLimitListener(data: KeyPairBoolData?)
    }

    //Adapter Class
    inner class MyAdapter internal constructor(
        context: Context,
        var arrayList: List<KeyPairBoolData>
    ) :
        BaseAdapter(), Filterable {
        val mOriginalValues // Original Values
                : List<KeyPairBoolData>
        val inflater: LayoutInflater

        init {
            mOriginalValues = arrayList
            inflater = LayoutInflater.from(context)
        }

        override fun getCount(): Int {
            return arrayList.size
        }

        override fun getItem(position: Int): Any {
            return position
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertViewParam: View?, parent: ViewGroup): View? {
//            //Log.i(TAG, "getView() enter");
            var convertView = convertViewParam
            val holder: ViewHolder
            if (convertView == null) {
                holder = ViewHolder()
                convertView = inflater.inflate(R.layout.item_listview_multiple, parent, false)
                holder.textView = convertView.findViewById(R.id.alertTextView)
                holder.checkBox = convertView.findViewById(R.id.alertCheckbox)
                convertView.tag = holder
            } else {
                holder = convertView.tag as ViewHolder
            }
            var background = R.color.white
            if (isColorSeparation) {
                val backgroundColor: Int =
                    if (position % 2 == 0) R.color.white else R.color.light_gray
                background = backgroundColor
                convertView?.setBackgroundColor(ContextCompat.getColor(context, backgroundColor))
            }
            val data = arrayList[position]
            holder.textView!!.text = data.name
            holder.checkBox!!.isChecked = data.isSelected
            convertView?.setOnClickListener { v: View ->
                if (data.isSelected) { // deselect
                    selected--
                } else { // selected
                    selected++
                    if (selected > limit && limit > 0) {
                        --selected // select with limit
                        if (limitListener != null) limitListener!!.onLimitListener(data)
                        return@setOnClickListener
                    }
                }
                val temp =
                    v.tag as ViewHolder
                temp.checkBox!!.isChecked = !temp.checkBox!!.isChecked
                data.isSelected = !data.isSelected
                //Log.i(TAG, "On Click Selected Item : " + data.getName() + " : " + data.isSelected());
                notifyDataSetChanged()
            }
            if (data.isSelected) {
                holder.textView!!.setTextColor(textColor)
                if (highlightSelected) {
                    holder.textView!!.setTypeface(null, Typeface.BOLD)
                    convertView?.setBackgroundColor(highlightColor)
                } else {
                    convertView?.setBackgroundColor(Color.WHITE)
                }
            } else {
                holder.textView!!.setTypeface(null, Typeface.NORMAL)
                convertView?.setBackgroundColor(ContextCompat.getColor(context, background))
            }
            holder.checkBox!!.tag = holder
            return convertView
        }

        @SuppressLint("DefaultLocale")
        override fun getFilter(): Filter {
            return object : Filter() {
                override fun publishResults(constraint: CharSequence, results: FilterResults) {
                    arrayList = results.values as List<KeyPairBoolData> // has the filtered values
                    notifyDataSetChanged() // notifies the data with new filtered values
                }

                override fun performFiltering(constraint: CharSequence): FilterResults {
                    var constraint: CharSequence? = constraint
                    val results =
                        FilterResults() // Holds the results of a filtering operation in values
                    val FilteredArrList: MutableList<KeyPairBoolData> = ArrayList()


                    /*
					 *
					 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
					 *  else does the Filtering and returns FilteredArrList(Filtered)
					 *
					 **/if (constraint == null || constraint.length == 0) {

                        // set the Original result to return
                        results.count = mOriginalValues.size
                        results.values = mOriginalValues
                    } else {
                        constraint = constraint.toString().lowercase(Locale.getDefault())
                        for (i in mOriginalValues.indices) {
                            //Log.i(TAG, "Filter : " + mOriginalValues.get(i).getName() + " -> " + mOriginalValues.get(i).isSelected());
                            val data = mOriginalValues[i].name
                            if (data?.lowercase(Locale.getDefault())
                                    ?.contains(constraint.toString())!!
                            ) {
                                FilteredArrList.add(mOriginalValues[i])
                            }
                        }
                        // set the Filtered result to return
                        results.count = FilteredArrList.size
                        results.values = FilteredArrList
                    }
                    return results
                }
            }
        }

        private inner class ViewHolder {
            var textView: TextView? = null
            var checkBox: CheckBox? = null
        }
    }

    companion object {
        var builder: AlertDialog.Builder? = null
        lateinit var ad: AlertDialog
    }
}