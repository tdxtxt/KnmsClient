package com.knms.adapter.im;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.knms.activity.pic.ImgBrowerPagerActivity;
import com.knms.android.R;
import com.knms.core.im.IMHelper;
import com.knms.core.im.cache.NimUserInfoCache;
import com.knms.core.im.media.ImageUtil;
import com.knms.core.im.media.MessageAudioControl;
import com.knms.core.im.media.base.BaseAudioControl;
import com.knms.core.im.media.base.Playable;
import com.knms.core.im.msg.Product;
import com.knms.core.im.msg.ProductAttachment;
import com.knms.util.BitmapHelper;
import com.knms.util.CommonUtils;
import com.knms.util.ImageLoadHelper;
import com.knms.util.ScreenUtil;
import com.knms.util.StrHelper;
import com.knms.view.emoji.MoonUtil;
import com.knms.view.listview.IViewReclaimer;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.attachment.AudioAttachment;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by tdx on 2016/9/28.
 */

public class ChatAdapter extends BaseAdapter implements IViewReclaimer {
    public static int MAX_AUDIO_TIME_SECOND = 120;//最多录制音频 2 * 60 秒
    public static final int CLICK_TO_PLAY_AUDIO_DELAY = 500;//延迟500毫秒播放
    private List<IMMessage> datas;
    private final Context cxt;
    private OnChatItemClickListener listener;
    private MessageAudioControl audioControl;
    public ChatAdapter(Context cxt, List<IMMessage> datas, OnChatItemClickListener listener) {
        this.cxt = cxt;
        if (datas == null) {
            datas = new ArrayList<IMMessage>(0);
        }
        this.datas = datas;
        this.listener = listener;
    }

    public List<IMMessage> getData(){
        return datas;
    }
    public void addDataAbove(List<IMMessage> datas){
        if(datas != null && datas.size() > 0){
            if(this.datas == null) this.datas = new ArrayList<>(0);
            this.datas.addAll(0,datas);
            notifyDataSetChanged();
        }
    }
    public IMMessage getLaterItem(){
        if(this.datas != null && this.datas.size() >0){
            return this.datas.get(this.datas.size() - 1);
        }
        return null;
    }
    public void addDataLater(List<IMMessage> datas){
        if(this.datas == null) this.datas = new ArrayList<>(0);
        if(datas != null && datas.size() > 0){
            this.datas.addAll(datas);
            notifyDataSetChanged();
        }
    }
    public void addDataLater(IMMessage data){
        if(this.datas == null) this.datas = new ArrayList<>(0);
        if(data != null){
            this.datas.add(data);
            notifyDataSetChanged();
        }
    }
    public void setNewData(List<IMMessage> datas){
        if(this.datas == null) this.datas = new ArrayList<>(0);
        this.datas.clear();
        if(datas != null && datas.size() > 0) this.datas.addAll(datas);
        notifyDataSetChanged();
    }
    public void changeItem(IMMessage msg){//收发消息状态改变
        if(this.datas == null) this.datas = new ArrayList<>(0);
        IMMessage temp = null;
        int pos = 0;
        for (IMMessage item:this.datas) {
            if(!TextUtils.isEmpty(msg.getUuid())){
                if(msg.getUuid().equals(item.getUuid())){
                    temp = item;
                    break;
                }
            }
            pos ++;
        }
        if(temp != null){
            ((ArrayList)this.datas).set(pos,msg);//替换
        }else{
            this.datas.add(msg);
        }
        notifyDataSetChanged();
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
        final IMMessage data = datas.get(position);
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
            holder.layout_audio = (FrameLayout) v.findViewById(R.id.message_item_audio_container);
            holder.iv_playing_animation = (ImageView) v.findViewById(R.id.message_item_audio_playing_animation);
            holder.tv_audio_duration = (TextView) v.findViewById(R.id.message_item_audio_duration);
            holder.iv_audio_unread_indicator = (ImageView) v.findViewById(R.id.message_item_audio_unread_indicator);
//            holder.tvBtn_send = (TextView) v.findViewById(R.id.tvBtn_send);

            holder.rl_chat_content = (RelativeLayout) v.findViewById(R.id.rl_chat_content);
            holder.ll_chat_custom_content_send = (LinearLayout) v.findViewById(R.id.ll_chat_custom_content_send);
            holder.ll_chat_custom_content = (LinearLayout) v.findViewById(R.id.ll_chat_custom_content);
            v.setTag(holder);
        }else{
            holder = (ViewHolder) v.getTag();
        }
        if(position == 0){
            holder.tv_date.setText(StrHelper.displayTime(data.getTime(),true,true));
            holder.tv_date.setVisibility(View.VISIBLE);
        }else{
            String showTime = StrHelper.showImTime(data.getTime(),datas.get(position - 1).getTime());
            if(TextUtils.isEmpty(showTime)){
                holder.tv_date.setVisibility(View.GONE);
            }else{
                holder.tv_date.setText(showTime);
                holder.tv_date.setVisibility(View.VISIBLE);
            }
        }
        //如果是文本类型，则隐藏图片，如果是图片则隐藏文本
        if (data.getMsgType() == MsgTypeEnum.text) {
            holder.rl_chat_content.setVisibility(View.VISIBLE);
            holder.ll_chat_custom_content_send.setVisibility(View.GONE);
            holder.ll_chat_custom_content.setVisibility(View.GONE);

            holder.tv_chatcontent.setVisibility(View.VISIBLE);
            holder.img_chatimage.setVisibility(View.GONE);
            holder.layout_audio.setVisibility(View.GONE);
            holder.iv_audio_unread_indicator.setVisibility(View.GONE);
            holder.tv_chatcontent.setTextIsSelectable(true);
            holder.tv_chatcontent.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    CommonUtils.copyText(holder.tv_chatcontent);
                    return true;
                }
            });
            //转译表情图片
            MoonUtil.identifyFaceExpressionAndTags(v.getContext(), holder.tv_chatcontent, data.getContent(), ImageSpan.ALIGN_BOTTOM, 0.45f);
        }else if(data.getMsgType() == MsgTypeEnum.image){
            holder.rl_chat_content.setVisibility(View.VISIBLE);
            holder.ll_chat_custom_content_send.setVisibility(View.GONE);
            holder.ll_chat_custom_content.setVisibility(View.GONE);

            holder.img_chatimage.setVisibility(View.VISIBLE);
            holder.tv_chatcontent.setVisibility(View.GONE);
            holder.layout_audio.setVisibility(View.GONE);
            holder.iv_audio_unread_indicator.setVisibility(View.GONE);
            holder.img_chatimage.setTag(data);
            loadThumbnailImage(holder.img_chatimage);
        }else if(data.getMsgType() == MsgTypeEnum.audio){
            holder.rl_chat_content.setVisibility(View.VISIBLE);
            holder.ll_chat_custom_content_send.setVisibility(View.GONE);
            holder.ll_chat_custom_content.setVisibility(View.GONE);

            if(audioControl == null) audioControl = MessageAudioControl.getInstance(v.getContext());
            holder.layout_audio.setVisibility(View.VISIBLE);
            holder.img_chatimage.setVisibility(View.GONE);
            holder.tv_chatcontent.setVisibility(View.GONE);
            MsgStatusEnum status = data.getStatus();
            AttachStatusEnum attachStatus = data.getAttachStatus();
            if (!isSend(data) && attachStatus == AttachStatusEnum.transferred  && status != MsgStatusEnum.read) {
                holder.iv_audio_unread_indicator.setVisibility(View.VISIBLE);
            } else {
                holder.iv_audio_unread_indicator.setVisibility(View.GONE);
            }
            controlPlaying(data,holder.layout_audio);
        }else if(data.getMsgType() == MsgTypeEnum.custom){//自定义消息
            if(data.getAttachment() != null
                    && (data.getAttachment() instanceof ProductAttachment)){
                ProductAttachment attachment = (ProductAttachment) data.getAttachment();
                if(attachment.isShowState()){ //消息本生的载体
                    holder.rl_chat_content.setVisibility(View.VISIBLE);
                    holder.ll_chat_custom_content.setVisibility(View.VISIBLE);
                    holder.ll_chat_custom_content_send.setVisibility(View.GONE);
                    holder.img_chatimage.setVisibility(View.GONE);
                    holder.tv_chatcontent.setVisibility(View.GONE);
                    holder.layout_audio.setVisibility(View.GONE);
                    holder.iv_audio_unread_indicator.setVisibility(View.GONE);

                    View tvBtn_send = holder.ll_chat_custom_content.findViewById(R.id.tvBtn_send);
                    tvBtn_send.setVisibility(View.GONE);
                }else{//点击发送的载体
                    holder.rl_chat_content.setVisibility(View.GONE);
                    holder.ll_chat_custom_content_send.setVisibility(View.VISIBLE);
                    holder.ll_chat_custom_content_send.setBackgroundResource(R.color.color_white);
                    View tvBtn_send = holder.ll_chat_custom_content_send.findViewById(R.id.tvBtn_send);
                    tvBtn_send.setVisibility(View.VISIBLE);
                    tvBtn_send.setTag(attachment.value);
                    tvBtn_send.setVisibility(View.VISIBLE);
                    tvBtn_send.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Product product = (Product) v.getTag();
                            ProductAttachment attachment = new ProductAttachment();
                            attachment.value = product;
                            IMMessage message = MessageBuilder.createCustomMessage(
                                    data.getSessionId(), // 会话对象
                                    SessionTypeEnum.P2P, // 会话类型
                                    attachment // 自定义消息附件
                            );
                            IMHelper.getInstance().sendMsg(message);
                        }
                    });
                }

                if(attachment.value != null){
                    final View parentView = attachment.isShowState() ? holder.ll_chat_custom_content : holder.ll_chat_custom_content_send;
                    ImageView iv_icon = (ImageView) parentView.findViewById(R.id.iv_icon);
                    ImageLoadHelper.getInstance().displayImage(cxt,attachment.value.icon,iv_icon);
                    TextView tv_content = (TextView) parentView.findViewById(R.id.tv_content);
                    tv_content.setText(attachment.value.content);
                    TextView tv_price = (TextView) parentView.findViewById(R.id.tv_price);
                    if(TextUtils.isEmpty(attachment.value.price)){
                        tv_price.setVisibility(View.GONE);
                    }else {
                        if("".equals(attachment.value.price.replace("0","").replace(".",""))){
                            tv_price.setVisibility(View.GONE);
                        }else {
                            tv_price.setVisibility(View.VISIBLE);
                            tv_price.setText(CommonUtils.addMoneySymbol(attachment.value.price));
                        }
                    }
                    parentView.setTag(attachment.value);
                    parentView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Product product = (Product) parentView.getTag();
                            CommonUtils.startActivity(cxt,product);
                        }
                    });
                }
            }
        }

        if (listener != null) {
            holder.tv_chatcontent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onTextClick(position);
                }
            });
        }
        holder.img_chatimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IMMessage msg = (IMMessage) v.getTag();
                IMHelper.getInstance().queryMessageListByImage(msg)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Map<String,Object>>() {
                    @Override
                    public void call(Map<String,Object> vaule) {
                        Intent intent = new Intent(cxt, ImgBrowerPagerActivity.class);
                        intent.putStringArrayListExtra("data", (ArrayList<String>)vaule.get("data"));
                        intent.putExtra("position", (int)vaule.get("position"));
                        cxt.startActivity(intent);
                    }
                });
            }
        });
        //显示头像
        String accout = "";
        if(isSend(data)){//自己
            accout = data.getFromAccount();
        }else{//别人
            accout = data.getSessionId();
        }
        NimUserInfoCache.getInstance().getUserInfoObserable(accout).subscribe(new Action1<NimUserInfo>() {
            @Override
            public void call(NimUserInfo nimUserInfo) {
                if(nimUserInfo != null)
                ImageLoadHelper.getInstance().displayImageHead(cxt,nimUserInfo.getAvatar(),holder.img_avatar);
            }
        });

        //重发
        holder.img_sendfail.setTag(data);
        holder.img_sendfail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IMMessage item = (IMMessage) v.getTag();
                IMHelper.getInstance().sendMsg(item);
                item.setStatus(MsgStatusEnum.sending);
                NIMClient.getService(MsgService.class).updateIMMessageStatus(item);
                changeItem(item);
            }
        });
        if(data.getStatus() == MsgStatusEnum.fail){
            holder.progress.setVisibility(View.GONE);
            holder.img_sendfail.setVisibility(View.VISIBLE);
        }else if(data.getStatus() == MsgStatusEnum.success){
            holder.progress.setVisibility(View.GONE);
            holder.img_sendfail.setVisibility(View.GONE);
        }else if(data.getStatus() == MsgStatusEnum.sending){
            holder.progress.setVisibility(View.VISIBLE);
            holder.img_sendfail.setVisibility(View.GONE);
        }
        return v;
    }

    /***************************图片*******************************/
    private void loadThumbnailImage(ImageView imageView) {
        IMMessage msg = (IMMessage) imageView.getTag();
        FileAttachment msgAttachment = (FileAttachment) msg.getAttachment();
        setImageSize(msgAttachment.getPath(),imageView);
        String thumbPath = msgAttachment.getThumbPath();
        if(TextUtils.isEmpty(thumbPath)){
            thumbPath = msgAttachment.getPath();
        }
        if(TextUtils.isEmpty(thumbPath)){
            thumbPath = msgAttachment.getUrl();
        }
        ImageLoadHelper.getInstance().displayImage(cxt,thumbPath,imageView,0,0);
    }
    private void setImageSize(String thumbPath,ImageView imageView) {
        IMMessage item = (IMMessage) imageView.getTag();
        int[] bounds = null;
        if (thumbPath != null) {
            bounds = BitmapHelper.decodeBound(new File(thumbPath));
        }
        if (bounds == null) {
            ImageAttachment attachment = (ImageAttachment) item.getAttachment();
            bounds = new int[]{attachment.getWidth(), attachment.getHeight()};
        }

        if (bounds != null) {
            ImageUtil.ImageSize imageSize = ImageUtil.getThumbnailDisplaySize(bounds[0], bounds[1], getImageMaxEdge(), getImageMinEdge());
            setLayoutParams(imageSize.width, imageSize.height, imageView);
        }else{
            setLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, imageView);
        }
    }
    // 设置控件的长宽
    protected void setLayoutParams(int width, int height, View... views) {
        for (View view : views) {
            ViewGroup.LayoutParams maskParams = view.getLayoutParams();
            maskParams.width = width;
            maskParams.height = height;
            view.setLayoutParams(maskParams);
        }
    }

    /***************************语音*******************************/

    private void controlPlaying(final IMMessage message, View content) {
        final AudioAttachment msgAttachment = (AudioAttachment) message.getAttachment();
        long duration = msgAttachment.getDuration();
        setAudioBubbleWidth(duration,content);
        ImageView animationView = (ImageView) content.findViewById(R.id.message_item_audio_playing_animation);
        TextView durationLabel = (TextView) content.findViewById(R.id.message_item_audio_duration);
        animationView.setTag(message);
        updateTime(duration,durationLabel);
        final AudioControlListener onPlayListener = new AudioControlListener(animationView,durationLabel);
        if (!isMessagePlaying(audioControl, message)) {
            if (audioControl.getAudioControlListener() != null
                    && audioControl.getAudioControlListener().equals(onPlayListener)) {
                audioControl.changeAudioControlListener(null);
            }
            stop(animationView);
        } else {
            audioControl.changeAudioControlListener(onPlayListener);
            play(animationView);
        }
        content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioControl.startPlayAudioDelay(CLICK_TO_PLAY_AUDIO_DELAY, message, onPlayListener);
            }
        });
    }
    private void updateTime(long milliseconds,TextView durationLabel) {
        long seconds = StrHelper.getSecondsByMilliseconds(milliseconds);

        if (seconds >= 0) {
            durationLabel.setText(seconds + "\"");
        } else {
            durationLabel.setText("");
        }
    }
    protected boolean isMessagePlaying(MessageAudioControl audioControl, IMMessage message) {
        if (audioControl.getPlayingAudio() != null && audioControl.getPlayingAudio().isTheSame(message)) {
            return true;
        } else {
            return false;
        }
    }
    private void setAudioBubbleWidth(long milliseconds,View content) {
        long seconds = StrHelper.getSecondsByMilliseconds(milliseconds);

        int currentBubbleWidth = calculateBubbleWidth(seconds, MAX_AUDIO_TIME_SECOND);
        ViewGroup.LayoutParams layoutParams = content.getLayoutParams();
        layoutParams.width = currentBubbleWidth;
        content.setLayoutParams(layoutParams);
    }
    private int calculateBubbleWidth(long seconds, int MAX_TIME) {
        int maxAudioBubbleWidth = getAudioMaxEdge();
        int minAudioBubbleWidth = getAudioMinEdge();

        int currentBubbleWidth;
        if (seconds <= 0) {
            currentBubbleWidth = minAudioBubbleWidth;
        } else if (seconds > 0 && seconds <= MAX_TIME) {
            currentBubbleWidth = (int) ((maxAudioBubbleWidth - minAudioBubbleWidth) * (2.0 / Math.PI)
                    * Math.atan(seconds / 10.0) + minAudioBubbleWidth);
        } else {
            currentBubbleWidth = maxAudioBubbleWidth;
        }

        if (currentBubbleWidth < minAudioBubbleWidth) {
            currentBubbleWidth = minAudioBubbleWidth;
        } else if (currentBubbleWidth > maxAudioBubbleWidth) {
            currentBubbleWidth = maxAudioBubbleWidth;
        }

        return currentBubbleWidth;
    }
    public static int getAudioMaxEdge() {
        return (int) (0.6 * (float)ScreenUtil.getScreenWidth() *  0.85);
    }

    public static int getAudioMinEdge() {
        return (int) (0.1875 * (float)ScreenUtil.getScreenWidth() *  0.85);
    }
    public static int getImageMaxEdge() {
        return (int) (165.0 / 320.0 * (float)ScreenUtil.getScreenWidth());
    }

    public static int getImageMinEdge() {
        return (int) (76.0 / 320.0 * (float)ScreenUtil.getScreenWidth());
    }
    private void play(ImageView animationView) {
        if (animationView.getBackground() instanceof AnimationDrawable) {
            AnimationDrawable animation = (AnimationDrawable) animationView.getBackground();
            animation.start();
        }
    }
    private void stop(ImageView animationView) {
        if (animationView.getBackground() instanceof AnimationDrawable) {
            AnimationDrawable animation = (AnimationDrawable) animationView.getBackground();
            animation.stop();
            animation.selectDrawable(2);
        }
    }
    private boolean isSend(IMMessage msg){
        return msg.getDirect() == MsgDirectionEnum.Out;
    }

    /**
     * 资源回收
     * @param view
     */
    @Override
    public void reclaimView(View view) {
        if (view == null) {
            return;
        }
        if (audioControl != null && audioControl.getAudioControlListener() != null) {
            audioControl.changeAudioControlListener(null);
        }
    }

    static class ViewHolder {
        TextView tv_date;
        ImageView img_avatar;
        TextView tv_chatcontent;//发送文字内容
        ImageView img_chatimage;//图片消息
        ImageView img_sendfail;//图片发送失败
        ProgressBar progress;//发送进度
        RelativeLayout layout_content,rl_chat_content;
        LinearLayout ll_chat_custom_content_send,ll_chat_custom_content;
        FrameLayout layout_audio;
        ImageView iv_playing_animation;
        TextView tv_audio_duration;
        ImageView iv_audio_unread_indicator;
//        TextView tvBtn_send;
    }
    public class AudioControlListener implements BaseAudioControl.AudioControlListener {
        ImageView animationView;
        TextView durationLabel;
        public AudioControlListener(ImageView animationView,TextView durationLabel){
            this.durationLabel = durationLabel;
            this.animationView = animationView;
        }
        @Override
        public void updatePlayingProgress(Playable playable, long curPosition) {
            if (curPosition > playable.getDuration()) {
                return;
            }
            updateTime(curPosition,durationLabel);
        }
        @Override
        public void onAudioControllerReady(Playable playable) {
            play(animationView);
        }
        @Override
        public void onEndPlay(Playable playable) {
            updateTime(playable.getDuration(),durationLabel);
            if(animationView.getTag() != null){
                IMMessage item = (IMMessage) animationView.getTag();
                if(!isSend(item)){
                    NIMClient.getService(MsgService.class).sendMessageReceipt(item.getSessionId(), item);
                    item.setStatus(MsgStatusEnum.read);//设置为已读
                    notifyDataSetChanged();//更新UI
                }
            }
            stop(animationView);
        }
    }

    /**
     * 聊天列表中对内容的点击事件监听
     */
    public interface OnChatItemClickListener {
        void onPhotoClick(int position);

        void onTextClick(int position);

        void onFaceClick(int position);
    }
}
