package com.hp.jetadvantage.link.logdaemon.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.hp.jetadvantage.link.system.IWebsocketCallback;
import com.hp.jetadvantage.link.system.IWebsocketCallbackService;


public abstract class BaseWebsocketCallbackService extends Service {

    public static final String TAG = "[LD][BaseWebsocketCallbackService]";

    protected IWebsocketCallbackService callbackService = null;
    private boolean serviceBound = false; // Track service binding state

    private String mTarget = "";
    private IWebsocketCallback callback = new IWebsocketCallback.Stub() {
        @Override
        public void onMessageReceived(int what, String data) throws RemoteException {
            onReceived(what, data);
        }
    };

    protected ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected .. : add callback to " + mTarget);
            serviceBound = true; // Set bound state to true
            try {
                callbackService = IWebsocketCallbackService.Stub.asInterface(service);
                // Add delay to prevent concurrent modification
                new Thread(() -> {
                    try {
                        Thread.sleep(100); // Small delay to avoid concurrency issues
                        addCallbackToService();
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Thread interrupted while adding callback: " + Log.getStackTraceString(e));
                        Thread.currentThread().interrupt(); // Restore interrupted status
                    }
                }).start();
            } catch (Exception e) {
                Log.e(TAG, "Error connecting to service: " + Log.getStackTraceString(e));
            }
        }

        private void addCallbackToService() {
            try {
                if (callbackService != null) {
                    // Include package name in the target identifier
                    String packageName = getPackageName();

                    callbackService.addCallback(callback, mTarget);
                    Log.i(TAG, "Successfully added callback with package: " + packageName);

                    // Test samples functionality has been moved to LogTestReceiver
                    // Use broadcast commands instead:
                    // adb shell am broadcast -a com.hp.jetadvantage.link.logdaemon.TEST_ALL_LOG_TYPES
                }
            } catch (android.os.DeadObjectException e) {
                Log.w(TAG, "Service died, attempting to reconnect: " + Log.getStackTraceString(e));
                handleServiceDeath();
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException adding callback: " + Log.getStackTraceString(e));
                // Retry after a short delay
                retryAddCallback();
            } catch (SecurityException e) {
                Log.e(TAG, "Security exception - interface mismatch: " + Log.getStackTraceString(e));
                handleInterfaceMismatch();
            } catch (Exception e) {
                Log.e(TAG, "Unexpected exception adding callback: " + Log.getStackTraceString(e));
                retryAddCallback();
            }
        }

        private void handleServiceDeath() {
            Log.i(TAG, "Handling service death - clearing reference and attempting reconnection");

            // Clear the dead service reference
            callbackService = null;

            // Unbind from the dead service if still bound
            try {
                if (serviceBound) {
                    unbindService(connection);
                    serviceBound = false;
                }
            } catch (Exception e) {
                Log.w(TAG, "Error unbinding from dead service: " + e.getMessage());
            }

            // Attempt to reconnect after a delay
            new Thread(() -> {
                try {
                    Thread.sleep(2000); // Wait longer for service recovery
                    Log.i(TAG, "Attempting to reconnect to websocket callback service");
                    reconnectToService();
                } catch (InterruptedException e) {
                    Log.e(TAG, "Service reconnection thread interrupted: " + Log.getStackTraceString(e));
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        /**
         * Reconnect to the websocket callback service
         */
        private void reconnectToService() {
            boolean result = bindToCallbackService();
            Log.i(TAG, "Reconnection attempt result: " + result);
        }

        private void retryAddCallback() {
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // Wait 1 second before retry
                    if (callbackService != null) {
                        addCallbackToService();
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, "Retry thread interrupted: " + Log.getStackTraceString(e));
                    Thread.currentThread().interrupt(); // Restore interrupted status
                }
            }).start();
        }

        private void handleInterfaceMismatch() {
            Log.w(TAG, "Attempting to handle interface mismatch...");
            callbackService = null;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected .. : ");
            callbackService = null;
            serviceBound = false; // Set bound state to false
        }
    };

    protected BaseWebsocketCallbackService(String target) {
        this.mTarget = target;
    }

    public abstract void onReceived(int what, String data);

    public void sendMessage(int what, String data) throws RemoteException {
        callbackService.sendMessage(what, data);
    }

    /**
     * Common method to bind to the websocket callback service
     * @return true if binding was successful, false otherwise
     */
    private boolean bindToCallbackService() {
        Intent intent = new Intent();
        intent.setAction("com.hp.jetadvantage.link.system.services.WebSocketCallbackService");
        intent.setPackage("com.hp.jetadvantage.link.system");

        try {
            boolean result = bindService(intent, connection, Context.BIND_AUTO_CREATE);
            Log.i(TAG, "Service binding attempt result: " + result);
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Exception during service binding: " + Log.getStackTraceString(e));
            return false;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean result = false;
        result = bindToCallbackService();

        Log.i(TAG, "onStartCommand() bindService result=" + result);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            if (serviceBound) {
                unbindService(connection);
                serviceBound = false;
            }
        } catch (Exception e) {
            Log.e(TAG, "onDestroy() unbindService Exception:" + Log.getStackTraceString(e));
        }
    }
}
