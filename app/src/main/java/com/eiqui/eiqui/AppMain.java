package com.eiqui.eiqui;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.eiqui.eiqui.utils.Constants;
import com.eiqui.odoojson_rpc.JSONRPCClientOdoo;

import java.net.MalformedURLException;

/**
 * Created by uchar on 19/09/16.
 */
public class AppMain extends Application {

    protected JSONRPCClientOdoo mOdooClient;
    private SharedPreferences mSettings;

    public JSONRPCClientOdoo OdooClient() { return mOdooClient; }

    public void startOdooClient(String host, String dbname, int uid, String pass) throws MalformedURLException {
        mOdooClient = new JSONRPCClientOdoo(host);
        mOdooClient.setConfig(dbname, uid, pass);
    }

    public Integer getUID() {
        return mSettings.getInt("UserID", -1);
    }

    @Override
    public void onCreate() {
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
        mSettings = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS_USER_INFO, Context.MODE_PRIVATE);

        // Auto-Start
        int uid = getUID();
        if (uid != -1) {
            try {
                startOdooClient(mSettings.getString("Host", ""),
                        mSettings.getString("DBName", ""), uid,
                        mSettings.getString("Pass", ""));

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

}
