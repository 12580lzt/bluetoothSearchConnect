
package com.wooask.testble.adapter;


import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;


import com.wooask.testble.MyBluetoothDevice;
import com.wooask.testble.R;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static android.bluetooth.BluetoothAdapter.STATE_DISCONNECTED;
import static android.bluetooth.BluetoothDevice.BOND_BONDED;
import static android.bluetooth.BluetoothDevice.BOND_BONDING;
import static android.bluetooth.BluetoothDevice.BOND_NONE;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.BluetoothDeviceViewHolder> {

    private ArrayList<BluetoothDevice> mData;
    private MyBluetoothDevice mBluetoothDevice;
    private List<BluetoothDevice> mAlreadyConnectBluetoothDevice;

    @NonNull
    @Override
    public BluetoothDeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bluetooth_devices, parent, false);
        BluetoothDeviceViewHolder holder = new BluetoothDeviceViewHolder(view);
        return holder;
    }

    ItemOnClickListener listener;

    public void setConnectBluetoothDevice(MyBluetoothDevice bluetoothDevice) {
        this.mBluetoothDevice = bluetoothDevice;
    }

    public void setAlreadyConnectBluetoothDevice(List<BluetoothDevice> alreadyConnectBluetoothDevice) {
        this.mAlreadyConnectBluetoothDevice = alreadyConnectBluetoothDevice;
    }


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
        Log.e("====", "name:" + name + " address:" + address + " bondState:" + bondState + " type:" + type);
        if (TextUtils.isEmpty(name)) {
            holder.tvDeviceName.setText(String.valueOf(address));
        } else {
            holder.tvDeviceName.setText(String.valueOf(name));
        }

        holder.tvBluetoothStatus.setText(String.valueOf(bondState));
        holder.tvDeviceStatus.setVisibility(View.GONE);
        if (bondState == BOND_BONDED) {
            holder.tvDeviceStatus.setVisibility(View.VISIBLE);
            holder.tvDeviceStatus.setText("已经配对");
        } else if (bondState == BOND_BONDING) {
            holder.tvDeviceStatus.setVisibility(View.VISIBLE);
            holder.tvDeviceStatus.setText("正在配对...");
        } else {
            //BOND_NONE初始状态
        }

//        BluetoothClass bluetoothClass = bluetoothDevice.getBluetoothClass();
//
//
//        int d = bluetoothClass.getMajorDeviceClass();
//
//
//        if (d == BluetoothClass.Device.Major.AUDIO_VIDEO) {
//            Log.e("======", "设备名称:" + name + "设备类型:" + d + " 耳机");
//        } else if (d == BluetoothClass.Device.Major.PHONE) {
//            Log.e("======", "设备名称:" + name + "设备类型:" + d + " 手机");
//        } else if (d == BluetoothClass.Device.Major.COMPUTER) {
//            Log.e("======", "设备名称:" + name + "设备类型:" + d + " 电脑");
//        } else {
//            Log.e("======", "设备名称:" + name + "设备类型:" + d + " 未知");
//        }

//        private static final int BITMASK = 0x1F00;
//        public static final int MISC = 0x0000;
//        public static final int COMPUTER = 0x0100;
//        public static final int PHONE = 0x0200;
//        public static final int NETWORKING = 0x0300;
//        public static final int AUDIO_VIDEO = 0x0400;
//        public static final int PERIPHERAL = 0x0500;
//        public static final int IMAGING = 0x0600;
//        public static final int WEARABLE = 0x0700;
//        public static final int TOY = 0x0800;
//        public static final int HEALTH = 0x0900;
//        public static final int UNCATEGORIZED = 0x1F00;


        if (mBluetoothDevice != null && mBluetoothDevice.getBluetoothDevice() != null) {

            if (TextUtils.equals(mBluetoothDevice.getBluetoothDevice().getAddress(), address)) {
                int connectStatus = mBluetoothDevice.getConnectStatus();
                if (connectStatus == BluetoothAdapter.STATE_CONNECTING) {
                    holder.tvDeviceStatus.setText("正在连接...");
                } else if (connectStatus == BluetoothAdapter.STATE_CONNECTED) {
                    holder.tvDeviceStatus.setText("已经连接");
                } else if (connectStatus == BluetoothAdapter.STATE_DISCONNECTED) {

                }
            }
        }

        if (checkConnected(bluetoothDevice)) {
            holder.tvDeviceStatus.setText("已经连接");
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onOnClick(bluetoothDevice, position);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.onOnLongClick(bluetoothDevice, position);
                }
                return false;
            }
        });
    }

    private boolean checkConnected(BluetoothDevice device) {
        boolean isConnected = false;


        if (Build.VERSION.SDK_INT <= 19) {
            return isConnected(device);
        }

        try {
            Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
            isConnectedMethod.setAccessible(true);
            isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isConnected;
    }

    private boolean isConnected(BluetoothDevice device) {
        if (mAlreadyConnectBluetoothDevice != null && mAlreadyConnectBluetoothDevice.size() > 0) {
            for (int i = 0; i < mAlreadyConnectBluetoothDevice.size(); i++) {
                BluetoothDevice bluetoothDevice = mAlreadyConnectBluetoothDevice.get(i);
                if (TextUtils.equals(bluetoothDevice.getAddress(), device.getAddress())) {
                    return true;
                }
            }
        }
        return false;
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

        void onOnLongClick(BluetoothDevice bluetoothDevice, int position);
    }

    public class BluetoothDeviceViewHolder extends RecyclerView.ViewHolder {

        public TextView tvDeviceName;
        public TextView tvDeviceStatus;
        public TextView tvBluetoothStatus;
        public ImageView ivDeviceType;

        public BluetoothDeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
            tvDeviceStatus = itemView.findViewById(R.id.tvDeviceStatus);
            ivDeviceType = itemView.findViewById(R.id.ivDeviceType);
            tvBluetoothStatus = itemView.findViewById(R.id.tvBluetoothStatus);
        }
    }

}
