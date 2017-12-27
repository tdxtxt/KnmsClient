package com.knms.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.knms.android.R;
import com.knms.bean.repair.Repair;
import com.knms.util.DialogHelper;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;

import java.util.List;

/**
 * Created by Administrator on 2016/10/17.
 */
public class MaintainListAdapter extends BaseQuickAdapter<Repair,BaseViewHolder> {
    private Context mContext;
    public MaintainListAdapter(Context context, List<Repair> data) {
        super(R.layout.item_maintain, data);
        mContext = context;
    }
    @Override
    protected void convert(BaseViewHolder helper, final Repair item) {
        helper.setText(R.id.maintain_name, item.usnickname);
        helper.setText(R.id.maremark, item.maremark);
        helper.setText(R.id.maexperience, "从业经验：" + item.maexperience + "年");
        ImageLoadHelper.getInstance().displayImageHead(mContext,item.usphoto, (ImageView) helper.getView(R.id.maintain_logo), LocalDisplay.dp2px(50), LocalDisplay.dp2px(50));
        helper.setOnClickListener(R.id.maintain_dial, new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                new DialogHelper().showPromptDialog((Activity) mContext, "", "拨打"+item.usphone, "取消", "", "确定", new DialogHelper.OnMenuClick() {
                    @Override
                    public void onLeftMenuClick() {

                    }

                    @Override
                    public void onCenterMenuClick() {

                    }

                    @Override
                    public void onRightMenuClick() {
                        Intent mIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
                                + item.usphone));
                        mContext.startActivity(mIntent);
                    }
                });
            }
        });
    }
}
