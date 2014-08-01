package com.qianchen.sportsbuddy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Qian Chen on 7/31/2014.
 */
public class DiscussionAdapter extends BaseAdapter {

    Context context;
    List<DiscussionPost> discussionList;
    SimpleDateFormat simpleDateFormat;

    public DiscussionAdapter(Context context, List<DiscussionPost> discussion){
        this.context = context;
        this.discussionList = discussion;
        simpleDateFormat = new SimpleDateFormat("MMM dd");

        // register DiscussionPost as the subclass of ParseObject
        ParseObject.registerSubclass(DiscussionPost.class);

        // authenticates this client to Parse
        Parse.initialize(context, context.getString(R.string.application_id), context.getString(R.string.client_key));
    }

    @Override
    public int getCount() {
        return discussionList.size();
    }

    @Override
    public Object getItem(int position) {
        return discussionList.get(position);
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
            view = inflater.inflate(R.layout.list_item_discussion,null);
        }
        DiscussionPost discussion = discussionList.get(position);
        ((TextView)view.findViewById(R.id.discussion_name)).setText(discussion.getAuthor());
        ((TextView) view.findViewById(R.id.discussion_date)).setText(simpleDateFormat.format(discussion.getCreatedAt()));
        ((TextView)view.findViewById(R.id.discussion_topic)).setText(discussion.getTitle());
        ((TextView)view.findViewById(R.id.discussion_content)).setText(discussion.getContent());
        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        // try to load from the cache; but if that fails, load results from the network
        userQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        ParseUser user = null;
        try {
            user = userQuery.get(discussion.getAuthorID());
        } catch (ParseException e) {
            // todo
        }
        ParseImageView imageView = (ParseImageView) view.findViewById(R.id.discussion_avatar);
        ParseFile test = user.getParseFile("avatar");
        imageView.setParseFile(test);
        imageView.loadInBackground();
        return view;
    }
}