package com.iyr.ian.utils.bluetooth.adapters;


import com.iyr.ian.R;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.iyr.ian.BuildConfig;
import com.iyr.ian.itag.ITag;
import com.iyr.ian.itag.ITagDefault;
import com.iyr.ian.utils.bluetooth.models.BLEScanResult;
import com.iyr.ian.utils.bluetooth.views.RssiView;
import java.util.ArrayList;
import java.util.List;

public class BLEDeviceAdapter extends RecyclerView.Adapter<ViewHolder> {




    private final Context context;
    private final IBLEDeviceAdapter inteface;
    private final String TAG = "BLEDeviceAdapter";
    public final List<BLEScanResult> scanResults = new ArrayList<>();

    public BLEDeviceAdapter(Context context,  IBLEDeviceAdapter callback ) {
        this.context = context;
        this.inteface = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_le_scan_item, parent, false);
        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onBindViewHolder position=" + position + " thread=" + Thread.currentThread().getName());
        }
        final BLEScanResult scanResult = scanResults.get(position);
        assert scanResult != null;

        View.OnClickListener onClickListener = sender -> {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onRemember  thread=" + Thread.currentThread().getName());
            }
            if (!ITag.store.remembered(scanResult.getId())) {
                ITag.store.remember(new ITagDefault(scanResult));
                ITag.ble.scanner().stop();
            }

        };
        try
        {
            holder.textName.setText(ITag.store.everById(scanResult.getId()).name());
        }
        catch (Exception e)
        {
            holder.textName.setText(scanResult.getName());
        }

        holder.textAddr.setText(scanResult.getId());
   /*
        holder.btnRemember.setTag(scanResult);
        holder.btnRemember.setOnClickListener(onClickListener);
   */
        holder.btnRemember2.setOnClickListener(onClickListener);

        if (position % 2 == 1) {
            holder.itemView.setBackgroundColor(0xffe0e0e0);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
        holder.rssiView.setRssi(scanResult.getRssi());
        /*if (getActivity() != null && isAdded()) {
            // issue #38 Fragment not attached to Activity
            holder.textRSSI.setText(String.format(AppClass.getInstance().getApplicationContext().getString(R.string.rssi), scanResult.getRssi()));
        } else*/

        holder.textRSSI.setText(String.format(context.getString(R.string.rssi), scanResult.getRssi()));
    }

    @Override
    public int getItemCount() {
        return scanResults.size();
    }

    public void onBluetoothOff() {

    }
}

class ViewHolder extends RecyclerView.ViewHolder {
    final TextView textName;
    final TextView textAddr;
    final TextView textRSSI;
    final RssiView rssiView;
    //final ImageView btnRemember;
    final View btnRemember2;

    ViewHolder(@NonNull View rootView) {
        super(rootView);
        textName = rootView.findViewById(R.id.text_name);
        textAddr = rootView.findViewById(R.id.text_addr);
        textRSSI = rootView.findViewById(R.id.text_rssi);
        rssiView = rootView.findViewById(R.id.rssi);
      //  btnRemember = rootView.findViewById(R.id.btn_connect);
        btnRemember2 = rootView.findViewById(R.id.btn_connect_2);
    }
}