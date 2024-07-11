package com.iyr.ian.sharedpreferences

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.iyr.ian.dao.models.EventFollowed
import com.iyr.ian.dao.models.User
import java.io.Serializable
import java.lang.reflect.Type

class SessionForProfile private constructor(_context: Context) {
    private val editor: SharedPreferences.Editor
    private val pref: SharedPreferences
    private val profileRef: DatabaseReference? = null
    private val PRIVATE_MODE = 0
    val loginMethod: String?
        get() = pref.getString("login_method", "")

    private fun setLoginMethod(string: String) {
        editor.putString("login_method", string)
        editor.commit()
    }

    var loggedInUserKey: String?
        get() = pref.getString("logged_in_key", "")
        set(string) {
            editor.putString("logged_in_key", string)
            editor.commit()
        }
    var languagePreferences: String?
        get() = pref.getString("language_preferences", "en")
        set(mLanguagePreferences) {
            editor.putString("language_preferences", mLanguagePreferences)
            editor.commit()
        }

    /*
    public String getProfileImageUrl() {
        String userAsObject = pref.getString("user_profile", "");
        User user = new Gson().fromJson(userAsObject, User.class);

        for (MediaFile image : user.images) {
            if (image.isFavorite)
                return image.url;
        }
        return user.images.get(0).url;
    }
*/
    private fun setProfileImageUrl(url: String) {
        editor.putString("profile_image_url", url)
        editor.commit()
    }

    /****
     * Starts : Store User setting in the  preferences for the offline storage and without flickering data received
     */
    fun setIsProfileEditFlag(flag: Boolean) {
        editor.putBoolean("is_profile_edit_flag", flag)
        editor.commit()
    }

    val isProfileEdit: Boolean
        get() = pref.getBoolean("is_profile_edit_flag", false)
    var profileIdALreadyLoggedInUser: String?
        get() = pref.getString("set_mStringProfileId", "")!!.trim { it <= ' ' }
        set(mStringProfileId) {
            editor.putString("set_mStringProfileId", mStringProfileId)
            editor.commit()
        }

    //pref.child("display_name").setValue(profile_fullname_);
    var profileFullname: String?
        get() = pref.getString("profile_fullname_", "")!!.trim { it <= ' ' }
        set(profile_fullname_) {
            editor.putString("profile_fullname_", profile_fullname_)
            editor.commit()
            //pref.child("display_name").setValue(profile_fullname_);
        }

    //        profileRef.child("user_name").setValue(profile_username_);
    var profileUsername: String?
        get() {
            val userAsObject = pref.getString("user_profile", "")
            val user = Gson().fromJson(userAsObject, User::class.java)
            return user.display_name.trim { it <= ' ' }
        }
        set(profile_username_) {
            editor.putString("profile_username_", profile_username_)
            editor.commit()
            //        profileRef.child("user_name").setValue(profile_username_);
        }

    //   profileRef.child("email").setValue(profile_fullname_);
    var profileEmail: String?
        get() = pref.getString("profile_email_", "")!!.trim { it <= ' ' }
        set(profile_fullname_) {
            editor.putString("profile_email_", profile_fullname_)
            editor.commit()
            //   profileRef.child("email").setValue(profile_fullname_);
        }

    //    profileRef.child("gender").setValue(profile_gender_);
    var profileGender: String?
        get() = pref.getString("profile_gender_", "")!!.trim { it <= ' ' }
        set(profile_gender_) {
            editor.putString("profile_gender_", profile_gender_)
            editor.commit()
            //    profileRef.child("gender").setValue(profile_gender_);
        }
    var isAdmin: Boolean
        get() = pref.getBoolean("is_admin", false)
        set(isAdmin) {
            editor.putBoolean("is_admin", isAdmin)
            editor.commit()
        }

    //        editor.putString("profile_birthday_", birthday);
    //      editor.commit();
    var profileBirthday: String?
        get() = pref.getString("profile_birthday_", "")!!.trim { it <= ' ' }
        set(birthday_) {
//        editor.putString("profile_birthday_", birthday);
            //      editor.commit();
            profileRef!!.child("dob").setValue(birthday_)
        }
    var profileRealTimeLocationStatus: String?
        get() = pref.getString("profile_real_time_location_status", "")!!.trim { it <= ' ' }
        set(profile_real_time_location_status) {
            editor.putString("profile_real_time_location_status", profile_real_time_location_status)
            editor.commit()
        }
    var profileImageIfOfflineChangedLink_: String?
        get() = pref.getString("profile_image_if_offline_changed_link", "")!!.trim { it <= ' ' }
        set(ProfileImageIfOfflineChangedLink_) {
            editor.putString(
                "profile_image_if_offline_changed_link",
                ProfileImageIfOfflineChangedLink_
            )
            editor.commit()
        }
    var profilePassword: String?
        get() = pref.getString("profile_password_", "")!!.trim { it <= ' ' }
        set(password) {
            editor.putString("profile_password_", password)
            editor.commit()
        }
    var profileImageIfOfflineChangedLink_Uri: String?
        get() = pref.getString("profile_image_if_offline_changed_link_uri", "")!!.trim { it <= ' ' }
        set(profile_image_if_offline_changed_link_uri) {
            editor.putString(
                "profile_image_if_offline_changed_link_uri",
                profile_image_if_offline_changed_link_uri
            )
            editor.commit()
        }

    fun setProfileImageTempPath(postId: String, temPath: String?) {
        editor.putString("ProfileImageTempPath__$postId", temPath)
        editor.commit()
    }

    fun getProfileImageTempPath(postId: String): String {
        return pref.getString("ProfileImageTempPath__$postId", "")!!.trim { it <= ' ' }
    }

    fun storeAsMap(key: String?, map: Map<*, *>?) {
        val gson = Gson()
        val json = gson.toJson(map)
        editor.putString(key, json)
        editor.commit()
    }

    fun storeAsMap(key: String?, `object`: Any?) {
        val gson = Gson()
        val json = gson.toJson(`object`)
        editor.putString(key, json)
        editor.commit()
    }

    fun getAsMap(key: String?): Map<*, *> {
        val gson = Gson()
        val json = pref.getString(key, null)
        return gson.fromJson<Map<*, *>>(json, MutableMap::class.java)
        //      return (ArrayList<?>) gson.fromJson(json, createJavaUtilListParameterizedType(destType.getClass()));
    }

    fun storeAsJson(key: String?, `object`: Any?) {
        val gson = Gson()
        val json = gson.toJson(`object`)
        editor.putString(key, json)
        editor.commit()
    }

    fun getJsonAsClass(key: String?, type: Type?): Any {
        val gson = Gson()
        val json = pref.getString(key, null)
        return gson.fromJson(json, type)
    }

    fun jsonContainsKey(key: String?): Boolean {
        return pref.contains(key)
    }

    fun getAsMap(key: String?, type: Type?): Map<*, *> {
        val gson = Gson()
        val json = pref.getString(key, null)
        return gson.fromJson(json, type)
        //      return (ArrayList<?>) gson.fromJson(json, createJavaUtilListParameterizedType(destType.getClass()));
    }

    //...........//............//...........//...........//.................//............//
    /*
          // For profile drop downs
          // this is used to store gender dropdowns details
          public void storeProfileDropdownsGenderDetails(List<GenderDetail> genderDeteils) {
              Gson gson = new Gson();
              String jsonFavorites = gson.toJson(genderDeteils);
              editor.putString("profile_dropdowns_gender_details", jsonFavorites);
              editor.commit();
      
          }
      
          // to retreive gender dropdowns detals
          public ArrayList<GenderDetail> getProfileDropdownsGenderDetails() {
              List<GenderDetail> gendersDetails;
              if (pref.contains("profile_dropdowns_gender_details")) {
      
                  String jsonFavorites = pref.getString("profile_dropdowns_gender_details", null);
                  Gson gson = new Gson();
      
                  GenderDetail[] favoriteItems = gson.fromJson(jsonFavorites, GenderDetail[].class);
      
                  gendersDetails = Arrays.asList(favoriteItems);
                  gendersDetails = new ArrayList<>(gendersDetails);
      
              } else {
                  return null;
              }
              return (ArrayList<GenderDetail>) gendersDetails;
          }
      
          public String getGenderIdForUpdateProfile(String mGenderTitle) {
              List<GenderDetail> favorites = getProfileDropdownsGenderDetails();
              if (favorites == null)
                  favorites = new ArrayList<>();
      
              for (int i = 0; i < favorites.size(); i++) {
                  if (mGenderTitle.equals(favorites.get(i).getGender())) {
                      return favorites.get(i).getGenderId();
                  }
              }
              return "";
          }
      
           */
    // End for gender details
    // this is used to store Location details
    /*
      
          // TODO : arrglar esto
          public void storeProfileDropdownsLocationDetails(List<LocationDetail> mLocationDetails) {
              Gson gson = new Gson();
              String jsonFavorites = gson.toJson(mLocationDetails);
              editor.putString("profile_dropdowns_location_details", jsonFavorites);
              editor.commit();
      
          }
      
          // to retreive Location detals
          public ArrayList<LocationDetail> getProfileDropdownsLocationDetails() {
              List<LocationDetail> favorites;
              if (pref.contains("profile_dropdowns_location_details")) {
      
                  String jsonFavorites = pref.getString("profile_dropdowns_location_details", null);
                  Gson gson = new Gson();
      
                  LocationDetail[] favoriteItems = gson.fromJson(jsonFavorites, LocationDetail[].class);
      
                  favorites = Arrays.asList(favoriteItems);
                  favorites = new ArrayList<>(favorites);
      
              } else {
                  return null;
              }
              return (ArrayList<LocationDetail>) favorites;
          }
      
          public String getLocationTypeIdForProfileUpdate(String mTypeLoc) {
              List<LocationDetail> favorites = getProfileDropdownsLocationDetails();
              if (favorites == null)
                  favorites = new ArrayList<>();
      
              for (int i = 0; i < favorites.size(); i++) {
                  if (mTypeLoc.equals(favorites.get(i).getTitle())) {
                      return favorites.get(i).getTitleId();
                  }
              }
              return "";
          }
      
           */
    // End for location details
    //..........//........//............//............//..........
    // For addresses
    // This four methods are used for maintaining favorites.
    /* esto es del proyecto
          public void saveLocationAddresses(List<UserAddressModel> favorites) {
              Gson gson = new Gson();
              String jsonFavorites = gson.toJson(favorites);
              editor.putString("location_addresses__", jsonFavorites);
              editor.commit();
      
          }
      
          public void addEditLocationAddresses(UserAddressModel address) {
              List<UserAddressModel> addresses = getLocationAddresses();
              if (addresses == null)
                  addresses = new ArrayList<>();
      
              for (int i = 0; i < addresses.size(); i++) {
                  if (addresses.get(i).address_type == addresses.get(i).address_type) {
                      addresses.set(i, address);
                      saveLocationAddresses(addresses);
                      return;
                  }
              }
              addresses.add(address);
              saveLocationAddresses(addresses);
          }
      
          public void removeLocationAddresses(UserAddressModel address) {
              ArrayList<UserAddressModel> addresses = getLocationAddresses();
              if (addresses != null) {
                  for (int i = 0; i < addresses.size(); i++) {
                      if (addresses.get(i).address_type == addresses.get(i).address_type) {
                          addresses.remove(i);
                          saveLocationAddresses(addresses);
                          return;
                      }
                  }
              }
          }
      
          public void clearLocationAddresses() {
              List<UserAddressModel> addresses = new ArrayList<>();
      
              Gson gson = new Gson();
              String jsonFavorites = gson.toJson(addresses);
              editor.putString("location_addresses__", jsonFavorites);
              editor.commit();
      
          }
      
          public ArrayList<UserAddressModel> getLocationAddresses() {
              List<UserAddressModel> addresses;
              if (pref.contains("location_addresses__")) {
      
                  String jsonAddresses = pref.getString("location_addresses__", null);
                  Gson gson = new Gson();
      
                  UserAddressModel[] favoriteItems = gson.fromJson(jsonAddresses, UserAddressModel[].class);
      
                  addresses = Arrays.asList(favoriteItems);
                  addresses = new ArrayList<>(addresses);
      
              } else {
                  return null;
              }
              return (ArrayList<UserAddressModel>) addresses;
          }
      
      */
    // End for address
    // Start PhoneNumbers
    val lastContactPhoneNumber: String?
        get() {
            var phoneNumber = ""
            phoneNumber = if (pref.contains("contact_phone_number")) {
                val jsonFavorites = pref.getString("contact_phone_number", null)
                val gson = Gson()
                gson.fromJson(jsonFavorites, String::class.java)
            } else {
                return null
            }
            return phoneNumber
        }

    @JvmName("setLastContactPhoneNumber1")
    fun setLastContactPhoneNumber(phoneNumber: String?) {
        val gson = Gson()
        val jsonFavorites = gson.toJson(phoneNumber)
        editor.putString("contact_phone_number", jsonFavorites)
        editor.commit()
    }
    // End for PhoneNumbers
    // Start Add children
    /*
    public void saveAddChildList(List<ChildrenDetailss> favorites) {
        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(favorites);
        editor.putString("add_childrens__", jsonFavorites);
        editor.commit();

    }

    public void addAddChild(ChildrenDetailss product) {
        List<ChildrenDetailss> favorites = getAddChild();
        if (favorites == null)
            favorites = new ArrayList<>();
        favorites.add(product);
        saveAddChildList(favorites);
    }

    public void removeAddChild(ChildrenDetailss product) {
        ArrayList<ChildrenDetailss> favorites = getAddChild();
        if (favorites != null) {
            favorites.remove(product);
            saveAddChildList(favorites);
        }
    }

    public ArrayList<ChildrenDetailss> getAddChild() {
        List<ChildrenDetailss> favorites;
        if (pref.contains("add_childrens__")) {

            String jsonFavorites = pref.getString("add_childrens__", null);
            Gson gson = new Gson();

            ChildrenDetailss[] favoriteItems = gson.fromJson(jsonFavorites, ChildrenDetailss[].class);

            favorites = Arrays.asList(favoriteItems);
            favorites = new ArrayList<>(favorites);

        } else
            return null;

        return (ArrayList<ChildrenDetailss>) favorites;
    }

     */
    // End Add children
    /*****
     * End of Store user setting
     */
    /*
    public void logoutUser() {
        AppClass.getInstance().setStartingTime(0);
        FirebaseDatabase.getInstance().getReference(TABLE_USERS).
                child(FirebaseAuth.getInstance().getUid())
                .child("login_time_in_millis").setValue(0);

        editor.remove("user_profile");
        editor.commit();
    }
*/
    fun setProfileProperty(key: String?, value: Any) {
        if (value is Int) {
            editor.putInt(key, value)
        } else if (value is Float) {
            editor.putFloat(key, value)
        } else if (value is String) {
            editor.putString(key, value.toString())
        } else if (value is Boolean) {
            editor.putBoolean(key, value)
        } else if (value is Serializable) {
            val gson = Gson()
            val json = gson.toJson(value)
            editor.putString(key, json)
        } else if (value is HashMap<*, *>) {
            val gson = Gson()
            val json = gson.toJson(value)
            editor.putString(key, json)
        }
        editor.commit()
    }

    fun getProfileProperty(key: String): Any? {
        val props = pref.all
        if (props.containsKey(key)) {
            var value = props[key]
            return if (value is String) {
                try {
                    val gson = Gson()
                    value = gson.fromJson(value as String?, Any::class.java)
                    value
                } catch (ex: JsonSyntaxException) {
                    value
                }
            } else value
        }
        return null
    }

    /*
    public Object getProfileProperty(String key, Object defValue) {
        Map<String, ?> props = pref.getAll();
        if (props.containsKey(key)) {
            Object value = props.get(key);
            if (value instanceof String) {
                try {
                    Gson gson = new Gson();
                    value = gson.fromJson((String) value, Object.class);
                    return value;
                } catch (com.google.gson.JsonSyntaxException ex) {
                    return value;
                }

            }
            return value;
        }


        return defValue;
    }
*/
    fun getProfileProperty(key: String, type: Type?): Any? {
        val props = pref.all
        if (props.containsKey(key)) {
            var value = props[key]
            if (value is String) {
                return try {
                    val gson = Gson()
                    value = gson.fromJson(value as String?, type)
                    value
                } catch (ex: JsonSyntaxException) {
                    value
                }
            } else if (value is Boolean) {
                return try {
                    val gson = Gson()
                    value = gson.fromJson(value as String?, type)
                    value
                } catch (ex: JsonSyntaxException) {
                    value
                }
            }
            return value
        }
        return null
    }

    fun getProfileProperty(key: String, defaultValue: Any): Any {
        val result = getProfileProperty(key)
        return result ?: defaultValue
    }

    fun getProfilePropertyFromJsonArray(key: String, type: Type?): Any? {
        val props = pref.all
        if (props.containsKey(key)) {
            var value = props[key]
            return if (value is String) {
                try {
                    val gson = Gson()
                    //   Type typeMyType = new TypeToken<ArrayList<classTo.>>(){}.getType();
                    value = gson.fromJson(value as String?, type)
                    value
                } catch (ex: JsonSyntaxException) {
                    value
                }
            } else value
        }
        return null
    }

    fun removeProfileProperty(key: String) {
        val props = pref.all
        if (props.containsKey(key)) {
            editor.putString(key, null)
            editor.remove(key)
            editor.apply()
        }
    }

    fun getLoggedInProfileTypeId(): Int {
        return pref.getInt("logged_in_profile_type_id", 0)
    }

    fun setLoggedInProfileTypeId(profileTypeId: Int) {
        editor.putInt("logged_in_profile_type_id", profileTypeId)
        editor.commit()
    }

    fun storeUserProfile(user: User?) {
        val gson = Gson()
        val json = gson.toJson(user)
        editor.putString("user_profile", json)
        editor.commit()
    }

    fun getUserProfile(): User {
        val gson = Gson()
        return gson.fromJson(pref.getString("user_profile", ""), User::class.java)
    }

    fun getUserId(): String {
        try {
            val gson = Gson()
            return gson.fromJson(pref.getString("user_profile", ""), User::class.java).user_key
                ?: ""
        } catch (e: Exception) {
            // dispara una excepcion si no hay usuario logueado
            return ""
        }
    }


    fun storeDeviceId(deviceId: String) {
        editor.putString("device_id", deviceId)
        editor.commit()
    }

    fun getDeviceId(): String? {
        return pref.getString("device_id", "")
    }

    fun setPendingAction(action: String, key: String) {
        val map = HashMap<String, Any>()
        map["action"] = action
        map["key"] = key
        setProfileProperty("pending_actions", map)
    }

    fun consumePendingAction() {
        removeProfileProperty("pending_actions")
    }

    fun logout() {
        editor.clear()
        editor.apply()
    }

    fun deletePendingValidationData(email: String) {
        removeProfileProperty("PENDING_EMAIL")
        removeProfileProperty(email)
    }

    fun addEventToUser(event: EventFollowed) {
        val me = getUserProfile()
        if (me.events == null) {
            me.events = ArrayList<EventFollowed>()
        }
        if (!me.events.contains(event)) {
            me.events.add(event)
        }
        storeUserProfile(me)
    }

    fun updateEventInUser(event: EventFollowed) {

        val me = getUserProfile()
        if (me.events != null) {
            val index = me.events.indexOf(event)
            if (index == -1) {
                me.events.add(event)
            } else {
                me.events[index] = event
            }
            storeUserProfile(me)
        }
    }

    fun removeEventInUser(event: EventFollowed) {
        val me = getUserProfile()
        if (me.events != null) {
            val events: ArrayList<EventFollowed> = me.events
            val index = events.indexOf(event)
            if (index > -1) {
                events.removeAt(index)
            }
            storeUserProfile(me)
        }
    }

    fun eventFollowedAdd(recordToAdd: EventFollowed) {

        val me = getUserProfile()
        if (me.events == null) {
            me.events = ArrayList<EventFollowed>()
        }
        if (!me.events.contains(recordToAdd)) {
            me.events.add(recordToAdd)
        }
        storeUserProfile(me)
    }

    fun getEventsFollowed(): java.util.ArrayList<EventFollowed> {

        val me = getUserProfile()
        if (me.events == null) {
            me.events = ArrayList<EventFollowed>()
        }
        return me.events
    }


    companion object {
        private const val PREF_NAME = "self_ordering_app_profile"
        private var sSoleInstance: SessionForProfile? = null
        fun getInstance(context: Context): SessionForProfile {
            if (sSoleInstance == null || FirebaseAuth.getInstance().currentUser == null) { //if there is no instance available... create new one
                sSoleInstance = SessionForProfile(context)
            }
            return sSoleInstance!!
        }
    }


    enum class ProfileProperies(val propertyName: String) {
        IS_PULSE_OK("is_pulse_ok")
    }

    init {
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref.edit()
    }
}