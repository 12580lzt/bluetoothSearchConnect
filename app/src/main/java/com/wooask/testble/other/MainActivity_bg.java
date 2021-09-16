package com.wooask.testble.other;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.wooask.testble.R;
import com.wooask.testble.other.BluetoothDeviceAdapter_bg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import static android.bluetooth.BluetoothAdapter.STATE_OFF;
import static android.bluetooth.BluetoothAdapter.STATE_ON;
import static android.bluetooth.BluetoothDevice.BOND_BONDED;
import static android.bluetooth.BluetoothDevice.BOND_BONDING;
import static android.bluetooth.BluetoothDevice.BOND_NONE;

public class MainActivity_bg extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvScan;
    private TextView tvStopScan;
    private ImageView toggleButton;
    private View llProgress;
    private BluetoothDeviceAdapter_bg bluetoothDeviceAdapter;

    private ArrayList<BluetoothDevice> devices = new ArrayList<>();
    private int REQUEST_ENABLE_BT = 283;
    private BluetoothAdapter bluetoothAdapter;
    private boolean isPairing = false;
    private boolean bluetoothOpen = false;

    BroadcastReceiver foundBluetoothDeviceReceiver = new FoundBluetoothDeviceReceiver();
    BroadcastReceiver bondBluetoothDeviceReceiver = new BondBluetoothDeviceReceiver();


    class FoundBluetoothDeviceReceiver extends BroadcastReceiver {

        @SuppressLint({"RestrictedApi"})
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.bluetooth.device.action.FOUND".equals(action)) {

                BluetoothDevice bluetoothDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                BluetoothClass bluetoothClass = bluetoothDevice.getBluetoothClass();
                if (!(TextUtils.isEmpty(bluetoothDevice.getName()) || devices.contains(bluetoothDevice))) {
                    devices.add(bluetoothDevice);
                    bluetoothDeviceAdapter.setData(devices);
                }
            } else if ("android.bluetooth.adapter.action.DISCOVERY_STARTED".equals(action)) {
                //开始搜索
                llProgress.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(true);
            } else if ("android.bluetooth.adapter.action.DISCOVERY_FINISHED".equals(action)) {
                //停止搜索
                llProgress.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    class BondBluetoothDeviceReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("android.bluetooth.device.action.BOND_STATE_CHANGED")) {
                BluetoothDevice bluetoothDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                updateBondStatus(bluetoothDevice);
                Log.e("=======", "BOND_STATE_CHANGED:" + bluetoothDevice.getName());
                if (bluetoothDevice.getBondState() == 12) {

                }
                if (bluetoothDevice.getBondState() == 11) {
                    //BOND_BONDING
                }
                if (bluetoothDevice.getBondState() == 10) {

                }
            } else if (TextUtils.equals(BluetoothAdapter.ACTION_STATE_CHANGED, action)) {
                int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                switch (blueState) {
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                    case STATE_ON:
                        //蓝牙开启
                        setBluetoothStatusIcon();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        //蓝牙关闭
                        setBluetoothStatusIcon();
                        break;
                }
            }
        }
    }

    private void updateBondStatus(BluetoothDevice bluetoothDevice) {
        if (devices != null && devices.size() > 0) {
            for (int i = 0; i < devices.size(); i++) {
                BluetoothDevice device = devices.get(i);
                if (!TextUtils.isEmpty(bluetoothDevice.getAddress()) && !TextUtils.isEmpty(device.getAddress())) {
                    if (TextUtils.equals(bluetoothDevice.getAddress(), device.getAddress())) {
                        devices.set(i, device);
                        bluetoothDeviceAdapter.setData(devices);
                    }
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bg);
        findViewById(R.id.tvBle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                } catch (Exception e) {
                }

            }
        });
        llProgress = findViewById(R.id.llProgress);
        recyclerView = findViewById(R.id.recyclerView);
        toggleButton = findViewById(R.id.toggleButton);
        tvScan = findViewById(R.id.tvScan);
        tvStopScan = findViewById(R.id.tvStopScan);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.e("======", "onRefresh");//不知道为什么不执行
                devices.clear();
                bluetoothDeviceAdapter.setData(devices);
                bluetoothAdapter.startDiscovery();
            }
        });
        swipeRefreshLayout.setRefreshing(false);

        openRecordingPermission();
        initRecyclerView();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            return;
        }

        setBluetoothStatusIcon();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.bluetooth.device.action.FOUND");
        intentFilter.addAction("android.bluetooth.adapter.action.DISCOVERY_STARTED");
        intentFilter.addAction("android.bluetooth.adapter.action.DISCOVERY_FINISHED");
        registerReceiver(foundBluetoothDeviceReceiver, intentFilter);
        registerBondBluetooth();

        tvScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                devices.clear();
                bluetoothDeviceAdapter.setData(devices);
//                getBondedDevices();
                bluetoothAdapter.startDiscovery();
            }
        });
        tvStopScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothAdapter.cancelDiscovery();
            }
        });

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothAdapter.getState() == STATE_OFF) {
                    bluetoothAdapter.enable();
                } else if (bluetoothAdapter.getState() == STATE_ON) {
                    bluetoothAdapter.disable();
                }
            }
        });
    }

    public void onRefresh() {
        Log.e("======", "onRefresh");
//        swipeRefreshLayout.setRefreshing(false);
        devices.clear();
        bluetoothDeviceAdapter.setData(devices);
        bluetoothAdapter.startDiscovery();
    }


    private void getBondedDevices() {
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if (bondedDevices != null) {
            Iterator<BluetoothDevice> iterator = bondedDevices.iterator();
            while (iterator.hasNext()) {
                BluetoothDevice bluetoothDevice = iterator.next();
                Log.e("=====", "getBondedDevices:" + bluetoothDevice.getName());
                if (devices == null || devices.size() == 0) {
                    devices.add(bluetoothDevice);
                    bluetoothDeviceAdapter.setData(devices);
                } else {
                    for (int i = 0; i < devices.size(); i++) {
                        BluetoothDevice device = devices.get(i);
                        if (!TextUtils.isEmpty(bluetoothDevice.getAddress()) && !TextUtils.isEmpty(device.getAddress())) {
                            if (TextUtils.equals(bluetoothDevice.getAddress(), device.getAddress())) {
                                devices.set(i, device);
                                bluetoothDeviceAdapter.setData(devices);
                            }
                        }
                    }
                }
            }
        }
    }

    private void registerBondBluetooth() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.bluetooth.device.action.BOND_STATE_CHANGED");
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bondBluetoothDeviceReceiver, intentFilter);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(foundBluetoothDeviceReceiver);
        unregisterReceiver(bondBluetoothDeviceReceiver);
    }


    private void initRecyclerView() {
        bluetoothDeviceAdapter = new BluetoothDeviceAdapter_bg();
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(bluetoothDeviceAdapter);
        bluetoothDeviceAdapter.setData(devices);

        bluetoothDeviceAdapter.setOnItemClickListener(new BluetoothDeviceAdapter_bg.ItemOnClickListener() {
            @Override
            public void onOnClick(BluetoothDevice bluetoothDevice, int position) {
                Log.e("======", "onOnClick" + bluetoothDevice.getBondState());
                if (bluetoothDevice.getBondState() == BOND_NONE) {
                    boolean bond = bluetoothDevice.createBond();
                    Log.e("======", "bond" + bond);
                } else if (bluetoothDevice.getBondState() == BOND_BONDED) {
                    removeBond(bluetoothDevice);
                } else if (bluetoothDevice.getBondState() == BOND_BONDING) {

                }
            }
        });
    }

    private void removeBond(BluetoothDevice bluetoothDevice) {
        try {
            bluetoothDevice.getClass().getMethod(("removeBond"), null).invoke(bluetoothDevice, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 请求录音必要权限
     */
    protected void openRecordingPermission() {
        ArrayList<String> temp = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String permissions[] = {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,

            };

            for (String perm : permissions) {
                if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                    //进入到这里代表没有权限.
                    temp.add(perm);
                }
            }
            if (temp.size() > 0) {
                String[] array = new String[temp.size()];
                temp.toArray(array);
                ActivityCompat.requestPermissions(this, array, 123);
            }
        }
    }


    private void setBluetoothStatusIcon() {
        if (bluetoothAdapter.getState() == STATE_ON) {
            toggleButton.setImageResource(R.mipmap.ic_switch_open);
            if (!swipeRefreshLayout.isEnabled()) {
                swipeRefreshLayout.setEnabled(true);
            }
            swipeRefreshLayout.setRefreshing(true);
            onRefresh();
        } else {
            devices.clear();
            bluetoothDeviceAdapter.setData(devices);
            toggleButton.setImageResource(R.mipmap.ic_switch_close);
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setEnabled(false);
        }
    }


}