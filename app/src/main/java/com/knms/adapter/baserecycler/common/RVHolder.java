package com.knms.adapter.baserecycler.common;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Administrator on 2016/8/26.
 */
public class RVHolder extends RecyclerView.ViewHolder{
    private ViewHolder viewHolder;

    public RVHolder(View itemView) {
        super(itemView);
        viewHolder= ViewHolder.getViewHolder(itemView);
    }
    public ViewHolder getViewHolder() {
        return viewHolder;
    }
}
