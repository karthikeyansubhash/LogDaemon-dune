package com.hp.jetadvantage.link.logdaemon.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;

import com.hp.jetadvantage.link.logdaemon.data.ReaderThread;
import com.hp.jetadvantage.link.logdaemon.data.GUIDManager;
import com.hp.jetadvantage.link.logdaemon.data.RingBufferRunner;
import com.hp.jetadvantage.link.logdaemon.data.UrlData;
import com.hp.jetadvantage.link.logdaemon.util.LogPathManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

public class LogcatService extends Service {

    public static final String TAG = "[LD][LogcatService]";

    private static Context mContext;

    ReaderThread mReaderThread = null;

    private GUIDManager mGuidManager;
    private RingBufferRunner mRingBuffer;
    private BroadcastReceiver mPackageAddedReceiver = null;
    private BroadcastReceiver mUuidLogReceiver = null;


    public static final String MODEL_TNT = "TNT";
    public static final String MODEL_TRON = "TRON";
    public static final String MODEL_DUNE = "DUNE";
    private boolean isSimulator = false;

    /**
     * Default constructor required by Android Service framework
     * Service initialization is handled in onCreate() method
     */
    public LogcatService() {
        // Empty constructor - Android Service framework requirement
        // All initialization logic is performed in onCreate()
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void setApplicationContext(Context context) {
        mContext = context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setCurrentInstance(this); // Use synchronized static method

        Log.i(TAG, "LogDaemon Service onCreate()");

        setApplicationContext(getApplicationContext());

        mPackageAddedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                packageAddedReceiver(intent);
            }
        };

        mUuidLogReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                uuidLogReceiver(intent);
            }
        };

        initService();
    }

    private void initService() {

        setCurrentAppVersion();
        setCurrentModel();

        // Initialize LogPathManager directories
        LogPathManager.initializeDirectories();

        if (!searchTarGz("/data/sdpramdisk")) {
            File appStorageDirectory = getFilesDir();//appStoragePath
            deleteFiles(appStorageDirectory);
        }

        startLogMonitor();
        initThread();
    }

    private void setCurrentAppVersion() {
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            String currentAppVersion = pi.versionName;
            Log.i(TAG, "versionName : " + currentAppVersion);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    private void setCurrentModel() {
        String currentModel = getModelName();
        Log.i(TAG, "Current Model : " + currentModel);
        if (isSimulator) {
            Log.i(TAG, "Current Type : SIMULATOR");
        } else {
            Log.i(TAG, "Current Type : DEVICE OR EMULATOR");
        }
    }

    private String getModelName() {
        String[] cmd = {"getprop"};
        String model = "";
        String gs = "";
        String qemu = "";

        try {
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String curLine;
            while ((curLine = reader.readLine()) != null) {
                model = checkProperty(curLine, "ro.arch", model);
                gs = checkProperty(curLine, "ro.lxc", gs);
                qemu = checkProperty(curLine, "ro.kernel.qemu", qemu);

                if ((!model.isEmpty() && !gs.isEmpty()) || !qemu.isEmpty()) {
                    break;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return determineModelName(model, gs, qemu);
    }

    private String checkProperty(String line, String property, String currentValue) {
        if (line.contains(property)) {
            return line;
        }
        return currentValue;
    }

    private String determineModelName(String model, String gs, String qemu) {
        if (model.contains("nanotesla")) {
            return MODEL_TNT;
        } else if (model.contains("tron")) {
            if (gs.contains("gs25")) {
                return MODEL_DUNE;
            } else {
                return MODEL_TRON;
            }
        } else if (qemu.contains("1")) {
            isSimulator = true;
            return MODEL_DUNE;
        } else {
            Log.e(TAG, "Unknown device!!!");
            return "";
        }
    }

    public static Context getServiceContext() {
        return mContext;
    }

    @Override
    public void onDestroy() {
        setCurrentInstance(null); // Use synchronized static method
        stopService();
        unregisterReceiver(mPackageAddedReceiver);
        unregisterReceiver(mUuidLogReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    public void stopService() {
        stopReaderThread();
        stopSelf();
    }

    private void stopReaderThread() {
        if (mReaderThread != null/* && mReaderThread.isRunningReaderThread()*/) {
            mReaderThread.stopReaderThreadRunnable();
        }

        mReaderThread = null;
        Log.d(TAG, "Thread is stopped");
    }

    public void startLogMonitor() {
        mGuidManager = new GUIDManager(mContext);
        mGuidManager.updatePackageId();
        mRingBuffer = new RingBufferRunner();

        // Properly stop existing thread before creating new one
        if (mReaderThread != null) {
            Log.i(TAG, "Stopping existing ReaderThread before creating new one");
            mReaderThread.stopReaderThreadRunnable(); // Actually stop the thread
            try {
                mReaderThread.join(1000); // Wait for thread to finish (max 1 second)
            } catch (InterruptedException e) {
                Log.w(TAG, "Interrupted while waiting for thread to stop: " + e.getMessage());
                Thread.currentThread().interrupt(); // Restore interrupt status
            }
            mReaderThread = null;
        }

        initThread();
        registerPackageAddedReceiver();
        registerUuidLogReceiver();
    }

    private ReaderThread.RestartThreadListener mRestartThreadListener = () -> {
        Log.d(TAG, "RestartThread Listener");
        initThread();
    };

    public void initThread() {
        // Ensure no duplicate thread creation
        if (mReaderThread != null && mReaderThread.isAlive()) {
            Log.w(TAG, "ReaderThread already exists and running, skipping creation");
            return;
        }

        mReaderThread = new ReaderThread("Reader Thread", mGuidManager, mRingBuffer);
        mReaderThread.setRestartThreadListener(mRestartThreadListener);
        mReaderThread.setRunningLoopState(true);
        mReaderThread.setDaemon(true);
        mReaderThread.start();
        Log.i(TAG, "ReaderThread created and started with ID: " + mReaderThread.getId());
    }

    /**
     * Start saving log file and return the generated file path
     * @param uuid The UUID for log file identification
     * @return The path of the generated log file, or null if failed
     */
    public String startSavingLogFile(String uuid) {
        Log.d(TAG, "startSavingLogFile()");
        if (mReaderThread == null /*|| !mReaderThread.isRunningLoop()*/) {
            Log.i(TAG, "Can not save log file. There is no Reader Thread or Loop is not started..");
            return null;
        }
        return mReaderThread.startSavingLogFile(uuid);
    }

    public void stopSavingLogFile() {
        Log.d(TAG, "stopSavingLogFile()");
        if (mReaderThread == null || !mReaderThread.isRunningLoop()) {
            Log.i(TAG, "Can not stop log file. There is no Reader Thread or Loop is not started..");
        }
    }

    protected void registerPackageAddedReceiver() {
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction(UrlData.getInstance().getJson("action_install"));
        packageFilter.addAction(UrlData.getInstance().getJson("action_update"));
        registerReceiver(mPackageAddedReceiver, packageFilter);
    }

    private void packageAddedReceiver(Intent intent) {
        String action = intent.getAction();
        if (UrlData.getInstance().getJson("action_install").equals(action) ||
                UrlData.getInstance().getJson("action_update").equals(action)) {
            Log.i(TAG, "install or update App");
            if (mReaderThread != null) {
                mReaderThread.setGuidUpdateFlag(false);
            }
        }
    }

    protected void registerUuidLogReceiver() {
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction("intent.com.hp.logdaemon.uuid");
        registerReceiver(mUuidLogReceiver, packageFilter);
    }

    private void uuidLogReceiver(Intent intent) {
        String action = intent.getAction();
        if (action.equals("intent.com.hp.logdaemon.uuid")) {
            Log.i(TAG, "get event for Log file");
            String uuid = intent.getStringExtra("uuid");
            if ("all".equals(uuid)) {
                startSavingLogFile("all");
            } else {
                startSavingLogFile(uuid);
            }
        }
    }

    private boolean searchTarGz(String path) {
        File directory = new File(path);
        File[] files = directory.listFiles();
        boolean result = false;

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".tar.gz")) {
                    result = true;
                }
            }
        }
        return result;
    }

    private void deleteFiles(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File childFile : files) {
                    deleteFiles(childFile);
                }
            }
        }

        try {
            Files.delete(file.toPath());
        } catch (java.io.IOException e) {
            Log.w(TAG, "Failed to delete file: " + file.getAbsolutePath() + ", reason: " + Log.getStackTraceString(e));
        }
    }

    /**
     * Handle log management request directly without broadcast
     * More efficient than broadcast for internal communication
     * @param uuid The UUID for log file identification
     * @return The path of the generated log file, or null if failed
     */
    public static String handleLogManagementRequest(String uuid) {
        LogcatService instance = getCurrentInstance();
        if (instance != null) {
            Log.i(TAG, "Direct log management request received for UUID: " + uuid);
            return instance.startSavingLogFile(uuid);
        } else {
            Log.e(TAG, "LogcatService instance not available");
            return null;
        }
    }

    /**
     * Current LogcatService instance for static access
     */
    private static LogcatService currentInstance;

    /**
     * Synchronized static method to set current instance
     * @param instance the LogcatService instance to set
     */
    private static synchronized void setCurrentInstance(LogcatService instance) {
        currentInstance = instance;
    }

    /**
     * Get current LogcatService instance
     * @return current service instance or null if not available
     */
    public static synchronized LogcatService getCurrentInstance() {
        return currentInstance;
    }

    /**
     * Check if current LogcatService instance is running in simulator mode
     * @return true if simulator, false otherwise
     */
    public static boolean isCurrentInstanceSimulator() {
        LogcatService instance = getCurrentInstance();
        return instance != null && instance.isSimulator;
    }
}
