package com.hp.jetadvantage.link.logdaemon.util;

import android.util.Log;

import com.hp.jetadvantage.link.logdaemon.data.FileManager;

import java.io.File;

/**
 * Central file path management class for log files
 * Provides unified path management for all log-related operations
 */
public class LogPathManager {
    private static final String TAG = "[LD][LogPathManager]";

    // Path configuration constants
    private static final String SUBFOLDER_CRASH = "crash";
    private static final String SUBFILE_CRASH = "_CrashLogs";
    private static final String SUBFOLDER_SOLUTION = "solutions";
    private static final String SUBFILE_SOLUTION = "_SolutionLogs";
    private static final String DEFAULT_LOG_PATH = "/data/workpath/log";
    private static final String PLATFORM_LOG_NAME = "AndroidLogs.tar.gz";

    // File extensions
    private static final String FILE_EXTENSION_LOG = ".log";
    private static final String FILE_EXTENSION_ARCHIVE = ".tar.gz";

    /**
     * Get base path based on test mode
     * @return base path for log storage
     */
    public static String getBasePath() {
        return new File(DEFAULT_LOG_PATH).getAbsolutePath();
    }

    /**
     * Get solution log directory path
     * @return full path to solution log directory
     */
    public static String getSolutionDirectoryPath() {
        return new File(getBasePath(), SUBFOLDER_SOLUTION).getAbsolutePath();
    }

    /**
     * Get crash log directory path
     * @return full path to crash log directory
     */
    public static String getCrashDirectoryPath() {
        return new File(getBasePath(), SUBFOLDER_CRASH).getAbsolutePath();
    }

    /**
     * Get platform log file path
     * @return full path to platform log file
     */
    public static String getPlatformLogPath() {
        return new File(getBasePath(), PLATFORM_LOG_NAME).getAbsolutePath();
    }

    /**
     * Get platform log file name
     * @return File name to platform log file
     */
    public static String getPlatformLogName() {
        return PLATFORM_LOG_NAME;
    }

    /**
     * Get generated log dir path based on UUID
     * @param filter The Filter for log type identification
     * @param uuid The UUID for log file identification
     * @return The actual path where the log file is created
     */
    public static String getGeneratedLogDirPath(String filter, String uuid) {
        String uuidDirectoryPath = FileManager.TAG_CRASH.equals(filter) ?
                getCrashDirectoryPath() + File.separator + uuid :
                getSolutionDirectoryPath() + File.separator + uuid;

        if(!ensureDirectoryExists(uuidDirectoryPath)) {
            Log.e(TAG, "Failed to create UUID directory for logs: " + uuidDirectoryPath);
        }

        return uuidDirectoryPath;
    }

    /**
     * Get generated log file path based on UUID
     * @param filter The Filter for log type identification
     * @param uuid The UUID for log file identification
     * @return The actual path where the log file is created
     */
    public static String getGeneratedLogArchivePath(String filter, String uuid) {
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US)
                .format(new java.util.Date());

        String fileName = FileManager.TAG_CRASH.equals(filter) ?
                uuid + SUBFILE_CRASH + "_archive_" + timestamp + FILE_EXTENSION_ARCHIVE :
                uuid + SUBFILE_SOLUTION + FILE_EXTENSION_ARCHIVE;

        // All archives (crash and solution) are written to Shared Bind Mount (solutions/{uuid}/)
        String archiveDirPath = getGeneratedLogDirPath(FileManager.TAG_LOG, uuid);

        return archiveDirPath + File.separator + fileName;
    }

    /**
     * Get log file name with extension
     * @param filter The Filter for log type identification
     * @param uuid The UUID for log file identification
     * @return log file name with .log extension
     */
    public static String getLogFileName(String filter, String uuid) {
        return FileManager.TAG_CRASH.equals(filter) ?
                FileManager.TAG_CRASH + "_" + uuid + FILE_EXTENSION_LOG :
                FileManager.TAG_LOG + "_" + uuid + FILE_EXTENSION_LOG;
    }

    /**
     * Create directory if it doesn't exist
     * @param directoryPath The path to create
     * @return true if directory exists or was created successfully
     */
    public static boolean ensureDirectoryExists(String directoryPath) {
        File dir = new File(directoryPath);
        if (!dir.exists()) {
            return dir.mkdirs();
        }
        return true;
    }

    /**
     * Initialize all required directories
     * @return true if all directories were created successfully
     */
    public static boolean initializeDirectories() {
        boolean success = true;

        success &= ensureDirectoryExists(getBasePath());
        success &= ensureDirectoryExists(getSolutionDirectoryPath());
        success &= ensureDirectoryExists(getCrashDirectoryPath());

        if (success) {
            Log.i(TAG, "All directories initialized successfully");
        } else {
            Log.e(TAG, "Failed to initialize some directories");
        }

        return success;
    }
}
