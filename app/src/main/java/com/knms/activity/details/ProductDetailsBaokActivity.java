package com.knms.activity.details;

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

import rx.Observable;

/**
 * 类描述：老版本-爆款商品详情
 * 创建人：Administrator
 * 创建时间：2017/9/1 11:37
 * 传参：
 * 返回:
 */
public class ProductDetailsBaokActivity extends CannotBuyBaseDetailsActivity{
    @Override
    public String getChatText() {
        return "联系商家";
    }

    @Override
    public String getProductType() {
        return "4";
    }

    @Override
    public Observable<ResponseBody<ClassifyDetail>> getDetailApi() {
        return RxRequestApi.getInstance().getApiService().getActGoodsDetail(goodsId);
    }
}
