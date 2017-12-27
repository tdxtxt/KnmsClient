package com.knms.activity.im;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.knms.activity.ShopActivityF;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.activity.repair.RepairDetailActivity;
import com.knms.adapter.im.ChatAdapter;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.im.BaseAction;
import com.knms.bean.user.UserType;
import com.knms.core.compress.Luban;
import com.knms.core.im.IMHelper;
import com.knms.core.im.cache.NimUserInfoCache;
import com.knms.core.im.input.ActionsPanel;
import com.knms.core.im.input.action.PicAction;
import com.knms.core.im.media.MessageAudioControl;
import com.knms.core.im.msg.Product;
import com.knms.core.im.msg.ProductAttachment;
import com.knms.net.RxRequestApi;
import com.knms.util.DialogHelper;
import com.knms.util.RequestCode;
import com.knms.util.SPUtils;
import com.knms.util.StrHelper;
import com.knms.util.Tst;
import com.knms.view.emoji.EmoticonPickerView;
import com.knms.view.emoji.IEmoticonSelectedListener;
import com.knms.view.emoji.MoonUtil;
import com.knms.view.listview.AutoRefreshListView;
import com.knms.view.listview.MessageListView;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.media.record.AudioRecorder;
import com.netease.nimlib.sdk.media.record.IAudioRecordCallback;
import com.netease.nimlib.sdk.media.record.RecordType;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.yuyh.library.imgsel.ImgSelActivity;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


/**
 * Created by tdx on 2016/9/28.
 * IM聊天界面
 * 必须要传参:sid，表示聊天对象id
 */

public class ChatActivity extends HeadBaseActivity {
    // 聊天对象
    protected String sid; // p2p对方Account
    private QueryDirectionEnum direction = null;
    private int LOAD_MESSAGE_COUNT = 10;//每次加载消息的条数
    private IMMessage anchor;
    private boolean isFristLoad = true;
    // 从服务器拉取消息记录
    private boolean remote = false;
    private MessageListView listView;
    private ChatAdapter adapter;

    private boolean actionPanelBottomLayoutHasSetup = true;//是否为首次加载更多true是,fasle不是首次

    private EditText editTextMessage;
    private EmoticonPickerView emoticonPickerView;//表情布局
    private View switchToTextButtonInInputBar;//文本消息选择按钮
    protected View switchToAudioButtonInInputBar;// 语音消息选择按钮
    private View sendMessageButtonInInputBar;// 发送消息按钮
    private View moreFuntionButtonInInputBar;// 更多消息选择按钮
    private View actionPanelBottomLayout; // 更多布局
    private Button audioRecordBtn; // 录音按钮
    protected View audioAnimLayout; // 录音动画布局

    // 语音
    protected AudioRecorder audioMessageHelper;
    private Chronometer time;
    private TextView timerTip;
    private LinearLayout timerTipContainer;
    private boolean started = false;
    private boolean cancelled = false;
    private boolean touched = false; // 是否按着

    private Product product;
    @Override
    protected void getParmas(Intent intent) {
        sid = intent.getStringExtra("sid");
        product = (Product) intent.getSerializableExtra("prodcut");

        if(TextUtils.isEmpty(sid)) finshActivity();
        if(SPUtils.getUser() != null && sid.equals(SPUtils.getUser().sid)){
            finshActivity();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getParmas(intent);
        isFristLoad = true;
        initData();
    }
    @Override
    protected void initBar(){
        View barView = findView(R.id.view);
        if(barView == null) return;
        mImmersionBar = ImmersionBar.with(this);
        mImmersionBar.statusBarView(barView)
                .keyboardEnable(true)  //解决软键盘与底部输入框冲突问题
                .statusBarDarkFont(true, 0.1f)//状态栏字体是深色，不写默认为亮色
                .flymeOSStatusBarFontColor(R.color.status_bar_textcolor);  //修改flyme OS状态栏字体颜色;
        mImmersionBar.init();
    }
    @Override
    public void setCenterTitleView(final TextView tv_center) {
        NimUserInfoCache.getInstance().getUserInfoObserable(sid).subscribe(new Action1<NimUserInfo>() {
            @Override
            public void call(NimUserInfo nimUserInfo) {
                if(nimUserInfo != null){
                    tv_center.setText(nimUserInfo.getName());
                }
            }
        });
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_chat;
    }

    @Override
    protected void initView() {
        listView = findView(R.id.lv_chat);
        listView.setMode(AutoRefreshListView.Mode.START);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            listView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
        editTextMessage = findView(R.id.editTextMessage);
        emoticonPickerView = findView(R.id.emoticon_picker_view);
        sendMessageButtonInInputBar = findView(R.id.buttonSendMessage);
        moreFuntionButtonInInputBar = findView(R.id.buttonMoreFuntionInText);
        switchToTextButtonInInputBar = findView(R.id.buttonTextMessage);
        switchToAudioButtonInInputBar = findView(R.id.buttonAudioMessage);
        actionPanelBottomLayout = findView(R.id.actionsLayout);
        audioRecordBtn = findView(R.id.audioRecord);// 语音
        audioAnimLayout = findView(R.id.layoutPlayAudio);
        time = findView(R.id.timer);
        timerTip = findView(R.id.timer_tip);
        timerTipContainer = findView(R.id.timer_tip_container);
        initTextEdit();
    }
    @Override
    protected void initData() {
        MessageAudioControl.getInstance(this);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        initAudioRecordButton();//初始化语音模块
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();//隐藏软件盘
                emoticonPickerView.setVisibility(View.GONE);
                actionPanelBottomLayout.setVisibility(View.GONE);
                return false;
            }
        });
        findView(R.id.emoji_button).setOnClickListener(onClickListener);
        findView(R.id.buttonSendMessage).setOnClickListener(onClickListener); //发消息
        moreFuntionButtonInInputBar.setOnClickListener(onClickListener);
        switchToAudioButtonInInputBar.setOnClickListener(onClickListener);
        switchToTextButtonInInputBar.setOnClickListener(onClickListener);

        switchToTextButtonInInputBar.setVisibility(View.GONE);
        switchToAudioButtonInInputBar.setVisibility(View.VISIBLE);

        //获取历史聊天记录,设置数据
        adapter = new ChatAdapter(this,new ArrayList<IMMessage>(),null);
        listView.setAdapter(adapter);
        listView.setOnRefreshListener(new AutoRefreshListView.OnRefreshListener() {
            @Override
            public void onRefreshFromStart() {
                if (remote) {
                    loadFromRemote();
                } else {
                    loadFromLocal(QueryDirectionEnum.QUERY_OLD);
                }
            }
            @Override
            public void onRefreshFromEnd() {
            }
        });
        registerObservers(true);
        loadHistory();
        reqApi();
    }

    @Override
    protected void reqApi() {
        RxRequestApi.getInstance().getApiService().userType(sid).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<UserType>>() {
                    @Override
                    public void call(ResponseBody<UserType> body) {
                        if (body.isSuccess()) {
                            final UserType userType = body.data;
                            if (userType != null) {
                                if ("1".equals(userType.type)) {//商铺
                                    setRightMenuCallBack(new RightCallBack() {
                                        @Override
                                        public void setRightContent(TextView tv, ImageView icon) {
                                            icon.setVisibility(View.GONE);
                                            tv.setVisibility(View.VISIBLE);
                                            tv.setText("进店");
                                        }
                                        @Override
                                        public void onclick() {
                                            Map<String, Object> params = new HashMap<String, Object>();
                                            params.put("shopId", userType.shopId);
                                            startActivityAnimGeneral(ShopActivityF.class, params);
                                        }
                                    });
                                } else if ("2".equals(userType.type)) {//维修师傅
                                    setRightMenuCallBack(new RightCallBack() {
                                        @Override
                                        public void setRightContent(TextView tv, final ImageView icon) {
                                            icon.setVisibility(View.VISIBLE);
                                            tv.setVisibility(View.GONE);
                                        }
                                        @Override
                                        public void onclick() {
                                            Map<String, Object> params = new HashMap<String, Object>();
                                            params.put("repairDetaile", userType.masterInfo);
                                            startActivityAnimGeneral(RepairDetailActivity.class, params);
                                        }
                                    });
                                }
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
        RxRequestApi.getInstance().getApiService().getAutoMsg(sid).compose(this.<ResponseBody<String>>applySchedulers())
                .subscribe(new Action1<ResponseBody<String>>() {
                    @Override
                    public void call(ResponseBody<String> body) {
                        // 创建一条tip消息，并保存到本地数据库，时间设置为当前时间10秒之前
                        if(body.isSuccess() && !TextUtils.isEmpty(body.data)){
                            IMMessage msg = MessageBuilder.createTextMessage(sid, SessionTypeEnum.P2P, body.data);
                            msg.setStatus(MsgStatusEnum.success);
                            msg.setDirect(MsgDirectionEnum.In);
                            NIMClient.getService(MsgService.class).saveMessageToLocal(msg, true);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {}
                });
    }

    @Override
    public String setStatisticsTitle() {
        return "IM聊天";
    }

    boolean isShow = false;//是否已发送这个产品,false没有发送
    private void showSendProduct() {
        if(isShow || product == null) return;
        ProductAttachment attachment = new ProductAttachment();
        attachment.value = product;
        attachment.type = ProductAttachment.TYPE_SEND_STATE;//表示要显示发送的产品
        IMMessage message = MessageBuilder.createCustomMessage(
                sid, // 会话对象
                SessionTypeEnum.P2P, // 会话类型
                attachment // 自定义消息附件
        );
        isShow = true;
//        IMHelper.getInstance().sendMsg(message);
        if(message != null){
            adapter.addDataLater(message);
            listView.setSelection(adapter.getCount());
        }
    }

    private void initTextEdit() {
        editTextMessage.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editTextMessage.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    switchToTextLayout(true);
                }
                return false;
            }
        });
        editTextMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                editTextMessage.setHint("");
                toggleSendButtonEnable(editTextMessage);
            }
        });
        editTextMessage.addTextChangedListener(new TextWatcher() {
            private int start;
            private int count;
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                this.start = start;
                this.count = count;
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                toggleSendButtonEnable(editTextMessage);
                MoonUtil.replaceEmoticons(ChatActivity.this, s, start, count);
                int editEnd = editTextMessage.getSelectionEnd();
                editTextMessage.removeTextChangedListener(this);
                while (StrHelper.counterChars(s.toString()) > 5000 && editEnd > 0) {
                    s.delete(editEnd - 1, editEnd);
                    editEnd--;
                }
                editTextMessage.setSelection(editEnd);
                editTextMessage.addTextChangedListener(this);
                if(TextUtils.isEmpty(s.toString().trim())){
                    sendMessageButtonInInputBar.setVisibility(View.GONE);
                }else{
                    sendMessageButtonInInputBar.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        editTextMessage.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideKeyboard();
            }
        },100);
        IMHelper.getInstance().enableMsgNotification(sid,false);
        sensorManager.registerListener(sensorEventListener,mSensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        IMHelper.getInstance().enableMsgNotification(true);
        MessageAudioControl.getInstance(this).stopAudio();
        sensorManager.unregisterListener(sensorEventListener);
    }
    /**
     * ****************************** 语音 ***********************************
     */
    private void initAudioRecordButton() {
        audioRecordBtn.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    touched = true;
                    initAudioRecord();
                    onStartAudioRecord();
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL
                        || event.getAction() == MotionEvent.ACTION_UP) {
                    touched = false;
                    onEndAudioRecord(isCancelled(v, event));
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    touched = false;
                    cancelAudioRecord(isCancelled(v, event));
                }
                return false;
            }
        });
    }
    // 上滑取消录音判断
    private static boolean isCancelled(View view, MotionEvent event) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);

        if (event.getRawX() < location[0] || event.getRawX() > location[0] + view.getWidth()
                || event.getRawY() < location[1] - 40) {
            return true;
        }
        return false;
    }
    /**
     * 结束语音录制
     *
     * @param cancel
     */
    private void onEndAudioRecord(boolean cancel) {
        getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        audioMessageHelper.completeRecord(cancel);
        audioRecordBtn.setText("按住 说话");
        audioRecordBtn.setBackgroundResource(R.drawable.nim_message_input_edittext_box);
        stopAudioRecordAnim();
    }

    /**
     * 取消语音录制
     *
     * @param cancel
     */
    private void cancelAudioRecord(boolean cancel) {
        // reject
        if (!started) {
            return;
        }
        // no change
        if (cancelled == cancel) {
            return;
        }

        cancelled = cancel;
        updateTimerTip(cancel);
    }
    /**
     * 初始化AudioRecord
     */
    private void initAudioRecord() {
        if (audioMessageHelper == null) {
            audioMessageHelper = new AudioRecorder(this, RecordType.AAC, AudioRecorder.DEFAULT_MAX_AUDIO_RECORD_TIME_SECOND, audioRecordCallback);
        }
    }
    IAudioRecordCallback audioRecordCallback = new IAudioRecordCallback() {
        @Override
        public void onRecordReady() {
        }
        @Override
        public void onRecordStart(File audioFile, RecordType recordType) {
        }
        @Override
        public void onRecordSuccess(File audioFile, long audioLength, RecordType recordType) {
//            showSendProduct();
            if(audioLength >= 1000){
                IMMessage audioMessage = MessageBuilder.createAudioMessage(sid, SessionTypeEnum.P2P, audioFile, audioLength);
                IMHelper.getInstance().sendMsg(audioMessage);
                adapter.addDataLater(audioMessage);
                listView.setSelection(adapter.getCount() - 1);
            }
        }
        @Override
        public void onRecordFail() {
        }
        @Override
        public void onRecordCancel() {
        }
        @Override
        public void onRecordReachedMaxTime(final int maxTime) {
            stopAudioRecordAnim();
            DialogHelper.showPromptDialog(ChatActivity.this, null, "录音已达最大限制长度,是否发送?", "否", null, "发送", new DialogHelper.OnMenuClick() {
                @Override
                public void onLeftMenuClick() {
                }
                @Override
                public void onCenterMenuClick() {
                }
                @Override
                public void onRightMenuClick() {
                    audioMessageHelper.handleEndRecord(true, maxTime);
                }
            });
        }
    };
    /**
     * 开始语音录制
     */
    private void onStartAudioRecord() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        started = audioMessageHelper.startRecord();
        cancelled = false;
        if (started == false) {
            Tst.showToast("初始化录音失败");
            return;
        }
        if (!touched) {
            return;
        }
        audioRecordBtn.setText("松开  结束");
        audioRecordBtn.setBackgroundResource(R.drawable.nim_message_input_edittext_box_pressed);

        updateTimerTip(false); // 初始化语音动画状态
        playAudioRecordAnim();
    }
    /**
     * 正在进行语音录制和取消语音录制，界面展示
     *
     * @param cancel
     */
    private void updateTimerTip(boolean cancel) {
        if (cancel) {
            timerTip.setText("松开手指,取消发送");
            timerTipContainer.setBackgroundResource(R.drawable.nim_cancel_record_red_bg);
        } else {
            timerTip.setText("手指上滑,取消发送");
            timerTipContainer.setBackgroundResource(0);
        }
    }
    /**
     * 开始语音录制动画
     */
    private void playAudioRecordAnim() {
        audioAnimLayout.setVisibility(View.VISIBLE);
        time.setBase(SystemClock.elapsedRealtime());
        time.start();
    }

    /**
     * 结束语音录制动画
     */
    private void stopAudioRecordAnim() {
        audioAnimLayout.setVisibility(View.GONE);
        time.stop();
        time.setBase(SystemClock.elapsedRealtime());
    }
    @Override
    public void showKeyboard() {
        editTextMessage.postDelayed(new Runnable() {
            @Override
            public void run() {
                editTextMessage.requestFocus();
//                editTextMessage.setSelection(editTextMessage.getText().length());
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    editTextMessage.requestFocus();
                    imm.showSoftInput(editTextMessage, 0);
                }
            }
        }, 200);
        editTextMessage.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(adapter != null && listView != null){
                    listView.setSelection(adapter.getCount());
                }
            }
        },500);
    }
    /**
     * ****************************** 底部按钮 ***********************************
     */
    /**
     * 显示发送或更多
     * @param editText
     */
    private void toggleSendButtonEnable(EditText editText) {
        String textMessage = editText.getText().toString();
        if (!TextUtils.isEmpty(StrHelper.removeBlanks(textMessage)) && editText.hasFocus()) {
            moreFuntionButtonInInputBar.setVisibility(View.GONE);
            sendMessageButtonInInputBar.setVisibility(View.VISIBLE);
        } else {
            sendMessageButtonInInputBar.setVisibility(View.GONE);
            moreFuntionButtonInInputBar.setVisibility(View.VISIBLE);
        }
    }
    /**
     * 显示或隐藏表情
     */
    private void toggleEmojiLayout() {
        if(emoticonPickerView.getVisibility() == View.GONE){
            editTextMessage.setVisibility(View.VISIBLE);
            switchToAudioButtonInInputBar.setVisibility(View.VISIBLE);
            switchToTextButtonInInputBar.setVisibility(View.GONE);
            actionPanelBottomLayout.setVisibility(View.GONE);
            hideKeyboard();
            emoticonPickerView.setVisibility(View.VISIBLE);
            emoticonPickerView.setWithSticker(true);
            emoticonPickerView.show(emotionSelectListener);
            actionPanelBottomLayout.setVisibility(View.GONE);
        }else{
            emoticonPickerView.setVisibility(View.GONE);
        }
    }
    /**
     * 显示或隐藏更多
     */
    private void toggleActionPanelLayout() {
        if (actionPanelBottomLayout == null || actionPanelBottomLayout.getVisibility() == View.GONE) {
            hideKeyboard();
            emoticonPickerView.setVisibility(View.GONE);
            if(actionPanelBottomLayoutHasSetup){
                List<BaseAction> actions = new ArrayList<>();
                PicAction action = new PicAction();
                action.setActivity(this);
                actions.add(action);
                ActionsPanel.init(actionPanelBottomLayout, actions);
            }
            actionPanelBottomLayout.setVisibility(View.VISIBLE);
            actionPanelBottomLayoutHasSetup = false;
        } else {
            actionPanelBottomLayout.setVisibility(View.GONE);
        }
    }
    // 切换成音频，收起键盘，按钮切换成键盘
    private void switchToAudioLayout() {
        editTextMessage.setVisibility(View.GONE);
        audioRecordBtn.setVisibility(View.VISIBLE);
        hideKeyboard();
        emoticonPickerView.setVisibility(View.GONE);
        actionPanelBottomLayout.setVisibility(View.GONE);

        switchToAudioButtonInInputBar.setVisibility(View.GONE);
        switchToTextButtonInInputBar.setVisibility(View.VISIBLE);
    }
    // 点击edittext，切换键盘和更多布局
    private void switchToTextLayout(boolean needShowInput) {
        emoticonPickerView.setVisibility(View.GONE);//隐藏表情
        editTextMessage.setVisibility(View.VISIBLE);
        switchToTextButtonInInputBar.setVisibility(View.GONE);
        switchToAudioButtonInInputBar.setVisibility(View.VISIBLE);
        actionPanelBottomLayout.setVisibility(View.GONE);
        if (needShowInput) {
            showKeyboard();
        }else{
            hideKeyboard();
        }
    }
    /**
     * ****************************** 注册&销毁 ***********************************
     */
    private void registerObservers(boolean register) {
        MsgServiceObserve service = NIMClient.getService(MsgServiceObserve.class);
        service.observeReceiveMessage(incomingMessageObserver, register);//注册接收消息对象
        service.observeMsgStatus(messageStatusObserver,register);//注册发送消息状态对象
    }
    /**
     * ****************************** 数据源获取 ***********************************
     */
    private void loadHistory(){
        if(remote){
            loadFromRemote();
        }else{
            loadFromLocal(QueryDirectionEnum.QUERY_OLD);
        }
    }

    private void loadFromRemote() {
        this.direction = QueryDirectionEnum.QUERY_OLD;
        NIMClient.getService(MsgService.class).pullMessageHistory(anchor(), LOAD_MESSAGE_COUNT, true)
                .setCallback(callback);
    }
    private void loadFromLocal(QueryDirectionEnum direction) {
        this.direction = direction;
        listView.onRefreshStart(direction == QueryDirectionEnum.QUERY_NEW ? AutoRefreshListView.Mode.END : AutoRefreshListView.Mode.START);
        NIMClient.getService(MsgService.class).queryMessageListEx(anchor(), direction, LOAD_MESSAGE_COUNT, true)
                .setCallback(callback);
    }
    private IMMessage anchor() {
        if (adapter.getData().size() == 0) {
            return anchor == null ? MessageBuilder.createEmptyMessage(sid, SessionTypeEnum.P2P, 0) : anchor;
        } else {
            int index = (direction == QueryDirectionEnum.QUERY_NEW ? adapter.getData().size() - 1 : 0);
            return adapter.getData().get(index);
        }
    }

    private RequestCallback<List<IMMessage>> callback = new RequestCallbackWrapper<List<IMMessage>>() {
        @Override
        public void onResult(int code, List<IMMessage> messages, Throwable exception) {
            if (messages != null) {
//                Collections.reverse(messages);
                if(isFristLoad){
                    adapter.setNewData(messages);
                    listView.setSelection(adapter.getCount());
                    sendMsgReceipt();
                }else {
                    adapter.addDataAbove(messages);
                    listView.setSelection(messages.size());
                }
            }else{
//                listView.setMode(AutoRefreshListView.Mode.END);
            }
            listView.onRefreshComplete();
            if(isFristLoad) showSendProduct();
            isFristLoad = false;
        }
    };
    /**
     * 监听消息发送状态的变化通知
     */
    Observer<IMMessage> messageStatusObserver = new Observer<IMMessage>() {
        @Override
        public void onEvent(IMMessage message) {
            // 参数为有状态发生改变的消息对象，其 msgStatus 和 attachStatus 均为最新状态。
            // 发送消息和接收消息的状态监听均可以通过此接口完成。
            adapter.changeItem(message);
            listView.setSelection(adapter.getCount());
        }
    };
    /**
     * 消息接收观察者
     */
    Observer<List<IMMessage>> incomingMessageObserver = new Observer<List<IMMessage>>() {
        @Override
        public void onEvent(List<IMMessage> messages) {
            if (messages == null || messages.isEmpty()) {
                return;
            }
            for (IMMessage message : messages) {
                if(message.getSessionId().equals(sid)){
                    if(message.getMsgType() == MsgTypeEnum.text){
                        adapter.addDataLater(messages);
                    }else if(message.getMsgType() == MsgTypeEnum.image){
                        adapter.changeItem(message);
                    }else{
                        adapter.changeItem(message);
                    }

                    listView.setSelection(adapter.getCount());
                }
            }
            sendMsgReceipt(); //新消息已读清零
        }
    };


    private void sendMsgReceipt() {
            NIMClient.getService(MsgService.class).clearUnreadCount(sid, SessionTypeEnum.P2P);
    }
    /*****************点击事件*****************/
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id){
                case R.id.emoji_button://表情包视图
                    toggleEmojiLayout();
                    break;
                case R.id.buttonMoreFuntionInText://发送更多
                    toggleActionPanelLayout();
                    break;
                case R.id.buttonSendMessage://发消息
                    showSendProduct();
                    String text = editTextMessage.getText().toString();
                    IMMessage newMsg = IMHelper.getInstance().sendTextMsg(sid,text);
                    if(newMsg != null){
                        adapter.addDataLater(newMsg);
                        listView.setSelection(adapter.getCount() - 1);
                    }
                    editTextMessage.setText("");
                    break;
                case R.id.buttonAudioMessage:
                    switchToAudioLayout();
                    break;
                case R.id.buttonTextMessage:
                    switchToTextLayout(true);
                    break;
            }
        }
    };
    /**
     * *************** IEmojiSelectedListener ***************
     */
    IEmoticonSelectedListener emotionSelectListener = new IEmoticonSelectedListener() {
        @Override
        public void onEmojiSelected(String key) {
            Editable mEditable = editTextMessage.getText();
            if (key.equals("/DEL")) {
                editTextMessage.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            } else {
                int start = editTextMessage.getSelectionStart();
                int end = editTextMessage.getSelectionEnd();
                start = (start < 0 ? 0 : start);
                end = (start < 0 ? 0 : end);
                mEditable.replace(start, end, key);
            }
        }
        @Override
        public void onStickerSelected(String categoryName, String stickerName) {
        }
    };
    /**
     * *************** 距离传感 ***************
     */
    SensorManager sensorManager;
    Sensor mSensor;
    SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float range = event.values[0];
            if (range == mSensor.getMaximumRange()) {//正常模式
                MessageAudioControl.getInstance(ChatActivity.this).setEarPhoneModeEnable(false);
            } else {//听筒模式
                MessageAudioControl.getInstance(ChatActivity.this).setEarPhoneModeEnable(true);
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    /*************************onActivityResult**************************/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)  return;
        switch (requestCode){
            case RequestCode.PICK_IMAGE:
//                showSendProduct();
                List<String> pathList = data.getStringArrayListExtra(ImgSelActivity.RESULT_LOACAL_PATH);
                Observable.from(pathList).map(new Func1<String, File>() {
                    @Override
                    public File call(String path) {
                        File file = new File(path);
                        return Luban.with(ChatActivity.this).load(file).get();
                    }
                }).compose(this.<File>applySchedulers())
                .onErrorReturn(new Func1<Throwable, File>() {
                    @Override
                    public File call(Throwable throwable) {
                        return null;
                    }
                }).subscribe(new Action1<File>() {
                    @Override
                    public void call(File file) {
                        if(file != null && file.length() > 0){
                            IMMessage newMsg = IMHelper.getInstance().sendImageMsg(sid,file.getAbsolutePath());
                            if(newMsg != null){
                                adapter.addDataLater(newMsg);
                                listView.setSelection(adapter.getCount() - 1);
                            }
                        }
                    }
                });
                break;
        }
    }

    @Override
    protected void onDestroy() {
        //释放资源
        registerObservers(false);
        MessageAudioControl.getInstance(this).onDestroy(); // 界面返回，停止语音播放
        super.onDestroy();
    }
}
