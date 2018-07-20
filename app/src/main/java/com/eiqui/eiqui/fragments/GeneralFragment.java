// Copyright 2018 - Alexandre Díaz - <dev@redneboa.es>
package com.eiqui.eiqui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.eiqui.eiqui.AppMain;
import com.eiqui.eiqui.utils.Constants;
import com.eiqui.eiqui.interfaces.IEiquiFragment;
import com.eiqui.eiqui.activities.MainActivity;
import com.eiqui.eiqui.R;
import com.eiqui.odoojson_rpc.JSONRPCClientOdoo;
import com.eiqui.odoojson_rpc.exceptions.OdooSearchException;


public class GeneralFragment extends Fragment implements IEiquiFragment {
    static public final String TAG = "GENERAL";
    private MainActivity mMainActivity;
    private ProgressBar mProgressTasks;
    private ProgressBar mProgressIssues;
    private TextView mTextNumTasks;
    private TextView mTextNumIssues;
    private CardView mCardTasks;
    private CardView mCardIssues;
    private LinearLayout mLoadLayout;
    private ImageView mImageCardTasks;
    private ImageView mImageCardIssues;
    private TextView mTextNumProjects;


    public GeneralFragment() {
        // Required empty public constructor
    }

    public static GeneralFragment newInstance() {
        GeneralFragment fragment = new GeneralFragment();
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
        View view = inflater.inflate(R.layout.fragment_general, container, false);


        mTextNumTasks = (TextView) view.findViewById(R.id.textNumTask);
        mTextNumIssues = (TextView) view.findViewById(R.id.textNumIssues);
        mCardTasks = (CardView) view.findViewById(R.id.cardTasks);
        mCardIssues = (CardView) view.findViewById(R.id.cardIssues);
        mLoadLayout = (LinearLayout) view.findViewById(R.id.layoutLoading);
        ((TextView) mLoadLayout.findViewById(R.id.textLoadingInfo)).setText(getResources().getString(R.string.loading_data));
        mProgressTasks = (ProgressBar) view.findViewById(R.id.progressTasks);
        mProgressIssues = (ProgressBar) view.findViewById(R.id.progressIssues);
        mImageCardTasks = (ImageView) view.findViewById(R.id.imgCardTasks);
        mImageCardIssues = (ImageView) view.findViewById(R.id.imgCardIssues);
        mTextNumProjects = (TextView) view.findViewById(R.id.textNumProjects);

        mMainActivity = (MainActivity) getActivity();
        mMainActivity.GetNavMenu().getItem(0).setChecked(true);
        mMainActivity.setAppBarInfo(null, null);

        mCardTasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMainActivity.switchFragment(TasksFragment.newInstance(), TasksFragment.TAG);
            }
        });

        mCardIssues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMainActivity.switchFragment(IssuesFragment.newInstance(), IssuesFragment.TAG);
            }
        });

        refreshData();

        return view;
    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);

        /*int cx = getView().getRight() - 30;
        int cy = getView().getBottom() - 60;
        int finalRadius = Math.max(getView().getWidth(), getView().getHeight());
        Animator anim = ViewAnimationUtils.createCircularReveal(getView(), cx, cy, 0, finalRadius);
        getView().setVisibility(View.VISIBLE);
        anim.start();*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    public void refreshData() {
        mCardTasks.setVisibility(View.GONE);
        mCardIssues.setVisibility(View.GONE);
        mLoadLayout.setVisibility(View.VISIBLE);
        mLoadLayout.findViewById(R.id.progressLoading).setVisibility(View.VISIBLE);
        mTextNumProjects.setVisibility(View.GONE);

        new GeneralInfoTask().execute();
    }

    private class GeneralInfoTask extends AsyncTask<String, Void, Boolean> {
        private Exception mException;
        private Integer mNumTasksUnfinished = 0;
        private Integer mNumTasks = 0;
        private Integer mNumIssuesUnfinished = 0;
        private Integer mNumIssues = 0;
        private Integer mNumProjects = 0;

        protected Boolean doInBackground(String... params) {
            mException = null;
            try {
                Integer uid = ((AppMain)getActivity().getApplication()).getUID();
                Integer[] Counts = new Integer[5]; // Total Unfinished, Total, Total Unfinised (Issues), Total (Issues), Total Projects
                JSONRPCClientOdoo odooClient = ((AppMain)mMainActivity.getApplication()).OdooClient();
                mNumTasksUnfinished = odooClient.callCount(
                        "project.task",
                        odooClient.createStringDomain(String.format(Constants.OD_USER_ID, uid),
                                Constants.OD_KANBAN_NOT_BLOCKED,
                                Constants.OD_STAGE_OPEN)
                );
                mNumTasks = odooClient.callCount(
                        "project.task",
                        odooClient.createStringDomain(String.format(Constants.OD_USER_ID, uid),
                                Constants.OD_KANBAN_NOT_BLOCKED)
                );
                mNumIssuesUnfinished = odooClient.callCount(
                        "project.issue",
                        odooClient.createStringDomain(String.format(Constants.OD_USER_ID, uid),
                                Constants.OD_KANBAN_NOT_BLOCKED,
                                Constants.OD_STAGE_OPEN)
                );
                mNumIssues = odooClient.callCount(
                        "project.issue",
                        odooClient.createStringDomain(String.format(Constants.OD_USER_ID, uid),
                                Constants.OD_KANBAN_NOT_BLOCKED)
                );
                mNumProjects = odooClient.callCount(
                        "project.project",
                        odooClient.createStringDomain(String.format(Constants.OD_USER_ID_IN_MEMBER, uid))
                );

                return Boolean.TRUE;
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
            }  else if (!res) {
                ((TextView)mLoadLayout.findViewById(R.id.textLoadingInfo)).setText(getResources().getString(R.string.oops_error_undefined));
                mLoadLayout.findViewById(R.id.progressLoading).setVisibility(View.GONE);
                return;
            }

            try {
                mImageCardTasks.setImageDrawable(getResources().getDrawable(mNumTasksUnfinished == 0 ? R.drawable.ic_card_tasks_god : R.drawable.ic_card_tasks_bad, getContext().getTheme()));
                mImageCardIssues.setImageDrawable(getResources().getDrawable(mNumIssuesUnfinished == 0 ? R.drawable.ic_card_issues_god : R.drawable.ic_card_issues_bad, getContext().getTheme()));

                mTextNumTasks.setText(mNumTasksUnfinished.toString());
                mTextNumIssues.setText(mNumIssuesUnfinished.toString());

                mProgressTasks.setMax(mNumTasks);
                mProgressTasks.setProgress(mNumTasks - mNumTasksUnfinished);
                mProgressIssues.setMax(mNumIssues);
                mProgressIssues.setProgress(mNumIssues - mNumIssuesUnfinished);

                mTextNumProjects.setText(mNumProjects.toString() + " "+getResources().getString(R.string.projects).toUpperCase());
                mTextNumProjects.setAlpha(0.0f);
                mTextNumProjects.setTranslationY(-100);
                mTextNumProjects.setVisibility(View.VISIBLE);
                mTextNumProjects.animate().alpha(1.0f).translationY(0.0f).setStartDelay(100);

                mCardTasks.setTranslationX(-200);
                mCardTasks.setAlpha(0.0f);
                mCardTasks.setVisibility(View.VISIBLE);
                mCardTasks.animate().alpha(1.0f).translationX(0);

                mCardIssues.setTranslationX(200);
                mCardIssues.setAlpha(0.0f);
                mCardIssues.setVisibility(View.VISIBLE);
                mCardIssues.animate().alpha(1.0f).translationX(0);

                mLoadLayout.setVisibility(View.GONE);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }
}
