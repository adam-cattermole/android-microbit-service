# android-microbit-service

Repository used for creating a android-microbit-service as part of a small project.

The service will sit on an Android phone, reading data from a BLE device (micro:bit), and transferring the data over MQTT to a data pipeline.

The BLE work is an adaptation of the open source android example to include the required functionality of this project. The original is here [android-BluetoothLeGatt](https://github.com/googlesamples/android-BluetoothLeGatt).

The work involving MQTT makes use of the [Eclipse Paho Android Service](https://eclipse.org/paho/clients/android/).
