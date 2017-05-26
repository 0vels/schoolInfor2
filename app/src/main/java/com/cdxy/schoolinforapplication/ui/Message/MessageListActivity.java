package com.cdxy.schoolinforapplication.ui.Message;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cdxy.schoolinforapplication.HttpUrl;
import com.cdxy.schoolinforapplication.R;
import com.cdxy.schoolinforapplication.SchoolInforManager;
import com.cdxy.schoolinforapplication.ScreenManager;
import com.cdxy.schoolinforapplication.adapter.message.MessageListAdapter;
import com.cdxy.schoolinforapplication.model.MessageReturnEntity;
import com.cdxy.schoolinforapplication.model.message.MessageEntity;
import com.cdxy.schoolinforapplication.ui.base.BaseActivity;
import com.cdxy.schoolinforapplication.ui.widget.ScrollListView;
import com.cdxy.schoolinforapplication.util.Constant;
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

import static com.cdxy.schoolinforapplication.util.Constant.MY_SEND_MESSAGE;

public class MessageListActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.txt_title)
    TextView txtTitle;
    @BindView(R.id.btn_right)
    Button btnRight;
    @BindView(R.id.layout_progress)
    LinearLayout layoutProgress;
    @BindView(R.id.scrollListView_mesage_list)
    ScrollListView scrollListViewMesageList;
    @BindView(R.id.activity_message_list)
    LinearLayout activityMessageList;
    private List<MessageEntity> list;
    private MessageListAdapter adapter;
    private int messageType;//重要消息、普通消息、我发送的消息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        ButterKnife.bind(this);
        ScreenManager.getScreenManager().pushActivity(this);
        init();
        switch (messageType) {
            case Constant.IMPORTANT_MESSAGE:
                txtTitle.setText("重要消息列表");
                adapter = new MessageListAdapter(MessageListActivity.this, list, messageType);
                scrollListViewMesageList.setAdapter(adapter);
                getImportantMessage(messageType);
                break;
            case Constant.NOT_IMPORTANT_MESSAGE:
                txtTitle.setText("普通消息列表");
                adapter = new MessageListAdapter(MessageListActivity.this, list, messageType);
                scrollListViewMesageList.setAdapter(adapter);
                getNotImportantMessage(messageType);
                break;
            case MY_SEND_MESSAGE:
                txtTitle.setText("我发送的消息列表");
                adapter = new MessageListAdapter(MessageListActivity.this, list, messageType);
                scrollListViewMesageList.setAdapter(adapter);
                getMySendMessage(messageType);
                break;
        }
        scrollListViewMesageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MessageListActivity.this, MessageDetailActivity.class);
                MessageEntity entity = list.get(i);
                intent.putExtra("message_type",messageType);
                intent.putExtra("message", entity);
                startActivity(intent);
            }
        });
    }

    @Override
    public void init() {
        Intent intent = getIntent();
        messageType = intent.getIntExtra("message_type", 0);
        list = new ArrayList<>();
    }

    private List<MessageEntity> getMessage(final int messageType) {
        final List<MessageEntity> returnList = new ArrayList<>();
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                OkHttpClient okHttpClient = HttpUtil.getClient();
                MessageEntity messageEntity = new MessageEntity();

                Request request = new Request.Builder().url(HttpUrl.GET_MESSAGE + "?messageType=" + messageType)
                        .get()
                        .build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    String result = response.body().string();
//                    JSONObject jsonObject = new JSONObject(result);
//                    JSONObject result1 = jsonObject.optJSONObject("result");
//                    String array = jsonObject.optJSONArray("data");

                    subscriber.onNext(result);
//                    subscriber.onCompleted();

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
                MessageReturnEntity<List<MessageEntity>> returnEntity = SchoolInforManager.gson.fromJson(s, MessageReturnEntity.class);
                List<MessageEntity> entityList = new ArrayList<>();
//                if (returnEntity != null) {
//                    returnEntity = gson.fromJson(s, new TypeToken<ReturnEntity<List<ReturnTopicEntity>>>() {
//                    }.getType());
                if (returnEntity.getCode() == 1) {
                    List<MessageEntity> returnTopicList = returnEntity.getData();
//                        returnList.addAll(returnTopicList);
                    messageNumber = returnTopicList.size();
                    for (int j = 0; j < messageNumber; j++) {
                        long TID = returnTopicList.get(j).getTID();
                        String userid = returnTopicList.get(j).getSendPersonName();
                        if (TID != 0 && (!TextUtils.isEmpty(userid))) {
                            MessageEntity messageEntity = new MessageEntity();
                            messageEntity = returnTopicList.get(j);
                            messageEntity.setContent(returnTopicList.get(j).getContent());
                            messageEntity.setTime(returnTopicList.get(j).getTime());
                            messageEntity.setMessageType(returnTopicList.get(j).getMessageType());
                            messageEntity.setSendPersonName(userid);
                            messageEntity.setTitle(returnTopicList.get(j).getTitle());
                            messageEntity.setSendTo(returnTopicList.get(j).getSendTo());
                            returnList.add(messageEntity);
                        }

                    }

                }
            }
//            }
        });


//        if (messageType == MY_SEND_MESSAGE) {
//            List<MessageEntity> myMessageList = new ArrayList<>();
//            for (MessageEntity messageEntity : returnList) {
//                if (messageEntity.getSendPersonName().equals(MyInformationActivity.getUserid())) {
//                    myMessageList.add(messageEntity);
//                }
//            }
//            return myMessageList;
//        }

        return returnList;
    }

    //我发出的消息列表
    private List<MessageEntity> getMyMessage(final String username) {
        final List<MessageEntity> returnList = new ArrayList<>();
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                OkHttpClient okHttpClient = HttpUtil.getClient();
                MessageEntity messageEntity = new MessageEntity();

                Request request = new Request.Builder().url(HttpUrl.GET_MyMESSAGE + "?userid=" + username)
                        .get()
                        .build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    String result = response.body().string();
//                    JSONObject jsonObject = new JSONObject(result);
//                    JSONObject result1 = jsonObject.optJSONObject("result");
//                    String array = jsonObject.optJSONArray("data");

                    subscriber.onNext(result);
//                    subscriber.onCompleted();

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
                MessageReturnEntity<List<MessageEntity>> returnEntity = SchoolInforManager.gson.fromJson(s, MessageReturnEntity.class);
                List<MessageEntity> entityList = new ArrayList<>();
//                if (returnEntity != null) {
//                    returnEntity = gson.fromJson(s, new TypeToken<ReturnEntity<List<ReturnTopicEntity>>>() {
//                    }.getType());
                if (returnEntity.getCode() == 1) {
                    List<MessageEntity> returnTopicList = returnEntity.getData();
//                        returnList.addAll(returnTopicList);
                    messageNumber = returnTopicList.size();
                    for (int j = 0; j < messageNumber; j++) {
                        long TID = returnTopicList.get(j).getTID();
                        String userid = returnTopicList.get(j).getSendPersonName();
                        if (TID != 0 && (!TextUtils.isEmpty(userid))) {
                            MessageEntity messageEntity = new MessageEntity();
                            messageEntity = returnTopicList.get(j);
                            messageEntity.setContent(returnTopicList.get(j).getContent());
                            messageEntity.setTime(returnTopicList.get(j).getTime());
                            messageEntity.setMessageType(returnTopicList.get(j).getMessageType());
                            messageEntity.setSendPersonName(userid);
                            messageEntity.setTitle(returnTopicList.get(j).getTitle());
                            messageEntity.setSendTo(returnTopicList.get(j).getSendTo());
                            returnList.add(messageEntity);
                        }

                    }

                }
            }
//            }
        });

        return returnList;
    }

    private void getImportantMessage(final int messageType) {
        new AsyncTask<Void, Void, List<MessageEntity>>() {
            @Override
            protected List<MessageEntity> doInBackground(Void... voids) {
                //测试数据
//                MessageEntity entity1 = new MessageEntity(1, "张三", "2016-12-27", "计算机系", "", "期末考试安排", "2017-01-04期末考试结束");
//                MessageEntity entity2 = new MessageEntity(1, "李四", "2016-12-22", "网工", "", "寒假放假安排", "放假时间2017-01-07——2017-02-22");
//                List<MessageEntity> entityList = new ArrayList<>();
//                entityList.add(entity1);
//                entityList.add(entity2);
                return getMessage(messageType);
            }

            @Override
            protected void onPostExecute(List<MessageEntity> messageEntities) {
                super.onPostExecute(messageEntities);
                if (list != null) {
                    list.clear();
                    list.addAll(messageEntities);
                    adapter.notifyDataSetChanged();
                    layoutProgress.setVisibility(View.GONE);
                }
            }
        }.execute();
    }

    private void getNotImportantMessage(final int messageType) {
        new AsyncTask<Void, Void, List<MessageEntity>>() {
            @Override
            protected List<MessageEntity> doInBackground(Void... voids) {
                //测试数据
//                MessageEntity entity1 = new MessageEntity(0, "zhang", "2016-12-23", "航空分院", "", "实习推荐", "国腾园招软件实习生");
//                MessageEntity entity2 = new MessageEntity(0, "li", "2016-12-21", "云计算", "", "小春熙又开新店了", "小春熙大冬天开了一个冰淇淋店，听说去的人还很多");
//                List<MessageEntity> entityList = new ArrayList<>();
//                entityList.add(entity1);
//                entityList.add(entity2);
                return getMessage(messageType);
            }

            @Override
            protected void onPostExecute(List<MessageEntity> messageEntities) {
                super.onPostExecute(messageEntities);
                if (list != null) {
                    list.clear();
                    list.addAll(messageEntities);
                    adapter.notifyDataSetChanged();
                    layoutProgress.setVisibility(View.GONE);
                }
            }
        }.execute();
    }

    private void getMySendMessage(final int messageType) {
        new AsyncTask<Void, Void, List<MessageEntity>>() {
            @Override
            protected List<MessageEntity> doInBackground(Void... voids) {
                //测试数据
//                MessageEntity entity1 = new MessageEntity(1, "杨老师", "2016-12-25", "计算机系", "", "期末考试教室变化", "二教205的考生请到206考试");
//                MessageEntity entity2 = new MessageEntity(0, "杨老师", "2016-12-29", "计算机系", "", "杨老师要办考研班了", "有想要报名的尽快来报名");
//                List<MessageEntity> entityList = new ArrayList<>();
//                entityList.add(entity1);
//                entityList.add(entity2);
                return getMyMessage(SharedPreferenceManager.instance(MessageListActivity.this).getUserInfor().getXingming());
            }

            @Override
            protected void onPostExecute(List<MessageEntity> messageEntities) {
                super.onPostExecute(messageEntities);
                if (list != null) {
                    list.clear();
                    list.addAll(messageEntities);
                    adapter.notifyDataSetChanged();
                    layoutProgress.setVisibility(View.GONE);
                }
            }
        }.execute();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                ScreenManager.getScreenManager().popActivty(this);
                break;
        }
    }
}
