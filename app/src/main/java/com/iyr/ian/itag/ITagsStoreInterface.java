package com.iyr.ian.itag;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iyr.ian.utils.bluetooth.ble.rasat.java.Observable;


public interface ITagsStoreInterface {
    int count();
    boolean isDisconnectAlert();
    @NonNull
    Observable<StoreOp> observable();
    @Nullable
    ITagInterface byId(@NonNull String id);
    ITagInterface byPos(int pos);
    ITagInterface everById(@NonNull String id);
    @NonNull
    String[] forgottenIDs();
    void forget(@NonNull String id);
    void forget(@NonNull ITagInterface tag);
    void remember(@NonNull ITagInterface tag);
    boolean remembered(@NonNull String id);
    void setAlertDelay(@NonNull String id,int delay);
    void setAlert(@NonNull String id,boolean alert);
    void setColor(@NonNull String id,@NonNull TagColor color);
    void setName(@NonNull String id,String name);
}
