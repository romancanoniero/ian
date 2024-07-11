package com.iyr.ian.utils.chat.media_types;

public class Voice {

    private final String url;
    private final int duration;

    public Voice(String url, int duration) {
        this.url = url;
        this.duration = duration;
    }

    public String getUrl() {
        return url;
    }

    public int getDuration() {
        return duration;
    }
}