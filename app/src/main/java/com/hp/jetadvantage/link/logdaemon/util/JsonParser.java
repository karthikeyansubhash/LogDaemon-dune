package com.hp.jetadvantage.link.logdaemon.util;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hp.jetadvantage.link.logdaemon.model.LogManagementRequestMessage;
import com.hp.jetadvantage.link.logdaemon.model.LogManagementResponseMessage;

public class JsonParser {
    private static final String TAG = "[LD][JsonParser]";
    private static Gson gson;

    static {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();
    }

    /**
     * Parse JSON string to LogManagementRequestMessage object
     * @param json JSON string
     * @return LogManagementRequestMessage object, null if parsing fails
     */
    public static LogManagementRequestMessage parseLogManagementMessage(String json) {
        try {
            if (json == null || json.trim().isEmpty()) {
                Log.w(TAG, "JSON string is null or empty");
                return null;
            }

            return gson.fromJson(json, LogManagementRequestMessage.class);
        } catch (Exception e) {
            Log.e(TAG, "JSON parsing failed for input: " + json);
            Log.e(TAG, "JSON parsing failed: " + Log.getStackTraceString(e));
            return null;
        }
    }

    /**
     * Convert LogManagementRequestMessage object to JSON string
     * @param message LogManagementRequestMessage object
     * @return JSON string, null if conversion fails
     */
    public static String toJson(LogManagementRequestMessage message) {
        try {
            return gson.toJson(message);
        } catch (Exception e) {
            Log.e(TAG, "JSON conversion failed: " + Log.getStackTraceString(e));
            return null;
        }
    }

    /**
     * Convert LogManagementResponseMessage object to JSON string
     * @param message LogManagementResponseMessage object
     * @return JSON string, null if conversion fails
     */
    public static String toJson(LogManagementResponseMessage message) {
        try {
            return gson.toJson(message);
        } catch (Exception e) {
            Log.e(TAG, "Response JSON conversion failed: " + Log.getStackTraceString(e));
            return null;
        }
    }

    /**
     * Create sample LogManagementMessage object with new schema
     * @return LogManagementMessage object with sample data
     */
    public static LogManagementRequestMessage createSampleMessage() {
        LogManagementRequestMessage message = new LogManagementRequestMessage();

        LogManagementRequestMessage.LogManagement logManagement = new LogManagementRequestMessage.LogManagement();
        LogManagementRequestMessage.Details details = new LogManagementRequestMessage.Details();
        LogManagementRequestMessage.LogOperation export = new LogManagementRequestMessage.LogOperation();

        export.setLogType(LogManagementRequestMessage.LOG_TYPE_ALL);
        export.setSolutionId("550e8400-e29b-41d4-a716-446655440000"); // Sample UUID

        details.setExport(export);
        logManagement.setDetails(details);
        logManagement.setTraceId(1);

        message.setLogManagement(logManagement);

        return message;
    }

    /**
     * Create sample message for specific log type
     * @param logType One of the LOG_TYPE constants from LogManagementMessage
     * @param solutionId UUID string for solution ID (can be null for non-solution types)
     * @param traceId Trace ID for logging
     * @return LogManagementMessage object with specified data
     */
    public static LogManagementRequestMessage createMessage(String logType, String solutionId, int traceId) {
        LogManagementRequestMessage message = new LogManagementRequestMessage();

        LogManagementRequestMessage.LogManagement logManagement = new LogManagementRequestMessage.LogManagement();
        LogManagementRequestMessage.Details details = new LogManagementRequestMessage.Details();
        LogManagementRequestMessage.LogOperation export = new LogManagementRequestMessage.LogOperation();

        export.setLogType(logType);
        if (solutionId != null) {
            export.setSolutionId(solutionId);
        }

        details.setExport(export);
        logManagement.setDetails(details);
        logManagement.setTraceId(traceId);

        message.setLogManagement(logManagement);

        return message;
    }

    /**
     * Create sample cleanup message for specific log type
     * @param logType One of the LOG_TYPE constants from LogManagementMessage
     * @param solutionId UUID string for solution ID (can be null for non-solution types)
     * @param operationCallId Operation call ID for tracking
     * @param traceId Trace ID for logging
     * @return LogManagementMessage object with cleanup operation
     */
    public static LogManagementRequestMessage createCleanupMessage(String logType, String solutionId, String operationCallId, int traceId) {
        LogManagementRequestMessage message = new LogManagementRequestMessage();

        LogManagementRequestMessage.LogManagement logManagement = new LogManagementRequestMessage.LogManagement();
        LogManagementRequestMessage.Details details = new LogManagementRequestMessage.Details();
        LogManagementRequestMessage.LogOperation cleanup = new LogManagementRequestMessage.LogOperation();

        cleanup.setLogType(logType);
        if (solutionId != null) {
            cleanup.setSolutionId(solutionId);
        }
        if (operationCallId != null) {
            cleanup.setOperationCallId(operationCallId);
        }

        details.setCleanup(cleanup);
        logManagement.setDetails(details);
        logManagement.setTraceId(traceId);

        message.setLogManagement(logManagement);

        return message;
    }

    /**
     * Create generic operation response message
     * @param logType The log type from the original request
     * @param status Operation status (esCompleted, esInProgress, esFailed, cuCompleted, cuFailed)
     * @param path File path for successful export (optional, mainly for export operations)
     * @param error Error message for failed operation (optional)
     * @param operationCallId The operation call ID from the original request
     * @param solutionId The solution ID from the original request (optional)
     * @param isCleanup Whether this is a cleanup operation (true) or export operation (false)
     * @return LogManagementResponseMessage object
     */
    public static LogManagementResponseMessage createOperationResponseMessage(String logType, String status, String path, String error, String operationCallId, String solutionId, boolean isCleanup) {
        LogManagementResponseMessage message = new LogManagementResponseMessage();

        LogManagementResponseMessage.LogManagement logManagement = new LogManagementResponseMessage.LogManagement();
        LogManagementResponseMessage.Details details = new LogManagementResponseMessage.Details();
        LogManagementResponseMessage.OperationStatus operationStatus = new LogManagementResponseMessage.OperationStatus();

        operationStatus.setLogType(logType);
        operationStatus.setStatus(status);

        if (operationCallId != null && !operationCallId.isEmpty()) {
            operationStatus.setOperationCallId(operationCallId);
        }

        if (path != null && !path.isEmpty()) {
            operationStatus.setPath(path);
        }

        if (solutionId != null && !solutionId.isEmpty()) {
            operationStatus.setSolutionId(solutionId);
        }

        if (error != null && !error.isEmpty()) {
            operationStatus.setError(error);
        }

        // Set appropriate status based on operation type
        if (isCleanup) {
            details.setCleanupStatus(operationStatus);
        } else {
            details.setExportStatus(operationStatus);
        }

        logManagement.setDetails(details);
        message.setLogManagement(logManagement);

        return message;
    }

    /**
     * Create response message for log export status
     * @param logType The log type from the original request
     * @param status Export status (esCompleted, esInProgress, esFailed)
     * @param path File path for successful export (optional)
     * @param error Error message for failed export (optional)
     * @param operationCallId The operation call ID from the original request
     * @return LogManagementResponseMessage object
     */
    public static LogManagementResponseMessage createResponseMessage(String logType, String status, String path, String error, String operationCallId) {
        return createOperationResponseMessage(logType, status, path, error, operationCallId, null, false);
    }

    /**
     * Create response message for log export status with solutionId
     */
    public static LogManagementResponseMessage createResponseMessage(String logType, String status, String path, String error, String operationCallId, String solutionId) {
        return createOperationResponseMessage(logType, status, path, error, operationCallId, solutionId, false);
    }

    /**
     * Create completed response message
     * @param logType The log type from the original request
     * @param filePath The path to the exported log file
     * @param operationCallId The operation call ID from the original request
     * @return LogManagementResponseMessage with completed status
     */
    public static LogManagementResponseMessage createCompletedResponse(String logType, String filePath, String operationCallId) {
        return createResponseMessage(logType, LogManagementResponseMessage.STATUS_COMPLETED, filePath, null, operationCallId);
    }

    /**
     * Create completed response message with solutionId
     */
    public static LogManagementResponseMessage createCompletedResponse(String logType, String filePath, String operationCallId, String solutionId) {
        return createResponseMessage(logType, LogManagementResponseMessage.STATUS_COMPLETED, filePath, null, operationCallId, solutionId);
    }

    /**
     * Create in-progress response message
     * @param logType The log type from the original request
     * @param operationCallId The operation call ID from the original request
     * @return LogManagementResponseMessage with in-progress status
     */
    public static LogManagementResponseMessage createInProgressResponse(String logType, String operationCallId) {
        return createResponseMessage(logType, LogManagementResponseMessage.STATUS_IN_PROGRESS, null, null, operationCallId);
    }

    /**
     * Create failed response message
     * @param logType The log type from the original request
     * @param errorMessage The error message describing the failure
     * @param operationCallId The operation call ID from the original request
     * @return LogManagementResponseMessage with failed status
     */
    public static LogManagementResponseMessage createFailedResponse(String logType, String errorMessage, String operationCallId) {
        return createResponseMessage(logType, LogManagementResponseMessage.STATUS_FAILED, null, errorMessage, operationCallId);
    }

    /**
     * Create cleanup response message for cleanup operations
     * @param logType The log type from the original request
     * @param status Cleanup status (csCompleted, csFailed)
     * @param error Error message for failed cleanup (optional)
     * @param operationCallId The operation call ID from the original request
     * @return LogManagementResponseMessage object with cleanupStatus
     */
    public static LogManagementResponseMessage createCleanupResponseMessage(String logType, String status, String error, String operationCallId) {
        return createOperationResponseMessage(logType, status, null, error, operationCallId, null, true);
    }

    /**
     * Create completed cleanup response message
     * @param logType The log type from the original request
     * @param operationCallId The operation call ID from the original request
     * @return LogManagementResponseMessage with cleanup completed status
     */
    public static LogManagementResponseMessage createCleanupCompletedResponse(String logType, String operationCallId) {
        return createCleanupResponseMessage(logType, LogManagementResponseMessage.CLEANUP_STATUS_COMPLETED, null, operationCallId);
    }

    /**
     * Create failed cleanup response message
     * @param logType The log type from the original request
     * @param errorMessage The error message describing the failure
     * @param operationCallId The operation call ID from the original request
     * @return LogManagementResponseMessage with cleanup failed status
     */
    public static LogManagementResponseMessage createCleanupFailedResponse(String logType, String errorMessage, String operationCallId) {
        return createCleanupResponseMessage(logType, LogManagementResponseMessage.CLEANUP_STATUS_FAILED, errorMessage, operationCallId);
    }
}
