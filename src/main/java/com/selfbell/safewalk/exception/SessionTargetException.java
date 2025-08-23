package com.selfbell.safewalk.exception;

public class SessionTargetException extends IllegalArgumentException {
    public SessionTargetException(String s) {
        super("Target은 \"me\" 혹은 \"ward\"만 가능합니다. 현재 Target:" + s);
    }
}
