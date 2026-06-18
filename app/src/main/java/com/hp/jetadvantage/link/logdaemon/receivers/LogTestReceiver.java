package com.hp.jetadvantage.link.logdaemon.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hp.jetadvantage.link.logdaemon.model.LogManagementRequestMessage;
import com.hp.jetadvantage.link.logdaemon.services.LogcatService;
import com.hp.jetadvantage.link.logdaemon.services.WSCallbackLogManagement;

public class LogTestReceiver extends BroadcastReceiver {
    private static final String TAG = "[LD][LogTestReceiver]";

    // Broadcast action constants for testing
    public static final String ACTION_TEST_ALL_LOGS = "com.hp.jetadvantage.link.logdaemon.TEST_ALL_LOGS";
    public static final String ACTION_TEST_PLATFORM_LOGS = "com.hp.jetadvantage.link.logdaemon.TEST_PLATFORM_LOGS";
    public static final String ACTION_TEST_SOLUTION_DEBUG_LOGS = "com.hp.jetadvantage.link.logdaemon.TEST_SOLUTION_DEBUG_LOGS";
    public static final String ACTION_TEST_SOLUTION_CRASH_LOGS = "com.hp.jetadvantage.link.logdaemon.TEST_SOLUTION_CRASH_LOGS";
    public static final String ACTION_TEST_DUMMY_CRASH_LOGS = "com.hp.jetadvantage.link.logdaemon.TEST_DUMMY_CRASH_LOGS";
    public static final String ACTION_TEST_ALL_LOG_TYPES = "com.hp.jetadvantage.link.logdaemon.TEST_ALL_LOG_TYPES";

    private static final String DEFAULT_TEST_UUID = "11111111-1111-1111-9999-111111111111";


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "Received broadcast action: " + action);

        if (action == null) {
            Log.w(TAG, "Action is null, ignoring broadcast");
            return;
        }

        switch (action) {
            case ACTION_TEST_ALL_LOGS:
                Log.i(TAG, "Testing ltAll log type");
                testLogType(LogManagementRequestMessage.LOG_TYPE_ALL, null);
                break;

            case ACTION_TEST_PLATFORM_LOGS:
                Log.i(TAG, "Testing ltPlatform log type");
                testLogType(LogManagementRequestMessage.LOG_TYPE_PLATFORM, null);
                break;

            case ACTION_TEST_SOLUTION_DEBUG_LOGS:
                Log.i(TAG, "Testing ltSolutionDebug log type");
                String debugSolutionId = intent.getStringExtra("solutionId");
                if (debugSolutionId == null || debugSolutionId.isEmpty()) {
                    debugSolutionId = DEFAULT_TEST_UUID; // Default test UUID
                }
                testLogType(LogManagementRequestMessage.LOG_TYPE_SOLUTION_DEBUG, debugSolutionId);
                break;

            case ACTION_TEST_SOLUTION_CRASH_LOGS:
                Log.i(TAG, "Testing ltSolutionCrash log type");
                String crashSolutionId = intent.getStringExtra("solutionId");
                if (crashSolutionId == null || crashSolutionId.isEmpty()) {
                    crashSolutionId = DEFAULT_TEST_UUID; // Default test UUID
                }
                testLogType(LogManagementRequestMessage.LOG_TYPE_SOLUTION_CRASH, crashSolutionId);
                break;

            case ACTION_TEST_DUMMY_CRASH_LOGS:
                Log.i(TAG, "Testing ltDummyCrash log type");
                String crashDummyId = intent.getStringExtra("solutionId");
                if (crashDummyId == null || crashDummyId.isEmpty()) {
                    crashDummyId = DEFAULT_TEST_UUID; // Default test UUID
                }
                testLogType(LogManagementRequestMessage.LOG_TYPE_DUMMY_CRASH, crashDummyId);
                break;

            case ACTION_TEST_ALL_LOG_TYPES:
                Log.i(TAG, "Testing all log types sequentially");
                runAllLogTypeTests();
                break;

            default:
                Log.w(TAG, "Unknown action received: " + action);
                break;
        }
    }

    /**
     * Test specific log type by creating sample JSON and triggering WSCallbackLogManagement
     * @param logType The log type to test
     * @param solutionId The solution ID (can be null for platform/all types)
     */
    private void testLogType(String logType, String solutionId) {
        try {
            String sampleJson = createSampleJsonForLogType(logType, solutionId);
            Log.i(TAG, "Generated test JSON for " + logType + ": " + sampleJson);

            // Simulate receiving the message through WSCallbackLogManagement
            // This will create a new instance to handle the test
            simulateWebSocketMessage(sampleJson);

        } catch (Exception e) {
            Log.e(TAG, "Error testing log type " + logType + ": " + Log.getStackTraceString(e));
        }
    }

    /**
     * Run all log type tests sequentially with delays
     */
    private void runAllLogTypeTests() {
        new Thread(() -> {
            try {
                Log.i(TAG, "Starting sequential test of all log types");

                // Test ltAll
                testLogType(LogManagementRequestMessage.LOG_TYPE_ALL, null);
                Thread.sleep(2000);

                // Test ltPlatform
                testLogType(LogManagementRequestMessage.LOG_TYPE_PLATFORM, null);
                Thread.sleep(2000);

                // Test ltSolutionDebug
                testLogType(LogManagementRequestMessage.LOG_TYPE_SOLUTION_DEBUG, DEFAULT_TEST_UUID);
                Thread.sleep(2000);

                // Test ltSolutionCrash
                testLogType(LogManagementRequestMessage.LOG_TYPE_SOLUTION_CRASH, DEFAULT_TEST_UUID);

                Log.i(TAG, "Completed all log type tests");

            } catch (InterruptedException e) {
                Log.e(TAG, "Test sequence interrupted: " + Log.getStackTraceString(e));
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Create sample JSON for specific log type
     * @param logType The log type to create sample for
     * @param solutionId The solution ID (can be null for platform/all types)
     * @return JSON string matching LogManagementRequestMessage format
     */
    private String createSampleJsonForLogType(String logType, String solutionId) {
        // Generate a sample operationCallId based on current time
        String operationCallId = "test-" + System.currentTimeMillis() + "-" + logType.substring(2);

        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"logManagement\": {");
        jsonBuilder.append("\"details\": {");
        jsonBuilder.append("\"export\": {");
        jsonBuilder.append("\"operationCallId\": \"").append(operationCallId).append("\",");
        jsonBuilder.append("\"logType\": \"").append(logType).append("\"");

        if (solutionId != null && !solutionId.isEmpty()) {
            jsonBuilder.append(",\"solutionId\": \"").append(solutionId).append("\"");
        }

        jsonBuilder.append("}");
        jsonBuilder.append("},");
        jsonBuilder.append("\"traceId\": ").append(System.currentTimeMillis() % 10000);
        jsonBuilder.append("}");
        jsonBuilder.append("}");

        return jsonBuilder.toString();
    }

    /**
     * Simulate WebSocket message by directly calling WSCallbackLogManagement.onReceived
     * @param jsonMessage The JSON message to simulate
     */
    private void simulateWebSocketMessage(String jsonMessage) {
        try {
            // Try to use existing service instance first
            boolean success = WSCallbackLogManagement.sendTestMessage(jsonMessage);

            if (!success) {
                // If no existing instance, start service and try again
                Log.i(TAG, "No existing service instance, starting WSCallbackLogManagement service");
                startServiceAndRetry(jsonMessage);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error simulating WebSocket message: " + Log.getStackTraceString(e));
        }
    }

    /**
     * Start WSCallbackLogManagement service and retry sending message
     * @param jsonMessage The JSON message to send
     */
    private void startServiceAndRetry(String jsonMessage) {
        try {
            Context context = LogcatService.getServiceContext();
            if (context == null) {
                Log.e(TAG, "Service context is null, cannot start service");
                return;
            }

            // Start the WSCallbackLogManagement service
            Intent serviceIntent = new Intent(context, WSCallbackLogManagement.class);
            context.startService(serviceIntent);

            // Wait for service to start and establish connection, then retry
            new Thread(() -> {
                try {
                    Thread.sleep(3000); // Wait 3 seconds for service to start and connect

                    // Retry sending the message
                    boolean success = WSCallbackLogManagement.sendTestMessage(jsonMessage);
                    if (success) {
                        Log.i(TAG, "Message sent successfully after starting service");
                    } else {
                        Log.e(TAG, "Failed to send message even after starting service");
                    }

                } catch (InterruptedException e) {
                    Log.e(TAG, "Thread interrupted while waiting for service: " + Log.getStackTraceString(e));
                    Thread.currentThread().interrupt();
                }
            }).start();

        } catch (Exception e) {
            Log.e(TAG, "Error starting service: " + Log.getStackTraceString(e));
        }
    }
}
