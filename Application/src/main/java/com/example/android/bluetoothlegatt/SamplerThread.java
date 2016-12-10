package com.example.android.bluetoothlegatt;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

/**
 * Created by Adam Cattermole on 10/12/2016.
 */
public class SamplerThread extends Thread {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final int SAMPLE_RATE = 100;

    BluetoothLeService bluetoothLeService;
    BluetoothGattCharacteristic accelerometerCharacteristic;

    private boolean kill = false;

    public SamplerThread(BluetoothLeService bluetoothLeService, BluetoothGattCharacteristic accelerometerCharacteristic) {
        this.bluetoothLeService = bluetoothLeService;
        this.accelerometerCharacteristic = accelerometerCharacteristic;
    }

    @Override
    public void run() {
        super.run();
        while(!kill) {
            bluetoothLeService.readCharacteristic(accelerometerCharacteristic);
            try {
                sleep(SAMPLE_RATE);
            } catch (InterruptedException e) {
                Log.e(TAG, "Sampler failed to sleep");
            }
        }
    }

    public void killSampler() {
        kill = true;
    }


}
