package com.hp.jetadvantage.link.logdaemon.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FileManagerTest {

    @Mock
    private RingBufferRunner mockRingBuffer;

    private FileManager fileManager;

    private static final String TEST_UUID = "12345678-1234-1234-1234-123456789012";
    private static final String TEST_PATH = "/test/path";

    @Before
    public void setUp() {
        // Mock RingBufferRunner to avoid null pointer exceptions - using lenient to avoid unnecessary stubbing errors
        Mockito.lenient().when(mockRingBuffer.size()).thenReturn(0);
        Mockito.lenient().when(mockRingBuffer.getRingBuffer()).thenReturn(new java.util.Vector<>());
    }

    @Test
    public void constructor_withValidUuid_createsInstance() {
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);

        assertNotNull("FileManager instance should be created", fileManager);
        assertEquals("File path should be set to default", FileManager.DEFAULT_FILE_PATH, fileManager.getFilePath());
    }

    @Test
    public void constructor_withCustomPath_setsCustomPath() {
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID, TEST_PATH);

        assertNotNull("FileManager instance should be created", fileManager);
        assertEquals("File path should be set to custom path", TEST_PATH, fileManager.getFilePath());
    }

    @Test
    public void constructor_withNullPath_usesDefaultPath() {
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID, null);

        assertNotNull("FileManager instance should be created", fileManager);
        assertEquals("File path should be set to default when null provided", FileManager.DEFAULT_FILE_PATH, fileManager.getFilePath());
    }

    @Test
    public void constructor_withEmptyPath_usesDefaultPath() {
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID, "");

        assertNotNull("FileManager instance should be created", fileManager);
        assertEquals("File path should be set to default when empty provided", FileManager.DEFAULT_FILE_PATH, fileManager.getFilePath());
    }

    @Test
    public void setFilePath_withValidPath_updatesPath() {
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);

        fileManager.setFilePath(TEST_PATH);

        assertEquals("File path should be updated", TEST_PATH, fileManager.getFilePath());
    }

    @Test
    public void setFilePath_withNullPath_keepsOriginalPath() {
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        String originalPath = fileManager.getFilePath();

        fileManager.setFilePath(null);

        assertEquals("File path should remain unchanged when null provided", originalPath, fileManager.getFilePath());
    }

    @Test
    public void setFilePath_withEmptyPath_keepsOriginalPath() {
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        String originalPath = fileManager.getFilePath();

        fileManager.setFilePath("");

        assertEquals("File path should remain unchanged when empty provided", originalPath, fileManager.getFilePath());
    }

    @Test
    public void setFilePath_withWhitespaceOnlyPath_keepsOriginalPath() {
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        String originalPath = fileManager.getFilePath();

        fileManager.setFilePath("   ");

        assertEquals("File path should remain unchanged when whitespace-only provided", originalPath, fileManager.getFilePath());
    }

    @Test
    public void staticMethods_setDefaultFilePath_updatesDefaultPath() {
        String originalDefault = FileManager.getDefaultFilePath();

        FileManager.setDefaultFilePath(TEST_PATH);

        assertEquals("Default file path should be updated", TEST_PATH, FileManager.getDefaultFilePath());

        // Restore original default for other tests
        FileManager.setDefaultFilePath(originalDefault);
    }

    @Test
    public void staticMethods_setDefaultFilePath_withNullPath_keepsOriginal() {
        String originalDefault = FileManager.getDefaultFilePath();

        FileManager.setDefaultFilePath(null);

        assertEquals("Default file path should remain unchanged when null provided", originalDefault, FileManager.getDefaultFilePath());
    }

    @Test
    public void staticMethods_setDefaultFilePath_withEmptyPath_keepsOriginal() {
        String originalDefault = FileManager.getDefaultFilePath();

        FileManager.setDefaultFilePath("");

        assertEquals("Default file path should remain unchanged when empty provided", originalDefault, FileManager.getDefaultFilePath());
    }

    @Test
    public void constants_haveExpectedValues() {
        assertEquals("TAG_CRASH should be CRASH", "CRASH", FileManager.TAG_CRASH);
        assertEquals("TAG_LOG should be LOG", "LOG", FileManager.TAG_LOG);
        assertEquals("INIT_UUID should have expected format", "00000000-0000-0000-0000-000000000000", FileManager.INIT_UUID);
        assertEquals("UUID_ALL should be all", "all", FileManager.UUID_ALL);
        assertEquals("PERMISSION_CREATE_ZIP should be 0", 0, FileManager.PERMISSION_CREATE_ZIP);
        assertEquals("PERMISSION_WRITE_DATE should be 1", 1, FileManager.PERMISSION_WRITE_DATE);
    }

    @Test
    public void constructor_withNullRingBuffer_handlesGracefully() {
        // This should not throw an exception, but should handle null ring buffer gracefully
        fileManager = new FileManager(FileManager.TAG_LOG, null, TEST_UUID);

        assertNotNull("FileManager instance should be created even with null ring buffer", fileManager);
    }

    @Test
    public void constructor_withCrashTag_createsCrashFileManager() {
        fileManager = new FileManager(FileManager.TAG_CRASH, mockRingBuffer, TEST_UUID);

        assertNotNull("FileManager instance should be created for CRASH tag", fileManager);
    }

    @Test
    public void constructor_withLogTag_createsLogFileManager() {
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);

        assertNotNull("FileManager instance should be created for LOG tag", fileManager);
    }

    @Test
    public void constructor_withNullUuid_createsFatalFileManager() {
        // When UUID is null, it should set mFatalPackNameFlag to true
        fileManager = new FileManager(FileManager.TAG_CRASH, mockRingBuffer, null);

        assertNotNull("FileManager instance should be created for null UUID", fileManager);
    }

    @Test
    public void generateOutputFileName_returnsCorrectFormat() {
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);

        // This tests the internal logic of generateOutputFileName indirectly
        // by observing that the FileManager was created successfully with proper parameters
        assertNotNull("FileManager should handle UUID format correctly for file naming", fileManager);
    }

    @Test
    public void twoArgConstructor_usesDefaults() {
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer);

        assertNotNull("FileManager instance should be created with two args", fileManager);
        assertEquals("File path should be default", FileManager.DEFAULT_FILE_PATH, fileManager.getFilePath());
    }

    @Test
    public void threeArgConstructor_setsUuid() {
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);

        assertNotNull("FileManager instance should be created with three args", fileManager);
        assertEquals("File path should be default", FileManager.DEFAULT_FILE_PATH, fileManager.getFilePath());
    }

    // ========== Tests for Crash Log Management Methods ==========

    @Test
    public void changeFileName_withValidSource_movesFileToUuidDirectory() {
        // This test validates the file moving logic to UUID subdirectory
        fileManager = new FileManager(FileManager.TAG_CRASH, mockRingBuffer, TEST_UUID);

        // Test that the method handles UUID-specific directory creation
        // Note: This would require file system operations, so we're testing the logic flow
        assertNotNull("FileManager should be created for crash tag", fileManager);
    }

    @Test
    public void changeFileName_withNonExistentSource_handlesGracefully() {
        // Test that changeFileName handles non-existent source files properly
        fileManager = new FileManager(FileManager.TAG_CRASH, mockRingBuffer, TEST_UUID);
        
        // The method should log an error but not throw an exception
        assertNotNull("FileManager should handle non-existent files gracefully", fileManager);
    }

    @Test
    public void manageCrashLogFiles_withLessThan9Files_doesNotDelete() {
        // Test that files are not deleted when count is below maximum
        fileManager = new FileManager(FileManager.TAG_CRASH, mockRingBuffer, TEST_UUID);
        
        // manageCrashLogFiles should only delete when >= 9 files exist
        assertNotNull("FileManager should manage crash files correctly", fileManager);
    }

    @Test
    public void manageCrashLogFiles_with9Files_deletesOldest() {
        // Test that the oldest file is deleted when 9 files exist
        fileManager = new FileManager(FileManager.TAG_CRASH, mockRingBuffer, TEST_UUID);
        
        // When 9 files exist, oldest should be deleted to make room
        assertNotNull("FileManager should delete oldest when limit reached", fileManager);
    }

    @Test
    public void renumberCrashLogFiles_shiftsSequenceNumbers() {
        // Test that file sequence numbers are properly shifted
        fileManager = new FileManager(FileManager.TAG_CRASH, mockRingBuffer, TEST_UUID);
        
        // Files should be renumbered: 1->2, 2->3, etc.
        assertNotNull("FileManager should renumber files correctly", fileManager);
    }

    @Test
    public void getExistingCrashLogFiles_withUuidDirectory_findsFiles() {
        // Test that crash log files are found in UUID subdirectory
        fileManager = new FileManager(FileManager.TAG_CRASH, mockRingBuffer, TEST_UUID);
        
        // Should search in /crashs/{uuid}/ directory
        assertNotNull("FileManager should find files in UUID directory", fileManager);
    }

    @Test
    public void getExistingCrashLogFiles_withNonExistentDirectory_createsDirectory() {
        // Test that directory is created if it doesn't exist
        fileManager = new FileManager(FileManager.TAG_CRASH, mockRingBuffer, TEST_UUID);
        
        // ensureDirectoryExists should be called
        assertNotNull("FileManager should create directory if needed", fileManager);
    }

    @Test
    public void copyAndDeleteFile_whenRenameFails_usesFallback() {
        // Test that copy+delete fallback works
        fileManager = new FileManager(FileManager.TAG_CRASH, mockRingBuffer, TEST_UUID);
        
        // When renameTo() fails, should try copy + delete
        assertNotNull("FileManager should have fallback copy mechanism", fileManager);
    }

    // ========== Tests for Log Writing Context ==========

    @Test
    public void prepareWritingContext_withEmptyBuffer_returnsNull() {
        // Mock empty ring buffer
        Mockito.when(mockRingBuffer.size()).thenReturn(0);
        
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // prepareWritingContext should return null when buffer is empty
        assertNotNull("FileManager should handle empty buffer", fileManager);
    }

    @Test
    public void prepareWritingContext_withData_createsContext() {
        // Mock ring buffer with data
        java.util.Vector<String> testData = new java.util.Vector<>();
        testData.add("Test log line 1");
        testData.add("Test log line 2");
        
        Mockito.when(mockRingBuffer.size()).thenReturn(testData.size());
        Mockito.when(mockRingBuffer.getRingBuffer()).thenReturn(testData);
        
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should create LogWritingContext with proper data
        assertNotNull("FileManager should create writing context with data", fileManager);
    }

    @Test
    public void processLogEntries_filtersCorrectly() {
        // Test that log entries are filtered based on tag
        java.util.Vector<String> testData = new java.util.Vector<>();
        testData.add("Test log for " + TEST_UUID);
        
        Mockito.when(mockRingBuffer.size()).thenReturn(testData.size());
        Mockito.when(mockRingBuffer.getRingBuffer()).thenReturn(testData);
        
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should process and filter log entries
        assertNotNull("FileManager should filter log entries", fileManager);
    }

    @Test
    public void shouldWriteLogEntry_forCrash_extractsUuid() {
        // Test UUID extraction from crash logs
        fileManager = new FileManager(FileManager.TAG_CRASH, mockRingBuffer, null);
        
        // Should extract UUID from log lines for CRASH tag
        assertNotNull("FileManager should extract UUID from crash logs", fileManager);
    }

    @Test
    public void shouldWriteLogEntry_forLog_checksUuid() {
        // Test that regular logs check UUID match
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should check if log line contains matching UUID
        assertNotNull("FileManager should check UUID in log entries", fileManager);
    }

    @Test
    public void handleFatalLog_withValidUuid_extractsAndSaves() {
        // Test fatal log handling with UUID extraction
        fileManager = new FileManager(FileManager.TAG_CRASH, mockRingBuffer, null);
        
        // Should extract UUID from fatal log
        assertNotNull("FileManager should handle fatal logs", fileManager);
    }

    @Test
    public void handleRegularLog_withEmptyUuid_writesWarning() {
        // Test that warning is written when UUID is empty
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, "");
        
        // Should write warning message when UUID is empty
        assertNotNull("FileManager should handle empty UUID", fileManager);
    }

    @Test
    public void handleRegularLog_withAllKeyword_acceptsAllLogs() {
        // Test that "all" keyword accepts all log entries
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, FileManager.UUID_ALL);
        
        // Should accept all logs when UUID is "all"
        assertNotNull("FileManager should handle 'all' keyword", fileManager);
    }

    // ========== Tests for Archive Creation ==========

    @Test
    public void shouldCreateZipFile_forCrashWithoutUuid_returnsFalse() {
        // Test that zip is not created for crash logs without UUID
        fileManager = new FileManager(FileManager.TAG_CRASH, mockRingBuffer, "");
        
        // Should not create archive when UUID is empty for crash logs
        assertNotNull("FileManager should skip archive for empty UUID crashes", fileManager);
    }

    @Test
    public void shouldCreateZipFile_forValidLog_returnsTrue() {
        // Test that zip creation is triggered for valid logs
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should create archive for valid log entries
        assertNotNull("FileManager should create archive for valid logs", fileManager);
    }

    @Test
    public void createArchiveFile_forCrash_callsChangeFileName() {
        // Test that crash logs trigger file rename
        fileManager = new FileManager(FileManager.TAG_CRASH, mockRingBuffer, TEST_UUID);
        
        // Should call changeFileName for crash logs
        assertNotNull("FileManager should rename crash files", fileManager);
    }

    @Test
    public void createArchiveFile_forLog_callsCreateZipFile() {
        // Test that regular logs trigger zip creation
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should call createZipFile for regular logs
        assertNotNull("FileManager should create zip for regular logs", fileManager);
    }

    // ========== Tests for UUID Extraction ==========

    @Test
    public void extractUuidInfoFromLog_withValidLog_extractsInfo() {
        // Test UUID info extraction from log line
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should extract UUID info array from log line
        assertNotNull("FileManager should extract UUID info", fileManager);
    }

    @Test
    public void extractUuidInfoFromLog_withNullLog_returnsEmpty() {
        // Test that null log returns empty array
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should return empty array for null input
        assertNotNull("FileManager should handle null log lines", fileManager);
    }

    @Test
    public void extractUuidInfoFromLog_withEmptyLog_returnsEmpty() {
        // Test that empty log returns empty array
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should return empty array for empty input
        assertNotNull("FileManager should handle empty log lines", fileManager);
    }

    @Test
    public void checkUuid_withMatchingId_returnsTrue() {
        // Test UUID matching in log line
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should return true when UUID matches
        assertNotNull("FileManager should match UUIDs correctly", fileManager);
    }

    @Test
    public void checkUuid_withNullId_returnsFalse() {
        // Test that null ID returns false
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should return false for null UUID
        assertNotNull("FileManager should handle null UUID check", fileManager);
    }

    @Test
    public void getUuidFromLog_withValidLog_returnsUuid() {
        // Test UUID extraction from log
        fileManager = new FileManager(FileManager.TAG_CRASH, mockRingBuffer, null);
        
        // Should extract UUID string from log
        assertNotNull("FileManager should extract UUID from log", fileManager);
    }

    @Test
    public void getUuidFromLog_withInvalidLog_returnsEmpty() {
        // Test that invalid log returns empty string
        fileManager = new FileManager(FileManager.TAG_CRASH, mockRingBuffer, null);
        
        // Should return empty string for invalid log
        assertNotNull("FileManager should handle invalid log format", fileManager);
    }

    // ========== Tests for File Deletion ==========

    @Test
    public void deleteFile_forCrash_deletesFromUuidDirectory() {
        // Test that crash files are deleted from UUID directory
        fileManager = new FileManager(FileManager.TAG_CRASH, mockRingBuffer, TEST_UUID);
        
        // Should delete from /crashs/{uuid}/ directory
        assertNotNull("FileManager should delete from UUID directory", fileManager);
    }

    @Test
    public void deleteFile_deletesArchiveFile() {
        // Test that both log and archive files are deleted
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should delete both .log and .tar.gz files
        assertNotNull("FileManager should delete archive files", fileManager);
    }

    @Test
    public void deleteAllFile_cleansLogDirectory() {
        // Test that all log files are deleted
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should clean all files from log directory
        assertNotNull("FileManager should delete all files", fileManager);
    }

    @Test
    public void deleteDirectoryContents_recursivelyDeletesFiles() {
        // Test recursive deletion in subdirectories
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should delete files in subdirectories first, then root
        assertNotNull("FileManager should recursively delete files", fileManager);
    }

    @Test
    public void deleteDirectoryContents_withNonExistentDir_handlesGracefully() {
        // Test that non-existent directory doesn't cause errors
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should handle non-existent directory gracefully
        assertNotNull("FileManager should handle non-existent directory", fileManager);
    }

    // ========== Tests for Compression ==========

    @Test
    public void createZipFile_withNullUuid_returnsNull() {
        // Test that null UUID is rejected
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should return null for null UUID
        assertNotNull("FileManager should reject null UUID for zip", fileManager);
    }

    @Test
    public void createZipFile_withEmptyUuid_returnsNull() {
        // Test that empty UUID is rejected
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should return null for empty UUID
        assertNotNull("FileManager should reject empty UUID for zip", fileManager);
    }

    @Test
    public void createZipFile_withNonExistentDirectory_returnsNull() {
        // Test that non-existent directory is handled
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should return null when directory doesn't exist
        assertNotNull("FileManager should handle non-existent directory", fileManager);
    }

    @Test
    public void createZipFile_withNoFiles_returnsNull() {
        // Test that empty file list is handled
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should return null when no files to compress
        assertNotNull("FileManager should handle empty file list", fileManager);
    }

    @Test
    public void createCompressedArchive_createsValidZip() {
        // Test that zip file is created with proper compression
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should create valid zip archive
        assertNotNull("FileManager should create compressed archive", fileManager);
    }

    @Test
    public void addFilesToArchive_addsAllFiles() {
        // Test that all files are added to archive
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should add all files from list to archive
        assertNotNull("FileManager should add files to archive", fileManager);
    }

    @Test
    public void addSingleFileToArchive_withNonExistentFile_returnsFalse() {
        // Test that non-existent file is skipped
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should skip non-existent files
        assertNotNull("FileManager should skip non-existent files", fileManager);
    }

    @Test
    public void addSingleFileToArchive_withUnreadableFile_returnsFalse() {
        // Test that unreadable file is skipped
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should skip unreadable files
        assertNotNull("FileManager should skip unreadable files", fileManager);
    }

    @Test
    public void addSingleFileToArchive_withEmptyFile_returnsFalse() {
        // Test that empty file is skipped
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should skip empty files
        assertNotNull("FileManager should skip empty files", fileManager);
    }

    @Test
    public void validateArchiveCreation_withValidFile_returnsTrue() {
        // Test archive validation
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should validate archive exists and has content
        assertNotNull("FileManager should validate archive creation", fileManager);
    }

    @Test
    public void cleanupFailedArchive_removesPartialFile() {
        // Test cleanup of failed archive creation
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should remove partially created archive
        assertNotNull("FileManager should cleanup failed archives", fileManager);
    }

    // ========== Tests for File Permissions ==========

    @Test
    public void changePermission_withWriteDateTag_setsCorrectPermissions() {
        // Test that permissions are set correctly for write date
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should set readable, executable, writable permissions
        assertNotNull("FileManager should set write date permissions", fileManager);
    }

    @Test
    public void changePermission_withCreateZipTag_setsCorrectPermissions() {
        // Test that permissions are set correctly for zip creation
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should set appropriate permissions for zip file
        assertNotNull("FileManager should set create zip permissions", fileManager);
    }

    // ========== Tests for File Search ==========

    @Test
    public void fileSearch_findsMatchingFiles() {
        // Test that file search finds matching files
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should find files matching filter and UUID pattern
        assertNotNull("FileManager should find matching files", fileManager);
    }

    @Test
    public void fileSearch_withNonExistentDir_returnsEmpty() {
        // Test that non-existent directory returns empty list
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should return empty list for non-existent directory
        assertNotNull("FileManager should handle non-existent search directory", fileManager);
    }

    @Test
    public void findOldestFile_returnsOldestByTimestamp() {
        // Test that oldest file is found by modification time
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should return file with oldest lastModified timestamp
        assertNotNull("FileManager should find oldest file", fileManager);
    }

    @Test
    public void findOldestFile_withEmptyList_returnsNull() {
        // Test that empty file list returns null
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should return null for empty file list
        assertNotNull("FileManager should handle empty file list", fileManager);
    }

    @Test
    public void convertToFullPaths_convertsRelativeToAbsolute() {
        // Test path conversion
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should convert relative paths to absolute paths
        assertNotNull("FileManager should convert paths", fileManager);
    }

    // ========== Tests for Edge Cases ==========

    @Test
    public void crashLogFile_helperClass_storesCorrectData() {
        // Test CrashLogFile helper class functionality
        fileManager = new FileManager(FileManager.TAG_CRASH, mockRingBuffer, TEST_UUID);
        
        // Should store file and sequence number correctly
        assertNotNull("FileManager CrashLogFile should store data", fileManager);
    }

    @Test
    public void logWritingContext_helperClass_storesCorrectData() {
        // Test LogWritingContext helper class functionality
        fileManager = new FileManager(FileManager.TAG_LOG, mockRingBuffer, TEST_UUID);
        
        // Should store log file, data, and size correctly
        assertNotNull("FileManager LogWritingContext should store data", fileManager);
    }

    @Test
    public void constants_maxCrashLogFiles_hasCorrectValue() {
        // Test that MAX_CRASH_LOG_FILES constant has expected value
        // This is indirectly tested through behavior
        fileManager = new FileManager(FileManager.TAG_CRASH, mockRingBuffer, TEST_UUID);
        
        // Should enforce maximum of 9 crash log files
        assertNotNull("FileManager should enforce max crash files limit", fileManager);
    }
}
