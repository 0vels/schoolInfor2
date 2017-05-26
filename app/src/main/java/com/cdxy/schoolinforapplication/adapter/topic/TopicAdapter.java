package com.cdxy.schoolinforapplication.adapter.topic;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.mobileim.channel.event.IWxCallback;
import com.bumptech.glide.Glide;
import com.cdxy.schoolinforapplication.HttpUrl;
import com.cdxy.schoolinforapplication.R;
import com.cdxy.schoolinforapplication.SchoolInforManager;
import com.cdxy.schoolinforapplication.model.ReturnEntity;
import com.cdxy.schoolinforapplication.model.UserInfor.UserInforEntity;
import com.cdxy.schoolinforapplication.model.topic.ReturnCommentEntity;
import com.cdxy.schoolinforapplication.model.topic.ThumbEntity;
import com.cdxy.schoolinforapplication.model.topic.TopicEntity;
import com.cdxy.schoolinforapplication.ui.load.LoginActivity;
import com.cdxy.schoolinforapplication.ui.topic.ShowBigPhotosActivity;
import com.cdxy.schoolinforapplication.ui.widget.EdtDialog;
import com.cdxy.schoolinforapplication.ui.widget.NotifyDialog;
import com.cdxy.schoolinforapplication.ui.widget.ScrollListView;
import com.cdxy.schoolinforapplication.util.Constant;
import com.cdxy.schoolinforapplication.util.HttpUtil;
import com.cdxy.schoolinforapplication.util.SharedPreferenceManager;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by huihui on 2016/12/30.
 */

public class TopicAdapter extends BaseAdapter {
    private List<TopicEntity> list;
    private Activity activity;
    private TopicPhotosAdapter topicPhotosAdapter;
    private TopicCommentContentAdapter commentContentAdapter;
    private LinearLayout layoutAddComment;
    private EditText edtAddComment;
    private TextView txtSendNewComment;
    private ViewHolder viewHolder;
    private UserInforEntity userInfor;
    private String myNikename;
    private String myUserid;
    private NotifyDialog notifyDialog;
    private EdtDialog edtDialog;


    public TopicAdapter(List<TopicEntity> list, Activity activity, LinearLayout layoutAddComment, EditText edtAddComment, TextView txtSendNewComment) {
        this.list = list;
        this.activity = activity;
        this.layoutAddComment = layoutAddComment;
        this.edtAddComment = edtAddComment;
        this.txtSendNewComment = txtSendNewComment;
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
    public View getView(final int i, View view, ViewGroup viewGroup) {
//        if (view == null) {
        view = LayoutInflater.from(activity).inflate(R.layout.item_topic, null);
        viewHolder = new ViewHolder(view);
        viewHolder.layout_divider = (LinearLayout) view.findViewById(R.id.layout_divider);
//        view.setTag(viewHolder);
//        } else {
//            viewHolder = (ViewHolder) view.getTag();
//        }
        //话题创建者的昵称、创建时间、话题内容的显示
        final TopicEntity entity = (TopicEntity) getItem(i);
        final String nickName = entity.getNickName();
        String createTime = entity.getCreate_time();
        String content = entity.getContent();
        if (!TextUtils.isEmpty(nickName)) {
            viewHolder.txtTopicNickname.setText(nickName);
        }
        if (!TextUtils.isEmpty(createTime)) {
            viewHolder.txtTopicCreateTime.setText(createTime);
        }
        if (!TextUtils.isEmpty(content)) {
            viewHolder.txtTopicContent.setText(content);
        }
        //话题创建者头像的显示
        Object icon = entity.getIcon();
        if (icon != null) {
            Glide.with(activity).load(icon).placeholder(R.drawable.loading).bitmapTransform(new CropCircleTransformation(activity)).into(viewHolder.imgTopicIcon);
        } else {
            Glide.with(activity).load(R.drawable.icon).placeholder(R.drawable.loading).bitmapTransform(new CropCircleTransformation(activity)).into(viewHolder.imgTopicIcon);
        }
        //点击头像发送添加好友请求
        viewHolder.imgTopicIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!entity.getUserid().equals(myUserid)) {
                    edtDialog = new EdtDialog(activity, R.style.MyDialog, new EdtDialog.AddFriendListener() {
                        @Override
                        public void onClick(View view) {
                            switch (view.getId()) {
                                case R.id.btn_sure:
                                    addFriend(entity.getUserid());
                                    edtDialog.dismiss();
                                    break;
                                case R.id.btn_cancel:
                                    edtDialog.dismiss();
                                    break;
                            }
                        }
                    }, activity, Constant.EDTDIALOG_TYPE_WANT_ADD_FRIEND);
                    edtDialog.show();
                }
            }
        });
        //加载图片
        final List<Object> photos = entity.getPhotos();
        if (photos != null) {
            if (photos.size() != 0) {
                viewHolder.framePhotos.setVisibility(View.VISIBLE);
                topicPhotosAdapter = new TopicPhotosAdapter(activity, photos);
                viewHolder.gridViewPhotos.setAdapter(topicPhotosAdapter);
                if (photos.size() > 4) {
                    viewHolder.txtMorePhotoNumber.setVisibility(View.VISIBLE);
                    viewHolder.txtMorePhotoNumber.setText("+" + (photos.size() - 4));
                }

                //点击话题图片放大
                viewHolder.gridViewPhotos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent = new Intent(activity, ShowBigPhotosActivity.class);
                        intent.putExtra("position", i);
                        intent.putExtra("photos", (Serializable) photos);
                        intent.putExtra("photosType", Constant.SHOW_BIG_PHOTO_TOPIC_FRAGMENT);
                        activity.startActivity(intent);
                    }
                });
            }
        }
        //加载讨论列表
        List<ReturnCommentEntity> comments = entity.getComments();
        if (comments != null) {
            if (comments.size() > 0) {
                commentContentAdapter = new TopicCommentContentAdapter(activity, comments);
                viewHolder.scrollComments.setAdapter(commentContentAdapter);
            }
        }
        //长按一条自己发送的评论实现删除
        viewHolder.scrollComments.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                return false;
            }
        });

        userInfor = SharedPreferenceManager.instance(activity).getUserInfor();
        if (userInfor != null) {
            myNikename = userInfor.getNicheng();
            myUserid = userInfor.getUserid();
            if (!TextUtils.isEmpty(userInfor.getUserid())) {
                //判断该话题是否是本人发的，如果是的话就可以删除
                if (entity.getUserid().equals(userInfor.getUserid())) {
                    viewHolder.imgDelete.setVisibility(View.VISIBLE);
                    viewHolder.imgDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            createNotifyDailog(entity.getTopicid(), i);
                        }
                    });
                } else {
                    viewHolder.imgDelete.setVisibility(View.GONE);
                }
            }
        }

        //点赞人姓名集合
        final List<String> thumbPersonsName = entity.getThumbPersonsNickname();
        if (thumbPersonsName != null) {
            int thumbNumber = thumbPersonsName.size() - 1;
            if (thumbNumber > 0) {
                viewHolder.layout_divider.setVisibility(View.VISIBLE);
                viewHolder.txtThumbPersonsNickname.setVisibility(View.VISIBLE);
                String thumbPersonsNameString = "";
                for (int j = 1; j < thumbPersonsName.size(); j++) {
                    if (j == 1) {
                        thumbPersonsNameString = thumbPersonsName.get(j);
                    } else {
                        if (!TextUtils.isEmpty(thumbPersonsName.get(j))) {
                            thumbPersonsNameString = thumbPersonsNameString + "," + thumbPersonsName.get(j);
                        }

                    }
                }
                thumbPersonsNameString = thumbPersonsNameString + " " + thumbNumber + "人为你点赞";
                viewHolder.txtThumbPersonsNickname.setText(thumbPersonsNameString);
            } else {
                viewHolder.txtThumbPersonsNickname.setVisibility(View.GONE);
                viewHolder.layout_divider.setVisibility(View.GONE);
            }
        }
        viewHolder.imgComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edtAddComment.setText("");
                layoutAddComment.setVisibility(View.VISIBLE);
                txtSendNewComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String newCommentcontent = edtAddComment.getText().toString();
                        if (TextUtils.isEmpty(newCommentcontent)) {
                            return;
                        }
                        String topicid = entity.getTopicid();
                        String receiverNickname = entity.getNickName();
                        if (!TextUtils.isEmpty(myNikename) && (!TextUtils.isEmpty(receiverNickname))) {
                            ReturnCommentEntity commentEntity = new ReturnCommentEntity(topicid, myNikename, receiverNickname, newCommentcontent);
                            String json = SchoolInforManager.gson.toJson(commentEntity);
                            sendComment(json, entity, commentEntity);
                        }
                    }
                });
            }
        });
        viewHolder.imgThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(myNikename) && (!TextUtils.isEmpty(myUserid))) {
                    String topicid = entity.getTopicid();
                    ThumbEntity thumbEntity = new ThumbEntity(topicid, myUserid, myNikename);
                    String jsonString = SchoolInforManager.gson.toJson(thumbEntity);
                    thumb(jsonString, entity, i);
                } else {
                    Toast.makeText(activity, "你还没设置昵称，快去设置吧", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;

    }

    private void thumb(final String json, final TopicEntity topicEntity, int position) {
        Observable.create(new OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                OkHttpClient okHttpClient = HttpUtil.getClient();
                final Request request = new Request.Builder().url(HttpUrl.THUMB + "?likejson=" + json).get().build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    subscriber.onNext(response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.newThread()).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                ReturnEntity returnEntity = SchoolInforManager.gson.fromJson(s, ReturnEntity.class);
                if (returnEntity != null) {
                    if (returnEntity.getCode() == 1) {
                        if (topicEntity.getThumbPersonsNickname().contains(myNikename)) {
                            topicEntity.getThumbPersonsNickname().remove(myNikename);
                        } else {
                            topicEntity.getThumbPersonsNickname().add(myNikename);

                        }
                        TopicAdapter.this.notifyDataSetChanged();
                    } else {
                        Toast.makeText(activity, returnEntity.getMsg(), Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }

    private void sendComment(final String commentjson, final TopicEntity topicEntity, final ReturnCommentEntity commentEntity) {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                OkHttpClient okhttpClient = HttpUtil.getClient();
                Request request = new Request.Builder().url(HttpUrl.SEND_COMMENT + "?commentjson=" + commentjson).get().build();
                try {
                    Response response = okhttpClient.newCall(request).execute();
                    subscriber.onNext(response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.newThread()).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                ReturnEntity returnEntity = SchoolInforManager.gson.fromJson(s, ReturnEntity.class);
                if (returnEntity.getCode() == 1) {
                    topicEntity.getComments().add(commentEntity);
                    TopicAdapter.this.notifyDataSetChanged();
                } else {
                    Toast.makeText(activity, returnEntity.getMsg(), Toast.LENGTH_SHORT).show();
                }
                layoutAddComment.setVisibility(View.GONE);
            }
        });
    }

    static class ViewHolder {
        @BindView(R.id.img_topic_icon)
        ImageView imgTopicIcon;
        @BindView(R.id.txt_topic_nickname)
        TextView txtTopicNickname;
        @BindView(R.id.txt_topic_create_time)
        TextView txtTopicCreateTime;
        @BindView(R.id.txt_topic_content)
        TextView txtTopicContent;
        @BindView(R.id.gridView_photos)
        GridView gridViewPhotos;
        @BindView(R.id.txt_more_photo_number)
        TextView txtMorePhotoNumber;
        @BindView(R.id.frame_photos)
        FrameLayout framePhotos;
        @BindView(R.id.img_comment)
        ImageView imgComment;
        @BindView(R.id.img_thumb)
        ImageView imgThumb;
        @BindView(R.id.img_delete)
        ImageView imgDelete;
        @BindView(R.id.txt_thumb_persons_nickname)
        TextView txtThumbPersonsNickname;
        @BindView(R.id.scroll_comments)
        ScrollListView scrollComments;
        private LinearLayout layout_divider;


        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    private void deleteMyToppic(final String topicid, final int position) {
        Observable.create(new OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                OkHttpClient okHttpClient = HttpUtil.getClient();
                Request request = new Request.Builder().url(HttpUrl.DELETE_MY_TOPIC + "?topicid=" + topicid).get().build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    subscriber.onNext(response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.newThread()).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                ReturnEntity returnEntity = SchoolInforManager.gson.fromJson(s, ReturnEntity.class);
                if (returnEntity != null) {
                    if (returnEntity.getCode() == 1) {
                        list.remove(position);
                        TopicAdapter.this.notifyDataSetChanged();
                    } else {
                        Toast.makeText(activity, returnEntity.getMsg(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void createNotifyDailog(final String topicid, final int position) {
        notifyDialog = new NotifyDialog(activity, R.style.MyDialog, new NotifyDialog.NotifyListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.dialog_ok:
                        deleteMyToppic(topicid, position);
                        notifyDialog.dismiss();
                        break;
                    case R.id.dialog_cancle:
                        notifyDialog.dismiss();
                        break;
                }
            }
        }, activity, "delete_my_topic");
        notifyDialog.show();
    }

    private void addFriend(final String target) {
//
        IWxCallback callback = new IWxCallback() {

            @Override
            public void onSuccess(Object... result) {
                Observable.just("你已经申请添加" + target + "为好友").observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Toast.makeText(activity, s, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(int i, String s) {

            }

            @Override
            public void onProgress(int progress) {

            }
        };
        LoginActivity.iywContactService.addContact(target, SchoolInforManager.appKay, target, edtDialog.content, callback);

    }
}
