package com.knms.oncall;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.knms.activity.CommWebViewActivity;
import com.knms.bean.product.Ad;
import com.knms.util.CommonUtils;

/**
 * Created by Administrator on 2016/8/25.
 */
public class BannerOnclick {
    /**
     * 0是app,1是h5
     * @param context
     * @param ad
     */
    public static void advertisementClick(Context context,Ad ad){
        if(ad == null) return;
        if(ad.type == 0){
            if(!TextUtils.isEmpty(ad.url)) CommonUtils.startActivity(context, Uri.parse(ad.url));
        }else{
            Intent intent=new Intent(context, CommWebViewActivity.class);
            intent.putExtra("url",ad.url);
            context.startActivity(intent);
        }
    }
}
