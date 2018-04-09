package com.nhance.android.managers;

import android.os.Environment;

import java.io.File;

/**
 * Created by android on 10/25/2016.
 */
public class StorageVolume {

    /**
     * Represents {@link StorageVolume} type
     */
    public enum Type {
        /**
         * Device built-in internal storage. Probably points to
         * {@link Environment#getExternalStorageDirectory()}
         */
        INTERNAL,

        /**
         * External storage. Probably removable, if no other
         * {@link StorageVolume} of type {@link #INTERNAL} is returned by
         * {@link StorageHelper#getStorages(boolean)}, this might be
         * pointing to {@link Environment#getExternalStorageDirectory()}
         */
        EXTERNAL,

        /**
         * Removable usb storage
         */
        USB
    }

    /**
     * Device name
     */
    public final String device;

    /**
     * Points to mount point of this device
     */
    public final File file;

    /**
     * File system of this device
     */
    public final String fileSystem;

    /**
     * if true, the storage is mounted as read-only
     */
    public boolean mReadOnly;

    /**
     * If true, the storage is removable
     */
    public boolean mRemovable;

    /**
     * If true, the storage is emulated
     */
    public boolean mEmulated;

    /**
     * Type of this storage
     */
    public Type mType;

    StorageVolume(String device, File file, String fileSystem) {
        this.device = device;
        this.file = file;
        this.fileSystem = fileSystem;
    }

    /**
     * Returns type of this storage
     *
     * @return Type of this storage
     */
    public Type getType() {
        return mType;
    }

    /**
     * Returns true if this storage is removable
     *
     * @return true if this storage is removable
     */
    public boolean isRemovable() {
        return mRemovable;
    }

    /**
     * Returns true if this storage is emulated
     *
     * @return true if this storage is emulated
     */
    public boolean isEmulated() {
        return mEmulated;
    }

    /**
     * Returns true if this storage is mounted as read-only
     *
     * @return true if this storage is mounted as read-only
     */
    public boolean isReadOnly() {
        return mReadOnly;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((file == null) ? 0 : file.hashCode());
        return result;
    }

    /**
     * Returns true if the other object is StorageHelper and it's
     * {@link #file} matches this one's
     *
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StorageVolume other = (StorageVolume) obj;
        if (file == null) {
            return other.file == null;
        }
        return file.equals(other.file);
    }

    @Override
    public String toString() {
        return file.getAbsolutePath() + (mReadOnly ? " ro " : " rw ") + mType + (mRemovable ? " R " : "")
                + (mEmulated ? " E " : "") + fileSystem;
    }
}
