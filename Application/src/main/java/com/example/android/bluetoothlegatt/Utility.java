package com.example.android.bluetoothlegatt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Adam Cattermole on 11/12/2016.
 */
public class Utility {

    public static short shortFromLittleEndianBytes(byte[] b) {
        ByteBuffer bb = ByteBuffer.wrap(b);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getShort();
    }
}
