package cn.shareuzi.bean;

import android.graphics.Bitmap;
import android.support.annotation.DrawableRes;
import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by zhanglifeng
 */
public class ShareEntity implements Serializable {

    public String title;
    public String remark;
    public String url;
    public String img;

    private int drawableId;
    private Bitmap bitmap;

    public ShareEntity(){}

    public ShareEntity(String title, String content) {
        this(title, content, null);
    }

    public ShareEntity(String title, String content, String url) {
        this(title, content, url, null);
    }

    public ShareEntity(String title, String content, String url, String imgUrl) {
        this.title = title;
        this.remark = content;
        this.url = url;
        this.img = imgUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImg() {
        return TextUtils.isEmpty(img)?"https://img.kebuyer.com/img/keicon.png":img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void setDrawableId(@DrawableRes int drawableId) {
        this.drawableId = drawableId;
    }

    public int getDrawableId() {
        return drawableId;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

   /* @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ShareEntity> CREATOR = new Creator<ShareEntity>() {
        @Override
        public ShareEntity createFromParcel(Parcel in) {
            return new ShareEntity(in);
        }

        @Override
        public ShareEntity[] newArray(int size) {
            return new ShareEntity[size];
        }
    };

    protected ShareEntity(Parcel in) {
        title = in.readString();
        remark = in.readString();
        url = in.readString();
        img = in.readString();
        drawableId = in.readInt();
        bitmap = in.readParcelable(Bitmap.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(remark);
        dest.writeString(url);
        dest.writeString(img);
        dest.writeInt(drawableId);
        dest.writeParcelable(bitmap, flags);
    }*/
}
