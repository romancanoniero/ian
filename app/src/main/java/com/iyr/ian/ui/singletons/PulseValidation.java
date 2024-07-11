package com.iyr.ian.ui.singletons;

import android.app.Activity;

import com.iyr.ian.ui.base.PulseRequestTarget;
import com.iyr.ian.ui.base.PulseValidationCallback;
import com.iyr.ian.ui.base.PulseValidatorDialog;

import java.util.Objects;

public class PulseValidation {
    private static PulseValidation myInstance = null;
    private PulseValidatorDialog dialog;

    public PulseValidation() {
    }

    public static PulseValidation getInstance() {
        if (myInstance == null) {
            myInstance = new PulseValidation();
        }
        return myInstance;
    }

    public void show(Activity activity, PulseRequestTarget reason, PulseValidationCallback callback) {
        if (dialog != null && dialog.isShowing()) {
            return;
        }
        dialog = new PulseValidatorDialog(activity);
        dialog.setValidationType(reason);
        dialog.setCallback(callback);
        dialog.show();
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            if (dialog.getCallback() != null) {
                Objects.requireNonNull(dialog.getCallback()).onCancel(dialog);
            }
            dialog.dismiss();
        }
    }

    public boolean isDialogShowing() {
        if (dialog != null)
            return dialog.isShowing();
        else
            return false;
    }

}
