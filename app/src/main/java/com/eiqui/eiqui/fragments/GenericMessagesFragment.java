// Copyright 2018 - Alexandre Díaz - <dev@redneboa.es>
package com.eiqui.eiqui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eiqui.eiqui.AppMain;
import com.eiqui.eiqui.R;
import com.eiqui.eiqui.activities.IssueActivity;
import com.eiqui.eiqui.adapters.MessagesAdapter;
import com.eiqui.odoojson_rpc.JSONRPCClientOdoo;
import com.eiqui.odoojson_rpc.exceptions.OdooSearchException;

import org.json.JSONArray;


public class GenericMessagesFragment extends Fragment {
    static public final String TAG = "GENERIC_MESSAGES";
    static public final String PARAM_MESSAGE_IDS = "messageIds";
    private FragmentActivity mActivity;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private MessagesAdapter mAdapter;
    private LinearLayout mLoadLayout;
    private MessagesTask mMessagesTask;

    public GenericMessagesFragment() {
        // Required empty public constructor
    }

    public static GenericMessagesFragment newInstance() {
        GenericMessagesFragment fragment = new GenericMessagesFragment();
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
        View view = inflater.inflate(R.layout.fragment_generic_messages, container, false);

        mActivity = (FragmentActivity) getActivity();
        mLoadLayout = (LinearLayout) view.findViewById(R.id.layoutLoading);
        ((TextView) mLoadLayout.findViewById(R.id.textLoadingInfo)).setText(getResources().getString(R.string.loading_messages));
        mRecyclerView = (RecyclerView)view.findViewById(R.id.recyclerMessages);

        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        Bundle bundle = getActivity().getIntent().getExtras();
        if (bundle != null) {
            mLoadLayout.setVisibility(View.VISIBLE);
            mLoadLayout.findViewById(R.id.progressLoading).setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);
            mMessagesTask = new MessagesTask();
            mMessagesTask.execute(bundle.getString(PARAM_MESSAGE_IDS));
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
        if (mMessagesTask != null)
            mMessagesTask.cancel(Boolean.TRUE);
    }


    private class MessagesTask extends AsyncTask<String, Void, Boolean> {
        private Exception mException;
        private JSONArray mMessagesInfo;

        protected Boolean doInBackground(String... params) {
            mException = null;
            try {
                mMessagesInfo = ((AppMain) mActivity.getApplication()).OdooClient().callExecute(
                        "read",
                        "mail.message",
                        params[0],
                        "['create_uid','create_date','author_avatar','body']"
                );
                return (mMessagesInfo!=null);
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

                if (mMessagesInfo.length() > 0) {
                    SharedPreferences settings = mActivity.getSharedPreferences("UserInfo", 0);
                    mAdapter = new MessagesAdapter(mMessagesInfo, settings.getInt("UserID", -1));
                    mRecyclerView.setAdapter(mAdapter);

                    mLoadLayout.setVisibility(View.INVISIBLE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

}
