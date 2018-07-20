package com.eiqui.eiqui.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.eiqui.eiqui.AppMain;
import com.eiqui.eiqui.R;
import com.eiqui.eiqui.activities.TaskActivity;
import com.eiqui.odoojson_rpc.exceptions.OdooSearchException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class TaskDetailsFragment extends Fragment {
    static public final String TAG = "TASK_DETAILS";
    private TaskActivity mTaskActivity;
    private ListView mListActivities;
    private TaskDetailsTask mTaskDetailsTask;


    public TaskDetailsFragment() {
        // Required empty public constructor
    }

    public static TaskDetailsFragment newInstance() {
        TaskDetailsFragment fragment = new TaskDetailsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(Boolean.TRUE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_task_details, container, false);

        mTaskActivity = (TaskActivity) getActivity();

        TextView textReviewerName = (TextView)view.findViewById(R.id.textReviewerName);
        TextView textDescription = (TextView)view.findViewById(R.id.textDescription);
        TextView textProjectName = (TextView)view.findViewById(R.id.textProjectName);
        mListActivities = (ListView)view.findViewById(R.id.listActivities);

        Bundle bundle = getActivity().getIntent().getExtras();
        if (bundle != null) {
            textReviewerName.setText(bundle.getString(TaskActivity.PARAM_REVIEWER_NAME));
            textDescription.setText(bundle.getString(TaskActivity.PARAM_DESCRIPTION));
            textProjectName.setText(bundle.getString(TaskActivity.PARAM_PROJECT_NAME));
            mTaskDetailsTask = new TaskDetailsTask();
            mTaskDetailsTask.execute(bundle.getString(TaskActivity.PARAM_ACTIVITY_IDS));
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mTaskDetailsTask != null)
            mTaskDetailsTask.cancel(Boolean.TRUE);
    }


    private class TaskDetailsTask extends AsyncTask<String, Void, Boolean> {
        private Exception mException;
        private JSONArray mActivitiesInfo;
        private Integer mNumOldUnfinishedTasks = 0;

        protected Boolean doInBackground(String... params) {
            mException = null;
            try {
                mActivitiesInfo = ((AppMain)mTaskActivity.getApplication()).OdooClient().callExecute(
                        "read",
                        "project.task.activity",
                        params[0],
                        "['description']"
                );

                return (mActivitiesInfo!=null);
            } catch (OdooSearchException e) {
                mException = e;
            }
            return Boolean.FALSE;
        }

        protected void onPostExecute(Boolean res) {
            if (!res || mException != null)
                return;

            try {

                String[] values = new String[mActivitiesInfo.length()];
                for (int i=0; i<mActivitiesInfo.length(); i++) {
                    JSONObject objActivity = mActivitiesInfo.getJSONObject(i);
                    values[i] = objActivity.getString("description");
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_list_item_1, android.R.id.text1, values);
                mListActivities.setAdapter(adapter);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

}
