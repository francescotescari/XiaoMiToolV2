package com.xiaomitool.v2.xiaomi;

public class XiaomiProcedureException extends Exception {
    ExceptionCode code;
    private String additional;

    public XiaomiProcedureException(String msg) {
        this(msg, ExceptionCode.EXCEPTION);
    }

    public XiaomiProcedureException(String msg, ExceptionCode code) {
        super(msg);
        this.code = code;
    }

    public XiaomiProcedureException(String msg, ExceptionCode code, String additional) {
        this(msg, code);
        this.additional = additional;
    }

    public ExceptionCode getCode() {
        return code;
    }

    public String getAdditional() {
        return additional;
    }

    public enum ExceptionCode {
        EXCEPTION,
        NEED_LOGIN,
        CONNECTION_ERROR,
        NOT_ALLOWED
    }
}
