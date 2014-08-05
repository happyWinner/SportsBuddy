package com.qianchen.sportsbuddy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * {@link ReplyAdapter} exposes a list of teams to a {@link android.widget.ListView}.
 *
 * Created by Qian Chen on 8/1/2014.
 */
public class ReplyAdapter extends BaseAdapter {

    private Context context;
    private List<DiscussionReply> replies;
    SimpleDateFormat simpleDateFormat;

    public ReplyAdapter(Context context, List<DiscussionReply> replies) {
        this.context = context;
        this.replies = replies;
        simpleDateFormat = new SimpleDateFormat("MMM dd");
    }

    @Override
    public int getCount() {
        return replies.size();
    }

    @Override
    public Object getItem(int position) {
        return replies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_reply, null);
        }

        DiscussionReply reply = replies.get(position);
        if (reply != null) {
            ((TextView) view.findViewById(R.id.reply_name)).setText(reply.getUserName());
            ((TextView) view.findViewById(R.id.reply_content)).setText(reply.getReplyMessage());
            ((TextView) view.findViewById(R.id.reply_date)).setText(simpleDateFormat.format(reply.getCreatedAt()));
            ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
            // try to load from the cache; but if that fails, load results from the network
            userQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
            userQuery.whereEqualTo("username", reply.getUserName());
            ParseUser user = null;
            try {
                user = userQuery.getFirst();
            } catch (ParseException e) {
            }
            ParseImageView imageView = (ParseImageView) view.findViewById(R.id.reply_avatar);
            imageView.setParseFile(user.getParseFile("avatar"));
            imageView.loadInBackground();
        }

        return view;
    }
}
