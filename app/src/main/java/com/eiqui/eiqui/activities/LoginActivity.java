package com.eiqui.eiqui.activities;

import android.accounts.AccountAuthenticatorActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.eiqui.eiqui.AppMain;
import com.eiqui.eiqui.R;
import com.eiqui.eiqui.utils.Constants;
import com.eiqui.odoojson_rpc.exceptions.OdooLoginException;

import java.net.MalformedURLException;

public class LoginActivity extends AccountAuthenticatorActivity implements OnClickListener {

    public static String PARAM_LOGOUT = "logout";
    private Spinner mSpinnerConnType;
    private EditText mEditHost;
    private EditText mEditDBName;
    private EditText mEditLogin;
    private EditText mEditPass;
    private TextView mTextError;
    private Button mBtnLoginIn;
    private ProgressBar mProgressBar;
    private ImageView mLogo;
    private SharedPreferences mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSettings = getSharedPreferences(Constants.SHARED_PREFS_USER_INFO, Context.MODE_PRIVATE);

        mEditHost = (EditText) findViewById(R.id.editHost);
        mEditDBName = (EditText) findViewById(R.id.editDBName);
        mEditLogin = (EditText) findViewById(R.id.editLogin);
        mEditPass = (EditText) findViewById(R.id.editPassword);
        mBtnLoginIn = (Button) findViewById(R.id.btnLogin);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mLogo = (ImageView) findViewById(R.id.imgLogo);
        mTextError = (TextView) findViewById(R.id.txtError);

        mLogo.setAlpha(0.0f);
        mLogo.setTranslationY(-100);
        mLogo.animate().alpha(1.0f).translationY(0).setStartDelay(250);

        if (getIntent().getBooleanExtra(PARAM_LOGOUT, Boolean.FALSE))
            logout();

        mEditHost.setText(mSettings.getString("Host", ""));
        mEditDBName.setText(mSettings.getString("DBName", ""));
        mEditLogin.setText(mSettings.getString("Login", ""));
        mEditPass.setText(mSettings.getString("Pass", ""));

        mBtnLoginIn.setOnClickListener(this);

        // Ignorar login si ya se logeo correctamente
        if (mSettings.getInt("UserID", -1) > 0)
            initApp();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnLogin)
        {
            new LoginTask().execute(
                    mEditHost.getText().toString(),
                    mEditDBName.getText().toString(), mEditLogin.getText().toString(),
                    mEditPass.getText().toString());
            mTextError.setVisibility(View.INVISIBLE);
            hideControls(Boolean.TRUE);
        }
    }

    private void logout() {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.remove("UserID");
        editor.remove("lastTaskID");
        editor.remove("lastIssueID");
        editor.remove("lastMessageID");
        editor.remove("Pass");
        editor.commit();
    }

    private void initApp() {
        try {
            ((AppMain)getApplication()).startOdooClient(mSettings.getString("Host",""),
                    mSettings.getString("DBName",""),
                    mSettings.getInt("UserID",-1), mSettings.getString("Pass",""));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            showErrorMessage(getResources().getString(R.string.init_app_error));
        }
        startActivity(new Intent(this, MainActivity.class));
        sendBroadcast(new Intent(Constants.ACTION_INIT_SERVICE));
        finish();
    }

    private void hideControls(Boolean state) {
        mEditLogin.setEnabled(!state);
        mEditPass.setEnabled(!state);
        mBtnLoginIn.setVisibility(state==Boolean.TRUE?View.GONE:View.VISIBLE);
        mProgressBar.setVisibility(state==Boolean.TRUE?View.VISIBLE:View.GONE);

    }

    private void showErrorMessage(String text) {
        mTextError.setAlpha(0.0f);
        mTextError.setVisibility(View.VISIBLE);
        mTextError.animate().alpha(1.0f);
        mTextError.setText(text);
        hideControls(Boolean.FALSE);
    }


    private class LoginTask extends AsyncTask<String, Void, Boolean> {
        private Exception mException;
        private Integer mUID;

        protected Boolean doInBackground(String... params) {
            mException = null;
            try {
                ((AppMain)getApplication()).startOdooClient(params[0], params[1], -1, "");
                mUID = ((AppMain)getApplication()).OdooClient().loginIn(params[2], params[3]);
                return (mUID>0);
            } catch (OdooLoginException e) {
                mException = e;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return Boolean.FALSE;
        }

        protected void onPostExecute(Boolean res) {
            if (res) {
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putInt("UserID", mUID);
                editor.putString("Host", mEditHost.getText().toString());
                editor.putString("DBName", mEditDBName.getText().toString());
                editor.putString("Login", mEditLogin.getText().toString());
                editor.putString("Pass", mEditPass.getText().toString());
                editor.commit();

                initApp();
            }
            else {
               showErrorMessage(
                       (mException != null)?mException.getMessage():getResources().getString(R.string.loginInvalid));
            }
        }
    }

}