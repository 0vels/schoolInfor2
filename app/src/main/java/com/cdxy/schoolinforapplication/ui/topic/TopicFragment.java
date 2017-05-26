package com.cdxy.schoolinforapplication.ui.topic;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cdxy.schoolinforapplication.HttpUrl;
import com.cdxy.schoolinforapplication.R;
import com.cdxy.schoolinforapplication.SchoolInforManager;
import com.cdxy.schoolinforapplication.adapter.topic.TopicAdapter;
import com.cdxy.schoolinforapplication.model.ReturnEntity;
import com.cdxy.schoolinforapplication.model.topic.ReturnCommentEntity;
import com.cdxy.schoolinforapplication.model.topic.ReturnThumb;
import com.cdxy.schoolinforapplication.model.topic.ReturnTopicEntity;
import com.cdxy.schoolinforapplication.model.topic.TopicEntity;
import com.cdxy.schoolinforapplication.ui.MainActivity;
import com.cdxy.schoolinforapplication.ui.base.BaseFragment;
import com.cdxy.schoolinforapplication.ui.widget.RefreshLayout;
import com.cdxy.schoolinforapplication.util.HttpUtil;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class TopicFragment extends BaseFragment {

    @BindView(R.id.refresh_layout)
    RefreshLayout refreshLayout;
    @BindView(R.id.list_topic)
    ListView listTopic;
    @BindView(R.id.edt_add_comment)
    EditText edtAddComment;
    @BindView(R.id.txt_send_new_comment)
    TextView txtSendNewComment;
    @BindView(R.id.layout_add_comment)
    LinearLayout layoutAddComment;
    @BindView(R.id.progress)
    ProgressBar progress;
    @BindView(R.id.empty_view)
    TextView emptyView;
    private TopicAdapter adapter;
    private List<TopicEntity> list;
    private int topicNummber;
    private boolean isFrashing=false;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            emptyView.setVisibility(View.GONE);
            if (msg.what == 1) {
                if (list.size() == 0) {
                    emptyView.setVisibility(View.VISIBLE);
                    listTopic.setEmptyView(emptyView);
                }
                adapter.notifyDataSetChanged();
                if (refreshLayout.isRefreshing()) {
                    refreshLayout.setRefreshing(false);
                    isFrashing=false;
                }
                progress.setVisibility(View.GONE);
                MainActivity.isReturnFromAddTopic = false;
            } else if (msg.what == 2) {
                progress.setVisibility(View.VISIBLE);
            }
        }
    };

    public TopicFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_topic, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();

    }

    @Override
    public void init() {
        list = new ArrayList<>();
        /**
         * 在 refreshLayout.setLoading(false);方法中添加了footerView
         * footerView的添加必须setAdapter之前
         * public void setAdapter(ListAdapter adapter) {
         ........
         if (mHeaderViewInfos.size() > 0|| mFooterViewInfos.size() > 0) {
         mAdapter = new HeaderViewListAdapter(mHeaderViewInfos, mFooterViewInfos, adapter);
         } else {
         mAdapter = adapter;
         }
         ........
         }
         * 通过adapter的setAdapter()方法可以看出
         * 在setAdapter之前要判断该是否存在headView或footerView，如果存在就创建并传HeaderViewListAdapter，否则传adapter
         */
        refreshLayout.setLoading(false);
        adapter = new TopicAdapter(list, getActivity(), layoutAddComment, edtAddComment, txtSendNewComment);
        listTopic.setAdapter(adapter);
        //设置下拉刷新时的颜色值，颜色需要定义在xml中
        refreshLayout.setColorSchemeColors(R.color.top_color, R.color.colorAccent, R.color.text_red_color, R.color.txt_departent_class_color);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isFrashing=true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getAllTopic();
                    }
                }).start();

            }
        });
        refreshLayout.setOnLoadListener(new RefreshLayout.OnLoadListener() {
            @Override
            public void onLoad() {
                refreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setLoading(false);
                    }
                }, 2000);
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        new Thread(new Runnable() {
            @Override
            public void run() {
                getAllTopic();
            }
        }).start();

    }

    private void getAllTopic() {
        if (!isFrashing){
            handler.sendEmptyMessage(2);
        }
        list.clear();
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    OkHttpClient okHttpClient = HttpUtil.getClient();
                    Request request = new Request.Builder().url(HttpUrl.All_TOPICS).get().build();
                    Response response = okHttpClient.newCall(request).execute();
                    subscriber.onNext(response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                ReturnEntity<List<ReturnTopicEntity>> returnEntity = SchoolInforManager.gson.fromJson(s, ReturnEntity.class);
                if (returnEntity != null) {
                    returnEntity = SchoolInforManager.gson.fromJson(s, new TypeToken<ReturnEntity<List<ReturnTopicEntity>>>() {
                    }.getType());
                    if (returnEntity.getCode() == 1) {
                        List<ReturnTopicEntity> returnTopicList = returnEntity.getData();
                        topicNummber = returnTopicList.size();
                        if (topicNummber == 0) {
                            handler.sendEmptyMessage(1);
                        } else {
                            for (int j = 0; j < topicNummber; j++) {
                                String topicid = returnTopicList.get(j).getTopicid();
                                String userid = returnTopicList.get(j).getAuthorid();
                                if ((!TextUtils.isEmpty(topicid)) && (!TextUtils.isEmpty(userid))) {
                                    TopicEntity topicEntity = new TopicEntity();
                                    topicEntity.setContent(returnTopicList.get(j).getContent());
                                    topicEntity.setCreate_time(returnTopicList.get(j).getCreatetime());
                                    topicEntity.setIcon(returnTopicList.get(j).getIcon());
                                    topicEntity.setNickName(returnTopicList.get(j).getNickName());
                                    topicEntity.setUserid(userid);
                                    topicEntity.setTopicid(topicid);
                                    getTopicPhoto(topicid, topicEntity, j);
                                }

                            }
                        }
                    } else {
                        toast(returnEntity.getMsg() + "");
                    }
                }
            }
        });
    }

    private void getTopicPhoto(final String topicid, final TopicEntity topicEntity, final int position) {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                OkHttpClient okHttpClient = HttpUtil.getClient();
                Request request = new Request.Builder().url(HttpUrl.ALL_TOPIC_PHOTOS + "?topicid=" + topicid).build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    subscriber.onNext(response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                ReturnEntity<List<Object>> returnEntity = SchoolInforManager.gson.fromJson(s, ReturnEntity.class);
                if (returnEntity != null) {
                    returnEntity = SchoolInforManager.gson.fromJson(s, new TypeToken<ReturnEntity<List<Object>>>() {
                    }.getType());
                    if (returnEntity.getCode() == 1) {
                        final List<Object> photos = returnEntity.getData();
                        if (photos != null) {
                            if (photos.size() != 0) {
                                topicEntity.setPhotos(photos);
                            }
                        }

                    } else {
                        toast(returnEntity.getMsg() + "");
                    }
                }

                getComments(topicid, topicEntity, position);


            }
        });
    }

    //获取评论列表
    public void getComments(final String topicid, final TopicEntity topicEntity, final int position) {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                OkHttpClient okhttpclient = HttpUtil.getClient();
                Request request = new Request.Builder().url(HttpUrl.ALL_TOPIC_COMMENTS + "?topicid=" + topicid).get().build();
                try {
                    Response response = okhttpclient.newCall(request).execute();
                    subscriber.onNext(response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                ReturnEntity<List<ReturnCommentEntity>> returnEntity = SchoolInforManager.gson.fromJson(s, ReturnEntity.class);
                if (returnEntity != null) {
                    returnEntity = SchoolInforManager.gson.fromJson(s, new TypeToken<ReturnEntity<List<ReturnCommentEntity>>>() {
                    }.getType());
                    if (returnEntity.getCode() == 1) {
                        List<ReturnCommentEntity> comments = returnEntity.getData();
                        if (comments != null) {
                            if (comments.size() != 0) {
                                topicEntity.setComments(comments);
                            }
                        }
                    } else {
                        toast(returnEntity.getMsg() + "");
                    }
                }
                getAllThumb(topicEntity, position);
            }
        });
    }

    //获取点赞人列表
    private void getAllThumb(final TopicEntity topicEntity, final int position) {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                OkHttpClient okHttpClient = HttpUtil.getClient();
                Request request = new Request.Builder().url(HttpUrl.All_TOPIC_THUMBS + "?topicid=" + topicEntity.getTopicid()).get().build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    subscriber.onNext(response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                ReturnEntity<List<ReturnThumb>> returnEntity = SchoolInforManager.gson.fromJson(s, ReturnEntity.class);
                if (returnEntity != null) {
                    returnEntity = SchoolInforManager.gson.fromJson(s, new TypeToken<ReturnEntity<List<ReturnThumb>>>() {
                    }.getType());
                    if (returnEntity.getCode() == 1) {
                        List<String> thumbs = new ArrayList<String>();
                        for (ReturnThumb thumb : returnEntity.getData()) {
                            thumbs.add(thumb.getNickName());
                        }
                        topicEntity.setThumbPersonsNickname(thumbs);
                    } else {
                        toast(returnEntity.getMsg() + "");
                    }

                }
                list.add(topicEntity);
                if (position == topicNummber - 1) {
                    handler.sendEmptyMessage(1);
                }

            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
