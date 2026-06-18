package com.hp.jetadvantage.link.logdaemon.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WSCallbackLogManagementTest {

    @Mock
    Context mockContext;

    @Mock
    private BaseWebsocketCallbackService mockBaseService;

    private WSCallbackLogManagement wsCallbackLogManagement;

    @Before
    public void setUp() {
        WSCallbackLogManagement callback = new WSCallbackLogManagement();
        wsCallbackLogManagement = Mockito.spy(callback);
    }

    @Test
    public void onReceived_logsMessageWhenDataIsProvided() {
        String testMessage = "{\n" +
                "    \"logManagement\": {\n" +
                "        \"details\": {\n" +
                "            \"export\": {\n" +
                "                \"logType\": \"ltSolutionDebug\",\n" +
                "                \"solutionId\": \"11111111-1111-1111-9996-111111111111\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"traceId\": 5\n" +
                "    }\n" +
                "}";

        wsCallbackLogManagement.onReceived(0, testMessage);

        // Verify parsed data is stored correctly
        assertEquals("ltSolutionDebug", wsCallbackLogManagement.getLastLogType());
        assertEquals("11111111-1111-1111-9996-111111111111", wsCallbackLogManagement.getLastSolutionId());
    }

    @Test
    public void onReceived_handlesEmptyDataGracefully() {
        wsCallbackLogManagement.onReceived(1, "");

        // Verify that empty data results in empty stored values
        assertEquals("", wsCallbackLogManagement.getLastLogType());
        assertEquals("", wsCallbackLogManagement.getLastSolutionId());
    }

    @Test
    public void onReceived_handlesNullDataGracefully() {
        wsCallbackLogManagement.onReceived(1, null);

        // Verify that null data results in empty stored values
        assertEquals("", wsCallbackLogManagement.getLastLogType());
        assertEquals("", wsCallbackLogManagement.getLastSolutionId());
    }

    // New test cases for better coverage

    @Test
    public void onReceived_handlesLtSolutionCrashType() {
        String crashMessage = "{\n" +
                "    \"logManagement\": {\n" +
                "        \"details\": {\n" +
                "            \"export\": {\n" +
                "                \"logType\": \"ltSolutionCrash\",\n" +
                "                \"solutionId\": \"22222222-2222-2222-2222-222222222222\",\n" +
                "                \"operationCallId\": \"crash-test-123\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

        wsCallbackLogManagement.onReceived(0, crashMessage);

        assertEquals("ltSolutionCrash", wsCallbackLogManagement.getLastLogType());
        assertEquals("22222222-2222-2222-2222-222222222222", wsCallbackLogManagement.getLastSolutionId());
        assertEquals("crash-test-123", wsCallbackLogManagement.getLastOperationCallId());
    }

    @Test
    public void onReceived_handlesLtAllType() {
        String allMessage = "{\n" +
                "    \"logManagement\": {\n" +
                "        \"details\": {\n" +
                "            \"export\": {\n" +
                "                \"logType\": \"ltAll\",\n" +
                "                \"operationCallId\": \"all-logs-456\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

        wsCallbackLogManagement.onReceived(0, allMessage);

        assertEquals("ltAll", wsCallbackLogManagement.getLastLogType());
        assertEquals("", wsCallbackLogManagement.getLastSolutionId()); // No solutionId for ltAll
        assertEquals("all-logs-456", wsCallbackLogManagement.getLastOperationCallId());
    }

    @Test
    public void onReceived_handlesLtPlatformType() {
        String platformMessage = "{\n" +
                "    \"logManagement\": {\n" +
                "        \"details\": {\n" +
                "            \"export\": {\n" +
                "                \"logType\": \"ltPlatform\",\n" +
                "                \"operationCallId\": \"platform-789\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

        wsCallbackLogManagement.onReceived(0, platformMessage);

        assertEquals("ltPlatform", wsCallbackLogManagement.getLastLogType());
        assertEquals("", wsCallbackLogManagement.getLastSolutionId()); // No solutionId for ltPlatform
        assertEquals("platform-789", wsCallbackLogManagement.getLastOperationCallId());
    }

    @Test
    public void onReceived_handlesInvalidJsonFormat() {
        String invalidJson = "{ invalid json format }";

        wsCallbackLogManagement.onReceived(0, invalidJson);

        // Should reset all values to empty on invalid JSON
        assertEquals("", wsCallbackLogManagement.getLastLogType());
        assertEquals("", wsCallbackLogManagement.getLastSolutionId());
        assertEquals("", wsCallbackLogManagement.getLastOperationCallId());
    }

    @Test
    public void onReceived_handlesMissingExportSection() {
        String messageWithoutExport = "{\n" +
                "    \"logManagement\": {\n" +
                "        \"details\": {\n" +
                "        }\n" +
                "    }\n" +
                "}";

        wsCallbackLogManagement.onReceived(0, messageWithoutExport);

        // Should result in empty values when export section is missing
        assertEquals("", wsCallbackLogManagement.getLastLogType());
        assertEquals("", wsCallbackLogManagement.getLastSolutionId());
        assertEquals("", wsCallbackLogManagement.getLastOperationCallId());
    }

    @Test
    public void onReceived_handlesMissingLogManagementSection() {
        String messageWithoutLogManagement = "{\n" +
                "    \"someOtherData\": {\n" +
                "        \"value\": \"test\"\n" +
                "    }\n" +
                "}";

        wsCallbackLogManagement.onReceived(0, messageWithoutLogManagement);

        // Should result in empty values when logManagement section is missing
        assertEquals("", wsCallbackLogManagement.getLastLogType());
        assertEquals("", wsCallbackLogManagement.getLastSolutionId());
        assertEquals("", wsCallbackLogManagement.getLastOperationCallId());
    }

    @Test
    public void onReceived_handlesPartialData() {
        String partialMessage = "{\n" +
                "    \"logManagement\": {\n" +
                "        \"details\": {\n" +
                "            \"export\": {\n" +
                "                \"logType\": \"ltSolutionDebug\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

        wsCallbackLogManagement.onReceived(0, partialMessage);

        // Should handle partial data gracefully
        assertEquals("ltSolutionDebug", wsCallbackLogManagement.getLastLogType());
        assertEquals("", wsCallbackLogManagement.getLastSolutionId()); // Missing solutionId
        assertEquals("", wsCallbackLogManagement.getLastOperationCallId()); // Missing operationCallId
    }

    @Test
    public void staticMethods_getCurrentInstance() {
        // Test that getCurrentInstance returns null when no instance is set
        assertNull("getCurrentInstance should return null initially",
                   WSCallbackLogManagement.getCurrentInstance());

        // Note: Testing setCurrentInstance would require actual service lifecycle,
        // which is beyond unit test scope and better suited for integration tests
    }

    @Test
    public void staticMethods_sendTestMessage() {
        String testMessage = "{ \"test\": \"message\" }";

        // Should return false when no instance is available
        assertEquals("sendTestMessage should return false when no instance available",
                    false, WSCallbackLogManagement.sendTestMessage(testMessage));
    }

    @Test
    public void constructor_createsInstanceSuccessfully() {
        WSCallbackLogManagement instance = new WSCallbackLogManagement();

        // Verify initial state
        assertNotNull("Instance should be created", instance);
        assertEquals("", instance.getLastLogType());
        assertEquals("", instance.getLastSolutionId());
        assertEquals("", instance.getLastOperationCallId());
    }

    @Test
    public void onReceived_handlesNullFields() {
        String messageWithNulls = "{\n" +
                "    \"logManagement\": {\n" +
                "        \"details\": {\n" +
                "            \"export\": {\n" +
                "                \"logType\": null,\n" +
                "                \"solutionId\": null,\n" +
                "                \"operationCallId\": null\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

        wsCallbackLogManagement.onReceived(0, messageWithNulls);

        // Should handle null fields as empty strings
        assertEquals("", wsCallbackLogManagement.getLastLogType());
        assertEquals("", wsCallbackLogManagement.getLastSolutionId());
        assertEquals("", wsCallbackLogManagement.getLastOperationCallId());
    }

    @Test
    public void onReceived_cleanupOperation_parsesCorrectly() {
        String cleanupMessage = "{\n" +
                "    \"logManagement\": {\n" +
                "        \"details\": {\n" +
                "            \"cleanup\": {\n" +
                "                \"logType\": \"ltSolutionCrash\",\n" +
                "                \"solutionId\": \"44444444-4444-4444-4444-444444444444\",\n" +
                "                \"operationCallId\": \"cleanup-001\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

        wsCallbackLogManagement.onReceived(0, cleanupMessage);

        assertEquals("ltSolutionCrash", wsCallbackLogManagement.getLastLogType());
        assertEquals("44444444-4444-4444-4444-444444444444", wsCallbackLogManagement.getLastSolutionId());
        assertEquals("cleanup-001", wsCallbackLogManagement.getLastOperationCallId());
    }

    @Test
    public void onReceived_handlesLtDummyCrashType() {
        String dummyCrashMessage = "{\n" +
                "    \"logManagement\": {\n" +
                "        \"details\": {\n" +
                "            \"export\": {\n" +
                "                \"logType\": \"ltDummyCrash\",\n" +
                "                \"solutionId\": \"55555555-5555-5555-5555-555555555555\",\n" +
                "                \"operationCallId\": \"dummy-crash-001\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

        wsCallbackLogManagement.onReceived(0, dummyCrashMessage);

        assertEquals("ltDummyCrash", wsCallbackLogManagement.getLastLogType());
        assertEquals("55555555-5555-5555-5555-555555555555", wsCallbackLogManagement.getLastSolutionId());
        assertEquals("dummy-crash-001", wsCallbackLogManagement.getLastOperationCallId());
    }

    @Test
    public void onReceived_handlesLtDummyCrashWithoutSolutionId() {
        String dummyCrashMessage = "{\n" +
                "    \"logManagement\": {\n" +
                "        \"details\": {\n" +
                "            \"export\": {\n" +
                "                \"logType\": \"ltDummyCrash\",\n" +
                "                \"operationCallId\": \"dummy-crash-002\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

        wsCallbackLogManagement.onReceived(0, dummyCrashMessage);

        assertEquals("ltDummyCrash", wsCallbackLogManagement.getLastLogType());
        assertEquals("", wsCallbackLogManagement.getLastSolutionId());
        assertEquals("dummy-crash-002", wsCallbackLogManagement.getLastOperationCallId());
    }
}