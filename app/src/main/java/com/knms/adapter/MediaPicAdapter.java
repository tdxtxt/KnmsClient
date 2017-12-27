package com.knms.adapter;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.knms.adapter.baserecycler.BaseMultiItemQuickAdapter;
import com.knms.adapter.baserecycler.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.other.Pic;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.ScreenUtil;
import java.util.ArrayList;

import static com.knms.bean.other.Pic.IMG_ALBUM;
import static com.knms.bean.other.Pic.IMG_CAMERA;
import static com.knms.bean.other.Pic.IMG_NORMAR;

/**
 * Created by tdx on 2016/9/13.
 */
public class MediaPicAdapter extends BaseMultiItemQuickAdapter<Pic> {
    public static Pic cameraPic,albumPic;
    int picWidth;
    int margin;
    int column = 5;
    private Context mContext;
    public MediaPicAdapter(Context context) {
        super(new ArrayList<Pic>(0));
        mContext = context;
        addItemType(IMG_ALBUM, R.layout.item_pic_one);
        addItemType(IMG_CAMERA, R.layout.item_pic_one);
        addItemType(Pic.IMG_NORMAR, R.layout.item_pic);

        margin = LocalDisplay.dp2px(10);
        picWidth = ((ScreenUtil.getScreenWidth() - margin) / column) - margin;
    }
    public void setCameraMode(){
        if(getData() == null) mData = new ArrayList();
        if(getData().size() > 0){
            Pic one = (Pic)getData().get(0);
            if(IMG_NORMAR == one.getItemType()){
                this.add(0,getInstanceCameraPic());
            }else{
                if(IMG_CAMERA != one.getItemType()){
                    this.getData().set(0,getInstanceCameraPic());
                }
            }
        }else{
            this.add(0,getInstanceCameraPic());
        }
        notifyItemChanged(0);
    }
    public void setAlbumMode(){
        if(getData() == null) mData = new ArrayList();
        if(getData().size() > 0){
            Pic one = (Pic)getData().get(0);
            if(IMG_NORMAR == one.getItemType()){
                this.add(0,getInstanceAlbumPic());
            }else{
                if(IMG_ALBUM != one.getItemType()){
                    this.getData().set(0,getInstanceAlbumPic());
                }
            }
        }else{
            this.add(0,getInstanceAlbumPic());
        }
        notifyItemChanged(0);
    }

    @Override
    protected void convert(BaseViewHolder helper, Pic item) {
        switch (helper.getItemViewType()) {
            case IMG_ALBUM:
            case IMG_CAMERA:
                LinearLayout ll = helper.getView(R.id.ll_one);
                ImageView iv_title = helper.getView(R.id.iv_title);
                TextView tv_title = helper.getView(R.id.tv_title);

                RelativeLayout.LayoutParams rp = (RelativeLayout.LayoutParams)ll.getLayoutParams();
                rp.height = picWidth;
                rp.width = picWidth;
                rp.setMargins(margin,margin,0,0);
                ll.setLayoutParams(rp);

                ImageLoadHelper.getInstance().displayImageOrigin(mContext,item.url, iv_title);
                if(IMG_ALBUM == item.getItemType()){
                    tv_title.setText("相册");
                }else if(IMG_CAMERA == item.getItemType()){
                    tv_title.setText("拍照");
                }
                break;
            case Pic.IMG_NORMAR:
                default:
                    ImageView iv_img = helper.getView(R.id.iv_img);
                    TextView iv_select = helper.getView(R.id.iv_select);

                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) iv_img.getLayoutParams();
                    lp.height = picWidth;
                    lp.width = picWidth;
                    lp.setMargins(margin,margin,0,0);
                    iv_img.setLayoutParams(lp);
                    iv_img.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    ImageLoadHelper.getInstance().displayImageRounde(mContext,item.url, iv_img);
                    if (item.isSelect) {
                        iv_select.setVisibility(View.VISIBLE);
                        iv_select.setText(item.order);
                    } else {
                        iv_select.setText("");
                        iv_select.setVisibility(View.GONE);
                    }
                break;
        }
    }
    public static Pic getInstanceCameraPic(){
        if(cameraPic == null){
            cameraPic = new Pic();
            cameraPic.url = "drawable://" + R.drawable.btn_camera_turn_n;
            cameraPic.setItemType(IMG_CAMERA);
        }
        return cameraPic;
    }
    public static Pic getInstanceAlbumPic(){
        if(albumPic == null){
            albumPic = new Pic();
            albumPic.url = "drawable://" + R.drawable.icon_xc;
            albumPic.setItemType(IMG_ALBUM);
        }
        return albumPic;
    }
}
