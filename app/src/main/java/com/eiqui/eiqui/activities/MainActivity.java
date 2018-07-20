// Copyright 2018 - Alexandre Díaz - <dev@redneboa.es>
package com.eiqui.eiqui.activities;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eiqui.eiqui.AppMain;
import com.eiqui.eiqui.R;
import com.eiqui.eiqui.fragments.GeneralFragment;
import com.eiqui.eiqui.fragments.IssuesFragment;
import com.eiqui.eiqui.fragments.SettingsFragment;
import com.eiqui.eiqui.fragments.TasksFragment;
import com.eiqui.eiqui.interfaces.IEiquiFragment;
import com.eiqui.eiqui.services.EiquiJobService;
import com.eiqui.eiqui.utils.Constants;
import com.eiqui.odoojson_rpc.exceptions.OdooSearchException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TasksFragment.OnFragmentInteractionListener, IssuesFragment.OnFragmentInteractionListener {

    private TextView mTextUserName;
    private TextView mTextLogin;
    private CircleImageView mImgAvatar;
    protected String mActiveFragmentTag;
    protected Menu mNavMenu;
    private LinearLayout mToolbarExtraInfo;
    private SharedPreferences mSettings;

    public Menu GetNavMenu() { return mNavMenu; }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSettings = getSharedPreferences(Constants.SHARED_PREFS_USER_INFO, Context.MODE_PRIVATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mNavMenu = navigationView.getMenu();

        View headerLayout = navigationView.getHeaderView(0);
        mTextUserName = (TextView) headerLayout.findViewById(R.id.textUserName);
        mTextLogin = (TextView) headerLayout.findViewById(R.id.textLogin);
        mImgAvatar = (CircleImageView) headerLayout.findViewById(R.id.imgAvatar);
        mToolbarExtraInfo = (LinearLayout)findViewById(R.id.linearLayoutExtraInfo);

        ImageButton btnCloseApp = (ImageButton) headerLayout.findViewById(R.id.btnCloseApp);
        btnCloseApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backToLogin(Boolean.TRUE);
            }
        });

        if (savedInstanceState == null) {
            // Obtener Info del Usuario
            new AccountInfoTask().execute();

            // Mostrar info general
            switchFragment(GeneralFragment.newInstance(), GeneralFragment.TAG);
        } else {
            mTextUserName.setText(savedInstanceState.getString("name").trim());
            mTextLogin.setText(savedInstanceState.getString("login").trim());

            byte[] bytes = savedInstanceState.getByteArray("image_medium");
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            mImgAvatar.setImageBitmap(bitmap);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("name", mTextUserName.getText().toString());
        outState.putString("login", mTextLogin.getText().toString());

        Bitmap bitmap = ((BitmapDrawable)mImgAvatar.getDrawable()).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        outState.putByteArray("image_medium", byteArrayOutputStream.toByteArray());
    }

    public void switchFragment(Fragment fragment, String fragmentTag) {
        mActiveFragmentTag = fragmentTag;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentSwitch, fragment, mActiveFragmentTag);
        //ft.addToBackStack(null);
        ft.setTransition( FragmentTransaction.TRANSIT_FRAGMENT_OPEN );
        ft.show(fragment);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.action_refresh) {
            if (mActiveFragmentTag == GeneralFragment.TAG || mActiveFragmentTag == TasksFragment.TAG || mActiveFragmentTag == IssuesFragment.TAG) {
                IEiquiFragment fragment = (IEiquiFragment) getSupportFragmentManager().findFragmentByTag(mActiveFragmentTag);
                fragment.refreshData();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            switchFragment(GeneralFragment.newInstance(), GeneralFragment.TAG);
        } else if (id == R.id.nav_tasks) {
            switchFragment(TasksFragment.newInstance(), TasksFragment.TAG);
        } else if (id == R.id.nav_issues) {
            switchFragment(IssuesFragment.newInstance(), IssuesFragment.TAG);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void backToLogin(Boolean logout) {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.putExtra(LoginActivity.PARAM_LOGOUT, logout);
        startActivity(intent);
        finish();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.v("eiqui", uri.toString());
    }

    public void setAppBarInfo(String count, String section) {
        if (count == null && section == null)
            mToolbarExtraInfo.setVisibility(View.GONE);
        else {
            mToolbarExtraInfo.setVisibility(View.VISIBLE);
            TextView textCount = ((TextView) mToolbarExtraInfo.findViewById(R.id.textCount));
            textCount.setTranslationX(-100);
            textCount.setAlpha(0.0f);
            textCount.setText(count);
            textCount.animate().alpha(1.0f).translationX(0);
            TextView textSection = ((TextView) mToolbarExtraInfo.findViewById(R.id.textSection));
            textSection.setTranslationX(-100);
            textSection.setAlpha(0.0f);
            textSection.setText(section);
            textSection.animate().alpha(1.0f).translationX(0);
        }
    }


    private class AccountInfoTask extends AsyncTask<String, Void, Boolean> {
        private Exception mException;
        private JSONArray mAccountInfo;

        protected Boolean doInBackground(String... params) {
            mException = null;
            try {
                mAccountInfo = ((AppMain)getApplication()).OdooClient().callExecute(
                        "read", "res.users", "["+((AppMain)getApplication()).getUID()+"]",
                        "['image_medium', 'name', 'login']");
                return (mAccountInfo!=null);
            } catch (OdooSearchException e) {
                mException = e;
            }
            return Boolean.FALSE;
        }

        protected void onPostExecute(Boolean res) {
            if (!res || mException != null)
                return;

            try {
                JSONObject jsonObj = mAccountInfo.getJSONObject(0);

                // Write Basic User Info
                mTextUserName.setText(jsonObj.getString("name").trim());
                mTextLogin.setText(jsonObj.getString("login").trim());

                // Update Avatar Image
                byte[] decodedString = Base64.decode(
                        jsonObj.getString("image_medium"), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(
                        decodedString, 0, decodedString.length);
                mImgAvatar.setImageBitmap(decodedByte);
            } catch (JSONException e) {
                mException.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

}
