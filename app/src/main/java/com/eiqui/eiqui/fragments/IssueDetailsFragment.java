package com.eiqui.eiqui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.eiqui.eiqui.R;
import com.eiqui.eiqui.activities.IssueActivity;


public class IssueDetailsFragment extends Fragment {
    static public final String TAG = "TASK_DETAILS";
    private IssueActivity mIssueActivity;
    private ListView mListActivities;

    public IssueDetailsFragment() {
        // Required empty public constructor
    }

    public static IssueDetailsFragment newInstance() {
        IssueDetailsFragment fragment = new IssueDetailsFragment();
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
        View view = inflater.inflate(R.layout.fragment_issue_details, container, false);

        mIssueActivity = (IssueActivity) getActivity();

        TextView textReviewerName = (TextView)view.findViewById(R.id.textContactName);
        TextView textDescription = (TextView)view.findViewById(R.id.textDescription);
        TextView textProjectName = (TextView)view.findViewById(R.id.textProjectName);
        TextView textTaskAssigned = (TextView)view.findViewById(R.id.textTaskAssigned);
        mListActivities = (ListView)view.findViewById(R.id.listActivities);

        Bundle bundle = getActivity().getIntent().getExtras();
        if (bundle != null) {
            textReviewerName.setText(bundle.getString(IssueActivity.PARAM_CONTACT));
            textDescription.setText(bundle.getString(IssueActivity.PARAM_DESCRIPTION));
            textProjectName.setText(bundle.getString(IssueActivity.PARAM_PROJECT_NAME));
            textTaskAssigned.setText(bundle.getString(IssueActivity.PARAM_TASK_ASSIGNED));
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
    }

}
