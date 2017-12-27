package com.knms.activity.im;

import android.content.Context;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.adapter.im.KnmsChatAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.im.KnmsMsg;
import com.knms.bean.im.SendState;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.net.RxRequestApi;
import com.knms.oncall.LoadListener;
import com.knms.util.StrHelper;
import com.knms.util.Tst;
import com.knms.view.listview.AutoRefreshListView;
import com.knms.view.listview.MessageListView;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by tdx on 2016/9/29.
 * 铠恩客服消息列表界面
 */
public class KnmsKefuChatActivity extends HeadBaseActivity {
    private MessageListView listView;
    private EditText editTextMessage;
    private int pageNum = 1;
    private RelativeLayout rl_status;
    private KnmsChatAdapter adapter;
    private Subscription subscription,sendSubscription,intervalSubscription;

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("买手客服");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_chat;
    }

    @Override
    protected void initView() {
        findView(R.id.switchLayout).setVisibility(View.GONE);
        findView(R.id.emoji_button).setVisibility(View.GONE);
        listView = findView(R.id.lv_chat);
        rl_status = findView(R.id.rl_status);
        editTextMessage = findView(R.id.editTextMessage);
        listView.setMode(AutoRefreshListView.Mode.START);
        findView(R.id.buttonSendMessage).setVisibility(View.VISIBLE);
    }

    @Override
    protected void initBar() {
        View barView = findView(R.id.view);
        if(barView == null) return;
        mImmersionBar = ImmersionBar.with(this);
        mImmersionBar.statusBarView(barView)
                .keyboardEnable(true)  //解决软键盘与底部输入框冲突问题
                .statusBarDarkFont(true, 0.1f)//状态栏字体是深色，不写默认为亮色
                .flymeOSStatusBarFontColor(R.color.status_bar_textcolor);  //修改flyme OS状态栏字体颜色;
        mImmersionBar.init();
    }

    @Override
    protected void initData() {
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();//隐藏软件盘
                return false;
            }
        });
        adapter = new KnmsChatAdapter(this,"KeFuMsg");
        listView.setAdapter(adapter);
        findView(R.id.buttonSendMessage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = editTextMessage.getText().toString();
                if(TextUtils.isEmpty(msg)){
                    Tst.showToast("请输入发送的内容");
                    return;
                }
                KnmsMsg knmsMsg = new KnmsMsg();
                knmsMsg.content = msg;
                knmsMsg.sendStatus = SendState.sending;
                knmsMsg.time = StrHelper.getCurrentTime(StrHelper.DATE_FORMAT2S);
                knmsMsg.role = 1;
                editTextMessage.setText("");
                sendMsg(knmsMsg);
            }
        });
        editTextMessage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    listView.setSelection(adapter.getCount());
                }
                return false;
            }
        });
        listView.setOnRefreshListener(new AutoRefreshListView.OnRefreshListener() {
            @Override
            public void onRefreshFromStart() {
                reqApi();
            }
            @Override
            public void onRefreshFromEnd() {
            }
        });
        editTextMessage.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    showKeyboard();
                }
                return false;
            }
        });
        pageNum = 1;
        KnmsApp.getInstance().showLoadViewIng(rl_status);
        reqApi();
        //开启定时任务
        intervalSubscription = Observable.interval(5, 5, TimeUnit.SECONDS)//延时5秒 ，每间隔5秒
                .flatMap(new Func1<Long, Observable<ResponseBody<List<KnmsMsg>>>>() {
                    @Override
                    public Observable<ResponseBody<List<KnmsMsg>>> call(Long aLong) {
                        return RxRequestApi.getInstance().getApiService().refreshKefuMsgs();
                    }
                }).onErrorResumeNext(new Func1<Throwable, Observable<? extends ResponseBody<List<KnmsMsg>>>>() {
                    @Override
                    public Observable<? extends ResponseBody<List<KnmsMsg>>> call(Throwable throwable) {
                        ResponseBody<List<KnmsMsg>> body = new ResponseBody<List<KnmsMsg>>();
                        body.code = "1000";//失败
                        return Observable.just(body);
                    }
                })
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<List<KnmsMsg>>>() {
                    @Override
                    public void call(ResponseBody<List<KnmsMsg>> body) {
                        //更新数据
                        if(body.isSuccess() && body.data != null && body.data.size() > 0){
                            adapter.putData(body.data);
                            RxBus.get().post(BusAction.REFRESH_KF_MSG,body.data.get(body.data.size() - 1));
                        }

                    }
                });
    }

    @Override
    public void showKeyboard() {
        editTextMessage.postDelayed(new Runnable() {
            @Override
            public void run() {
                editTextMessage.requestFocus();
                editTextMessage.setSelection(editTextMessage.getText().length());
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    editTextMessage.requestFocus();
                    imm.showSoftInput(editTextMessage, 0);
                }
            }
        }, 200);
        editTextMessage.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(adapter != null && listView != null){
                    listView.setSelection(adapter.getCount());
                }
            }
        },500);
    }

    @Override
    public String setStatisticsTitle() {
        return "铠恩客服消息列表";
    }

    @Override
    protected void reqApi() {
        subscription = RxRequestApi.getInstance().getApiService().getknmsKefuMsgs(pageNum)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<List<KnmsMsg>>>() {
                    @Override
                    public void call(ResponseBody<List<KnmsMsg>> body) {
                        listView.onRefreshComplete();
                        KnmsApp.getInstance().hideLoadView(rl_status);
                        if(body.isSuccess()){
                            updateView(body.data);
                            if(pageNum == 1) RxBus.get().post(BusAction.REFRESH_MSG_TIP,KnmsKefuChatActivity.class.getSimpleName());
                            pageNum ++;
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        listView.onRefreshComplete();
                        if(pageNum == 1){
                            KnmsApp.getInstance().showLoadViewFaild(rl_status, new LoadListener() {
                                @Override
                                public void onclick() {
                                    KnmsApp.getInstance().showLoadViewIng(rl_status);
                                    reqApi();
                                }
                            });
                        }else{
                            KnmsApp.getInstance().hideLoadView(rl_status);
                        }
                        Tst.showToast("网络不给力，请检查网络原因");
                    }
                });
    }
    private void updateView(List<KnmsMsg> data) {
        if(pageNum == 1){
            adapter.setNewData(data);
            listView.setSelection(adapter.getCount());
        }else{
            listView.setSelection(adapter.addData(data));
        }
    }
    private void sendMsg(final KnmsMsg msg){
        sendSubscription = Observable.just(msg).flatMap(new Func1<KnmsMsg, Observable<ResponseBody<KnmsMsg>>>() {
            @Override
            public Observable<ResponseBody<KnmsMsg>> call(KnmsMsg knmsMsg) {
                return RxRequestApi.getInstance().getApiService().sendKnmsMsg(knmsMsg.content);
            }
        }).onErrorResumeNext(new Func1<Throwable, Observable<? extends ResponseBody<KnmsMsg>>>() {
            @Override
            public Observable<? extends ResponseBody<KnmsMsg>> call(Throwable throwable) {
                ResponseBody<KnmsMsg> body = new ResponseBody<KnmsMsg>();
                body.code = "1000";
                return Observable.just(body);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<ResponseBody<KnmsMsg>>() {
                    @Override
                    public void call(ResponseBody<KnmsMsg> knmsMsgResponseBody) {
                        adapter.addItem(msg);
                        listView.setSelection(adapter.getCount());
                    }
                })
                .subscribe(new Action1<ResponseBody<KnmsMsg>>() {
                    @Override
                    public void call(ResponseBody<KnmsMsg> body) {
                        if(body.isSuccess()){
                            RxBus.get().post(BusAction.REFRESH_MSG_KNMS,body.data);
                            adapter.removeItem(msg);
                            adapter.addItem(body.data);
                            listView.setSelection(adapter.getCount());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        msg.sendStatus = SendState.fail;
                        adapter.notifyDataSetChanged();
                    }
                });
    }
    @Override
    protected void onDestroy() {
        if(subscription != null) subscription.unsubscribe();
        if(sendSubscription != null) sendSubscription.unsubscribe();
        if(intervalSubscription != null) intervalSubscription.unsubscribe();
        super.onDestroy();
    }
}
