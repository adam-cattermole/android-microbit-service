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

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class GattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String ACCELEROMETER_SERVICE = "e95d0753-251d-470a-a062-fa1922dfa9a8";
    public static String ACCELEROMETER_MEASUREMENT = "e95dca4b-251d-470a-a062-fa1922dfa9a8";
    public static String ACCELEROMETER_PERIOD = "e95dfb24-251d-470a-a062-fa1922dfa9a8";

    public static String TEMPERATURE_SERVICE = "e95d6100-251d-470a-a062-fa1922dfa9a8";
    public static String TEMPERATURE_MEASUREMENT = "e95d9250-251d-470a-a062-fa1922dfa9a8";
    public static String TEMPERATURE_PERIOD = "e95d1b25-251d-470a-a062-fa1922dfa9a8";

    public static String BUTTON_SERVICE = "e95d9882-251d-470a-a062-fa1922dfa9a8";
    public static String BUTTON_A_MEASUREMENT = "e95dda90-251d-470a-a062-fa1922dfa9a8";
    public static String BUTTON_B_MEASUREMENT ="e95dda91-251d-470a-a062-fa1922dfa9a8";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public static String MAGNETOMETER_SERVICE = "e95df2d8-251d-470a-a062-fa1922dfa9a8";
    public static String MAGNETOMETER_MEASUREMENT = "e95dfb11-251d-470a-a062-fa1922dfa9a8";
    public static String MAGNETOMETER_PERIOD = "e95d386c-251d-470a-a062-fa1922dfa9a8";
    public static String MAGNETOMETER_BEARING = "e95d9715-251d-470a-a062-fa1922dfa9a8";

    static {
        // General Services
        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Generic Access Profile");
        attributes.put("00001801-0000-1000-8000-00805f9b34fb", "Generic Attribute Profile");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Additional Services
        attributes.put(ACCELEROMETER_SERVICE, "Accelerometer Service");
        attributes.put(TEMPERATURE_SERVICE, "Temperature Service");
        attributes.put(BUTTON_SERVICE, "Button Service");
        attributes.put(MAGNETOMETER_SERVICE, "Magnetometer Service");

        // Accelerometer Characteristics
        attributes.put(ACCELEROMETER_MEASUREMENT, "Accelerometer Measurement");
        attributes.put(ACCELEROMETER_PERIOD, "Accelerometer Period");
        // Temperature Characteristics
        attributes.put(TEMPERATURE_MEASUREMENT, "Temperature Measurement");
        attributes.put(TEMPERATURE_PERIOD, "Temperature Period");
        // Button Characteristics
        attributes.put(BUTTON_A_MEASUREMENT, "Button A Data");
        attributes.put(BUTTON_B_MEASUREMENT, "Button B Data");
        // Magnetometer characteristics
        attributes.put(MAGNETOMETER_MEASUREMENT, "Magnetometer Measurement");
        attributes.put(MAGNETOMETER_PERIOD, "Magnetometer Period");
        attributes.put(MAGNETOMETER_BEARING, "Magnetometer Bearing");
        // Client config
        attributes.put(CLIENT_CHARACTERISTIC_CONFIG, "Client Configuration Configuration Descriptor");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
