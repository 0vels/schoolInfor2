package com.cdxy.schoolinforapplication.ui.Message;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cdxy.schoolinforapplication.R;
import com.cdxy.schoolinforapplication.ScreenManager;
import com.cdxy.schoolinforapplication.adapter.message.NotSeeMessageStudentAdapter;
import com.cdxy.schoolinforapplication.model.message.MessageEntity;
import com.cdxy.schoolinforapplication.model.message.NotSeeMessageStudentEntity;
import com.cdxy.schoolinforapplication.ui.base.BaseActivity;
import com.cdxy.schoolinforapplication.ui.widget.ScrollListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotSeeMessageStudentsActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.txt_title)
    TextView txtTitle;
    @BindView(R.id.btn_right)
    Button btnRight;
    @BindView(R.id.layout_show_progressBar)
    LinearLayout layoutShowProgressBar;
    @BindView(R.id.txt_not_see_message_student_number)
    TextView txtNotSeeMessageStudentNumber;
    @BindView(R.id.scroll_not_see_message_student)
    ScrollListView scrollNotSeeMessageStudent;
    @BindView(R.id.activity_not_see_message_students)
    LinearLayout activityNotSeeMessageStudents;
    private NotSeeMessageStudentAdapter adapter;
    private List<NotSeeMessageStudentEntity> list;
    private MessageEntity messageEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_not_see_message_students);
        ButterKnife.bind(this);
        ScreenManager.getScreenManager().pushActivity(this);
        init();
        getNotSeeMessageStudentInfor();
    }

    @Override
    public void init() {
        Intent intent=getIntent();
        messageEntity= (MessageEntity) intent.getSerializableExtra("messageEntity");
        txtTitle.setText("未查看名单");
//        list=new ArrayList<>();
//        adapter=new NotSeeMessageStudentAdapter(NotSeeMessageStudentsActivity.this,list,messageEntity,NotSeeMessageStudentsActivity.this);
//        scrollNotSeeMessageStudent.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                ScreenManager.getScreenManager().popActivty(this);
                break;
        }
    }
    //测试数据
    private void getNotSeeMessageStudentInfor(){
        new AsyncTask<Void, Void, List<NotSeeMessageStudentEntity>>() {
            @Override
            protected List<NotSeeMessageStudentEntity> doInBackground(Void... voids) {
                List<NotSeeMessageStudentEntity> notSeeMessageStudentEntities=new ArrayList<>();
                NotSeeMessageStudentEntity entity1=new NotSeeMessageStudentEntity("kaylee","1340219012","计算机系","游戏开发","1340918383");
                NotSeeMessageStudentEntity entity2=new NotSeeMessageStudentEntity("Andy","1340219321","计算机系","软件测试","1340918123");
                notSeeMessageStudentEntities.add(entity1);
                notSeeMessageStudentEntities.add(entity2);
                return notSeeMessageStudentEntities;
            }

            @Override
            protected void onPostExecute(List<NotSeeMessageStudentEntity> notSeeMessageStudentEntities) {
                super.onPostExecute(notSeeMessageStudentEntities);
                if (notSeeMessageStudentEntities!=null){
                    list.addAll(notSeeMessageStudentEntities);
                    adapter.notifyDataSetChanged();
                    txtNotSeeMessageStudentNumber.setText(notSeeMessageStudentEntities.size()+"");
                    layoutShowProgressBar.setVisibility(View.GONE);
                }
            }
        }.execute();
    }
}
