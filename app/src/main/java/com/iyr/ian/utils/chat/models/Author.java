package com.iyr.ian.utils.chat.models;

import androidx.annotation.Nullable;

import com.stfalcon.chatkit.commons.models.IUser;

import java.io.Serializable;

/*
 * Created by troy379 on 04.04.17.
 */
public class Author implements IUser, Serializable {

    private final String id;
    private final String name;
    private String avatar;
    private final boolean online;

    public Author(String id, String name, String avatar, boolean online) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.online = online;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(@Nullable String avatar) {
        this.avatar = avatar;
    }

    public boolean isOnline() {
        return online;
    }
}
