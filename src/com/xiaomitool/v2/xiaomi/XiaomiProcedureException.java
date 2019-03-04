package com.xiaomitool.v2.xiaomi;

public class XiaomiProcedureException extends Exception {
    ExceptionCode code;

    public static  enum ExceptionCode {
        EXCEPTION,
        NEED_LOGIN,
        CONNECTION_ERROR
    }
    public XiaomiProcedureException(String msg){
        this(msg,ExceptionCode.EXCEPTION);
    }
    public XiaomiProcedureException(String msg, ExceptionCode code ){
        super(msg);
        this.code = code;
    }
    public ExceptionCode getCode() {
        return code;
    }

}
