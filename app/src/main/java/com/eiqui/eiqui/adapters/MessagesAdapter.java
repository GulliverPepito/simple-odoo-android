package com.eiqui.eiqui.adapters;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eiqui.eiqui.AppMain;
import com.eiqui.eiqui.R;
import com.eiqui.eiqui.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by uchar on 13/09/16.
 */
public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {
    private final JSONArray mDataset;
    private Integer mUID;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextAuthorName;
        private TextView mTextMessageBody;
        private TextView mTextMessageDate;
        private CircleImageView mImgAvatar;
        private CardView mCardView;
        private View mView;
        public ViewHolder(View v) {
            super(v);
            mView = v;
            mTextAuthorName = (TextView)v.findViewById(R.id.textAuthorName);
            mTextMessageBody = (TextView)v.findViewById(R.id.textMessageBody);
            mTextMessageDate = (TextView)v.findViewById(R.id.textMessageDate);
            mImgAvatar = (CircleImageView) v.findViewById(R.id.imgAvatar);
            mCardView = (CardView) v.findViewById(R.id.cv);
        }
    }

    public MessagesAdapter(JSONArray jsonDataset, Integer uid) {
        mDataset = jsonDataset;
        mUID = uid;
    }

    @Override
    public MessagesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_message, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            JSONObject jsonObj = mDataset.getJSONObject(position);
            if (jsonObj != null) {
                if (jsonObj.getJSONArray("create_uid").getInt(0) == mUID) {
                    holder.mCardView.setBackgroundResource(R.color.message_owner);
                } else
                    holder.mCardView.setBackgroundColor(-1);

                holder.mTextMessageBody.setText(Html.fromHtml(jsonObj.getString("body")));
                holder.mTextAuthorName.setText(jsonObj.getJSONArray("create_uid").getString(1));
                holder.mTextMessageDate.setText(jsonObj.getString("create_date"));

                // Update Avatar Image
                try {
                    byte[] decodedString = Base64.decode(jsonObj.getString("author_avatar"), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    holder.mImgAvatar.setImageBitmap(decodedByte);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    holder.mImgAvatar.setImageDrawable(holder.mView.getResources().getDrawable(R.drawable.no_image));
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

