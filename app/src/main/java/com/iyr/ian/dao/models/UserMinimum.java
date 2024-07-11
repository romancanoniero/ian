package com.iyr.ian.dao.models;

import com.google.firebase.database.Exclude;
import com.iyr.ian.utils.support_models.MediaFile;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class UserMinimum {

    @Exclude
    public String user_key;
    public String display_name;

    public String getUser_key() {
        return user_key;
    }

    public void setUser_key(String user_key) {
        this.user_key = user_key;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    @NotNull
    public MediaFile image = null;

    @NotNull
    public MediaFile getImage() {
        return image;
    }

    public void setImage(@NotNull MediaFile image) {
        this.image = image;
    }




    public UserMinimum() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserMinimum that = (UserMinimum) o;
        return Objects.equals(user_key, that.user_key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user_key);
    }
}
