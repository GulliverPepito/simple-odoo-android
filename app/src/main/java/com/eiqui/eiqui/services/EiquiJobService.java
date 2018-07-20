// Copyright 2018 - Alexandre Díaz - <dev@redneboa.es>
package com.eiqui.eiqui.services;


import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.eiqui.eiqui.AppMain;
import com.eiqui.eiqui.R;
import com.eiqui.eiqui.activities.IssueActivity;
import com.eiqui.eiqui.activities.TaskActivity;
import com.eiqui.eiqui.utils.Constants;
import com.eiqui.eiqui.utils.EiquiUtils;
import com.eiqui.odoojson_rpc.JSONRPCClientOdoo;
import com.eiqui.odoojson_rpc.exceptions.OdooSearchException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by uchar on 20/09/16.
 */

public class EiquiJobService extends JobService {
    private static final String TAG = "SyncService";
    private SharedPreferences mSettings;

    @Override
    public boolean onStartJob(JobParameters params) {
        mSettings = getSharedPreferences(Constants.SHARED_PREFS_USER_INFO, Context.MODE_PRIVATE);
        if (((AppMain)getApplication()).getUID() > 0) {
            new GeneralInfoTask().execute();
            return false;
        }

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    private void createTaskNotification(JSONObject jsonObj) {
        try {
            Intent intent = new Intent(this, TaskActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(TaskActivity.PARAM_TASK_TITLE, jsonObj.getString("name"));
            bundle.putInt(TaskActivity.PARAM_TASK_ID, jsonObj.getInt("id"));
            JSONArray reviewer_id = jsonObj.getJSONArray("reviewer_id");
            bundle.putString(TaskActivity.PARAM_REVIEWER_NAME, (reviewer_id.length() > 0) ? reviewer_id.get(1).toString() : getResources().getString(R.string.undefined));
            bundle.putString(TaskActivity.PARAM_DESCRIPTION, jsonObj.getString("description"));
            bundle.putString(TaskActivity.PARAM_ACTIVITY_IDS, jsonObj.getString("activity_ids"));
            JSONArray project_id = jsonObj.getJSONArray("project_id");
            String projectName = (project_id.length() > 0) ? project_id.getString(1) : getResources().getString(R.string.undefined);
            bundle.putString(TaskActivity.PARAM_PROJECT_NAME, projectName);
            bundle.putString(TaskActivity.PARAM_PROJECT_LETTER, String.valueOf(projectName.toUpperCase().charAt(0)));
            bundle.putInt(TaskActivity.PARAM_BKG_COLOR, EiquiUtils.generateColorRandom(projectName.toUpperCase().charAt(0)));
            bundle.putString(TaskActivity.PARAM_MESSAGE_IDS, jsonObj.getString("message_ids"));
            intent.putExtras(bundle);
            EiquiUtils.createNotification(this, R.drawable.ic_card_tasks_bad, jsonObj.getString("name"), jsonObj.getString("description"), intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void createTaskMessageNotification(JSONObject jsonObj, Integer numMessages) {
        try {
            Intent intent = new Intent(this, TaskActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(TaskActivity.PARAM_TASK_TITLE, jsonObj.getString("name"));
            bundle.putInt(TaskActivity.PARAM_TASK_ID, jsonObj.getInt("id"));
            JSONArray reviewer_id = jsonObj.getJSONArray("reviewer_id");
            bundle.putString(TaskActivity.PARAM_REVIEWER_NAME, (reviewer_id.length() > 0) ? reviewer_id.get(1).toString() : getResources().getString(R.string.undefined));
            bundle.putString(TaskActivity.PARAM_DESCRIPTION, jsonObj.getString("description"));
            bundle.putString(TaskActivity.PARAM_ACTIVITY_IDS, jsonObj.getString("activity_ids"));
            JSONArray project_id = jsonObj.getJSONArray("project_id");
            String projectName = (project_id.length() > 0) ? project_id.getString(1) : getResources().getString(R.string.undefined);
            bundle.putString(TaskActivity.PARAM_PROJECT_NAME, projectName);
            bundle.putString(TaskActivity.PARAM_PROJECT_LETTER, String.valueOf(projectName.toUpperCase().charAt(0)));
            bundle.putInt(TaskActivity.PARAM_BKG_COLOR, EiquiUtils.generateColorRandom(projectName.toUpperCase().charAt(0)));
            bundle.putString(TaskActivity.PARAM_MESSAGE_IDS, jsonObj.getString("message_ids"));
            bundle.putBoolean(TaskActivity.PARAM_OPEN_MESSAGES, Boolean.TRUE);
            intent.putExtras(bundle);
            EiquiUtils.createNotification(this, R.drawable.ic_message, jsonObj.getString("name"), String.format(getResources().getString(R.string.notification_new_message), numMessages), intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void createIssueNotification(JSONObject jsonObj) {
        try {
            Intent intent = new Intent(this, IssueActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(IssueActivity.PARAM_ISSUE_TITLE, jsonObj.getString("name"));
            bundle.putInt(IssueActivity.PARAM_ISSUE_ID, jsonObj.getInt("id"));
            JSONArray partner_id = jsonObj.getJSONArray("partner_id");
            bundle.putString(IssueActivity.PARAM_CONTACT, (partner_id.length() > 0)?partner_id.get(1).toString():getResources().getString(R.string.undefined));
            bundle.putString(IssueActivity.PARAM_DESCRIPTION, jsonObj.getString("description"));
            JSONArray project_id = jsonObj.getJSONArray("project_id");
            String projectName = (project_id.length() > 0) ? project_id.getString(1) : getResources().getString(R.string.undefined);
            bundle.putString(IssueActivity.PARAM_PROJECT_NAME, projectName);
            bundle.putString(IssueActivity.PARAM_PROJECT_LETTER, String.valueOf(projectName.toUpperCase().charAt(0)));
            bundle.putInt(IssueActivity.PARAM_BKG_COLOR, EiquiUtils.generateColorRandom(projectName.toUpperCase().charAt(0)));
            bundle.putString(IssueActivity.PARAM_MESSAGE_IDS, jsonObj.getString("message_ids"));
            JSONArray task_id = jsonObj.getJSONArray("task_id");
            bundle.putString(IssueActivity.PARAM_TASK_ASSIGNED, (task_id.length() > 0)?task_id.get(1).toString():getResources().getString(R.string.undefined));
            intent.putExtras(bundle);
            EiquiUtils.createNotification(this, R.drawable.ic_card_issues_bad, jsonObj.getString("name"), jsonObj.getString("description"), intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void createIssueMessageNotification(JSONObject jsonObj, Integer numMessages) {
        try {
            Intent intent = new Intent(this, IssueActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(IssueActivity.PARAM_ISSUE_TITLE, jsonObj.getString("name"));
            bundle.putInt(IssueActivity.PARAM_ISSUE_ID, jsonObj.getInt("id"));
            JSONArray partner_id = jsonObj.getJSONArray("partner_id");
            bundle.putString(IssueActivity.PARAM_CONTACT, (partner_id.length() > 0)?partner_id.get(1).toString():getResources().getString(R.string.undefined));
            bundle.putString(IssueActivity.PARAM_DESCRIPTION, jsonObj.getString("description"));
            JSONArray project_id = jsonObj.getJSONArray("project_id");
            String projectName = (project_id.length() > 0) ? project_id.getString(1) : getResources().getString(R.string.undefined);
            bundle.putString(IssueActivity.PARAM_PROJECT_NAME, projectName);
            bundle.putString(IssueActivity.PARAM_PROJECT_LETTER, String.valueOf(projectName.toUpperCase().charAt(0)));
            bundle.putInt(IssueActivity.PARAM_BKG_COLOR, EiquiUtils.generateColorRandom(projectName.toUpperCase().charAt(0)));
            bundle.putString(IssueActivity.PARAM_MESSAGE_IDS, jsonObj.getString("message_ids"));
            JSONArray task_id = jsonObj.getJSONArray("task_id");
            bundle.putString(IssueActivity.PARAM_TASK_ASSIGNED, (task_id.length() > 0)?task_id.get(1).toString():getResources().getString(R.string.undefined));
            bundle.putBoolean(TaskActivity.PARAM_OPEN_MESSAGES, Boolean.TRUE);
            intent.putExtras(bundle);
            EiquiUtils.createNotification(this, R.drawable.ic_message, jsonObj.getString("name"), String.format(getResources().getString(R.string.notification_new_message), numMessages), intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    private class GeneralInfoTask extends AsyncTask<String, Void, Boolean> {
        private Exception mException;
        private JSONArray mTasksInfo;
        private JSONArray mIssuesInfo;
        private ArrayList<Integer> mMessageIds;


        protected Boolean doInBackground(String... params) {
            mException = null;
            try {
                Integer uid = ((AppMain)getApplication()).getUID();
                JSONRPCClientOdoo odooClient = ((AppMain)getApplication()).OdooClient();

                // Obtener Tareas nuevas
                mTasksInfo = odooClient.callExecute(
                        "search_read",
                        "project.task",
                        odooClient.createStringDomain(String.format(Constants.OD_USER_ID, uid),
                                Constants.OD_KANBAN_NOT_BLOCKED,
                                Constants.OD_STAGE_OPEN),
                        "['project_id','date_start','name','id','reviewer_id','description','activity_ids','message_ids']"
                );

                // Obtener Incidencias Nuevas
                mIssuesInfo = odooClient.callExecute(
                        "search_read",
                        "project.issue",
                        odooClient.createStringDomain(String.format(Constants.OD_USER_ID, uid),
                                Constants.OD_KANBAN_NOT_BLOCKED,
                                Constants.OD_STAGE_OPEN),
                        "['project_id','create_date','name','id','description','message_ids','task_id','partner_id']"
                );

                // Obtener todos los mensajes
                int last_msg_id = mSettings.getInt("lastMessageID", 0);
                int big_msg_id = last_msg_id;
                mMessageIds = new ArrayList<Integer>();
                // tareas
                for (int i = 0; i < mTasksInfo.length(); i++) {
                    try {
                        JSONObject jsonObj = mTasksInfo.getJSONObject(i);
                        JSONArray ids = jsonObj.getJSONArray("message_ids");
                        for (int e=0; e<ids.length(); e++) {
                            int curId = ids.getInt(e);
                            if (curId > last_msg_id) {
                                if (curId > big_msg_id)
                                    big_msg_id = curId;
                                mMessageIds.add(curId);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                // incidencias
                for (int i = 0; i < mIssuesInfo.length(); i++) {
                    try {
                        JSONObject jsonObj = mIssuesInfo.getJSONObject(i);
                        JSONArray ids = jsonObj.getJSONArray("message_ids");
                        for (int e=0; e<ids.length(); e++) {
                            int curId = ids.getInt(e);
                            if (curId > last_msg_id) {
                                if (curId > big_msg_id)
                                    big_msg_id = curId;
                                mMessageIds.add(curId);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putInt("lastMessageID", big_msg_id);
                editor.commit();

                return Boolean.TRUE;
            } catch (OdooSearchException e) {
                mException = e;
            }

            return Boolean.FALSE;
        }

        protected void onPostExecute(Boolean res) {
            if (!res || mException != null)
                return;


            // Notificar Tareas
            if (mTasksInfo.length() > 0) {
                int last_id = mSettings.getInt("lastTaskID", 0);
                for (int i = 0; i < mTasksInfo.length(); i++) {
                    try {
                        JSONObject jsonObj = mTasksInfo.getJSONObject(i);
                        int id = jsonObj.getInt("id");
                        if (id > last_id) {
                            createTaskNotification(jsonObj);
                            last_id = id;
                        } else {
                            JSONArray msg_ids = jsonObj.getJSONArray("message_ids");
                            Boolean has_messages = Boolean.FALSE;
                            Integer count_messages = 0;
                            for (int e = 0; e < msg_ids.length(); e++) {
                                int msg_id = msg_ids.getInt(e);
                                if (mMessageIds.contains(msg_id)) {
                                    has_messages = Boolean.TRUE;
                                    ++count_messages;
                                }
                            }

                            if (has_messages)
                                createTaskMessageNotification(jsonObj, count_messages);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                SharedPreferences.Editor editor = mSettings.edit();
                editor.putInt("lastTaskID", last_id);
                editor.commit();
            }

            // Notificar Incidencias
            if (mIssuesInfo.length() > 0) {
                int last_id = mSettings.getInt("lastIssueID", 0);
                for (int i = 0; i < mIssuesInfo.length(); i++) {
                    try {
                        JSONObject jsonObj = mIssuesInfo.getJSONObject(i);
                        int id = jsonObj.getInt("id");
                        if (id > last_id) {
                            createIssueNotification(jsonObj);
                            last_id = id;
                        } else {
                            JSONArray msg_ids = jsonObj.getJSONArray("message_ids");
                            Boolean has_messages = Boolean.FALSE;
                            Integer count_messages = 0;
                            for (int e = 0; e < msg_ids.length(); e++) {
                                int msg_id = msg_ids.getInt(e);
                                if (mMessageIds.contains(msg_id)) {
                                    has_messages = Boolean.TRUE;
                                    ++count_messages;
                                }
                            }

                            if (has_messages)
                                createIssueMessageNotification(jsonObj, count_messages);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                SharedPreferences.Editor editor = mSettings.edit();
                editor.putInt("lastIssueID", last_id);
                editor.commit();
            }
        }
    }
}

