package com.knms.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.knms.activity.details.DecorationStyleDetailsActivity;
import com.knms.adapter.DecorationStyleAdapter;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.style.StyleDetails;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.RxBus;
import com.knms.fragment.base.BaseFragment;
import com.knms.fragment.base.LazyLoadFragment;
import com.knms.net.RxRequestApi;
import com.knms.util.BitmapHelper;
import com.knms.util.DialogHelper;
import com.knms.util.Tst;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


/**
 * Created by Administrator on 2016/10/25.
 * 家装风格详情
 */

public class DecorationStyleDetailsFragment extends LazyLoadFragment {
    public ViewPager auto_vp;
    public DecorationStyleAdapter adapter_auto;

    private String id;
    private String position;
    DecorationStyleDetailsActivity activity;
    public static BaseFragment newInstance(String position,String id) {
        BaseFragment fragment = new DecorationStyleDetailsFragment();
        Bundle args = new Bundle();
        args.putString("id", id);
        args.putString("position",position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            id = args.getString("id");
            position = args.getString("position");
        }
    }

    protected void initView() {
        auto_vp = findViewById(R.id.viewpager);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getmActivity() != null && getmActivity() instanceof DecorationStyleDetailsActivity) {
            activity = (DecorationStyleDetailsActivity) getmActivity();
        }
    }
    @Override
    protected int setContentView() {
        return R.layout.activity_decoration_style_details;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        initView();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if(activity != null && activity.details.get(id) != null){
            updateViewnew(activity.details.get(id));
        }
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void lazyLoad() {
        reqApi();
    }
    @Override
    public void reqApi() {
        if(activity != null && activity.details.get(id) != null){
//            updateViewnew(activity.details.get(id));把这部分代码移动到onViewCreated方法中比较好
        }else {
            RxRequestApi.getInstance().getApiService().getFurnitureStyleDeails(id)
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<ResponseBody<StyleDetails>>() {
                        @Override
                        public void call(ResponseBody<StyleDetails> body) {
                            hideProgress();
                            if (body.isSuccess()) {
                                updateViewnew(body.data);
                                if(activity != null) activity.details.put(id,body.data);
                            } else {
                                Tst.showToast(body.desc);
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            hideProgress();
                            Tst.showToast(throwable.toString());
                        }
                    });
        }
    }
    private void updateViewnew(StyleDetails data){
        if (data.imglist != null && data.imglist.size() > 0) {
            adapter_auto = new DecorationStyleAdapter(getContext(),data.imglist);
            adapter_auto.setOnclickListener(new DecorationStyleAdapter.OnclickListener() {
                @Override
                public void onClick(int position) {
                    RxBus.get().post(BusAction.HIDDED_TOP_LAYOUT,"onclick");
                }
            });
            adapter_auto.setOnLongclickListener(new DecorationStyleAdapter.OnLongclickListener() {
                @Override
                public void onLoingClick(View view) {
                    showDialog(view);
                }
            });
            auto_vp.setAdapter(adapter_auto);
            if(position.equals(activity.getCurrentPage() + "")){
                activity.setDetail(data);
            }
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){// 横屏
            if(adapter_auto != null) auto_vp.postDelayed(new Runnable() {
                @Override
                public void run() {
                    adapter_auto.notifyDataSetChanged();
                }
            },400);//这里的延迟是解决横竖屏切换后图片不居中
        } else if(this.getResources().getConfiguration().orientation ==Configuration.ORIENTATION_PORTRAIT) {// 竖屏
            if(adapter_auto != null) auto_vp.postDelayed(new Runnable() {
                @Override
                public void run() {
                    adapter_auto.notifyDataSetChanged();
                }
            },400);
        }
        super.onConfigurationChanged(newConfig);
    }
    private void showDialog(final View imageview) {
        DialogHelper.showBottomDialog(getActivity(), R.layout.dialog_decoration_style, new DialogHelper.OnEventListener<Dialog>() {
            @Override
            public void eventListener(View parentView, final Dialog window) {
                TextView tvCancel = (TextView) parentView.findViewById(R.id.cancel_sava);
                tvCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        window.dismiss();
                    }
                });
                TextView tvSava = (TextView) parentView.findViewById(R.id.sava_style_img);
                tvSava.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String path = BitmapHelper.saveImageToGallery(drawableToBitmap(((ImageView) imageview).getDrawable()));
                        Tst.showToast(TextUtils.isEmpty(path) ? "保存失败" : (path.contains("保存失败") ? path : "保存至" + path));
                        window.dismiss();
                    }
                });
            }
        });
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ?
                        Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }
    @Override
    public void onDestroy() {
        if(auto_vp != null) auto_vp.clearOnPageChangeListeners();
        super.onDestroy();
    }
}
