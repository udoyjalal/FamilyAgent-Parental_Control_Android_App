package com.example.familyagent;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.telecom.TelecomManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.familyagent.databinding.ActivityMainBinding;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mBinding;
    private SharedPreferencesHelper mHelper;
    String PROVIDER = LocationManager.NETWORK_PROVIDER;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mHelper = ((ParentalMonitorApplication) getApplication()).getSharedPreferencesHelper();

        mBinding.startButton.setOnClickListener(v -> checkValidation());

        validateEmailAddress();
        validateInputTime();

        requestForPermission();

    }

    private void validateInputTime() {
        mBinding.timeTextInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mBinding.emailTextInputLayout.setError("");
            }
        });
    }

    private void validateEmailAddress() {
        mBinding.emailTextInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mBinding.emailTextInputLayout.setError("");
            }
        });
    }

    private void checkValidation() {
        Editable mEmailEditTextText = mBinding.emailTextInputEditText.getText();
        if (mEmailEditTextText != null) {
            String email = mEmailEditTextText.toString();
            if (isValidEmail(email)) {
                Editable mTimeEditTextText = mBinding.timeTextInputEditText.getText();
                if (mTimeEditTextText != null) {
                    String timeString = mTimeEditTextText.toString();
                    int time = Integer.parseInt(timeString);
                    if (time >= 15) {
                        setupWorkManager(email, time);
                    } else {
                        showIntervalTimeError();
                    }
                } else {
                    showIntervalTimeError();
                }
            } else {
                showEmailAddressError();
            }
        } else {
            showEmailAddressError();
        }
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    private void showEmailAddressError() {
        String error = getString(R.string.error_invalid_email_address);
        mBinding.emailTextInputLayout.setError(error);
    }

    private void showIntervalTimeError() {
        String error = getString(R.string.error_invalid_interval_time);
        mBinding.timeTextInputLayout.setError(error);
    }

    private void setupWorkManager(String email, int time) {
        mHelper.saveEmailAddress(email);
        mHelper.saveIntervalTime(time);
        Data data = new Data.Builder()
                .putString(SynchronousWork.EMAIL, email)
                .build();
        PeriodicWorkRequest saveRequest =
                new PeriodicWorkRequest.Builder(SynchronousWork.class, time, TimeUnit.MINUTES)
                        .setInputData(data)
                        .build();
        WorkManager.getInstance().enqueue(saveRequest);
        hideIcon();
        finish();
    }

    private void hideIcon() {
        final PackageManager mPackageManager = getPackageManager();
        final ComponentName componentName = new ComponentName(this, MainActivity.class);
        mPackageManager.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("CheckResult")
    private void requestForPermission() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .requestEachCombined(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_CALL_LOG)
                .subscribe(permission -> { // will emit 1 Permission object
                    if (permission.granted) {
                        // All permissions are granted !
                        takingPermissionToRunInBackground();
                        //get last known location, if available
                        //showMyLocation();

                    } else if (permission.shouldShowRequestPermissionRationale) {
                        // At least one denied permission without ask never again
                    } else {
                        // At least one denied permission with ask never again
                        // Need to go to the settings
                    }
                });
    }

    private void takingPermissionToRunInBackground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }
    }



 /*   @RequiresApi(api = Build.VERSION_CODES.M)

    private void showMyLocation() {

        GPSTracker finder;
        double longitude = 0.0, latitude = 0.0;
        finder = new GPSTracker(this);
        if (finder.canGetLocation()) {
            latitude = finder.getLatitude();
            longitude = finder.getLongitude();
            Toast.makeText(this, "Location > " + latitude + " — " + longitude, Toast.LENGTH_LONG).show();
        } else {
            finder.showSettingsAlert();
        }
    }  */


}
