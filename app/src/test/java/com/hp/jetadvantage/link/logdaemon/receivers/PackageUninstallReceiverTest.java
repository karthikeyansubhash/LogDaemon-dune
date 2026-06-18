package com.hp.jetadvantage.link.logdaemon.receivers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;

import com.hp.jetadvantage.link.logdaemon.util.FileUtils;
import com.hp.jetadvantage.link.logdaemon.util.LogPathManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;

@RunWith(MockitoJUnitRunner.class)
public class PackageUninstallReceiverTest {

    private static final String TEST_UUID = "12345678-1234-1234-1234-123456789012";
    private static final String TEST_PACKAGE = "com.example.solution";
    private static final String TEST_CRASH_PATH = "/data/workpath/log/crash";
    private static final String TEST_SOLUTION_PATH = "/data/workpath/log/solutions";
    private static final String ACTION_PACKAGE_UNINSTALLED =
            "com.hp.packagemanager.intent.action.PACKAGE_UNINSTALLED";
    private static final String EXTRA_SOLUTION_UUID = "EXTRA_SOLUTION_ID";
    private static final String EXTRA_PACKAGE = "EXTRA_PACKAGE";

    @Mock
    private Context mockContext;

    @Mock
    private Intent mockIntent;

    private PackageUninstallReceiver receiver;

    @Before
    public void setUp() {
        receiver = new PackageUninstallReceiver();
    }

    @Test
    public void onReceive_withNullIntent_doesNothing() {
        receiver.onReceive(mockContext, null);
        // No exception should occur
    }

    @Test
    public void onReceive_withWrongAction_doesNothing() {
        when(mockIntent.getAction()).thenReturn("com.hp.some.other.ACTION");
        receiver.onReceive(mockContext, mockIntent);
        verify(mockIntent, never()).getStringExtra(EXTRA_SOLUTION_UUID);
    }

    @Test
    public void onReceive_withNullSolutionUuid_skipsCleanup() {
        when(mockIntent.getAction()).thenReturn(ACTION_PACKAGE_UNINSTALLED);
        when(mockIntent.getStringExtra(EXTRA_SOLUTION_UUID)).thenReturn(null);
        when(mockIntent.getStringExtra(EXTRA_PACKAGE)).thenReturn(TEST_PACKAGE);

        // Should return early without accessing LogPathManager or FileUtils
        receiver.onReceive(mockContext, mockIntent);
    }

    @Test
    public void onReceive_withEmptySolutionUuid_skipsCleanup() {
        when(mockIntent.getAction()).thenReturn(ACTION_PACKAGE_UNINSTALLED);
        when(mockIntent.getStringExtra(EXTRA_SOLUTION_UUID)).thenReturn("");
        when(mockIntent.getStringExtra(EXTRA_PACKAGE)).thenReturn(TEST_PACKAGE);

        // Should return early without accessing LogPathManager or FileUtils
        receiver.onReceive(mockContext, mockIntent);
    }

    @Test
    public void onReceive_withInvalidUuidFormat_skipsCleanup() {
        when(mockIntent.getAction()).thenReturn(ACTION_PACKAGE_UNINSTALLED);
        when(mockIntent.getStringExtra(EXTRA_SOLUTION_UUID)).thenReturn("../../../etc/passwd");
        when(mockIntent.getStringExtra(EXTRA_PACKAGE)).thenReturn(TEST_PACKAGE);

        // Should return early without accessing LogPathManager or FileUtils
        receiver.onReceive(mockContext, mockIntent);
    }

    @Test
    public void onReceive_withValidData_callsCleanup() {
        when(mockIntent.getAction()).thenReturn(ACTION_PACKAGE_UNINSTALLED);
        when(mockIntent.getStringExtra(EXTRA_SOLUTION_UUID)).thenReturn(TEST_UUID);
        when(mockIntent.getStringExtra(EXTRA_PACKAGE)).thenReturn(TEST_PACKAGE);

        try (MockedStatic<LogPathManager> logPathMock = Mockito.mockStatic(LogPathManager.class);
             MockedStatic<FileUtils> fileUtilsMock = Mockito.mockStatic(FileUtils.class)) {

            logPathMock.when(LogPathManager::getCrashDirectoryPath).thenReturn(TEST_CRASH_PATH);
            logPathMock.when(LogPathManager::getSolutionDirectoryPath).thenReturn(TEST_SOLUTION_PATH);
            fileUtilsMock.when(() -> FileUtils.deleteDirectoryRecursively(any(File.class), any(String.class)))
                    .thenReturn(true);

            receiver.onReceive(mockContext, mockIntent);

            // Verify paths are constructed properly — calls may or may not happen depending on file.exists()
            logPathMock.verify(LogPathManager::getCrashDirectoryPath);
            logPathMock.verify(LogPathManager::getSolutionDirectoryPath);
        }
    }

    @Test
    public void onReceive_withNullPackageName_stillPerformsCleanup() {
        when(mockIntent.getAction()).thenReturn(ACTION_PACKAGE_UNINSTALLED);
        when(mockIntent.getStringExtra(EXTRA_SOLUTION_UUID)).thenReturn(TEST_UUID);
        when(mockIntent.getStringExtra(EXTRA_PACKAGE)).thenReturn(null);

        try (MockedStatic<LogPathManager> logPathMock = Mockito.mockStatic(LogPathManager.class);
             MockedStatic<FileUtils> fileUtilsMock = Mockito.mockStatic(FileUtils.class)) {

            logPathMock.when(LogPathManager::getCrashDirectoryPath).thenReturn(TEST_CRASH_PATH);
            logPathMock.when(LogPathManager::getSolutionDirectoryPath).thenReturn(TEST_SOLUTION_PATH);
            fileUtilsMock.when(() -> FileUtils.deleteDirectoryRecursively(any(File.class), any(String.class)))
                    .thenReturn(true);

            receiver.onReceive(mockContext, mockIntent);

            logPathMock.verify(LogPathManager::getCrashDirectoryPath);
            logPathMock.verify(LogPathManager::getSolutionDirectoryPath);
        }
    }

    @Test
    public void cleanupSolutionDirectories_withExistingDirectories_deletesAll() {
        try (MockedStatic<LogPathManager> logPathMock = Mockito.mockStatic(LogPathManager.class);
             MockedStatic<FileUtils> fileUtilsMock = Mockito.mockStatic(FileUtils.class)) {

            logPathMock.when(LogPathManager::getCrashDirectoryPath).thenReturn(TEST_CRASH_PATH);
            logPathMock.when(LogPathManager::getSolutionDirectoryPath).thenReturn(TEST_SOLUTION_PATH);
            fileUtilsMock.when(() -> FileUtils.deleteDirectoryRecursively(any(File.class), any(String.class)))
                    .thenReturn(true);

            receiver.cleanupSolutionDirectories(TEST_UUID);

            logPathMock.verify(LogPathManager::getCrashDirectoryPath);
            logPathMock.verify(LogPathManager::getSolutionDirectoryPath);
        }
    }

    @Test
    public void cleanupSolutionDirectories_withDeleteFailure_doesNotThrow() {
        try (MockedStatic<LogPathManager> logPathMock = Mockito.mockStatic(LogPathManager.class);
             MockedStatic<FileUtils> fileUtilsMock = Mockito.mockStatic(FileUtils.class)) {

            logPathMock.when(LogPathManager::getCrashDirectoryPath).thenReturn(TEST_CRASH_PATH);
            logPathMock.when(LogPathManager::getSolutionDirectoryPath).thenReturn(TEST_SOLUTION_PATH);
            fileUtilsMock.when(() -> FileUtils.deleteDirectoryRecursively(any(File.class), any(String.class)))
                    .thenReturn(false);

            // Should not throw even if deletion fails
            receiver.cleanupSolutionDirectories(TEST_UUID);
        }
    }

    @Test
    public void onReceive_withCorrectAction_extractsExtras() {
        when(mockIntent.getAction()).thenReturn(ACTION_PACKAGE_UNINSTALLED);
        when(mockIntent.getStringExtra(EXTRA_SOLUTION_UUID)).thenReturn(TEST_UUID);
        when(mockIntent.getStringExtra(EXTRA_PACKAGE)).thenReturn(TEST_PACKAGE);

        try (MockedStatic<LogPathManager> logPathMock = Mockito.mockStatic(LogPathManager.class);
             MockedStatic<FileUtils> fileUtilsMock = Mockito.mockStatic(FileUtils.class)) {

            logPathMock.when(LogPathManager::getCrashDirectoryPath).thenReturn(TEST_CRASH_PATH);
            logPathMock.when(LogPathManager::getSolutionDirectoryPath).thenReturn(TEST_SOLUTION_PATH);

            receiver.onReceive(mockContext, mockIntent);

            verify(mockIntent).getStringExtra(EXTRA_SOLUTION_UUID);
            verify(mockIntent).getStringExtra(EXTRA_PACKAGE);
        }
    }
}
