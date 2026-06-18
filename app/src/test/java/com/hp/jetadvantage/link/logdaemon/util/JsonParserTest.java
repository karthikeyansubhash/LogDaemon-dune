package com.hp.jetadvantage.link.logdaemon.util;

import com.hp.jetadvantage.link.logdaemon.model.LogManagementRequestMessage;
import com.hp.jetadvantage.link.logdaemon.model.LogManagementResponseMessage;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Unit tests for JsonParser class to verify Gson 2.13.2 compatibility
 * Tests cover:
 * - Basic JSON parsing and serialization
 * - Null field handling with serializeNulls()
 * - Error handling for malformed JSON
 */
public class JsonParserTest {

    @Before
    public void setUp() {
        // JsonParser uses static initialization, no setup needed
    }

    // ========== Basic Parsing Tests ==========

    @Test
    public void testParseValidExportMessage() {
        // Valid export message with all fields
        String validJson = "{\n" +
                "  \"logManagement\": {\n" +
                "    \"details\": {\n" +
                "      \"export\": {\n" +
                "        \"logType\": \"ltSolutionDebug\",\n" +
                "        \"solutionId\": \"test-uuid-123\",\n" +
                "        \"operationCallId\": \"operation-456\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        LogManagementRequestMessage message = JsonParser.parseLogManagementMessage(validJson);

        assertNotNull("Message should be parsed successfully", message);
        assertNotNull("LogManagement should not be null", message.getLogManagement());
        assertNotNull("Details should not be null", message.getLogManagement().getDetails());
        assertNotNull("Export should not be null", message.getLogManagement().getDetails().getExport());
        
        LogManagementRequestMessage.LogOperation export = message.getLogManagement().getDetails().getExport();
        assertEquals("ltSolutionDebug", export.getLogType());
        assertEquals("test-uuid-123", export.getSolutionId());
        assertEquals("operation-456", export.getOperationCallId());
    }

    @Test
    public void testParseValidCleanupMessage() {
        // Valid cleanup message
        String validJson = "{\n" +
                "  \"logManagement\": {\n" +
                "    \"details\": {\n" +
                "      \"cleanup\": {\n" +
                "        \"logType\": \"ltAll\",\n" +
                "        \"solutionId\": null,\n" +
                "        \"operationCallId\": \"cleanup-789\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        LogManagementRequestMessage message = JsonParser.parseLogManagementMessage(validJson);

        assertNotNull("Message should be parsed successfully", message);
        assertNotNull("Cleanup should not be null", message.getLogManagement().getDetails().getCleanup());
        
        LogManagementRequestMessage.LogOperation cleanup = message.getLogManagement().getDetails().getCleanup();
        assertEquals("ltAll", cleanup.getLogType());
        assertNull("SolutionId should be null", cleanup.getSolutionId());
        assertEquals("cleanup-789", cleanup.getOperationCallId());
    }

    // ========== Null Handling Tests (serializeNulls) ==========

    @Test
    public void testSerializeNullsInResponse() {
        // Create response with null path (filePath)
        LogManagementResponseMessage response = JsonParser.createCompletedResponse(
                "ltSolutionDebug", 
                null,  // null path
                "test-operation-id"
        );

        String json = JsonParser.toJson(response);

        assertNotNull("JSON should not be null", json);
        assertTrue("JSON should contain 'path' key", json.contains("\"path\""));
        assertTrue("JSON should contain null value for path", json.contains("\"path\":null") || json.contains("\"path\": null"));
        
        System.out.println("Serialized JSON with null field:");
        System.out.println(json);
    }

    @Test
    public void testParseJsonWithNullFields() {
        // JSON with explicit null values
        String jsonWithNulls = "{\n" +
                "  \"logManagement\": {\n" +
                "    \"details\": {\n" +
                "      \"export\": {\n" +
                "        \"logType\": \"ltSolutionCrash\",\n" +
                "        \"solutionId\": null,\n" +
                "        \"operationCallId\": null\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        LogManagementRequestMessage message = JsonParser.parseLogManagementMessage(jsonWithNulls);

        assertNotNull("Message should be parsed successfully", message);
        LogManagementRequestMessage.LogOperation export = message.getLogManagement().getDetails().getExport();
        
        assertEquals("ltSolutionCrash", export.getLogType());
        assertNull("SolutionId should be null", export.getSolutionId());
        assertNull("OperationCallId should be null", export.getOperationCallId());
    }

    @Test
    public void testSerializeAndDeserializeRoundTrip() {
        // Create a response message
        LogManagementResponseMessage original = JsonParser.createFailedResponse(
                "ltPlatform",
                "Test error message",
                "operation-round-trip"
        );

        // Serialize to JSON
        String json = JsonParser.toJson(original);
        assertNotNull("Serialization should not return null", json);

        System.out.println("Round-trip test JSON:");
        System.out.println(json);

        // Note: We can't deserialize back to LogManagementResponseMessage 
        // because there's no fromJson method for it in JsonParser
        // But we can verify the JSON contains expected fields
        assertTrue("JSON should contain logType", json.contains("\"logType\""));
        assertTrue("JSON should contain status", json.contains("\"status\""));
        assertTrue("JSON should contain error", json.contains("\"error\""));
    }

    // ========== Error Handling Tests ==========

    @Test
    public void testParseNullJson() {
        LogManagementRequestMessage message = JsonParser.parseLogManagementMessage(null);
        assertNull("Parsing null JSON should return null", message);
    }

    @Test
    public void testParseEmptyJson() {
        LogManagementRequestMessage message = JsonParser.parseLogManagementMessage("");
        assertNull("Parsing empty JSON should return null", message);
    }

    @Test
    public void testParseWhitespaceJson() {
        LogManagementRequestMessage message = JsonParser.parseLogManagementMessage("   \n  \t  ");
        assertNull("Parsing whitespace JSON should return null", message);
    }

    @Test
    public void testParseMalformedJson() {
        String malformedJson = "{ invalid json without quotes }";
        LogManagementRequestMessage message = JsonParser.parseLogManagementMessage(malformedJson);
        assertNull("Parsing malformed JSON should return null", message);
    }

    @Test
    public void testParseIncompleteJson() {
        String incompleteJson = "{\n" +
                "  \"logManagement\": {\n" +
                "    \"details\": {\n";  // Incomplete JSON

        LogManagementRequestMessage message = JsonParser.parseLogManagementMessage(incompleteJson);
        assertNull("Parsing incomplete JSON should return null", message);
    }

    @Test
    public void testParseJsonWithExtraFields() {
        // JSON with extra unknown fields (Gson should ignore them)
        String jsonWithExtra = "{\n" +
                "  \"logManagement\": {\n" +
                "    \"unknownField\": \"should be ignored\",\n" +
                "    \"details\": {\n" +
                "      \"export\": {\n" +
                "        \"logType\": \"ltAll\",\n" +
                "        \"solutionId\": \"uuid-with-extra\",\n" +
                "        \"operationCallId\": \"op-123\",\n" +
                "        \"extraField\": \"also ignored\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        LogManagementRequestMessage message = JsonParser.parseLogManagementMessage(jsonWithExtra);

        assertNotNull("Message with extra fields should be parsed", message);
        LogManagementRequestMessage.LogOperation export = message.getLogManagement().getDetails().getExport();
        assertEquals("ltAll", export.getLogType());
        assertEquals("uuid-with-extra", export.getSolutionId());
    }

    // ========== Response Message Serialization Tests ==========

    @Test
    public void testSerializeInProgressResponse() {
        LogManagementResponseMessage response = JsonParser.createInProgressResponse(
                "ltSolutionDebug",
                "in-progress-op"
        );

        String json = JsonParser.toJson(response);

        assertNotNull("Serialization should not return null", json);
        assertTrue("JSON should contain status", json.contains("\"status\""));
        assertTrue("JSON should contain esInProgress status", json.contains("esInProgress"));
        
        System.out.println("In-progress response JSON:");
        System.out.println(json);
    }

    @Test
    public void testSerializeCompletedResponse() {
        LogManagementResponseMessage response = JsonParser.createCompletedResponse(
                "ltSolutionCrash",
                "/path/to/crash/logs.tar.gz",
                "completed-op"
        );

        String json = JsonParser.toJson(response);

        assertNotNull("Serialization should not return null", json);
        assertTrue("JSON should contain esCompleted status", json.contains("esCompleted"));
        assertTrue("JSON should contain path", json.contains("\"path\""));
        assertTrue("JSON should contain file path value", json.contains("/path/to/crash/logs.tar.gz"));
        
        System.out.println("Completed response JSON:");
        System.out.println(json);
    }

    @Test
    public void testSerializeFailedResponse() {
        LogManagementResponseMessage response = JsonParser.createFailedResponse(
                "ltPlatform",
                "File not found error",
                "failed-op"
        );

        String json = JsonParser.toJson(response);

        assertNotNull("Serialization should not return null", json);
        assertTrue("JSON should contain esFailed status", json.contains("esFailed"));
        assertTrue("JSON should contain error", json.contains("\"error\""));
        assertTrue("JSON should contain error text", json.contains("File not found error"));
        
        System.out.println("Failed response JSON:");
        System.out.println(json);
    }

    @Test
    public void testSerializeCleanupResponse() {
        LogManagementResponseMessage completedCleanup = JsonParser.createCleanupCompletedResponse(
                "ltAll",
                "cleanup-completed-op"
        );

        String completedJson = JsonParser.toJson(completedCleanup);
        assertNotNull("Cleanup completed serialization should not return null", completedJson);
        assertTrue("JSON should contain cuCompleted status", completedJson.contains("cuCompleted"));

        LogManagementResponseMessage failedCleanup = JsonParser.createCleanupFailedResponse(
                "ltSolutionDebug",
                "Cleanup failed due to permission error",
                "cleanup-failed-op"
        );

        String failedJson = JsonParser.toJson(failedCleanup);
        assertNotNull("Cleanup failed serialization should not return null", failedJson);
        assertTrue("JSON should contain cuFailed status", failedJson.contains("cuFailed"));
        assertTrue("JSON should contain error message", failedJson.contains("permission error"));
        
        System.out.println("Cleanup completed response JSON:");
        System.out.println(completedJson);
        System.out.println("\nCleanup failed response JSON:");
        System.out.println(failedJson);
    }

    // ========== Edge Cases ==========

    @Test
    public void testParseJsonWithUnicodeCharacters() {
        String unicodeJson = "{\n" +
                "  \"logManagement\": {\n" +
                "    \"details\": {\n" +
                "      \"export\": {\n" +
                "        \"logType\": \"ltSolutionDebug\",\n" +
                "        \"solutionId\": \"uuid-한글-테스트-🔥\",\n" +
                "        \"operationCallId\": \"op-unicode\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        LogManagementRequestMessage message = JsonParser.parseLogManagementMessage(unicodeJson);

        assertNotNull("Message with Unicode should be parsed", message);
        LogManagementRequestMessage.LogOperation export = message.getLogManagement().getDetails().getExport();
        assertTrue("SolutionId should contain Unicode characters", 
                export.getSolutionId().contains("한글"));
    }

    @Test
    public void testSerializeResponseWithSpecialCharacters() {
        LogManagementResponseMessage response = JsonParser.createFailedResponse(
                "ltSolutionCrash",
                "Error: \"File not found\" in path /data/log\\crash",  // Quotes and backslash
                "special-chars-op"
        );

        String json = JsonParser.toJson(response);

        assertNotNull("Serialization with special chars should not return null", json);
        // Gson should escape special characters properly
        assertTrue("JSON should be valid", json.contains("\\\"") || json.contains("\\\\"));
        
        System.out.println("Response with special characters:");
        System.out.println(json);
    }

    // ========== Performance Test (Optional) ==========

    @Test
    public void testParsingPerformance() {
        String testJson = "{\n" +
                "  \"logManagement\": {\n" +
                "    \"details\": {\n" +
                "      \"export\": {\n" +
                "        \"logType\": \"ltSolutionDebug\",\n" +
                "        \"solutionId\": \"performance-test-uuid\",\n" +
                "        \"operationCallId\": \"perf-op-123\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        long startTime = System.currentTimeMillis();
        int iterations = 1000;

        for (int i = 0; i < iterations; i++) {
            LogManagementRequestMessage message = JsonParser.parseLogManagementMessage(testJson);
            assertNotNull("Each iteration should parse successfully", message);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("Performance test: Parsed " + iterations + " messages in " + duration + "ms");
        System.out.println("Average: " + (duration / (double) iterations) + "ms per parse");

        // Assert reasonable performance (should be much less than 1 second total)
        assertTrue("Parsing should complete in reasonable time", duration < 5000);
    }

    // ========== Response solutionId Field Tests ==========

    @Test
    public void testCompletedResponse_withSolutionId() {
        LogManagementResponseMessage response = JsonParser.createCompletedResponse(
                "ltSolutionCrash",
                "/solutions/uuid-123/uuid-123_CrashLogs_archive_20261001_120000.tar.gz",
                "op-with-solution",
                "uuid-123"
        );

        String json = JsonParser.toJson(response);
        assertNotNull("JSON should not be null", json);
        assertTrue("JSON should contain solutionId", json.contains("\"solutionId\""));
        assertTrue("JSON should contain solutionId value", json.contains("uuid-123"));
        assertTrue("JSON should contain esCompleted", json.contains("esCompleted"));
        assertTrue("JSON should contain path", json.contains("CrashLogs_archive"));
    }

    @Test
    public void testCompletedResponse_withoutSolutionId() {
        LogManagementResponseMessage response = JsonParser.createCompletedResponse(
                "ltPlatform",
                "/AndroidLogs.tar.gz",
                "op-no-solution"
        );

        String json = JsonParser.toJson(response);
        assertNotNull("JSON should not be null", json);
        assertTrue("JSON should contain esCompleted", json.contains("esCompleted"));
        // solutionId should be null (serialized as null due to serializeNulls)
        assertTrue("JSON should contain solutionId key with null", 
                json.contains("\"solutionId\""));
    }

    @Test
    public void testResponseMessage_withSolutionId_roundTrip() {
        String solutionId = "11111111-1111-1111-9999-111111111111";
        LogManagementResponseMessage response = JsonParser.createResponseMessage(
                "ltSolutionCrash",
                LogManagementResponseMessage.STATUS_COMPLETED,
                "/solutions/" + solutionId + "/archive.tar.gz",
                null,
                "op-round-trip",
                solutionId
        );

        String json = JsonParser.toJson(response);
        assertNotNull("JSON should not be null", json);
        assertTrue("JSON should contain solutionId", json.contains(solutionId));
        assertTrue("JSON should have exportStatus", json.contains("exportStatus"));
    }

    // ========== OperationStatus solutionId Getter/Setter Tests ==========

    @Test
    public void testOperationStatus_solutionId_getterSetter() {
        LogManagementResponseMessage.OperationStatus status = new LogManagementResponseMessage.OperationStatus();
        assertNull("solutionId should be null initially", status.getSolutionId());

        status.setSolutionId("test-solution-id");
        assertEquals("test-solution-id", status.getSolutionId());

        status.setSolutionId(null);
        assertNull("solutionId should be null after setting null", status.getSolutionId());
    }
}
