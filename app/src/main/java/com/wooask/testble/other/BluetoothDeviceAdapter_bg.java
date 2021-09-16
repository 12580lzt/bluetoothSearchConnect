
package com.wooask.testble.other;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wooask.testble.R;

import java.util.ArrayList;

import static android.bluetooth.BluetoothDevice.BOND_BONDED;
import static android.bluetooth.BluetoothDevice.BOND_BONDING;

public class BluetoothDeviceAdapter_bg extends RecyclerView.Adapter<BluetoothDeviceAdapter_bg.BluetoothDeviceViewHolder> {


    private ArrayList<BluetoothDevice> mData;

    @NonNull
    @Override
    public BluetoothDeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nearby_devices, parent, false);
        BluetoothDeviceViewHolder holder = new BluetoothDeviceViewHolder(view);
        return holder;
    }

    ItemOnClickListener listener;

    public void setOnItemClickListener(ItemOnClickListener listener) {
        this.listener = listener;
    }

    public void setData(ArrayList<BluetoothDevice> data) {
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull BluetoothDeviceViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        BluetoothDevice bluetoothDevice = mData.get(position);
        String name = bluetoothDevice.getName();
        int bondState = bluetoothDevice.getBondState();
        String address = bluetoothDevice.getAddress();
        int type = bluetoothDevice.getType();
        if (type == 1024) {
            holder.ivDeviceType.setText("耳机");
        } else if (type == 1280) {
            holder.ivDeviceType.setText("外围设备");
        } else if (type == 1792) {
            holder.ivDeviceType.setText("手表");
        } else if (type == 2304) {
            holder.ivDeviceType.setText("健康");
        } else if (type == 7936) {
            holder.ivDeviceType.setText("未知");
        }

        Log.e("====", "name:" + name + " address:" + address + " bondState:" + bondState + " type:" + type);

        holder.tvDeviceName.setText(String.valueOf(name));
        holder.tvDeviceAddress.setText(String.valueOf(address));
        holder.tvBluetoothStatus.setText(String.valueOf(bondState));
        if (bondState == BOND_BONDED) {
            holder.tvBluetoothPairUnpair.setText("已经配对");
        } else if (bondState == BOND_BONDING) {
            holder.tvBluetoothPairUnpair.setText("配对中...");
        } else {
            //BOND_NONE初始状态
            holder.tvBluetoothPairUnpair.setText("配对");
        }
        holder.rlBluetoothPairUnpair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (listener != null) {
                    listener.onOnClick(bluetoothDevice, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    public interface ItemOnClickListener {

        void onOnClick(BluetoothDevice bluetoothDevice, int position);
    }

    public class BluetoothDeviceViewHolder extends RecyclerView.ViewHolder {

        public TextView tvDeviceName;
        public TextView tvDeviceAddress;
        public TextView tvBluetoothPairUnpair;
        public TextView tvBluetoothStatus;
        public TextView ivDeviceType;
        public View rlBluetoothPairUnpair;

        public BluetoothDeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
            tvDeviceAddress = itemView.findViewById(R.id.tvDeviceAddress);
            tvBluetoothPairUnpair = itemView.findViewById(R.id.tvBluetoothPairUnpair);
            rlBluetoothPairUnpair = itemView.findViewById(R.id.rlBluetoothPairUnpair);
            ivDeviceType = itemView.findViewById(R.id.ivDeviceType);
            tvBluetoothStatus = itemView.findViewById(R.id.tvBluetoothStatus);
        }
    }

}
