package com.knms.activity.details;

import android.view.View;
import android.widget.ImageView;

import com.knms.activity.details.base.CannotBuyBaseDetailsActivity;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.classification.ClassifyDetail;
import com.knms.bean.user.User;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.net.RxRequestApi;
import com.knms.util.LocalDisplay;
import com.knms.view.GuideView;

import rx.Observable;

/**
 * 类描述：普通商品详情-可比比货
 * 创建人：Administrator
 * 创建时间：2017/9/1 11:34
 * 传参：
 * 返回:
 */
public class ProductDetailsOrdinaryActivity extends CannotBuyBaseDetailsActivity{
    @Override
    public String getChatText() {
        return "去询价 去砍价";
    }

    @Override
    public String getProductType() {
        return "5";
    }
    @Override
    public Observable<ResponseBody<ClassifyDetail>> getDetailApi() {
        return RxRequestApi.getInstance().getApiService().getGoodsDetail(goodsId);
    }

    @Override
    protected void showGriade(View view) {
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.bg_griade_product);
        new GuideView.Builder()
                .setTargetView(view)    // 必须调用，设置需要Guide的View
                .setCustomGuideView(imageView)  // 必须调用，设置GuideView，可以使任意View的实例，比如ImageView 或者TextView
                .setDirction(GuideView.Direction.TOP)   // 设置GuideView 相对于TargetView的位置，有八种，不设置则默认在屏幕左上角,其余的可以显示在右上，右下等等
                .setShape(GuideView.MyShape.ELLIPSE)   // 设置显示形状，支持圆形，椭圆，矩形三种样式，矩形可以是圆角矩形，
                .setOnclickExit(true)   // 设置点击消失，可以传入一个Callback，执行被点击后的操作
                .setRadius(LocalDisplay.dp2px(30))
//                .setCenter(300, 300)    // 设置圆心，默认是targetView的中心
                .setOffset(LocalDisplay.dp2px(40), -10)     // 设置偏移，一般用于微调GuideView的位置
                .showOnce()             // 设置首次显示，设置后，显示一次后，不再显示
                .build(this)                // 必须调用，Buider模式，返回GuideView实例
                .show();                // 必须调用，显示GuideView
    }

    @Subscribe(tags = {@Tag(BusAction.ACTION_LOGIN)})
    public void parityExists(User user){
        checkBbprice();
    }
}
