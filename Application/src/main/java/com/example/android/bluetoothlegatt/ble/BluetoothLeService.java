/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * It should be noted that this file has been modified
 */

package com.example.android.bluetoothlegatt.ble;

import android.app.Service;
import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import com.example.android.bluetoothlegatt.Utility;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public final static String ACCELEROMETER_MEASUREMENT =
            "com.example.bluetooth.le.ACCELEROMETER_MEASUREMENT";
    public final static String ACCELEROMETER_PERIOD =
            "com.example.bluetooth.le.ACCELEROMETER_PERIOD";
    public final static String TEMPERATURE_MEASUREMENT =
            "com.example.bluetooth.le.TEMPERATURE_MEASUREMENT";
    public final static String TEMPERATURE_PERIOD =
            "com.example.bluetooth.le.TEMPERATURE_PERIOD";
    public final static String BUTTON_A_MEASUREMENT =
            "com.example.bluetooth.le.BUTTON_A_MEASUREMENT";
    public final static String BUTTON_B_MEASUREMENT =
            "com.example.bluetooth.le.BUTTON_B_MEASUREMENT";
    public final static String MAGNETOMETER_MEASUREMENT =
            "com.example.bluetooth.le.MAGNETOMETER_MEASUREMENT";
    public final static String MAGNETOMETER_PERIOD =
            "com.example.bluetooth.le.MAGNETOMETER_PERIOD";
    public final static String MAGNETOMETER_BEARING =
            "com.example.bluetooth.le.MAGNETOMETER_BEARING";

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection. Delayed by 2 sec due to caching bug
                // https://devzone.nordicsemi.com/question/22751/nrftoobox-on-android-not-recognizing-changes-in-application-type-running-on-nordic-pcb/
                SystemClock.sleep(2000);
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
//        Log.d(TAG, "data received from: "+characteristic.getUuid());
        if (UUID.fromString(GattAttributes.ACCELEROMETER_MEASUREMENT).equals(characteristic.getUuid())) {
            intent.addCategory(ACCELEROMETER_MEASUREMENT);

            // range is -1024 : +1024
            // Starting with the LED display face up and level (perpendicular to gravity) and edge connector towards your body:
            // A negative X value means tilting left, a positive X value means tilting right
            // A negative Y value means tilting away from you, a positive Y value means tilting towards you
            // A negative Z value means ?
            float[] accel_out = Utility.byteInputToFloat(characteristic.getValue());
            String value = String.format(Locale.UK, "(%.3f,%.3f,%.3f)", accel_out[0], accel_out[1], accel_out[2]);
            Log.d(TAG, "Accelerometer data converted: "+value);
            intent.putExtra(EXTRA_DATA, value);
        } else if (UUID.fromString(GattAttributes.ACCELEROMETER_PERIOD).equals(characteristic.getUuid())) {
            intent.addCategory(ACCELEROMETER_PERIOD);
            short period = Utility.shortFromLittleEndianBytes(characteristic.getValue());
            Log.d(TAG, "Accelerometer period: "+period);
            intent.putExtra(EXTRA_DATA, period);
        } else if (UUID.fromString(GattAttributes.TEMPERATURE_MEASUREMENT).equals(characteristic.getUuid())) {
            intent.addCategory(TEMPERATURE_MEASUREMENT);
            byte[] b = characteristic.getValue();
            String value = Integer.toString(Utility.byteToInteger(b[0]));
            Log.d(TAG, "Temp Measurement: "+value);
            intent.putExtra(EXTRA_DATA, value);
        } else if (UUID.fromString(GattAttributes.TEMPERATURE_PERIOD).equals(characteristic.getUuid())) {
            intent.addCategory(TEMPERATURE_PERIOD);
            short period = Utility.shortFromLittleEndianBytes(characteristic.getValue());
            Log.d(TAG, "Temperature period: "+period);
            intent.putExtra(EXTRA_DATA, period);
        } else if (UUID.fromString(GattAttributes.BUTTON_A_MEASUREMENT).equals(characteristic.getUuid())) {
            intent.addCategory(BUTTON_A_MEASUREMENT);
            byte[] b = characteristic.getValue();
            String value = Integer.toString(Utility.byteToInteger(b[0]));
            Log.d(TAG, "Button A data: "+value);
            intent.putExtra(EXTRA_DATA, value);
        } else if (UUID.fromString(GattAttributes.BUTTON_B_MEASUREMENT).equals(characteristic.getUuid())) {
            intent.addCategory(BUTTON_B_MEASUREMENT);
            byte[] b = characteristic.getValue();
            String value = Integer.toString(Utility.byteToInteger(b[0]));
            Log.d(TAG, "Button B data: "+value);
            intent.putExtra(EXTRA_DATA, value);
        } else if (UUID.fromString(GattAttributes.MAGNETOMETER_MEASUREMENT).equals(characteristic.getUuid())) {
            intent.addCategory(MAGNETOMETER_MEASUREMENT);
            float[] magn_out = Utility.byteInputToFloat(characteristic.getValue());
            String value = String.format(Locale.UK, "(%.3f,%.3f,%.3f)", magn_out[0], magn_out[1], magn_out[2]);
            Log.d(TAG, "Magnetometer data converted: "+value);
            intent.putExtra(EXTRA_DATA, value);
        } else if (UUID.fromString(GattAttributes.MAGNETOMETER_PERIOD).equals(characteristic.getUuid())) {
            intent.addCategory(MAGNETOMETER_PERIOD);
            short period = Utility.shortFromLittleEndianBytes(characteristic.getValue());
            Log.d(TAG, "Magnetometer period: "+period);
            intent.putExtra(EXTRA_DATA, period);
        } else if (UUID.fromString(GattAttributes.MAGNETOMETER_BEARING).equals(characteristic.getUuid())) {
            intent.addCategory(MAGNETOMETER_BEARING);
            byte[] bearing_bytes = new byte[2];
            System.arraycopy(characteristic.getValue(), 0, bearing_bytes, 0, 2);
            short bearing = Utility.shortFromLittleEndianBytes(bearing_bytes);
            String out = String.format(Locale.UK, "%s - %d",Utility.compassBearing(bearing), bearing);
            Log.d(TAG, "Magnetometer bearing: " + out);
            intent.putExtra(EXTRA_DATA, out);
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" +
                        stringBuilder.toString());
                Log.d(TAG, "Received data: "+stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Should write the bytes to a given {@code BluetoothGattCharacteristic}.
     * @param characteristic The characteristic to write to.
     * @param value The value to write to the characteristic
     */
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        characteristic.setValue(value);
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        UUID serviceUUID = characteristic.getService().getUuid();
        // This is specific to the microbit services
        if (serviceUUID.equals(UUID.fromString(GattAttributes.ACCELEROMETER_SERVICE)) ||
                serviceUUID.equals(UUID.fromString(GattAttributes.TEMPERATURE_SERVICE)) ||
                serviceUUID.equals(UUID.fromString(GattAttributes.BUTTON_SERVICE)) ||
                serviceUUID.equals(UUID.fromString(GattAttributes.MAGNETOMETER_SERVICE))) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    public boolean discoverServices() {
        return mBluetoothGatt.discoverServices();
    }
}
