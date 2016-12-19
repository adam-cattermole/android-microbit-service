package com.example.android.bluetoothlegatt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Adam Cattermole on 11/12/2016.
 */
public class Utility {

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static short shortFromLittleEndianBytes(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    public static byte[] leBytesFromShort(short s) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(s).array();
    }

    public static String bytesToHexString(byte[] b) {
        char[] hexChars = new char[b.length *2];
        for (int i = 0; i < b.length; i++) {
            int v = b[i] & 0xFF;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[i * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static int byteToInteger(byte b) {
        return b & 0xFF;
    }
}
