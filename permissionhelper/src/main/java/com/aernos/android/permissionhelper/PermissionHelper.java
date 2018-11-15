package com.aernos.android.permissionhelper;

import android.content.Context;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Helper class for requesting app permission
 * Note: Framework detail
 */
public final class PermissionHelper {

    private static final String LOG_TAG = PermissionHelper.class.getSimpleName();

    private String[] mPermissions;
    @Nullable
    private Context mContext;
    private OnPermissionsChangedListener mOnPermissionChangedListener;

    private PermissionHelper(@NonNull Context context) {
        mContext = context;
    }

    @NonNull
    public static PermissionBuilder with(@NonNull Context context) {
        return new PermissionBuilder(context);
    }

    public void check() {
        ResultReceiver resultReceiver = new PermissionResultReceiver(new Handler())
                .withListener(mOnPermissionChangedListener);

        if (mContext == null) {
            // Can't check permissions without context
            if (mOnPermissionChangedListener != null) {
                mOnPermissionChangedListener.onError(new IllegalArgumentException("context is required"));
            }
        } else {
            PermissionActivity.checkPermissions(mContext, resultReceiver, mPermissions);
        }
    }

    private void setOnPermissionChangedListener(@NonNull OnPermissionsChangedListener listener) {
        this.mOnPermissionChangedListener = listener;
    }

    private void setPermissions(@NonNull String... permissions) {
        this.mPermissions = permissions;
    }

    public static class PermissionBuilder implements Builder {
        private PermissionHelper mInstance;

        PermissionBuilder(@NonNull Context context) {
            mInstance = new PermissionHelper(context);
        }

        @NonNull
        @Override
        public PermissionBuilder withListener(@NonNull OnPermissionsChangedListener listener) {
            mInstance.setOnPermissionChangedListener(listener);
            return this;
        }

        @NonNull
        public PermissionBuilder withPermissions(@NonNull String... permissions) {
            mInstance.setPermissions(permissions);
            return this;
        }

        @Override
        public void check() {
            mInstance.check();
        }
    }

    /**
     * Defines builder responsibilities and its fluent API
     */
    interface Builder {

        @NonNull
        PermissionBuilder withListener(@NonNull OnPermissionsChangedListener listener);

        @NonNull
        PermissionBuilder withPermissions(@NonNull String... permissions);

        void check();

    }

    public interface OnPermissionsChangedListener {
        void onGranted();

        void onDenied();

        void onError(@NonNull Throwable t);

        void onComplete();
    }
}
