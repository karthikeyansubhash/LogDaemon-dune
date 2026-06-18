package com.hp.jetadvantage.link.logdaemon.data;

import android.os.Build;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class UrlData {
    private volatile static UrlData mInstance = null;
    private static final String TAG = "[LD][UrlData]";
    private static final String URL = "/system/etc/sss";
    private static final String OREO_URL = "/vendor/etc/sss";

    private JsonObject json;

    public UrlData() {
        init();
    }

    public static UrlData getInstance() {
        if(mInstance == null) {
            synchronized ( UrlData.class ) {
                if(mInstance == null) {
                    mInstance = new UrlData();
                }
            }
        }
        return mInstance;
    }

    private void init() {
        String text ;
        try{
            File file;
            if(Build.VERSION.SDK_INT < 26)
                file = new File(URL);
            else
                file = new File(OREO_URL);
            FileInputStream fis = new FileInputStream(file);
            Reader in = new InputStreamReader(fis);

            int size = fis.available();
            char[] buffer = new char[size];
            in.read(buffer);
            in.close();

            text = new String(buffer);

            json = JsonParser.parseString(text).getAsJsonObject();

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getJson(String key){
        String value = "";
        if(json != null && json.has(key)) value = json.get(key).getAsString();
        return value;
    }
}