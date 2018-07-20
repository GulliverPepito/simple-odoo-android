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
import com.eiqui.eiqui.activities.IssueActivity;
import com.eiqui.eiqui.utils.Constants;
import com.eiqui.eiqui.interfaces.IEiquiFragment;
import com.eiqui.eiqui.activities.MainActivity;
import com.eiqui.eiqui.R;
import com.eiqui.eiqui.adapters.IssuesAdapter;
import com.eiqui.eiqui.utils.EiquiUtils;
import com.eiqui.odoojson_rpc.JSONRPCClientOdoo;
import com.eiqui.odoojson_rpc.exceptions.OdooSearchException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;


public class IssuesFragment extends Fragment implements IEiquiFragment, View.OnClickListener {
    static public final String TAG = "ISSUES";
    private OnFragmentInteractionListener mListener;
    private MainActivity mMainActivity;
    private LinearLayout mLoadLayout;
    private LinearLayout mEmptyDataLayout;
    private RecyclerView mRecyclerView;
    private IssuesAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private IssuesTask mIssuesTask;


    public IssuesFragment() {
        // Required empty public constructor
    }

    public static IssuesFragment newInstance() {
        IssuesFragment fragment = new IssuesFragment();
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

        SharedPreferences settings = mMainActivity.getSharedPreferences("UserInfo", 0);
        Integer uid = settings.getInt("UserID", -1);

        if (mIssuesTask != null)
            mIssuesTask.cancel(Boolean.TRUE);
        mIssuesTask = new IssuesTask();
        mIssuesTask.execute(uid.toString());
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
        View view = inflater.inflate(R.layout.fragment_issues, container, false);

        mMainActivity = (MainActivity) getActivity();
        mMainActivity.GetNavMenu().getItem(2).setChecked(true);
        mMainActivity.setAppBarInfo(null, null);

        mLoadLayout = (LinearLayout) view.findViewById(R.id.layoutLoading);
        ((TextView) mLoadLayout.findViewById(R.id.textLoadingInfo)).setText(getResources().getString(R.string.loading_data));
        mEmptyDataLayout = (LinearLayout) view.findViewById(R.id.layoutEmpty);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerIssues);
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
        if (mIssuesTask != null)
            mIssuesTask.cancel(Boolean.TRUE);
    }

    @Override
    public void onClick(View view) {
        int pos = mRecyclerView.getChildAdapterPosition(view);

        TextView textIssueTitle = (TextView)view.findViewById(R.id.textTitle);
        TextView textProjectLetter = (TextView)view.findViewById(R.id.textProjectLetter);
        ColorDrawable bkgColor = (ColorDrawable) textProjectLetter.getBackground();

        try {
            Intent intent = new Intent(mMainActivity, IssueActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(IssueActivity.PARAM_BKG_COLOR, bkgColor.getColor());
            bundle.putString(IssueActivity.PARAM_PROJECT_LETTER, textProjectLetter.getText().toString());
            bundle.putString(IssueActivity.PARAM_ISSUE_TITLE, textIssueTitle.getText().toString());

            JSONObject item = mAdapter.getItem(pos);
            bundle.putInt(IssueActivity.PARAM_ISSUE_ID, item.getInt("id"));
            JSONArray partner_id = item.getJSONArray("partner_id");
            bundle.putString(IssueActivity.PARAM_CONTACT, (partner_id.length() > 0)?partner_id.get(1).toString():getResources().getString(R.string.undefined));
            bundle.putString(IssueActivity.PARAM_DESCRIPTION, item.getString("description"));
            JSONArray project_id = item.getJSONArray("project_id");
            bundle.putString(IssueActivity.PARAM_PROJECT_NAME, (project_id.length() > 0)?project_id.getString(1):getResources().getString(R.string.undefined));
            bundle.putString(IssueActivity.PARAM_MESSAGE_IDS, item.getString("message_ids"));
            JSONArray task_id = item.getJSONArray("task_id");
            bundle.putString(IssueActivity.PARAM_TASK_ASSIGNED, (task_id.length() > 0)?task_id.get(1).toString():getResources().getString(R.string.undefined));

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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


    private class IssuesTask extends AsyncTask<String, Void, Boolean> {
        private Exception mException;
        private JSONArray mIssuesInfo;
        private Integer mNumOldUnfinishedIssues = 0;
        private final Integer mNumWeeks = 1;

        protected Boolean doInBackground(String... params) {
            mException = null;
            try {
                Integer uid = ((AppMain)getActivity().getApplication()).getUID();
                JSONRPCClientOdoo odooClient = ((AppMain)mMainActivity.getApplication()).OdooClient();
                mIssuesInfo = odooClient.callExecute(
                        "search_read",
                        "project.issue",
                        odooClient.createStringDomain(String.format(Constants.OD_USER_ID, uid),
                                Constants.OD_KANBAN_NOT_BLOCKED,
                                Constants.OD_STAGE_OPEN),
                        "['project_id','create_date','name','id','description','message_ids','task_id','partner_id']"
                );

                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.WEEK_OF_MONTH, -mNumWeeks);
                final String frmtDateLong = EiquiUtils.dateToStringLong(cal.getTime());
                mNumOldUnfinishedIssues = odooClient.callCount(
                        "project.issue",
                        odooClient.createStringDomain(String.format(Constants.OD_USER_ID, uid),
                                Constants.OD_KANBAN_NOT_BLOCKED,
                                Constants.OD_STAGE_OPEN,
                                "['create_date', '<=', '"+frmtDateLong+"']")
                );

                return (mIssuesInfo!=null);
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

                // TODO: Actualizar 'lastIssueID'
                mMainActivity.setAppBarInfo(Integer.toString(mIssuesInfo.length()), getResources().getString(R.string.issues));
                if (mIssuesInfo.length() > 0) {
                    mAdapter = new IssuesAdapter(mIssuesInfo, IssuesFragment.this, mNumWeeks);
                    mRecyclerView.setAdapter(mAdapter);
                } else {
                    mEmptyDataLayout.setVisibility(View.VISIBLE);
                }

                if (mNumOldUnfinishedIssues > 0)
                    Snackbar.make(getView(), String.format(getResources().getString(R.string.old_issues), mNumOldUnfinishedIssues, mNumWeeks), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).setDuration(8000).show();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }
}
