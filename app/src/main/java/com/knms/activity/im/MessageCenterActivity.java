package com.knms.activity.im;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.adapter.im.RecentContactAdapter;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.im.KnmsMsg;
import com.knms.bean.im.MsgCenterData;
import com.knms.bean.user.User;
import com.knms.core.im.IMHelper;
import com.knms.core.im.uinfo.UserInfoHelper;
import com.knms.core.im.uinfo.UserInfoObservable;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.net.NetworkHelper;
import com.knms.net.RxRequestApi;
import com.knms.other.RetrofitCache;
import com.knms.util.CommonUtils;
import com.knms.util.SPUtils;
import com.knms.util.StrHelper;
import com.knms.util.Tst;
import com.knms.view.clash.FullyLinearLayoutManager;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.sina.weibo.sdk.utils.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func3;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tdx on 2016/9/28.
 */
public class MessageCenterActivity extends HeadBaseActivity {
    private static final String EXTRA_APP_QUIT = "APP_QUIT";
    public static final long RECENT_TAG_STICKY = 1; // 联系人置顶tag
    PullToRefreshScrollView pullToRefreshScrollView;
    RecyclerView rv_im_msgs;
    RecentContactAdapter adapter;
    View view_official, view_kf;
    TextView tv_tips;
    private CompositeSubscription mSubscriptions;
    private String fromValue;

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("消息中心");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_msg_center;
    }

    @Override
    protected void getParmas(Intent intent) {
        if (intent.hasExtra(NimIntent.EXTRA_NOTIFY_CONTENT)) {
            IMMessage message = (IMMessage) getIntent().getSerializableExtra(NimIntent.EXTRA_NOTIFY_CONTENT);
            switch (message.getSessionType()) {
                case P2P:
                    Map<String, Object> params = new HashMap<>();
                    params.put("sid", message.getSessionId());
                    startActivityAnimGeneral(ChatActivity.class, params);
                    break;
                default:
                    break;
            }
        } else if (intent.hasExtra("from")) {
            fromValue = intent.getStringExtra("from");
        }
    }

    @Override
    protected void initView() {
        pullToRefreshScrollView = findView(R.id.refresh_scrollView);
        view_official = findView(R.id.item_msg_official);
        view_kf = findView(R.id.item_msg_kf);
        view_official.setVisibility(View.GONE);
        view_kf.setVisibility(View.GONE);
        tv_tips = findView(R.id.tv_tips);
        tv_tips.setVisibility(View.GONE);
        rv_im_msgs = findView(R.id.rv_im_msgs);
        rv_im_msgs.setLayoutManager(new FullyLinearLayoutManager(this));
        rv_im_msgs.setFocusable(false);//TODO 失去焦点，这样才不会让RecyclerView把上面的控件顶出界面外

        if ("mainTab".equals(fromValue)) findView(R.id.iv_back).setVisibility(View.INVISIBLE);
    }

    @Override
    protected void initBar() {
        if("mainTab".equals(fromValue)){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                View barView = findView(R.id.view);
                ViewGroup.LayoutParams lp = barView.getLayoutParams();
                lp.height = ImmersionBar.getStatusBarHeight(this);
                barView.setLayoutParams(lp);
            }
        }else{
            super.initBar();
        }
    }

    @Override
    protected void initData() {
        mSubscriptions = new CompositeSubscription();
        adapter = new RecentContactAdapter(this,new ArrayList<RecentContact>(0));
        rv_im_msgs.setAdapter(adapter);
        pullToRefreshScrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        pullToRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ScrollView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
                reqApi();
                Observable.just("").delay(5, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<String>() {
                            @Override
                            public void call(String s) {
                                pullToRefreshScrollView.onRefreshComplete();
                            }
                        });
            }
        });
        view_official.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityAnimGeneral(KnmsChatActivity.class, null);
            }
        });
        view_kf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityAnimGeneral(KnmsKefuChatActivity.class, null);
            }
        });
        tv_tips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IMHelper.getInstance().loginIM();
            }
        });
        adapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                RecentContact item = adapter.getData().get(position);
                Map<String, Object> parmas = new HashMap<String, Object>();
                parmas.put("sid", item.getContactId());
                startActivityAnimGeneral(ChatActivity.class, parmas);
            }
        });
        adapter.setOnRecyclerViewItemLongClickListener(new BaseQuickAdapter.OnRecyclerViewItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View view, final int position) {
                final RecentContact item = adapter.getItem(position);
                String title = (IMHelper.getInstance().isTagSet(item, RECENT_TAG_STICKY) ? "取消置顶" : "置顶该聊天");
                AlertDialog.Builder builder = new AlertDialog.Builder(MessageCenterActivity.this);
                String[] strarr = {title, "删除该会话"};
                builder.setItems(strarr, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (arg1 == 0) {//消息置顶
                            if (IMHelper.getInstance().isTagSet(item, RECENT_TAG_STICKY)) {//取消消息置顶
                                IMHelper.getInstance().removeTag(item, RECENT_TAG_STICKY);
                            } else {
                                IMHelper.getInstance().addTag(item, RECENT_TAG_STICKY);
                            }
                            NIMClient.getService(MsgService.class).updateRecent(item);
                            loadRecentContacts();
                        } else if (arg1 == 1) {//删除该会话
                            NIMClient.getService(MsgService.class).deleteRoamingRecentContact(item.getContactId(), SessionTypeEnum.P2P);//删除指定最近联系人的漫游消息
                            NIMClient.getService(MsgService.class).deleteRecentContact2(item.getContactId(), SessionTypeEnum.P2P);
//                            adapter.remove(position);
                            RxBus.get().post(BusAction.REFRESH_MSG_TIP,"notify");
                        }
                    }
                });
                builder.show();
                return false;
            }
        });
        registerObservers(true);
        pullToRefreshScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                pullToRefreshScrollView.setRefreshing();
            }
        }, 1000);
        Observable.just("").delay(5, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        pullToRefreshScrollView.onRefreshComplete();
                    }
                });
    }

    @Override
    public String setStatisticsTitle() {
        return "消息中心";
    }

    @Override
    public void onResume() {
        super.onResume();
        IMHelper.getInstance().enableMsgNotification(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        IMHelper.getInstance().enableMsgNotification(true);
    }

    private void registerObservers(boolean register) {
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(onlineStatus, register);//在线状态变更
//        NIMClient.getService(AuthServiceObserver.class).observeOtherClients(onlineclients, true);//多端登录状态观察者
        NIMClient.getService(MsgServiceObserve.class).observeRecentContact(messageObserver, register);//监听最近会话变更
        NIMClient.getService(MsgServiceObserve.class).observeRecentContactDeleted(recentContactDeletedObserver, register);//注册消息已读回执对象
        if(register){//注册用户资料变化观察者。
            UserInfoHelper.registerObserver(userInfoObserver);
        }else {
            UserInfoHelper.unregisterObserver(userInfoObserver);
        }
    }

    /**
     * 消息已读回执设置成功监听
     */
//    private Observer<List<MessageReceipt>> messageReceiptObserver = new Observer<List<MessageReceipt>>() {
//        @Override
//        public void onEvent(List<MessageReceipt> messageReceipts) {
//            loadRecentContacts();
//        }
//    };
    //最近会话变更
    Observer<List<RecentContact>> messageObserver = new Observer<List<RecentContact>>() {
        @Override
        public void onEvent(List<RecentContact> messages) {
            loadRecentContacts();
        }
    };
    Observer<RecentContact> recentContactDeletedObserver = new Observer<RecentContact>() {
        @Override
        public void onEvent(RecentContact recentContact) {
            loadRecentContacts();
        }
    };
    Observer<StatusCode> onlineStatus = new Observer<StatusCode>() {
        public void onEvent(StatusCode code) {
            if (code.wontAutoLogin()) {
                // 被踢出、账号被禁用、密码错误等情况，自动登录失败，需要返回到登录界面进行重新登录操作
                Tst.showToast("你的账号在其他设备有登录");
                kickOut(code);
            } else {
                if (code == StatusCode.NET_BROKEN) {
                    tv_tips.setVisibility(View.VISIBLE);
                    tv_tips.setText(R.string.net_broken);
                } else if (code == StatusCode.UNLOGIN) {
                    tv_tips.setVisibility(View.VISIBLE);
                    tv_tips.setText(R.string.nim_status_unlogin);
                    IMHelper.getInstance().loginIM();
                } else if (code == StatusCode.CONNECTING) {
                    tv_tips.setVisibility(View.VISIBLE);
                    tv_tips.setText(R.string.nim_status_connecting);
                } else if (code == StatusCode.LOGINING) {
                    tv_tips.setVisibility(View.VISIBLE);
                    tv_tips.setText(R.string.nim_status_logining);
                } else {
                    tv_tips.setText("");
                    tv_tips.setVisibility(View.GONE);
                }
            }
        }
    };
    UserInfoObservable.UserInfoObserver userInfoObserver = new UserInfoObservable.UserInfoObserver() {
        @Override
        public void onUserInfoChanged(List<String> accounts) {
            loadRecentContacts();
        }
    };
    private void kickOut(StatusCode code) {
        if (code == StatusCode.PWD_ERROR) {
            LogUtil.e("Auth", "user password error");
            Tst.showToast("登录密码错误");
        }
        CommonUtils.logout();
    }

    @Override
    protected void reqApi() {
        if(!NetworkHelper.isNetwork()) Tst.showToast("网络不给力，请检查网络设置");
        IMHelper.getInstance().loginIM();
        mSubscriptions.add(Observable.zip(RetrofitCache.load("msgCenter",RxRequestApi.getInstance().getApiService().getMsgCenter()).onErrorResumeNext(new Func1<Throwable, Observable<? extends ResponseBody<List<KnmsMsg>>>>() {
                    @Override
                    public Observable<? extends ResponseBody<List<KnmsMsg>>> call(Throwable throwable) {
                        ResponseBody<List<KnmsMsg>> newBody = new ResponseBody<>();
                        newBody.code = "0";
                        List<KnmsMsg> knmsMsgs = new ArrayList<>();
                        knmsMsgs.add(new KnmsMsg());
                        knmsMsgs.add(new KnmsMsg());
                        newBody.data = knmsMsgs;
                        return Observable.just(newBody);
                    }
                }), IMHelper.getInstance().getIMTotalUnreadCount(), getRecentContacts()
                , new Func3<ResponseBody<List<KnmsMsg>>, Integer, List<RecentContact>, MsgCenterData>() {
                    @Override
                    public MsgCenterData call(ResponseBody<List<KnmsMsg>> body1, Integer body2, List<RecentContact> body3) {
                        MsgCenterData msgCenterData = new MsgCenterData();
                        if (body1 != null && body1.isSuccess()) {
                            msgCenterData.knmsMsgs = body1.data;
                            if (body1.data != null) {
                                int pos = 0;
                                for (KnmsMsg item : body1.data) {
                                    if (pos == 0) msgCenterData.knmsCount = item.notReadNumber;
                                    if (pos == 1) msgCenterData.knmsKefuCount = item.notReadNumber;
                                    pos++;
                                }
                            }
                        } else {
                            Tst.showToast(body1.desc);
                        }
                        msgCenterData.recentContacts = body3;
                        msgCenterData.imCount = body2;
                        msgCenterData.totalCount = msgCenterData.imCount + msgCenterData.knmsCount + msgCenterData.knmsKefuCount;
                        return msgCenterData;
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.toString());
                    }
                }).onErrorResumeNext(new Func1<Throwable, Observable<? extends MsgCenterData>>() {
                    @Override
                    public Observable<? extends MsgCenterData> call(Throwable throwable) {
                        return Observable.empty();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).cache()
                .subscribe(new Action1<MsgCenterData>() {
                    @Override
                    public void call(MsgCenterData msgCenterData) {
                        pullToRefreshScrollView.onRefreshComplete();
                        RxBus.get().post(BusAction.REFRESH_MSG_TIP, "notify");
                        if(msgCenterData != null){
                            updateKnmsMsg(msgCenterData.knmsMsgs);
                            adapter.setNewData(msgCenterData.recentContacts);
                            tv_title_center.setText("消息中心" + (msgCenterData.totalCount == 0 ? "" : "(" + (msgCenterData.totalCount > 99 ? "99+" : msgCenterData.totalCount) + ")"));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        pullToRefreshScrollView.onRefreshComplete();
                        Tst.showToast(throwable.toString());
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        pullToRefreshScrollView.onRefreshComplete();
                    }
                }));
    }

    /**
     * 加载最近会话列表
     */
    private void loadRecentContacts() {
        NIMClient.getService(MsgService.class).queryRecentContacts()
                .setCallback(new RequestCallbackWrapper<List<RecentContact>>() {
                    @Override
                    public void onResult(int code, List<RecentContact> recents, Throwable e) {
                        if (code != ResponseCode.RES_SUCCESS || recents == null) return;
                        if (recents == null) {
                            return;
                        }
                        Collections.sort(recents, comp);
                        refreshMsgCount("msg");
                        adapter.setNewData(recents);
                    }
                });
    }

    private Observable<List<RecentContact>> getRecentContacts() {
        return Observable.create(new Observable.OnSubscribe<List<RecentContact>>() {
            @Override
            public void call(final Subscriber<? super List<RecentContact>> subscriber) {
                List<RecentContact> recentContacts = NIMClient.getService(MsgService.class).queryRecentContactsBlock();
                Collections.sort(recentContacts, comp);
                subscriber.onNext(recentContacts);
                subscriber.onCompleted();
            }
        });
    }

    private void updateKnmsMsg(List<KnmsMsg> datas) {
        if (!(datas != null && datas.size() > 0)) return;
        KnmsMsg msgOfficial = datas.get(0);
        KnmsMsg msgKf = null;
        if (datas.size() > 1) {
            msgKf = datas.get(1);
            view_official.setVisibility(View.VISIBLE);
        }
        updateKnmsOfficialMsg(msgOfficial);
        updateKnmsKfMsg(msgKf);
    }
    private void updateKnmsOfficialMsg(KnmsMsg knmsMsg){
        ((TextView) view_official.findViewById(R.id.tv_name)).setText("铠恩买手");
        ((TextView) view_official.findViewById(R.id.tv_content)).setText(knmsMsg.content);
        ((TextView) view_official.findViewById(R.id.tv_data)).setText(TextUtils.isEmpty(knmsMsg.time) ? "" : StrHelper.displayTime(knmsMsg.time, true, false));
        ((TextView) view_official.findViewById(R.id.tv_new_count)).setText(knmsMsg.notReadNumber > 99 ? "99+" : knmsMsg.notReadNumber + "");
        ((ImageView) view_official.findViewById(R.id.iv_avatar)).setImageResource(R.drawable.icon_knms);
        view_official.findViewById(R.id.tv_decorate).setVisibility(View.VISIBLE);
    }
    @Subscribe(tags = {@Tag(BusAction.REFRESH_KF_MSG)})
    public void updateKnmsKfMsg(KnmsMsg knmsMsg){
        ((TextView) view_kf.findViewById(R.id.tv_name)).setText("买手客服");
        ((ImageView) view_kf.findViewById(R.id.iv_avatar)).setImageResource(R.drawable.icon_kefu);
        if (knmsMsg != null) {
            view_kf.setVisibility(View.VISIBLE);
            ((TextView) view_kf.findViewById(R.id.tv_content)).setText(knmsMsg.content);
            ((TextView) view_kf.findViewById(R.id.tv_data)).setText(TextUtils.isEmpty(knmsMsg.time) ? "" : StrHelper.displayTime(knmsMsg.time, true, false));
            ((TextView) view_kf.findViewById(R.id.tv_new_count)).setText(knmsMsg.notReadNumber > 99 ? "99+" : knmsMsg.notReadNumber + "");
        }
    }
    private static Comparator<RecentContact> comp = new Comparator<RecentContact>() {
        @Override
        public int compare(RecentContact o1, RecentContact o2) {
            // 先比较置顶tag
            long sticky = (o1.getTag() & RECENT_TAG_STICKY) - (o2.getTag() & RECENT_TAG_STICKY);
            if (sticky != 0) {
                return sticky > 0 ? -1 : 1;
            } else {
                long time = o1.getTime() - o2.getTime();
                return time == 0 ? 0 : (time > 0 ? -1 : 1);
            }
        }
    };

    @Subscribe(tags = {@Tag(BusAction.REFRESH_MSG_TIP)})
    public void refreshNewTip(String simpeName) {
        refreshMsgCount("msg");
    }

    @Subscribe(tags = {@Tag(BusAction.ACTION_LOGIN)})
    public void changAccout(User user) {
        updateKnmsMsg(new ArrayList<KnmsMsg>(2));
        adapter.setNewData(new ArrayList<RecentContact>(0));
        pullToRefreshScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                pullToRefreshScrollView.setRefreshing();
            }
        }, 1000);
    }

    @Subscribe(tags = {@Tag(BusAction.REFRESH_MSG_COUNT)})
    public void refreshMsgCount(String msg){
        if(!SPUtils.isLogin()) return;
        RxRequestApi.getInstance().getApiService().getMsgCenter()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<List<KnmsMsg>>>() {
                    @Override
                    public void call(ResponseBody<List<KnmsMsg>> body) {
                        int knmsCount = 0;
                        if (body.isSuccess() && body.data != null) {
                            updateKnmsMsg(body.data);
                            for (KnmsMsg msg : body.data) {
                                knmsCount += msg.notReadNumber;
                            }
                        }
                        int imCount = NIMClient.getService(MsgService.class).getTotalUnreadCount();
                        int totalCount = knmsCount + imCount;
                        tv_title_center.setText("消息中心" + (totalCount == 0 ? "" : "(" + (totalCount > 99 ? "99+" : totalCount) + ")"));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                    }
                });
    }

    @Subscribe(tags = {@Tag(BusAction.REFRESH_MSG_KNMS)})
    public void refreshKnmsMsg(KnmsMsg msgKf) {
        refreshMsgCount("msg");
    }

    @Override
    protected void onDestroy() {
        if (mSubscriptions != null) mSubscriptions.unsubscribe();
        //注销观察者
        registerObservers(false);
        super.onDestroy();
    }


    long firstTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(!"mainTab".equals(fromValue)) {
                finshActivity();
            }else {
                long secondTime = System.currentTimeMillis();
                if (secondTime - firstTime >= 1000) {//如果两次按键时间间隔大于800毫秒，则不退出
                    Tst.showToast("再按一次退出程序");
                    firstTime = secondTime;//更新firstTime
                    return true;
                } else {
                    System.exit(0);//否则退出程序
                }
            }
        }
        return super.onKeyUp(keyCode, event);
    }
}
