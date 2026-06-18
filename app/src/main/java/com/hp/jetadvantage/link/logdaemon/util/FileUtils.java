package com.hp.jetadvantage.link.logdaemon.util;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

/**
 * Utility class for file operations with improved error handling
 */
public class FileUtils {
    private static final String TAG = "[LD][FileUtils]";

    /**
     * Delete a file with improved error messages
     * @param file The file to delete
     * @return true if the file was successfully deleted, false otherwise
     */
    public static boolean deleteFile(File file) {
        if (file == null) {
            Log.w(TAG, "Cannot delete null file");
            return false;
        }

        if (!file.exists()) {
            Log.d(TAG, "File does not exist, nothing to delete: " + file.getAbsolutePath());
            return true; // Consider as success since the goal is achieved
        }

        try {
            Files.delete(file.toPath());
            Log.d(TAG, "Successfully deleted file: " + file.getAbsolutePath());
            return true;
        } catch (NoSuchFileException e) {
            Log.d(TAG, "File was already deleted: " + file.getAbsolutePath());
            return true; // Consider as success since the goal is achieved
        } catch (IOException e) {
            Log.e(TAG, "Failed to delete file: " + file.getAbsolutePath() + " - " + e.getMessage());
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error deleting file: " + file.getAbsolutePath() + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a file with improved error messages and custom logging
     * @param file The file to delete
     * @param description Description of the file for logging purposes
     * @return true if the file was successfully deleted, false otherwise
     */
    public static boolean deleteFileWithDescription(File file, String description) {
        if (file == null) {
            Log.w(TAG, "(" + description + ") Cannot delete null file");
            return false;
        }

        if (!file.exists()) {
            Log.d(TAG, "(" + description + ") does not exist, nothing to delete: " + file.getAbsolutePath());
            return true;
        }

        try {
            Files.delete(file.toPath());
            Log.d(TAG, "(" + description + ") Successfully deleted: " + file.getName());
            return true;
        } catch (NoSuchFileException e) {
            Log.w(TAG, "(" + description + ") File does not exist: " + file.getAbsolutePath());
            return false;
        } catch (DirectoryNotEmptyException e) {
            Log.e(TAG, "(" + description + ") Directory not empty: " + file.getAbsolutePath());
            return false;
        } catch (AccessDeniedException e) {
            Log.e(TAG, "(" + description + ") Access denied when deleting: " + file.getAbsolutePath());
            return false;
        } catch (java.io.IOException e) {
            Log.e(TAG, "(" + description + ") IO error when deleting file: " + file.getAbsolutePath() + ", reason: " + Log.getStackTraceString(e));
            return false;
        } catch (Exception e) {
            Log.e(TAG, "(" + description + ") Unexpected error when deleting file: " + file.getAbsolutePath() + ", error: " + Log.getStackTraceString(e));
            Log.e(TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    /**
     * Recursively delete a directory and all its contents
     * @param dir The directory to delete
     * @param description Description for logging purposes
     * @return true if all files and the directory were successfully deleted
     */
    public static boolean deleteDirectoryRecursively(File dir, String description) {
        if (dir == null || !dir.exists()) {
            return true;
        }

        boolean allSuccess = true;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    allSuccess &= deleteDirectoryRecursively(file, description);
                } else {
                    if (!deleteFileWithDescription(file, description)) {
                        Log.w(TAG, "(" + description + ") Failed to delete file: " + file.getName());
                        allSuccess = false;
                    }
                }
            }
        }

        try {
            Files.delete(dir.toPath());
        } catch (DirectoryNotEmptyException e) {
            Log.w(TAG, "(" + description + ") Directory not empty: " + dir.getAbsolutePath());
            allSuccess = false;
        } catch (IOException e) {
            Log.w(TAG, "(" + description + ") Failed to delete directory: " + dir.getAbsolutePath());
            allSuccess = false;
        }

        return allSuccess;
    }

    /**
     * Move a file from source to target location
     * @param sourceFile The source file to move
     * @param targetFile The target location
     * @return true if the file was successfully moved, false otherwise
     */
    public static boolean moveFile(File sourceFile, File targetFile) {
        if (sourceFile == null || targetFile == null) {
            Log.w(TAG, "Cannot move file with null source or target");
            return false;
        }

        if (!sourceFile.exists()) {
            Log.w(TAG, "Source file does not exist: " + sourceFile.getAbsolutePath());
            return false;
        }

        try {
            // Try rename first (faster if on same filesystem)
            if (sourceFile.renameTo(targetFile)) {
                Log.i(TAG, "Successfully moved file from " + sourceFile.getAbsolutePath() + " to " + targetFile.getAbsolutePath());
                return true;
            } else {
                // If rename fails, try copy and delete
                Files.copy(sourceFile.toPath(), targetFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                boolean deleted = deleteFile(sourceFile);
                if (deleted) {
                    Log.i(TAG, "Successfully copied and deleted file from " + sourceFile.getAbsolutePath() + " to " + targetFile.getAbsolutePath());
                    return true;
                } else {
                    Log.w(TAG, "File copied but failed to delete source: " + sourceFile.getAbsolutePath());
                    return false;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to move file from " + sourceFile.getAbsolutePath() + " to " + targetFile.getAbsolutePath() + " - " + e.getMessage());
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error moving file: " + e.getMessage());
            return false;
        }
    }
}
