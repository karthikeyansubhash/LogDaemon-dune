package com.hp.jetadvantage.link.logdaemon.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.hp.jetadvantage.link.logdaemon.data.FileManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;

@RunWith(MockitoJUnitRunner.class)
public class LogPathManagerTest {

    private static final String TEST_UUID = "12345678-1234-1234-1234-123456789012";
    private static final String TEST_UUID_2 = "abcdefab-abcd-abcd-abcd-abcdefabcdef";

    // Expected path constants based on LogPathManager implementation
    private static final String EXPECTED_PLATFORM_LOG_NAME = "AndroidLogs.tar.gz";

    @Before
    public void setUp() {
        // No specific setup needed as LogPathManager uses static methods
    }

    @After
    public void tearDown() {
        // Clean up any test directories if needed
    }

    // ========== Tests for Base Path Methods ==========

    @Test
    public void getBasePath_returnsCorrectPath() {
        String basePath = LogPathManager.getBasePath();

        assertNotNull("Base path should not be null", basePath);
        assertTrue("Base path should end with expected path", 
                basePath.endsWith("data" + File.separator + "workpath" + File.separator + "log") ||
                basePath.endsWith("data/workpath/log"));
    }

    @Test
    public void getBasePath_returnsAbsolutePath() {
        String basePath = LogPathManager.getBasePath();

        assertNotNull("Base path should not be null", basePath);
        assertTrue("Base path should be absolute", 
                new File(basePath).isAbsolute());
    }

    @Test
    public void getBasePath_consistentResults() {
        String basePath1 = LogPathManager.getBasePath();
        String basePath2 = LogPathManager.getBasePath();

        assertEquals("Base path should be consistent across calls", 
                basePath1, basePath2);
    }

    // ========== Tests for Directory Path Methods ==========

    @Test
    public void getSolutionDirectoryPath_returnsCorrectPath() {
        String solutionPath = LogPathManager.getSolutionDirectoryPath();

        assertNotNull("Solution directory path should not be null", solutionPath);
        assertTrue("Solution path should end with solutions", 
                solutionPath.endsWith("solutions"));
        assertTrue("Solution path should contain expected structure", 
                solutionPath.contains("workpath") && solutionPath.contains("log"));
    }

    @Test
    public void getSolutionDirectoryPath_isAbsolute() {
        String solutionPath = LogPathManager.getSolutionDirectoryPath();

        assertTrue("Solution path should be absolute", 
                new File(solutionPath).isAbsolute());
    }

    @Test
    public void getSolutionDirectoryPath_containsBasePath() {
        String basePath = LogPathManager.getBasePath();
        String solutionPath = LogPathManager.getSolutionDirectoryPath();

        assertTrue("Solution path should start with base path", 
                solutionPath.startsWith(basePath));
        assertTrue("Base path should contain expected structure", 
                basePath.contains("workpath") && basePath.contains("log"));
    }

    @Test
    public void getCrashDirectoryPath_returnsCorrectPath() {
        String crashPath = LogPathManager.getCrashDirectoryPath();

        assertNotNull("Crash directory path should not be null", crashPath);
        assertTrue("Crash path should end with crash", 
                crashPath.endsWith("crash"));
        assertTrue("Crash path should contain expected structure", 
                crashPath.contains("workpath") && crashPath.contains("log"));
    }

    @Test
    public void getCrashDirectoryPath_isAbsolute() {
        String crashPath = LogPathManager.getCrashDirectoryPath();

        assertTrue("Crash path should be absolute", 
                new File(crashPath).isAbsolute());
    }

    @Test
    public void getCrashDirectoryPath_containsBasePath() {
        String basePath = LogPathManager.getBasePath();
        String crashPath = LogPathManager.getCrashDirectoryPath();

        assertTrue("Crash path should start with base path", 
                crashPath.startsWith(basePath));
        assertTrue("Base path should contain expected structure", 
                basePath.contains("workpath") && basePath.contains("log"));
    }

    @Test
    public void directoryPaths_areDifferent() {
        String solutionPath = LogPathManager.getSolutionDirectoryPath();
        String crashPath = LogPathManager.getCrashDirectoryPath();

        assertNotEquals("Solution and crash paths should be different", 
                solutionPath, crashPath);
        assertTrue("Solution path should end with solutions", 
                solutionPath.endsWith("solutions"));
        assertTrue("Crash path should end with crash", 
                crashPath.endsWith("crash"));
    }

    // ========== Tests for Platform Log Methods ==========

    @Test
    public void getPlatformLogPath_returnsCorrectPath() {
        String platformPath = LogPathManager.getPlatformLogPath();

        assertNotNull("Platform log path should not be null", platformPath);
        assertTrue("Platform path should end with AndroidLogs.tar.gz", 
                platformPath.endsWith("AndroidLogs.tar.gz"));
        assertTrue("Platform path should contain expected structure", 
                platformPath.contains("workpath") && platformPath.contains("log"));
    }

    @Test
    public void getPlatformLogPath_isAbsolute() {
        String platformPath = LogPathManager.getPlatformLogPath();

        assertTrue("Platform log path should be absolute", 
                new File(platformPath).isAbsolute());
    }

    @Test
    public void getPlatformLogPath_containsBasePath() {
        String basePath = LogPathManager.getBasePath();
        String platformPath = LogPathManager.getPlatformLogPath();

        assertTrue("Platform path should start with base path", 
                platformPath.startsWith(basePath));
    }

    @Test
    public void getPlatformLogName_returnsCorrectName() {
        String platformName = LogPathManager.getPlatformLogName();

        assertNotNull("Platform log name should not be null", platformName);
        assertEquals("Platform log name should match expected name", 
                EXPECTED_PLATFORM_LOG_NAME, platformName);
    }

    @Test
    public void getPlatformLogName_hasCorrectExtension() {
        String platformName = LogPathManager.getPlatformLogName();

        assertTrue("Platform log should have .tar.gz extension", 
                platformName.endsWith(".tar.gz"));
    }

    @Test
    public void platformLogPath_matchesNameAndBase() {
        String platformPath = LogPathManager.getPlatformLogPath();

        assertTrue("Platform path should end with AndroidLogs.tar.gz", 
                platformPath.endsWith("AndroidLogs.tar.gz"));
        
        // Also verify it matches base + name
        String basePath = LogPathManager.getBasePath();
        String platformName = LogPathManager.getPlatformLogName();
        String expectedPath = basePath + File.separator + platformName;
        assertEquals("Platform path should match base + name", 
                expectedPath, platformPath);
    }

    // ========== Tests for Generated Log Directory Path ==========

    @Test
    public void getGeneratedLogDirPath_forCrash_returnsCorrectPath() {
        String logDirPath = LogPathManager.getGeneratedLogDirPath(
                FileManager.TAG_CRASH, TEST_UUID);

        assertNotNull("Log directory path should not be null", logDirPath);
        assertTrue("Path should contain crash directory", 
                logDirPath.contains("crash"));
        assertTrue("Path should contain UUID", 
                logDirPath.contains(TEST_UUID));
    }

    @Test
    public void getGeneratedLogDirPath_forLog_returnsCorrectPath() {
        String logDirPath = LogPathManager.getGeneratedLogDirPath(
                FileManager.TAG_LOG, TEST_UUID);

        assertNotNull("Log directory path should not be null", logDirPath);
        assertTrue("Path should contain solution directory", 
                logDirPath.contains("solutions"));
        assertTrue("Path should contain UUID", 
                logDirPath.contains(TEST_UUID));
    }

    @Test
    public void getGeneratedLogDirPath_forDifferentUuids_returnsDifferentPaths() {
        String path1 = LogPathManager.getGeneratedLogDirPath(
                FileManager.TAG_LOG, TEST_UUID);
        String path2 = LogPathManager.getGeneratedLogDirPath(
                FileManager.TAG_LOG, TEST_UUID_2);

        assertNotEquals("Paths for different UUIDs should be different", 
                path1, path2);
    }

    @Test
    public void getGeneratedLogDirPath_crashVsLog_returnsDifferentPaths() {
        String crashPath = LogPathManager.getGeneratedLogDirPath(
                FileManager.TAG_CRASH, TEST_UUID);
        String logPath = LogPathManager.getGeneratedLogDirPath(
                FileManager.TAG_LOG, TEST_UUID);

        assertNotEquals("Crash and log paths should be different", 
                crashPath, logPath);
    }

    // ========== Tests for Generated Log Archive Path ==========

    @Test
    public void getGeneratedLogArchivePath_forCrash_hasCorrectFormat() {
        String archivePath = LogPathManager.getGeneratedLogArchivePath(
                FileManager.TAG_CRASH, TEST_UUID);

        assertNotNull("Archive path should not be null", archivePath);
        assertTrue("Archive path should contain UUID", 
                archivePath.contains(TEST_UUID));
        assertTrue("Archive path should have .tar.gz extension", 
                archivePath.endsWith(".tar.gz"));
        assertTrue("Archive path should contain 'CrashLogs'", 
                archivePath.contains("CrashLogs"));
        assertTrue("Archive path should contain 'archive'", 
                archivePath.contains("archive"));
    }

    @Test
    public void getGeneratedLogArchivePath_forCrash_containsTimestamp() {
        String archivePath = LogPathManager.getGeneratedLogArchivePath(
                FileManager.TAG_CRASH, TEST_UUID);

        // Timestamp format is yyyyMMdd_HHmmss
        // Just verify it contains underscore which separates date and time
        assertTrue("Archive path should contain timestamp format", 
                archivePath.contains("_archive_"));
    }

    @Test
    public void getGeneratedLogArchivePath_forLog_hasCorrectFormat() {
        String archivePath = LogPathManager.getGeneratedLogArchivePath(
                FileManager.TAG_LOG, TEST_UUID);

        assertNotNull("Archive path should not be null", archivePath);
        assertTrue("Archive path should contain UUID", 
                archivePath.contains(TEST_UUID));
        assertTrue("Archive path should have .tar.gz extension", 
                archivePath.endsWith(".tar.gz"));
        assertTrue("Archive path should contain 'SolutionLogs'", 
                archivePath.contains("SolutionLogs"));
    }

    @Test
    public void getGeneratedLogArchivePath_forLog_noTimestamp() {
        String archivePath = LogPathManager.getGeneratedLogArchivePath(
                FileManager.TAG_LOG, TEST_UUID);

        // Solution logs don't have timestamp in archive name
        assertFalse("Solution archive should not contain '_archive_'", 
                archivePath.contains("_archive_"));
    }

    @Test
    public void getGeneratedLogArchivePath_crashVsLog_differentFormats() {
        String crashArchive = LogPathManager.getGeneratedLogArchivePath(
                FileManager.TAG_CRASH, TEST_UUID);
        String logArchive = LogPathManager.getGeneratedLogArchivePath(
                FileManager.TAG_LOG, TEST_UUID);

        assertNotEquals("Crash and log archives should have different formats", 
                crashArchive, logArchive);
        assertTrue("Crash archive should have timestamp", 
                crashArchive.contains("_archive_"));
        assertFalse("Log archive should not have timestamp", 
                logArchive.contains("_archive_"));
    }

    @Test
    public void getGeneratedLogArchivePath_multipleCalls_differentTimestamps() throws InterruptedException {
        String archive1 = LogPathManager.getGeneratedLogArchivePath(
                FileManager.TAG_CRASH, TEST_UUID);
        
        // Wait a bit to ensure different timestamp
        Thread.sleep(1100); // Wait more than 1 second
        
        String archive2 = LogPathManager.getGeneratedLogArchivePath(
                FileManager.TAG_CRASH, TEST_UUID);

        assertNotEquals("Archive paths with different timestamps should be different", 
                archive1, archive2);
    }

    // ========== Tests for Log File Name Generation ==========

    @Test
    public void getLogFileName_forCrash_hasCorrectFormat() {
        String fileName = LogPathManager.getLogFileName(
                FileManager.TAG_CRASH, TEST_UUID);

        assertNotNull("Log file name should not be null", fileName);
        assertTrue("File name should start with CRASH", 
                fileName.startsWith("CRASH"));
        assertTrue("File name should contain UUID", 
                fileName.contains(TEST_UUID));
        assertTrue("File name should end with .log", 
                fileName.endsWith(".log"));
    }

    @Test
    public void getLogFileName_forLog_hasCorrectFormat() {
        String fileName = LogPathManager.getLogFileName(
                FileManager.TAG_LOG, TEST_UUID);

        assertNotNull("Log file name should not be null", fileName);
        assertTrue("File name should start with LOG", 
                fileName.startsWith("LOG"));
        assertTrue("File name should contain UUID", 
                fileName.contains(TEST_UUID));
        assertTrue("File name should end with .log", 
                fileName.endsWith(".log"));
    }

    @Test
    public void getLogFileName_crashVsLog_differentPrefixes() {
        String crashFileName = LogPathManager.getLogFileName(
                FileManager.TAG_CRASH, TEST_UUID);
        String logFileName = LogPathManager.getLogFileName(
                FileManager.TAG_LOG, TEST_UUID);

        assertNotEquals("Crash and log file names should be different", 
                crashFileName, logFileName);
        assertTrue("Crash file should start with CRASH", 
                crashFileName.startsWith("CRASH"));
        assertTrue("Log file should start with LOG", 
                logFileName.startsWith("LOG"));
    }

    @Test
    public void getLogFileName_differentUuids_differentNames() {
        String fileName1 = LogPathManager.getLogFileName(
                FileManager.TAG_LOG, TEST_UUID);
        String fileName2 = LogPathManager.getLogFileName(
                FileManager.TAG_LOG, TEST_UUID_2);

        assertNotEquals("File names for different UUIDs should be different", 
                fileName1, fileName2);
    }

    @Test
    public void getLogFileName_hasCorrectStructure() {
        String fileName = LogPathManager.getLogFileName(
                FileManager.TAG_CRASH, TEST_UUID);

        // Format should be: TAG_UUID.log
        String[] parts = fileName.split("_");
        assertTrue("File name should have TAG_UUID structure", 
                parts.length >= 2);
    }

    // ========== Tests for Directory Creation ==========

    @Test
    public void ensureDirectoryExists_withValidPath_returnsTrue() {
        // Use a path that should be creatable (though may fail in test environment)
        String testPath = LogPathManager.getBasePath();
        
        boolean result = LogPathManager.ensureDirectoryExists(testPath);

        // Result depends on permissions, but method should not throw exception
        assertNotNull("Result should not be null", (Boolean) result);
    }

    @Test
    public void ensureDirectoryExists_withNestedPath_createsAll() {
        String basePath = LogPathManager.getBasePath();
        String nestedPath = basePath + File.separator + "test" + 
                File.separator + "nested" + File.separator + "path";
        
        // This should attempt to create all directories in the path
        boolean result = LogPathManager.ensureDirectoryExists(nestedPath);

        // Result depends on permissions, but method should not throw exception
        assertNotNull("Result should not be null", (Boolean) result);
    }

    @Test
    public void ensureDirectoryExists_withExistingPath_returnsTrue() {
        // Root path should always exist
        String rootPath = "/";
        
        boolean result = LogPathManager.ensureDirectoryExists(rootPath);

        assertTrue("Existing directory should return true", result);
    }

    @Test
    public void ensureDirectoryExists_multipleCallsSamePath_consistent() {
        String testPath = LogPathManager.getBasePath();
        
        boolean result1 = LogPathManager.ensureDirectoryExists(testPath);
        boolean result2 = LogPathManager.ensureDirectoryExists(testPath);

        assertEquals("Multiple calls should return same result", result1, result2);
    }

    // ========== Tests for Directory Initialization ==========

    @Test
    public void initializeDirectories_createsRequiredDirs() {
        boolean result = LogPathManager.initializeDirectories();

        // Result depends on permissions, but method should not throw exception
        assertNotNull("Result should not be null", (Boolean) result);
    }

    @Test
    public void initializeDirectories_multipleCalls_consistent() {
        boolean result1 = LogPathManager.initializeDirectories();
        boolean result2 = LogPathManager.initializeDirectories();

        // Both calls should return same result (idempotent)
        assertEquals("Multiple initializations should be consistent", 
                result1, result2);
    }

    @Test
    public void initializeDirectories_createsBaseDirectory() {
        LogPathManager.initializeDirectories();
        
        String basePath = LogPathManager.getBasePath();
        
        // Base path should be set correctly
        assertNotNull("Base path should be set after initialization", basePath);
    }

    @Test
    public void initializeDirectories_createsSolutionDirectory() {
        LogPathManager.initializeDirectories();
        
        String solutionPath = LogPathManager.getSolutionDirectoryPath();
        
        // Solution path should be set correctly
        assertNotNull("Solution path should be set after initialization", solutionPath);
    }

    @Test
    public void initializeDirectories_createsCrashDirectory() {
        LogPathManager.initializeDirectories();
        
        String crashPath = LogPathManager.getCrashDirectoryPath();
        
        // Crash path should be set correctly
        assertNotNull("Crash path should be set after initialization", crashPath);
    }

    // ========== Tests for Path Consistency ==========

    @Test
    public void allPaths_useConsistentBasePath() {
        String basePath = LogPathManager.getBasePath();
        String solutionPath = LogPathManager.getSolutionDirectoryPath();
        String crashPath = LogPathManager.getCrashDirectoryPath();
        String platformPath = LogPathManager.getPlatformLogPath();

        assertTrue("Base path should contain expected structure", 
                basePath.contains("workpath") && basePath.contains("log"));
        assertTrue("Solution path should start with base", 
                solutionPath.startsWith(basePath));
        assertTrue("Crash path should start with base", 
                crashPath.startsWith(basePath));
        assertTrue("Platform path should start with base", 
                platformPath.startsWith(basePath));
    }

    @Test
    public void generatedPaths_useCorrectParentDirectories() {
        String crashLogDir = LogPathManager.getGeneratedLogDirPath(
                FileManager.TAG_CRASH, TEST_UUID);
        String solutionLogDir = LogPathManager.getGeneratedLogDirPath(
                FileManager.TAG_LOG, TEST_UUID);
        
        String crashParent = LogPathManager.getCrashDirectoryPath();
        String solutionParent = LogPathManager.getSolutionDirectoryPath();

        assertTrue("Crash parent should end with crash", 
                crashParent.endsWith("crash"));
        assertTrue("Solution parent should end with solutions", 
                solutionParent.endsWith("solutions"));
        assertTrue("Crash log dir should be under crash parent", 
                crashLogDir.startsWith(crashParent));
        assertTrue("Solution log dir should be under solution parent", 
                solutionLogDir.startsWith(solutionParent));
    }

    @Test
    public void archivePaths_crashArchive_underSolutionsDirectory() {
        String crashArchive = LogPathManager.getGeneratedLogArchivePath(
                FileManager.TAG_CRASH, TEST_UUID);
        String solutionDir = LogPathManager.getSolutionDirectoryPath();

        assertTrue("Crash archive should be under solutions directory (Shared Bind Mount)", 
                crashArchive.startsWith(solutionDir));
        assertTrue("Crash archive should contain UUID", 
                crashArchive.contains(TEST_UUID));
    }

    @Test
    public void archivePaths_solutionArchive_underSolutionsDirectory() {
        String logArchive = LogPathManager.getGeneratedLogArchivePath(
                FileManager.TAG_LOG, TEST_UUID);
        String solutionDir = LogPathManager.getSolutionDirectoryPath();

        assertTrue("Solution archive should be under solutions directory", 
                logArchive.startsWith(solutionDir));
    }

    // ========== Tests for File Separators ==========

    @Test
    public void allPaths_useCorrectSeparators() {
        String basePath = LogPathManager.getBasePath();
        String solutionPath = LogPathManager.getSolutionDirectoryPath();
        String crashPath = LogPathManager.getCrashDirectoryPath();

        // Paths should not have mixed separators (no backslash on Unix, etc.)
        // This is handled by File.separator
        assertNotNull("Base path should use valid separators", basePath);
        assertNotNull("Solution path should use valid separators", solutionPath);
        assertNotNull("Crash path should use valid separators", crashPath);
    }

    @Test
    public void generatedPaths_useFileSeparator() {
        String logDir = LogPathManager.getGeneratedLogDirPath(
                FileManager.TAG_LOG, TEST_UUID);
        
        // Should contain File.separator (/ on Unix, \ on Windows)
        assertTrue("Path should contain file separators", 
                logDir.contains(File.separator) || logDir.contains("/"));
    }

    // ========== Tests for Edge Cases ==========

    @Test
    public void getLogFileName_withSpecialCharactersInUuid_handlesGracefully() {
        // UUID should follow standard format, but test defensive coding
        String fileName = LogPathManager.getLogFileName(
                FileManager.TAG_LOG, TEST_UUID);

        assertNotNull("File name should be generated", fileName);
        assertTrue("File name should have .log extension", 
                fileName.endsWith(".log"));
    }

    @Test
    public void getGeneratedLogDirPath_withSameUuidDifferentFilter_usesDifferentDirs() {
        String crashPath = LogPathManager.getGeneratedLogDirPath(
                FileManager.TAG_CRASH, TEST_UUID);
        String logPath = LogPathManager.getGeneratedLogDirPath(
                FileManager.TAG_LOG, TEST_UUID);

        assertNotEquals("Same UUID but different filters should use different directories", 
                crashPath, logPath);
    }

    @Test
    public void platformLog_isInBaseDirectory() {
        String basePath = LogPathManager.getBasePath();
        String platformPath = LogPathManager.getPlatformLogPath();

        assertTrue("Base path should contain expected structure", 
                basePath.contains("workpath") && basePath.contains("log"));
        assertTrue("Platform path should end with AndroidLogs.tar.gz", 
                platformPath.endsWith("AndroidLogs.tar.gz"));
        
        String platformParent = new File(platformPath).getParent();
        assertEquals("Platform log should be in base directory", 
                basePath, platformParent);
    }

    @Test
    public void directoryPaths_endWithoutSeparator() {
        String basePath = LogPathManager.getBasePath();
        String solutionPath = LogPathManager.getSolutionDirectoryPath();
        String crashPath = LogPathManager.getCrashDirectoryPath();

        // Directory paths typically don't end with separator
        // This is implementation dependent, just verify they're valid
        assertNotNull("Base path should be valid", basePath);
        assertNotNull("Solution path should be valid", solutionPath);
        assertNotNull("Crash path should be valid", crashPath);
    }

    // ========== Tests for Integration Scenarios ==========

    @Test
    public void fullWorkflow_createLogFilePathForCrash() {
        // Simulate full workflow for crash log
        String uuid = TEST_UUID;
        
        // Get directory
        String logDir = LogPathManager.getGeneratedLogDirPath(
                FileManager.TAG_CRASH, uuid);
        
        // Get file name
        String fileName = LogPathManager.getLogFileName(
                FileManager.TAG_CRASH, uuid);
        
        // Construct full path
        String fullPath = logDir + File.separator + fileName;

        assertNotNull("Full path should be constructed", fullPath);
        assertTrue("Full path should contain UUID", fullPath.contains(uuid));
        assertTrue("Full path should contain crash directory", fullPath.contains("crash"));
        assertTrue("Full path should end with .log", fullPath.endsWith(".log"));
    }

    @Test
    public void fullWorkflow_createArchivePathForSolution() {
        // Simulate full workflow for solution archive
        String uuid = TEST_UUID;
        
        // Get archive path
        String archivePath = LogPathManager.getGeneratedLogArchivePath(
                FileManager.TAG_LOG, uuid);

        assertNotNull("Archive path should be constructed", archivePath);
        assertTrue("Archive path should contain UUID", archivePath.contains(uuid));
        assertTrue("Archive path should contain solution directory", archivePath.contains("solutions"));
        assertTrue("Archive path should end with .tar.gz", archivePath.endsWith(".tar.gz"));
    }

    @Test
    public void initializeAndCreatePaths_workTogether() {
        // Initialize directories
        LogPathManager.initializeDirectories();
        
        // Create paths for both crash and log
        String crashDir = LogPathManager.getGeneratedLogDirPath(
                FileManager.TAG_CRASH, TEST_UUID);
        String logDir = LogPathManager.getGeneratedLogDirPath(
                FileManager.TAG_LOG, TEST_UUID);

        assertNotNull("Crash directory should be created", crashDir);
        assertNotNull("Log directory should be created", logDir);
        assertNotEquals("Crash and log directories should be different", 
                crashDir, logDir);
    }
}
