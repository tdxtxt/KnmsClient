package com.knms.adapter;

import android.app.Dialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.gson.reflect.TypeToken;
import com.knms.activity.MinePriceRationDetailsActivity;
import com.knms.activity.base.BaseActivity;
import com.knms.adapter.base.ViewHolder;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.myparity.MyParity;
import com.knms.net.HttpConstant;
import com.knms.net.ReqApi;
import com.knms.net.RxRequestApi;
import com.knms.oncall.AsyncHttpCallBack;
import com.knms.util.DialogHelper;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.StrHelper;
import com.knms.util.Tst;
import com.knms.view.CircleImageView;
import com.knms.view.clash.FullyLinearLayoutManager;
import com.knms.view.flowlayout.TagFlowLayout;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/9/12.
 */
public class MinePriceRationAdapter extends BaseQuickAdapter<MyParity, BaseViewHolder> {

    BaseActivity context;

    public MinePriceRationAdapter(BaseActivity context, List<MyParity> mDatas) {
        super(R.layout.item_mine_price_ration, mDatas);
        this.context = context;
    }
    @Override
    protected void convert(BaseViewHolder helper, final MyParity data) {
        ImageView undercarriage = helper.getView(R.id.undercarriage);
        final TextView textView= helper.getView(R.id.tv_myprice_refresh);
        final SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
        final String currentTime=dateFormat.format(new Date());
        if(!currentTime.equals(data.updatetime.split(" ")[0])||TextUtils.equals(data.cocreated,data.updatetime)){
            textView.setTextColor(0xffee4b62);
            textView.setText("刷新");
        }else{
            textView.setTextColor(0xff666666);
            textView.setText("已刷新");
        }

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(mContext, "refreshOfMyCompareClick");
                if(textView.getText().toString().equals("已刷新")){
                    Tst.showToast("每天只能刷新一次哦~");
                    return;
                }
                ((BaseActivity)mContext).showProgress("刷新中");
                RxRequestApi.getInstance().getApiService().refreshMyParity(data.coid)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<ResponseBody<String>>() {
                            @Override
                            public void call(ResponseBody<String> stringResponseBody) {
                                ((BaseActivity)mContext).hideProgress();
                                if(stringResponseBody.isSuccess()){
                                    data.updatetime=currentTime;
                                    textView.setTextColor(0xff666666);
                                    textView.setText("已刷新");
                                    Tst.showToast("刷新成功~");
                                }else Tst.showToast(stringResponseBody.desc);
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                ((BaseActivity)mContext).hideProgress();
                                Tst.showToast(throwable.getMessage());
                            }
                        });
            }
        });


        if (data.coState.equals("0")) {
            undercarriage.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
        } else if (data.coState.equals("1")) {
            undercarriage.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
        }
        helper.setText(R.id.release_time, StrHelper.displayTime(data.cocreated, true, false));
        helper.setText(R.id.remark, data.coremark);
        TextView check_all = helper.getView(R.id.check_all);
        RecyclerView rv_reply = helper.getView(R.id.recyclerview_reply);
        rv_reply.setLayoutManager(new FullyLinearLayoutManager(mContext));
        if (data.commentList != null ) {
            check_all.setVisibility(data.commentList.size()==0?View.GONE:View.VISIBLE);
            helper.setText(R.id.reply_amount, data.coType==5?"为您推荐"+ data.commentList.size() +"个同类商家":"共有" + data.commentList.size() + "个商家回复");
            rv_reply.setAdapter(new BaseQuickAdapter<MyParity.Reply,BaseViewHolder>(R.layout.item_merchant_reply, data.commentList.size()>2?data.commentList.subList(0,2):data.commentList) {
                @Override
                protected void convert(BaseViewHolder helper, final MyParity.Reply item) {
                    helper.setText(R.id.name, item.shopName);
                    helper.setText(R.id.reply_time,data.coType==5?"进店逛逛":StrHelper.displayTime(item.contactTime, true, false));
                    CircleImageView head_picture = helper.getView(R.id.head_picture);
                    ImageLoadHelper.getInstance().displayImageHead(context,item.shopLogo, head_picture);
                    helper.getConvertView().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Map<String, Object> params = new HashMap<String, Object>();
                            params.put("myParity", data);
                            params.put("isHidden", true);
                            context.startActivityAnimGeneral(MinePriceRationDetailsActivity.class, params);
                        }
                    });
                }
            });
        }

        ImageView main_picture = helper.getView(R.id.main_picture);
        if (TextUtils.isEmpty(data.coInspirationPic))
            main_picture.setImageResource(R.drawable.icon_default_img);
        else
            ImageLoadHelper.getInstance().displayImage(context,data.coInspirationPic, main_picture, LocalDisplay.dp2px(80),LocalDisplay.dp2px(80));


        check_all.setTag(data);
        check_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyParity item = (MyParity) v.getTag();
                if(item.commentList == null) item.commentList = new ArrayList<MyParity.Reply>();
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put("myParity", item);
                hashMap.put("id",item.coid);
                hashMap.put("isHidden", true);
                context.startActivityAnimGeneral(MinePriceRationDetailsActivity.class, hashMap);
            }
        });

        TagFlowLayout tagFlowLayout = helper.getView(R.id.tag_flow_layout);
        if (data.labelList.size() == 0) {
            tagFlowLayout.setVisibility(View.GONE);
        } else {
            tagFlowLayout.setVisibility(View.VISIBLE);
            LabelAdapter adapter = new LabelAdapter(data.labelList,false);
            tagFlowLayout.setAdapter(adapter);
        }

        TextView edit = helper.getView(R.id.edit);
        edit.setTag(data);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MyParity item = (MyParity) v.getTag();
                DialogHelper.showBottomDialog(context, R.layout.dialog_mine_price_ration, new DialogHelper.OnEventListener<Dialog>() {
                    @Override
                    public void eventListener(View parentView, final Dialog window) {
                        TextView tvUndercarriage = (TextView) parentView.findViewById(R.id.undercarriage);
                        TextView tvDelete = (TextView) parentView.findViewById(R.id.delete);
                        if (item.coState.equals("0")) {
                            tvUndercarriage.setVisibility(View.GONE);
                        } else {
                            tvUndercarriage.setText("已解决");
                            tvDelete.setVisibility(View.GONE);
                        }
                        tvUndercarriage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
//                                if (item.coState.equals("0")) {
//                                    Tst.showToast("已下架");
//                                }else {
                                DialogHelper.showPromptDialog(context, "确定已解决了吗？", "如果已经解决了，商家将不再看到此消息", "取消", "", "确定", new DialogHelper.OnMenuClick() {
                                    @Override
                                    public void onLeftMenuClick() { }
                                    @Override
                                    public void onCenterMenuClick() { }
                                    @Override
                                    public void onRightMenuClick() {
                                        HashMap<String, Object> map1 = new HashMap<String, Object>();
                                        map1.put("goid", item.coid);//比比价id
                                        map1.put("state", 3);//3：下架，4：删除
                                        ReqApi.getInstance().postMethod(HttpConstant.myParityUpdateState, map1, new AsyncHttpCallBack() {
                                            @Override
                                            public void onSuccess(ResponseBody body) {
                                                Tst.showToast(body.desc);
                                                if (body.isSuccess()) {
                                                    if (callBack != null) callBack.refresh();
                                                }
                                            }
                                            @Override
                                            public void onFailure(String msg) {
                                                Tst.showToast(msg);
                                            }
                                            @Override
                                            public Type setType() {
                                                return new TypeToken<ResponseBody>() {
                                                }.getType();
                                            }
                                        });
                                    }
                                });
//                                }
                                window.dismiss();
                            }
                        });
                        parentView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                DialogHelper.showPromptDialog(context, "", "确定要删除求购/比货消息吗？", "取消", "", "确定", new DialogHelper.OnMenuClick() {
                                    @Override
                                    public void onLeftMenuClick() { }
                                    @Override
                                    public void onCenterMenuClick() { }
                                    @Override
                                    public void onRightMenuClick() {
                                        HashMap<String, Object> map2 = new HashMap<String, Object>();
                                        map2.put("goid", item.coid);
                                        map2.put("state", 4);//3：下架，4：删除
                                        ReqApi.getInstance().postMethod(HttpConstant.myParityUpdateState, map2, new AsyncHttpCallBack() {
                                            @Override
                                            public void onSuccess(ResponseBody body) {
                                                if (body.isSuccess()) {
                                                    if (callBack != null) callBack.refresh();
                                                }else{
                                                    Tst.showToast(body.desc);
                                                }
                                            }
                                            @Override
                                            public void onFailure(String msg) {
                                                Tst.showToast(msg);
                                            }

                                            @Override
                                            public Type setType() {
                                                return new TypeToken<ResponseBody>() {
                                                }.getType();
                                            }
                                        });
                                    }
                                });
                                window.dismiss();
                            }
                        });
                        parentView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                window.dismiss();
                            }
                        });
                    }
                });
            }
        });
    }

    CallBack callBack;

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public interface CallBack {
        public void refresh();
    }
}
