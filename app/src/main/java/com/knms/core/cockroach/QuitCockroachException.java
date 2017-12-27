package com.knms.core.cockroach;

/**
 * Created by Administrator on 2017/2/22.
 */
final class QuitCockroachException extends RuntimeException {
    public QuitCockroachException(String message) {
        super(message);
    }
}
