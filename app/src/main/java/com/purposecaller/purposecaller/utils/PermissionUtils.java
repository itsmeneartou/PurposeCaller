package com.purposecaller.purposecaller.utils;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import static com.purposecaller.purposecaller.activities.MainActivity.ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE;

public class PermissionUtils  {


    public PermissionUtils() {
    }

   public  boolean checkAndRequestPermissions(Activity ctx,int requestCode,Fragment fragment, String ... permissions) {

        List<String> listPermissionsNeeded = new ArrayList<>();
        for(int i=0;i<permissions.length;i++){
            if(ContextCompat.checkSelfPermission(ctx,permissions[i])!=PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(permissions[i]);
            }
        }


        if (!listPermissionsNeeded.isEmpty() && android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {

           fragment.requestPermissions(listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]),requestCode);
            return false;
        }
        return true;
    }

    public  boolean checkAndRequestPermissions(Activity activity,int requestCode, String ... permissions) {

        List<String> listPermissionsNeeded = new ArrayList<>();
        for(int i=0;i<permissions.length;i++){
            if(ActivityCompat.checkSelfPermission(activity,permissions[i])!=PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(permissions[i]);
            }
        }


        if (!listPermissionsNeeded.isEmpty() && android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {

           ActivityCompat.requestPermissions(activity,listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]),requestCode);
            return false;
        }
        return true;
    }

    public boolean checkOverlayPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity)) {

                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" +activity.getPackageName()));
                activity.startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
                return false;
        }
        return true;
    }

}
