package com.cdxy.schoolinforapplication.adapter.message;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cdxy.schoolinforapplication.R;
import com.cdxy.schoolinforapplication.model.message.MessageEntity;
import com.cdxy.schoolinforapplication.util.Constant;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by huihui on 2016/12/27.
 */

public class MessageListAdapter extends BaseAdapter {
    private Context context;
    private List<MessageEntity> list;
    private int messageFrom;

    public MessageListAdapter(Context context, List<MessageEntity> list, int messageFrom) {
        this.context = context;
        this.list = list;
        this.messageFrom = messageFrom;
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_list, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        MessageEntity entity = (MessageEntity) getItem(i);
        String title = entity.getTitle();
        if (!TextUtils.isEmpty(title))
            viewHolder.txtMessageTitle.setText(title);
        String sender = entity.getSendPersonName();
        if (!TextUtils.isEmpty(sender))
            viewHolder.txtMessageSender.setText(sender);
        ArrayList<String> acceptGroup = entity.getSendTo();
        String acceptGroupString = "";
        if (acceptGroup != null) {
            if (acceptGroup.size() != 0) {
                for (int j = 0; j < acceptGroup.size() - 1; j++) {
                    acceptGroupString += acceptGroup.get(j) + "、";
                }
                acceptGroupString += acceptGroup.get(acceptGroup.size() - 1);
            }
        }
        viewHolder.txtMessageAcceptGroup.setText(acceptGroupString);
        String sendTime = entity.getTime();
        if (!TextUtils.isEmpty(sendTime))
            viewHolder.txtMessageSendTime.setText(sendTime);
        int messageType = entity.getMessageType();
        if (messageFrom == Constant.MY_SEND_MESSAGE && messageType == 1)
            viewHolder.flagImportantMessage.setVisibility(View.VISIBLE);
        return view;
    }

    class ViewHolder {
        @BindView(R.id.img_message_sender_icon)
        ImageView imgMessageSenderIcon;
        @BindView(R.id.flag_important_message)
        TextView flagImportantMessage;
        @BindView(R.id.txt_message_title)
        TextView txtMessageTitle;
        @BindView(R.id.txt_message_sender)
        TextView txtMessageSender;
        @BindView(R.id.txt_message_accept_group)
        TextView txtMessageAcceptGroup;
        @BindView(R.id.txt_message_send_time)
        TextView txtMessageSendTime;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
