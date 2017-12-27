package com.knms.bean.other;

import com.google.gson.annotations.SerializedName;
import com.knms.adapter.baserecycler.entity.MultiItemEntity;
import com.knms.android.R;

import java.io.Serializable;

/**
 * Created by tdx on 2016/9/6.
 */
public class Pic extends MultiItemEntity implements Serializable {
    public static final int IMG_NORMAR = 0;//固定图片类型-相机图片
    public static final int IMG_CAMERA = 1;//固定图片类型-相册图片
    public static final int IMG_ALBUM = 2;//图片不固定类型
    private static final long serialVersionUID = -6201675444803547584L;

    @SerializedName("imageId")
    public String id;
    @SerializedName(value = "imageUrl",alternate = {"imgUrl","imageName"})
    public String url;
    @SerializedName(value = "imageSeq",alternate = {"imageSorting","imageseq"})
    public String seq;
    public boolean isSelect;//是否被选中
    public String order;//选中的顺序


    @Override
    public String toString() {
        return "id:" + this.id + ";url:" + this.url;
    }
    @Override
    public boolean equals(Object arg0) {
        if(arg0 instanceof Pic){
            if(this.toString().equals(arg0.toString())){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }
}
