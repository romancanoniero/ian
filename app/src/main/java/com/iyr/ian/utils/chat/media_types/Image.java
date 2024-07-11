package com.iyr.ian.utils.chat.media_types;


import androidx.annotation.Nullable;

import java.io.Serializable;


public class Image implements Serializable {


    public String url = "";
    String id = null;

    public Image() {

    }

    @Nullable
    public String getImageUrl() {
        return url;
    }
}