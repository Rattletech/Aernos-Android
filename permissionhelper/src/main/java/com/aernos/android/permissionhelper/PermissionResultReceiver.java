package com.aernos.android.permissionhelper;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Responsible for defining behavior on result
 * used in communication between android framework classes.
 */
final class PermissionResultReceiver extends ResultReceiver {

    public static final int RESULT_PERMISSION_GRANTED = 1001;
    public static final int RESULT_PERMISSION_DENIED = 2001;
    public static final int RESULT_PERMISSION_REQUEST_COMPLETE = 3001;

    private PermissionHelper.OnPermissionsChangedListener mOnPermissionChangedListener;

    PermissionResultReceiver(Handler handler) {
        super(handler);
    }

    public PermissionResultReceiver withListener(@NonNull PermissionHelper.OnPermissionsChangedListener listener){
        mOnPermissionChangedListener = listener;
        return this;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case PermissionResultReceiver.RESULT_PERMISSION_GRANTED:
                if (mOnPermissionChangedListener != null) {
                    mOnPermissionChangedListener.onGranted();
                }
                break;
            case PermissionResultReceiver.RESULT_PERMISSION_DENIED:
                if (mOnPermissionChangedListener != null) {
                    mOnPermissionChangedListener.onDenied();
                }
                break;
            case PermissionResultReceiver.RESULT_PERMISSION_REQUEST_COMPLETE:
                if (mOnPermissionChangedListener != null) {
                    mOnPermissionChangedListener.onComplete();
                }
                break;
        }
        super.onReceiveResult(resultCode, resultData);
    }

}
