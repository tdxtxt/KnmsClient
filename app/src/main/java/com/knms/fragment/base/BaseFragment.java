package com.knms.fragment.base;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.knms.activity.base.BaseFragmentActivity;
import com.knms.view.progress.CircleProgressDialog;

/**
 * Created by tdx on 2016/8/29.
 */
public class BaseFragment extends Fragment{
    public boolean hasData = false;//是否有网络数据，特定地方使用，比如我的收藏
    public BaseFragmentActivity mActivity;
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if (context instanceof BaseFragmentActivity){
            mActivity = (BaseFragmentActivity) context;
        }
    }
    CircleProgressDialog circleProgressDialog;
    public void showProgress(){
        if(mActivity == null) return;
        if(mActivity.isFinishing() || mActivity.isDestroyed()) return;
        if(circleProgressDialog == null) circleProgressDialog = new CircleProgressDialog(this.mActivity);
        if(circleProgressDialog.isShowing()) circleProgressDialog.dismiss();

        circleProgressDialog.showDialog();
    }
    public void hideProgress(){
        if(mActivity == null) return;
        if(mActivity.isFinishing() || mActivity.isDestroyed()) return;
        if(circleProgressDialog != null) circleProgressDialog.dismiss();
    }
    public BaseFragmentActivity getmActivity() {
        return mActivity;
    }
    public void reqApi(){}
    public String getTitle(){return "";}
}
