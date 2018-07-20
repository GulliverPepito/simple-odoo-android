// Copyright 2018 - Alexandre Díaz - <dev@redneboa.es>
package com.eiqui.eiqui.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.eiqui.eiqui.AppMain;
import com.eiqui.eiqui.R;
import com.eiqui.eiqui.fragments.IssueDetailsFragment;
import com.eiqui.eiqui.fragments.GenericMessagesFragment;
import com.eiqui.eiqui.utils.Constants;


public class IssueActivity extends FragmentActivity {
    static public final String PARAM_BKG_COLOR = "bkgColor";
    static public final String PARAM_PROJECT_LETTER = "projectLetter";
    static public final String PARAM_ISSUE_TITLE = "issueTitle";
    static public final String PARAM_ISSUE_ID = "issueID";
    static public final String PARAM_CONTACT = "contact";
    static public final String PARAM_DESCRIPTION = "description";
    static public final String PARAM_PROJECT_NAME = "projectName";
    static public final String PARAM_MESSAGE_IDS = "messageIds";
    static public final String PARAM_TASK_ASSIGNED = "taskAssigned";
    static public final String PARAM_OPEN_MESSAGES = "openMessages";


    private FragmentTabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue);

        TextView textIssueTitle = (TextView)findViewById(R.id.textTitle);
        TextView textIssueID = (TextView)findViewById(R.id.textID);
        TextView textProjectLetter = (TextView)findViewById(R.id.textProjectLetter);

        mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            textIssueTitle.setText(bundle.getString(PARAM_ISSUE_TITLE));
            textIssueID.setText("#"+Integer.toString(bundle.getInt(PARAM_ISSUE_ID)));
            textProjectLetter.setText(bundle.getString(PARAM_PROJECT_LETTER));
            textProjectLetter.setBackgroundColor(bundle.getInt(PARAM_BKG_COLOR));

            mTabHost.addTab(mTabHost.newTabSpec("issue_tab_details").setIndicator(getResources().getString(R.string.details)),
                    IssueDetailsFragment.class, bundle);

            Bundle nbundle = new Bundle();
            nbundle.putString(
                    GenericMessagesFragment.PARAM_MESSAGE_IDS,
                    bundle.getString(PARAM_MESSAGE_IDS));
            mTabHost.addTab(mTabHost.newTabSpec("tab_generic_messages").setIndicator(getResources().getString(R.string.messages)),
                    GenericMessagesFragment.class, nbundle);

            if (bundle.getBoolean(PARAM_OPEN_MESSAGES))
                mTabHost.setCurrentTab(1);
        }
    }

}