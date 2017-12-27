package com.knms.core.sku;

import android.text.TextUtils;

import com.knms.bean.sku.base.SkuModel;
import com.knms.bean.sku.base.Spec;
import com.knms.bean.sku.base.Vaule;
import com.knms.other.SkuDataUI;
import com.knms.other.SkuOnSubscribe;
import com.knms.util.ToolsHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rx.Observable;


/**
 * Created by Administrator on 2017/7/25.
 */

public class Sku {
    public static Observable<SkuDataUI> getSkuData(List<SkuModel> data){
        return Observable.unsafeCreate(new SkuOnSubscribe(data));
    }
    /**
     * 提取库存中 规格-属性
     * @param data
     * @return
     */
    public static List<Spec> getSpecs(List<? extends SkuModel> data){
        List<Spec> result = new ArrayList<>();
        Map<String,Set<Vaule>> specs = new HashMap<>();
        for (SkuModel mode : data) {
            if(mode.vaules == null) continue;
            for (Vaule vaule : mode.vaules) {
                Set<Vaule> mapSet = specs.get(vaule.specName);
                if(mapSet == null) mapSet = new HashSet<>();
                mapSet.add(vaule);
                specs.put(vaule.specName,mapSet);
            }
        }

        for (String specName : specs.keySet()) {
            Spec spec = new Spec();
            spec.name = specName;
            List<Vaule> specVaules = new ArrayList<>();
            Set<Vaule> vaules = specs.get(specName);
            int maxSort = 1;
            for (Vaule vaule : vaules) {
                specVaules.add(vaule);
                maxSort = Math.max(vaule.sort,maxSort);
            }
            spec.specSort = maxSort;
            ToolsHelper.getInstance().sort(specVaules,"sort");
            spec.vaules = specVaules;
            result.add(spec);
        }

        ToolsHelper.getInstance().sort(result,"specSort");
        int id = 0;
        for (int i = 0;i < result.size(); i++){
            result.get(i).specSort = result.size() - i;
            if(result.get(i).vaules != null && result.get(i).vaules.size() > 0){
                for (Vaule vaule : result.get(i).vaules) {
                    vaule.id = id;
                    id ++;
                }
            }
        }

        for (SkuModel mode : data) {
            if(mode.vaules == null) continue;
            for (Vaule vaule : mode.vaules) {
                Set<Vaule> mapSet = specs.get(vaule.specName);
                for (Vaule mapV : mapSet) {
                    if(vaule.equals(mapV)){
                        vaule.id = mapV.id;
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 算法入口
     *
     * @param initData 所有库存的hashMap组合
     * @return 拆分所有组合产生的所有情况（生成客户端自己的字典）
     */
    public static Map<String, ? extends SkuModel> skuCollection(Map<String, ? extends SkuModel> initData) {
        //用户返回数据
        HashMap<String, SkuModel> result = new HashMap<>();
        // 遍历所有库存
        for (String subKey : initData.keySet()) {
            SkuModel skuModel = initData.get(subKey);
            //根据；拆分key的组合
            String[] skuKeyAttrs = subKey.split(";");

            //获取所有的组合
            ArrayList<ArrayList<String>> combArr = combInArray(skuKeyAttrs);

            // 对应所有组合添加到结果集里面
            for (int i = 0; i < combArr.size(); i++) {
                add2SKUResult(result, combArr.get(i), skuModel);
            }

            // 将原始的库存组合也添加进入结果集里面
            String key = TextUtils.join(";", skuKeyAttrs);
            result.put(key, skuModel);
        }
        return result;
    }

    /**
     * 获取所有的组合放到ArrayList里面
     *
     * @param skuKeyAttrs 单个key被； 拆分的数组
     * @return ArrayList
     */
    private static ArrayList<ArrayList<String>> combInArray(String[] skuKeyAttrs) {
        if (skuKeyAttrs == null || skuKeyAttrs.length <= 0)
            return null;
        int len = skuKeyAttrs.length;
        ArrayList<ArrayList<String>> aResult = new ArrayList<>();
        for (int n = 1; n < len; n++) {
            ArrayList<Integer[]> aaFlags = getCombFlags(len, n);
            for (int i = 0; i < aaFlags.size(); i++) {
                Integer[] aFlag = aaFlags.get(i);
                ArrayList<String> aComb = new ArrayList<>();
                for (int j = 0; j < aFlag.length; j++) {
                    if (aFlag[j] == 1) {
                        aComb.add(skuKeyAttrs[j]);
                    }
                }
                aResult.add(aComb);
            }
        }
        return aResult;
    }

    /**
     * 算法拆分组合 用1和0 的移位去做控制
     * （这块需要你打印才能看的出来）
     *
     * @param len
     * @param n
     * @return
     */
    private static ArrayList<Integer[]> getCombFlags(int len, int n) {
        if (n <= 0) {
            return new ArrayList<>();
        }
        ArrayList<Integer[]> aResult = new ArrayList<>();
        Integer[] aFlag = new Integer[len];
        boolean bNext = true;
        int iCnt1 = 0;
        //初始化
        for (int i = 0; i < len; i++) {
            aFlag[i] = i < n ? 1 : 0;
        }
        aResult.add(aFlag.clone());
        while (bNext) {
            iCnt1 = 0;
            for (int i = 0; i < len - 1; i++) {
                if (aFlag[i] == 1 && aFlag[i + 1] == 0) {
                    for (int j = 0; j < i; j++) {
                        aFlag[j] = j < iCnt1 ? 1 : 0;
                    }
                    aFlag[i] = 0;
                    aFlag[i + 1] = 1;
                    Integer[] aTmp = aFlag.clone();
                    aResult.add(aTmp);
                    if (!TextUtils.join("", aTmp).substring(len - n).contains("0")) {
                        bNext = false;
                    }
                    break;
                }
                if (aFlag[i] == 1) {
                    iCnt1++;
                }
            }
        }
        return aResult;
    }

    /**
     * 添加到数据集合
     *
     * @param result
     * @param newKeyList
     * @param skuModel
     */
    private static void add2SKUResult(HashMap<String, SkuModel> result, ArrayList<String> newKeyList, SkuModel skuModel) {
        String key = TextUtils.join(";", newKeyList);
        if (result.keySet().contains(key)) {
            result.get(key).stock = result.get(key).stock + skuModel.stock;
        } else {
            result.put(key, new SkuModel(skuModel.stock,skuModel.imageUrl,skuModel.price));
        }
    }
}
