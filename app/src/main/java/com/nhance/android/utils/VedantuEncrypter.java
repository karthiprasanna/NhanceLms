package com.nhance.android.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import android.util.Log;

public class VedantuEncrypter {

    private static enum KeyType {
        ORG, USER;
    }

    private static String          TAG                        = "VedantuEncrypter";
    public static VedantuEncrypter INSTANCE                   = new VedantuEncrypter();
    private static final String    RSA_ALGORITHM              = "RSA";
    public static final String     AES_ALGORITHM_WITH_PADDING = "AES/ECB/PKCS5Padding"; // "AES/ECB/PKCS5Padding";
    public static final String     AES                        = "AES";
    private static final int       RSA_CHUNK_SIZE             = 117;

    private VedantuEncrypter() {

    }

    public byte[] AESEncrypt(byte[] clear, Key key) throws Exception, NoSuchPaddingException {

        Cipher cipher = Cipher.getInstance(AES_ALGORITHM_WITH_PADDING);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(clear);
    }

    public byte[] AESDecrypt(byte[] encrypted, Key key) throws NoSuchAlgorithmException, Exception {

        Cipher cipher = Cipher.getInstance(AES_ALGORITHM_WITH_PADDING);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encrypted);
    }

    public byte[] RSAEncrypt(byte[] clear, Key key) throws Exception, NoSuchPaddingException {

        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return processData(cipher, clear, RSA_CHUNK_SIZE);
    }

    public byte[] RSADecrypt(byte[] encrypted, Key key) throws Exception, NoSuchPaddingException {

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return processData(cipher, encrypted, 128);
    }

    private byte[] processData(Cipher cipher, byte[] clear, int processChunk)
            throws IllegalBlockSizeException, BadPaddingException {

        byte[] processed = new byte[] {};
        if (clear.length > processChunk) {

            int loopCount = (clear.length % processChunk) == 0 ? (clear.length / processChunk)
                    : ((clear.length / processChunk) + 1);
            for (int i = 0; i < loopCount; i++) {
                int srcPos = i * processChunk;
                int endPos = Math.min(processChunk, (clear.length - srcPos));
                byte[] encPart = new byte[endPos];
                if (i == clear.length / endPos && clear.length % endPos != 0) {
                    endPos = clear.length % endPos;
                }
                System.out.println("i= " + i + " srcPos: " + srcPos + ", endPos: "
                        + (srcPos + endPos));
                System.arraycopy(clear, srcPos, encPart, 0, endPos);
                encPart = processChunkData(cipher, encPart);
                processed = copyBytes(processed, encPart);
                encPart = null;
            }
        } else {
            processed = processChunkData(cipher, clear);
        }
        return processed;
    }

    private byte[] processChunkData(Cipher cipher, byte[] clear) throws IllegalBlockSizeException,
            BadPaddingException {

        System.out.println("input chunks : " + clear.length);
        byte[] processed = cipher.doFinal(clear);
        System.out.println("output chunks : " + processed.length);
        return processed;
    }

    private byte[] copyBytes(byte[] one, byte[] two) {

        byte[] combined = new byte[one.length + two.length];
        for (int i = 0; i < combined.length; ++i) {
            combined[i] = i < one.length ? one[i] : two[i - one.length];
        }
        return combined;
    }

    public void saveKeyPair(String path, KeyPair keyPair, KeyType keyType) throws IOException {

        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        // Store Public Key.
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
        FileOutputStream fos = new FileOutputStream(path + "/" + keyType.name() + ".public.key");
        fos.write(x509EncodedKeySpec.getEncoded());
        closeInputOutputStream(fos);

        // Store Private Key.
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
        fos = new FileOutputStream(path + "/" + keyType.name() + ".private.key");
        fos.write(pkcs8EncodedKeySpec.getEncoded());
        closeInputOutputStream(fos);
    }

    public KeyPair loadKeyPair(String path, String algorithm, KeyType keyType) throws IOException,
            NoSuchAlgorithmException, InvalidKeySpecException {

        // Read Public Key.
        File filePublicKey = new File(path + "/" + keyType.name() + ".public.key");
        File filePrivateKey = new File(path + "/" + keyType.name() + ".private.key");
        if (!filePublicKey.exists() || !filePrivateKey.exists()) {
            System.out.println("public & private keys are not present");
            return null;
        }

        FileInputStream fis = new FileInputStream(filePublicKey);
        byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
        fis.read(encodedPublicKey);
        closeInputOutputStream(fis);

        // Read Private Key.

        fis = new FileInputStream(filePrivateKey);
        byte[] encodedPrivateKey = new byte[(int) filePrivateKey.length()];
        fis.read(encodedPrivateKey);
        closeInputOutputStream(fis);

        // Generate KeyPair.
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
        return new KeyPair(publicKey, privateKey);
    }

    public KeyPair generateKeys() throws NoSuchAlgorithmException {

        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        keyGenerator.initialize(1024);
        System.out.println("generating new public/private key pairs");
        return keyGenerator.genKeyPair();
    }

    public static void encryptFile(File inputFile, File outputFile, Key key, String algorithm)
            throws NoSuchAlgorithmException, NoSuchPaddingException, Exception {

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        InputStream is = new FileInputStream(inputFile);
        OutputStream os = new FileOutputStream(outputFile);
        try {
            byte[] buf = new byte[2048];
            // bytes at this stream are first encoded
            os = new CipherOutputStream(os, cipher);
            // read in the clear text and write to out to encrypt
            int numRead = 0;
            while ((numRead = is.read(buf)) >= 0) {
                os.write(buf, 0, numRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // close all streams
            closeInputOutputStream(is);
            closeInputOutputStream(os);
        }
        System.out.println("inputfile: " + inputFile.length() + ", encrypted file length : "
                + outputFile.length());
    }

    public static void decryptFile(File inputFile, File outputFile, Key key, String algorithm)
            throws NoSuchAlgorithmException, NoSuchPaddingException, Exception {

        InputStream is = new FileInputStream(inputFile);
        decryptFile(is, outputFile, key, algorithm);
    }

    public static void decryptFile(InputStream is, File outputFile, Key key, String algorithm)
            throws NoSuchAlgorithmException, NoSuchPaddingException, Exception {

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key);
        OutputStream os = new FileOutputStream(outputFile);
        try {
            byte[] buf = new byte[2048];
            // bytes at this stream are first encoded
            os = new CipherOutputStream(os, cipher);
            // read in the clear text and write to out to encrypt
            int numRead = 0;
            while ((numRead = is.read(buf)) >= 0) {
                os.write(buf, 0, numRead);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // close all streams
            closeInputOutputStream(is);
            closeInputOutputStream(os);
        }
    }

    private static void closeInputOutputStream(Closeable stream) {

        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String decryptWithPublicKey(String passphrase, byte[] encodedPublicKey) {

        try {
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
            PublicKey kPublicKey = keyFactory.generatePublic(publicKeySpec);
            Log.d(TAG, "passphrase to be decrypted: " + passphrase);
            byte[] decreptedPassPhrase = RSADecrypt(StringUtils.parseHexBinary(passphrase),
                    kPublicKey);
            Log.d(TAG, "decrypted key: " + decreptedPassPhrase);
            Log.d(TAG, "decrypted string: " + new String(decreptedPassPhrase));
            return new String(decreptedPassPhrase);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (InvalidKeySpecException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (NoSuchPaddingException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public String decryptWithPrivateKey(String passphrase, byte[] encodedPrivateKey) {

        try {

            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
            PrivateKey kPrivateKey = keyFactory.generatePrivate(privateKeySpec);
            Log.d(TAG, "passphrase to be decrypted: " + passphrase);
            byte[] decreptedPassPhrase = RSADecrypt(StringUtils.parseHexBinary(passphrase),
                    kPrivateKey);
            Log.d(TAG, "decrypted key: " + decreptedPassPhrase);
            Log.d(TAG, "decrypted string: " + new String(decreptedPassPhrase));
            return new String(decreptedPassPhrase);

        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (InvalidKeySpecException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (NoSuchPaddingException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public String md5(String input) {

        String md5 = null;
        if (null == input)
            return null;
        try {
            // Create MessageDigest object for MD5
            MessageDigest digest = MessageDigest.getInstance("MD5");
            // Update input string in message digest
            digest.update(input.getBytes(), 0, input.length());
            // Converts message digest value in base 16 (hex)
            md5 = new BigInteger(1, digest.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {}
        return md5;
    }
}
