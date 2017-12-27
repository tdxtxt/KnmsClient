package com.knms.activity.comment;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.activity.base.BaseActivity;
import com.knms.activity.base.HeadNotifyBaseActivity;
import com.knms.activity.im.ChatActivity;
import com.knms.activity.login.FasterLoginActivity;
import com.knms.activity.pic.ImgBrowerPagerActivity;
import com.knms.adapter.CommentImgAdapter;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.other.Pic;
import com.knms.bean.remark.Comment;
import com.knms.net.RxRequestApi;
import com.knms.util.ImageLoadHelper;
import com.knms.util.SPUtils;
import com.knms.util.StrHelper;
import com.knms.util.Tst;
import com.knms.view.Star;
import com.knms.view.clash.FullyGridLayoutManager;
import com.knms.view.tv.ExpandableTextView;
import com.umeng.analytics.MobclickAgent;
import com.wx.goodview.GoodView;

import java.util.HashMap;
import java.util.Map;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;
import rx.Observable;
import rx.functions.Action1;


/**
 * Created by tdx on 2017/4/28.
 * 评论信息界面
 */

public class CommentInfoActivity extends HeadNotifyBaseActivity{
    private GoodView goodView;
    private ExpandableTextView tv_expandable;
    private RecyclerView recyclerView;
    private ImageView iv_avatar,iv_icon,ivBtn_praise;
    private TextView tv_name;
    private Star ratingbar;
    private TextView tv_create_time,tv_browsenumber,tv_praise,tv_reply_time,tv_reply_content;
    private RelativeLayout rl_shop_reply;
    private CommentImgAdapter imgAdapter;
    private Comment data;

    private String commentId;//评论信息id
    private String orderId;//订单id
    private String title;

    @Override
    protected void getParmas(Intent intent) {
        commentId = intent.getStringExtra("commentId");
        if(!TextUtils.isEmpty(commentId)) title = "评价信息";

        orderId = intent.getStringExtra("orderId");
        if(!TextUtils.isEmpty(orderId)) title = "用户评价";
    }

    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText(title);
    }
    @Override
    protected int layoutResID() {
        return R.layout.activity_comment_info;
    }

    @Override
    protected void initView() {
        tv_expandable = findView(R.id.tv_expandable);
        recyclerView = findView(R.id.recyclerView);
        iv_avatar = findView(R.id.iv_avatar);
        iv_icon = findView(R.id.iv_icon);
        ivBtn_praise = findView(R.id.ivBtn_praise);
        tv_name = findView(R.id.tv_name);
        ratingbar = findView(R.id.ratingBar);
        tv_create_time = findView(R.id.tv_create_time);
        tv_browsenumber = findView(R.id.tv_browsenumber);
        tv_praise = findView(R.id.tv_praise);
        tv_reply_content = findView(R.id.tv_reply_content);
        tv_reply_time = findView(R.id.tv_reply_time);
        rl_shop_reply = findView(R.id.rl_shop_reply);

        recyclerView.setLayoutManager(new FullyGridLayoutManager(this,3));
        imgAdapter = new CommentImgAdapter(null);
        recyclerView.setAdapter(imgAdapter);
    }
    private void updateView(Comment comment) {
        if(comment == null) return;
        this.data = comment;
        tv_expandable.setNormalText(data.content);
        imgAdapter.setNewData(data.imgList);
        ImageLoadHelper.getInstance().displayImageHead(this,data.userPhoto,iv_avatar);
        iv_avatar.setTag(comment.sid);
        iv_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sid = (String) v.getTag();
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("sid", sid);
                startActivityAnimGeneral(ChatActivity.class, params);
            }
        });
        if(data.state == 2){
            iv_icon.setVisibility(View.VISIBLE);
        }else{
            iv_icon.setVisibility(View.GONE);
        }
        tv_name.setText(data.nikeName);
        ratingbar.setMark(data.score);
        tv_create_time.setText(StrHelper.displayTime(data.created,true, false, false));
        tv_praise.setText("" + data.agreenumber);
        tv_browsenumber.setText("浏览" + data.browsenumber);
        if(data.isAgree == 1){//已点赞
            ivBtn_praise.setImageResource(R.drawable.icon_praise_o);
        }else{
            ivBtn_praise.setImageResource(R.drawable.icon_praise_x);
        }
        if(data.shopReply != null){
            rl_shop_reply.setVisibility(View.VISIBLE);
            tv_reply_content.setText(data.shopReply.content);
            tv_reply_time.setText(StrHelper.displayTime(data.shopReply.created,true,false,false));
        }else{
            rl_shop_reply.setVisibility(View.GONE);
        }
        imgAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<Pic>() {
            @Override
            public void onItemClick(BaseQuickAdapter<Pic, ? extends BaseViewHolder> adapter, View view, int position) {
                if(data.imgList != null && data.imgList.size() > 0){
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("pics", adapter.getData());
                    params.put("position", position);
                    startActivityAnimGeneral(ImgBrowerPagerActivity.class, params);
                }
            }
        });
    }
    @Override
    protected void initData() {
        //滑动黏性
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollview);
        OverScrollDecoratorHelper.setUpOverScroll(scrollView);
        //初始化点赞动画加载器
        goodView = new GoodView(this);
        findView(R.id.rlBtn_praise).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                praiseToComment();
            }
        });
        reqApi();
    }
    private void praiseToComment(){
        MobclickAgent.onEvent(this, "clickCommentInfoFromPraise");
        if(ContextCompat.getDrawable(this,R.drawable.icon_praise_o).getConstantState().equals(ivBtn_praise.getDrawable().getCurrent().getConstantState())){
            Tst.showToast("你已经赞过了");
            return;
        }
        if(!SPUtils.isLogin()){
            Tst.showToast("请先登录");
            startActivityAnimGeneral(FasterLoginActivity.class,null);
            return;
        }
        if(data == null) return;
        RxRequestApi.getInstance().getApiService().praiseToComment(commentId)
                .compose(this.<ResponseBody>applySchedulers())
                .subscribe(new Action1<ResponseBody>() {
                    @Override
                    public void call(ResponseBody body) {
                        if(body.isSuccess()){
                            goodView.setTextInfo("+1", Color.parseColor("#ccfa3c55"),12);
                            goodView.show(ivBtn_praise);
                            tv_praise.setText("" + (data.agreenumber + 1));
                            ivBtn_praise.setImageResource(R.drawable.icon_praise_o);
                        }else{
                            Tst.showToast(body.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {}
                });
    }
    @Override
    public String setStatisticsTitle() {
        return null;
    }

    @Override
    protected void reqApi() {
        Observable<ResponseBody<Comment>> observable = null;
        if(!TextUtils.isEmpty(orderId)){
            observable = RxRequestApi.getInstance().getApiService().getCommentByOrder(orderId);
        }else if(!TextUtils.isEmpty(commentId)){
            observable = RxRequestApi.getInstance().getApiService().getCommentByProduct(commentId);
        }
        if(observable == null) return;
        observable.compose(this.<ResponseBody<Comment>>applySchedulers())
                .subscribe(new Action1<ResponseBody<Comment>>() {
                    @Override
                    public void call(ResponseBody<Comment> body) {
                        if(body.isSuccess()){
                            if(TextUtils.isEmpty(commentId)) commentId = body.data.id;
                            updateView(body.data);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {}
                });
    }
}
