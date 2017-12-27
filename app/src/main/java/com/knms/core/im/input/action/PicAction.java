package com.knms.core.im.input.action;

import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;
import com.knms.android.R;
import com.knms.bean.im.BaseAction;
import com.knms.util.ImageLoadHelper;
import com.knms.util.RequestCode;
import com.yuyh.library.imgsel.ImageLoader;
import com.yuyh.library.imgsel.ImgSelActivity;
import com.yuyh.library.imgsel.ImgSelConfig;

/**
 * Created by Administrator on 2016/10/9.
 */

public class PicAction extends BaseAction {
    private static final int PICK_IMAGE_COUNT = 9;
    /**
     * 构造函数
     */
    public PicAction() {
        super(R.drawable.nim_message_plus_photo_normal, R.string.image);
    }
    // 自定义图片加载器
    private ImageLoader loader = new ImageLoader() {
        @Override
        public void displayImage(Context context, String path, ImageView imageView) {
            ImageLoadHelper.getInstance().displayImageLocal(context,path,imageView);
        }
    };
    @Override
    public void onClick() {
        // 自由配置选项
        ImgSelConfig config = new ImgSelConfig.Builder(loader)
                // 是否多选
                .multiSelect(true)
                .backResId(R.drawable.sign_63)
                .titleColor(Color.WHITE)
                .needCrop(false)
                // 第一个是否显示相机
                .needCamera(true)
                // 最大选择图片数量
                .maxNum(9)
                .build();
        // 跳转到图片选择器
        ImgSelActivity.startActivity(getActivity(), config, RequestCode.PICK_IMAGE);
    }
}
