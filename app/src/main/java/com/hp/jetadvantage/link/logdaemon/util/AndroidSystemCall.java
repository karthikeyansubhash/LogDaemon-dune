package com.hp.jetadvantage.link.logdaemon.util;

import android.annotation.SuppressLint;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Android system call class.
 * All methods are static so this class don't need to be instantiated
 */
public class AndroidSystemCall {

    private static final String TAG = "[LD][AndSysCall]";

    //get SystemProperties to create dumpstate log
    private static final String SYSTEM_PROPERTIES_CLASS = "android.os.SystemProperties";
    private static final String SET_METHOD = "set";
    private static boolean setPropertyStatus = true;

    // Add this constant at the top of the class
    private static final String PROPERTY_NOTIFY_GET_LOG_DUMP_END = "service.link.notifygetlogdump.end";

    /**
     * Private constructor to prevent instantiation
     * This utility class should not be instantiated as all methods are static
     */
    private AndroidSystemCall() {
        // Utility class - no instantiation allowed
        throw new IllegalStateException("Utility class");
    }

    @SuppressLint({"PrivateApi", "DiscouragedPrivateApi"})
    public static void setSystemProperties(String key, String val){
        Log.i(TAG, "Set system properties");
        Method method = null;
        Class<?> systemPropertiesClass = null;
        try {
            systemPropertiesClass = Class.forName(SYSTEM_PROPERTIES_CLASS);
            method = systemPropertiesClass.getDeclaredMethod(SET_METHOD, String.class, String.class);
            method.setAccessible(true);
            method.invoke(null, key, val);
        } catch (ClassNotFoundException | NoSuchMethodException
                | IllegalAccessException | InvocationTargetException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public static void checkStatusForSetPropertiesSettings(){
        Log.i(TAG, "checkStatusForSetPropertiesSettings : " + setPropertyStatus);
        if(setPropertyStatus) {
            // Check current property value before setting
            String currentValue = getSystemProperties(PROPERTY_NOTIFY_GET_LOG_DUMP_END);
            Log.i(TAG, "Current property value before setting: " + currentValue);

            setSystemProperties(PROPERTY_NOTIFY_GET_LOG_DUMP_END, "1");

            // Verify property was set successfully
            String newValue = getSystemProperties(PROPERTY_NOTIFY_GET_LOG_DUMP_END);
            Log.i(TAG, "Property value after setting: " + newValue);

            if (!"1".equals(newValue)) {
                Log.e(TAG, "Failed to set system property! Expected: 1, Actual: " + newValue);
            } else {
                Log.i(TAG, "System property set successfully");
            }

            setPropertyStatus = false;

            new Thread(() -> {
                try {
                    Thread.sleep(1000L * 60);
                    setPropertyStatus = true;
                } catch (InterruptedException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                    Thread.currentThread().interrupt();
                }
                Log.i(TAG, "setPropertyStatus change : " + setPropertyStatus);
            }).start();
        }
    }

    /**
     * Get system property value
     * @param key Property key to get
     * @return Property value or empty string if not found
     */
    @SuppressLint({"PrivateApi", "DiscouragedPrivateApi"})
    public static String getSystemProperties(String key) {
        String value = "";
        try {
            Class<?> systemPropertiesClass = Class.forName(SYSTEM_PROPERTIES_CLASS);
            Method getMethod = systemPropertiesClass.getDeclaredMethod("get", String.class);
            getMethod.setAccessible(true);
            value = (String) getMethod.invoke(null, key);
            Log.d(TAG, "Got system property " + key + " = " + value);
        } catch (ClassNotFoundException | NoSuchMethodException
                | IllegalAccessException | InvocationTargetException e) {
            Log.e(TAG, "Failed to get system property " + key + ": " + Log.getStackTraceString(e));
        }
        return value != null ? value : "";
    }
}

