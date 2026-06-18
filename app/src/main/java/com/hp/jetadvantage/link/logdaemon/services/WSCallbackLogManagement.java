package com.hp.jetadvantage.link.logdaemon.services;

import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

import com.hp.jetadvantage.link.logdaemon.data.FileManager;
import com.hp.jetadvantage.link.logdaemon.util.AndroidSystemCall;
import com.hp.jetadvantage.link.logdaemon.util.FileUtils;
import com.hp.jetadvantage.link.logdaemon.util.JsonParser;
import com.hp.jetadvantage.link.logdaemon.util.LogPathManager;
import com.hp.jetadvantage.link.logdaemon.util.UuidValidator;
import com.hp.jetadvantage.link.logdaemon.model.LogManagementRequestMessage;
import com.hp.jetadvantage.link.logdaemon.model.LogManagementResponseMessage;

import java.io.File;

public class WSCallbackLogManagement extends BaseWebsocketCallbackService {
    private static final String TAG = "[LD][WSCallbackLogManagement]";

    // Store parsed data as strings
    private String processType = "";
    private static final String PROCESS_TYPE_EXPORT = "export";
    private static final String PROCESS_TYPE_CLEANUP = "cleanup";
    private String lastLogType = "";
    private String lastSolutionId = "";
    private String lastOperationCallId = "";

    // Static instance management for testing
    private static WSCallbackLogManagement currentInstance;

    // Timeout and interval constants
    public static final int PLATFORM_LOG_TIMEOUT_SEC = 90;
    public static final int SOLUTION_LOG_TIMEOUT_SEC = 10;
    public static final int CHECK_INTERVAL_MS = 1000;

    public WSCallbackLogManagement() {
        super("logManagement");
    }

    @Override
    public void onReceived(int what, String data) {
        Log.i(TAG, "onMessageReceived : " + data);

        // Parse JSON data using the created JsonParser
        LogManagementRequestMessage message = JsonParser.parseLogManagementMessage(data);

        if (message == null || message.getLogManagement() == null) {
            handleInvalidMessage();
            return;
        }

        processValidMessage(message);
    }

    /**
     * Handle invalid or unparseable message
     */
    private void handleInvalidMessage() {
        Log.e(TAG, "Failed to parse JSON data");
        processType = "";
        lastLogType = "";
        lastSolutionId = "";
        lastOperationCallId = "";
    }

    /**
     * Process valid LogManagementRequestMessage
     * @param message The validated message to process
     */
    private void processValidMessage(LogManagementRequestMessage message) {
        // Extract and store data from message
        extractMessageData(message);

        Log.i(TAG, "Parsed ProcessType: " + processType);
        Log.i(TAG, "Parsed LogType: " + lastLogType);
        Log.i(TAG, "Parsed SolutionId: " + lastSolutionId);
        Log.i(TAG, "Parsed OperationCallId: " + lastOperationCallId);

        if (processType.isEmpty()) {
            return;
        }

        if (PROCESS_TYPE_EXPORT.equals(processType)) {
            // Send in-progress response first
            sendInProgressResponse(lastLogType, lastOperationCallId);

            // Handle different logType cases with new schema
            handleLogTypeAction(lastLogType, lastSolutionId, lastOperationCallId);
        } else if (PROCESS_TYPE_CLEANUP.equals(processType)) {
            Log.i(TAG, "Processing cleanup operation for logType: " + lastLogType);

            // Handle cleanup based on logType
            handleCleanupOperation(lastLogType, lastSolutionId);
        }
    }

    /**
     * Extract and store data from LogManagementRequestMessage
     * @param message The message to extract data from
     */
    private void extractMessageData(LogManagementRequestMessage message) {
        LogManagementRequestMessage.LogManagement logManagement = message.getLogManagement();
        initializeFields();

        if (logManagement.getDetails() == null) {
            return;
        }

        LogManagementRequestMessage.LogOperation export = logManagement.getDetails().getExport();
        LogManagementRequestMessage.LogOperation cleanup = logManagement.getDetails().getCleanup();

        if (export != null) {
            extractExportData(export);
        } else if (cleanup != null) {
            extractCleanupData(cleanup);
        }
    }

    /**
     * Initialize all fields to empty strings
     */
    private void initializeFields() {
        processType = "";
        lastLogType = "";
        lastSolutionId = "";
        lastOperationCallId = "";
    }

    /**
     * Extract data from export operation
     */
    private void extractExportData(LogManagementRequestMessage.LogOperation export) {
        processType = PROCESS_TYPE_EXPORT;
        extractOperationFields(export);
    }

    /**
     * Extract data from cleanup operation
     */
    private void extractCleanupData(LogManagementRequestMessage.LogOperation cleanup) {
        processType = PROCESS_TYPE_CLEANUP;
        extractOperationFields(cleanup);
    }

    /**
     * Extract common operation fields with null safety
     */
    private void extractOperationFields(LogManagementRequestMessage.LogOperation operation) {
        lastLogType = safeGetString(operation.getLogType());
        lastSolutionId = safeGetString(operation.getSolutionId());
        lastOperationCallId = safeGetString(operation.getOperationCallId());
    }

    /**
     * Safely extract string value, returning empty string if null
     */
    private String safeGetString(String value) {
        return value != null ? value : "";
    }

    /**
     * Send response message back to websocket using AIDL sendMessage
     * @param responseMessage The response message to send
     */
    private void sendResponse(LogManagementResponseMessage responseMessage) {
        try {
            String responseJson = JsonParser.toJson(responseMessage);
            if (responseJson != null) {
                sendWebSocketMessage(responseJson);
                Log.i(TAG, "Response sent");
            } else {
                Log.e(TAG, "Failed to convert response to JSON");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sending response: " + Log.getStackTraceString(e));
        }
    }

    /**
     * Send in-progress response
     * @param logType The log type being processed
     */
    private void sendInProgressResponse(String logType, String operationCallId) {
        LogManagementResponseMessage response = JsonParser.createInProgressResponse(logType, operationCallId);
        sendResponse(response);
    }

    /**
     * Send completed response
     * @param logType The log type that was processed
     * @param filePath The path to the exported log file
     * @param operationCallId The operation call ID from the request
     */
    private void sendCompletedResponse(String logType, String filePath, String operationCallId) {
        LogManagementResponseMessage response = JsonParser.createCompletedResponse(logType, filePath, operationCallId, lastSolutionId);
        sendResponse(response);
    }

    /**
     * Send failed response
     * @param logType The log type that failed
     * @param errorMessage The error message
     * @param operationCallId The operation call ID from the request
     */
    private void sendFailedResponse(String logType, String errorMessage, String operationCallId) {
        LogManagementResponseMessage response = JsonParser.createFailedResponse(logType, errorMessage, operationCallId);
        sendResponse(response);
    }

    /**
     * Send cleanup completed response
     * @param logType The log type that was processed
     * @param operationCallId The operation call ID from the request
     */
    private void sendCleanupCompletedResponse(String logType, String operationCallId) {
        LogManagementResponseMessage response = JsonParser.createCleanupCompletedResponse(logType, operationCallId);
        sendResponse(response);
    }

    /**
     * Send cleanup failed response
     * @param logType The log type that failed
     * @param errorMessage The error message
     * @param operationCallId The operation call ID from the request
     */
    private void sendCleanupFailedResponse(String logType, String errorMessage, String operationCallId) {
        LogManagementResponseMessage response = JsonParser.createCleanupFailedResponse(logType, errorMessage, operationCallId);
        sendResponse(response);
    }

    /**
     * Send message through websocket using AIDL IWebsocketCallbackService.sendMessage
     * @param message The JSON message to send
     */
    private void sendWebSocketMessage(String message) {
        try {
            if (callbackService != null) {
                // Use AIDL sendMessage method
                // what: 0 (can be used for message type identification if needed)
                // data: JSON response message
                callbackService.sendMessage(0, message);
                Log.d(TAG, "WebSocket message sent via AIDL");
            } else {
                Log.e(TAG, "CallbackService is null, cannot send WebSocket message");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException when sending WebSocket message: " + Log.getStackTraceString(e));
        } catch (Exception e) {
            Log.e(TAG, "Exception when sending WebSocket message: " + Log.getStackTraceString(e));
        }
    }

    /**
     * Convert absolute file path to relative path by removing DEFAULT_LOG_PATH prefix
     * @param absolutePath The absolute file path
     * @return Relative path with DEFAULT_LOG_PATH prefix removed
     */
    private String convertToReturnPath(String absolutePath) {
        if (absolutePath == null || absolutePath.isEmpty()) {
            return absolutePath;
        }

        String defaultLogPath = LogPathManager.getBasePath();
        String relativePath = absolutePath;

        if (absolutePath.startsWith(defaultLogPath)) {
            relativePath = absolutePath.substring(defaultLogPath.length());
        }

        return relativePath;
    }

    /**
     * Handle different actions based on logType
     * @param logType The type of log (ltAll, ltPlatform, ltSolutionDebug, ltSolutionCrash)
     * @param solutionId The solution ID (for solution-specific logs)
     */
    private void handleLogTypeAction(String logType, String solutionId, String operationCallId) {
        switch (logType) {
            case LogManagementRequestMessage.LOG_TYPE_ALL:
                handleLogTypeAll(logType);
                break;
            case LogManagementRequestMessage.LOG_TYPE_PLATFORM:
                cleanupPlatformFiles();
                handleLogTypePlatform(logType);
                break;
            case LogManagementRequestMessage.LOG_TYPE_SOLUTION_DEBUG:
                handleLogTypeSolutionDebug(logType, solutionId);
                break;
            case LogManagementRequestMessage.LOG_TYPE_SOLUTION_CRASH:
                handleLogTypeSolutionCrash(logType, solutionId);
                break;
            case LogManagementRequestMessage.LOG_TYPE_DUMMY_CRASH:
                handleLogTypeDummyCrash(logType, solutionId);
                break;
            default:
                Log.w(TAG, "Unknown logType: " + logType);
                sendFailedResponse(logType, "Unknown log type: " + logType, operationCallId);
                break;
        }
    }

    /**
     * Handle ltAll logType - export all logs
     * @param logType The log type for response
     */
    private void handleLogTypeAll(String logType) {
        Log.i(TAG, "Handling ltAll");

        try {
            // Direct method call for all logs and get actual file path
            String actualFilePath = LogcatService.handleLogManagementRequest("all");

            if (actualFilePath != null && !actualFilePath.isEmpty()) {
                sendCompletedResponse(logType, actualFilePath, lastOperationCallId);
                Log.i(TAG, "Direct method call completed for ltAll with file: " + actualFilePath);
            } else {
                sendFailedResponse(logType, "Failed to generate all logs file", lastOperationCallId);
                Log.e(TAG, "No file path returned for ltAll");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling ltAll: " + Log.getStackTraceString(e));
            sendFailedResponse(logType, "Failed to export all logs: " + Log.getStackTraceString(e), lastOperationCallId);
        }
    }

    /**
     * Handle ltPlatform logType - export platform logs only
     * @param logType The log type for response
     */
    private void handleLogTypePlatform(String logType) {
        Log.i(TAG, "Handling ltPlatform");

        try {
            // Use LogPathManager for platform log path
            String actualFilePath = LogPathManager.getPlatformLogPath();
            String fileName = LogPathManager.getPlatformLogName();

            // Check if current device is simulator and handle accordingly
            if (LogcatService.isCurrentInstanceSimulator()) {
                Log.i(TAG, "Detected simulator environment - using logcat log handling");
                handleSimulatorPlatformLogs(actualFilePath);
            } else {
                Log.i(TAG, "Detected real device/emulator environment - using platform log handling");
                AndroidSystemCall.checkStatusForSetPropertiesSettings();
            }

            // Wait for file creation with timeout (90 seconds, check every 1 second)
            if (waitForFileCreation(actualFilePath, PLATFORM_LOG_TIMEOUT_SEC, CHECK_INTERVAL_MS)) {
                File platformLogFile = new File(actualFilePath);
                Log.i(TAG, "Platform log file verified: " + actualFilePath + " (size: " + platformLogFile.length() + " bytes)");
                sendCompletedResponse(logType, fileName, lastOperationCallId);
                Log.i(TAG, "Direct method call completed for ltPlatform with file: " + actualFilePath);
            } else {
                String errorMsg = "Platform log file not found or empty after "
                        + PLATFORM_LOG_TIMEOUT_SEC + " seconds: " + actualFilePath;
                Log.e(TAG, errorMsg);
                sendFailedResponse(logType, errorMsg, lastOperationCallId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling ltPlatform: " + Log.getStackTraceString(e));
            sendFailedResponse(logType, "Failed to export platform logs: " + Log.getStackTraceString(e), lastOperationCallId);
        }
    }

    /**
     * Handle simulator platform logs - specific handling for simulator environment
     * @param actualFilePath The actual file path for the logs
     */
    private void handleSimulatorPlatformLogs(String actualFilePath) {
        try {
            // Use logcat to capture logs for simulator
            String tempFilePath = LogcatService.handleLogManagementRequest("all");

            // Move generated file from temp location to platform log location
            moveFileFromTempToTarget(tempFilePath, actualFilePath);
        } catch (Exception e) {
            Log.e(TAG, "Error handling simulator platform logs: " + Log.getStackTraceString(e));
        }
    }

    /**
     * Move file from temporary location to target location
     * @param tempFilePath The temporary file path
     * @param actualFilePath The target file path
     */
    private void moveFileFromTempToTarget(String tempFilePath, String actualFilePath) {
        if (tempFilePath != null && !tempFilePath.isEmpty()) {
            File tempFile = new File(tempFilePath);
            File targetFile = new File(actualFilePath);

            if (tempFile.exists()) {
                try {
                    // Move file from temp to target location
                    if (FileUtils.moveFile(tempFile, targetFile)) {
                        Log.i(TAG, "Successfully moved simulator log file from " + tempFilePath + " to " + actualFilePath);
                    } else {
                        Log.e(TAG, "Failed to move simulator log file using FileUtils");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to move simulator log file: " + Log.getStackTraceString(e));
                }
            } else {
                Log.w(TAG, "Temporary log file does not exist: " + tempFilePath);
            }
        } else {
            Log.w(TAG, "No temporary file path returned for simulator");
        }
    }

    /**
     * Wait for file creation with timeout and periodic checks
     * @param filePath The path of the file to wait for
     * @param timeoutSeconds Maximum time to wait in seconds
     * @param checkIntervalMs Interval between checks in milliseconds
     * @return true if file exists and is not empty, false if timeout or file not found
     */
    private boolean waitForFileCreation(String filePath, int timeoutSeconds, int checkIntervalMs) {
        Log.i(TAG, "Waiting for file creation: " + filePath + " (timeout: " + timeoutSeconds + "s, interval: " + checkIntervalMs + "ms)");

        long startTime = System.currentTimeMillis();
        long timeoutMs = timeoutSeconds * 1000L;
        int attemptCount = 0;

        // Log initial directory contents
        logDirectoryContents(filePath, "Initial");

        while ((System.currentTimeMillis() - startTime) < timeoutMs) {
            attemptCount++;
            File file = new File(filePath);

            if (file.exists() && file.length() > 0) {
                Log.i(TAG, "File found after " + attemptCount + " attempts: " + filePath + " (size: " + file.length() + " bytes)");
                return true;
            }

            if (attemptCount % 10 == 0) { // Log every 5 attempts to avoid spam
                Log.d(TAG, "File check attempt " + attemptCount + ": " + filePath + " not found or empty");
                // Log directory contents every 5 attempts
                logDirectoryContents(filePath, "Attempt " + attemptCount);
            }

            try {
                Thread.sleep(checkIntervalMs);
            } catch (InterruptedException e) {
                Log.w(TAG, "File wait interrupted: " + Log.getStackTraceString(e));
                Thread.currentThread().interrupt(); // Restore interrupted status
                return false;
            }
        }

        // Log final directory contents before timeout
        logDirectoryContents(filePath, "Final (timeout)");

        Log.w(TAG, "File creation timeout after " + attemptCount + " attempts (" + timeoutSeconds + "s): " + filePath);
        return false;
    }

    /**
     * Log the contents of the directory containing the specified file path
     * @param filePath The file path to check directory contents for
     * @param context Context string to identify when this logging occurred
     */
    private void logDirectoryContents(String filePath, String context) {
        try {
            File file = new File(filePath);
            File parentDir = file.getParentFile();

            if (parentDir == null) {
                Log.w(TAG, context + " - No parent directory for path: " + filePath);
                return;
            }

            if (!parentDir.exists()) {
                Log.w(TAG, context + " - Parent directory does not exist: " + parentDir.getAbsolutePath());
                return;
            }

            if (!parentDir.isDirectory()) {
                Log.w(TAG, context + " - Parent path is not a directory: " + parentDir.getAbsolutePath());
                return;
            }

            File[] files = parentDir.listFiles();
            if (files == null) {
                Log.w(TAG, context + " - Cannot list files in directory: " + parentDir.getAbsolutePath());
                return;
            }

            Log.i(TAG, context + " - Directory contents for: " + parentDir.getAbsolutePath() + " (Total files: " + files.length + ")");

            if (files.length == 0) {
                Log.i(TAG, context + " - Directory is empty");
            } else {
                for (int i = 0; i < files.length; i++) {
                    File currentFile = files[i];
                    String fileType = currentFile.isDirectory() ? "DIR" : "FILE";
                    String sizeInfo = currentFile.isFile() ? " (size: " + currentFile.length() + " bytes)" : "";
                    String lastModified = " (modified: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(new java.util.Date(currentFile.lastModified())) + ")";

                    Log.i(TAG, context + " - [" + (i + 1) + "] " + fileType + ": " + currentFile.getName() + sizeInfo + lastModified);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, context + " - Error listing directory contents for " + filePath + ": " + Log.getStackTraceString(e));
        }
    }

    /**
     * Handle ltSolutionDebug logType - export solution debug logs
     * @param logType The log type for response
     * @param solutionId The solution ID
     */
    private void handleLogTypeSolutionDebug(String logType, String solutionId) {
        Log.i(TAG, "Handling ltSolutionDebug with solutionId: " + solutionId);

        try {
            // Use solutionId as the identifier for solution-specific logs
            String identifier = solutionId != null && !solutionId.isEmpty() ? solutionId : "unknown";
            String actualFilePath = LogcatService.handleLogManagementRequest(identifier);

            if (actualFilePath != null && !actualFilePath.isEmpty()) {
                if (waitForFileCreation(actualFilePath, SOLUTION_LOG_TIMEOUT_SEC, CHECK_INTERVAL_MS)) {
                    File platformLogFile = new File(actualFilePath);
                    Log.i(TAG, "Solution log file verified: " + actualFilePath + " (size: " + platformLogFile.length() + " bytes)");
                    
                    sendCompletedResponse(logType, convertToReturnPath(actualFilePath), lastOperationCallId);
                    Log.i(TAG, "Direct method call completed for ltSolutionDebug with file: " + actualFilePath);
                } else {
                    String errorMsg = "Solution log file not found or empty after "
                            + SOLUTION_LOG_TIMEOUT_SEC + " seconds: " + actualFilePath;
                    Log.e(TAG, errorMsg);
                    sendFailedResponse(logType, errorMsg, lastOperationCallId);
                }
            } else {
                sendFailedResponse(logType, "Failed to generate solution debug logs file path", lastOperationCallId);
                Log.e(TAG, "No file path returned for ltSolutionDebug");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling ltSolutionDebug: " + Log.getStackTraceString(e));
            sendFailedResponse(logType, "Failed to export solution debug logs: " + Log.getStackTraceString(e), lastOperationCallId);
        }
    }

    /**
     * Handle ltSolutionCrash logType - export solution crash logs
     * @param logType The log type for response
     * @param solutionId The solution ID
     */
    private void handleLogTypeSolutionCrash(String logType, String solutionId) {
        Log.i(TAG, "Handling ltSolutionCrash with solutionId: " + solutionId);

        try {
            // Use solutionId as the identifier for solution-specific logs
            String identifier = solutionId != null && !solutionId.isEmpty() ? solutionId : "unknown";

            // For crash logs, we need to collect existing crash files and create archive
            String actualFilePath = collectAndCreateCrashArchive(identifier);

            if (actualFilePath == null) {
                // null means an actual error occurred
                sendFailedResponse(logType, "Failed to generate solution crash logs archive", lastOperationCallId);
                Log.e(TAG, "Error creating crash archive for ltSolutionCrash");
            } else if (actualFilePath.isEmpty()) {
                // Empty string means no crash log files exist — return esCompleted with empty path
                sendCompletedResponse(logType, "", lastOperationCallId);
                Log.i(TAG, "No crash log files found for ltSolutionCrash, returning esCompleted with empty path");
            } else {
                sendCompletedResponse(logType, convertToReturnPath(actualFilePath), lastOperationCallId);
                Log.d(TAG, "Crash archive created successfully for ltSolutionCrash with file: " + actualFilePath);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling ltSolutionCrash: " + Log.getStackTraceString(e));
            sendFailedResponse(logType, "Failed to export solution crash logs: " + Log.getStackTraceString(e), lastOperationCallId);
        }
    }

    /**
     * Handle ltDummyCrash logType - generate a dummy crash log file and archive it
     * Creates a new dummy crash file each time, then reuses the ltSolutionCrash archiving flow.
     * @param logType The log type for response
     * @param solutionId The solution ID
     */
    private void handleLogTypeDummyCrash(String logType, String solutionId) {
        Log.i(TAG, "Handling ltDummyCrash with solutionId: " + solutionId);

        try {
            String identifier = solutionId != null && !solutionId.isEmpty() ? solutionId : "unknown";

            // Step 1: Create a dummy crash log file
            if (!createDummyCrashFile(identifier)) {
                sendFailedResponse(logType, "Failed to create dummy crash log file", lastOperationCallId);
                return;
            }

            // Step 2: Reuse ltSolutionCrash archiving flow
            String actualFilePath = collectAndCreateCrashArchive(identifier);

            if (actualFilePath == null) {
                sendFailedResponse(logType, "Failed to generate dummy crash logs archive", lastOperationCallId);
                Log.e(TAG, "Error creating crash archive for ltDummyCrash");
            } else if (actualFilePath.isEmpty()) {
                sendFailedResponse(logType, "No crash log files found after dummy file creation", lastOperationCallId);
                Log.e(TAG, "No crash files found for ltDummyCrash despite creating dummy file");
            } else {
                sendCompletedResponse(logType, convertToReturnPath(actualFilePath), lastOperationCallId);
                Log.d(TAG, "Dummy crash archive created successfully: " + actualFilePath);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling ltDummyCrash: " + Log.getStackTraceString(e));
            sendFailedResponse(logType, "Failed to export dummy crash logs: " + Log.getStackTraceString(e), lastOperationCallId);
        }
    }

    /**
     * Create a dummy crash log file for testing purposes
     * @param solutionId The solution ID to create the dummy crash file for
     * @return true if the dummy crash file was created successfully
     */
    private boolean createDummyCrashFile(String solutionId) {
        File tempFile = null;
        try {
            String crashDirPath = LogPathManager.getGeneratedLogDirPath(FileManager.TAG_CRASH, solutionId);
            File crashDir = new File(crashDirPath);

            // Create a temporary file with dummy crash content
            tempFile = File.createTempFile("dummy_crash_", ".log", crashDir);
            String dummyContent = generateDummyCrashContent(solutionId);

            try (java.io.FileWriter writer = new java.io.FileWriter(tempFile)) {
                writer.write(dummyContent);
            }

            Log.i(TAG, "Dummy crash temp file created: " + tempFile.getAbsolutePath());

            // Use FileManager.addCrashLogFile to apply FIFO rotation and proper naming
            File result = FileManager.addCrashLogFile(tempFile, solutionId);
            if (result != null) {
                Log.i(TAG, "Dummy crash file added successfully: " + result.getAbsolutePath());
                return true;
            }

            Log.e(TAG, "Failed to add dummy crash file via FileManager");
            return false;

        } catch (Exception e) {
            Log.e(TAG, "Error creating dummy crash file: " + Log.getStackTraceString(e));
            return false;
        } finally {
            // Clean up temp file if it still exists (addCrashLogFile moves it)
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * Generate dummy crash log content that resembles real crash logs
     * @param solutionId The solution ID
     * @return The dummy crash log content string
     */
    private String generateDummyCrashContent(String solutionId) {
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.US)
                .format(new java.util.Date());

        return "*** DUMMY CRASH LOG FOR TESTING ***\n" +
                "Timestamp: " + timestamp + "\n" +
                "SolutionId: " + solutionId + "\n" +
                "Process: com.hp.workpath.testsolution\n" +
                "PID: 12345\n" +
                "Thread: main\n\n" +
                "java.lang.RuntimeException: DUMMY TEST CRASH\n" +
                "    at com.hp.workpath.testsolution.MainActivity.onCreate(MainActivity.java:42)\n" +
                "    at android.app.Activity.performCreate(Activity.java:8000)\n" +
                "    at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1309)\n" +
                "    at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:3422)\n" +
                "    at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:3601)\n" +
                "    at android.app.ActivityThread.main(ActivityThread.java:7500)\n" +
                "    at java.lang.reflect.Method.invoke(Native Method)\n" +
                "    at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:548)\n" +
                "    at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:940)\n" +
                "Caused by: java.lang.NullPointerException: Dummy null pointer for testing\n" +
                "    at com.hp.workpath.testsolution.CrashGenerator.generate(CrashGenerator.java:15)\n" +
                "    at com.hp.workpath.testsolution.MainActivity.onCreate(MainActivity.java:40)\n" +
                "    ... 7 more\n" +
                "\n*** END OF DUMMY CRASH LOG ***\n";
    }

    /**
     * Collect existing crash log files for the specified solution and create archive
     * @param solutionId The solution ID to search crash logs for
     * @return The path of the created crash archive, empty string if no crash files found, or null on error
     */
    private String collectAndCreateCrashArchive(String solutionId) {
        try {
            File crashDir = ensureCrashDirectoryExists(solutionId);
            if (crashDir == null) {
                return null;
            }

            java.util.List<File> crashFiles = findCrashFilesForSolution(crashDir, solutionId);
            if (crashFiles.isEmpty()) {
                Log.i(TAG, "No crash files found for solution: " + solutionId);
                return "";
            }

            java.util.List<File> limitedFiles = limitCrashFiles(crashFiles);
            Log.i(TAG, "Collecting " + limitedFiles.size() + " crash files for solution: " + solutionId);

            return createArchiveFromFiles(limitedFiles, solutionId);

        } catch (Exception e) {
            Log.e(TAG, "Error collecting crash files: " + Log.getStackTraceString(e));
            return null;
        }
    }

    /**
     * Ensure crash directory exists and is valid
     * @param solutionId The solution ID to search crash logs for
     * @return The crash directory if valid, null otherwise
     */
    private File ensureCrashDirectoryExists(String solutionId) {
        String crashDirPath = LogPathManager.getGeneratedLogDirPath(FileManager.TAG_CRASH, solutionId);
        File crashDir = new File(crashDirPath);

        if (!crashDir.exists() || !crashDir.isDirectory()) {
            Log.e(TAG, "Crash directory is invalid after creation attempt: " + crashDirPath);
            return null;
        }

        return crashDir;
    }

    /**
     * Find crash files for the specified solution
     * @param crashDir The crash directory to search in
     * @param solutionId The solution ID to search for
     * @return List of crash files, or empty list if none found or error occurred
     */
    private java.util.List<File> findCrashFilesForSolution(File crashDir, String solutionId) {
        File[] allFiles = crashDir.listFiles();
        if (allFiles == null) {
            Log.w(TAG, "Cannot list files in crash directory: " + crashDir.getAbsolutePath());
            return new java.util.ArrayList<>();
        }

        java.util.List<File> crashFiles = filterCrashFiles(allFiles, solutionId);

        if (crashFiles.isEmpty()) {
            logNoCrashFilesFound(solutionId, crashDir, allFiles);
        }

        return crashFiles;
    }

    /**
     * Filter files to find crash files for the solution
     * @param allFiles All files in the directory
     * @param solutionId The solution ID to filter for
     * @return List of matching crash files
     */
    private java.util.List<File> filterCrashFiles(File[] allFiles, String solutionId) {
        java.util.List<File> crashFiles = new java.util.ArrayList<>();
        String searchPattern = FileManager.TAG_CRASH + "_" + solutionId;

        for (File file : allFiles) {
            if (file.isFile() && file.getName().startsWith(searchPattern) && file.getName().endsWith(".log")) {
                crashFiles.add(file);
                Log.d(TAG, "Found crash file: " + file.getName());
            }
        }

        return crashFiles;
    }

    /**
     * Log information when no crash files are found
     * @param solutionId The solution ID searched for
     * @param crashDir The crash directory
     * @param allFiles All files in the directory
     */
    private void logNoCrashFilesFound(String solutionId, File crashDir, File[] allFiles) {
        Log.w(TAG, "No crash files found for solution ID: " + solutionId + " in directory: " + crashDir.getAbsolutePath());

        Log.d(TAG, "Directory contents:");
        for (File file : allFiles) {
            Log.d(TAG, "  - " + file.getName() + (file.isDirectory() ? " (DIR)" : " (FILE, " + file.length() + " bytes)"));
        }
    }

    /**
     * Limit crash files to most recent 10
     * @param crashFiles List of crash files to limit
     * @return Limited list of crash files
     */
    private java.util.List<File> limitCrashFiles(java.util.List<File> crashFiles) {
        crashFiles.sort((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

        if (crashFiles.size() > 10) {
            java.util.List<File> limitedFiles = crashFiles.subList(0, 10);
            Log.i(TAG, "Limited crash files to most recent 10 out of " + crashFiles.size() + " total files");
            return limitedFiles;
        }

        return crashFiles;
    }

    /**
     * Create archive from the list of crash files
     * @param crashFiles List of crash files to archive
     * @param solutionId The solution ID
     * @return Path to created archive, or null if failed
     */
    private String createArchiveFromFiles(java.util.List<File> crashFiles, String solutionId) {
        String archiveFilePath = LogPathManager.getGeneratedLogArchivePath(FileManager.TAG_CRASH, solutionId);
        File archiveFile = new File(archiveFilePath);

        if (createCrashLogArchive(crashFiles, archiveFile)) {
            Log.d(TAG, "Crash archive created successfully: " + archiveFile.getAbsolutePath());
            
            return archiveFile.getAbsolutePath();
        } else {
            Log.e(TAG, "Failed to create crash archive");
            return null;
        }
    }

    /**
     * Create compressed archive from crash log files
     * @param crashFiles List of crash files to archive
     * @param archiveFile Output archive file
     * @return true if successful, false otherwise
     */
    private boolean createCrashLogArchive(java.util.List<File> crashFiles, File archiveFile) {
        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(
                java.nio.file.Files.newOutputStream(archiveFile.toPath()))) {

            zos.setLevel(9); // Maximum compression

            for (File crashFile : crashFiles) {
                if (!addCrashFileToArchive(crashFile, zos)) {
                    Log.e(TAG, "Failed to add crash file to archive: " + crashFile.getName());
                    return false;
                }
            }

            zos.finish();
            Log.i(TAG, "Crash archive completed with " + crashFiles.size() + " files");
            return true;

        } catch (java.io.IOException e) {
            Log.e(TAG, "IOException during crash archive creation: " + Log.getStackTraceString(e));
            // Clean up failed archive
            if (archiveFile.exists()) {
                try {
                    java.nio.file.Files.delete(archiveFile.toPath());
                } catch (Exception ex) {
                    Log.w(TAG, "Failed to clean up partial crash archive: " + ex.getMessage());
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error during crash archive creation: " + Log.getStackTraceString(e));
            return false;
        }
    }

    /**
     * Add a single crash file to the archive
     * @param crashFile The crash file to add
     * @param zos ZipOutputStream to write to
     * @return true if file was added successfully
     */
    private boolean addCrashFileToArchive(File crashFile, java.util.zip.ZipOutputStream zos) {
        try {
            if (!crashFile.exists() || !crashFile.canRead()) {
                Log.w(TAG, "Cannot read crash file: " + crashFile.getAbsolutePath());
                return false;
            }

            if (crashFile.length() == 0) {
                Log.w(TAG, "Crash file is empty, skipping: " + crashFile.getName());
                return true; // Skip empty files but don't fail
            }

            Log.d(TAG, "Adding crash file to archive: " + crashFile.getName());

            // Create zip entry
            java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(crashFile.getName());
            entry.setTime(crashFile.lastModified());
            zos.putNextEntry(entry);

            // Copy file content
            try (java.io.FileInputStream fis = new java.io.FileInputStream(crashFile);
                 java.io.BufferedInputStream bis = new java.io.BufferedInputStream(fis)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytesRead = 0;

                while ((bytesRead = bis.read(buffer)) != -1) {
                    zos.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }

                Log.d(TAG, "Added " + totalBytesRead + " bytes from crash file: " + crashFile.getName());
            }

            zos.closeEntry();
            return true;

        } catch (java.io.IOException e) {
            Log.e(TAG, "IO error adding crash file to archive: " + crashFile.getName() + " - " + e.getMessage());
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error adding crash file to archive: " + crashFile.getName() + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * Handle cleanup operations based on logType
     * @param logType The type of log to clean up
     * @param solutionId The solution ID (for solution-specific cleanup)
     */
    private void handleCleanupOperation(String logType, String solutionId) {
        Log.i(TAG, "Starting cleanup operation for logType: " + logType);

        try {
            switch (logType) {
                case LogManagementRequestMessage.LOG_TYPE_ALL:
                    handleCleanupAll();
                    break;
                case LogManagementRequestMessage.LOG_TYPE_PLATFORM:
                    handleCleanupPlatform();
                    break;
                case LogManagementRequestMessage.LOG_TYPE_SOLUTION_DEBUG:
                    handleCleanupSolutionDebug(solutionId);
                    break;
                case LogManagementRequestMessage.LOG_TYPE_SOLUTION_CRASH:
                    handleCleanupSolutionCrash(solutionId);
                    break;
                default:
                    Log.w(TAG, "Unknown cleanup logType: " + logType);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in cleanup operation: " + Log.getStackTraceString(e));
        }
    }

    /**
     * Handle cleanup for all log types
     */
    private void handleCleanupAll() {
        Log.i(TAG, "Cleaning up all log files");

        // Clean platform logs
        boolean platformResult = cleanupPlatformFiles();
        Log.i(TAG, "Platform cleanup result: " + (platformResult ? "SUCCESS" : "FAILED"));

        // Clean solution debug logs (all solutions)
        boolean debugResult = cleanupSolutionDebugFiles(null);
        Log.i(TAG, "Solution debug cleanup result: " + (debugResult ? "SUCCESS" : "FAILED"));

        // Clean solution crash logs (all solutions)
        boolean crashResult = cleanupSolutionCrashFiles(null);
        Log.i(TAG, "Solution crash cleanup result: " + (crashResult ? "SUCCESS" : "FAILED"));

        Log.i(TAG, "All logs cleanup completed");
    }

    /**
     * Handle cleanup for platform logs only
     */
    private void handleCleanupPlatform() {
        Log.i(TAG, "Cleaning up platform log files");

        boolean success = cleanupPlatformFiles();

        if (success) {
            Log.i(TAG, "Platform logs cleanup completed successfully");
            sendCleanupCompletedResponse(LogManagementRequestMessage.LOG_TYPE_PLATFORM, lastOperationCallId);
        } else {
            Log.i(TAG, "Platform logs cleanup failed");
            sendCleanupFailedResponse(LogManagementRequestMessage.LOG_TYPE_PLATFORM, "Failed to cleanup platform logs", lastOperationCallId);
        }
    }

    /**
     * Handle cleanup for solution debug logs
     * @param solutionId The solution ID (null for all solutions)
     */
    private void handleCleanupSolutionDebug(String solutionId) {
        Log.i(TAG, "Cleaning up solution debug logs" + (solutionId != null ? " for solution: " + solutionId : " for all solutions"));

        boolean success = cleanupSolutionDebugFiles(solutionId);

        if (success) {
            Log.i(TAG, "Solution debug logs cleanup completed successfully");
            sendCleanupCompletedResponse(LogManagementRequestMessage.LOG_TYPE_SOLUTION_DEBUG, lastOperationCallId);
        } else {
            Log.i(TAG, "Solution debug logs cleanup failed");
            sendCleanupFailedResponse(LogManagementRequestMessage.LOG_TYPE_SOLUTION_DEBUG, "Failed to cleanup solution debug logs", lastOperationCallId);
        }
    }

    /**
     * Handle cleanup for solution crash logs
     * @param solutionId The solution ID (null for all solutions)
     */
    private void handleCleanupSolutionCrash(String solutionId) {
        Log.i(TAG, "Cleaning up solution crash logs" + (solutionId != null ? " for solution: " + solutionId : " for all solutions"));

        boolean success = cleanupSolutionCrashFiles(solutionId);

        if (success) {
            Log.i(TAG, "Solution crash logs cleanup completed successfully");
            sendCleanupCompletedResponse(LogManagementRequestMessage.LOG_TYPE_SOLUTION_CRASH, lastOperationCallId);
        } else {
            Log.i(TAG, "Solution crash logs cleanup failed");
            sendCleanupFailedResponse(LogManagementRequestMessage.LOG_TYPE_SOLUTION_CRASH, "Failed to cleanup solution crash logs", lastOperationCallId);
        }
    }

    /**
     * Clean up platform log files
     * @return true if successful, false otherwise
     */
    private boolean cleanupPlatformFiles() {
        try {
            String platformLogPath = LogPathManager.getPlatformLogPath();
            File platformLogFile = new File(platformLogPath);
            File platformLogDir = platformLogFile.getParentFile();

            if (!isValidPlatformDirectory(platformLogDir)) {
                Log.i(TAG, "Platform files cleanup completed. Deleted 0 files");
                return true;
            }

            PlatformCleanupResult result = cleanupFilesInDirectory(platformLogDir);
            Log.i(TAG, "Platform files cleanup completed. Deleted " + result.deletedCount + " files");
            return result.allSuccess;

        } catch (Exception e) {
            Log.e(TAG, "Error cleaning platform files: " + Log.getStackTraceString(e));
            return false;
        }
    }

    /**
     * Check if platform directory is valid for cleanup
     */
    private boolean isValidPlatformDirectory(File platformLogDir) {
        return platformLogDir != null && platformLogDir.exists() && platformLogDir.isDirectory();
    }

    /**
     * Clean up files in the given directory
     */
    private PlatformCleanupResult cleanupFilesInDirectory(File platformLogDir) {
        File[] files = platformLogDir.listFiles();
        if (files == null) {
            return new PlatformCleanupResult(0, true);
        }

        int deletedCount = 0;
        boolean allSuccess = true;

        for (File file : files) {
            if (file.isFile() && shouldDeletePlatformFile(file)) {
                boolean deleted = deletePlatformFile(file);
                if (deleted) {
                    deletedCount++;
                } else {
                    allSuccess = false;
                }
            }
        }

        return new PlatformCleanupResult(deletedCount, allSuccess);
    }

    /**
     * Delete a platform file and log the result
     */
    private boolean deletePlatformFile(File file) {
        return FileUtils.deleteFileWithDescription(file, FileManager.TAG_PLATFORM);
    }

    /**
     * Result class for platform cleanup operation
     */
    private static class PlatformCleanupResult {
        final int deletedCount;
        final boolean allSuccess;

        PlatformCleanupResult(int deletedCount, boolean allSuccess) {
            this.deletedCount = deletedCount;
            this.allSuccess = allSuccess;
        }
    }

    /**
     * Clean up solution debug log files
     * @param solutionId The solution ID to clean (null for all solutions)
     * @return true if successful, false otherwise
     */
    private boolean cleanupSolutionDebugFiles(String solutionId) {
        try {
            String logDirPath = LogPathManager.getSolutionDirectoryPath();
            File logDir = new File(logDirPath);

            if (!logDir.exists() || !logDir.isDirectory()) {
                Log.i(TAG, "Solution debug log directory does not exist: " + logDirPath);
                return true;
            }

            File[] files = logDir.listFiles();
            if (files == null) {
                Log.w(TAG, "Cannot list files in solution debug directory: " + logDirPath);
                return false;
            }

            int deletedCount = 0;
            boolean allSuccess = true;

            for (File file : files) {
                if (file.isFile() && shouldDeleteSolutionDebugFile(file, solutionId)) {
                    boolean deleted = FileUtils.deleteFileWithDescription(file, FileManager.TAG_LOG);
                    if (deleted) {
                        deletedCount++;
                        Log.d(TAG, "Deleted solution debug file: " + file.getName());
                    } else {
                        Log.w(TAG, "Failed to delete solution debug file: " + file.getName());
                        allSuccess = false;
                    }
                }
            }

            Log.i(TAG, "Solution debug files cleanup completed. Deleted " + deletedCount + " files");
            return allSuccess;

        } catch (Exception e) {
            Log.e(TAG, "Error cleaning solution debug files: " + Log.getStackTraceString(e));
            return false;
        }
    }

    /**
     * Clean up solution crash log files
     * @param solutionId The solution ID to clean (null for all solutions)
     * @return true if successful, false otherwise
     */
    private boolean cleanupSolutionCrashFiles(String solutionId) {
        try {
            String crashDirPath = LogPathManager.getCrashDirectoryPath();
            File crashDir = new File(crashDirPath);

            if (!crashDir.exists() || !crashDir.isDirectory()) {
                Log.i(TAG, "Solution crash log directory does not exist: " + crashDirPath);
                return true;
            }

            if (solutionId != null && !solutionId.isEmpty()) {
                if (!UuidValidator.isValidUuid(solutionId)) {
                    Log.e(TAG, "Invalid solution ID: " + solutionId);
                    return false;
                }
                // Clean specific solution's crash directory
                return cleanupSingleSolutionCrashDir(new File(crashDir, solutionId));
            }

            // Clean all solution crash directories
            return cleanupAllSolutionCrashDirs(crashDir);

        } catch (Exception e) {
            Log.e(TAG, "Error cleaning solution crash files: " + Log.getStackTraceString(e));
            return false;
        }
    }

    /**
     * Clean up crash files for a single solution UUID directory
     * @param uuidDir The UUID directory to clean
     * @return true if successful, false otherwise
     */
    private boolean cleanupSingleSolutionCrashDir(File uuidDir) {
        if (!uuidDir.exists()) {
            Log.i(TAG, "Solution crash directory does not exist: " + uuidDir.getAbsolutePath());
            return true;
        }

        if (!uuidDir.isDirectory()) {
            Log.w(TAG, "Solution crash path is not a directory: " + uuidDir.getAbsolutePath());
            return false;
        }

        boolean allSuccess = FileUtils.deleteDirectoryRecursively(uuidDir, FileManager.TAG_CRASH);
        Log.i(TAG, "Solution crash directory cleanup " + (allSuccess ? "succeeded" : "failed") + ": " + uuidDir.getAbsolutePath());
        return allSuccess;
    }

    /**
     * Clean up all solution crash directories under the crash base directory
     * @param crashDir The crash base directory
     * @return true if all cleanups succeeded, false otherwise
     */
    private boolean cleanupAllSolutionCrashDirs(File crashDir) {
        File[] entries = crashDir.listFiles();
        if (entries == null) {
            Log.w(TAG, "Cannot list files in crash directory: " + crashDir.getAbsolutePath());
            return false;
        }

        int deletedCount = 0;
        boolean allSuccess = true;

        for (File entry : entries) {
            if (entry.isDirectory()) {
                if (FileUtils.deleteDirectoryRecursively(entry, FileManager.TAG_CRASH)) {
                    deletedCount++;
                } else {
                    allSuccess = false;
                }
            } else if (entry.isFile()) {
                // Also clean any stray files at the top level
                if (FileUtils.deleteFileWithDescription(entry, FileManager.TAG_CRASH)) {
                    deletedCount++;
                } else {
                    allSuccess = false;
                }
            }
        }

        Log.i(TAG, "Solution crash files cleanup completed. Deleted " + deletedCount + " entries");
        return allSuccess;
    }

    /**
     * Determine if a platform log file should be deleted
     * @param file The file to check
     * @return true if file should be deleted
     */
    private boolean shouldDeletePlatformFile(File file) {
        String fileName = file.getName();
        // Delete platform log files (usually contains "platform" in name or specific patterns)
        return fileName.contains("AndroidLogs") || fileName.endsWith(".log") || fileName.endsWith(".gz");
    }

    /**
     * Determine if a solution debug log file should be deleted
     * @param file The file to check
     * @param solutionId The solution ID filter (null for all solutions)
     * @return true if file should be deleted
     */
    private boolean shouldDeleteSolutionDebugFile(File file, String solutionId) {
        String fileName = file.getName();

        // If solutionId is null, delete all solution debug log files
        if (solutionId == null) {
            return fileName.endsWith(".log") || fileName.endsWith(".gz");
        }

        // If solutionId is specified, only delete logs for that specific solution
        return (fileName.contains(solutionId) && (fileName.endsWith(".log") || fileName.endsWith(".gz")));
    }

    // Getter methods to access parsed data
    public String getLastLogType() {
        return lastLogType;
    }

    public String getLastSolutionId() {
        return lastSolutionId;
    }

    public String getLastOperationCallId() {
        return lastOperationCallId;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Set current instance when service starts
        setCurrentInstance(this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        // Clear current instance when service stops
        setCurrentInstance(null);
        super.onDestroy();
    }

    /**
     * Set current service instance (synchronized for thread safety)
     * @param instance the service instance to set
     */
    private static synchronized void setCurrentInstance(WSCallbackLogManagement instance) {
        currentInstance = instance;
        if (instance != null) {
            Log.i(TAG, "WSCallbackLogManagement instance set for testing");
        } else {
            Log.i(TAG, "WSCallbackLogManagement instance cleared");
        }
    }

    /**
     * Get current service instance for testing
     * @return current service instance or null if not available
     */
    public static synchronized WSCallbackLogManagement getCurrentInstance() {
        return currentInstance;
    }

    /**
     * Static method to send test message to current service instance
     * @param jsonMessage The JSON message to send
     * @return true if message was sent successfully, false otherwise
     */
    public static boolean sendTestMessage(String jsonMessage) {
        WSCallbackLogManagement instance = getCurrentInstance();
        if (instance != null) {
            try {
                Log.i(TAG, "Sending test message to existing service instance");
                instance.onReceived(0, jsonMessage);
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error sending test message: " + Log.getStackTraceString(e));
                return false;
            }
        } else {
            Log.w(TAG, "No service instance available for test message");
            return false;
        }
    }
}
