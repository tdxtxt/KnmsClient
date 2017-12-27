package com.knms.adapter;

import android.app.Dialog;
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
public class UndercarriageFragmentAdapter extends BaseQuickAdapter<MyIdle,BaseViewHolder> {

    BaseFragmentActivity context;
    private String goid;

    public UndercarriageFragmentAdapter(BaseFragmentActivity context, List<MyIdle> mDatas) {
        super(R.layout.item_undercarriage, mDatas);
        this.context = context;
    }

    @Override
    public void convert(BaseViewHolder helper, final MyIdle data) {
        TextView grounding = helper.getView(R.id.grounding);
        TextView tv_txt = helper.getView(R.id.tv_txt);
        if (data.coState == 1) {
            grounding.setVisibility(View.GONE);
            tv_txt.setText("删除");
        } else {
            grounding.setVisibility(View.VISIBLE);
            tv_txt.setText("…");
        }
        grounding.setTag(data);
        grounding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyIdle item = (MyIdle) v.getTag();
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("goid", item.goid);
                params.put("state", 0);//0正常(上架)，3下架，4删除
                ReqApi.getInstance().postMethod(HttpConstant.updateState, params, new AsyncHttpCallBack() {
                    @Override
                    public void onSuccess(ResponseBody body) {
                        if (body.isSuccess()) {
                            RxBus.get().post(BusAction.REFRESH_IDLE, "");
                        }else{
                            Tst.showToast(body.desc);//提示上架成功
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

        tv_txt.setTag(data);
        tv_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MyIdle item = (MyIdle) v.getTag();
                final HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("goid", item.goid);
                params.put("state", 4);//0正常(上架)，3下架，4删除
                if (item.coState == 1) {
                    DialogHelper.showPromptDialog(context, "", "你确定要删除这个宝贝吗?", "取消", null, "确定", new DialogHelper.OnMenuClick() {
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

                } else {
                    DialogHelper.showBottomDialog(context, R.layout.dialog_undercarriage_fragement_adapter, new DialogHelper.OnEventListener<Dialog>() {
                        @Override
                        public void eventListener(View parentView, final Dialog window) {
                            parentView.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {//进编辑页面
                                    Map<String, Object> parmas = new HashMap<String, Object>();
                                    parmas.put("idle", data);
                                    context.startActivityAnimGeneral(ReleaseIdleActivity.class, parmas);
                                    window.dismiss();
                                }
                            });
                            parentView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {//删除
                                    DialogHelper.showPromptDialog(context, "", "你确定要删除这个宝贝吗?", "取消", null, "确定", new DialogHelper.OnMenuClick() {
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
                                public void onClick(View v) {//取消
                                    window.dismiss();
                                }
                            });
                        }
                    });
                }
            }
        });
        helper.setText(R.id.describe_content, data.coremark);
        helper.setText(R.id.time, StrHelper.displayTime(data.goreleasetime, true, false));
        helper.setText(R.id.price, CommonUtils.addMoneySymbol(CommonUtils.keepTwoDecimal(data.goprice)));
        helper.setText(R.id.collect_amount, data.collectNumber + "");
        helper.setText(R.id.browse_amount, data.browseNumber + "");
        ImageView img = helper.getView(R.id.img);
        ImageLoadHelper.getInstance().displayImage(mContext,data.coInspirationPic, img);
    }

    CallBack callBack;

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public interface CallBack {
        public void refresh();
    }
}
