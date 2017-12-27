package com.knms.adapter.im;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.knms.android.R;
import com.knms.bean.im.SendState;
import com.knms.bean.im.KnmsMsg;
import com.knms.util.CommonUtils;
import com.knms.util.ImageLoadHelper;
import com.knms.util.SPUtils;
import com.knms.util.StrHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by tdx on 2016/9/28.
 */

public class KnmsChatAdapter extends BaseAdapter{
    private List<KnmsMsg> datas;
    private final Context cxt;
    private String type;
    private String KeFuMsg = "KeFuMsg";
    private String KnmsMsg = "KnmsMsg";

    public KnmsChatAdapter(Context cxt,String type) {
        this.cxt = cxt;
        this.type = type;
        this.datas = new ArrayList<>(0);
    }
    public void setNewData(List<KnmsMsg> datas) {
        if (datas == null) {
            datas = new ArrayList<>(0);
        }
        this.datas = datas;
        notifyDataSetChanged();
    }
    public void removeItem(KnmsMsg item){
        this.datas.remove(item);
    }
    public int addData(List<KnmsMsg> datas){
        if(datas != null && datas.size() > 0){
            this.datas.addAll(0,datas);
            notifyDataSetChanged();
            return datas.size();
        }
        return 0;
    }
    public int putData(List<KnmsMsg> datas){
        if(datas != null && datas.size() > 0){
            this.datas.addAll(datas);
            notifyDataSetChanged();
            return this.datas.size();
        }
        return 0;
    }
    public void addItem(KnmsMsg data){
        if(data != null){
            this.datas.add(data);
            notifyDataSetChanged();
        }
    }
    public KnmsMsg getLastData(){
        if(this.datas != null && this.datas.size() > 0){
            return datas.get(datas.size() - 1);
        }
        return null;
    }
    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return isSend(datas.get(position)) ? 1 : 0;
    }
    @Override
    public int getViewTypeCount() {
        return 2;
    }
    @Override
    public View getView(final int position, View v, ViewGroup parent) {
        final ViewHolder holder;
        final KnmsMsg data = datas.get(position);
        if (v == null) {
            holder = new ViewHolder();
            if(isSend(data)){
                v = View.inflate(cxt, R.layout.chat_item_list_right, null);
            }else {
                v = View.inflate(cxt, R.layout.chat_item_list_left, null);
            }
            holder.layout_content = (RelativeLayout) v.findViewById(R.id.chat_item_layout_content);
            holder.img_avatar = (ImageView) v.findViewById(R.id.chat_item_avatar);
            holder.img_chatimage = (ImageView) v.findViewById(R.id.chat_item_content_image);
            holder.img_sendfail = (ImageView) v.findViewById(R.id.chat_item_fail);
            holder.progress = (ProgressBar) v.findViewById(R.id.chat_item_progress);
            holder.tv_chatcontent = (TextView) v.findViewById(R.id.chat_item_content_text);
            holder.tv_date = (TextView) v.findViewById(R.id.chat_item_date);
            v.setTag(holder);
        }else{
            holder = (ViewHolder) v.getTag();
        }
        if(isSend(data)){
            if(SPUtils.getUser() != null) ImageLoadHelper.getInstance().displayImageHead(cxt,SPUtils.getUser().uicon,holder.img_avatar);
        }else{
            if(KeFuMsg.equals(type)){
                holder.img_avatar.setImageResource(R.drawable.icon_kefu);
            }else if(KnmsMsg.equals(type)){
                holder.img_avatar.setImageResource(R.drawable.icon_knms);
            }
        }
        if(position == 0){
            holder.tv_date.setText(StrHelper.displayTime(data.time,true,true));
            holder.tv_date.setVisibility(View.VISIBLE);
        }else{
            String time = StrHelper.showImTime(StrHelper.str2Millis(data.time),StrHelper.str2Millis(datas.get(position - 1).time));
            if(TextUtils.isEmpty(time)){
                holder.tv_date.setVisibility(View.GONE);
            }else{
                holder.tv_date.setText(time);
                holder.tv_date.setVisibility(View.VISIBLE);
            }
        }

        //文本类型，则隐藏图片
        holder.img_chatimage.setVisibility(View.GONE);
        holder.tv_chatcontent.setVisibility(View.VISIBLE);

        CommonUtils.handleText(holder.tv_chatcontent, data.content);
        holder.tv_chatcontent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                CommonUtils.copyText(holder.tv_chatcontent);
                return true;
            }
        });

            if(data.sendStatus == SendState.fail){
                holder.progress.setVisibility(View.GONE);
                holder.img_sendfail.setVisibility(View.VISIBLE);
            }else if(data.sendStatus == SendState.success){
                holder.progress.setVisibility(View.GONE);
                holder.img_sendfail.setVisibility(View.GONE);
            }else if(data.sendStatus == SendState.sending){
                holder.progress.setVisibility(View.VISIBLE);
                holder.img_sendfail.setVisibility(View.GONE);
            }
        return v;
    }

    private boolean isSend(KnmsMsg msg){
        return 1 == msg.role;//用户
    }
    static class ViewHolder {
        TextView tv_date;
        ImageView img_avatar;
        TextView tv_chatcontent;
        ImageView img_chatimage;
        ImageView img_sendfail;
        ProgressBar progress;
        RelativeLayout layout_content;
    }
}
