package com.selfbell.sos.exception;

import com.selfbell.global.error.ApiException;
import com.selfbell.global.error.ErrorCode;

public class SosMessageEmptyException extends ApiException {
    public SosMessageEmptyException() {
        super(ErrorCode.SOS_MESSAGE_EMPTY);
    }
}
