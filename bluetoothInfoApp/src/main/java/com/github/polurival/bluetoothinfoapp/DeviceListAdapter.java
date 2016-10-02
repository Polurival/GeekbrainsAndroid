package com.github.polurival.bluetoothinfoapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Polurival
 * on 02.10.2016.
 */
public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceHolder> {

    private Context mContext;
    private List<DeviceEntity> mDeviceEntityList;

    public DeviceListAdapter(Context context, List<DeviceEntity> deviceEntityList) {
        this.mContext = context;
        this.mDeviceEntityList = deviceEntityList;
    }

    @Override
    public DeviceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_device_list, parent, false);
        return new DeviceHolder(view);
    }

    @Override
    public void onBindViewHolder(DeviceHolder holder, int position) {
        holder.tvDeviceAddress.setText(mDeviceEntityList.get(position).getAddress());
        holder.tvDeviceName.setText(mDeviceEntityList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return mDeviceEntityList == null ? 0 : mDeviceEntityList.size();
    }

    public static class DeviceHolder extends RecyclerView.ViewHolder {

        TextView tvDeviceAddress;
        TextView tvDeviceName;

        public DeviceHolder(View itemView) {
            super(itemView);
            tvDeviceAddress = (TextView) itemView.findViewById(R.id.address);
            tvDeviceName = (TextView) itemView.findViewById(R.id.name);
        }
    }
}
