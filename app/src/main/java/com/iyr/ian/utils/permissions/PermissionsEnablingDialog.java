package com.iyr.ian.utils.permissions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.iyr.ian.Constants;
import com.iyr.ian.R;

import org.jetbrains.annotations.NotNull;


public class PermissionsEnablingDialog extends AlertDialog {
    private int mRequestCode;
    private String[] mPermissionsRequired;
    private int mResMessageId;
    private Activity mActivity;
    private Context mContext;
    private final View mDialoglayout;
    private OnClickListener mOnOpenSettingsButtonCallback;

    public PermissionsEnablingDialog(@NonNull Context context, @NonNull Activity activity) {
        super(context);
        this.mActivity = activity;
        this.mContext = context;


        LayoutInflater inflater = activity.getLayoutInflater();
        mDialoglayout = inflater.inflate(R.layout.permission_enabling_popup, null);
        this.setView(mDialoglayout);

        Button tryAgainButton = mDialoglayout.findViewById(R.id.tryAgainButton);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

        lp.copyFrom(getWindow().getAttributes());
        lp.gravity = Gravity.CENTER;
        getWindow().setAttributes(lp);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setGravity(Gravity.CENTER);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

    }


    public PermissionsEnablingDialog(@NonNull Context context, @NonNull Activity activity, int resMessageId, String[] permissions, int requestCode) {
        this(context, activity);
        //super(context);
        this.mActivity = activity;
        this.mContext = context;
        this.mResMessageId = resMessageId;
        this.mPermissionsRequired = permissions;
        this.mRequestCode = requestCode;

        TextView rationaleExplanation = mDialoglayout.findViewById(R.id.permissionExplanation);
        Button tryAgainButton = mDialoglayout.findViewById(R.id.tryAgainButton);
        Button cancelButton = mDialoglayout.findViewById(R.id.cancelButton);


        rationaleExplanation.setText(mResMessageId);
        tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                /*
                if (mOnOpenSettingsButtonCallback!=null)
                {
                    mOnOpenSettingsButtonCallback.onClick(PermissionsEnablingDialog.this, 0);
                }

                 */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                    intent.setData(uri);
// This will take the user to a page where they have to click twice to drill down to grant the permission
                    activity.startActivityForResult(intent, Constants.LOCATION_REQUEST_CODE);

                } else {

                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                    intent.setData(uri);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(intent);
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.finish();

            }
        });

    }

    public void setOnOpenSettingsButtonCallback(@NotNull OnClickListener callback) {
        mOnOpenSettingsButtonCallback = callback;
    }
}
