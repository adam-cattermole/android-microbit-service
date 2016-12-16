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

package com.example.android.bluetoothlegatt.ui;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.example.android.bluetoothlegatt.R;
import com.example.android.bluetoothlegatt.Utility;
import com.example.android.bluetoothlegatt.ble.BluetoothLeService;
import com.example.android.bluetoothlegatt.ble.GattAttributes;
import com.example.android.bluetoothlegatt.mqtt.MqttConfig;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;

import java.util.*;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final short SAMPLE_RATE = 80;

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";


    //MqttVariable
    private MqttAndroidClient mMqttAndroidClient;
    private TextView mMqttConnectState;

    //BLE Variables
    private TextView mBleConnectState;
    private TextView mAccellData;
    private TextView mAccellPeriod;
    private TextView mTempData;
    private TextView mTempPeriod;
    private TextView mButtonAData;
    private TextView mButtonBData;
    private String mDeviceName;
    private String mDeviceAddress;
    private LinearLayout mGattServicesList;
    private ListAdapter mGattServicesListAdapter;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mBleConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mBleConnected = true;
                updateBleConnectState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mBleConnected = false;
                updateBleConnectState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                List<BluetoothGattService> services = mBluetoothLeService.getSupportedGattServices();
                displayGattServices(services);

                // find accelerometer characteristic
                boolean accelActive = false;
                boolean tempActive = false;
                boolean btnActive = false;

                HashMap<String, BluetoothGattCharacteristic> characteristics = new HashMap<>();
                // Finding all characteristics of services which are defined with a name in the GattAttributes class
                // (characteristics which are known to us) and stored in an easy to access way
                for (BluetoothGattService s: services) {
                    if (s.getUuid().equals(UUID.fromString(GattAttributes.ACCELEROMETER_SERVICE))) {
                        accelActive = true;
                    } else if (s.getUuid().equals(UUID.fromString(GattAttributes.TEMPERATURE_SERVICE))) {
                        tempActive = true;
                    } else if (s.getUuid().equals(UUID.fromString(GattAttributes.BUTTON_SERVICE))) {
                        btnActive = true;
                    }
                    for (BluetoothGattCharacteristic c: s.getCharacteristics()) {
                        String name = GattAttributes.lookup(c.getUuid().toString(), null);
                        if (name != null) {
                            characteristics.put(c.getUuid().toString(), c);
                        }
                    }
                }

                if (accelActive) {
                    // Set the accelerometer period to our defined value and read to check
                    final BluetoothGattCharacteristic achar = characteristics.get(GattAttributes.ACCELEROMETER_MEASUREMENT);
                    final BluetoothGattCharacteristic pchar = characteristics.get(GattAttributes.ACCELEROMETER_PERIOD);
                    Log.d(TAG, "Accelerometer period set to "+SAMPLE_RATE+" ms");
                    mBluetoothLeService.writeCharacteristic(pchar, Utility.leBytesFromShort(SAMPLE_RATE));
                    Handler handler = new Handler();
                    // Delay the read so sufficient time has occurred to write
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Read the period in and display it to the user (may not be necessary)
                            mBluetoothLeService.readCharacteristic(pchar);
                        }
                    }, 1000);
                    // Delay the enabling of notifications so the device has finished writing and reading the period
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Enabling notifications");
                            // Enable notifications triggering data events
                            mBluetoothLeService.setCharacteristicNotification(achar, true);

                        }
                    }, 3000);
                } else {
                    Log.d(TAG, "Accelerometer service not found");
                }
                if (tempActive) {
                    //TODO: set notification period and enable notifications
                }
                if (btnActive) {
                    //TODO: set notifications going for both buttons
                }
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                if (intent.hasCategory(BluetoothLeService.ACCELEROMETER_MEASUREMENT)) {
                    float[] data = intent.getFloatArrayExtra(BluetoothLeService.EXTRA_DATA);
                    String strData = String.format(Locale.UK, "(%.3f,%.3f,%.3f)", data[0], data[1], data[2]);
                    displayData(mAccellData, strData);
                    publishMqttMessage(MqttConfig.TOPIC_ACCELEROMETER, strData);
                } else if (intent.hasCategory(BluetoothLeService.ACCELEROMETER_PERIOD)) {
                    displayPeriod(mAccellPeriod, intent.getShortExtra(BluetoothLeService.EXTRA_DATA, (short) 0));
                } else if (intent.hasCategory(BluetoothLeService.TEMPERATURE_MEASUREMENT)) {
                    //TODO: do something with the temperature data
                } else if (intent.hasCategory(BluetoothLeService.TEMPERATURE_PERIOD)) {
                    //TODO: do something with the temp period
                    displayPeriod(mTempPeriod, intent.getShortExtra(BluetoothLeService.EXTRA_DATA, (short) 0));
                } else if (intent.hasCategory(BluetoothLeService.BUTTON_A_MEASUREMENT)) {
                    //TODO: publish button data
                } else if (intent.hasCategory(BluetoothLeService.BUTTON_B_MEASUREMENT)) {
                    //TODO: publish button data
                } else {
                    // Our cases are specific - should know the data coming back is one of these categories, as that is
                    // all we have requested
                    Log.d(TAG, "Unknown data: "+intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                }
            }
        }
    };

    private final MqttCallbackExtended mMqttCallbackExtended = new MqttCallbackExtended() {
        @Override
        public void connectComplete(boolean reconnect, String serverURI) {

            if (reconnect) {
                Log.d(TAG, "MQTT reconnected: " + serverURI);
                updateMqttConnectState(R.string.connected);
            } else {
                Log.d(TAG, "MQTT connected: " + serverURI);
                updateMqttConnectState(R.string.connected);
            }
        }

        @Override
        public void connectionLost(Throwable cause) {
            Log.d(TAG, "MQTT disconnected: " + MqttConfig.SERVER_URI);
            updateMqttConnectState(R.string.disconnected);
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            Log.d(TAG,"MQTT message: " + new String(message.getPayload()));
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }
    };

    private void clearUI() {
        mGattServicesList.removeAllViews();
        mAccellData.setText(R.string.no_data);
        mAccellPeriod.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = (LinearLayout) findViewById(R.id.gatt_services_list);
        mBleConnectState = (TextView) findViewById(R.id.ble_connect_state);

        mAccellData = (TextView) findViewById(R.id.accel_data_value);
        mAccellPeriod = (TextView) findViewById(R.id.accel_period_value);
        mTempData = (TextView) findViewById(R.id.temp_data_value);
        mTempPeriod = (TextView) findViewById(R.id.temp_period_value);
        mButtonAData = (TextView) findViewById(R.id.btna_data_value);
        mButtonBData = (TextView) findViewById(R.id.btnb_data_value);

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        // MQTT Setup
        ((TextView) findViewById(R.id.server_address)).setText(MqttConfig.SERVER_URI);
        mMqttConnectState = (TextView) findViewById(R.id.mqtt_connect_state);
        mMqttAndroidClient = new MqttAndroidClient(getApplicationContext(), MqttConfig.SERVER_URI, MqttConfig.CLIENT_ID);
        mMqttAndroidClient.setCallback(mMqttCallbackExtended);
    }


    public void mqttConnect() {
        if (!mMqttAndroidClient.isConnected()) {
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setAutomaticReconnect(true);
            mqttConnectOptions.setCleanSession(false);
            try {
                mMqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                        disconnectedBufferOptions.setBufferEnabled(true);
                        disconnectedBufferOptions.setBufferSize(100);
                        disconnectedBufferOptions.setPersistBuffer(false);
                        disconnectedBufferOptions.setDeleteOldestMessages(false);
                        mMqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.e(TAG, "MQTT connect failed: " + MqttConfig.SERVER_URI);
                    }
                });
            } catch (MqttException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void mqttDisconnect() {
        if (mMqttAndroidClient.isConnected()) {
            try {
                mMqttAndroidClient.disconnect();
            } catch (MqttException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
        mqttConnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mBleConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                mqttConnect();
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                mqttDisconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateBleConnectState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBleConnectState.setText(resourceId);
            }
        });
    }

    private void updateMqttConnectState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMqttConnectState.setText(resourceId);
            }
        });
    }

    private void displayData(TextView textView, String data) {
        if (data != null) {
            textView.setText(data);
        }
    }

    private void displayPeriod(TextView textView, short period) {
        if (period > 0) {
            textView.setText(String.format(Locale.UK,"%d ms", period));
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, GattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, GattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        mGattServicesListAdapter = new SimpleAdapter (
                this,
                gattServiceData,
                android.R.layout.simple_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        for (int i = 0; i < mGattServicesListAdapter.getCount(); i++) {
            View item = mGattServicesListAdapter.getView(i, null, null);
            mGattServicesList.addView(item);
        }
//        mGattServicesList.setAdapter(gattServiceAdapter);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        // Additional categories
        intentFilter.addCategory(BluetoothLeService.ACCELEROMETER_MEASUREMENT);
        intentFilter.addCategory(BluetoothLeService.ACCELEROMETER_PERIOD);
        return intentFilter;
    }

    public void publishMqttMessage(String topic, String message){
        try {
            MqttMessage m = new MqttMessage();
            m.setPayload(message.getBytes());
            mMqttAndroidClient.publish(topic, m);

//            For some reason the buffer is set to null - could be worth figuring this out
//            if (!mMqttAndroidClient.isConnected()) {
//                Log.d(TAG, mMqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
//            }
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
