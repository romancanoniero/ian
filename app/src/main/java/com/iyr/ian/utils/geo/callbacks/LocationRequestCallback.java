package com.iyr.ian.utils.geo.callbacks;


import com.iyr.ian.dao.models.EventLocation;

public interface LocationRequestCallback {
    void onBeforeStart();

    void onFinish(EventLocation location);

    void onError(Exception exception);
}
