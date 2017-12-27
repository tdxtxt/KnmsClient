package com.knms.core.location;

import android.text.TextUtils;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.other.City;
import com.knms.net.RxRequestApi;

import java.util.List;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func2;


/**
 * Created by tdx on 2016/10/20.
 */

public class LocationHelper {
    LocationClient locationClient;
    /**
     * 单例
     */
    public static LocationHelper getInstance() {
        return InstanceHolder.instance;
    }


    static class InstanceHolder {
        final static LocationHelper instance = new LocationHelper();
    }

    private LocationHelper(){
        locationClient = new LocationClient(KnmsApp.getInstance());
        initLocation(locationClient);
    }
   public Observable<City> startLocation(){
        return Observable.zip(RxRequestApi.getInstance().getApiService().getCitys(), Observable.create(new LocationOnSubscribe(locationClient)), new Func2<ResponseBody<List<City>>, BDLocation, City>() {
            @Override
            public City call(ResponseBody<List<City>> body, BDLocation location) {
                City temp = null;
                String cityName = location.getCity();
                String district = location.getDistrict();
                if(TextUtils.isEmpty(cityName)){
                   return temp;
                }
                if(TextUtils.isEmpty(district)){
                    return temp;
                }
                if(body.isSuccess() && body.data != null && body.data.size() > 0){
                    for (City item: body.data) {
                        if(item.name.contains(cityName) || cityName.contains(item.name)){
                            temp = item;
                            for (City subCity: item.subCitys) {
                                if(subCity.name.contains(district) || district.contains(subCity.name)){
                                    temp = subCity;
                                    temp.name=item.name+temp.name;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
                return temp;
            }
        }).doOnError(new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {}
        });
    }
    private void initLocation(LocationClient locationClient){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        locationClient.setLocOption(option);
    }
}
