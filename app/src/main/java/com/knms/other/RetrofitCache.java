package com.knms.other;

import java.io.Serializable;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by tdx on 2017/1/9.
 */

public class RetrofitCache {
    /**
     * @param cacheKey 缓存的Key
     * @param fromNetwork
     * @param <T>
     * @return
     */
    public static <T> Observable<T> load(final String cacheKey,
                                         Observable<T> fromNetwork) {
        return fromNetwork.map(new Func1<T, T>() {
            @Override
            public T call(T result) {
                CacheManager.saveObject((Serializable) result, cacheKey);
                return result;
            }
        }).onErrorResumeNext(new Func1<Throwable, Observable<? extends T>>() {
            @Override
            public Observable<? extends T> call(Throwable throwable) {
                return Observable.unsafeCreate(new Observable.OnSubscribe<T>() {
                    @Override
                    public void call(Subscriber<? super T> subscriber) {
                        T cache = (T) CacheManager.readObject(cacheKey, 0);
                        if (cache != null) {
                            subscriber.onNext(cache);
                            subscriber.onCompleted();
                        } else {
                            subscriber.onCompleted();
                        }
                    }
                });
            }
        });
    }
}
