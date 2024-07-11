package com.iyr.ian.ui.dialogs;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.iyr.ian.R;

import org.jetbrains.annotations.NotNull;

public class iTagDialog extends AlertDialog {

    private final iTagDialog mThisDialog;
    private final Activity mActivity;
    private final Context mContext;
    private final View mDialoglayout;
    private View.OnClickListener mButton1Callback;
    private View.OnClickListener mButton2Callback;
    private String mTitle;
    private String mLegend;
    private String mButton1Caption;
    private String mButton2Caption;
    private BluetoothDevice device;


    public iTagDialog(@NonNull Context context, @NonNull Activity activity) {
        super(context);
        this.mActivity = activity;
        this.mContext = context;

        this.mThisDialog = this;

        LayoutInflater inflater = activity.getLayoutInflater();
        mDialoglayout = inflater.inflate(R.layout.itag_popup, null);
        this.setView(mDialoglayout);


        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

        lp.copyFrom(getWindow().getAttributes());
        lp.gravity = Gravity.CENTER;
        getWindow().setAttributes(lp);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setGravity(Gravity.CENTER);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mTitle != null) {
            TextView title = mDialoglayout.findViewById(R.id.title);
            title.setText(mTitle);
        }
        /*
        if (mLegend != null) {
            TextView legend = mDialoglayout.findViewById(R.id.legend);
            legend.setText(mLegend);
        }
*/

// Hide after some seconds
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (isShowing()) {
                    dismiss();
                }
            }
        };
        handler.postDelayed(runnable, 1000);


    }

    @Override
    public void setTitle(int resId) {
        mTitle = mContext.getString(resId);
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setLegend(int resId) {
        mLegend = mContext.getString(resId);
    }

    public void setLegend(String message) {
        mLegend = message;
    }

    public void setButton1Caption(int resId) {
        mButton1Caption = mContext.getString(resId);
    }

    public void setButton1Caption(String text) {
        mButton1Caption = text;
    }

    public void setButton2Caption(String text) {
        mButton2Caption = text;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void show() {
        super.show();

        TextView dialogTitle = mDialoglayout.findViewById(R.id.title);
        TextView deviceName = mDialoglayout.findViewById(R.id.device_name);
        if (device != null) {
            deviceName.setText(device.getName() + " (" + device.getAddress() + ")");
        }
        if (mTitle!=null) {
            dialogTitle.setText(mTitle);
        }
/*
        Button buttonTwo = mDialoglayout.findViewById(R.id.buttonTwo);

        if (mButton2Caption != null)
            buttonTwo.setVisibility(View.VISIBLE);
        else
            buttonTwo.setVisibility(View.GONE);
  */
    }

    public void setButton1Callback(@NotNull View.OnClickListener onClickListener) {
        mButton1Callback = onClickListener;
    }


    @NotNull
    public iTagDialog withTitle(String title) {
        this.mTitle = title;
        return this;
    }

    @NotNull
    public iTagDialog withDevice(BluetoothDevice device) {
        this.device = device;
        return this;
    }
}
