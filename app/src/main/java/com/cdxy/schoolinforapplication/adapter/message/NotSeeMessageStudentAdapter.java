package com.cdxy.schoolinforapplication.adapter.message;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cdxy.schoolinforapplication.R;
import com.cdxy.schoolinforapplication.model.message.MessageEntity;
import com.cdxy.schoolinforapplication.model.message.SeeMeaaseStudentEntity;
import com.cdxy.schoolinforapplication.ui.widget.ChooseWayDialog;
import com.cdxy.schoolinforapplication.util.Constant;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by huihui on 2016/12/27.
 */

public class NotSeeMessageStudentAdapter extends BaseAdapter {
    private Context context;
    private List<SeeMeaaseStudentEntity> list;
    private MessageEntity messageEntity;
    private ChooseWayDialog chooseWayDialog;
    private Activity activity;
    private String isQuee;

    public NotSeeMessageStudentAdapter(Context context, List<SeeMeaaseStudentEntity> list, MessageEntity messageEntity, Activity activity, String isQuee) {
        this.context = context;
        this.list = list;
        this.messageEntity = messageEntity;
        this.activity = activity;
        this.isQuee = isQuee;
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
            view = LayoutInflater.from(context).inflate(R.layout.item_not_see_message_students, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        if (isQuee.equals("yes")){
            viewHolder.lyIsCall.setVisibility(View.GONE);
        }else {
            viewHolder.lyIsCall.setVisibility(View.VISIBLE);
        }
        final SeeMeaaseStudentEntity entity = (SeeMeaaseStudentEntity) getItem(i);
        String name = entity.getName();
        if (!TextUtils.isEmpty(name))
            viewHolder.txtNotSeeMessageStudentName.setText(name);
        String id = entity.getXuehao();
        if (!TextUtils.isEmpty(id))
            viewHolder.txtNotSeeMessageStudentId.setText(id);
        String department = entity.getXibie();
        if (!TextUtils.isEmpty(department))
            viewHolder.txtNotSeeMessageStudentDepartment.setText(department);
        String clazz = entity.getBanji();
        if (!TextUtils.isEmpty(clazz))
            viewHolder.txtNotSeeMessageStudentClazz.setText(clazz);
        final String phoneNumber = entity.getUserid();
        if (!TextUtils.isEmpty(phoneNumber))
            viewHolder.txtNoSeeMessageStudentPhoneNumber.setText(phoneNumber);
        viewHolder.txtNoSeeMessageStudentPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseWayDialog = new ChooseWayDialog(context, R.style.MyDialog, new ChooseWayDialog.ChooseWayDialogListener() {
                    @Override
                    public void onClick(View view) {
                        switch (view.getId()) {
                            case R.id.txt_way1:
                                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
                                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.

                                    return;
                                }
                                context.startActivity(intent);
                                chooseWayDialog.dismiss();
                                break;
                            case R.id.txt_way2:
                                intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNumber));
                                String sender = messageEntity.getSendPersonName();
                                String content = messageEntity.getContent();
                                if ((!TextUtils.isEmpty(sender)) && (!TextUtils.isEmpty(content)))
                                    intent.putExtra("sms_body", content + "    --" + sender);
                                context.startActivity(intent);
                                chooseWayDialog.dismiss();
                                break;
                        }
                    }
                }, activity, Constant.CHOOSE_WAY_DIALOG_TYPE_NOTIFY_NOT_SEE);
                chooseWayDialog.show();
            }
        });
        return view;
    }


    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    static class ViewHolder {
        @BindView(R.id.txt_not_see_message_student_id)
        TextView txtNotSeeMessageStudentId;
        @BindView(R.id.txt_not_see_message_student_name)
        TextView txtNotSeeMessageStudentName;
        @BindView(R.id.txt_not_see_message_student_department)
        TextView txtNotSeeMessageStudentDepartment;
        @BindView(R.id.txt_not_see_message_student_clazz)
        TextView txtNotSeeMessageStudentClazz;
        @BindView(R.id.txt_no_see_message_student_phoneNumber)
        TextView txtNoSeeMessageStudentPhoneNumber;
        @BindView(R.id.ly_is_call)
        LinearLayout lyIsCall;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
