package com.kashyapjha.wifichat;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CustomAdapter extends BaseAdapter
{
    private Context mContext;
    private ArrayList<Message> mMessages;
    public CustomAdapter(Context context, ArrayList<Message> messages)
    {
        super();
        this.mContext = context;
        this.mMessages = messages;
    }

    @Override
    public int getCount()
    {
        return mMessages.size();
    }

    @Override
    public Object getItem(int position)
    {
        return mMessages.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Message message = (Message) this.getItem(position);

        ViewHolder holder;
        if(convertView == null)
        {
            holder = new ViewHolder();
            if (message.isMine())
                convertView = LayoutInflater.from(mContext).inflate(R.layout.right_row, parent, false);
            else convertView = LayoutInflater.from(mContext).inflate(R.layout.left_row, parent, false);

            holder.message = (TextView) convertView.findViewById(R.id.message_text);
            holder.messageLayout=(LinearLayout) convertView.findViewById(R.id.messageLayout);
            holder.timestamp=(TextView)convertView.findViewById(R.id.timestamp);
            convertView.setTag(holder);
        }
        else
            holder = (ViewHolder) convertView.getTag();

        holder.message.setTextColor(Color.WHITE);
        holder.message.setText(message.getMessage());
        Date now=new Date();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("hh:mm");
        holder.timestamp.setText(simpleDateFormat.format(now));

        LayoutParams lp = (LayoutParams) holder.messageLayout.getLayoutParams();

        if(message.isMine() || message.getMessage().startsWith("Me"))
        {
            lp.gravity = Gravity.RIGHT;
        }
        else
        {
            lp.gravity = Gravity.LEFT;
        }
        holder.message.setLayoutParams(lp);
        return convertView;
    }

    private static class ViewHolder
    {
        LinearLayout messageLayout;
        TextView message;
        TextView timestamp;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

}