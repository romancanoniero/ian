package com.iyr.ian.dao.models;


import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class UserLocation extends GeoLocation {

    public String display_name;
    @NotNull
    public String profile_image_path = "";

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserLocation that = (UserLocation) o;
        return Objects.equals(user_key, that.user_key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user_key);
    }


}
