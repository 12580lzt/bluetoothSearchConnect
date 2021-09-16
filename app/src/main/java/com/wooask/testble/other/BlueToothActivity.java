//package com.wooask.testble.other;
//
//import android.app.Activity;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.util.Log;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.CompoundButton;
//import android.widget.ToggleButton;
//
//
//import androidx.annotation.Nullable;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
//
//import com.jaeger.library.StatusBarUtil;
//import com.wooask.testble.R;
//
//import java.util.ArrayList;
//
//public class BlueToothActivity extends Activity {
//    private BluetoothAdapter mBluetoothAdapter;
//    private ToggleButton toggleButton;
//    private SwipeRefreshLayout swipeRefreshLayout;
//
//    private ArrayList<BluetoothDevice> bluetoothDeviceArrayList = new ArrayList<BluetoothDevice>();
//    private BlueToothListAdapter blueToothListAdapter;
//    private RecyclerView recyclerView;
//    private ViewGroup rlt_bt_state;
//
//    private static final String TAG = "BlueToothActivity:";
//
//
//
//    private Handler mHandler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what){
//                case 0:
//                    scanBt();
//                    mHandler.sendEmptyMessageDelayed(1,5000);
//                    break;
//                case 1:
//                    mBluetoothAdapter.stopLeScan(callback);
//                    break;
//                case 2:
//                    swipeRefreshLayout.setRefreshing(false);
//                    break;
//            }
//        }
//    };
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.bluetooth_activity);
//        StatusBarUtil.setTranslucent(this, 0);
//        initView();
//        initListener();
//        register();
//    }
//
//    private void register() {
//        // 注册这个 BroadcastReceiver
//        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        registerReceiver(mReceiver,filter);
//    }
//
//    // 创建一个接受 ACTION_FOUND 的 BroadcastReceiver
//    private final BroadcastReceiver mReceiver = new BroadcastReceiver(){
//
//        public void onReceive(Context context,Intent intent){
//            String action = intent.getAction();
//            // 当 Discovery 发现了一个设备
//            if(BluetoothDevice.ACTION_FOUND.equals(action)){
//                // 从 Intent 中获取发现的 BluetoothDevice
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                // 将名字和地址放入要显示的适配器中
//                //mArrayAdapter.add(device.getName + "\n" + device.getAddress());
//                Log.i(TAG,"BluetoothDevice find:"+device.getName() + "\n" + device.getAddress());
//            }
//        }
//    };
//
//    public void initListener(){
//        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                switchBlueTooth(isChecked);
//            }
//        });
//
//        rlt_bt_state.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                toggleButton.setChecked(!mBluetoothAdapter.isEnabled());
//            }
//        });
//
//      swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//               // scanBt();
//                swipeRefreshLayout.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                      //  mBluetoothAdapter.stopLeScan(callback);
//                        swipeRefreshLayout.setRefreshing(false);
//                    }
//                },8000);
//            }
//        });
//    }
//
//    private BluetoothAdapter.LeScanCallback callback = new BluetoothAdapter.LeScanCallback() {
//        @Override
//        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
//            boolean isCan = false;
//            for (int i = 0; i < bluetoothDeviceArrayList.size(); i++) {
//                if (bluetoothDeviceArrayList.get(i).getAddress().equals(device.getAddress())) {
//                    isCan = true;
//                    break;
//                }
//            }
//            if (!isCan) {
//                bluetoothDeviceArrayList.add(device);
//                // recyclerView.setAdapter(blueToothListAdapter);
//                blueToothListAdapter.notifyDataSetChanged();
//            }
//            Log.d(TAG, "device."+device.getAddress()+"\n"+device.getName());
//        }
//    };
//    public void initView(){
//        toggleButton = findViewById(R.id.toggleButton);
//        recyclerView = findViewById(R.id.recycler_list_bt);
//        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
//        rlt_bt_state = findViewById(R.id.rlt_bt_state);
//
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        recyclerView.setLayoutManager(layoutManager);
//
//        blueToothListAdapter = new
//                BlueToothListAdapter(bluetoothDeviceArrayList);
//
//        recyclerView.setAdapter(blueToothListAdapter);
//
//        mBluetoothAdapter = BluetoothAdapter
//                .getDefaultAdapter();
//
//        toggleButton.setChecked(mBluetoothAdapter.isEnabled());
//        if (toggleButton.isChecked()) {
//            mHandler.sendEmptyMessageDelayed(0,1000);
//        }
//    }
//
//
//    private void switchBlueTooth(boolean isChecked) {
//        if (isChecked) {
//            // 询问打开蓝牙
//            if (!mBluetoothAdapter.isEnabled()) {
//                mBluetoothAdapter.enable();
//                scanBt();
//              //  mHandler.sendEmptyMessageDelayed(0,1000);
//            }
//        }else{
//            if (mBluetoothAdapter.isEnabled()) {
//                mBluetoothAdapter.disable();
//            }
//        }
//    }
//
//    public void back(View view) {
//        finish();
//    }
//
//    public void scanBt(){
//        mBluetoothAdapter.startLeScan(callback);
//        Log.i(TAG,"startDiscovery");
//    }
//}
