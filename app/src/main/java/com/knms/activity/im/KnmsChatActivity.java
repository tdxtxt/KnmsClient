package com.knms.activity.im;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.knms.activity.base.HeadBaseActivity;
import com.knms.adapter.im.KnmsChatAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.im.KnmsMsg;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.net.RxRequestApi;
import com.knms.oncall.LoadListener;
import com.knms.util.Tst;
import com.knms.view.listview.AutoRefreshListView;
import com.knms.view.listview.MessageListView;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


/**
 * Created by tdx on 2016/9/29.
 * 铠恩买手官方消息列表界面
 */

public class KnmsChatActivity extends HeadBaseActivity {
    private MessageListView listView;
    private int pageNum = 1;
    private RelativeLayout rl_status;
    private KnmsChatAdapter adapter;
    private Subscription subscription;
    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("铠恩买手");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_chat;
    }

    @Override
    protected void initView() {
        findView(R.id.include_footerview).setVisibility(View.GONE);
        listView = findView(R.id.lv_chat);
        rl_status = findView(R.id.rl_status);
        listView.setMode(AutoRefreshListView.Mode.START);
    }

    @Override
    protected void initData() {
        adapter = new KnmsChatAdapter(this,"KnmsMsg");
        listView.setAdapter(adapter);
        listView.setOnRefreshListener(new AutoRefreshListView.OnRefreshListener() {
            @Override
            public void onRefreshFromStart() {
                reqApi();
            }
            @Override
            public void onRefreshFromEnd() {

            }
        });
        pageNum = 1;
        KnmsApp.getInstance().showLoadViewIng(rl_status);
        reqApi();
    }

    @Override
    public String setStatisticsTitle() {
        return "铠恩买手官方消息列表";
    }

    @Override
    protected void reqApi() {
        subscription = RxRequestApi.getInstance().getApiService().getknmsMsgs(pageNum)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<List<KnmsMsg>>>() {
                    @Override
                    public void call(ResponseBody<List<KnmsMsg>> body) {
                        listView.onRefreshComplete();
                        KnmsApp.getInstance().hideLoadView(rl_status);
                        if(body.isSuccess()){
                            updateView(body.data);
                            if(pageNum == 1) RxBus.get().post(BusAction.REFRESH_MSG_TIP,KnmsChatActivity.class.getSimpleName());
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
                        Tst.showToast(throwable.toString());
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

    @Override
    protected void onDestroy() {
        if(subscription != null) subscription.unsubscribe();
        super.onDestroy();
    }
}
