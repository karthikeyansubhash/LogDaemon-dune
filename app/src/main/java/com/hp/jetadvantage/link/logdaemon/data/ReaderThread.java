package com.hp.jetadvantage.link.logdaemon.data;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.hp.jetadvantage.link.logdaemon.services.LogcatService;
import com.hp.jetadvantage.link.logdaemon.util.LogPathManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class ReaderThread extends Thread {

    public static final String TAG = "[LD][ReaderThread]";

    public static final String LINK_ADB_DEFAULT_CMD = "logcat";
    public static final String LINK_ADB_DEFAULT_CMD_UNDER_API23 = "logcat -v threadtime";

    /*For edit log data*/
    public static final String LOGDATA_DELIMITER = " ";
    public static final int LOGDATA_PID_INDEX_WITH_DEFAULT = 2;
    public static final int LOGDATA_TAG_INDEX_WITH_DEFAULT = 5;

    private boolean mFatalFlag = false;
    private int mNonErrorLogCount = 0; // Count non-error logs after fatal detection
    private static final int MAX_NON_ERROR_LOGS = 5; // Allow up to 5 non-error logs

    private RingBufferRunner mFatalRingBuffer;

    private RestartThreadListener mRestartThreadListener;

    public interface RestartThreadListener{
        void restartThread();
    }

    public void setRestartThreadListener(RestartThreadListener listener){
        mRestartThreadListener = listener;
    }

    public RestartThreadListener getRestartThreadListener() {
        return mRestartThreadListener;
    }

    public boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException | NullPointerException e) {
            return false;
        }
        return true;
    }

    /*For control thread life-cycle*/
    private volatile boolean mIsRunnableRunning = false; // state of Runnable itself
    private volatile boolean mIsLoopRunning = false; // For stop sub loop within run()

    /*For watch logcat*/
    private Process mLogcatProcess = null;
    private BufferedReader mReader = null;
    private Object lockObj = new Object();

    private ActivityManager mActivityManager;

    private GUIDManager mGuidManager;

    private RingBufferRunner mRingBuffer;

    private RingBufferRunner mAllBuffer;

    private PreProcess mPreProcessCls;

    private boolean mGuidUpdatedFlag = true;

    private boolean supportAppFlag = false;

    public ReaderThread(String name, GUIDManager idManager, RingBufferRunner buffer) {
        super(name);
        this.mGuidManager = idManager;
        this.mRingBuffer = buffer;
        this.mAllBuffer = buffer;
        this.mActivityManager = (ActivityManager) LogcatService.getServiceContext().getSystemService(Context.ACTIVITY_SERVICE);
        this.mPreProcessCls = new PreProcess("", "", "");
    }

    public void setGuidUpdateFlag(boolean bool){
        this.mGuidUpdatedFlag = bool;
    }

    @Override
    public void run() {
        synchronized (lockObj) {
            initializeThread();
            if (!startLogcatProcess()) {
                cleanup();
                return;
            }

            try {
                processLogLines();
            } catch (IOException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            } finally {
                cleanup();
            }
        }
    }

    private void initializeThread() {
        mIsRunnableRunning = true;
        Log.i(TAG, "~~~~~~~~~~~ Start LOG THREAD ~~~~~~~~~~~~~~");
    }

    private boolean startLogcatProcess() {
        try {
            mLogcatProcess = Runtime.getRuntime().exec(
                    (Build.VERSION.SDK_INT > 22) ?
                            LINK_ADB_DEFAULT_CMD : LINK_ADB_DEFAULT_CMD_UNDER_API23);
            mReader = new BufferedReader(new InputStreamReader(mLogcatProcess.getInputStream()));
            return true;
        } catch (IOException e) {
            killRuntimeProcess();
            return false;
        }
    }

    private void processLogLines() throws IOException {
        String curLogStr;
        while (isRunningLoop()) {
            updateGuidIfNeeded();

            curLogStr = mReader.readLine();
            if (curLogStr == null) break;

            mAllBuffer.append(curLogStr);
            processLogLine(curLogStr);
        }
    }

    private void updateGuidIfNeeded() {
        if (!mGuidUpdatedFlag) {
            mGuidManager.updatePackageId();
            mGuidUpdatedFlag = true;
        }
    }

    private void processLogLine(String logStr) {
        String[] splits = logStr.split("(\\s+)", 6);
        if (!isValidLogLine(splits)) return;

        supportAppFlag = false;
        if (splits.length > LOGDATA_PID_INDEX_WITH_DEFAULT && isInteger(splits[LOGDATA_PID_INDEX_WITH_DEFAULT])) {
            String processedLog = processLogWithAppInfo(splits, logStr);
            if (supportAppFlag) {
                handleFatalExceptionAndAppendLog(splits, processedLog);
            }
        }
    }

    private boolean isValidLogLine(String[] splits) {
        return splits != null && splits[0].matches("^\\d.*$");
    }

    private String processLogWithAppInfo(String[] splits, String originalLog) {
        splits[LOGDATA_PID_INDEX_WITH_DEFAULT] = getAppInfoFromPid(splits[LOGDATA_PID_INDEX_WITH_DEFAULT]);

        if (!supportAppFlag) return originalLog;

        StringBuilder editLog = new StringBuilder(splits[0]);
        for (int i = 1; i < splits.length; i++) {
            editLog.append(LOGDATA_DELIMITER).append(splits[i]);
        }

        return editLog.toString();
    }

    private void handleFatalExceptionAndAppendLog(String[] splits, String processedLog) {
        if (isFatalException(splits)) {
            startFatalLogging(processedLog);
            return;
        }

        if (mFatalFlag) {
            processFatalLog(splits, processedLog);
        }

        mRingBuffer.append(processedLog);
    }

    private boolean isFatalException(String[] splits) {
        boolean isAndroidRuntimeFatal = splits.length > LOGDATA_TAG_INDEX_WITH_DEFAULT &&
               splits[LOGDATA_TAG_INDEX_WITH_DEFAULT].contains("AndroidRuntime") &&
               splits[LOGDATA_TAG_INDEX_WITH_DEFAULT].contains("FATAL EXCEPTION");

        boolean isSigsegvFatal = splits.length > LOGDATA_TAG_INDEX_WITH_DEFAULT &&
               splits[LOGDATA_TAG_INDEX_WITH_DEFAULT].contains("Fatal signal 11") &&
               splits[LOGDATA_TAG_INDEX_WITH_DEFAULT].contains("SIGSEGV");

        return isAndroidRuntimeFatal || isSigsegvFatal;
    }

    private void startFatalLogging(String logStr) {
        Log.i(TAG, "~~~~~~~~~~~ STARTING FATAL LOGGING ~~~~~~~~~~~~~~");
        mFatalFlag = true;
        mNonErrorLogCount = 0; // Reset counter when starting fatal logging
        mFatalRingBuffer = new RingBufferRunner();
        mRingBuffer.append(logStr);
        mFatalRingBuffer.append(logStr);
        Log.i(TAG, "Fatal logging started, non-error counter reset to 0");
    }

    private void processFatalLog(String[] splits, String logStr) {
        // Always append to fatal ring buffer first
        mFatalRingBuffer.append(logStr);

        if (splits.length <= 4) {
            // Invalid log format, continue collecting
            Log.d(TAG, "Invalid log format during fatal collection, continuing...");
            return;
        }

        String logLevel = splits[4];

        // Check if this is an Error (E) or Assert (A) log
        if (logLevel.contains("E") || logLevel.contains("A")) {
            // Reset non-error counter when we see E or A logs
            mNonErrorLogCount = 0;
            Log.d(TAG, "Error/Assert log detected during fatal collection, resetting counter");
            return;
        }

        // This is not an E or A log, increment non-error counter
        mNonErrorLogCount++;
        Log.d(TAG, "Non-error log during fatal collection, count: " + mNonErrorLogCount + "/" + MAX_NON_ERROR_LOGS);

        // If we've seen too many consecutive non-error logs, end fatal collection
        if (mNonErrorLogCount >= MAX_NON_ERROR_LOGS) {
            Log.i(TAG, "~~~~~~~~~~~ ENDING FATAL LOGGING ~~~~~~~~~~~~~~");
            Log.i(TAG, "Max non-error logs reached (" + MAX_NON_ERROR_LOGS + "), ending crash log collection");
            mFatalFlag = false;
            mNonErrorLogCount = 0; // Reset counter
            startSavingFatalFile();
        }
    }

    private void cleanup() {
        closeBufferedReader();
        killRuntimeProcess();
        mIsRunnableRunning = false;
        if (mRestartThreadListener != null) {
            mRestartThreadListener.restartThread();
        }
    }

    private String getAppInfoFromPid(String pid){
        String result = "";

        if(mPreProcessCls.getPid().equals(pid)){
            return mPreProcessCls.getResult();
        }
        List<ActivityManager.RunningAppProcessInfo> activities = mActivityManager.getRunningAppProcesses();
        for (int i = 0; i < activities.size(); i++) {
            if(activities.get(i).pid == Integer.parseInt(pid)) {
                String name = activities.get(i).processName;
                if (name == null || name.isEmpty()) {
                    Log.w(TAG, "Process name is null or empty for PID: " + pid);
                    return result;
                }
                String uuid = mGuidManager.getId(name);
                mPreProcessCls = new PreProcess(pid, name, uuid);
                result = mPreProcessCls.getResult();
                return result;
            }
        }
        return result;
    }

    private class PreProcess {
        private String pid = "";
        private String uuid = "";
        private String name = "";

        public PreProcess(String pid, String name, String uuid){
            this.pid = pid;
            this.uuid = uuid;
            this.name = name;
        }

        public String getPid(){return pid;}

        public String getUuid(){return uuid;}

        public String getResult(){
            if(GUIDManager.matchSupportApp(name) || !"".equals(uuid)){
                supportAppFlag = true;
            }
            return pid+"/"+uuid;
        }
    }

    private void killRuntimeProcess() {
        if (mLogcatProcess != null) {
            mLogcatProcess.destroy();
        }
        mLogcatProcess = null;
    }

    private void closeBufferedReader() {
        if (mReader != null) {
            try {
                mReader.close();
            } catch (IOException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
        mReader = null;
    }

    public boolean stopReaderThreadRunnable() {
        setRunningLoopState(false);
        while (isRunningReaderThread()) {
            //Log.d(TAG, "stop:Waiting to stop thread");
        }
        resetReaderThreadRunningState();
        Log.d(TAG, "Thread is stopped");
        return true;
    }

    public synchronized boolean isRunningReaderThread() {
        return mIsRunnableRunning;
    }

    public synchronized void resetReaderThreadRunningState() {
        mIsLoopRunning = false;
        mIsRunnableRunning = false;
    }

    public synchronized boolean isRunningLoop() {

        return mIsLoopRunning;
    }

    public synchronized void setRunningLoopState(boolean run) {
        mIsLoopRunning = run;
    }

    public void startSavingFatalFile(){
        new FileManager(FileManager.TAG_CRASH, mFatalRingBuffer, null, LogPathManager.getCrashDirectoryPath());
    }

    /**
     * Start saving log file and return the generated file path
     * @param uuid The UUID for log file identification
     * @return The path of the generated log file, or null if failed
     */
    public String startSavingLogFile(String uuid){
        try {
            // Use LogPathManager to get the actual file path
            String actualFilePath = LogPathManager.getGeneratedLogArchivePath(FileManager.TAG_LOG, uuid);
            Log.i(TAG, "Generated log archive path: " + actualFilePath);

            if(uuid.equals("all")) {
                new FileManager(FileManager.TAG_LOG, mAllBuffer, uuid, LogPathManager.getSolutionDirectoryPath());
            } else {
                new FileManager(FileManager.TAG_LOG, mRingBuffer, uuid, LogPathManager.getGeneratedLogDirPath(FileManager.TAG_LOG,uuid));
            }

            // Wait a bit for file processing to complete
            Thread.sleep(1000);

            return actualFilePath;

        } catch (InterruptedException e) {
            Log.e(TAG, "Thread interrupted while saving log file: " + Log.getStackTraceString(e));
            Thread.currentThread().interrupt(); // Restore interrupted status
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error saving log file: " + Log.getStackTraceString(e));
            return null;
        }
    }
}
