package com.example.android.bluetoothlegatt.mqtt;

/**
 * Created by Adam Cattermole on 11/12/2016.
 */
public class MqttConfig {

    public static final String SERVER_URI = "tcp://192.168.1.107:1883";
    public static final String CLIENT_ID = "AndroidMQTT";
    public static final String TOPIC_ACCELEROMETER = "ACCELEROMETER";
    public static final String TOPIC_BUTTON = "BUTTON";
    public static final String TOPIC_TEMPERATURE = "TEMPERATURE";
    public static final String TOPIC_MAGNETOMETER = "MAGNETOMETER";
    public static final String TOPIC_MAGNETOMETER_DATA = TOPIC_MAGNETOMETER+"/DATA";
    public static final String TOPIC_MAGNETOMETER_BEARING = TOPIC_MAGNETOMETER+"/BEARING";
}
