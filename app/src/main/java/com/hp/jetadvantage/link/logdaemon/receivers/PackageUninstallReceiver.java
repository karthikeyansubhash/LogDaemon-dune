package com.hp.jetadvantage.link.logdaemon.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hp.jetadvantage.link.logdaemon.util.FileUtils;
import com.hp.jetadvantage.link.logdaemon.util.LogPathManager;
import com.hp.jetadvantage.link.logdaemon.util.UuidValidator;

import java.io.File;

/**
 * Receives PACKAGE_UNINSTALLED broadcast from PackageManager
 * and cleans up crash log and solution log directories for the uninstalled solution.
 */
public class PackageUninstallReceiver extends BroadcastReceiver {
    private static final String TAG = "[LD][PkgUninstall]";

    static final String ACTION_PACKAGE_UNINSTALLED =
            "com.hp.packagemanager.intent.action.PACKAGE_UNINSTALLED";

    static final String EXTRA_SOLUTION_UUID = "EXTRA_SOLUTION_ID";
    static final String EXTRA_PACKAGE = "EXTRA_PACKAGE";

    private static final String LOG_DESC_CRASH = "CRASH";
    private static final String LOG_DESC_SOLUTION = "SOLUTION";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !ACTION_PACKAGE_UNINSTALLED.equals(intent.getAction())) {
            return;
        }

        String solutionUuid = intent.getStringExtra(EXTRA_SOLUTION_UUID);
        String packageName = intent.getStringExtra(EXTRA_PACKAGE);

        Log.i(TAG, "Package uninstalled - package: " + packageName + ", solutionUuid: " + solutionUuid);

        if (!UuidValidator.isValidUuid(solutionUuid)) {
            Log.w(TAG, "solutionUuid is null, empty, or invalid format, skipping cleanup");
            return;
        }

        cleanupSolutionDirectories(solutionUuid);
    }

    /**
     * Delete crash log and solution log directories for the given UUID
     * @param solutionUuid The solution UUID whose directories should be deleted
     */
    void cleanupSolutionDirectories(String solutionUuid) {
        boolean crashResult = deleteCrashDirectory(solutionUuid);
        boolean solutionResult = deleteSolutionDirectory(solutionUuid);

        Log.i(TAG, "Cleanup completed for UUID: " + solutionUuid
                + " - crash: " + (crashResult ? "OK" : "FAILED")
                + ", solution: " + (solutionResult ? "OK" : "FAILED"));
    }

    private boolean deleteCrashDirectory(String solutionUuid) {
        File crashDir = new File(LogPathManager.getCrashDirectoryPath(), solutionUuid);
        if (!crashDir.exists()) {
            Log.d(TAG, "Crash directory does not exist, nothing to delete: " + crashDir.getAbsolutePath());
            return true;
        }
        return FileUtils.deleteDirectoryRecursively(crashDir, LOG_DESC_CRASH);
    }

    private boolean deleteSolutionDirectory(String solutionUuid) {
        File solutionDir = new File(LogPathManager.getSolutionDirectoryPath(), solutionUuid);
        if (!solutionDir.exists()) {
            Log.d(TAG, "Solution directory does not exist, nothing to delete: " + solutionDir.getAbsolutePath());
            return true;
        }
        return FileUtils.deleteDirectoryRecursively(solutionDir, LOG_DESC_SOLUTION);
    }
}
