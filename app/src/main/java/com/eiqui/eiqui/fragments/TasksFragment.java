// Copyright 2018 - Alexandre Díaz - <dev@redneboa.es>
package com.eiqui.eiqui.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eiqui.eiqui.AppMain;
import com.eiqui.eiqui.activities.TaskActivity;
import com.eiqui.eiqui.utils.Constants;
import com.eiqui.eiqui.interfaces.IEiquiFragment;
import com.eiqui.eiqui.activities.MainActivity;
import com.eiqui.eiqui.R;
import com.eiqui.eiqui.adapters.TasksAdapter;
import com.eiqui.eiqui.utils.EiquiUtils;
import com.eiqui.odoojson_rpc.JSONRPCClientOdoo;
import com.eiqui.odoojson_rpc.exceptions.OdooSearchException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;


public class TasksFragment extends Fragment implements IEiquiFragment, View.OnClickListener {
    static public final String TAG = "TASKS";

    private OnFragmentInteractionListener mListener;
    private MainActivity mMainActivity;
    private LinearLayout mLoadLayout;
    private LinearLayout mEmptyDataLayout;
    private RecyclerView mRecyclerView;
    private TasksAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TasksTask mTaskTasks;


    public TasksFragment() {
        // Required empty public constructor
    }

    public static TasksFragment newInstance() {
        TasksFragment fragment = new TasksFragment();
        //Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        //fragment.setArguments(args);
        return fragment;
    }

    public void refreshData() {
        mRecyclerView.setVisibility(View.GONE);
        mLoadLayout.setVisibility(View.VISIBLE);
        mLoadLayout.findViewById(R.id.progressLoading).setVisibility(View.VISIBLE);
        mEmptyDataLayout.setVisibility(View.GONE);

        if (mTaskTasks != null)
            mTaskTasks.cancel(Boolean.TRUE);
        mTaskTasks = new TasksTask();
        mTaskTasks.execute();
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
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        mMainActivity = (MainActivity) getActivity();
        mMainActivity.GetNavMenu().getItem(1).setChecked(true);
        mMainActivity.setAppBarInfo(null, null);

        mLoadLayout = (LinearLayout) view.findViewById(R.id.layoutLoading);
        ((TextView) mLoadLayout.findViewById(R.id.textLoadingInfo)).setText(getResources().getString(R.string.loading_data));
        mEmptyDataLayout = (LinearLayout) view.findViewById(R.id.layoutEmpty);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerTasks);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        refreshData();

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (mTaskTasks != null)
            mTaskTasks.cancel(Boolean.TRUE);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onClick(View view) {
        int pos = mRecyclerView.getChildAdapterPosition(view);

        TextView textTaskTitle = (TextView)view.findViewById(R.id.textTitle);
        TextView textProjectLetter = (TextView)view.findViewById(R.id.textProjectLetter);
        ColorDrawable bkgColor = (ColorDrawable) textProjectLetter.getBackground();

        try {
            Intent intent = new Intent(mMainActivity, TaskActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(TaskActivity.PARAM_BKG_COLOR, bkgColor.getColor());
            bundle.putString(TaskActivity.PARAM_PROJECT_LETTER, textProjectLetter.getText().toString());
            bundle.putString(TaskActivity.PARAM_TASK_TITLE, textTaskTitle.getText().toString());

            JSONObject item = mAdapter.getItem(pos);
            bundle.putInt(TaskActivity.PARAM_TASK_ID, item.getInt("id"));

            JSONArray reviewer_id = item.getJSONArray("reviewer_id");
            bundle.putString(TaskActivity.PARAM_REVIEWER_NAME, (reviewer_id.length()>0)?reviewer_id.get(1).toString():getResources().getString(R.string.undefined));
            bundle.putString(TaskActivity.PARAM_DESCRIPTION, item.getString("description"));
            bundle.putString(TaskActivity.PARAM_ACTIVITY_IDS, item.getString("activity_ids"));
            JSONArray project_id = item.getJSONArray("project_id");
            bundle.putString(TaskActivity.PARAM_PROJECT_NAME, (project_id.length()>0)?project_id.getString(1):getResources().getString(R.string.undefined));
            bundle.putString(TaskActivity.PARAM_MESSAGE_IDS, item.getString("message_ids"));

            intent.putExtras(bundle);

            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                            mMainActivity,
                            view,
                            getString(R.string.transition_task_issue_detail)
                    );
            ActivityCompat.startActivity(mMainActivity, intent, options.toBundle());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    private class TasksTask extends AsyncTask<String, Void, Boolean> {
        private Exception mException;
        private JSONArray mTasksInfo;
        private Integer mNumOldUnfinishedTasks = 0;
        private final Integer mNumMonths = 2;

        protected Boolean doInBackground(String... params) {
            mException = null;
            try {
                Integer uid = ((AppMain)getActivity().getApplication()).getUID();
                JSONRPCClientOdoo odooClient = ((AppMain)mMainActivity.getApplication()).OdooClient();
                mTasksInfo = odooClient.callExecute(
                        "search_read",
                        "project.task",
                        odooClient.createStringDomain(String.format(Constants.OD_USER_ID, uid),
                                Constants.OD_KANBAN_NOT_BLOCKED,
                                Constants.OD_STAGE_OPEN),
                        "['project_id','date_start','name','id','reviewer_id','description','activity_ids','message_ids']"
                );

                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, -mNumMonths);
                final String frmtDateLong = EiquiUtils.dateToStringLong(cal.getTime());
                mNumOldUnfinishedTasks = odooClient.callCount(
                        "project.task",
                        odooClient.createStringDomain(String.format(Constants.OD_USER_ID, uid),
                                Constants.OD_KANBAN_NOT_BLOCKED,
                                Constants.OD_STAGE_OPEN,
                                "['date_start', '<=', '"+frmtDateLong+"']")
                );

                return (mTasksInfo!=null);
            } catch (OdooSearchException e) {
                mException = e;
            }
            return Boolean.FALSE;
        }

        protected void onPostExecute(Boolean res) {
            if (mException != null) {
                ((TextView)mLoadLayout.findViewById(R.id.textLoadingInfo)).setText(String.format(getResources().getString(R.string.oops_error), mException.getMessage()));
                mLoadLayout.findViewById(R.id.progressLoading).setVisibility(View.GONE);
                return;
            } else if (!res) {
                ((TextView)mLoadLayout.findViewById(R.id.textLoadingInfo)).setText(getResources().getString(R.string.oops_error_undefined));
                mLoadLayout.findViewById(R.id.progressLoading).setVisibility(View.GONE);
                return;
            }

            try {
                mRecyclerView.setVisibility(View.VISIBLE);
                mLoadLayout.setVisibility(View.GONE);

                mMainActivity.setAppBarInfo(Integer.toString(mTasksInfo.length()), getResources().getString(R.string.tasks));
                if (mTasksInfo.length() > 0) {
                    mAdapter = new TasksAdapter(mTasksInfo, TasksFragment.this, mNumMonths);
                    mRecyclerView.setAdapter(mAdapter);
                } else {
                    mEmptyDataLayout.setVisibility(View.VISIBLE);
                }

                if (mNumOldUnfinishedTasks > 0)
                    Snackbar.make(getView(), String.format(getResources().getString(R.string.old_tasks), mNumOldUnfinishedTasks, mNumMonths), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).setDuration(8000).show();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

}
