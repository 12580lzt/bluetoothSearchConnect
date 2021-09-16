package com.wooask.testble;

import android.bluetooth.BluetoothDevice;

public class MyBluetoothDevice {

    private BluetoothDevice bluetoothDevice;
    private int connectStatus;

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public int getConnectStatus() {
        return connectStatus;
    }

    public void setConnectStatus(int connectStatus) {
        this.connectStatus = connectStatus;
    }
}
