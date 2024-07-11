package com.iyr.ian.ui.map.models;


import com.google.android.gms.maps.model.LatLng;
import com.iyr.ian.dao.models.EventLocation;
import com.iyr.ian.enums.UserTypesEnum;
import com.iyr.ian.ui.map.enums.MapObjectsType;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class EventMapObject {

    @NotNull
    public Map<String, Object> updates = new HashMap<>();
    public MapObjectsType type;

    public String key;

    public UserTypesEnum userType;
    public Object resourceLocation;
    public LatLng latLng;
    public HashMap<Long, LatLng> path = new HashMap<>();
    public TreeMap<String, EventLocation> previousLocations;

    public EventMapObject(String key, MapObjectsType type, UserTypesEnum userTypesEnum, Object resourceLocation, LatLng latLng) {
        this.key = key;
        this.type = type;
        this.userType = userTypesEnum;
        this.resourceLocation = resourceLocation;
        this.latLng = latLng;
        // this.previousLocations = previousLocations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventMapObject that = (EventMapObject) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

}
