package com.howcode.aichat.core.domain.enums;

/** 通用开关。 */
public enum Toggle {
    OPEN(1), CLOSE(0);

    private final int code;

    Toggle(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
