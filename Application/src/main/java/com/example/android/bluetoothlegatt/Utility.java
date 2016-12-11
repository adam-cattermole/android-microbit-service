package com.example.android.bluetoothlegatt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Adam Cattermole on 11/12/2016.
 */
public class Utility {

    public static short shortFromLittleEndianBytes(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
//        ByteBuffer bb = ByteBuffer.wrap(b);
//        bb.order(ByteOrder.LITTLE_ENDIAN);
//        return bb.getShort();
    }

    public static byte[] leBytesFromShort(short s) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(s).array();
//        ByteBuffer bb = ByteBuffer.allocate(2);
//        bb.order(ByteOrder.LITTLE_ENDIAN);
//        bb.asShortBuffer();
//        bb.pu(s);
//        return bb.array();
    }
}
