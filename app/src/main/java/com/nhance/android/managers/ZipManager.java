package com.nhance.android.managers;

import com.google.analytics.tracking.android.Log;
import com.nhance.android.processors.FileMaskProcessor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by android on 11/21/2016.
 */
public class ZipManager {
    private static final int BUFFER = 1024;

    public static void zip(String[] _files, String zipFileName) {
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            byte data[] = new byte[BUFFER];

            for (int i = 0; i < _files.length; i++) {
                FileInputStream fi = new FileInputStream(_files[i]);
                origin = new BufferedInputStream(fi, BUFFER);

                ZipEntry entry = new ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;

                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unzip(String _zipFile, String _targetLocation, String decryptedPassphrase) {

        //create target location folder if not exist
        FileMaskProcessor fileMaskProcessor = new FileMaskProcessor(decryptedPassphrase);
        dirChecker(_targetLocation, null);
        try {
            FileInputStream fin = new FileInputStream(_zipFile);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;
            byte[] buffer = new byte[1024 * 16];
            while ((ze = zin.getNextEntry()) != null) {

                //create dir if required while unzipping
                if (ze.isDirectory()) {
                    dirChecker(_targetLocation, ze.getName());
                } else {
                    if(ze.getName().contains("/")) {
                        String lSubSrting = ze.getName().substring(0,ze.getName().lastIndexOf("/"));
                        dirChecker(_targetLocation, lSubSrting);
                    }
                    String lFilePath = _targetLocation +"/"+ze.getName();
                    FileOutputStream fout = new FileOutputStream(lFilePath);
                    int bytesRead = 0;
                    int totalBytesRead = 0;
                    int len = 0;
                    while ((len = zin.read(buffer))>= 0) {
                        fout.write(buffer, 0, len);
                    }
                    zin.closeEntry();
                    fout.close();

                    byte[] readBuffer = new byte[1024 * 16];
                    FileInputStream fis = new FileInputStream(lFilePath);
                    FileOutputStream fos = new FileOutputStream(lFilePath.replace("_enc",""));
                    while ((bytesRead = fis.read(readBuffer)) != -1) {
                        byte[] writeBuffer = new byte[bytesRead];
                        fileMaskProcessor.process(readBuffer, totalBytesRead, bytesRead, writeBuffer);
                        fos.write(writeBuffer);
                        totalBytesRead = totalBytesRead + bytesRead;
                    }
                    fis.close();
                    File lFile = new File(lFilePath);
                    if(lFile.exists()){
                        lFile.delete();
                    }
                    fis.close();
                    fos.close();
                }

            }
            zin.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void dirChecker(String pTarget, String pFilePath){
        File lFile = new File(pTarget);
        if(!lFile.exists()){
            lFile.mkdir();
        }

        if(null != pFilePath ){
            lFile = new File(pTarget,pFilePath );
            if(!lFile.exists()){
                lFile.mkdirs();
            }
        }
    }
}
