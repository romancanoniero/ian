package com.iyr.ian.sharedpreferences;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class SessionApp {


    public boolean BTPanicButtonEnabled;
    protected SharedPreferences pref;

    private final int PRIVATE_MODE;

    protected Context _context;

    private static final String PREF_NAME = "ian_app_preferences";

    protected static SessionApp sSoleInstance;
    //private final String mPreferencesName;
    protected SharedPreferences.Editor editor;

    protected SessionApp(Context context) {
        PRIVATE_MODE = 0;
        _context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }


    public static SessionApp getInstance(Context context) {

        if (sSoleInstance == null || FirebaseAuth.getInstance().getCurrentUser() == null) { //if there is no instance available... create new one
            sSoleInstance = new SessionApp(context);
        }

        return sSoleInstance;
    }


    public void setIsLogged(boolean flag) {
        editor.putBoolean("is_Login", flag);
        editor.commit();
    }

    public boolean isLogged() {
        return pref.getBoolean("is_Login", false);
    }


    public void isInPanic(Boolean value)
    {
        editor.putBoolean("isInPanic", value);
        editor.commit();
    }
/*
    public Boolean isInPanic()
    {

        return pref.getBoolean("isInPanic", false);
    }
*/

    public void isBTPanicButtonEnabled(Boolean enabled)
    {
        editor.putBoolean("BTPanicButtonEnabled", enabled);
        editor.commit();
    }

    public Boolean isBTPanicButtonEnabled()
    {
        return pref.getBoolean("BTPanicButtonEnabled", false);
    }


    /*
    public String getLanguagePreferences() {
        return pref.getString("language_preferences", "en");
    }

    public void setLanguagePreferences(String mLanguagePreferences) {
        editor.putString("language_preferences", mLanguagePreferences);
        editor.commit();
    }


 */

    public String getLoggedInId() {
        return pref.getString("logged_in_id", "");
    }

    public void setLoggedInId(String id) {
        editor.putString("logged_in_id", id);
        editor.commit();
    }

    /*
    public void setLoggedInUserKey(String string) {
        editor.putString("logged_in_key", string);
        editor.commit();
    }

    public String getLoggedInUserKey() {
        return pref.getString("logged_in_key", "");
    }


     */

    public void setLoggedInProfileTypeId(int profileTypeId) {
        editor.putInt("logged_in_profile_type_id", profileTypeId);
        editor.commit();
    }

    public Integer getLoggedInProfileTypeId() {
        return pref.getInt("logged_in_profile_type_id", 0);


    }

/*

    public String getLoggedInUsername() {
        return pref.getString("logged_in_username", "");
    }

    public void setLoggedInUsername(String username) {
        editor.putString("logged_in_username", username);
        editor.commit();
    }

*/

    public String getLoggedInEmail() {
        return pref.getString("logged_in_email", "");
    }

    public void setLoggedInEmail(String id) {
        editor.putString("logged_in_email", id);
        editor.commit();
    }


    public String getLoggedInAuthKey() {
        return pref.getString("logged_in_auth_key", "");
    }

    public void setLoggedInAuthKey(String logged_in_auth_key) {
        editor.putString("logged_in_auth_key", logged_in_auth_key);
        editor.commit();
    }

    public String getLoggedInRoleId() {
        return pref.getString("logged_in_role_id", "");
    }

    public void setLoggedInRoleId(String logged_in_auth_key) {
        editor.putString("logged_in_role_id", logged_in_auth_key);
        editor.commit();
    }

    /*
    lo saco porque esto va en la sesion de usuario
        public String getLoggedInUserImage() {
            return pref.getString("logged_in_user_image", "");
        }

        public void setLoggedInUserImage(String logged_in_user_image) {
            editor.putString("logged_in_user_image", logged_in_user_image);
            editor.commit();
        }



     */
    public String getLoggedIn_checkname() {
        return pref.getString("logged_in_user_checkname", "");
    }

    public void setLoggedIn_checkname(String logged_in_user_checkname) {
        editor.putString("logged_in_user_checkname", logged_in_user_checkname);
        editor.commit();
    }


    public String getLoggedIn_checkgender() {
        return pref.getString("logged_in_user_checkgender", "");
    }

    public void setLoggedIn_checkgender(String logged_in_user_checkgender) {
        editor.putString("logged_in_user_checkgender", logged_in_user_checkgender);
        editor.commit();
    }


    public String getLoggedIn_checkDOB() {
        return pref.getString("logged_in_user_checkDOB", "");
    }

    public void setLoggedIn_checkDOB(String logged_in_user_checkDOB) {
        editor.putString("logged_in_user_checkDOB", logged_in_user_checkDOB);
        editor.commit();
    }


    public void storeArrayList(String key, ArrayList array) {
        Gson gson = new Gson();
        String json = gson.toJson(array);
        editor.putString(key, json);
        editor.commit();
    }


    public void storeAsHashMap(String key, HashMap map) {
        Gson gson = new Gson();
        String json = gson.toJson(map);
        editor.putString(key, json);
        editor.commit();
    }

    public HashMap getHashMap(String key) {
        Gson gson = new Gson();
        String json = pref.getString(key, null);
        return gson.fromJson(json, HashMap.class);
        //      return (ArrayList<?>) gson.fromJson(json, createJavaUtilListParameterizedType(destType.getClass()));
    }


    /*
    static ParameterizedType createJavaUtilListParameterizedType(final Type elementType) {
        return new ParameterizedType() {
            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{ elementType };
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
    }
*/


    public ArrayList<?> getArrayList(String key, Type type) {
        Gson gson = new Gson();
        String json = pref.getString(key, null);

        return gson.fromJson(json, type);

        //      return (ArrayList<?>) gson.fromJson(json, createJavaUtilListParameterizedType(destType.getClass()));
    }




    public Map getAsMap(String key, Type type) {
        Gson gson = new Gson();
        String json = pref.getString(key, null);
        return gson.fromJson(json, type);
        //      return (ArrayList<?>) gson.fromJson(json, createJavaUtilListParameterizedType(destType.getClass()));
    }





    /****
     * Starts : Store User setting in the  preferences for the offline storage and without flikering data received
     * ****/
    public String getUserSettingOffline() {
        return pref.getString("user_setting_offline", "");
    }

    public void setUserSettingOffline(String userSettingOffline) {
        editor.putString("user_setting_offline", userSettingOffline);
        editor.commit();
    }
    /*****
     * End of Store user setting
     * ****/
    //////////......................//////////..................///////////.......................////////////

    /*****
     * Start of edit profile data offline
     * ****/
    public void setIsProfileEditFlag(boolean flag) {
        editor.putBoolean("is_profile_edit_flag", flag);
        editor.commit();
    }


    /*****
     public void setLoggedInId(String id) {
     editor.putString("logged_in_id", id);
     editor.commit();
     }
     *****/


    public void logoutUser() {
        editor.clear();
        editor.commit();

    }

    public void storeVideoTempPath(String postId, String temPath) {
        editor.putString("Video_" + postId, temPath);
        editor.commit();
    }

    public String getTempVideoPath(String postId) {
        return pref.getString("Video_" + postId, "");
    }


    public void setMessagingToken(String myToken) {
        editor.putString("messaging_token", myToken);
        editor.commit();
    }

    public String getMessagingToken() {
        return pref.getString("messaging_token", null);
    }

    public boolean isTrackingEnabled() {
        return pref.getBoolean("tracking_enabled", false);
    }

    public void setTrackingEnabled(boolean enabled) {
        editor.putBoolean("tracking_enabled", enabled);
        editor.commit();
    }

    public boolean wasTrackingExplained() {
        return pref.getBoolean("tracking_explained", false);
    }

    public void setTrackingExplained(boolean status) {
        editor.putBoolean("tracking_explained", status);
        editor.commit();
    }


    public void setPlayServicesAvailable(boolean available) {
        editor.putBoolean("play_services_available", available);
        editor.commit();
    }

    public String getLocationIntensity() {
        return pref.getString("location_intensity_mode", "LOW");
    }
/*
    public void setLocationIntensity(@NotNull LocationService.LocationIntensity intensity) {
        editor.putString("location_intensity_mode", intensity.name());
        editor.commit();
    }
*/
    public HashMap getBleDevicesToConnect()
    {
        var content = pref.getString("ble_devices_to_connect", new Gson().toJson(new HashMap<String,BluetoothDevice>()));
        return new Gson().fromJson(content, HashMap.class);
    }


    public void addBleDeviceToConnectList(@NotNull BluetoothDevice device) {
        var hashMap = getBleDevicesToConnect();
        hashMap.put(device.getAddress(),device.getAddress());
        editor.putString("ble_devices_to_connect",  new Gson().toJson(hashMap));
        editor.commit();
    }

    public void removeFromBleDevicesToConnectList(@NotNull BluetoothDevice device) {
        var hashMap = getBleDevicesToConnect();
        hashMap.remove(device.getAddress());
        editor.putString("ble_devices_to_connect",  new Gson().toJson(hashMap));
        editor.commit();
    }


}
