package com.iyr.ian.ui.base;



import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;


import com.iyr.ian.R;
import com.iyr.ian.utils.UIUtils;

import org.jetbrains.annotations.NotNull;

public class SingleButtonDialog extends AlertDialog {

    private final SingleButtonDialog mThisDialog;
    private final Activity mActivity;
    private final Context mContext;

    private View.OnClickListener mButton1Callback;
    private View.OnClickListener mButton2Callback;
    private final View mDialoglayout;
    private String mTitle;
    private String mLegend;
    private String mButton1Caption;
    private String mButton2Caption;


    public SingleButtonDialog(@NonNull Context context, @NonNull Activity activity) {
        super(context);
        this.mActivity = activity;
        this.mContext = context;

        this.mThisDialog = this;

        LayoutInflater inflater = activity.getLayoutInflater();
        mDialoglayout = inflater.inflate(R.layout.single_button_popup, null);
        this.setView(mDialoglayout);

        Button buttonOne = mDialoglayout.findViewById(R.id.buttonOne);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

        lp.copyFrom(getWindow().getAttributes());
        lp.gravity = Gravity.CENTER;
        getWindow().setAttributes(lp);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setGravity(Gravity.CENTER);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        buttonOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UIUtils.INSTANCE.handleTouch(mActivity);

                if (mButton1Callback != null) {
                    mButton1Callback.onClick(view);
                }
                dismiss();
            }
        });


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
        if (mLegend != null) {
            TextView legend = mDialoglayout.findViewById(R.id.legend);
            legend.setText(mLegend);
        }

        if (mButton1Caption != null) {
            Button buttonOne = mDialoglayout.findViewById(R.id.buttonOne);
            buttonOne.setText(mButton1Caption);
        }

        if (mButton1Callback != null) {
            Button buttonOne = mDialoglayout.findViewById(R.id.buttonOne);
            buttonOne.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mButton1Callback.onClick(mDialoglayout);
                    dismiss();
                }
            });

        }


        if (mButton2Caption != null) {
            Button buttonTwo = mDialoglayout.findViewById(R.id.buttonTwo);
            buttonTwo.setText(mButton2Caption);
        }

        if (mButton2Callback != null) {
            Button buttonTwo = mDialoglayout.findViewById(R.id.buttonTwo);
            buttonTwo.setOnClickListener(mButton2Callback);
        } else {
            Button buttonTwo = mDialoglayout.findViewById(R.id.buttonTwo);
            buttonTwo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    (mThisDialog).dismiss();
                }
            });

        }

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

    @Override
    public void show() {
        super.show();

        Button buttonTwo = mDialoglayout.findViewById(R.id.buttonTwo);

        if (mButton2Caption != null)
            buttonTwo.setVisibility(View.VISIBLE);
        else
            buttonTwo.setVisibility(View.GONE);
    }

    public void setButton1Callback(@NotNull View.OnClickListener onClickListener) {
        mButton1Callback = onClickListener;
    }


}
