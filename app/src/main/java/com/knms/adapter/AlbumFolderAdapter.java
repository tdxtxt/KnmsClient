package com.knms.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;

import com.knms.adapter.baserecycler.BaseQuickAdapter;
import com.knms.adapter.baserecycler.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.other.AlbumFolder;
import com.knms.util.ImageLoadHelper;

import java.util.List;

/**
 * Created by Administrator on 2016/11/24.
 */

public class AlbumFolderAdapter extends BaseQuickAdapter<AlbumFolder>{
    private Context mContext;
    public AlbumFolderAdapter(Context context,List<AlbumFolder> data) {
        super(R.layout.item_album, data);
        mContext = context;
    }
    @Override
    protected void convert(BaseViewHolder helper, AlbumFolder item) {
        helper.setText(R.id.tv_name,item.name);
        helper.setText(R.id.tv_count,item.pics != null ? item.pics.size() - 1 + "                                                                                                                                                                                                                                                                                                                                                                   " : "0");
        if(item.cover != null && !TextUtils.isEmpty(item.cover.url)){
            ImageView iv_image = helper.getView(R.id.iv_image);
            ImageLoadHelper.getInstance().displayImage(mContext,item.cover.url,iv_image);
        }
    }
}
