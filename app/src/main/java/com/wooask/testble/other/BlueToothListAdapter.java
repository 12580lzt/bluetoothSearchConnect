package com.wooask.testble.other;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wooask.testble.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class BlueToothListAdapter extends RecyclerView.Adapter<BlueToothListAdapter.ViewHolder> {
    private String TAG = "BlueToothListAdapter";
    private ArrayList<BluetoothDevice> bluetoothDeviceArrayList;
    private MediaPlayer mediaPlayer = new MediaPlayer();

    public BlueToothListAdapter(ArrayList<BluetoothDevice> bluetoothDeviceArrayList) {
        this.bluetoothDeviceArrayList = bluetoothDeviceArrayList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvRecordName, tvTime;
        private View mView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            tvTime = view.findViewById(R.id.tvTime);
            tvRecordName = view.findViewById(R.id.tvRecordName);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.record_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    private BluetoothAdapter mBluetoothAdapter;

    public void connectBt(BluetoothDevice btDev) {

        final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
        UUID uuid = UUID.fromString(SPP_UUID);
        BluetoothSocket socket = null;
        try {
            socket = btDev.createRfcommSocketToServiceRecord(uuid);

            mBluetoothAdapter = BluetoothAdapter
                    .getDefaultAdapter();

            if (socket != null)
                //全局只有一个bluetooth，所以我们可以将这个socket对象保存在appliaction中
                // AskApplication.bluetoothSocket = mBluetoothSocket;
                //通过反射得到bltSocket对象，与uuid进行连接得到的结果一样，但这里不提倡用反射的方法
                //mBluetoothSocket = (BluetoothSocket) btDev.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(btDev, 1);
                Log.d("blueTooth", "开始连接...");
            //在建立之前调用
            if (mBluetoothAdapter.isDiscovering())
                //停止搜索
                mBluetoothAdapter.cancelDiscovery();
            //如果当前socket处于非连接状态则调用连接
            if (!socket.isConnected()) {
                socket.connect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

//    public static RecordBean mRecordBean;

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothDevice bluetoothDevice = bluetoothDeviceArrayList.get(position);

        if (bluetoothDevice.getName() != null) {
            holder.tvRecordName.setText(bluetoothDevice.getName());
        }else{
            holder.tvRecordName.setText(bluetoothDevice.getAddress());
        }

       // holder.tvTime.setText(recordBean.getLastTime());


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                connectBt(bluetoothDeviceArrayList.get(position));
            }
        });
     }

    @Override
    public int getItemCount() {
        return bluetoothDeviceArrayList.size();
    }

}
