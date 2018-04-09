package com.nhance.android.processors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import android.support.v4.util.LruCache;

import com.nhance.android.utils.ArrayUtils;

public class FileMaskProcessor {

    public static final String                    PASS_PHRASE      = "the world is not enough";
    public static final int                       PASS_PHRASE_SIZE = 256;

    private static final LruCache<String, String> memCache         = new LruCache<String, String>(
                                                                           20);

    private final String                          passPhrase;
    private final int                             passBytesSize;
    private final byte[]                          passPhraseBytes;

    public FileMaskProcessor(final String passPhrase) {

        this(passPhrase, PASS_PHRASE_SIZE);
    }

    public FileMaskProcessor(final String passPhrase, final int passBytesSize) {

        this.passPhrase = passPhrase;
        this.passBytesSize = passBytesSize;
        this.passPhraseBytes = passPhraseToByteArray();
    }

    private byte[] passPhraseToByteArray() {

        byte[] t = passPhrase.getBytes();
        byte[] p = ArrayUtils.copyOfRange(t, 0, passBytesSize);
        return p;
    }

    public void process(File inFile, int readBufferSize, File outFile) {

        try {
            byte[] readBuffer = new byte[readBufferSize];
            FileOutputStream fos = new FileOutputStream(outFile);
            FileInputStream fis = new FileInputStream(inFile);
            int bytesRead = 0;
            int totalBytesRead = 0;
            while ((bytesRead = fis.read(readBuffer)) != -1) {
                byte[] writeBuffer = new byte[bytesRead];
                process(readBuffer, totalBytesRead, bytesRead, writeBuffer);
                fos.write(writeBuffer);
                totalBytesRead = totalBytesRead + bytesRead;
            }
            fis.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] process(File inFile, int readBufferSize) {
        byte[] readBuffer = null;
        try {
            ArrayList<byte[]> lbuffer = new ArrayList<>();
            readBuffer = new byte[readBufferSize];
            FileInputStream fis = new FileInputStream(inFile);
            int bytesRead = 0;
            int totalBytesRead = 0;
            while ((bytesRead = fis.read(readBuffer)) != -1) {
                byte[] writeBuffer = new byte[bytesRead];
                process(readBuffer, totalBytesRead, bytesRead, writeBuffer);
                lbuffer.add(writeBuffer);
                totalBytesRead = totalBytesRead + bytesRead;
            }
            fis.close();
            readBuffer = new byte[totalBytesRead];
            int i =0;
            for( byte[] lVals: lbuffer ){
                for(byte lval : lVals) {
                    readBuffer[i] = lval;
                    i++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return readBuffer;
    }

    public String process(byte[] inBytes) {

        byte[] outBytes = new byte[inBytes.length];
        process(inBytes, 0, outBytes.length, outBytes);
        return new String(outBytes);
    }

    public void process(byte[] bytes, int startPoint, int length, byte[] result) {

        byte[] t = ArrayUtils.copyOfRange(bytes, 0, length);
        byte[] p = getPassPhrase(startPoint, length);
        for (int i = 0; i < length; i++) {
            result[i] = (byte) (t[i] ^ p[i]);
        }
    }

    private byte[] getPassPhrase(int startPoint, int length) {

        int remainingFromLastBoundary = startPoint % passBytesSize;

        byte[] resultPassPhrase = new byte[length];
        for (int i = 0, j = remainingFromLastBoundary; i < length; i++, j = (j + 1) % passBytesSize) {
            resultPassPhrase[i] = passPhraseBytes[j];
        }
        return resultPassPhrase;
    }

    public static void putContentPassPhrase(String filePath, String passPhrase) {

        memCache.put(filePath, passPhrase);
    }

    public static String getPassPhrase(String filePath) {

        return memCache.get(filePath);
    }

    public static void main(String[] argv) {

        System.out.println("=========================================");

        final String originalFilePath = "/disk1/mydocuments/projects/nhance/tmp/ikea_lamp.3gp";
        final String encryptedFilePath = "/disk1/mydocuments/projects/nhance/tmp/enc_ikea_lamp.3gp";
        final String decryptedFilePath = "/disk1/mydocuments/projects/nhance/tmp/dec_ikea_lamp.3gp";

        FileMaskProcessor xorVideo = new FileMaskProcessor(PASS_PHRASE, PASS_PHRASE_SIZE);
        final int encReadBufferSize = PASS_PHRASE_SIZE;
        System.out.println(originalFilePath + " --> " + encryptedFilePath);
        xorVideo.process(new File(originalFilePath), encReadBufferSize, new File(encryptedFilePath));
        final int decReadBufferSize = 300;
        System.out.println(encryptedFilePath + " --> " + decryptedFilePath);
        xorVideo.process(new File(encryptedFilePath), decReadBufferSize,
                new File(decryptedFilePath));

        System.out.println("=========================================");
    }
}