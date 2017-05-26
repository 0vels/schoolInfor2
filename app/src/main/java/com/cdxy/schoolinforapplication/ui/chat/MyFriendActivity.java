package com.cdxy.schoolinforapplication.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.mobileim.channel.event.IWxCallback;
import com.alibaba.mobileim.contact.IYWDBContact;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.cdxy.schoolinforapplication.R;
import com.cdxy.schoolinforapplication.SchoolInforManager;
import com.cdxy.schoolinforapplication.ScreenManager;
import com.cdxy.schoolinforapplication.adapter.chat.MyFriendsAdapter;
import com.cdxy.schoolinforapplication.model.chat.MyFriendEntity;
import com.cdxy.schoolinforapplication.ui.base.BaseActivity;
import com.cdxy.schoolinforapplication.ui.load.LoginActivity;
import com.cdxy.schoolinforapplication.ui.widget.EdtDialog;
import com.cdxy.schoolinforapplication.ui.widget.NotifyDialog;
import com.cdxy.schoolinforapplication.util.Constant;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyFriendActivity extends BaseActivity implements View.OnClickListener {
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.txt_title)
    TextView txtTitle;
    @BindView(R.id.btn_right)
    Button btnRight;
    @BindView(R.id.activity_my_friend)
    LinearLayout activityMyFriend;
    @BindView(R.id.swiplist_my_friend)
    SwipeMenuListView swiplistMyFriend;
    private List<MyFriendEntity> list;
    private MyFriendsAdapter adapter;
    private NotifyDialog notifyDialog;
    private EdtDialog edtDialogUpdateName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_friend);
        ButterKnife.bind(this);
        ScreenManager.getScreenManager().pushActivity(this);
        init();


    }
private void getMyFriends(){
    list.clear();
    IWxCallback callback = new IWxCallback() {
        @Override
        public void onSuccess(Object... result) {

            List<IYWDBContact> contactsFromCache = LoginActivity.iywContactService.getContactsFromCache();
            //iywContactService.syncContactsOnlineStatus((List<IYWContact>) (List) contactsFromCache,iWxCallbackIsOnline);
            for (IYWDBContact iywdbContact : contactsFromCache) {
                MyFriendEntity entity = new MyFriendEntity();
                entity.setName(iywdbContact.getShowName());
                entity.setIcon(iywdbContact.getAvatarPath());
                entity.setUserId(iywdbContact.getUserId());
                list.add(entity);
            }
            Log.i("iywContactService", list.toString());
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onProgress(int progress) {
        }


        @Override
        public void onError(int code, String info) {
            toast("获取好友列表出错");
        }
    };
    LoginActivity.iywContactService.syncContacts(callback);

}
    @Override
    public void init() {
        txtTitle.setText("我的好友");
        list = new ArrayList<>();
        adapter = new MyFriendsAdapter(list, MyFriendActivity.this);
        getCreator();
        swiplistMyFriend.setAdapter(adapter);
        getMyFriends();
        swiplistMyFriend.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final String target = ((MyFriendEntity) adapter.getItem(i)).getUserId(); //消息接收者ID
                Intent intent = LoginActivity.ywimKit.getChattingActivityIntent(target, SchoolInforManager.appKay);
                startActivity(intent);
            }
        });
        swiplistMyFriend.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, SwipeMenu menu, int index) {
                final MyFriendEntity myFriendEntity= (MyFriendEntity) adapter.getItem(position);
                switch (index) {
                    case 0:
                        //修改备注
                        edtDialogUpdateName = new EdtDialog(MyFriendActivity.this, R.style.MyDialog, new EdtDialog.AddFriendListener() {
                            @Override
                            public void onClick(View view) {
                                switch (view.getId()) {
                                    case R.id.btn_sure:
                                        updateName(myFriendEntity.getUserId());
                                        edtDialogUpdateName.dismiss();
                                        break;
                                    case R.id.btn_cancel:
                                        edtDialogUpdateName.dismiss();
                                        break;
                                }
                            }
                        },MyFriendActivity.this, Constant.EDTDIALOG_TYPE_UPDATE_NAME);
                        edtDialogUpdateName.show();
                        break;
                    case 1:
                        createNotifyDailog(position);
                        break;
                }
            }
        });

    }

    private void getCreator() {
        final SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                //创建“update”按钮的item
                SwipeMenuItem updateMenu = new SwipeMenuItem(MyFriendActivity.this);
                updateMenu.setBackground(R.color.modify_my_friend_name);
                updateMenu.setWidth(120);
                updateMenu.setTitle("修改备注");
                updateMenu.setTitleSize(16);
                updateMenu.setTitleColor(getResources().getColor(R.color.white));
                menu.addMenuItem(updateMenu);
                //创建“删除”按钮的item
                SwipeMenuItem deleteMenu = new SwipeMenuItem(MyFriendActivity.this);
                deleteMenu.setBackground(R.color.edt_line_color);
                deleteMenu.setWidth(120);
                deleteMenu.setTitle("删除好友");
                deleteMenu.setTitleSize(16);
                deleteMenu.setTitleColor(getResources().getColor(R.color.white));
                menu.addMenuItem(deleteMenu);

            }
        };
        swiplistMyFriend.setMenuCreator(creator);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                ScreenManager.getScreenManager().popActivty(this);
                break;
        }
    }

    private void createNotifyDailog(final int position) {
        notifyDialog = new NotifyDialog(MyFriendActivity.this, R.style.MyDialog, new NotifyDialog.NotifyListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.dialog_ok:
                        deleteFriend(position);
                        notifyDialog.dismiss();
                        break;
                    case R.id.dialog_cancle:
                        notifyDialog.dismiss();
                        break;
                }
            }
        }, MyFriendActivity.this,"delete_my_friend");
        notifyDialog.show();
    }
private void updateName(String userId){
    IWxCallback callback = new IWxCallback() {

        @Override
        public void onSuccess(Object... result) {
            // onSuccess参数为空
            getMyFriends();

        }

        @Override
        public void onProgress(int progress) {

        }

        @Override
        public void onError(int code,String info) {

        }
    };
    LoginActivity.iywContactService.chgContactRemark(userId,SchoolInforManager.appKay,EdtDialog.content,callback);
}
    private void deleteFriend(final int position) {
        IWxCallback callback = new IWxCallback() {

            @Override
            public void onSuccess(Object... result) {
                if (result != null && result.length > 0) {
                    toast("删除好友成功");
                    list.remove(position);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onProgress(int progress) {

            }

            @Override
            public void onError(int code, String info) {

            }
        };
        LoginActivity.iywContactService.delContact(((MyFriendEntity) adapter.getItem(position)).getUserId(), SchoolInforManager.appKay, callback);
    }
}
