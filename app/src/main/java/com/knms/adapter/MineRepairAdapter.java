package com.knms.adapter;

import android.app.Dialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.activity.base.BaseActivity;
import com.knms.activity.im.ChatActivity;
import com.knms.activity.mine.MineRepairDetailActivity;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.repair.MyRepair;
import com.knms.net.RxRequestApi;
import com.knms.util.DialogHelper;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.StrHelper;
import com.knms.util.Tst;
import com.knms.view.CircleImageView;
import com.knms.view.clash.FullyLinearLayoutManager;
import com.knms.view.flowlayout.TagFlowLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/9/12.
 */
public class MineRepairAdapter extends BaseQuickAdapter<MyRepair,BaseViewHolder> {

    BaseActivity context;

    public MineRepairAdapter(BaseActivity context, List<MyRepair> mDatas) {
        super(R.layout.item_mine_price_ration, mDatas);
        this.context = context;
    }

    @Override
    public void convert(BaseViewHolder helper, MyRepair data) {
        helper.getView(R.id.tv_myprice_refresh).setVisibility(View.GONE);
        ImageView undercarriage = helper.getView(R.id.undercarriage);
        if (data.restate == 1) {
            undercarriage.setVisibility(View.VISIBLE);
        } else if (data.restate == 0) {
            undercarriage.setVisibility(View.GONE);
        }
        helper.setText(R.id.release_time, StrHelper.displayTime(data.recreated,true,false));
        helper.setText(R.id.remark, data.reremark);
        TextView check_all = helper.getView(R.id.check_all);
        RecyclerView rv_reply= helper.getView(R.id.recyclerview_reply);
        rv_reply.setLayoutManager(new FullyLinearLayoutManager(mContext));
        if (data.commentList != null) {
            check_all.setVisibility(data.commentList.size()==0?View.GONE:View.VISIBLE);
            helper.setText(R.id.reply_amount, "共有" + data.commentList.size() + "个师傅回复");
            rv_reply.setAdapter(new BaseQuickAdapter<MyRepair.CommentList,BaseViewHolder>(R.layout.item_merchant_reply,data.commentList.size()>2?data.commentList.subList(0,2):data.commentList) {
                @Override
                protected void convert(BaseViewHolder helper, final MyRepair.CommentList item) {
                    helper.setText(R.id.name, item.usnickname);
                    helper.setText(R.id.reply_time, StrHelper.displayTime(item.concattime,true,false));
                    CircleImageView head_picture = helper.getView(R.id.head_picture);
                    ImageLoadHelper.getInstance().displayImageHead(context,item.usphoto, head_picture);
                    helper.getConvertView().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("sid", item.userid);
                            context.startActivityAnimGeneral(ChatActivity.class, map);
                        }
                    });
                }
            });
        }else{
            helper.setText(R.id.reply_amount, "共有0个师傅回复");
        }

        ImageView main_picture = helper.getView(R.id.main_picture);
        if(TextUtils.isEmpty(data.image))
            main_picture.setImageResource(R.drawable.icon_default_img);
        else
            ImageLoadHelper.getInstance().displayImage(context,data.image, main_picture, LocalDisplay.dp2px(80),LocalDisplay.dp2px(80));

        check_all.setTag(data);
        check_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyRepair item = (MyRepair) v.getTag();
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("myRepair", item);
                params.put("falg",true);
                context.startActivityForResultAnimGeneral(MineRepairDetailActivity.class, params, 1);
            }
        });

        helper.getView(R.id.type).setVisibility(!data.retype.equals("") ? View.VISIBLE : View.GONE);
        helper.setText(R.id.type, data.retype);
        helper.getView(R.id.location).setVisibility(!data.rearea.equals("") ? View.VISIBLE : View.GONE);
        helper.setText(R.id.location, data.rearea);

        TagFlowLayout tagFlowLayout = helper.getView(R.id.tag_flow_layout);
        tagFlowLayout.setVisibility(View.GONE);

        TextView edit = helper.getView(R.id.edit);
        edit.setTag(data);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MyRepair item = (MyRepair) v.getTag();
                DialogHelper.showBottomDialog(context, R.layout.dialog_mine_price_ration, new DialogHelper.OnEventListener<Dialog>() {
                    @Override
                    public void eventListener(View parentView, final Dialog window) {
                        TextView tvUndercarriage = (TextView) parentView.findViewById(R.id.undercarriage);
                        TextView tvDelete = (TextView) parentView.findViewById(R.id.delete);
                        if (item.restate == 1) {
                            tvUndercarriage.setVisibility(View.GONE);
                        } else {
                            tvUndercarriage.setText("已解决");
                            tvDelete.setVisibility(View.GONE);
                        }
                        tvUndercarriage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                DialogHelper.showPromptDialog(context, "确定已解决了吗？", "如果选择解决了，维修师傅将不再看到此消息", "取消", "", "确定", new DialogHelper.OnMenuClick() {
                                    @Override
                                    public void onLeftMenuClick() {
                                    }

                                    @Override
                                    public void onCenterMenuClick() {
                                    }

                                    @Override
                                    public void onRightMenuClick() {
                                        updateState(item.reid, 1);
                                    }
                                });
                                window.dismiss();
                            }
                        });
                        parentView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                DialogHelper.showPromptDialog(context, "", "确定删除这条维修消息吗？", "取消", "", "确定", new DialogHelper.OnMenuClick() {
                                    @Override
                                    public void onLeftMenuClick() {
                                    }

                                    @Override
                                    public void onCenterMenuClick() {
                                    }

                                    @Override
                                    public void onRightMenuClick() {
                                        updateState(item.reid, 2);
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

    private void updateState(String id, int state) {
        RxRequestApi.getInstance().getApiService().updateMyRepairState(state, id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody>() {
                    @Override
                    public void call(ResponseBody responseBody) {
                        if (responseBody.isSuccess()) {
                            if (callBack != null) callBack.refresh();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast(throwable.getMessage());
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
