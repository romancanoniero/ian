package com.iyr.ian.itag;

import android.content.Context;

import androidx.annotation.NonNull;


import com.iyr.ian.utils.bluetooth.preference.SerializablePreferenceNotNull;

import java.util.HashSet;
import java.util.Set;



class PreferenceIDsForever extends SerializablePreferenceNotNull<Set<String>> {

    PreferenceIDsForever(@NonNull Context context) {
        super(context, "foreverids", new HashSet<String>());
    }
}
