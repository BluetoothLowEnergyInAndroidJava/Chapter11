package tonyg.example.com.examplebleperipheral.utilities;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;


/**
 * Convert data formats
 *
 * @author Tony Gaitatzis backupbrain@gmail.com
 * @date 2015-12-21
 */

public class DataConverter {
    private static final String TAG = DataConverter.class.getSimpleName();


    /**
     * Convert bytes to a hexadecimal String
     *
     * @param bytes a byte array
     * @return hexadecimal string
     */
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 3];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            hexChars[j * 3 + 2] = 0x20; // space
        }
        return new String(hexChars);
    }


    /**
     * Convert hex String to a byte array
     *
     * @param hexString a String representation of hexadecimal
     * @return byte array
     */
    public static byte[] hexToBytes(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i+1), 16));
        }
        return data;
    }

    /**
     * convert bytes to an integer in Little Endian for debugging purposes
     *
     * @param bytes a byte array
     * @return integer integer representation of byte array
     */
    public static int bytesToInt(byte[] bytes) {
        if (bytes.length < 1) {
            return 0;
        }
        return (int) bytes[0];
    }

    /**
     * Convert uint16_t byte array into double
     *
     * @param bytes a 2-byte byte array
     * @return double value of bytes
     */
    public static double bytesToDouble(byte[] bytes) {
        if (bytes.length < 2) {
            return 0;
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.BIG_ENDIAN);
        return (double) buffer.getChar();
    }

    /**
     * Convert uint16_t byte array into a signed integer
     *
     * @param bytes a 1-byte byte array
     * @return signed integer value of bytes
     */
    public static int bytesToSignedInt(byte[] bytes) {
        if (bytes.length < 1) {
            return 0;
        }
        return (int) bytes[0];
    }

    /**
     * Convert uint16_t byte array into an unsigned integer
     *
     * @param bytes a 2-byte byte array
     * @return unsigned int value of bytes
     */
    public static int bytesToUnsignedInt(byte[] bytes) {
        if (bytes.length < 2) {
            return 0;
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.BIG_ENDIAN);


        int intValue = (int) buffer.getChar();
        if (intValue < 0) intValue = intValue & 0xffffffff;
        return intValue;
    }

    /**
     * Convert uint16_t byte array into UUID
     *
     * @param bytes a 2-byte byte array
     * @return a UUID
     */
    public static UUID bytesToUuid(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.BIG_ENDIAN);
        UUID uuid = new UUID(buffer.getLong(), buffer.getLong());
        return uuid;
    }
}
