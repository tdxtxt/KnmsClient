package com.knms.bean.sku.base;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.knms.util.ToolsHelper;

import java.io.Serializable;
import java.util.List;

/**
 * 类描述：
 * 创建人：tdx
 * 创建时间：2017/07/22
 * 传参：
 * 返回:
 */
public class SkuModel implements Serializable{
    private static final long serialVersionUID = -6737870038485317685L;
    /**库存商品id**/
    @SerializedName("commoditySpecificationId")
    public String skuId;
    /**规格商品配图**/
    @SerializedName("specificationImg")
    public String imageUrl;
    /**商品单个展示价格**/
    @SerializedName("specificationShowMoney")
    public String orprice;
    /**商品实际单个卖价(交易价)**/
    @SerializedName("specificationMoney")
    public String price;
    @SerializedName("specificationTotalQuantity")
    public long stock;//库存
    @SerializedName("specifications")
    public List<Vaule> vaules;//属性

    public SkuModel(){
    }
    public SkuModel(long stock, String imageUrl, String price){
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.price = price;
    }
    /**
     * 获取vaules 排序 之后的组合
     * @return
     */
    public String getKey(){
        String key = "";
        if(vaules != null){
            ToolsHelper.getInstance().sort(vaules,"id",true);
            key = TextUtils.join(";",vaules);
        }
        return key;
    }
    public String getAttrs(){
        StringBuffer stringBuffer = new StringBuffer();
        if(vaules != null){
            for (int i=0;i<vaules.size();i++){
                stringBuffer.append(vaules.get(i).getContent());
                if (i != vaules.size() - 1) {//不是最后一个
                    stringBuffer.append("|");
                }
            }
        }
        return stringBuffer.toString();
    }
}
