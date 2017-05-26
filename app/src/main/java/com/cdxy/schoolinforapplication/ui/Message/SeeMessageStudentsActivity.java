package com.cdxy.schoolinforapplication.ui.Message;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cdxy.schoolinforapplication.HttpUrl;
import com.cdxy.schoolinforapplication.R;
import com.cdxy.schoolinforapplication.SchoolInforManager;
import com.cdxy.schoolinforapplication.ScreenManager;
import com.cdxy.schoolinforapplication.adapter.message.NotSeeMessageStudentAdapter;
import com.cdxy.schoolinforapplication.model.QuerenOrNotReturnEntity;
import com.cdxy.schoolinforapplication.model.message.MessageEntity;
import com.cdxy.schoolinforapplication.model.message.SeeMeaaseStudentEntity;
import com.cdxy.schoolinforapplication.ui.base.BaseActivity;
import com.cdxy.schoolinforapplication.ui.widget.ScrollListView;
import com.cdxy.schoolinforapplication.util.HttpUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

public class SeeMessageStudentsActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.txt_title)
    TextView txtTitle;
    @BindView(R.id.btn_right)
    Button btnRight;
    @BindView(R.id.layout_show_progressBar)
    LinearLayout layoutShowProgressBar;
    @BindView(R.id.txt_see_message_student_number)
    TextView txtSeeMessageStudentNumber;
    @BindView(R.id.scroll_see_message_student)
    ScrollListView scrollSeeMessageStudent;
    @BindView(R.id.activity_see_message_students)
    LinearLayout activitySeeMessageStudents;
    @BindView(R.id.tv_see_students_number)
    TextView tvSeeStudentsNumber;
    private NotSeeMessageStudentAdapter adapter;
    private List<SeeMeaaseStudentEntity> list;
    String isQueren;
    String T;

    /*

    确认和未确认的学生都在这个Activity，只是传入的值不同，获取到的数据不同

     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_message_students);
        ScreenManager.getScreenManager().pushActivity(this);
        ButterKnife.bind(this);
        int TID = getIntent().getIntExtra("TID", 0);
        isQueren = getIntent().getStringExtra("isQueren");
        T = getIntent().getStringExtra("T");
        init();
        getSeeMessageStudents(TID, isQueren);


    }

    @Override
    public void init() {
        if (isQueren.equals("yes")) {
            txtTitle.setText("查看情况");
            tvSeeStudentsNumber.setText("查看消息学生数：");
//            btnRight.setText("未查看提醒");
        } else {
            txtTitle.setText("未查看名单");
//            btnRight.setText("未查看提醒");
            tvSeeStudentsNumber.setText("未查看消息学生数：");

        }

        list = new ArrayList<>();
        adapter = new NotSeeMessageStudentAdapter(SeeMessageStudentsActivity.this, list, (MessageEntity) getIntent().getSerializableExtra("message"), SeeMessageStudentsActivity.this, isQueren);
        scrollSeeMessageStudent.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                ScreenManager.getScreenManager().popActivty(this);
                break;
            case R.id.btn_right:
                Intent intent = new Intent(SeeMessageStudentsActivity.this, NotSeeMessageStudentsActivity.class);
                intent.putExtra("messageEntity", getIntent().getSerializableExtra("messageEntity"));
                startActivity(intent);
        }
    }


    //测试数据
    private void getSeeMessageStudents(final int TID, final String isQueren) {
        new AsyncTask<Void, Void, List<SeeMeaaseStudentEntity>>() {
            @Override
            protected List<SeeMeaaseStudentEntity> doInBackground(Void... voids) {
//                List<SeeMeaaseStudentEntity> seeMeaaseStudentEntities = new ArrayList<>();
//                SeeMeaaseStudentEntity entity1 = new SeeMeaaseStudentEntity("zhang", "1340610232", "计算机系", "嵌入式1班", "2016-12-27");
//                SeeMeaaseStudentEntity entity2 = new SeeMeaaseStudentEntity("li", "1340610222", "计算机系", "软件技术1班", "2016-12-21");
//                seeMeaaseStudentEntities.add(entity1);
//                seeMeaaseStudentEntities.add(entity2);
//                return seeMeaaseStudentEntities;
                return getQuerenOrNot(TID, isQueren);
            }

            @Override
            protected void onPostExecute(List<SeeMeaaseStudentEntity> seeMeaaseStudentEntities) {
                super.onPostExecute(seeMeaaseStudentEntities);
                if (seeMeaaseStudentEntities != null) {
                    list.clear();
                    list.addAll(seeMeaaseStudentEntities);
                    adapter.notifyDataSetChanged();
                    layoutShowProgressBar.setVisibility(View.GONE);
                    txtSeeMessageStudentNumber.setText(seeMeaaseStudentEntities.size() + "");
                }
            }
        }.execute();
    }


    private List<SeeMeaaseStudentEntity> getQuerenOrNot(final int TID, final String isQueren) {
        final List<SeeMeaaseStudentEntity> returnList = new ArrayList<>();
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                OkHttpClient okHttpClient = HttpUtil.getClient();
                SeeMeaaseStudentEntity messageEntity = new SeeMeaaseStudentEntity();

                Request request = new Request.Builder().url(HttpUrl.GET_QUERENORNOT + "?TID=" + TID + "&isQueren=" + isQueren)
                        .get()
                        .build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    String result = response.body().string();

                    subscriber.onNext(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                int messageNumber;
                QuerenOrNotReturnEntity<List<SeeMeaaseStudentEntity>> returnEntity = SchoolInforManager.gson.fromJson(s, QuerenOrNotReturnEntity.class);
                if (returnEntity.getCode() == 1) {
                    List<SeeMeaaseStudentEntity> seeMeaaseStudentEntityList = returnEntity.getData();
//                        returnList.addAll(returnTopicList);
                    messageNumber = seeMeaaseStudentEntityList.size();
                    for (int j = 0; j < messageNumber; j++) {
//                        long TID = seeMeaaseStudentEntityList.get(j).getTID();
                        String userid = seeMeaaseStudentEntityList.get(j).getUserid();
                        if (TID != 0 && (!TextUtils.isEmpty(userid))) {
                            SeeMeaaseStudentEntity messageEntity = new SeeMeaaseStudentEntity();
                            messageEntity = seeMeaaseStudentEntityList.get(j);
                            messageEntity.setQueshi(T);
                            messageEntity.setBanji(seeMeaaseStudentEntityList.get(j).getBanji());
                            messageEntity.setXibie(seeMeaaseStudentEntityList.get(j).getXibie());
                            messageEntity.setUserid(userid);
                            messageEntity.setName(seeMeaaseStudentEntityList.get(j).getName());
                            messageEntity.setPhone(seeMeaaseStudentEntityList.get(j).getPhone());
                            returnList.add(messageEntity);
                        }
                    }
                }
            }
        });
        return returnList;
    }
}
