package com.knms.bean.other;

import java.io.Serializable;
import java.util.List;

/**
 * Created by tdx on 2016/9/20.
 */
public class BBPrice implements Serializable {
    public List<Pic> pics;//bb价选中的图片
    public List<Label> labels;//选中的标签
    public String desc;//描述
    public List<Pic> fromAllPics;//可供选择的图片
}
