package com.knms.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * Created by Administrator on 2016/11/29.
 */

public class ToolsHelper {
    /**
     * 单例
     */
    public static ToolsHelper getInstance() {
        return InstanceHolder.instance;
    }
    static class InstanceHolder {
        final static ToolsHelper instance = new ToolsHelper();
    }

    public <T> void sort(List<T> data, final String sortField){
        sort(data,sortField,false);
    }
    /**
     * 排序
     */
    public <T> void sort(List<T> data, final String sortField, final boolean isASC){
        if(data == null || (data != null && data.size() < 2)) return;
        Collections.sort(data, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                try {
                    Class clazz = o1.getClass();
                    Field field = clazz.getDeclaredField(sortField); //获取成员变量
                    field.setAccessible(true); //设置成可访问状态
                    String typeName = field.getType().getName().toLowerCase(); //转换成小写
                    Object v1 = field.get(o1); //获取field的值
                    Object v2 = field.get(o2); //获取field的值
                    //判断字段数据类型，并比较大小
                    if(typeName.endsWith("string")) {
                        String value1 = v1.toString();
                        String value2 = v2.toString();
                        int order1 = 0;
                        int order2 = 0;
                        try {
                            order1 = Integer.parseInt(value1);
                            order2 = Integer.parseInt(value2);
                        } catch (NumberFormatException e) {
//                            e.printStackTrace();
                        }
                        return isASC ? (order1 > order2 ? 1 : -1) : (order1 < order2 ? 1 : -1);
                    }else if(typeName.endsWith("int") || typeName.endsWith("integer")) {
                        Integer value1 = Integer.parseInt(v1.toString());
                        Integer value2 = Integer.parseInt(v2.toString());
                        return  isASC ? value1.compareTo(value2) : (value2.compareTo(value1));
                    }else if(typeName.endsWith("long")) {
                        Long value1 = Long.parseLong(v1.toString());
                        Long value2 = Long.parseLong(v2.toString());
                        return  isASC ? value1.compareTo(value2) : (value2.compareTo(value1));
                    }
                    else if(typeName.endsWith("float")) {
                        Float value1 = Float.parseFloat(v1.toString());
                        Float value2 = Float.parseFloat(v2.toString());
                        return  isASC ? value1.compareTo(value2) : (value2.compareTo(value1));
                    }
                    else if(typeName.endsWith("double")) {
                        Double value1 = Double.parseDouble(v1.toString());
                        Double value2 = Double.parseDouble(v2.toString());
                        return  isASC ? value1.compareTo(value2) : (value2.compareTo(value1));
                    }else if(typeName.endsWith("date")) {
                        Date value1 = (Date)(v1);
                        Date value2 = (Date)(v2);
                        return  isASC ? value1.compareTo(value2) : (value2.compareTo(value1));
                    }else {
                        //调用对象的compareTo()方法比较大小
                        Method method = field.getType().getDeclaredMethod("compareTo", new Class[]{field.getType()});
                        method.setAccessible(true); //设置可访问权限
                        int result  =  isASC ?  (Integer)method.invoke(v2, new Object[]{v1}) : (Integer)method.invoke(v1, new Object[]{v2});
                        return result;
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
                return 0; //未知类型，无法比较大小
            }
        });
    }
    public String acckey(String k_ex){
        String k_cc = "";
        if (k_ex != null) {
            if (!"".equals(k_ex)) {
                if (k_ex.length() >= 2) {
                    String mode = k_ex.substring(0, 2);
                    String cctmp = k_ex.substring(2, k_ex.length());
                    //加密模板  代码 00
                    if ("00".equals(mode) && !"".equals(cctmp)) {
                        k_cc = encryption00(cctmp);
                        k_cc = mode + k_cc;
                    }
                }
            }
        }
        return k_cc;
    }
    private String encryption00(String  k_ex){
        String k_cc = "";
        String keytmp = "123456hijklmnopqrstuvwxyzABCDEF7890abcdefgGHIJKLMNOPQRSTUVWXYZ";
        char[] keys = keytmp.toCharArray();
        int keysint = keys.length;
        char[] k_exs = k_ex.toCharArray();
        for (char c : k_exs) {
            for (int i = 0; i < keysint; i++) {
                if (c == keys[i]) {
                    k_cc += i;
                    break;
                }
            }
        }
        return k_cc;
    }
}
