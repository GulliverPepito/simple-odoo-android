// Copyright 2018 - Alexandre Díaz - <dev@redneboa.es>
package com.eiqui.eiqui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eiqui.eiqui.R;
import com.eiqui.eiqui.utils.EiquiUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Created by uchar on 13/09/16.
 */
public class IssuesAdapter extends RecyclerView.Adapter<IssuesAdapter.ViewHolder> {
    private final JSONArray mDataset;
    private final View.OnClickListener mOnClickListener;
    private Integer mLastPos = 0;
    private Integer mAnimDelayMult = 0;
    private Integer mWeeks = 1;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextProjectLetter;
        private TextView mTextIssueTitle;
        private TextView mTextIssueID;
        private View mView;
        public ViewHolder(View v) {
            super(v);

            mView = v;
            mTextProjectLetter = (TextView)v.findViewById(R.id.textProjectLetter);
            mTextIssueTitle = (TextView)v.findViewById(R.id.textTitle);
            mTextIssueID = (TextView)v.findViewById(R.id.textID);
        }
    }

    public IssuesAdapter(JSONArray jsonDataset, View.OnClickListener onClickListener, Integer weeks) {
        mDataset = jsonDataset;
        mOnClickListener = onClickListener;
        mWeeks = weeks;
    }

    @Override
    public IssuesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_tasks_issues, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final int diff = position-mLastPos;
        if (diff != 1) {
            mLastPos = position;
            mAnimDelayMult = 0;
        }
        else
            ++mAnimDelayMult;

        holder.mView.setOnClickListener(mOnClickListener);
        holder.mView.setAlpha(0.0f);
        holder.mView.setTranslationY(200);
        holder.mView.animate().alpha(1.0f).translationY(0).setStartDelay(100*mAnimDelayMult).setDuration(600);

        try {
            JSONObject jsonObj = mDataset.getJSONObject(position);
            if (jsonObj != null) {
                char ascii = jsonObj.getJSONArray("project_id").getString(1).toUpperCase().charAt(0);
                holder.mTextProjectLetter.setText("" + ascii);
                holder.mTextIssueTitle.setText(jsonObj.getString("name"));
                holder.mTextIssueID.setText("#" + ((Integer) jsonObj.getInt("id")).toString());
                holder.mTextProjectLetter.setBackgroundColor(EiquiUtils.generateColorRandom(ascii));

                if (EiquiUtils.compareDateToday(jsonObj.getString("create_date"), Calendar.MONTH, -mWeeks) >= 0) {
                    holder.mView.setBackgroundColor(holder.mView.getResources().getColor(R.color.old_item));
                } else {
                    holder.mView.setBackgroundColor(-1);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.length();
    }

    public JSONObject getItem(int position) {
        JSONObject res = null;
        try {
            res = mDataset.getJSONObject(position);
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return res;
    }
}

