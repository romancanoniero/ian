package com.iyr.ian.ui.friends.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.iyr.ian.AppConstants
import com.iyr.ian.AppConstants.Companion.USERNAME_MINIMUM_LENGTH
import com.iyr.ian.R
import com.iyr.ian.callbacks.OnCompleteCallback
import com.iyr.ian.dao.models.Contact
import com.iyr.ian.glide.GlideApp
import com.iyr.ian.repository.implementations.databases.realtimedatabase.ContactsRepositoryImpl
import com.iyr.ian.repository.implementations.databases.realtimedatabase.StorageRepositoryImpl
import com.iyr.ian.ui.friends.enums.FriendshipStatusEnums
import com.iyr.ian.utils.FirebaseExtensions.downloadUrlWithCache
import com.iyr.ian.utils.StringUtils
import com.iyr.ian.utils.Validators
import com.iyr.ian.utils.coroutines.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class SearchUsersListAdapter(
    mContext: Context, private val itemLayout: Int
) : ArrayAdapter<Any?>(mContext, itemLayout) {
    protected var dataListAllItems: ArrayList<Contact> = ArrayList()
    protected val listFilter: ListFilter = ListFilter(USERNAME_MINIMUM_LENGTH, dataListAllItems)

    private var dataList: ArrayList<Contact> = ArrayList<Contact>()

    private var contactsRepository = ContactsRepositoryImpl()

    override fun getCount(): Int {
        return dataList.size
    }

    override fun getItem(position: Int): Contact {
        Log.d(
            "CustomListAdapter", dataList[position].toString()
        )
        return dataList[position]
    }

    override fun getView(position: Int, _view: View?, parent: ViewGroup): View {
        var view: View? = _view
        if (view == null) {
            view = LayoutInflater.from(parent.context).inflate(itemLayout, parent, false)
        }

        val contact = getItem(position)
        val userImage = view?.findViewById(R.id.user_image) as ImageView
        val strName = view.findViewById(R.id.title) as TextView
        val strSecondLine = view.findViewById(R.id.second_line) as TextView
        val primaryActionButton = view.findViewById(R.id.primary_action_button) as ImageView
        val status = view.findViewById(R.id.status) as TextView

        strName.text = contact.display_name

        if ((contact.email ?: "").isNotEmpty()) {
            strSecondLine.text = contact.email
        } else if ((contact.telephone_number ?: "").isNotEmpty()) {
            strSecondLine.text = contact.telephone_number
        }

        when (contact.status) {
            FriendshipStatusEnums.ACCEPTED.name -> {
                status.text = context.getText(R.string.friends)
                status.visibility = VISIBLE
                primaryActionButton.visibility = GONE
            }

            FriendshipStatusEnums.PENDING.name -> {
                status.text = context.getText(R.string.pending)
                status.visibility = VISIBLE
                primaryActionButton.visibility = GONE
            }

            else -> {
                status.visibility = GONE
                primaryActionButton.visibility = VISIBLE
            }
        }


        if (contact.status != FriendshipStatusEnums.USER_NOT_FOUND.name) {

            var storageRepository = StorageRepositoryImpl()

            GlobalScope.launch(Dispatchers.Main) {

                /*
                              var storageReference = storageRepository.getFileUrl(
                                  PROFILE_IMAGES_STORAGE_PATH,
                                  contact.user_key.toString(),
                                  contact.image?.file_name.toString()
                              )
              */
                var storageReference = FirebaseStorage.getInstance()
                    .getReference(AppConstants.PROFILE_IMAGES_STORAGE_PATH)
                    .child(contact.user_key.toString())
                    .child(contact.image?.file_name.toString())
                    .downloadUrlWithCache(context)
                /*
                //--------------
                var storageReferenceCache = FirebaseStorage.getInstance()
                    .getReference(AppConstants.PROFILE_IMAGES_STORAGE_PATH)
                    .child(event.author_key!!).child(event.author?.profile_image_path.toString())
                    .downloadUrlWithCache(context)
                */
                //-----------------
                GlideApp.with(context).asBitmap().load(storageReference).placeholder(
                    AppCompatResources.getDrawable(
                        context, R.drawable.progress_animation
                    )
                ).error(AppCompatResources.getDrawable(context, R.drawable.ic_error))
                    .into(userImage)

            }
        } else {
            GlideApp.with(context).asBitmap()
                .load(AppCompatResources.getDrawable(context, R.drawable.ic_unknown_user))
                .into(userImage)

            if (!(contact.email ?: "").isEmpty()) {
                primaryActionButton.setImageDrawable(context.getDrawable(R.drawable.ic_envelope))
            } else {
                primaryActionButton.setImageDrawable(context.getDrawable(R.drawable.ic_sms))
            }
        }
        return view
    }

    override fun getFilter(): Filter {
        return listFilter
    }

    fun addAll(contacts: ArrayList<Contact>) {
        dataListAllItems?.clear()
        dataListAllItems?.addAll(contacts)
        filter
    }


    fun updateDate(constraint: String, data: ArrayList<Contact>) {
        listFilter.originalList.clear()
        listFilter.originalList.addAll(data)
        filter.filter(constraint)
    }

    /*
        fun addContact(contact: Contact) {
            dataListAllItems?.add(contact)
            filter
        }
    */
    inner class ListFilter(private val threshold: Int, val originalList: ArrayList<Contact>) :
        Filter() {
        private val lock = Any()
        override fun performFiltering(prefix: CharSequence?): FilterResults {
            val results = FilterResults()
            // Verifica si la longitud del texto de consulta es menor que el umbral
            if (prefix == null || prefix.length < threshold || originalList.isEmpty()) {
                // Si es así, devuelve un FilterResults vacío
                results.values = ArrayList<Contact>()
                results.count = 0
            } else {
                // Si no, realiza el filtrado normal
                val filteredList = originalList.filter {
                    (it.display_name ?: "").contains(prefix.toString(), ignoreCase = true) ||
                            (it.email ?: "").contains(prefix.toString(), ignoreCase = true) ||
                            (it.telephone_number ?: "").contains(
                                prefix.toString(),
                                ignoreCase = true
                            )
                }
                results.values = filteredList
                results.count = filteredList.size
            }
            /*
                      if (this@SearchUsersListAdapter.dataListAllItems?.isEmpty() == true) {

                      } else {
                          var pp = 3
                      }
          *//*
            val matchValues = ArrayList<Contact>()

            results.values = matchValues
            results.count = matchValues.size
            */
            publishResults(prefix, results)
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {

            dataList = if (results.values != null) {
                results.values as ArrayList<Contact>
            } else {
                ArrayList<Contact>()
            }
            if (results.count > 0) {
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }
    }


    private fun onSearchBoxChange(text: String, callback: OnCompleteCallback) {

        if (text.length < 8) {
            Log.d("SEARCHBOX", "Skipeo")
            return
        }


        var proceedWithSearch = false
        if (!StringUtils.areOnlyDigits(text) && !text.contains("@")) {
            proceedWithSearch = true
        } else if (StringUtils.areOnlyDigits(text) && text.length >= 9) {
            proceedWithSearch = true
        } else if (StringUtils.emailAtSymbolPresent(text) && Validators.isValidMail(text)) {
            proceedWithSearch = true
        }

        if (proceedWithSearch) {

            showLoader()

            Log.d("SEARCHBOX", "Busco")
            GlobalScope.launch(Dispatchers.IO) {
                var call = contactsRepository.searchContacts(
                    FirebaseAuth.getInstance().uid.toString(), text
                )
                when (call) {
                    is Resource.Error -> {}
                    is Resource.Loading -> {

                    }

                    is Resource.Success -> {
                        var contacts = call.data
                        if (contacts != null) {
                            callback.onComplete(true, contacts)
                        }

                    }
                }

                var pp = 3
            }


        } else Log.d("SEARCHBOX", "Skipeo")

    }

    private fun onNoData() {
        // esto lo que hace es borrar todo      TODO("Not yet implemented")
    }

    private fun hideTextLoader() {
        //   TODO("Not yet implemented")
    }

    private fun showLoader() {
        //TODO("Not yet implemented")
    }


}