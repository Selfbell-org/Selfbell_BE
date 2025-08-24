package com.selfbell.address.exception;

import com.selfbell.global.error.ApiException;
import com.selfbell.global.error.ErrorCode;

public class AddressUnauthorizedException extends ApiException {
    public AddressUnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
