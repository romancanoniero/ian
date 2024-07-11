package com.iyr.ian.utils.bluetooth.preference;

import android.content.Context;

import androidx.annotation.NonNull;

class PreferenceTag<T> extends SerializablePreferenceNullable<T> {
    PreferenceTag(@NonNull Context context, String id) {
        super(context, "tag"+id, null);
    }
}

