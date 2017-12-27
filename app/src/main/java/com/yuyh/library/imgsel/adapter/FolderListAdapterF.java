package com.yuyh.library.imgsel.adapter;

import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.android.R;
import com.yuyh.library.imgsel.bean.Folder;

import java.util.List;

import static com.yuyh.library.imgsel.common.Constant.config;

/**
 * 类描述：
 * 创建人：Administrator
 * 创建时间：2017/11/15 15:07
 * 传参：
 * 返回:
 */
public class FolderListAdapterF extends BaseQuickAdapter<Folder,BaseViewHolder>{
    private int selected = -1;//选择的item
    public FolderListAdapterF(List<Folder> data) {
        super(R.layout.item_img_sel_folder, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Folder item) {
        helper.setText(R.id.tvFolderName, item.name).setText(R.id.tvImageNum, "共" + item.images.size() + "张");
        ImageView ivFolder = helper.getView(R.id.ivFolder);
        if (item.images.size() > 0) {
            config.loader.displayImage(helper.getConvertView().getContext(), item.cover.path, ivFolder);
        }
        if(helper.getLayoutPosition() == selected){
            helper.setVisible(R.id.indicator, true);
        }else{
            helper.setVisible(R.id.indicator, false);
        }
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }
}
