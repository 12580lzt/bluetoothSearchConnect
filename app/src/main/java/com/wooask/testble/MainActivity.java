package com.wooask.testble;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
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

import com.jaeger.library.StatusBarUtil;
import com.wooask.testble.adapter.BluetoothDeviceAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static android.bluetooth.BluetoothAdapter.STATE_OFF;
import static android.bluetooth.BluetoothAdapter.STATE_ON;
import static android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED;
import static android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED;
import static android.bluetooth.BluetoothDevice.BOND_BONDED;
import static android.bluetooth.BluetoothDevice.BOND_BONDING;
import static android.bluetooth.BluetoothDevice.BOND_NONE;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "MainActivity::";
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvScan;
    private TextView tvStopScan;
    private ImageView toggleButton;
    private View llProgress;
    private BluetoothDeviceAdapter bluetoothDeviceAdapter;

    private ArrayList<BluetoothDevice> devices = new ArrayList<>();
    private int REQUEST_ENABLE_BT = 283;
    private BluetoothAdapter bluetoothAdapter;
    private boolean isPairing = false;
    private boolean bluetoothOpen = false;

    BroadcastReceiver foundBluetoothDeviceReceiver = new FoundBluetoothDeviceReceiver();
    BroadcastReceiver bondBluetoothDeviceReceiver = new BondBluetoothDeviceReceiver();

    public void back(View view) {
        finish();
    }


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
                    Log.i(TAG, bluetoothDevice.getName());
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
            Log.e("BondBluetoothDevice", "action:" + action);
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
                        if (a2dpConnectedDevices != null) {
                            a2dpConnectedDevices.clear();
                        }
                        setBluetoothStatusIcon();
                        break;
                }
            } else if (TextUtils.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED, action)) {
                if (intent.hasExtra(BluetoothDevice.EXTRA_DEVICE)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    boolean exist = checkDeviceExist(device);
                    if (!exist) {
                        devices.add(0, device);
                        bluetoothDeviceAdapter.setData(devices);
                    }
                    int state = 0;
                    try {
                        if (intent.hasExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE)) {
                            state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, 0);
                        }
                    } catch (Exception e) {
                        state = 0;
                    }

                    MyBluetoothDevice myBluetoothDevice = new MyBluetoothDevice();
                    myBluetoothDevice.setBluetoothDevice(device);
                    myBluetoothDevice.setConnectStatus(state);
                    bluetoothDeviceAdapter.setConnectBluetoothDevice(myBluetoothDevice);
                    bluetoothDeviceAdapter.notifyDataSetChanged();
                }
            } else if (TextUtils.equals(ACTION_ACL_CONNECTED, action)) {
                Log.e("=====", "ACTION_ACL_CONNECTED " + intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
                updateAlreadyConnectDevice(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE), true);
                bluetoothDeviceAdapter.setAlreadyConnectBluetoothDevice(a2dpConnectedDevices);
                bluetoothDeviceAdapter.notifyDataSetChanged();
            } else if (TextUtils.equals(ACTION_ACL_DISCONNECTED, action)) {
                Log.e("=====", "ACTION_ACL_DISCONNECTED " + intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
                updateAlreadyConnectDevice(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE), false);
                bluetoothDeviceAdapter.setAlreadyConnectBluetoothDevice(a2dpConnectedDevices);
                bluetoothDeviceAdapter.notifyDataSetChanged();
            }
        }
    }

    private void updateAlreadyConnectDevice(BluetoothDevice device, boolean isConnect) {

        if (device != null && a2dpConnectedDevices != null) {
            ArrayList<BluetoothDevice> copyList = new ArrayList<>();
            copyList.addAll(a2dpConnectedDevices);

            if (a2dpConnectedDevices.size() == 0 && isConnect) {
                a2dpConnectedDevices.add(device);
            }

            for (int i = 0; i < copyList.size(); i++) {
                BluetoothDevice bluetoothDevice = copyList.get(i);
                if (TextUtils.equals(bluetoothDevice.getAddress(), device.getAddress())) {
                    if (isConnect) {
                        if (!checkExist(device)) {
                            a2dpConnectedDevices.add(device);
                        }
                    } else {
                        if (checkExist(device)) {
                            remove(device, copyList);
                        }
                    }
                }
            }

        }
    }

    private boolean checkExist(BluetoothDevice device) {
        if (a2dpConnectedDevices != null && a2dpConnectedDevices.size() > 0) {
            for (BluetoothDevice a2dpConnectedDevice : a2dpConnectedDevices) {
                if (TextUtils.equals(a2dpConnectedDevice.getAddress(), device.getAddress())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void remove(BluetoothDevice device, List<BluetoothDevice> list) {
        if (a2dpConnectedDevices != null && a2dpConnectedDevices.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                BluetoothDevice bluetoothDevice = list.get(i);
                if (TextUtils.equals(bluetoothDevice.getAddress(), device.getAddress())) {
                    a2dpConnectedDevices.remove(i);
                }
            }
        }
    }

    private boolean checkDeviceExist(BluetoothDevice device) {

        if (devices != null) {
            for (int i = 0; i < devices.size(); i++) {
                BluetoothDevice bluetoothDevice = devices.get(i);
                if (TextUtils.equals(device.getAddress(), bluetoothDevice.getAddress())) {
                    return true;
                }
            }
        }

        return false;
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

    private void getBondedDevices() {
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if (bondedDevices != null) {
            Iterator<BluetoothDevice> iterator = bondedDevices.iterator();
            while (iterator.hasNext()) {
                BluetoothDevice bluetoothDevice = iterator.next();
                Log.e("=====", "getBondedDevices:" + bluetoothDevice.getName());
                if (devices != null) {
                    devices.add(bluetoothDevice);
                    bluetoothDeviceAdapter.setData(devices);
                }
            }
        }
    }


    /**
     * 获取已经连接的调备
     *
     * @return
     */
    public ArrayList<BluetoothDevice> getConnectedDevice() {
        ArrayList<BluetoothDevice> connectDevices = new ArrayList<>();
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        Class<BluetoothAdapter> bluetoothAdapterClass = BluetoothAdapter.class;//得到BluetoothAdapter的Class对象
        try {//得到连接状态的方法
            Method method = bluetoothAdapterClass.getDeclaredMethod("getConnectionState", (Class[]) null);
            //打开权限
            method.setAccessible(true);
            int state = (int) method.invoke(adapter, (Object[]) null);

            if (state == BluetoothAdapter.STATE_CONNECTED) {
                Log.i("BLUETOOTH", "BluetoothAdapter.STATE_CONNECTED");
                Set<BluetoothDevice> devices = adapter.getBondedDevices();
                Log.i("BLUETOOTH", "devices:" + devices.size());

                for (BluetoothDevice device : devices) {
                    Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
                    method.setAccessible(true);
                    boolean isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);
                    if (isConnected) {
                        connectDevices.add(device);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connectDevices;
    }

    private boolean checkBluetoothConnect(BluetoothDevice device) {

        if (Build.VERSION.SDK_INT <= 19) {
            return isConnected(device);
        }

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        Class<BluetoothAdapter> bluetoothAdapterClass = BluetoothAdapter.class;//得到BluetoothAdapter的Class对象
        try {//得到连接状态的方法
            Method method = bluetoothAdapterClass.getDeclaredMethod("getConnectionState", (Class[]) null);
            //打开权限
            method.setAccessible(true);
            int state = (int) method.invoke(adapter, (Object[]) null);

            if (state == BluetoothAdapter.STATE_CONNECTED) {
                Log.i("BLUETOOTH", "BluetoothAdapter.STATE_CONNECTED");
                Set<BluetoothDevice> devices = adapter.getBondedDevices();
                Log.i("BLUETOOTH", "devices:" + devices.size());

                Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
                method.setAccessible(true);
                boolean isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);
                return isConnected;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean isConnected(BluetoothDevice device) {
        if (a2dpConnectedDevices != null && a2dpConnectedDevices.size() > 0) {
            for (int i = 0; i < a2dpConnectedDevices.size(); i++) {
                BluetoothDevice bluetoothDevice = a2dpConnectedDevices.get(i);
                if (TextUtils.equals(bluetoothDevice.getAddress(), device.getAddress())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StatusBarUtil.setTranslucent(this, 0);
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

        swipeRefreshLayout.setOnRefreshListener(this);
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
            bluetoothAdapter.enable();

            // Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        getConnectDevice();

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


    @Override
    public void onRefresh() {
        Log.e("======", "onRefresh");//不知道为什么不执行
        devices.clear();
        bluetoothDeviceAdapter.setData(devices);
        getBondedDevices();
        bluetoothAdapter.startDiscovery();
    }


    private void registerBondBluetooth() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.bluetooth.device.action.BOND_STATE_CHANGED");
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(ACTION_ACL_CONNECTED);
        intentFilter.addAction(ACTION_ACL_DISCONNECTED);
        registerReceiver(bondBluetoothDeviceReceiver, intentFilter);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(foundBluetoothDeviceReceiver);
        unregisterReceiver(bondBluetoothDeviceReceiver);
        bluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP, mBluetoothProfile);
    }


    private void initRecyclerView() {
        bluetoothDeviceAdapter = new BluetoothDeviceAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(bluetoothDeviceAdapter);
        bluetoothDeviceAdapter.setData(devices);

        bluetoothDeviceAdapter.setOnItemClickListener(new BluetoothDeviceAdapter.ItemOnClickListener() {
            @Override
            public void onOnClick(BluetoothDevice bluetoothDevice, int position) {
                Log.e("======", "onOnClick" + bluetoothDevice.getBondState());

                if (bluetoothDevice.getBondState() == BOND_NONE) {
                    bluetoothAdapter.cancelDiscovery();
                    boolean bond = bluetoothDevice.createBond();
                    Log.e("======", "bond" + bond);
                } else if (bluetoothDevice.getBondState() == BOND_BONDED) {
                    boolean isConnect = checkBluetoothConnect(bluetoothDevice);
                    if (!isConnect) {
                        bluetoothAdapter.cancelDiscovery();
                        connectBluetoothDevice(bluetoothDevice);
                    }
                } else if (bluetoothDevice.getBondState() == BOND_BONDING) {

                }
            }

            @Override
            public void onOnLongClick(BluetoothDevice bluetoothDevice, int position) {

                if (bluetoothDevice.getBondState() == BOND_NONE) {
                } else if (bluetoothDevice.getBondState() == BOND_BONDED) {
                    bluetoothAdapter.cancelDiscovery();
                    cancelSaveBluetooth(bluetoothDevice);
                } else if (bluetoothDevice.getBondState() == BOND_BONDING) {

                }
            }
        });
    }


    private void connectBluetoothDevice(BluetoothDevice device) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MyBluetoothDevice myBluetoothDevice = new MyBluetoothDevice();
                            myBluetoothDevice.setBluetoothDevice(device);
                            myBluetoothDevice.setConnectStatus(BluetoothAdapter.STATE_CONNECTING);
                            bluetoothDeviceAdapter.setConnectBluetoothDevice(myBluetoothDevice);
                            bluetoothDeviceAdapter.notifyDataSetChanged();
                        }
                    });
                    connectDevice(device);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            boolean b = checkBluetoothConnect(device);
                            MyBluetoothDevice myBluetoothDevice = new MyBluetoothDevice();
                            myBluetoothDevice.setBluetoothDevice(device);
                            myBluetoothDevice.setConnectStatus(b ? BluetoothAdapter.STATE_CONNECTED : BluetoothAdapter.STATE_DISCONNECTED);
                            bluetoothDeviceAdapter.setConnectBluetoothDevice(myBluetoothDevice);
                            bluetoothDeviceAdapter.notifyDataSetChanged();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MyBluetoothDevice myBluetoothDevice = new MyBluetoothDevice();
                            myBluetoothDevice.setBluetoothDevice(device);
                            myBluetoothDevice.setConnectStatus(BluetoothAdapter.STATE_DISCONNECTED);
                            bluetoothDeviceAdapter.setConnectBluetoothDevice(myBluetoothDevice);
                            bluetoothDeviceAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        }).start();

    }

    public boolean connectDevice(BluetoothDevice device) throws Exception {
        boolean connect = false;
        if (mBluetoothProfile != null && mBluetoothProfile instanceof BluetoothA2dp) {
            BluetoothA2dp bluetoothA2dp = (BluetoothA2dp) mBluetoothProfile;
            Method isConnectedMethod = BluetoothA2dp.class.getDeclaredMethod("connect", BluetoothDevice.class);
            //打开权限
            isConnectedMethod.setAccessible(true);
            connect = (boolean) isConnectedMethod.invoke(bluetoothA2dp, device);
        }
        Log.e("=====", "connectDevice:" + connect);
        return connect;
    }


    private void cancelSaveBluetooth(BluetoothDevice bluetoothDevice) {
        DialogSure deleteDialog = new DialogSure(this);
        deleteDialog.getCancelSaveView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeBond(bluetoothDevice);
                deleteDialog.cancel();
            }
        });
        deleteDialog.show();
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
            toggleButton.setImageResource(R.drawable.switch_on);
            if (!swipeRefreshLayout.isEnabled()) {
                swipeRefreshLayout.setEnabled(true);
            }
            swipeRefreshLayout.setRefreshing(true);
            onRefresh();
        } else {
            devices.clear();
            bluetoothDeviceAdapter.setData(devices);
            toggleButton.setImageResource(R.drawable.switch_off);
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setEnabled(false);
        }
    }

    List<BluetoothDevice> a2dpConnectedDevices;
    BluetoothA2dp mBluetoothProfile;
    BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.A2DP) {
                mBluetoothProfile = (BluetoothA2dp) proxy;
                a2dpConnectedDevices = mBluetoothProfile.getConnectedDevices();
                if (a2dpConnectedDevices != null && a2dpConnectedDevices.size() > 0) {
                    bluetoothDeviceAdapter.setAlreadyConnectBluetoothDevice(a2dpConnectedDevices);
                }
            }

        }

        public void onServiceDisconnected(int profile) {

        }
    };

    private void getConnectDevice() {
        bluetoothAdapter.getProfileProxy(this, mProfileListener, BluetoothProfile.A2DP);
    }


}