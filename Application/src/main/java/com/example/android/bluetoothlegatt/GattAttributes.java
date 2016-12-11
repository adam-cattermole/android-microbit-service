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

package com.example.android.bluetoothlegatt;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class GattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String ACCELEROMETER_MEASUREMENT = "e95dca4b-251d-470a-a062-fa1922dfa9a8";
    public static String ACCELEROMETER_PERIOD = "e95dfb24-251d-470a-a062-fa1922dfa9a8";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    static {
        // General Services
        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Generic Access Profile");
        attributes.put("00001801-0000-1000-8000-00805f9b34fb", "Generic Attribute Profile");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Additional Services
        attributes.put("e95d0753-251d-470a-a062-fa1922dfa9a8", "Accelerometer Service");
        attributes.put("e95d9882-251d-470a-a062-fa1922dfa9a8", "Button Service");
        attributes.put("e95d6100-251d-470a-a062-fa1922dfa9a8", "Temperature Service");
        // Accelerometer Characteristics
        attributes.put(ACCELEROMETER_MEASUREMENT, "Accelerometer Measurement");
        attributes.put(ACCELEROMETER_PERIOD, "Accelerometer Period");
        // Button Characteristics
        attributes.put("e95dda90-251d-470a-a062-fa1922dfa9a8", "Button A Data");
        attributes.put("e95dda91-251d-470a-a062-fa1922dfa9a8", "Button B Data");
        // Temperature Characteristics
        attributes.put("e95d9250-251d-470a-a062-fa1922dfa9a8", "Temperature Measurement");
        attributes.put("e95d1b25-251d-470a-a062-fa1922dfa9a8", "Temperature Period");
        // Client config
        attributes.put(CLIENT_CHARACTERISTIC_CONFIG, "Client Configuration Configuration Descriptor");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
