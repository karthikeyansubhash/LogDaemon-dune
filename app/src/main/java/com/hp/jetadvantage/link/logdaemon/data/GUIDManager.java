package com.hp.jetadvantage.link.logdaemon.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class GUIDManager {
    private static final String TAG = "[LD][GUID]";

    private Context mContext;

    private HashMap<String, String> mIdMap;

    private HashMap<String, MultiplePackage> mMultiMap;

    private static final String SUPPORT_APP_SERVICES = "com.hp.jetadvantage.link.services";
    private static final String SUPPORT_APP_PACKMAN = "com.hp.jetadvantage.link.packagemanager";
    private static final String SUPPORT_APP_SYSTEM = "system";

    private static final String PACKAGES_URI_AUTHORITY = "packages";
    private static final String COL_APPLICATION_AGENT_ID = "applicationAgentId";
    private static final String COL_PACKAGE_NAME = "packageName";
    private static final String COL_SOLUTION_ID = "solutionId";

    public GUIDManager(Context context){
        this.mContext = context;
        mIdMap = new HashMap<>();
        mMultiMap = new HashMap<>();
    }

    public String getId(String name){
        String id = "";

        if(mIdMap.containsKey(name)){
            id = mIdMap.get(name);
        }
        return id;
    }

    public void updatePackageId(){
        mIdMap.clear();
        mMultiMap.clear();
        Cursor cursor = null;
        try {
            String[] projection = {COL_APPLICATION_AGENT_ID, COL_PACKAGE_NAME, COL_SOLUTION_ID};
            cursor = mContext.getContentResolver().query(
                    new Uri.Builder().scheme("content").authority(PACKAGES_URI_AUTHORITY).build(),
                    projection, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String packageName = cursor.getString(cursor.getColumnIndex(COL_PACKAGE_NAME));
                    String uuid = cursor.getString(cursor.getColumnIndex(COL_APPLICATION_AGENT_ID));
                    String solutionUuid = cursor.getString(cursor.getColumnIndex(COL_SOLUTION_ID));

                    Log.i(TAG, "packageName: " + packageName);
                    Log.i(TAG, "    uuid: " + uuid);
                    Log.i(TAG, "    solutionUuid: " + solutionUuid);

                    if(!mIdMap.containsKey(packageName)){ //package-1 uuid case
                        Log.i(TAG, "new! Package added in IdMap");
                        mIdMap.put(packageName, solutionUuid);
                    } else { // package-multi uuid case
                        Log.i(TAG, "already exist! Package added in MultiMap");
                        if(mMultiMap.containsKey(packageName)){
                            Objects.requireNonNull(mMultiMap.get(packageName)).addUuid(solutionUuid);
                        } else {
                            mMultiMap.put(packageName, new MultiplePackage(solutionUuid));
                        }
                    }
                }
            }
            else{
                Log.i(TAG, "Cursor NULL");
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public class MultiplePackage{
        private List<String> mArray = new ArrayList<>();

        MultiplePackage(String uuid){
            mArray.add(uuid);
        }

        /**
         * Add a UUID to the array
         * @param uuid UUID to add
         */
        public void addUuid(String uuid) {
            if (uuid != null && !uuid.trim().isEmpty()) {
                mArray.add(uuid);
            }
        }

        /**
         * Get the list of UUIDs (read-only view)
         * @return unmodifiable list of UUIDs
         */
        public List<String> getUuids() {
            return new ArrayList<>(mArray);
        }

        /**
         * Get the number of UUIDs
         * @return size of the UUID array
         */
        public int size() {
            return mArray.size();
        }
    }

    public static boolean matchSupportApp(String name){
        return SUPPORT_APP_SYSTEM.equals(name) || SUPPORT_APP_SERVICES.equals(name) || SUPPORT_APP_PACKMAN.contains(name);
    }

}
