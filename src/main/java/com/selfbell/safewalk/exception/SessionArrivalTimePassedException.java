package com.selfbell.safewalk.exception;

import com.selfbell.global.error.ApiException;
import com.selfbell.global.error.ErrorCode;

public class SessionArrivalTimePassedException extends ApiException {
    public SessionArrivalTimePassedException(String s) {
        super(ErrorCode.SESSION_ARRIVAL_TIME_PASSED, s);
    }
}
