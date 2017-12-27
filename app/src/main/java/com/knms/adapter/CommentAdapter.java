package com.knms.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.activity.base.BaseActivity;
import com.knms.activity.comment.CommentInfoActivity;
import com.knms.activity.im.ChatActivity;
import com.knms.activity.pic.ImgBrowerPagerActivity;
import com.knms.android.R;
import com.knms.bean.other.Pic;
import com.knms.bean.remark.Comment;
import com.knms.oncall.RecyclerItemClickListener;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.ScreenUtil;
import com.knms.util.StrHelper;
import com.knms.view.Star;
import com.knms.view.clash.FullyGridLayoutManager;
import com.knms.view.tv.ExpandableTextView;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Administrator on 2017/4/26.
 */

public class CommentAdapter extends BaseQuickAdapter<Comment,BaseViewHolder>{
    public CommentAdapter(List<Comment> data) {
        super(R.layout.item_comment, data);
    }
    public CommentAdapter() {
        this(null);
    }

    @Override
    protected void convert(final BaseViewHolder helper, final Comment item) {
        helper.setText(R.id.tv_name, item.nikeName);
        helper.setText(R.id.tv_create_time, StrHelper.displayTime(item.created, true, false, false));
        helper.setText(R.id.tv_browsenumber, "浏览" + item.browsenumber);
        helper.setText(R.id.tv_praise, item.agreenumber + "");
        if (item.shopReply != null) {
            helper.getView(R.id.rl_shop_reply).setVisibility(View.VISIBLE);
            helper.setText(R.id.tv_reply_time, StrHelper.displayTime(item.shopReply.created,true,false, false));
            helper.setText(R.id.tv_reply_content, item.shopReply.content);
        } else {
            helper.getView(R.id.rl_shop_reply).setVisibility(View.GONE);
        }
        helper.setVisible(R.id.tv_specs,!TextUtils.isEmpty(item.specDesc));
        helper.setText(R.id.tv_specs,TextUtils.isEmpty(item.specDesc) ? "" : item.specDesc);
        ImageView iv_avatar = helper.getView(R.id.iv_avatar);
        ImageLoadHelper.getInstance().displayImageHead(helper.getConvertView().getContext(), item.userPhoto, iv_avatar, LocalDisplay.dp2px(40), LocalDisplay.dp2px(40));
        iv_avatar.setTag(item.sid);
        iv_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(v.getContext(), "clickCommentInfoFromAvatar");
                String sid = (String) v.getTag();
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("sid", sid);
                ((BaseActivity) helper.getConvertView().getContext()).startActivityAnimGeneral(ChatActivity.class, params);
            }
        });
        ExpandableTextView tv_expand = helper.getView(R.id.tv_expandable);
        tv_expand.setTag(item);
        tv_expand.setOnclickListener(new ExpandableTextView.OnclickListener() {
            @Override
            public void onclick(View view) {
                MobclickAgent.onEvent(view.getContext(), "clickCommentInfoFromContent");
                Comment clickItem = (Comment) view.getTag();
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("commentId", clickItem.id);
                ((BaseActivity) helper.getConvertView().getContext()).startActivityAnimGeneral(CommentInfoActivity.class, params);
            }
        });
        tv_expand.updateForRecyclerView
                (item.content,
                        TextView.BufferType.NORMAL,
                        ScreenUtil.getScreenWidth() - LocalDisplay.dp2px(70));
        Star ratingBar = helper.getView(R.id.ratingBar);
        ratingBar.setMark(item.score);
        ImageView ivBtn_praise = helper.getView(R.id.ivBtn_praise);
        if (1 == item.isAgree) {
            ivBtn_praise.setImageResource(R.drawable.icon_praise_o);
        } else {
            ivBtn_praise.setImageResource(R.drawable.icon_praise_x);
        }
        if(item.state == 2){
            helper.setVisible(R.id.iv_icon,true);
        }else{
            helper.setVisible(R.id.iv_icon,false);
        }
        RecyclerView recyclerView = helper.getView(R.id.recyclerView);
        recyclerView.setLayoutManager(new FullyGridLayoutManager(helper.getConvertView().getContext(), 3));
        CommentImgAdapter adapter = new CommentImgAdapter(item.imgList);
        recyclerView.setAdapter(adapter);
        recyclerView.setTag(item);

        adapter.setOnItemClickListener(new OnItemClickListener<Pic>() {
            @Override
            public void onItemClick(BaseQuickAdapter<Pic, ? extends BaseViewHolder> adapter, View view, int position) {
                MobclickAgent.onEvent(view.getContext(), "clickCommentInfoFromPic");
                if(adapter.getData() != null && adapter.getData().size() > 0){
                    Intent intent = new Intent();
                    intent.setClass(view.getContext(),ImgBrowerPagerActivity.class);
                    intent.putExtra("pics",(Serializable) adapter.getData());
                    intent.putExtra("position",position);
                    view.getContext().startActivity(intent);
                }
            }
        });
        recyclerView.setTag(item);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(helper.getConvertView().getContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (item != null && position >= 0) {
                    MobclickAgent.onEvent(view.getContext(), "clickCommentInfoFromPic");
                    Intent intent = new Intent();
                    intent.setClass(view.getContext(),ImgBrowerPagerActivity.class);
                    intent.putExtra("pics",(Serializable) item.imgList);
                    intent.putExtra("position",position);
                    mContext.startActivity(intent);
                } else {
                    Map<String,Object> params = new HashMap<String, Object>();
                    params.put("commentId",item.id);
                    ((BaseActivity)mContext).startActivityAnimGeneral(CommentInfoActivity.class,params);
                }
            }
            @Override
            public void onItemLongClick(View view, int position) {}
        }));
    }
}
