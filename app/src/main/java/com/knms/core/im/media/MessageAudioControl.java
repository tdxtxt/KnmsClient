package com.knms.core.im.media;

import android.content.Context;
import android.widget.Toast;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.core.im.media.base.BaseAudioControl;
import com.knms.core.im.media.base.Playable;
import com.knms.core.im.storage.StorageUtil;
import com.knms.util.Tst;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.attachment.AudioAttachment;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.util.List;

public class MessageAudioControl extends BaseAudioControl<IMMessage> {
    private static MessageAudioControl mMessageAudioControl = null;

    private boolean mIsNeedPlayNext = false;


    private IMMessage mItem = null;

    private MessageAudioControl(Context context) {
        super(context, true);
    }

    public static MessageAudioControl getInstance(Context context) {
        if (mMessageAudioControl == null) {
            synchronized (MessageAudioControl.class) {
                if (mMessageAudioControl == null) {
                    mMessageAudioControl = new MessageAudioControl(context);
                }
            }
        }

        return mMessageAudioControl;
    }

    @Override
    protected void setOnPlayListener(Playable playingPlayable, AudioControlListener audioControlListener) {
        this.audioControlListener = audioControlListener;

        BasePlayerListener basePlayerListener = new BasePlayerListener(currentAudioPlayer, playingPlayable) {
            @Override
            public void onInterrupt() {
                if (!checkAudioPlayerValid()) {
                    return;
                }

                super.onInterrupt();
//                cancelPlayNext();
            }

            @Override
            public void onError(String error) {
                if (!checkAudioPlayerValid()) {
                    return;
                }

                super.onError(error);
//                cancelPlayNext();
            }

            @Override
            public void onCompletion() {
                if (!checkAudioPlayerValid()) {
                    return;
                }
                resetAudioController(listenerPlayingPlayable);
                boolean isLoop = false;
//                if (mIsNeedPlayNext) {
//                    if (mAdapter != null && mItem != null) {
//                        isLoop = playNextAudio(mAdapter, mItem);
//                    }
//                }

                if(!isLoop) {
                    if (audioControlListener != null) {
                        audioControlListener.onEndPlay(currentPlayable);
                    }

                    playSuffix();
                }
            }
        };

        basePlayerListener.setAudioControlListener(audioControlListener);
        currentAudioPlayer.setOnPlayListener(basePlayerListener);
    }

    @Override
    public IMMessage getPlayingAudio() {
        if (isPlayingAudio() && AudioMessagePlayable.class.isInstance(currentPlayable)) {
            return ((AudioMessagePlayable) currentPlayable).getMessage();
        } else {
            return null;
        }
    }

    @Override
    public void startPlayAudioDelay(
            long delayMillis,
            IMMessage message,
            AudioControlListener audioControlListener, int audioStreamType)
    {
        startPlayAudio(message, audioControlListener, audioStreamType, true, delayMillis);
    }

    //连续播放时不需要resetOrigAudioStreamType
    private void startPlayAudio(
            IMMessage message,
            AudioControlListener audioControlListener,
            int audioStreamType,
            boolean resetOrigAudioStreamType,
            long delayMillis)
    {
        if (StorageUtil.isExternalStorageExist()) {

            if (startAudio(new AudioMessagePlayable(message), audioControlListener, audioStreamType, resetOrigAudioStreamType, delayMillis)) {
                // 将未读标识去掉,更新数据库
                if (isUnreadAudioMessage(message)) {
                    message.setStatus(MsgStatusEnum.read);
                    NIMClient.getService(MsgService.class).updateIMMessageStatus(message);
                }
            }
        } else {
            Tst.showToast("请插入SD卡");
        }
    }


    public void stopAudio() {
        super.stopAudio();
    }
    public void onDestroy(){
        this.stopAudio();
//        this.mContext = null;//删除引用，防止内存泄露
    }
    public boolean isUnreadAudioMessage(IMMessage message) {
        if ((message.getMsgType() == MsgTypeEnum.audio)
                && message.getDirect() == MsgDirectionEnum.In
                && message.getAttachStatus() == AttachStatusEnum.transferred
                && message.getStatus() != MsgStatusEnum.read) {
            return true;
        } else {
            return false;
        }
    }
}
