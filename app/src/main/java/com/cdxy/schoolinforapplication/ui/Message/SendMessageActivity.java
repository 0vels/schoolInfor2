package com.cdxy.schoolinforapplication.ui.Message;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.cdxy.schoolinforapplication.HttpUrl;
import com.cdxy.schoolinforapplication.R;
import com.cdxy.schoolinforapplication.SchoolInforManager;
import com.cdxy.schoolinforapplication.adapter.topic.ParentAdapter;
import com.cdxy.schoolinforapplication.model.ReturnEntity;
import com.cdxy.schoolinforapplication.model.SendMessageEntity;
import com.cdxy.schoolinforapplication.model.tree.ChildEntity;
import com.cdxy.schoolinforapplication.model.tree.ParentEntity;
import com.cdxy.schoolinforapplication.ui.base.BaseActivity;
import com.cdxy.schoolinforapplication.ui.widget.ScollerExpandableListView;
import com.cdxy.schoolinforapplication.util.HttpUtil;
import com.cdxy.schoolinforapplication.util.SharedPreferenceManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class SendMessageActivity extends BaseActivity implements View.OnClickListener, ExpandableListView.OnGroupClickListener, CompoundButton.OnCheckedChangeListener {
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.txt_title)
    TextView txtTitle;
    @BindView(R.id.btn_right)
    Button btnRight;
    @BindView(R.id.txt_child_name)
    TextView txtChildName;
    @BindView(R.id.ck_all)
    CheckBox ckAll;
    @BindView(R.id.eList)
    ScollerExpandableListView eList;
    @BindView(R.id.activity_send_message)
    LinearLayout activitySendMessage;
    @BindView(R.id.edt_title)
    EditText edtTitle;
    @BindView(R.id.edt_content)
    EditText edtContent;
    @BindView(R.id.rbtn_importent)
    RadioButton rbtnImportent;
    @BindView(R.id.rbtn_not_importent)
    RadioButton rbtnNotImportent;
    @BindView(R.id.radioGroup)
    RadioGroup radioGroup;
    @BindView(R.id.progress)
    ProgressBar progress;
    private ParentAdapter adapter;
    private List<ParentEntity> list;
    int messageTye = 0;
    int isSelectAll = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);
        ButterKnife.bind(this);
        init();
        initData();
        adapter = new ParentAdapter(list, SendMessageActivity.this);
        eList.setAdapter(adapter);
        eList.setOnGroupClickListener(this);
        ckAll.setOnCheckedChangeListener(this);
    }

    @Override
    public void init() {
        txtTitle.setText("发送消息");
        btnRight.setText("发送");
    }

    private void initData() {
        list = new ArrayList<>();
        List<ChildEntity> children1 = new ArrayList<>();
        ChildEntity childEntitya1 = new ChildEntity(getResources().getColor(R.color.colorPrimary), "嵌入式1班");
        children1.add(childEntitya1);
        ChildEntity childEntitya2 = new ChildEntity(getResources().getColor(R.color.colorPrimary), "游戏开发1班");
        children1.add(childEntitya2);
        ChildEntity childEntitya3 = new ChildEntity(getResources().getColor(R.color.colorPrimary), "软件测试1班");
        children1.add(childEntitya3);
        ChildEntity childEntitya4 = new ChildEntity(getResources().getColor(R.color.colorPrimary), "软件技术1班");
        children1.add(childEntitya4);
        ChildEntity childEntitya5 = new ChildEntity(getResources().getColor(R.color.colorPrimary), "软件技术2班");
        children1.add(childEntitya5);
        ChildEntity childEntitya6 = new ChildEntity(getResources().getColor(R.color.colorPrimary), "软件技术3班");
        children1.add(childEntitya6);
        ChildEntity childEntitya7 = new ChildEntity(getResources().getColor(R.color.colorPrimary), "软件技术4班");
        children1.add(childEntitya7);
        ChildEntity childEntitya8 = new ChildEntity(getResources().getColor(R.color.colorPrimary), "网络集成1班");
        children1.add(childEntitya8);
        ChildEntity childEntitya9 = new ChildEntity(getResources().getColor(R.color.colorPrimary), "网络安全1班");
        children1.add(childEntitya9);
        ParentEntity parentEntity1 = new ParentEntity(getResources().getColor(R.color.text_color), "计算机系", children1);
        list.add(parentEntity1);
        List<ChildEntity> children2 = new ArrayList<>();
        ChildEntity childEntityb3 = new ChildEntity(getResources().getColor(R.color.colorPrimary), "云计算");
        children2.add(childEntityb3);
        ParentEntity parentEntity2 = new ParentEntity(getResources().getColor(R.color.text_color), "云计算系", children2);
        list.add(parentEntity2);
        List<ChildEntity> children3 = new ArrayList<>();
        ChildEntity childEntityc1 = new ChildEntity(getResources().getColor(R.color.colorPrimary), "国民经济管理");
        children3.add(childEntityc1);
        ChildEntity childEntityc2 = new ChildEntity(getResources().getColor(R.color.colorPrimary), "现代经济管理");
        children3.add(childEntityc2);
        ChildEntity childEntityc3 = new ChildEntity(getResources().getColor(R.color.colorPrimary), "工(商)业经济管理");
        children3.add(childEntityc3);
        ParentEntity parentEntity3 = new ParentEntity(getResources().getColor(R.color.text_color), "经管系", children3);
        list.add(parentEntity3);
    }

    @Override
    public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
        if (expandableListView.isGroupExpanded(i)) {
            expandableListView.collapseGroup(i);
            list.get(i).setExpand(false);
        } else {
            expandableListView.expandGroup(i);
            list.get(i).setExpand(true);
        }
        adapter.notifyDataSetChanged();

        return true;
    }

    private void changeAllStatus(boolean isSelect) {
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setSelect(isSelect);
            List<ChildEntity> childEntities = list.get(i).getChildren();
            for (int j = 0; j < childEntities.size(); j++) {
                childEntities.get(j).setSelect(isSelect);
            }

        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            changeAllStatus(true);
        } else {
            changeAllStatus(false);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.btn_right:
                sendMessage();
                break;
        }
    }

    private void sendMessage() {
        final String title = edtTitle.getText().toString();
        if (TextUtils.isEmpty(title)) {
            toast("请输入消息标题");
            return;
        }
        final String content = edtContent.getText().toString();
        if (TextUtils.isEmpty(content)) {
            toast("请输入消息内容");
            return;
        }
        int checkId = radioGroup.getCheckedRadioButtonId();
        if (checkId == 0) {
            toast("请选择消息类型");
            return;
        } else if (checkId == R.id.rbtn_importent) {
            messageTye = 1;
        } else if (checkId == R.id.rbtn_not_importent) {
            messageTye = 0;
        }
        final List<String> sendTo = new ArrayList<>();

        if (ckAll.isChecked()) {
            //如果全选
            isSelectAll = 1;
            sendTo.add("全学院");
        } else {
            isSelectAll = 0;
            for (int i = 0; i < list.size(); i++) {
                ParentEntity parentEntity = list.get(i);
                if (parentEntity.isSelect()) {
                    //如果选择了整个系
                    sendTo.add(parentEntity.getName());
                } else {
                    //分班选择
                    List<ChildEntity> childEntityList = parentEntity.getChildren();
                    for (int j = 0; j < childEntityList.size(); j++) {
                        ChildEntity childEntity = childEntityList.get(j);
                        if (childEntity.isSelect()) {
                            sendTo.add(childEntity.getName());
                        }
                    }
                }
            }
        }
        progress.setVisibility(View.VISIBLE);
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                OkHttpClient okHttpClient = HttpUtil.getClient();
                SendMessageEntity sendMessageEntity = new SendMessageEntity(title, content, messageTye, sendTo, SharedPreferenceManager.instance(SendMessageActivity.this).getUserInfor().getXingming(), isSelectAll);
                String json = SchoolInforManager.gson.toJson(sendMessageEntity);
                RequestBody formBody = RequestBody.create(JSON, json);
//                Request request = new Request.Builder().url(HttpUrl.SEND_MESSAGE)
//                        .post(formBody).build();
                Request request = new Request.Builder().url(HttpUrl.SEND_MESSAGE+"?tongzhijson="+json)
                        .get().build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    String result = response.body().string();
                    JSONObject jsonObject = new JSONObject(result);
                    subscriber.onNext(jsonObject.getString("result"));
                    subscriber.onCompleted();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                ReturnEntity returnEntity=SchoolInforManager.gson.fromJson(s,ReturnEntity.class);
                if (returnEntity!=null){
                    if (returnEntity.getCode()==1){
                        toast(returnEntity.getData().toString());

                    }else {
                        toast(returnEntity.getMsg()+"");
                    }
                }

            }
        });
        progress.setVisibility(View.GONE);
        finish();

    }

}

