package com.nhance.android.utils;

import java.util.Arrays;

public class ArrayUtils {

    public static byte[] copyOfRange(byte[] original, int start, int end) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
            return Arrays.copyOfRange(original, start, end);
        } else {
            int length = end - start;
            byte[] result = new byte[length];
            System.arraycopy(original, start, result, 0, length);
            return result;
        }
    }
}
