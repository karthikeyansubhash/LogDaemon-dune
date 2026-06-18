package com.hp.jetadvantage.link.logdaemon.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hp.jetadvantage.link.logdaemon.services.LogcatService;
import com.hp.jetadvantage.link.logdaemon.services.WSCallbackLogManagement;

import java.util.concurrent.atomic.AtomicInteger;

public class DeviceReadyReceiver extends BroadcastReceiver {
    private static final String TAG = "[LD][DevReady]";
    public static final String DEVICE_IP = "device_ip";
    public static final String SENT_COUNT = "sent_count";
    private static final String DEVICE_READY_ACTION = "com.hp.workpath.system.DEVICE_READY";
    private static final AtomicInteger recvCount = new AtomicInteger(1);

    /**
     * The System app will send a DEVICE_READY broadcast, including the device's IP and token, upon successfully
     * connecting to the device and obtaining the access token after boot-up is completed.
     * Upon receiving the DEVICE_READY broadcast, DeviceReadyReceiver will create a OneTimeWorkRequest to initialize
     * StandardDeviceManagementService and to start StandardWebsocketCallbackService.
     * If StandardDeviceManagementService is already initialized and connected to the device, it will simply update
     * the device information without queueing the WorkRequest.
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "DeviceReady received");
        if (intent == null) {
            return;
        }

        int count = recvCount.getAndIncrement();
        Log.i(TAG, "onReceive : ENTER :" + count);
        if (context == null || !DEVICE_READY_ACTION.equals(intent.getAction())) {
            Log.i(TAG, "onReceive : Not an expected event");
            return;
        }

        String ip = intent.getStringExtra(DEVICE_IP);
        int sentCount = intent.getIntExtra(SENT_COUNT, 0);

        Log.i(TAG, "DEVICE_READY -- Count: " + count + "/" + sentCount);
        if (ip != null) {
            Log.i(TAG, "DEVICE_READY -- OK");
            if (count == 1) {
                Intent startSvc = new Intent(context.getApplicationContext(), LogcatService.class);
                context.getApplicationContext().startService(startSvc);

                Intent wsLogMgr = new Intent(context, WSCallbackLogManagement.class);
                context.getApplicationContext().startService(wsLogMgr);
            }
        } else {
            Log.e(TAG, "onReceive : IP or Token is null, IP=" + ip);
        }
        Log.i(TAG, "onReceive : EXIT :" + count);
    }
}
