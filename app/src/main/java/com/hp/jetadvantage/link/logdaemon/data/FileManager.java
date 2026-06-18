package com.hp.jetadvantage.link.logdaemon.data;

import android.os.Build;
import android.util.Log;

import com.hp.jetadvantage.link.logdaemon.util.FileUtils;
import com.hp.jetadvantage.link.logdaemon.util.LogPathManager;
import com.hp.jetadvantage.link.logdaemon.util.UuidValidator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FileManager {
    public static final String TAG = "[LD][FILE]";

    // Default file path - now using LogPathManager
    public static final String DEFAULT_FILE_PATH = LogPathManager.getBasePath();

    // ========== Tag Constants ==========
    public static final String TAG_CRASH = "CRASH";
    public static final String TAG_LOG = "LOG";
    public static final String TAG_PLATFORM = "PLATFORM";
    public static final String INIT_UUID = "00000000-0000-0000-0000-000000000000";
    public static final String UUID_ALL = "all";

    // ========== File Operation Constants ==========
    private static final int LOG_FILE_EXTENSION_LENGTH = ".log".length(); // 4 characters
    private static final int FILE_COPY_BUFFER_SIZE = 8192; // 8KB optimal buffer size
    
    // ========== Compression Constants ==========
    private static final int COMPRESSION_LEVEL_MAXIMUM = 9; // Maximum compression level
    
    // ========== Crash Log Management Constants ==========
    private static final int MAX_CRASH_LOG_FILES = 10;
    
    // ========== Timeout Constants ==========
    private static final int ZIP_CREATION_TIMEOUT_SECONDS = 30;
    private static final int ZIP_CREATION_SLEEP_MS = 1000;
    
    // ========== Log Parsing Constants ==========
    public static final int LOG_ARG_SIZE = 3;
    public static final int LOG_APP_INFO_POSITION = 2;
    public static final int LOG_APP_UUID_POSITION = 1;  // Changed from 2 to 1 - UUID is at index 1 after splitting by "/"
    
    // ========== Permission Constants ==========
    public static final int PERMISSION_CREATE_ZIP = 0;
    public static final int PERMISSION_WRITE_DATE = 1;

    // ========== Instance Variables ==========
    // Customizable file path
    private String mFilePath;
    private static volatile String sDefaultFilePath = DEFAULT_FILE_PATH;

    /*For saving log/fatal file*/
    private String mFileName = null;
    private RingBufferRunner mRingBuffer = null;
    private String mFilter = "";

    /*For saving fatal flag*/
    private boolean mFatalPackNameFlag = false;

    //App Information
    private String mPackageUuid = "";


    public FileManager(String tag, RingBufferRunner ring) {
        this(tag, ring, null, DEFAULT_FILE_PATH);
    }

    public FileManager(String tag, RingBufferRunner ring, String uuid) {
        this(tag, ring, uuid, DEFAULT_FILE_PATH);
    }

    /**
     * Constructor with customizable file path
     *
     * @param tag            Log type tag (CRASH or LOG)
     * @param ring           RingBufferRunner for log data
     * @param uuid           UUID for log identification (can be null for FATAL logs)
     * @param fileDir Custom path for log files storage
     */
    public FileManager(String tag, RingBufferRunner ring, String uuid, String fileDir) {
        mRingBuffer = ring;
        mFilter = tag;
        mPackageUuid = uuid;
        mFilePath = (fileDir != null && !fileDir.trim().isEmpty())
                ? fileDir : DEFAULT_FILE_PATH;

        if (uuid == null) {
            mFatalPackNameFlag = true;
            startSavingFile();
        } else {
            deleteFile(mFilePath, mFilter, uuid);
            if (UuidValidator.isValidUuid(uuid) || UUID_ALL.equals(uuid))
                startSavingFile();
            else
                Log.i(TAG, "this is wrong request : " + uuid);
        }
    }

    /**
     * Get current file path
     *
     * @return current file path being used
     */
    public String getFilePath() {
        return mFilePath;
    }

    /**
     * Set custom file path
     *
     * @param customPath new path for file storage
     */
    public void setFilePath(String customPath) {
        if (customPath != null && !customPath.trim().isEmpty()) {
            mFilePath = customPath;
        }
    }

    /**
     * Set default file path for static methods
     *
     * @param defaultPath new default path
     */
    public static void setDefaultFilePath(String defaultPath) {
        if (defaultPath != null && !defaultPath.trim().isEmpty()) {
            sDefaultFilePath = defaultPath;
        }
    }

    /**
     * Get current default file path
     *
     * @return current default file path
     */
    public static String getDefaultFilePath() {
        return sDefaultFilePath;
    }

    /*Need to move to service*/
    public void startSavingFile() {
        Log.i(TAG, "~~~~~~~~~~~ startSavingFile : " + mFilter + " ~~~~~~~~~~~~~~");
        if (mRingBuffer == null) {
            Log.i(TAG, "LogBuffer is null. can't make LogFile");
            return;
        }
        mFileName = LogPathManager.getLogFileName(mFilter, mPackageUuid);
        mSavingFileRunnable.run();
    }

    private Runnable mSavingFileRunnable = this::writeDataInFile;

    private File setFileDirectory() {
        // Use LogPathManager for directory paths
        String directoryPath = TAG_CRASH.equals(mFilter) ?
                LogPathManager.getCrashDirectoryPath() :
                LogPathManager.getSolutionDirectoryPath();

        // Handle null or empty UUID case (e.g., initial CRASH logs before UUID extraction)
        File fileDir;
        if (mPackageUuid == null || mPackageUuid.trim().isEmpty()) {
            // Use base directory directly when UUID is not yet known
            fileDir = new File(directoryPath);
            Log.d(TAG, "Setting file directory (no UUID): " + fileDir.getAbsolutePath());
        } else {
            // Create subdirectory with UUID
            fileDir = new File(directoryPath, mPackageUuid);
            Log.d(TAG, "Setting file directory: " + fileDir.getAbsolutePath());
        }

        // Ensure directory exists using LogPathManager
        LogPathManager.ensureDirectoryExists(directoryPath);

        return fileDir;
    }

    private synchronized void writeDataInFile() {
        File fileDir = setFileDirectory();
        LogWritingContext context = prepareWritingContext(fileDir);

        if (context == null) {
            return;
        }

        boolean shouldCreateArchive = false;

        try (FileWriter fileWriter = new FileWriter(context.logFile, true)) {
            int writtenCount = processLogEntries(fileWriter, context.logData);
            finalizeLogFile(fileWriter, writtenCount);

            shouldCreateArchive = shouldCreateZipFile();

            changePermission(context.logFile, PERMISSION_WRITE_DATE);
        } catch (IOException e) {
            Log.e(TAG, "IOException : " + Log.getStackTraceString(e));
        } catch (Exception e) {
            Log.e(TAG, "Exception : " + Log.getStackTraceString(e));
        } finally {
            changePermission(fileDir, PERMISSION_WRITE_DATE);
        }

        // Create archive after FileWriter is completely closed
        if (shouldCreateArchive) {
            Log.i(TAG, "FileWriter closed, now creating archive file");
            createArchiveFile(fileDir);
        }
    }

    /**
     * Context class to hold log writing parameters
     */
    private static class LogWritingContext {
        final File logFile;
        final ArrayList<String> logData;
        final int totalSize;

        LogWritingContext(File logFile, ArrayList<String> logData, int totalSize) {
            this.logFile = logFile;
            this.logData = logData;
            this.totalSize = totalSize;
        }
    }

    /**
     * Prepare context for log writing
     */
    private LogWritingContext prepareWritingContext(File fileDir) {
        int size = mRingBuffer.size();
        if (size == 0) {
            Log.i(TAG, "Ring buffer is empty, nothing to write");
            return null;
        }

        ArrayList<String> savedLogData = new ArrayList<>(mRingBuffer.getRingBuffer());
        File logFile = new File(fileDir, mFileName);

        Log.i(TAG, "Starting to write " + size + " log entries, filter: " + mFilter);

        return new LogWritingContext(logFile, savedLogData, size);
    }

    /**
     * Process all log entries and write them to file
     */
    private int processLogEntries(FileWriter fileWriter, ArrayList<String> logData) throws IOException {
        int count = 0;

        for (String log : logData) {
            if (log == null) {
                Log.w(TAG, "Log entry is null, skipping");
                continue;
            }

            if (shouldWriteLogEntry(log, fileWriter)) {
                fileWriter.append(log).append("\n");
                count++;
            }
        }

        logData.clear();
        return count;
    }

    /**
     * Determine if a log entry should be written based on filter
     */
    private boolean shouldWriteLogEntry(String log, FileWriter fileWriter) throws IOException {
        switch (mFilter) {
            case TAG_CRASH:
                return handleFatalLog(log);
            case TAG_LOG:
                return handleRegularLog(log, fileWriter);
            default:
                Log.w(TAG, "Unknown filter type: " + mFilter);
                return true;
        }
    }

    /**
     * Handle fatal log processing
     */
    private boolean handleFatalLog(String log) {
        if (!mFatalPackNameFlag) {
            return true;
        }

        String extractedUuid = getUuidFromLog(log);
        if (!"".equals(extractedUuid)) {
            mPackageUuid = extractedUuid;
            return true;
        } else {
            Log.w(TAG, "Could not extract UUID from log: " + log.substring(0, Math.min(log.length(), 100)));
            return false;
        }
    }

    /**
     * Handle regular log processing
     */
    private boolean handleRegularLog(String log, FileWriter fileWriter) throws IOException {
        if (mPackageUuid.equals("")) {
            Log.w(TAG, "UUID is empty, writing warning message");
            fileWriter.append("UUID information is null\n");
            return false; // Don't write the actual log entry
        } else if (UUID_ALL.equals(mPackageUuid)) {
            return true;
        } else {
            return checkUuid(log, mPackageUuid);
        }
    }

    /**
     * Finalize log file writing
     */
    private void finalizeLogFile(FileWriter fileWriter, int writtenCount) throws IOException {
        Log.i(TAG, "Output File size : " + writtenCount);

        if (writtenCount == 0) {
            String noDataMessage = "No matching log entries found for filter: " + mFilter +
                    ", packageUuid: " + mPackageUuid + "\n";
            fileWriter.append(noDataMessage);
            Log.w(TAG, noDataMessage);
        }
    }

    /**
     * Check if zip file should be created
     */
    private boolean shouldCreateZipFile() {
        if (TAG_CRASH.equals(mFilter) && "".equals(mPackageUuid)) {
            Log.i(TAG, "This is not 3rdApp. can't make file");
            return false;
        }
        return true;
    }

    /**
     * Create archive file based on filter type
     */
    private void createArchiveFile(File fileDir) {
        if (TAG_CRASH.equals(mFilter)) {
            changeFileName(fileDir, mFileName, mPackageUuid);
        } else {
            createZipFile(mPackageUuid);
        }
    }

    @SuppressWarnings("java:S899")
    public void changePermission(File file, int tag) {
        if (tag == PERMISSION_WRITE_DATE) {
            file.setReadable(true, false);
            file.setExecutable(true, false);
            file.setWritable(true, false);
        } else if (tag == PERMISSION_CREATE_ZIP) {
            if (Build.VERSION.SDK_INT == 29) {
                Log.i(TAG, "Create zip for Q");
                Path path = Paths.get(mFilePath, file.getName());
                Path dirPath = Paths.get(mFilePath);
                Log.i(TAG, "path : " + path);
                Set<PosixFilePermission> posixFilePermissions = PosixFilePermissions.fromString("rw-rw----");
                Set<PosixFilePermission> posixDirPermissions = PosixFilePermissions.fromString("rwxrwxrwx");
                try {
                    Files.setPosixFilePermissions(path, posixFilePermissions);
                    Files.setPosixFilePermissions(dirPath, posixDirPermissions);
                } catch (IOException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            } else {
                file.setReadable(true, false);
                file.setWritable(true, false);
                file.setExecutable(false, false);

                File dir = new File(mFilePath);
                dir.setReadable(true, false);
                dir.setWritable(true, false);
                dir.setExecutable(true, false);
            }
        }
    }

    public void changeFileName(File fileDir, String fileName, String uuid) {
        Log.d(TAG, "Moving and renaming CRASH log file for UUID: " + uuid);
        Log.d(TAG, "Source file name: " + fileName);
        Log.d(TAG, "Source directory: " + fileDir.getAbsolutePath());

        File sourceFile = new File(fileDir, fileName);

        if (!sourceFile.exists()) {
            Log.e(TAG, "Source file does not exist: " + sourceFile.getAbsolutePath());
            return;
        }

        File result = addCrashLogFile(sourceFile, uuid);
        if (result != null) {
            Log.i(TAG, "SUCCESS! CRASH file moved and renamed to: " + result.getAbsolutePath());
        } else {
            Log.e(TAG, "FAIL! Could not move CRASH file for UUID: " + uuid);
        }
    }

    /**
     * Add a file as the newest crash log (_1.log) for the given UUID,
     * applying FIFO rotation on existing crash files.
     *
     * @param sourceFile The source file to add
     * @param uuid       The solution UUID
     * @return The resulting crash file (CRASH_{uuid}_1.log), or null on failure
     */
    public static File addCrashLogFile(File sourceFile, String uuid) {
        if (sourceFile == null || !sourceFile.exists()) {
            Log.e(TAG, "Source file does not exist for addCrashLogFile");
            return null;
        }

        if (!UuidValidator.isValidUuid(uuid)) {
            Log.e(TAG, "(addCrashLogFile) Invalid UUID format: " + uuid);
            return null;
        }

        String crashDirPath = LogPathManager.getCrashDirectoryPath();
        File uuidCrashDir = new File(crashDirPath, uuid);

        if (!LogPathManager.ensureDirectoryExists(uuidCrashDir.getAbsolutePath())) {
            Log.e(TAG, "Failed to create UUID crash directory: " + uuidCrashDir.getAbsolutePath());
            return null;
        }

        manageCrashLogFiles(uuid);

        String newFileName = TAG_CRASH + "_" + uuid + "_1.log";
        File targetFile = new File(uuidCrashDir, newFileName);

        if (targetFile.exists()
                && !FileUtils.deleteFileWithDescription(targetFile, TAG_CRASH)) {
            Log.e(TAG, "Failed to delete existing target file: " + targetFile.getAbsolutePath());
            return null;
        }

        if (FileUtils.moveFile(sourceFile, targetFile)) {
            Log.i(TAG, "Crash log file added: " + targetFile.getAbsolutePath());
            return targetFile;
        }

        Log.e(TAG, "Failed to add crash log file");
        return null;
    }


    /**
     * Manage crash log files for a specific UUID
     * - Keep maximum 10 files
     * - Renumber files: 1 = newest, 10 = oldest
     * - Delete oldest when exceeding 10 files
     *
     * @param uuid The UUID to manage crash log files for
     */
    public static void manageCrashLogFiles(String uuid) {
        try {
            Log.i(TAG, "Managing crash log files for UUID: " + uuid);

            // Get all existing crash files for this UUID
            List<CrashLogFile> existingFiles = getExistingCrashLogFiles(uuid);

            if (existingFiles.isEmpty()) {
                Log.i(TAG, "No existing crash files found for UUID: " + uuid);
                return;
            }

            // Sort by current sequence number (1 is newest, higher numbers are older)
            existingFiles.sort((f1, f2) -> Integer.compare(f1.sequenceNumber, f2.sequenceNumber));

            Log.i(TAG, "Found " + existingFiles.size() + " existing crash files for UUID: " + uuid);
            for (CrashLogFile file : existingFiles) {
                Log.d(TAG, "Existing file: " + file.file.getName() + " (sequence: " + file.sequenceNumber + ")");
            }

            // Since we're adding a new file as #1, we need to make room
            // If we already have MAX_CRASH_LOG_FILES, delete the oldest one before renumbering
            while (existingFiles.size() >= MAX_CRASH_LOG_FILES) {
                CrashLogFile oldestFile = existingFiles.get(existingFiles.size() - 1);
                Log.i(TAG, "Deleting oldest crash file to make room: " + oldestFile.file.getName());

                if (FileUtils.deleteFileWithDescription(oldestFile.file, TAG_CRASH)) {
                    Log.i(TAG, "Successfully deleted: " + oldestFile.file.getName());
                    existingFiles.remove(existingFiles.size() - 1);
                } else {
                    Log.w(TAG, "Failed to delete: " + oldestFile.file.getName());
                    break; // Don't get stuck in infinite loop if deletion fails
                }
            }

            // Now renumber remaining files: shift all numbers up by 1
            // This makes room for the new file to be numbered as 1
            renumberCrashLogFiles(uuid, existingFiles);

        } catch (Exception e) {
            Log.e(TAG, "Error managing crash log files for UUID " + uuid + ": " + Log.getStackTraceString(e));
        }
    }

    /**
     * Renumber existing crash log files to make room for new file
     * New file will be 1, so shift existing files: 1->2, 2->3, etc.
     *
     * @param uuid          The UUID of the crash log files
     * @param existingFiles List of existing crash log files (sorted by sequence number)
     */
    private static void renumberCrashLogFiles(String uuid, List<CrashLogFile> existingFiles) {
        String crashDirPath = LogPathManager.getCrashDirectoryPath();
        File uuidCrashDir = new File(crashDirPath, uuid);

        // Renumber in reverse order to avoid conflicts
        // Start from the highest number and work down
        for (int i = existingFiles.size() - 1; i >= 0; i--) {
            CrashLogFile crashFile = existingFiles.get(i);
            int newSequenceNumber = crashFile.sequenceNumber + 1;

            String newFileName = TAG_CRASH + "_" + uuid + "_" + newSequenceNumber + ".log";
            File newFile = new File(uuidCrashDir, newFileName);

            if (crashFile.file.renameTo(newFile)) {
                Log.d(TAG, "Renumbered: " + crashFile.file.getName() + " -> " + newFileName);
            } else {
                Log.w(TAG, "Failed to renumber: " + crashFile.file.getName() + " -> " + newFileName);
            }
        }
    }

    /**
     * Get all existing crash log files for a specific UUID
     *
     * @param uuid The UUID to search for
     * @return List of CrashLogFile objects representing existing files
     */
    private static List<CrashLogFile> getExistingCrashLogFiles(String uuid) {
        List<CrashLogFile> crashFiles = new ArrayList<>();
        String crashDirPath = LogPathManager.getCrashDirectoryPath();
        File uuidCrashDir = new File(crashDirPath, uuid);

        if (!uuidCrashDir.exists() || !uuidCrashDir.isDirectory()) {
            return crashFiles;
        }

        File[] files = uuidCrashDir.listFiles();
        if (files == null) {
            Log.w(TAG, "Cannot list files in UUID crash directory: " + uuidCrashDir.getAbsolutePath());
            return crashFiles;
        }

        String searchPattern = TAG_CRASH + "_" + uuid + "_";

        for (File file : files) {
            if (file.isFile() && file.getName().startsWith(searchPattern) && file.getName().endsWith(".log")) {
                try {
                    // Extract sequence number from filename
                    String fileName = file.getName();
                    String numberPart = fileName.substring(searchPattern.length(), 
                            fileName.length() - LOG_FILE_EXTENSION_LENGTH);
                    int sequenceNumber = Integer.parseInt(numberPart);

                    crashFiles.add(new CrashLogFile(file, sequenceNumber));
                    Log.d(TAG, "Found crash file: " + fileName + " (sequence: " + sequenceNumber + ")");

                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid crash file name format: " + file.getName());
                }
            }
        }

        return crashFiles;
    }

    /**
     * Helper class to represent a crash log file with its sequence number
     */
    private static class CrashLogFile {
        final File file;
        final int sequenceNumber;

        CrashLogFile(File file, int sequenceNumber) {
            this.file = file;
            this.sequenceNumber = sequenceNumber;
        }
    }

    private static List<String> fileSearch(String path, String filter, String uuid) {
        List<String> searchFileList = new ArrayList<>();
        String searchText = filter + "_" + uuid;

        File dir = new File(path);

        Log.d(TAG, "Searching for files in directory: " + dir.getAbsolutePath() + " with pattern: " + searchText);

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().contains(searchText)) {
                    Log.d(TAG, "Found file: " + file.getName());
                    searchFileList.add(file.getName());
                }
            }
        } else {
            Log.w(TAG, "Directory does not exist or cannot be read: " + dir.getAbsolutePath());
        }

        Log.d(TAG, "Found " + searchFileList.size() + " files matching pattern");
        return searchFileList;
    }

    private static String findOldestFile(List<String> fileList) {
        if (fileList != null && !fileList.isEmpty()) {
            String oldestFileName = fileList.get(0);

            // Use LogPathManager for crash directory path
            File crashDir = new File(LogPathManager.getCrashDirectoryPath());

            File oldestFile = new File(crashDir, oldestFileName);
            long oldestModifiedTime = oldestFile.lastModified();

            for (String fileName : fileList) {
                File currentFile = new File(crashDir, fileName);
                long currentModifiedTime = currentFile.lastModified();

                if (currentModifiedTime < oldestModifiedTime) {
                    oldestFileName = fileName;
                    oldestModifiedTime = currentModifiedTime;
                }
            }
            return oldestFileName;
        }
        return null;
    }

    /**
     * Extract UUID information array from log line
     * @param logLine The log line to parse
     * @return String array containing parsed information, or empty array if parsing failed
     */
    private String[] extractUuidInfoFromLog(String logLine) {
        if (logLine == null || logLine.trim().isEmpty()) {
            return new String[0];
        }
        
        String[] line = logLine.split("\\s", LOG_ARG_SIZE + 1);
        if (line.length > LOG_APP_INFO_POSITION) {
            String[] info = line[LOG_APP_INFO_POSITION].split("/");
            if (info.length > LOG_APP_UUID_POSITION) {
                return info;
            }
        }
        return new String[0];
    }

    /**
     * Check if log line contains the specified UUID
     * @param base The log line to check
     * @param id The UUID to match
     * @return true if UUID matches, false otherwise
     */
    private boolean checkUuid(String base, String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }

        String[] info = extractUuidInfoFromLog(base);
        if (info.length == 0) {
            return false;
        }
        
        return id.equals(info[LOG_APP_UUID_POSITION]);
    }

    /**
     * Extract UUID from log line
     * @param base The log line to parse
     * @return The extracted UUID, or empty string if not found
     */
    private String getUuidFromLog(String base) {
        String[] info = extractUuidInfoFromLog(base);
        if (info.length > 0) {
            mFatalPackNameFlag = false;
            return info[LOG_APP_UUID_POSITION];
        }
        
        return "";
    }

    public static void deleteFile(String filePath, String filter, String uuid) {

        File dir = new File(filePath);

        String fileName = LogPathManager.getLogFileName(filter, uuid);
        File file = new File(dir, fileName);

        if (TAG_CRASH.equals(filter)) {
            List<String> searchFileList = fileSearch(filePath, filter, uuid);
            if (searchFileList.size() > MAX_CRASH_LOG_FILES) {
                fileName = findOldestFile(searchFileList);
                file = new File(dir, fileName);
            }
        }

        Log.d(TAG, "Deleting log file before creating: " + file.getAbsolutePath());
        FileUtils.deleteFileWithDescription(file, filter);

        // Delete corresponding archive file
        fileName = LogPathManager.getGeneratedLogArchivePath(filter, uuid);
        file = new File(fileName);
        Log.d(TAG, "Deleting archive file before creating: " + file.getAbsolutePath());
        FileUtils.deleteFileWithDescription(file, filter);
    }

    public static void deleteAllFile() {
        // Use LogPathManager for directory paths
        File logDir = new File(LogPathManager.getSolutionDirectoryPath());
        deleteDirectoryContents(logDir, TAG_LOG);
    }

    /**
     * Delete all contents of a directory
     *
     * @param directory The directory to clean
     * @param folderType Type description for logging
     */
    private static void deleteDirectoryContents(File directory, String folderType) {
        if (!directory.exists()) {
            Log.i(TAG, folderType + " directory does not exist: " + directory.getAbsolutePath());
            return;
        }

        // First, delete files in all subdirectories
        File[] childDirList = directory.listFiles(File::isDirectory);
        if (childDirList != null) {
            int iDir = 0;
            for (File childDir : childDirList) {
                Log.i(TAG, "[" + iDir + "] " + folderType + " subdirectory: " + childDir.getName());
                deleteFilesInDirectory(childDir, folderType);
                FileUtils.deleteFileWithDescription(childDir, folderType);
                iDir++;
            }
        }

        // Then, delete files in the root directory
        deleteFilesInDirectory(directory, folderType);
    }

    /**
     * Delete all files in a single directory (non-recursive)
     *
     * @param directory The directory to clean
     * @param folderType Type description for logging
     */
    private static void deleteFilesInDirectory(File directory, String folderType) {
        File[] childFileList = directory.listFiles(File::isFile);
        if (childFileList == null) {
            Log.w(TAG, "Cannot list files in " + folderType + " directory: " + directory.getAbsolutePath());
            return;
        }

        int deletedCount = 0;
        for (int i = 0; i < childFileList.length; i++) {
            File childFile = childFileList[i];
            String fileName = childFile.getName();
            
            Log.d(TAG, "[" + i + "] " + folderType + " file: " + fileName);
            
            if (FileUtils.deleteFileWithDescription(childFile, folderType)) {
                deletedCount++;
            }
        }
        
        Log.i(TAG, "Deleted " + deletedCount + " files from " + folderType + " directory: " + directory.getAbsolutePath());
    }

    /**
     * Create zip file with improved resource management and error handling
     *
     * @param uuid UUID for file identification
     * @return The path of the created zip file, or null if failed
     */
    public String createZipFile(String uuid) {
        // ========== Early Validation ==========
        // Validate UUID format
        if (uuid == null || uuid.trim().isEmpty()) {
            Log.e(TAG, "Invalid UUID: null or empty");
            return null;
        }

        // Get paths
        String path = LogPathManager.getGeneratedLogDirPath(mFilter, uuid);
        String archiveFilePath = LogPathManager.getGeneratedLogArchivePath(mFilter, uuid);

        // Validate directory exists
        File directory = new File(path);
        if (!directory.exists() || !directory.isDirectory()) {
            Log.w(TAG, "Directory does not exist or is not a directory: " + path);
            return null;
        }

        // Search for files to compress
        List<String> searchFileList = fileSearch(path, mFilter, uuid);
        if (searchFileList.isEmpty()) {
            Log.w(TAG, "No files found for UUID: " + uuid + " in directory: " + path);
            return null;
        }

        Log.i(TAG, "Found " + searchFileList.size() + " file(s) to compress for UUID: " + uuid);

        // ========== File Processing ==========
        // Convert file names to full paths
        searchFileList = convertToFullPaths(searchFileList, path);

        Log.i(TAG, "Output archive file name : " + archiveFilePath);
        File archiveFile = new File(archiveFilePath);

        // Execute tar command with proper resource management
        if (createCompressedArchive(searchFileList, archiveFile)) {
            changePermission(archiveFile, PERMISSION_CREATE_ZIP);

            if (!TAG_CRASH.equals(mFilter)) {
                waitForZipCreation(archiveFile);
            }

            Log.d(TAG, "Zip file created successfully: " + archiveFilePath);
            return archiveFilePath;
        } else {
            Log.e(TAG, "Failed to create zip file: " + archiveFilePath);
            return null;
        }
    }

    /**
     * Convert relative file names to full paths
     */
    private List<String> convertToFullPaths(List<String> fileNames, String basePath) {
        List<String> fullPaths = new ArrayList<>();
        for (String fileName : fileNames) {
            File fullPathFile = new File(basePath, fileName);
            fullPaths.add(fullPathFile.getAbsolutePath());
        }
        return fullPaths;
    }

    /**
     * Create compressed archive using Java compression libraries
     *
     * @param filePath  List of file names to compress
     * @param outputFile Output archive file
     * @return true if successful, false otherwise
     */
    private boolean createCompressedArchive(List<String> filePath, File outputFile) {

        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(
                Files.newOutputStream(outputFile.toPath()))) {

            zos.setLevel(COMPRESSION_LEVEL_MAXIMUM);

            if (!addFilesToArchive(filePath, zos)) {
                cleanupFailedArchive(outputFile);
                return false;
            }

            zos.finish();
            return validateArchiveCreation(outputFile);

        } catch (java.io.IOException e) {
            Log.e(TAG, "IOException during archive creation: " + Log.getStackTraceString(e));
            cleanupFailedArchive(outputFile);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error during archive creation: " + Log.getStackTraceString(e));
            return false;
        }
    }

    /**
     * Add files to the zip archive
     *
     * @param filePath List of file names to add
     * @param zos       ZipOutputStream to write to
     * @return true if all files were added successfully
     */
    private boolean addFilesToArchive(List<String> filePath, java.util.zip.ZipOutputStream zos) {
        try {
            for (String file : filePath) {
                if (!addSingleFileToArchive(file, zos)) {
                    return false;
                }
            }
            return true;
        } catch (java.io.IOException e) {
            Log.e(TAG, "Error adding files to archive: " + Log.getStackTraceString(e));
            return false;
        }
    }

    /**
     * Add a single file to the archive
     *
     * @param filePath  File name to add
     * @param zos       ZipOutputStream to write to
     * @return true if file was added successfully, false if there was an error
     */
    private boolean addSingleFileToArchive(String filePath, java.util.zip.ZipOutputStream zos)
            throws java.io.IOException {
        File sourceFile = new File(filePath);
        if (!sourceFile.exists()) {
            Log.w(TAG, "Skipping non-existent file: " + sourceFile.getAbsolutePath());
            return true; // Skip missing files, don't fail the entire operation
        }

        if (!sourceFile.canRead()) {
            Log.e(TAG, "Cannot read file: " + sourceFile.getAbsolutePath());
            return false; // File exists but cannot be read - this is an error
        }

        if (sourceFile.length() == 0) {
            Log.w(TAG, "File is empty, skipping: " + sourceFile.getAbsolutePath());
            return true; // Empty files are skipped but not considered an error
        }

        Log.d(TAG, "Adding file to archive: " + filePath);

        try {
            // Create zip entry
            java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(sourceFile.getName());
            entry.setTime(sourceFile.lastModified());
            zos.putNextEntry(entry);

            // Copy file content
            try (java.io.FileInputStream fis = new java.io.FileInputStream(sourceFile);
                 java.io.BufferedInputStream bis = new java.io.BufferedInputStream(fis)) {

                byte[] buffer = new byte[FILE_COPY_BUFFER_SIZE];
                int bytesRead;
                long totalBytesRead = 0;

                while ((bytesRead = bis.read(buffer)) != -1) {
                    zos.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }

                // Verify that we read the expected amount of data
                if (totalBytesRead != sourceFile.length()) {
                    Log.e(TAG, "File size mismatch for " + sourceFile.getName() +
                            ": expected " + sourceFile.length() + " bytes, read " + totalBytesRead + " bytes");
                    return false;
                }

                Log.d(TAG, "Successfully added " + totalBytesRead + " bytes from " + sourceFile.getName());
            }

            zos.closeEntry();
            return true;

        } catch (java.io.FileNotFoundException e) {
            Log.e(TAG, "File not found during archive creation: " + sourceFile.getAbsolutePath());
            return false;
        } catch (java.io.IOException e) {
            Log.e(TAG, "IO error while adding file to archive: " + sourceFile.getName() + " - " + e.getMessage());
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error while adding file to archive: " + sourceFile.getName() + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * Validate that the archive was created successfully
     *
     * @param outputFile Output file to validate
     * @return true if archive exists and has content
     */
    private boolean validateArchiveCreation(File outputFile) {
        if (outputFile.exists() && outputFile.length() > 0) {
            Log.i(TAG, "Archive created successfully: " + outputFile.getAbsolutePath() + " (size: " + outputFile.length() + " bytes)");
            return true;
        } else {
            Log.e(TAG, "Archive file was not created or is empty: " + outputFile.getAbsolutePath());
            return false;
        }
    }

    /**
     * Clean up failed archive file
     *
     * @param outputFile File to clean up
     */
    private void cleanupFailedArchive(File outputFile) {
        if (outputFile.exists()) {
            try {
                Files.delete(outputFile.toPath());
                Log.d(TAG, "Cleaned up partial archive file: " + outputFile.getAbsolutePath());
            } catch (java.nio.file.NoSuchFileException e) {
                Log.w(TAG, "Archive file already deleted: " + outputFile.getAbsolutePath());
            } catch (java.nio.file.DirectoryNotEmptyException e) {
                Log.e(TAG, "Cannot delete archive file, directory not empty: " + outputFile.getAbsolutePath());
            } catch (java.nio.file.AccessDeniedException e) {
                Log.e(TAG, "Access denied when deleting archive file: " + outputFile.getAbsolutePath());
            } catch (java.io.IOException e) {
                Log.e(TAG, "IO error when deleting archive file: " + outputFile.getAbsolutePath() + " - " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error when deleting archive file: " + outputFile.getAbsolutePath() + " - " + e.getMessage());
            }
        }
    }

    /**
     * Wait for zip file creation and send POST request
     *
     * @param outputFile File of the created file
     */
    private void waitForZipCreation(File outputFile) {

        try {
            Thread.sleep(ZIP_CREATION_SLEEP_MS);
        } catch (InterruptedException e) {
            Log.i(TAG, "Thread interrupted during zip creation sleep");
            Thread.currentThread().interrupt();
            return;
        }

        int attemptCount = 0;

        while (attemptCount < ZIP_CREATION_TIMEOUT_SECONDS && !Thread.currentThread().isInterrupted()) {
            try {
                if (outputFile.exists()) {
                    Log.i(TAG, "Zip file created successfully: " + outputFile.getAbsolutePath());
                    return;
                } else {
                    Log.d(TAG, outputFile.getAbsolutePath() + " does not exist yet, attempt " + (attemptCount + 1));
                    Thread.sleep(ZIP_CREATION_SLEEP_MS);
                    attemptCount++;
                }
            } catch (InterruptedException e) {
                Log.i(TAG, "Thread interrupted while waiting for zip file creation");
                Thread.currentThread().interrupt();
                return;
            }
        }

        Log.e(TAG, "Timeout waiting for zip file creation: " + outputFile.getAbsolutePath());
    }
}
