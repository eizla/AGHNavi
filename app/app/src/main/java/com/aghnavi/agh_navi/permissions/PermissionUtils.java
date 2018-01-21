package com.aghnavi.agh_navi.permissions;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

public final class PermissionUtils {

    private PermissionUtils() {
    }

    public static boolean checkPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (!checkPermission(context, permission)) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isDeviceInfoGranted(Context context) {
        return checkPermission(context, Manifest.permission.READ_PHONE_STATE);
    }

    public static boolean isStorageReadGranted(Context context) {
        return checkPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public static boolean isStorageWriteGranted(Context context) {
        return checkPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static void requestPermissions(Object o, int permissionId, String... permissions) {
        ActivityCompat.requestPermissions((AppCompatActivity) o, permissions, permissionId);
    }
}
