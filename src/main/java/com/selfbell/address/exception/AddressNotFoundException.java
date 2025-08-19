package com.selfbell.address.exception;

import com.selfbell.global.error.ApiException;
import com.selfbell.global.error.ErrorCode;

public class AddressNotFoundException extends ApiException {
    public AddressNotFoundException(ErrorCode errorCode, String overrideMessage) {
        super(errorCode, overrideMessage);
    }
}
