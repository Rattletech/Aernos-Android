package com.aernos.android.permissionhelper;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Framework View class
 * Handles asking and processing permission requests.
 * <p>
 * Lets client classes to request permission and get request result
 * from anywhere with a {@link Context}. Uses {@link ResultReceiver} to deliver results to caller.
 */
public final class PermissionActivity extends AppCompatActivity {

    private static final String LOG_TAG = PermissionActivity.class.getSimpleName();

    private static final String INTENT_ACTION_CHECK_REQUIRED_PERMISSIONS = "PermissionActivity.action_permission_check";
    private static final String INTENT_EXTRA_RESULT_RECEIVER = "PermissionActivity.receiver";
    private static final String INTENT_EXTRA_PERMISSION_REQUEST_LIST = "PermissionActivity.permission_request_list";

    /**
     * Handles checking a collection of permissions
     *
     * @param context        application context
     * @param resultReceiver receiver to get
     * @param permissions    any more permission(s) to check, may be null
     */
    static void checkPermissions(@NonNull Context context,
                                 @NonNull ResultReceiver resultReceiver,
                                 @NonNull String... permissions) {
        ArrayList<String> requests = new ArrayList<>();

        // If there are more permissions to check, check them as well
        for (String morePermission : permissions) {
            if (ContextCompat.checkSelfPermission(context, morePermission) != PackageManager.PERMISSION_GRANTED) {
                requests.add(morePermission);
            }
        }

        if (!requests.isEmpty()) {
            // has permission requests
            Intent intent = new Intent(context, PermissionActivity.class);
            intent.setAction(PermissionActivity.INTENT_ACTION_CHECK_REQUIRED_PERMISSIONS);
            intent.putExtra(PermissionActivity.INTENT_EXTRA_RESULT_RECEIVER, resultReceiver);
            intent.putStringArrayListExtra(PermissionActivity.INTENT_EXTRA_PERMISSION_REQUEST_LIST, requests);
            context.startActivity(intent);
        } else {
            // has no permission to request
            // all argument permission(s) are granted already
            resultReceiver.send(PermissionResultReceiver.RESULT_PERMISSION_GRANTED, null);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String action = intent.getAction();
        if (action == null) {
            Toast.makeText(this, "Unable to request permissions", Toast.LENGTH_LONG).show();
            return;
        }

        switch (action) {
            case INTENT_ACTION_CHECK_REQUIRED_PERMISSIONS:
                ResultReceiver mResultReceiver = intent.getParcelableExtra(PermissionActivity.INTENT_EXTRA_RESULT_RECEIVER);
                ArrayList<String> mPermissionRequests = intent.getStringArrayListExtra(PermissionActivity.INTENT_EXTRA_PERMISSION_REQUEST_LIST);
                int numRequests = mPermissionRequests.size();
                checkPermissions(mResultReceiver, mPermissionRequests.toArray(new String[numRequests]));
                break;
        }
    }

    private void checkPermissions(@NonNull final ResultReceiver resultReceiver,
                                  @NonNull String... permissions) {
        if (BuildConfig.DEBUG) Log.d(LOG_TAG, "checkPermissions()");

        Dexter.withActivity(this)
                .withPermissions(permissions)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (BuildConfig.DEBUG) Log.d(LOG_TAG, "onPermissionsChecked()");

                        if (report.areAllPermissionsGranted()) {
                            resultReceiver.send(PermissionResultReceiver.RESULT_PERMISSION_GRANTED, null);
                        } else {
                            launchAppInfoUi();
                            resultReceiver.send(PermissionResultReceiver.RESULT_PERMISSION_DENIED, null);
                        }
                        resultReceiver.send(PermissionResultReceiver.RESULT_PERMISSION_REQUEST_COMPLETE, null);
                        finish();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        for (PermissionRequest permissionRequest : permissions) {
                            String name = permissionRequest.getName();
                            if (BuildConfig.DEBUG)
                                Log.d(LOG_TAG, "onPermissionRationaleShouldBeShown() for " + name);
                        }
                        token.continuePermissionRequest();
                        resultReceiver.send(PermissionResultReceiver.RESULT_PERMISSION_REQUEST_COMPLETE, null);
                        finish();
                    }
                }).check();
    }

    /**
     * Launches app info UI to let user grant all required permissions
     */
    private void launchAppInfoUi() {
        Toast.makeText(PermissionActivity.this,
                "All permissions are required to continue using the app", Toast.LENGTH_LONG).show();
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

}
