package cn.shareuzi.interfaces;
import cn.shareuzi.bean.ShareEntity;

/**
 * Created by zhanglifeng
 */
public interface IShareBase {
    /**
     * @param data {@link ShareEntity}
     * @param listener {@link OnShareListener}
     */
    void share(ShareEntity data, OnShareListener listener);
}
