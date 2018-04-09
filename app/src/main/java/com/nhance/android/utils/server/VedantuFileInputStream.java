package com.nhance.android.utils.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.util.Log;

import com.nhance.android.processors.FileMaskProcessor;

public class VedantuFileInputStream extends FileInputStream {

    private static final String TAG = "VedantuFileInputStream";

    private long                dataLen;
    private int                 bytesRead;
    private FileMaskProcessor   fileMaskProcessor;

    public VedantuFileInputStream(File file, final Long dataLen) throws FileNotFoundException {

        super(file);
        this.dataLen = dataLen;
        String passPhrase = FileMaskProcessor.getPassPhrase(getFileCachePath(file));
        // Log.d(TAG, "file : " + getFileCachePath(file) + ", passPhrase: " +
        // passPhrase);
        if (passPhrase != null) {
            fileMaskProcessor = new FileMaskProcessor(passPhrase);
        }
        Log.d(TAG, "created VedantuFileInputStream");
    }

    @Override
    public int available() throws IOException {

        return (int) dataLen;
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {

        int bytesRead = 0;
        if (null != fileMaskProcessor) {
            byte[] tempBuffer = new byte[buffer.length];
            bytesRead = super.read(tempBuffer, offset, length);
            fileMaskProcessor.process(tempBuffer, this.bytesRead, length, buffer);
            incReadCount(bytesRead);
        } else {
            bytesRead = super.read(buffer, offset, length);
        }
        return bytesRead;
    }

    @Override
    public long skip(long byteCount) throws IOException {

        long skipBytes = super.skip(byteCount);
        incReadCount(skipBytes);
        return skipBytes;
    }

    public String getFileCachePath(File file) {

        return file.getAbsolutePath();
        // .replace(
        // Environment.getExternalStorageDirectory().getAbsolutePath(),
        // LocalManager.EMPTY_TEXT);
    }

    private void incReadCount(long bytesRead) {

        this.bytesRead += bytesRead;
    }
}
