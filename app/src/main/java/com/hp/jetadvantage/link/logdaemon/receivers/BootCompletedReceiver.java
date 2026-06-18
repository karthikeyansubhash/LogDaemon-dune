package com.hp.jetadvantage.link.logdaemon.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hp.jetadvantage.link.logdaemon.data.FileManager;

public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = "[LD][BCRcv]";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "BC received");


    }
}
