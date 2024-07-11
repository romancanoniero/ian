package com.iyr.ian.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.annotation.IntRange;

public class NetworkUtils {

    public static final int CONNECTION_TYPE_NONE = 0 ;
    public static final int CONNECTION_TYPE_MOBILE_DATA = 1 ;
    public static final int CONNECTION_TYPE_WIFI = 2 ;
    public static final int CONNECTION_TYPE_VPN = 3 ;

    @IntRange(from = 0, to = 3)
    public static int getConnectionType(Context context) {
        int result = 0; // Returns connection type. 0: none; 1: mobile data; 2: wifi
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (cm != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        result = CONNECTION_TYPE_WIFI;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        result = CONNECTION_TYPE_MOBILE_DATA;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                        result = CONNECTION_TYPE_VPN;
                    }
                }
            }
        } else {
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null) {
                    // connected to the internet
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        result = CONNECTION_TYPE_WIFI;
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        result = CONNECTION_TYPE_MOBILE_DATA;
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_VPN) {
                        result = CONNECTION_TYPE_VPN;
                    }
                }
            }
        }
        return result;
    }



}
