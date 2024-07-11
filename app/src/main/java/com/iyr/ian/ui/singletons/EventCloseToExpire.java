package com.iyr.ian.ui.singletons;

import android.app.Activity;
import android.os.Bundle;

import com.iyr.ian.ui.base.EventCloseToExpireDialog;
import com.iyr.ian.ui.base.EventCloseToExpireDialogCallback;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;

public class EventCloseToExpire {
    private static EventCloseToExpire myInstance = null;
    private EventCloseToExpireDialog dialog;

    public static EventCloseToExpire getInstance() {
        if (myInstance == null) {
            myInstance = new EventCloseToExpire();
        }
        return myInstance;
    }


    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            if (dialog.getCallback() != null) {
                Objects.requireNonNull(dialog.getCallback()).onDismiss(dialog);
            }
            dialog.dismiss();
        }
    }

    public void show(@NotNull Activity activity, @NotNull String eventKey, Bundle extras, @Nullable EventCloseToExpireDialogCallback callback) {
        if (dialog != null && dialog.isShowing()) {
            return;
        }
        dialog = new EventCloseToExpireDialog(activity);
        dialog.setUserName(extras.get("display_name").toString());
        dialog.setRemainingTime(extras.getLong("remaining_time"));

        dialog.setEventKey(eventKey);
        dialog.setCallback(callback);
        dialog.show();
    }

    public boolean isDialogShowing() {
        if (dialog != null)
            return dialog.isShowing();
        else
            return false;
    }
}
