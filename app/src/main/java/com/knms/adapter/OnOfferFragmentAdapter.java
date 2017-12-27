package com.knms.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.gson.reflect.TypeToken;
import com.knms.activity.ReleaseIdleActivity;
import com.knms.activity.base.BaseFragmentActivity;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.myidle.MyIdle;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.net.HttpConstant;
import com.knms.net.ReqApi;
import com.knms.oncall.AsyncHttpCallBack;
import com.knms.util.CommonUtils;
import com.knms.util.DialogHelper;
import com.knms.util.ImageLoadHelper;
import com.knms.util.StrHelper;
import com.knms.util.Tst;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/9/2.
 */
public class OnOfferFragmentAdapter extends BaseQuickAdapter<MyIdle,BaseViewHolder> {

    BaseFragmentActivity context;
    public OnOfferFragmentAdapter(BaseFragmentActivity context, List<MyIdle> mDatas) {
        super(R.layout.item_on_offer, mDatas);
        this.context = context;
    }

    @Override
    public void convert(BaseViewHolder helper, MyIdle data) {
        TextView edit = helper.getView(R.id.edit);
        TextView undercarriage = helper.getView(R.id.undercarriage);
        if(data.coState == 0){
            undercarriage.setText("下架");
        }else if(data.coState == 1){
            undercarriage.setText("删除");
        }else if(data.coState == 3){
            undercarriage.setText("已下架");
        }
        edit.setTag(data);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyIdle item = (MyIdle) v.getTag();
//                if(StrHelper.isToday(item.goreleasetime)){
//                    Tst.showToast("今日已编辑过，请明天再试");
//                    return;
//                }
                Map<String,Object> parmas = new HashMap<String, Object>();
                parmas.put("idle",item);
                parmas.put("isShowRedMsg",0);
                context.startActivityAnimGeneral(ReleaseIdleActivity.class,parmas);
            }
        });
        undercarriage.setTag(data);
        undercarriage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MyIdle item = (MyIdle) v.getTag();
                final HashMap<String,Object> params = new HashMap<String, Object>();
                params.put("goid",item.goid);
                String title = "";
                if(item.coState == 0||item.coState == 2){
                    params.put("state",3);//0正常(上架)，3下架，4删除
                    title = "确定要下架吗?";
                }else if(item.coState == 1){
                    params.put("state",4);//0正常(上架)，3下架，4删除
                    title = "确定要删除吗?";
                }else if(item.coState == 3){
                    params.put("state",0);//0正常(上架)，3下架，4删除
                    title = "确定要重新上架吗?";
                }
                DialogHelper.showPromptDialog(context, "", title, "取消", null, "确定", new DialogHelper.OnMenuClick() {
                    @Override
                    public void onLeftMenuClick() {
                    }
                    @Override
                    public void onCenterMenuClick() {
                    }
                    @Override
                    public void onRightMenuClick() {
                        ReqApi.getInstance().postMethod(HttpConstant.updateState, params, new AsyncHttpCallBack() {
                            @Override
                            public void onSuccess(ResponseBody body) {
                                if (body.isSuccess()) {
                                    RxBus.get().post(BusAction.REFRESH_IDLE,"");
                                }
                                Tst.showToast(body.desc);//提示下架成功
                            }
                            @Override
                            public void onFailure(String msg) {
                                Tst.showToast(msg);
                            }
                            @Override
                            public Type setType() {
                                return new TypeToken<ResponseBody>(){}.getType();
                            }
                        });
                    }
                });
            }
        });
        helper.setText(R.id.describe_content, data.coremark);
        helper.setText(R.id.time, StrHelper.displayTime(data.goreleasetime,true,false));
        helper.setText(R.id.price, CommonUtils.addMoneySymbol(CommonUtils.keepTwoDecimal(data.goprice)));
        helper.setText(R.id.collect_amount, data.collectNumber+"");
        helper.setText(R.id.browse_amount, data.browseNumber+"");
        ImageView img = helper.getView(R.id.img);
        ImageLoadHelper.getInstance().displayImage(mContext,data.coInspirationPic, img);
    }
    CallBack callBack;
    public void setCallBack(CallBack callBack){
        this.callBack = callBack;
    }
    public interface CallBack{
        public void refresh();
    }
}
