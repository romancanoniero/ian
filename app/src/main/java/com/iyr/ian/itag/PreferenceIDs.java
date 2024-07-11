package com.iyr.ian.itag;

import android.content.Context;

import androidx.annotation.NonNull;

//import com.iyr.fewtouchs.bluetooth.preference.SerializablePreferenceNotNull;

import com.iyr.ian.utils.bluetooth.preference.SerializablePreferenceNotNull;

import java.util.ArrayList;
import java.util.List;



class PreferenceIDs extends SerializablePreferenceNotNull<List<String>> {

    PreferenceIDs(@NonNull Context context) {
        super(context, "ids", new ArrayList<String>());
    }
}
