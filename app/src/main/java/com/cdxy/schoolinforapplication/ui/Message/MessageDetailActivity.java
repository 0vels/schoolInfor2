package com.cdxy.schoolinforapplication.ui.Message;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cdxy.schoolinforapplication.HttpUrl;
import com.cdxy.schoolinforapplication.R;
import com.cdxy.schoolinforapplication.SchoolInforManager;
import com.cdxy.schoolinforapplication.ScreenManager;
import com.cdxy.schoolinforapplication.model.MessageReturnEntity;
import com.cdxy.schoolinforapplication.model.message.MessageEntity;
import com.cdxy.schoolinforapplication.ui.base.BaseActivity;
import com.cdxy.schoolinforapplication.util.HttpUtil;
import com.cdxy.schoolinforapplication.util.SharedPreferenceManager;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
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

public class MessageDetailActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.txt_title)
    TextView txtTitle;
    @BindView(R.id.btn_right)
    Button btnRight;
    @BindView(R.id.button6)
    Button querenbtn;
    @BindView(R.id.noseebtn)
    Button noseebtn;
    @BindView(R.id.button7)
    Button queren;
    @BindView(R.id.txt_message_detail_title)
    TextView txtMessageDetailTitle;
    @BindView(R.id.txt_message_detail_content)
    TextView txtMessageDetailContent;
    @BindView(R.id.txt_message_detail_accept_group)
    TextView txtMessageDetailAcceptGroup;
    @BindView(R.id.txt_message_detail_sender)
    TextView txtMessageDetailSender;
    @BindView(R.id.txt_message_detail_send_time)
    TextView txtMessageDetailSendTime;
    @BindView(R.id.activity_message_detail)
    LinearLayout activityMessageDetail;
    private MessageEntity messageEntity;
    private String messageType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_detail);
        ButterKnife.bind(this);
        ScreenManager.getScreenManager().pushActivity(this);
        init();
        onClick();
        String title = messageEntity.getTitle();
        if (!TextUtils.isEmpty(title))
            txtMessageDetailTitle.setText(title);
        String content = messageEntity.getContent();
        if (!TextUtils.isEmpty(content))
            txtMessageDetailContent.setText("  " + content);
        String acceptGroup = messageEntity.getSendTo();
        if (!TextUtils.isEmpty(acceptGroup))
            txtMessageDetailAcceptGroup.setText(acceptGroup);
        String sender = messageEntity.getSendPersonName();
        if (!TextUtils.isEmpty(sender))
            txtMessageDetailSender.setText(sender);
        String sendTime = messageEntity.getTime();
        if (!TextUtils.isEmpty(sendTime))
            txtMessageDetailSendTime.setText(sendTime);
        int type = messageEntity.getMessageType();
//        switch (messageType) {
//            case Constant.MY_SEND_MESSAGE:
//                if (type==1) {
//                    btnRight.setText("查看情况");
//                    btnRight.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            Intent intent=new Intent(MessageDetailActivity.this,SeeMessageStudentsActivity.class);
//                            intent.putExtra("messageEntity",messageEntity);
//                            startActivity(intent);
//                        }
//                    });
//                }
//                break;
//        }
    }

    @Override
    public void init() {
        txtTitle.setText("消息详情");
        Intent intent = getIntent();
        messageEntity = (MessageEntity) intent.getSerializableExtra("message");
    }

    private void onClick() {

        //查看已确认学生列表
        querenbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MessageDetailActivity.this, SeeMessageStudentsActivity.class);
                intent.putExtra("TID", messageEntity.getTID());
                intent.putExtra("isQueren", "yes");
                startActivity(intent);
            }
        });

        //未查看列表
        noseebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MessageDetailActivity.this, SeeMessageStudentsActivity.class);
                intent.putExtra("TID", messageEntity.getTID());
                intent.putExtra("isQueren", "no");
                startActivity(intent);
            }
        });


        //学生确认查看消息
        queren.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent1 = new Intent(MessageDetailActivity.this, SeeMessageStudentsActivity.class);
//                intent1.putExtra("TID", messageEntity.getTID());
//                intent1.putExtra("isQueren", "no");
//                startActivity(intent1);
                String userid = SharedPreferenceManager.instance(MessageDetailActivity.this).getUserInfor().getUserid();
                queren(messageEntity.getTID(), userid);
            }
        });

    }

    private void queren(final int tid, final String userid) {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                OkHttpClient okHttpClient = HttpUtil.getClient();
                MessageEntity messageEntity = new MessageEntity();

                Request request = new Request.Builder().url(HttpUrl.QUEREN_MESSAGE + "?userid=" + userid + "&TID=" + tid)
                        .get()
                        .build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    String result = response.body().string();
//                    JSONObject jsonObject = new JSONObject(result);
//                    JSONObject result1 = jsonObject.optJSONObject("result");
//                    String array = jsonObject.optJSONArray("data");

                    subscriber.onNext(result);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                int messageNumber;
                Type listType = new TypeToken<List<MessageReturnEntity>>() {
                }.getType();
                MessageReturnEntity<String> returnEntity = SchoolInforManager.gson.fromJson(s, MessageReturnEntity.class);
                List<MessageEntity> entityList = new ArrayList<>();
                Toast.makeText(MessageDetailActivity.this, returnEntity.getMsg(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                ScreenManager.getScreenManager().popActivty(this);
                break;
//            case R.id.button6:
//                Intent intent = new Intent(MessageDetailActivity.this, SeeMessageStudentsActivity.class);
//                intent.putExtra("TID", messageEntity.getTID());
//                intent.putExtra("isQueren", "yes");
//                startActivity(intent);
//                break;
//            case R.id.button7:
//                Intent intent1 = new Intent(MessageDetailActivity.this, SeeMessageStudentsActivity.class);
//                intent1.putExtra("TID", messageEntity.getTID());
//                intent1.putExtra("isQueren", "no");
//                startActivity(intent1);
//                break;
        }
    }
}
