package com.knms.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bm.library.PhotoView;
import com.knms.adapter.base.RecyclingPagerAdapter;
import com.knms.bean.other.Pic;
import com.knms.util.ImageLoadHelper;

import java.util.List;


/**
 * Created by Administrator on 2016/8/25.
 */
public class DecorationStyleAdapter extends RecyclingPagerAdapter<Pic> {
    private OnclickListener onclickListener;
    private OnLongclickListener onLongclickListener;
    private Context mContext;

    public void setOnclickListener(OnclickListener onclickListener) {
        this.onclickListener = onclickListener;
    }
    public void setOnLongclickListener(OnLongclickListener onLongclickListener) {
        this.onLongclickListener = onLongclickListener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup container) {
//        if(convertView == null){
//            convertView = new PhotoView(container.getContext());
//        }
        PhotoView view = new PhotoView(container.getContext());
        view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        view.enable();
        if(this.datas.size() <= position){
            return convertView;
        }
        ImageLoadHelper.getInstance().displayImageOrigin(mContext,this.datas.get(position).url,view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onclickListener != null){
                    onclickListener.onClick(position);
                }
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(onLongclickListener!=null)
                    onLongclickListener.onLoingClick(v);
                return false;
            }
        });
        return view;
    }

    @Override
    public int getCount() {
        return this.datas == null ? 0 : this.datas.size();
    }

    public interface OnclickListener{
        public void onClick(int position);
    }
    public interface OnLongclickListener{
        public void onLoingClick(View view);
    }
    public DecorationStyleAdapter(Context context,List<Pic> imgs){
        this.datas = imgs;
        mContext = context;
    }

}
